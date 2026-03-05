package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.*
import com.example.df.backend.enums.TipoProjetoLei
import com.example.df.backend.integrations.cldf.CldfInterface
import com.example.df.backend.integrations.cldf.ProposicaoCldfCompletaDTO
import com.example.df.backend.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
@Suppress("unused") // Remove avisos de "Function is never used" na IDE
class ProposicaoService(
    private val proposicaoRepo: ProposicaoRepository,
    private val temaRepo: TemaRepository,
    private val politicoRepo: PoliticoRepository,
    private val autoriaRepo: AutoriaRepository,
    private val cldfIntegration: CldfInterface,
    private val historicoRepo: HistoricoRepository,
    private val documentoService: DocumentoService,
    private val documentoRepo: DocumentoRepository
) {

    private val logger = LoggerFactory.getLogger(ProposicaoService::class.java)

    // =========================================================================
    // MOTOR DE VARREDURA RESILIENTE (CARGA AUTOMÁTICA USANDO DTOs DE INTEGRAÇÃO)
    // =========================================================================

    fun sincronizarCargaTotal(ano: Int, paginaInicial: Int = 0) {
        var pagina = paginaInicial
        val tamanho = 20

        logger.info(">>> Iniciando Varredura: Ano $ano, Página $pagina")

        // CORREÇÃO: Removida a variável 'continuar' inútil apontada pela IDE
        while (true) {
            try {
                val listaBasica = cldfIntegration.varrerProposicoesRecentes(ano, pagina, tamanho)

                if (listaBasica.isEmpty()) {
                    break // Sai do loop naturalmente quando acabar as páginas
                }

                listaBasica.forEach { item ->
                    try {
                        orquestrarSincronizacaoIndividual(item.publicId)
                    } catch (e: Exception) {
                        logger.error("Falha na Proposição ${item.publicId}: ${e.message}")
                    }
                }

                logger.info("Checkpoint: Página $pagina concluída.")
                pagina++

            } catch (e: Exception) {
                logger.error("Erro de timeout/API na página $pagina. Detalhe: ${e.message}. Re-tentando em 5s...")
                Thread.sleep(5000)
            }
        }
    }

    // CORREÇÃO: Retirado o @Transactional para evitar o aviso de "self-invocation" da IDE
    // O JPA já gerencia as transações individuais nos métodos .save() dos repositórios
    fun orquestrarSincronizacaoIndividual(publicId: String) {
        val dtoCompleto = cldfIntegration.buscarDetalhesCompletos(publicId) ?: return
        val existente = proposicaoRepo.findByPublicId(publicId)

        if (existente == null) {
            salvarProposicaoCldf(dtoCompleto)
        } else {
            atualizarProposicaoCldf(existente, dtoCompleto)
        }
    }

    private fun salvarProposicaoCldf(dto: ProposicaoCldfCompletaDTO) {
        val nova = Proposicao(
            publicId = dto.publicId,
            titulo = dto.titulo,
            ementa = dto.ementa,
            numeroProcesso = dto.numeroProcesso,
            statusTramitacao = dto.statusTramitacao ?: "Aguardando",
            regiaoAdministrativa = dto.regiaoAdministrativa,
            regimeUrgencia = dto.regimeUrgencia,
            dataApresentacao = parseLocalDateSeguro(dto.dataApresentacao) ?: LocalDate.now(),
            dataLimite = parseLocalDateSeguro(dto.dataLimite),
            linkCompleto = dto.linkCompleto,
            tipo = converterSiglaEnum(dto.siglaTipo),
            temas = cldfIntegration.processarTemasProposicao(dto.temaIdRaw, dto.temaNomeRaw).toMutableSet()
        )

        val salva = proposicaoRepo.save(nova)
        sincronizarHistoricoIncremental(salva)
        sincronizarDocumentosIncremental(salva)
    }

    private fun atualizarProposicaoCldf(existente: Proposicao, dto: ProposicaoCldfCompletaDTO) {
        existente.statusTramitacao = dto.statusTramitacao ?: existente.statusTramitacao
        existente.regimeUrgencia = dto.regimeUrgencia
        existente.ementa = dto.ementa
        proposicaoRepo.save(existente)

        sincronizarHistoricoIncremental(existente)
        sincronizarDocumentosIncremental(existente)
    }

    private fun sincronizarHistoricoIncremental(proposicao: Proposicao) {
        val historicosApi = cldfIntegration.buscarHistorico(proposicao.publicId)
        historicosApi.forEach { h ->
            val dataEvDate = parseLocalDateSeguro(h.dataEvento) ?: LocalDate.now()
            val dataEvDateTime = dataEvDate.atStartOfDay()

            val existe = historicoRepo.existsByProjetoAndDataEventoAndFaseTramitacao(proposicao, dataEvDateTime, h.fase)

            if (!existe) {
                historicoRepo.save(ProposicaoHistorico(
                    dataEvento = dataEvDateTime,
                    faseTramitacao = h.fase,
                    unidadeResponsavel = h.unidade,
                    descricao = h.descricao,
                    projeto = proposicao,
                    publicId = h.publicId
                ))
            }
        }
    }

    private fun sincronizarDocumentosIncremental(proposicao: Proposicao) {
        val docsApi = cldfIntegration.buscarDocumentos(proposicao.publicId)
        docsApi.forEach { d ->
            val docPublicId = d.publicId ?: gerarPublicId12Caracteres()

            if (documentoRepo.findByPublicId(docPublicId) == null) {
                documentoRepo.save(DocumentosArquivos(
                    publicId = docPublicId,
                    tipoDocumento = d.tipo,
                    nomeExibicao = d.nome,
                    nomeStorage = "CLDF_${docPublicId}.pdf",
                    linkDireto = d.link,
                    tipoRelacionado = "PROPOSICAO",
                    idRelacionado = proposicao.id!!
                ))
            }
        }
    }

    // =========================================================================
    // MÉTODOS MANUAIS ORIGINAIS (USANDO SEUS DTOs DO SISTEMA)
    // =========================================================================

    @Transactional
    fun adicionarProposicao(politicoId: Long, dto: CriarProposicaoDTO): Proposicao {
        val politico = politicoRepo.findById(politicoId)
            .orElseThrow { IllegalArgumentException("Político não encontrado") }

        val temasGarantidos = temaRepo.findAllById(dto.temaId).toMutableSet()

        val proposicao = Proposicao(
            publicId = dto.publicId,
            // CORREÇÃO: A Entidade espera o Enum. Passamos o DTO (String) no conversor:
            tipo = converterSiglaEnum(dto.tipoSigla),
            numeroProcesso = dto.numero,
            titulo = dto.titulo,
            ementa = dto.ementa,
            statusTramitacao = dto.statusTramitacao,
            dataApresentacao = dto.dataApresentacao,
            linkCompleto = dto.linkCompleto,
            temas = temasGarantidos
        )

        val propSalva = proposicaoRepo.save(proposicao)

        val autoriaId = AutoriaId(politico.id!!, propSalva.id!!)
        val novaAutoria = Autoria(
            id = autoriaId,
            politico = politico,
            proposicao = propSalva,
            tipoVinculacao = dto.tipoVinculacao
        )
        autoriaRepo.save(novaAutoria)

        return propSalva
    }

    @Transactional(readOnly = true)
    fun listarTodas(): List<ProposicaoResumoDTO> {
        return proposicaoRepo.findAll().map { p ->
            // CORREÇÃO: Nomes exatos exigidos pela classe do ProposicaoResumoDTO
            ProposicaoResumoDTO(
                id = p.id!!,
                publicId = p.publicId,
                titulo = p.titulo,
                ementa = p.ementa,
                status = p.statusTramitacao ?: "Aguardando",
                data = p.dataApresentacao, // Substituiu dataApresentacao
                numero = p.numeroProcesso, // Campo obrigatório faltante
                tipo = TipoProposicaoDTO(p.tipo.name, p.tipo.name, ""), // Campo obrigatório faltante
                linkCompleto = p.linkCompleto, // Campo obrigatório faltante
                minhaVinculacao = null, // Sem contexto de usuário numa listagem geral
                // CORREÇÃO: Retirado o '!!' desnecessário e corrigido para 'tema'
                tema = p.temas.map { TemaDTO(it.id, it.nome) }
            )
        }
    }

    @Transactional(readOnly = true)
    fun buscarDetalhe(id: Long): ProposicaoDetalheDTO {
        val p = proposicaoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Proposição não encontrada") }

        val historicos = historicoRepo.findByProjetoOrderByDataEventoDesc(p)
        val documentos = documentoService.listarDocumentos("PROPOSICAO", p.id!!)

        return ProposicaoDetalheDTO(
            id = p.id,
            publicId = p.publicId,
            tipo = TipoProposicaoDTO(p.tipo.name, p.tipo.name, ""),
            numeroProcesso = p.numeroProcesso,
            numeroDefinitivo = p.numeroDefinitivo,
            titulo = p.titulo,
            ementa = p.ementa,
            statusTramitacao = p.statusTramitacao,
            regiaoAdministrativa = p.regiaoAdministrativa,
            regimeUrgencia = p.regimeUrgencia,
            dataApresentacao = p.dataApresentacao,
            dataLimite = p.dataLimite,
            linkCompleto = p.linkCompleto,
            tema = p.temas.map { TemaDTO(it.id, it.nome) },
            historicos = historicos.map { HistoricoDTO(it.dataEvento.toLocalDate(), it.faseTramitacao, it.unidadeResponsavel, it.descricao ?: "") },
            documentos = documentos,
            autores = p.autores.map { autoria ->
                PoliticoResumoDTO(
                    id = autoria.politico.id!!,
                    nome = autoria.politico.nomeUrna ?: autoria.politico.nomeCompleto,
                    partido = autoria.politico.partidoAtual,
                    status = autoria.politico.status,
                    foto = autoria.politico.urlFoto,
                    // CORREÇÃO: Referência a 'AutoriaId' substituída pelo correto 'publicId'
                    publicId = autoria.politico.publicId
                )
            }
        )
    }

    @Transactional
    fun adicionarHistorico(idProposicao: Long, dto: NovoHistoricoDTO): ProposicaoDetalheDTO {
        val prop = proposicaoRepo.findById(idProposicao)
            .orElseThrow { IllegalArgumentException("Proposição não encontrada") }

        val novoHistorico = ProposicaoHistorico(
            // CORREÇÃO: Inclusão do publicId que estava faltando ao criar um Histórico manualmente
            publicId = gerarPublicId12Caracteres(),
            dataEvento = dto.dataEvento.atStartOfDay(),
            faseTramitacao = dto.faseTramitacao,
            unidadeResponsavel = dto.unidadeResponsavel,
            descricao = dto.descricao,
            projeto = prop
        )

        historicoRepo.save(novoHistorico)

        if (dto.atualizarStatusDaProposicao) {
            prop.statusTramitacao = dto.faseTramitacao
            proposicaoRepo.save(prop)
        }

        return buscarDetalhe(idProposicao)
    }

    @Transactional
    fun atualizarTemas(idProposicao: Long, novosTemasIds: List<Long>): ProposicaoDetalheDTO {
        val prop = proposicaoRepo.findById(idProposicao)
            .orElseThrow { IllegalArgumentException("Proposição não encontrada") }

        val novosTemas = temaRepo.findAllById(novosTemasIds)
        prop.temas.clear()
        prop.temas.addAll(novosTemas)
        proposicaoRepo.save(prop)

        return buscarDetalhe(idProposicao)
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    private fun gerarPublicId12Caracteres(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12)
    }

    private fun parseLocalDateSeguro(data: String?): LocalDate? {
        if (data.isNullOrBlank() || data.length < 10) return null
        return try {
            LocalDate.parse(data.substring(0, 10))
        } catch (_: Exception) { // CORREÇÃO: Substituído 'e' por '_' pois não é utilizado
            null
        }
    }

    private fun converterSiglaEnum(sigla: String): TipoProjetoLei {
        return try {
            TipoProjetoLei.valueOf(sigla.uppercase())
        } catch (_: Exception) { // CORREÇÃO: Substituído 'e' por '_' pois não é utilizado
            TipoProjetoLei.PL
        }
    }
}