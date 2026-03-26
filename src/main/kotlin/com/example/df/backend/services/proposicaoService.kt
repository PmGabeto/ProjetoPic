package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.*
import com.example.df.backend.enums.TipoProjetoLei
import com.example.df.backend.integrations.cldf.CldfInterface
import com.example.df.backend.integrations.cldf.ProposicaoCldfBaseDTO
import com.example.df.backend.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
@Suppress("unused")
open class ProposicaoService(
    private val proposicaoRepo: ProposicaoRepository,
    private val temaRepo: TemaRepository,
    private val politicoRepo: PoliticoRepository,
    private val autoriaRepo: AutoriaRepository,
    private val raRepo: RegiaoAdministrativaRepository,
    private val cldfIntegration: CldfInterface,
    private val historicoRepo: HistoricoRepository,
    private val documentoRepo: DocumentoRepository
) {
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private lateinit var self: ProposicaoService
    private val logger = LoggerFactory.getLogger(ProposicaoService::class.java)

    @Volatile
    var varreduraAtiva: Boolean = false

    fun pararVarredura() {
        varreduraAtiva = false
        logger.info("🛑 Sinal de parada manual enviado para a varredura!")
    }

    fun sincronizarCargaTotal(filtros: Map<String, Any>, paginaInicial: Int = 0) {
        varreduraAtiva = true
        var pagina = paginaInicial
        val tamanho = 50 // A CLDF costuma aceitar bem lotes de 20 a 50

        // 1. Mudamos o log para mostrar os filtros dinâmicos em vez de apenas o ano
        logger.info("🚀 Iniciando varredura CLDF com filtros $filtros a partir da página $pagina")

        while (varreduraAtiva) {

            // 2. Passamos o mapa 'filtros' inteiro para a integração
            val listaBase = cldfIntegration.varrerProposicoesRecentes(filtros, pagina, tamanho)

            if (listaBase.isEmpty()) {
                // 3. Atualizamos o log de finalização
                logger.info("🏁 Nenhuma proposição encontrada na página $pagina. Fim da varredura para os filtros $filtros.")
                break
            }

            for (baseDto in listaBase) {
                if (!varreduraAtiva) break
                try {
                    self.processarE_SalvarProposicao(baseDto)
                } catch (e: Exception) {
                    logger.error("❌ Erro ao salvar proposicao ${baseDto.publicId}: ${e.message}")
                }
            }
            pagina++
        }

        varreduraAtiva = false
        logger.info("✅ Processo de varredura finalizado!")
    }

    @Transactional
    open fun processarE_SalvarProposicao(baseDto: ProposicaoCldfBaseDTO) {
        // Usa o exato nome mapeado no seu DTO Base
        val publicIdStr = baseDto.publicId ?: return

        if (proposicaoRepo.existsByPublicId(publicIdStr)) {
            logger.info("⏭️ Proposição $publicIdStr já existe no banco. Pulando...")
            return
        }

        // Busca Detalhes Completos da API
        val detalhes = cldfIntegration.buscarDetalhesCompletos(publicIdStr) ?: return
        val propCompleta = detalhes.proposicao
        val listaHistorico = detalhes.historico

        // 1. Processar RAs (Usando a lista exata do DTO base com erro de digitação proposital da API e IDs do Completo)
        val rasSincronizadas = processarRasDinamicamente(
            nomesRas = baseDto.regiaoAdiminstrativaNomeLista,
            idsRas = propCompleta?.regiaoAdministrativa
        )

        // 2. Montar a Entidade Principal Proposição
        val proposicao = Proposicao(
            publicId = publicIdStr,
            tipo = propCompleta?.tipoProposicao?.sigla?.let { converterSiglaEnum(it) } ?: TipoProjetoLei.PL,
            numeroProcesso = baseDto.siglaNumeroAno ?: "S/N",
            titulo = baseDto.siglaNumeroAno ?: propCompleta?.siglaNumero ?: "Sem título",
            ementa = baseDto.ementa,
            numeroDefinitivo = propCompleta?.numeroDefinitivo,
            statusTramitacao = propCompleta?.statusTramitacao ?: baseDto.etapa,
            regimeUrgencia = propCompleta?.regimeUrgencia ?: false,
            excluido = propCompleta?.excluido ?: false,
            dataApresentacao = baseDto.dataCadastro ?: parseLocalDateSeguro(propCompleta?.dataCadastro) ?: LocalDate.now(),
            idUnidadeGeradora = propCompleta?.idUnidadeGeradora,
            linkCompleto = "https://ple.cl.df.gov.br/#/proposicao/$publicIdStr/consultar",
            regioesAdministrativas = rasSincronizadas.toMutableSet()
        )

        val proposicaoSalva = proposicaoRepo.save(proposicao)

        // 3. Vincular Temas (A API retorna IDs como List<Int> em temasIds)
        val temasIdsNumericos = propCompleta?.temasIds
        if (!temasIdsNumericos.isNullOrEmpty()) {
            val temasProcessados = cldfIntegration.processarTemasProposicao(temasIdsNumericos)
            proposicaoSalva.temas.addAll(temasProcessados)
            proposicaoRepo.save(proposicaoSalva)
        }

        // 4. Vincular Autores
        val listaAutoresDto = propCompleta?.autores

        if (!listaAutoresDto.isNullOrEmpty()) {

            // 2. Iteramos sobre cada autor retornado no JSON
            listaAutoresDto.forEach { autorDto ->
                val nomeCru = autorDto.nome

                // Se por algum motivo bizarro a API mandar um nome nulo, a gente pula para o próximo
                if (nomeCru.isNullOrBlank()) return@forEach

                // 3. Limpamos o título (Deputado, Dra, Pastor, etc)
                val nomeLimpo = nomeCru.replace(
                    Regex("^(Deputado|Deputada|Pastor|Pastora|Doutor|Doutora|Dr\\.|Dra\\.)\\s+", RegexOption.IGNORE_CASE),
                    ""
                ).trim()

                // 4. Procuramos o político no banco pelo nome limpo
                val politico = politicoRepo.findByNomeUrnaContainingIgnoreCase(nomeLimpo).firstOrNull()

                if (politico != null) {
                    // 5. Criamos o vínculo na tabela de Autoria
                    val novaAutoria = Autoria(
                        proposicao = proposicaoSalva,
                        politico = politico,
                        // Usamos o tipo de autor que vem da API (Ex: "PARLAMENTAR") ou "AUTOR" como garantia
                        tipoVinculacao = autorDto.tipoAutor ?: "AUTOR"
                    )
                    autoriaRepo.save(novaAutoria)
                    logger.info("✅ Autoria vinculada com sucesso: ${politico} (${novaAutoria.tipoVinculacao})")
                } else {
                    logger.warn("⚠️ Político não encontrado no banco para vincular autoria: '$nomeLimpo' (Original: '$nomeCru')")
                }
            }
        } else {
            logger.info("ℹ️ Proposição ${proposicaoSalva.publicId} sem lista de autores no detalhamento.")
        }

        // 5. Salvar Histórico (Embutido no DetalhesDTO, lendo com os nomes EXATOS do HistoricoCldfDTO)
        listaHistorico.forEach { histDto ->
            if (histDto.dataHistorico != null && histDto.acao != null) {

                val historico = ProposicaoHistorico(
                    publicId = histDto.publicId ?: UUID.randomUUID().toString(),
                    dataEvento = histDto.dataHistorico,
                    faseTramitacao = histDto.acao,
                    unidadeResponsavel = histDto.nomeUnidade ?: histDto.sigla ?: "CLDF",
                    descricao = histDto.descricao,
                    projeto = proposicaoSalva
                )
                historicoRepo.save(historico)
            }
        }

        // 6. Salvar Documentos (Usando o DocumentoCldfDTO com os nomes idArquivo e validoDesde)
        val documentosDto = cldfIntegration.buscarDocumentos(publicIdStr)
        documentosDto.forEach { docDto ->
            val docPublicId = docDto.idArquivo
            val autorLimpoDoDocumento = docDto.autoria?.replace(
                Regex("^(Deputado|Deputada|Pastor|Pastora|Doutor|Doutora|Dr\\.|Dra\\.)\\s+", RegexOption.IGNORE_CASE), ""
            )?.trim()
            if (docPublicId != null) {
                val doc = DocumentosArquivos(
                    publicId = docPublicId,
                    tipoDocumento = docDto.nome ?: "DOCUMENTO",
                    nomeExibicao = docDto.nome ?: "Documento da Proposição",
                    nomeStorage = null,
                    linkDireto = "/api/proposicoes/$publicIdStr/documentos/$docPublicId/pdf",
                    tipoRelacionado = "PROPOSICAO",
                    idRelacionado = proposicaoSalva.id!!, // Garante que é Long
                    validoDesde = docDto.validoDesde ?: LocalDateTime.now(),
                    dataCadastro = docDto.dataDocumento,
                    autor = autorLimpoDoDocumento
                )
                documentoRepo.save(doc)
            }
        }

        logger.info("✅ Proposição $publicIdStr salva com sucesso (Temas, RAs, Autores, Histórico e Documentos)!")
    }
    fun baixarPdfDocumento(idProposicao: String, idDocumento: String): ByteArray? {
        // Repassa a chamada para a sua integração que já faz o POST na CLDF
        return cldfIntegration.baixarDocumentos(idProposicao, idDocumento)
    }
    fun buscarHtmlDocumento(idProposicao: String, idDocumento: String): String? {
        return cldfIntegration.buscarHtmlDocumento(idProposicao, idDocumento)
    }
    // =========================================================================
    // LÓGICA DINÂMICA DE REGIÕES ADMINISTRATIVAS
    // =========================================================================
    private fun processarRasDinamicamente(nomesRas: List<String>, idsRas: List<Int>?): List<RegiaoAdministrativa> {
        val listaFinal = mutableSetOf<RegiaoAdministrativa>()

        // 1. Extrai, limpa as hashtags e remove referências genéricas ao DF
        val nomesLimpos = nomesRas.flatMap { it.split("#") }
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.contains("DISTRITO FEDERAL (INTEIRO)") }

        val tamanho = maxOf(nomesLimpos.size, idsRas?.size ?: 0)

        for (i in 0 until tamanho) {
            val nomeDaRa = nomesLimpos.getOrNull(i)
            val idDaApi = idsRas?.getOrNull(i)?.toString()

            if (nomeDaRa == null && idDaApi == null) continue

            var raExistente: RegiaoAdministrativa? = null

            if (idDaApi != null) raExistente = raRepo.findByPublicId(idDaApi)
            if (raExistente == null && nomeDaRa != null) raExistente = raRepo.findByNomeContainingIgnoreCase(nomeDaRa)

            if (raExistente != null) {
                if (raExistente.publicId == null && idDaApi != null) {
                    raExistente.publicId = idDaApi
                    raExistente = raRepo.save(raExistente)
                }
                listaFinal.add(raExistente)
            } else if (nomeDaRa != null) {
                val novaRa = RegiaoAdministrativa(
                    publicId = idDaApi,
                    nome = nomeDaRa
                )
                listaFinal.add(raRepo.save(novaRa))
                logger.info("🆕 Nova RA cadastrada dinamicamente: ${novaRa.nome} (ID: $idDaApi)")
            }
        }

        return listaFinal.toList()
    }
    // =========================================================================
    // 1. LISTAGEM GERAL
    // =========================================================================
    @Transactional(readOnly = true)
    fun listarTodas(): List<ProposicaoResumoDTO> {
        val proposicoes = proposicaoRepo.findAll()

        return proposicoes.map { p ->
            ProposicaoResumoDTO(
                id = p.id!!,
                publicId = p.publicId,
                tipo = TipoProposicaoDTO(
                    sigla = p.tipo.name,
                    nome = p.tipo.name,
                    //descricaoPedagogica = "Proposição legislativa"
                ),
                numero = p.numeroProcesso, // Vindo exato da sua entidade Proposicao
                titulo = p.titulo,         // Vindo exato da sua entidade Proposicao
                ementa = p.ementa,
                status = p.statusTramitacao ?: "TRAMITANDO",
                data = p.dataApresentacao, // LocalDate direto da entidade
                tema = p.temas.map { TemaDTO(id = it.id, nome = it.nome) }, // 'temas' da entidade
                linkCompleto = p.linkCompleto,
                autores = p.autores.map { it.politico.nomeUrna }, // Pega só o nome de cada autor
                regioesAdministrativas = p.regioesAdministrativas.map { it.nome } // Pega só o nome da RA
            )
        }
    }

    // =========================================================================
    // 2. BUSCAR DETALHES
    // =========================================================================
    @Transactional(readOnly = true)
    fun buscarDetalhe(id: Long): ProposicaoDetalheDTO {
        val p = proposicaoRepo.findById(id)
            .orElseThrow { NoSuchElementException("Proposição com ID $id não encontrada.") }

        // Como Autores estão mapeados na Entidade via @OneToMany, não precisamos do AutoriaRepo aqui.
        // Precisamos buscar apenas os documentos fisicos/virtuais no DocumentoRepo
        val documentosBanco = documentoRepo.findByTipoRelacionadoAndIdRelacionado("PROPOSICAO", id)

        // Se você tiver o repositório de histórico, chame-o aqui (ou use p.historicos se tiver mapeado @OneToMany)
        val historicoBanco = historicoRepo.findByProjetoIdOrderByDataEventoDesc(id)

        return ProposicaoDetalheDTO(
            id = p.id!!,
            publicId = p.publicId,
            tipo = TipoProposicaoDTO(
                sigla = p.tipo.name,
                nome = p.tipo.name,
               // descricaoPedagogica = "Proposição legislativa detalhada"
            ),
            numeroProcesso = p.numeroProcesso,
            numeroDefinitivo = p.numeroDefinitivo,
            titulo = p.titulo,
            ementa = p.ementa,
            statusTramitacao = p.statusTramitacao ?: "TRAMITANDO",
            regiaoAdministrativa = p.regioesAdministrativas.map { it.nome }, // Puxa do seu Set de RegiaoAdministrativa
            regimeUrgencia = p.regimeUrgencia,
            dataApresentacao = p.dataApresentacao,
            dataLimite = p.dataLimite,
            tema = p.temas.map { TemaDTO(id = it.id, nome = it.nome) }, // 'temas' da entidade
            linkCompleto = p.linkCompleto,

            // Documentos com os campos corretos da Entidade DocumentosArquivos
            documentos = documentosBanco.map { d ->
                DocumentoResponseDTO(
                    id = d.id!!,
                    publicId = d.publicId,
                    tipoRelacionado = d.tipoRelacionado,
                    nomeExibicao = d.nomeExibicao,
                    tipoDocumento = d.tipoDocumento,
                    urlDownload = if (d.nomeStorage != null) "/api/documentos/v/${d.publicId}" else null,
                    extensao = d.nomeStorage?.substringAfterLast(".", "pdf") ?: "pdf",
                    dataCadastro = d.dataCadastro ?: LocalDateTime.now(),
                    validoDesde = d.validoDesde ?: LocalDateTime.now(),
                    autor = d.autor
                )
            },

            // Históricos puxando da entidade ProposicaoHistorico
            historicos = historicoBanco.map { h ->
                HistoricoDTO(
                    data = h.dataEvento, // Converte LocalDateTime para LocalDate
                    fase = h.faseTramitacao,
                    unidadeResponsavel = h.unidadeResponsavel,
                    descricao = h.descricao ?: ""
                )
            },

            // Autores puxando direto da relação p.autores
            autores = p.autores.map { a ->
                PoliticoResumoDTO(
                    id = a.politico.id!!,
                    publicId = a.politico.publicId,
                    nome = a.politico.nomeUrna ?: a.politico.nomeCompleto,
                    partido = a.politico.partidoAtual,
                    status = a.politico.status,
                    tipoAutor = a.politico.tipoAutor,
                    foto = a.politico.urlFoto

                )
            }
        )
    }

    // =========================================================================
    // 3. ADICIONAR TRAMITAÇÃO MANUAL
    // =========================================================================

    @Transactional
    fun adicionarHistorico(id: Long, dto: NovoHistoricoDTO): ProposicaoDetalheDTO {
        val p = proposicaoRepo.findById(id)
            .orElseThrow { NoSuchElementException("Proposição com ID $id não encontrada.") }

        // Cria a entidade com os nomes EXATOS da sua classe ProposicaoHistorico
        val novoHistorico = ProposicaoHistorico(
            publicId = java.util.UUID.randomUUID().toString(), // Obrigatorio pelo unique=true
            dataEvento = dto.dataEvento.atStartOfDay(), // Converte LocalDate do DTO para LocalDateTime da Entidade
            faseTramitacao = dto.faseTramitacao,
            unidadeResponsavel = dto.unidadeResponsavel,
            descricao = dto.descricao,
            projeto = p // A referência à entidade Proposicao
        )
        historicoRepo.save(novoHistorico)

        // Se o DTO mandar atualizar o status principal da proposição
        if (dto.atualizarStatusDaProposicao) {
            p.statusTramitacao = dto.faseTramitacao
            proposicaoRepo.save(p)
        }

        return buscarDetalhe(id)
    }

    // =========================================================================
    // 4. ATUALIZAR TEMAS (VINCULAÇÃO MANUAL)
    // =========================================================================

    @Transactional
    fun atualizarTemas(id: Long, novosTemasIds: List<Long>): ProposicaoDetalheDTO {
        val p = proposicaoRepo.findById(id)
            .orElseThrow { NoSuchElementException("Proposição com ID $id não encontrada.") }

        val novosTemasEntity = temaRepo.findAllById(novosTemasIds)

        // Limpa a lista atual e injeta os novos (usando a variável 'temas' da sua entidade)
        p.temas.clear()
        p.temas.addAll(novosTemasEntity)
        proposicaoRepo.save(p)

        return buscarDetalhe(id)
    }

    // =========================================================================
    // PARSERS E UTILITÁRIOS
    // =========================================================================

    private fun parseLocalDateSeguro(dataStr: String?): LocalDate? {
        if (dataStr.isNullOrBlank()) return null
        return try {
            // Pega apenas a parte da data caso a API mande com horário (Ex: "2021-03-12T11:23:58")
            val apenasData = if (dataStr.contains("T")) dataStr.substringBefore("T") else dataStr
            LocalDate.parse(apenasData)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseLocalDateTimeSeguro(dataStr: String?): LocalDateTime? {
        if (dataStr.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(dataStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }

    private fun converterSiglaEnum(sigla: String): TipoProjetoLei {
        return try {
            TipoProjetoLei.valueOf(sigla.uppercase())
        } catch (e: Exception) {
            TipoProjetoLei.PL // Fallback caso seja uma sigla que não existe no Enum
        }
    }


}