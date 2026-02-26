package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.*
import com.example.df.backend.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposicaoService(
    private val proposicaoRepo: ProposicaoRepository,
    private val temaRepo: TemaRepository,
    private val historicoRepo: HistoricoRepository,
    private val documentoRepo: DocumentoRepository
) {

    @Transactional(readOnly = true)
    fun listarTodas(): List<ProposicaoResumoDTO> {
        return proposicaoRepo.findAll().map { prop ->
            ProposicaoResumoDTO(
                id = prop.id!!,
                idExterno = prop.idExterno,
                tipo = TipoProposicaoDTO(prop.tipo.sigla, prop.tipo.nome, prop.tipo.descricaoPedagogica),
                numero = prop.numeroProcesso,
                titulo = prop.titulo,
                tema = prop.temas.map { TemaDTO(it.id, it.nome) }, // Mapeia lista de temas
                status = prop.statusTramitacao ?: "Em tramitação",
                data = prop.dataApresentacao,
                minhaVinculacao = null
            )
        }
    }

    @Transactional(readOnly = true)
    fun buscarDetalhe(id: Long): ProposicaoDetalheDTO {
        val prop = proposicaoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Proposição não encontrada com ID: $id") }

        return ProposicaoDetalheDTO(
            id = prop.id!!,
            idExterno = prop.idExterno,
            tipo = TipoProposicaoDTO(prop.tipo.sigla, prop.tipo.nome, prop.tipo.descricaoPedagogica),
            numeroProcesso = prop.numeroProcesso,
            numeroDefinitivo = prop.numeroDefinitivo,
            titulo = prop.titulo,
            ementa = prop.ementa,
            statusTramitacao = prop.statusTramitacao,
            regiaoAdministrativa = prop.regiaoAdministrativa,
            regimeUrgencia = prop.regimeUrgencia,
            dataApresentacao = prop.dataApresentacao,
            dataLimite = prop.dataLimite,
            tema = prop.temas.map { TemaDTO(it.id, it.nome) }, // Mapeia lista de temas

            documentos = prop.documentos.map { doc ->
                DocumentoDTO(doc.idExterno, doc.tipoDocumento, doc.dataDocumento, doc.linkArquivo, doc.autorDocumento)
            },

            historicos = prop.historicos.sortedByDescending { it.dataEvento }.map { hist ->
                HistoricoDTO(hist.dataEvento, hist.faseTramitacao, hist.unidadeResponsavel, hist.descricao)
            },

            autores = prop.autores.map { autoria ->
                PoliticoResumoDTO(
                    id = autoria.politico.id!!,
                    idExterno = autoria.politico.idExterno,
                    nome = autoria.politico.nomeUrna ?: autoria.politico.nomeCompleto,
                    partido = autoria.politico.partidoAtual,
                    status = autoria.politico.status,
                    foto = autoria.politico.urlFoto
                )
            }
        )
    }

    @Transactional
    fun adicionarHistorico(idProposicao: Long, dto: NovoHistoricoDTO): ProposicaoDetalheDTO {
        val prop = proposicaoRepo.findById(idProposicao)
            .orElseThrow { IllegalArgumentException("Proposição não encontrada") }

        val novoHistorico = ProposicaoHistorico(
            dataEvento = dto.dataEvento,
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
}