package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.Obra
import com.example.df.backend.entities.ObraHistorico
import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.StatusObra
import com.example.df.backend.repositories.ObraHistoricoRepository
import com.example.df.backend.repositories.ObraRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime


@Service
class ObraService(
    private val repository: ObraRepository,
    private val historicoRepository: ObraHistoricoRepository
) {

    // A. PARA O MAPA (Retorna só os PINS)
    fun listarPins(): List<ObraPinDTO> {
        // 1. Buscamos todas
        return repository.findAll()
            // 2. Filtramos: Só passam as que NÃO estão concluídas
            .filter { it.status != StatusObra.CONCLUIDA }
            // 3. Mapeamos para o DTO
            .map { obra ->
                ObraPinDTO(
                    id = obra.id!!,
                    latitude = obra.latitude?.toDouble() ?: 0.0,
                    longitude = obra.longitude?.toDouble() ?: 0.0,
                    nome = obra.nome,
                    status = obra.status, // Converte string para Enum
                    progresso = obra.percentualConclusao
                )
            }
    }
    // B. PARA A LISTAGEM (Com Filtros)
    fun listarObras(ra: String?, orgao: OrgaoExecutor?): List<ObraListagemDTO> {
        return repository.buscarComFiltros(ra, orgao).map { obra ->
            ObraListagemDTO(
                id = obra.id!!,
                nome = obra.nome,
                orgao = obra.orgaoExecutor,
                ra = obra.raAdministrativa,
                status = obra.status,
                progresso = obra.percentualConclusao
            )
        }
    }

    // C. DETALHE ÚNICO
    @Transactional(readOnly = true)
    fun buscarDetalhes(id: Long): ObraDetalheDTO {
        val obra = repository.findById(id)
            .orElseThrow { IllegalArgumentException("Obra não encontrada com ID: $id") }

        // Mapeia o histórico da entidade para o DTO
        val historicoDTOs = obra.historico.map { h ->
            HistoricoResumoDTO(
                data = h.dataAtualizacao.toLocalDate(),
                descricao = h.descricaoMudanca,
                statusNovo = h.statusNovo
            )
        }

        // Mapeia os aditivos da entidade para o DTO
        val aditivosDTOs = obra.aditivos.map { a ->
            AditivoResumoDTO(
                data = a.dataAprovacao,
                valor = a.valor,
                justificativa = a.justificativa
            )
        }

        return ObraDetalheDTO(
            id = obra.id!!,
            nome = obra.nome,
            descricao = obra.descricao,
            orgao = obra.orgaoExecutor,
            ra = obra.raAdministrativa,
            status = obra.status,
            orcamento = obra.orcamentoPrevisto,
            progresso = obra.percentualConclusao,
            inicio = obra.dataInicioPrevista,
            fim = obra.dataFimPrevista,
            linkDocumento = obra.urlDocumento,
            historico = historicoDTOs,
            aditivos = aditivosDTOs
        )
    }

    // D. CADASTRAR (Manual MVP)
    @Transactional
    fun criarObra(dto: CriarObraDTO): Obra {
        val lonBD = BigDecimal.valueOf(dto.longitude)
        val latBD = BigDecimal.valueOf(dto.latitude)
        val novaObra = Obra(
            nome = dto.nome,
            descricao = dto.descricao,
            latitude = latBD,
            longitude = lonBD,
            raAdministrativa = dto.ra,
            orgaoExecutor = dto.orgao,
            status = dto.status,
            percentualConclusao = dto.progresso ?: 0,
            orcamentoPrevisto = dto.orcamentoPrevisto,
            orcamentoGasto = null,
            empresaContratada = dto.empresaContratada,
            dataInicioPrevista = dto.dataInicio,
            dataFimPrevista = dto.dataFim,
            urlDocumento = dto.urlDocumento
        )
        return repository.save(novaObra)
    }

    @Transactional
    fun atualizarProgresso(
        idObra: Long,
        novoPercentual: Int,
        novoStatus: StatusObra?,
        descricao: String?
    ) {
        val obra = repository.findById(idObra)
            .orElseThrow { IllegalArgumentException("Obra não encontrada") }

        // 1. Guardamos o status antigo antes de mudar
        val statusAnterior = obra.status

        // 2. Atualizamos a Obra
        obra.percentualConclusao = novoPercentual
        if (novoStatus != null) {
            obra.status = novoStatus
        }

        // 3. Criamos o registro de Histórico (Transparência)
        val historico = ObraHistorico(
            obra = obra,
            statusAnterior = statusAnterior,
            statusNovo = obra.status,
            descricaoMudanca = descricao ?: "Atualização de progresso para $novoPercentual%",
            dataAtualizacao = LocalDateTime.now()
        )

        // 4. Salvamos tudo
        repository.save(obra)
        historicoRepository.save(historico)
    }
}