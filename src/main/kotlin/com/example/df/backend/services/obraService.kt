package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.Obra
import com.example.df.backend.entities.ObraHistorico
import com.example.df.backend.entities.OrcamentoAditivo
import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.StatusObra
import com.example.df.backend.enums.RaAdministrativa
import com.example.df.backend.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ObraService(
    private val repository: ObraRepository,
    private val historicoRepository: ObraHistoricoRepository,
    private val aditivoRepository: AditivosRepository,
    private val fotoRepository: OcorrenciaFotoRepository,
    private val documentoService: DocumentoService // 1. Injetamos o novo serviço de documentos
) {

    // =========================================================================
    // 1. CONSULTAS
    // =========================================================================

    fun listarPins(incluirConcluidas: Boolean = false): List<ObraPinDTO> {
        return repository.buscarDadosSimplificadosMapa(incluirConcluidas).map { p ->
            ObraPinDTO(
                id = p.id,
                latitude = p.latitude,
                longitude = p.longitude,
                nome = p.nome,
                endereco = p.endereco,
                status = p.status,
                orgao = p.orgaoExecutor,
                ra = p.raAdministrativa,
                progresso = p.progresso
            )
        }
    }

    fun listarObras(
        ra: RaAdministrativa?,
        orgao: OrgaoExecutor?,
        status: List<StatusObra>?
    ): List<ObraListagemDTO> {
        // Aqui usamos o método 'buscarComFiltros' que está no seu ObraRepository
        return repository.buscarComFiltros(ra, orgao, status).map { obra ->
            ObraListagemDTO(
                id = obra.id!!,
                nome = obra.nome,
                ra = obra.raAdministrativa,
                orgao = obra.orgaoExecutor,
                status = obra.status,
                progresso = obra.percentualConclusao,
                dataAtualizacao = obra.dataUltimaAtualizacao.toLocalDate()
            )
        }
    }
    fun buscarDetalhes(id: Long): ObraDetalheDTO {
        val obra = repository.findById(id)
            .orElseThrow { NoSuchElementException("Obra não encontrada") }

        // 2. Buscamos as fotos (Lógica que você já tinha)
        val fotos = fotoRepository.findByTipoMidiaAndIdRelacionado("OBRA", id).map { foto ->
            FotoResponseDTO(
                id = foto.id ?: 0,
                publicId = foto.publicId, // Certifique-se que o campo na Entity é publicId
                url = "https://vigiadf.pmhub.cloud/api/foto/v/${foto.publicId}",
                nomeOriginal = foto.nomeOriginal // Certifique-se que o campo na Entity é nomeOriginal
            )
        }
        // 3. BUSCA DINÂMICA DE DOCUMENTOS (Nova lógica)
        val documentos = documentoService.listarDocumentos("OBRA", id)

        return ObraDetalheDTO(
            id = obra.id!!,
            nome = obra.nome,
            descricao = obra.descricao,
            endereco = obra.endereco,
            ra = obra.raAdministrativa,
            orgao = obra.orgaoExecutor,
            status = obra.status,
            progresso = obra.percentualConclusao,
            orcamentoPrevisto = obra.orcamentoPrevisto,
            dataInicio = obra.dataInicioPrevista,
            dataFim = obra.dataFimPrevista,
            empresaContratada = obra.empresaContratada,
            documentos = documentos, // 4. Injetamos a lista no DTO de resposta
            fotos = fotos,
            historico = obra.historico.map { HistoricoResumoDTO(it.dataAtualizacao.toLocalDate(), it.descricaoMudanca, it.statusNovo) },
            aditivos = obra.aditivos.map { AditivoResumoDTO(it.dataAprovacao, it.valor, it.justificativa) }
        )
    }

    // =========================================================================
    // 2. MANUTENÇÃO (CRIAÇÃO E ATUALIZAÇÃO)
    // =========================================================================

    @Transactional
    fun criarObra(dto: CriarObraDTO): Obra {
        val obra = Obra(
            nome = dto.nome,
            descricao = dto.descricao,
            endereco = dto.endereco,
            latitude = dto.latitude,
            longitude = dto.longitude,
            raAdministrativa = dto.ra,
            orgaoExecutor = dto.orgao,
            status = dto.status,
            percentualConclusao = dto.progresso ?: 0,
            orcamentoPrevisto = dto.orcamentoPrevisto,
            empresaContratada = dto.empresaContratada,
            dataInicioPrevista = dto.dataInicio,
            dataFimPrevista = dto.dataFim
            // urlDocumento removido conforme a nova Entity
        )
        return repository.save(obra)
    }

    @Transactional
    fun atualizarObra(id: Long, request: AtualizarObraRequest) {
        val obra = repository.findById(id)
            .orElseThrow { NoSuchElementException("Obra não encontrada") }

        val statusAnterior = obra.status
        val percentualAnterior = obra.percentualConclusao

        // Atualização de campos básicos
        request.novoNome?.let { obra.nome = it }
        request.novaDescricao?.let { obra.descricao = it }
        request.novoPercentual?.let { obra.percentualConclusao = it }
        request.novoStatus?.let { obra.status = it }
        request.novaDataFim?.let { obra.dataFimPrevista = it }
        request.novoOrcamentoBase?.let { obra.orcamentoPrevisto = it }
        request.novaEmpresa?.let { obra.empresaContratada = it }
        request.novaRa?.let { obra.raAdministrativa = it }

        obra.dataUltimaAtualizacao = LocalDateTime.now()

        // 5. Histórico obrigatório para auditoria
        val historico = ObraHistorico(
            obra = obra,
            statusAnterior = statusAnterior,
            statusNovo = obra.status,
            percentualAnterior = percentualAnterior,
            descricaoMudanca = request.descricaoMudanca,
            dataAtualizacao = LocalDateTime.now()
        )

        historicoRepository.save(historico)
        repository.save(obra)
    }

    @Transactional
    fun adicionarAditivo(idObra: Long, dto: CriarAditivoDTO) {
        val obra = repository.findById(idObra)
            .orElseThrow { NoSuchElementException("Obra não encontrada") }

        val aditivo = OrcamentoAditivo(
            obra = obra,
            valor = dto.valor,
            dataAprovacao = dto.dataAprovacao,
            justificativa = dto.justificativa
        )
        aditivoRepository.save(aditivo)

        val historico = ObraHistorico(
            obra = obra,
            statusAnterior = obra.status,
            statusNovo = obra.status,
            descricaoMudanca = "Aditivo Financeiro: R$ ${dto.valor}. Justificativa: ${dto.justificativa}",
            dataAtualizacao = LocalDateTime.now()
        )
        historicoRepository.save(historico)
    }
}