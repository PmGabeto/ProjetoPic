package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.*
import com.example.df.backend.enums.StatusPolitico
import com.example.df.backend.enums.TipoAutor
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
import org.springframework.scheduling.annotation.Scheduled

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
    private val politicosPendentesDeCadastro = mutableSetOf<String>()

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
                    self.processareSalvarproposicao(baseDto)
                } catch (e: Exception) {
                    logger.error("❌ Erro ao salvar proposicao ${baseDto.publicId}: ${e.message}")
                }
            }
            pagina++
        }

        varreduraAtiva = false
        logger.info("✅ Processo de varredura finalizado!")
    }


    // =========================================================================
    // AGENDAMENTO AUTOMÁTICO (Robô da Madrugada)
    // =========================================================================

    @Scheduled(cron = "0 0 2 * * *", zone = "America/Sao_Paulo")
    fun sincronizacaoNoturnaAutomatica() {
        if (varreduraAtiva) {
            logger.warn("⚠️ A varredura já está em andamento. Ignorando o agendamento desta noite.")
            return
        }

        logger.info("🌙 Iniciando varredura agendada da madrugada (Anos: 2022 a 2026)...")
        varreduraAtiva = true

        val anosAlvo = listOf(2022, 2023, 2024, 2025, 2026)
        val tamanho = 50

        try {
            for (ano in anosAlvo) {
                if (!varreduraAtiva) {
                    logger.info("🛑 Varredura noturna interrompida manualmente.")
                    break
                }

                logger.info("📅 Buscando carga de proposições do ano: $ano")

                var pagina = 0
                // Cria o filtro dinâmico exatamente como a API espera
                val filtros = mapOf("ano" to ano)

                // Faz a paginação para aquele ano
                while (varreduraAtiva) {
                    // Chama a função real que existe na sua integração!
                    val listaBase = cldfIntegration.varrerProposicoesRecentes(filtros, pagina, tamanho)

                    if (listaBase.isEmpty()) {
                        logger.info("🏁 Nenhuma proposição encontrada na página $pagina para o ano $ano. Indo para o próximo ano.")
                        break // Sai do 'while' (acabaram as páginas deste ano) e vai para o próximo 'for' (próximo ano)
                    }

                    for (dto in listaBase) {
                        if (!varreduraAtiva) break

                        try {
                            self.processareSalvarproposicao(dto)
                        } catch (e: Exception) {
                            logger.error("❌ Erro ao processar a proposição ${dto.publicId}: ${e.message}")
                        }
                    }

                    pagina++

                    // Respiro de 2 segundos para não tomar bloqueio de IP da CLDF
                    Thread.sleep(2000)
                }
            }
        } catch (e: Exception) {
            logger.error("🚨 Erro fatal durante a varredura agendada: ${e.message}", e)
        } finally {
            varreduraAtiva = false
            logger.info("☀️ Varredura noturna finalizada com sucesso! O robô vai voltar a dormir.")
            if (politicosPendentesDeCadastro.isNotEmpty()) {
                // \u001B[41m = Fundo Vermelho | \u001B[37m = Texto Branco | \u001B[1m = Negrito | \u001B[0m = Reseta Cor
                logger.warn("\n\u001B[41m\u001B[37m\u001B[1m ============================================================================== \u001B[0m")
                logger.warn("\u001B[41m\u001B[37m\u001B[1m 🚨 ALERTA DE SISTEMA: ${politicosPendentesDeCadastro.size} NOVAS ENTIDADES POLÍTICAS FORAM CADASTRADAS! 🚨 \u001B[0m")
                logger.warn("\u001B[41m\u001B[37m\u001B[1m ============================================================================== \u001B[0m")
                logger.warn("\u001B[41m\u001B[37m\u001B[1m O robô criou perfis básicos baseados nos dados da CLDF.                        \u001B[0m")
                logger.warn("\u001B[41m\u001B[37m\u001B[1m Favor acessar o painel de administração e completar o cadastro de:             \u001B[0m")

                politicosPendentesDeCadastro.forEach { nomeAlerta ->
                    logger.warn("\u001B[41m\u001B[37m\u001B[1m -> $nomeAlerta \u001B[0m")
                }

                logger.warn("\u001B[41m\u001B[37m\u001B[1m ============================================================================== \u001B[0m\n")

                // Limpa a lista para a próxima madrugada não repetir os nomes velhos
                politicosPendentesDeCadastro.clear()
            }
        }
    }
    // =========================================================================
    // O "MAESTRO" - Decide se vai Atualizar ou Inserir
    // =========================================================================
    @Transactional
    open fun processareSalvarproposicao(baseDto: ProposicaoCldfBaseDTO) {
        val publicIdStr = baseDto.publicId ?: return

        // 1. Em vez de usar existsByPublicId, já trazemos a entidade para poder atualizar
        val proposicaoExistente = proposicaoRepo.findByPublicId(publicIdStr)

        if (proposicaoExistente != null) {
            // Se já existe, joga para a função especialista em UPDATE
            atualizarProposicaoExistente(proposicaoExistente, baseDto, publicIdStr)
        } else {
            // Se não existe, joga para a função especialista em INSERT
            inserirNovaProposicao(baseDto, publicIdStr)
        }
    }

    // =========================================================================
    // LÓGICA DE UPDATE (Sincronização de Diferenças)
    // =========================================================================
    private fun atualizarProposicaoExistente(proposicaoExistente: Proposicao, baseDto: ProposicaoCldfBaseDTO, publicIdStr: String) {
        logger.info("🔄 Proposição $publicIdStr já existe. A verificar atualizações...")
        var teveAtualizacao = false

        val detalhes = cldfIntegration.buscarDetalhesCompletos(publicIdStr) ?: return
        val propCompleta = detalhes.proposicao
        val listaHistorico = detalhes.historico

        // 1. VERIFICA MUDANÇA DE STATUS / URGÊNCIA
        val novoStatus = propCompleta?.statusTramitacao ?: baseDto.etapa
        if (proposicaoExistente.statusTramitacao != novoStatus) {
            logger.info("📉 Status da proposição $publicIdStr mudou para '$novoStatus'")
            proposicaoExistente.statusTramitacao = novoStatus
            teveAtualizacao = true
        }

        if (propCompleta?.regimeUrgencia != null && proposicaoExistente.regimeUrgencia != propCompleta.regimeUrgencia) {
            proposicaoExistente.regimeUrgencia = propCompleta.regimeUrgencia
            teveAtualizacao = true
        }

        if (teveAtualizacao) proposicaoRepo.save(proposicaoExistente)

        // 2. VERIFICA NOVOS HISTÓRICOS (Comparando pela DATA exata)
        val historicosBanco = historicoRepo.findByProjetoIdOrderByDataEventoDesc(proposicaoExistente.id!!)
        val datasHistoricoBanco = historicosBanco.map { it.dataEvento }.toSet()

        listaHistorico.forEach { histDto ->
            val dataDoHistoricoDaApi = histDto.dataHistorico

            if (dataDoHistoricoDaApi != null && !datasHistoricoBanco.contains(dataDoHistoricoDaApi)) {
                val historico = ProposicaoHistorico(
                    publicId = histDto.publicId ?: UUID.randomUUID().toString(), // Gera UUID se vier nulo
                    dataEvento = dataDoHistoricoDaApi,
                    faseTramitacao = histDto.acao,
                    unidadeResponsavel = histDto.nomeUnidade ?: histDto.sigla ?: "CLDF",
                    descricao = histDto.descricao,
                    projeto = proposicaoExistente
                )
                historicoRepo.save(historico)
                logger.info("➕ Novo histórico adicionado na atualização: ${historico.faseTramitacao}")
            }
        }

        // 3. VERIFICA NOVOS DOCUMENTOS
        val documentosBanco = documentoRepo.findByTipoRelacionadoAndIdRelacionado("PROPOSICAO", proposicaoExistente.id)
        val idsDocsBanco = documentosBanco.map { it.publicId }.toSet()
        val documentosDto = cldfIntegration.buscarDocumentos(publicIdStr)

        documentosDto.forEach { docDto ->
            val docPublicId = docDto.idArquivo
            if (docPublicId != null && !idsDocsBanco.contains(docPublicId)) {
                val autorLimpoDoDocumento = docDto.autoria?.replace(
                    Regex("^(Deputado|Deputada|Pastor|Pastora|Doutor|Doutora|Dr\\.|Dra\\.)\\s+", RegexOption.IGNORE_CASE), ""
                )?.trim()

                val doc = DocumentosArquivos(
                    publicId = docPublicId,
                    tipoDocumento = docDto.nome ?: "DOCUMENTO",
                    nomeExibicao = docDto.nome ?: "Documento da Proposição",
                    linkDireto = "/api/proposicoes/$publicIdStr/documentos/$docPublicId/pdf",
                    tipoRelacionado = "PROPOSICAO",
                    idRelacionado = proposicaoExistente.id,
                    validoDesde = docDto.validoDesde ?: LocalDateTime.now(),
                    dataCadastro = docDto.dataDocumento,
                    autor = autorLimpoDoDocumento,
                    siglaUnidadeCriacao = docDto.siglaUnidadeCriacao
                )
                documentoRepo.save(doc)
                logger.info("📄 Novo documento salvo na atualização: ${doc.nomeExibicao}")
            }
        }
       // 4. VERIFICA NOVAS REGIÕES ADMINISTRATIVAS
        val rasSincronizadas = processarRasDinamicamente(
            nomesRas = baseDto.regiaoAdiminstrativaNomeLista ?: emptyList(),
            idsRas = propCompleta?.regiaoAdministrativa ?: emptyList()
        )

        if (rasSincronizadas.isNotEmpty()) {
            val tamanhoAtual = proposicaoExistente.regioesAdministrativas.size
            proposicaoExistente.regioesAdministrativas.addAll(rasSincronizadas)

            // Se o tamanho do conjunto de RAs aumentou, é porque entrou RA nova, então salva!
            if (proposicaoExistente.regioesAdministrativas.size > tamanhoAtual) {
                proposicaoRepo.save(proposicaoExistente)
                logger.info("📍 Novas RAs vinculadas à proposição ${proposicaoExistente.publicId} na atualização: ${rasSincronizadas.map { it.nome }}")
            }
        }
    }

    // =========================================================================
    // LÓGICA DE INSERT (Criação do Zero)
    // =========================================================================
    private fun inserirNovaProposicao(baseDto: ProposicaoCldfBaseDTO, publicIdStr: String) {
        logger.info("🆕 A criar nova proposição $publicIdStr...")

        val detalhes = cldfIntegration.buscarDetalhesCompletos(publicIdStr) ?: return
        val propCompleta = detalhes.proposicao
        val listaHistorico = detalhes.historico

        val rasSincronizadas = processarRasDinamicamente(
            nomesRas = baseDto.regiaoAdiminstrativaNomeLista,
            idsRas = propCompleta?.regiaoAdministrativa
        )

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

        val temasIdsNumericos = propCompleta?.temasIds
        if (!temasIdsNumericos.isNullOrEmpty()) {
            val temasProcessados = cldfIntegration.processarTemasProposicao(temasIdsNumericos)
            proposicaoSalva.temas.addAll(temasProcessados)
            proposicaoRepo.save(proposicaoSalva)
        }

        val listaAutoresDto = propCompleta?.autores
        if (!listaAutoresDto.isNullOrEmpty()) {
            val politicosJaVinculados = mutableSetOf<Long>()

            listaAutoresDto.forEach { autorDto ->
                val nomeCru = autorDto.nome
                if (nomeCru.isNullOrBlank()) return@forEach

                val nomeLimpo = nomeCru.replace(
                    Regex("^(Deputado|Deputada|Pastor|Pastora|Doutor|Doutora|Dr\\.|Dra\\.)\\s+", RegexOption.IGNORE_CASE), ""
                ).trim()

                var politico = politicoRepo.findByNomeUrnaContainingIgnoreCase(nomeLimpo).firstOrNull()
                if (politico == null) {
                    // 1. Converte as ‘Strings’ do JSON para os Enums de forma segura
                    val statusSafe = runCatching {
                        StatusPolitico.valueOf(autorDto.situacao?.uppercase() ?: "ATIVO")
                    }.getOrDefault(StatusPolitico.ATIVO)

                    val tipoAutorSafe = runCatching {
                        TipoAutor.valueOf(autorDto.tipoAutor?.uppercase() ?: "PARLAMENTAR")
                    }.getOrDefault(TipoAutor.PARLAMENTAR)

                    // 2. Monta a entidade com os dados disponíveis
                    val novoPolitico = Politico(
                        // Transforma o ID numérico do JSON no publicId (String).
                        // Se por acaso vier nulo da API, gera um temporário para não violar o nullable=false do banco.
                        publicId = autorDto.id?.toString() ?: "TEMP_${java.util.UUID.randomUUID()}",
                        nomeCompleto = nomeCru, // Ex: "Deputado Chico Vigilante"
                        nomeUrna = nomeLimpo,   // Ex: "Chico Vigilante"
                        status = statusSafe,
                        tipoAutor = tipoAutorSafe


                    )

                    // Salva no banco imediatamente e atribui à variável 'politico'
                    politico = politicoRepo.save(novoPolitico)

                    // Adiciona na memória do robô para soltar o alerta vermelho no final da madrugada
                    politicosPendentesDeCadastro.add(nomeLimpo)

                    // Alerta amarelo no log na mesma hora
                    logger.warn("\u001B[43m\u001B[30m ⚠️ AUTO-CADASTRO: Novo político criado no sistema ($nomeLimpo). \u001B[0m")
                }
                if (politico.id != null) {
                    if (!politicosJaVinculados.contains(politico.id)) {
                        val novaAutoria = Autoria(
                            proposicao = proposicaoSalva,
                            politico = politico,
                            tipoVinculacao = autorDto.tipoAutor ?: "AUTOR"
                        )
                        autoriaRepo.save(novaAutoria)
                        politicosJaVinculados.add(politico.id)
                        logger.info("✅ Autoria vinculada com sucesso: ${politico.nomeUrna}")
                    }
                }
            }
        }

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
                    idRelacionado = proposicaoSalva.id!!,
                    validoDesde = docDto.validoDesde ?: LocalDateTime.now(),
                    dataCadastro = docDto.dataDocumento,
                    autor = autorLimpoDoDocumento,
                    siglaUnidadeCriacao = docDto.siglaUnidadeCriacao
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
    private fun processarRasDinamicamente(nomesRas: List<String>?, idsRas: List<Int>?): MutableSet<RegiaoAdministrativa> {
        val rasSincronizadas = mutableSetOf<RegiaoAdministrativa>()

        val listaRaNomes = nomesRas ?: emptyList()
        val listaRaIds = idsRas ?: emptyList()

        val tamanhoRas = maxOf(listaRaNomes.size, listaRaIds.size)

        for (i in 0 until tamanhoRas) {
            val nomeCru = listaRaNomes.getOrNull(i)
            val idCru = listaRaIds.getOrNull(i)?.toString() ?: "SEM_ID_$i"

            // Ignora se não houver nome
            if (nomeCru.isNullOrBlank()) continue

            // Ignora o Distrito Federal genérico e IDs 0
            if (nomeCru.contains("DISTRITO FEDERAL", ignoreCase = true) || idCru == "0") {
                continue
            }

            // Tenta achar a RA que você pré-cadastrou via SQL
            var ra = raRepo.findByNomeContainingIgnoreCase(nomeCru)

            if (ra == null) {
                // Se a CLDF inventar uma RA nova que não estava na sua lista, ele cria.
                val novaRa = RegiaoAdministrativa(
                    nome = nomeCru,
                    publicId = idCru
                )
                ra = raRepo.save(novaRa)
                logger.info("📍 Nova RA cadastrada dinamicamente que não estava na lista: $nomeCru")
            } else if (ra.publicId?.startsWith("TEMP_") == true || ra.publicId != idCru) {
                // Atualiza o ID temporário com o oficial da API
                ra.publicId = idCru
                ra = raRepo.save(ra)
                logger.info("🔄 RA pré-cadastrada '${ra.nome}' atualizada com o ID oficial da CLDF: $idCru")
            }

            rasSincronizadas.add(ra)
        }

        return rasSincronizadas
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
                    autor = d.autor,
                    siglaUnidadeCriacao = d.siglaUnidadeCriacao
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
            publicId = UUID.randomUUID().toString(), // Obrigatorio pelo unique=true
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