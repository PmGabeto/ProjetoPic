package com.example.df.backend.services

import com.example.df.backend.dtos.NovoHistoricoDTO
import com.example.df.backend.dtos.ProposicaoResumoDTO
import com.example.df.backend.entities.ProposicaoHistorico
import com.example.df.backend.enums.TipoVinculacao
import com.example.df.backend.repositories.HistoricoRepository
import com.example.df.backend.repositories.ProposicaoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposicaoService(
    private val proposicaoRepo: ProposicaoRepository,
    private val historicoRepo: HistoricoRepository
) {

    /**
     * Adiciona uma nova tramitação e, opcionalmente, atualiza o status principal da Lei.
     * @Transactional garante que: ou salva tudo (histórico + status novo) ou não salva nada se der erro.
     */
    @Transactional
    fun adicionarHistorico(idProposicao: Long, dto: NovoHistoricoDTO): ProposicaoResumoDTO {

        // 1. Busca a Proposição Pai (Fail-fast: se não achar, para tudo agora)
        val proposicao = proposicaoRepo.findById(idProposicao)
            .orElseThrow { IllegalArgumentException("Proposição não encontrada com ID: $idProposicao") }

        // 2. Cria a entidade do Histórico vinculada à Proposição
        val novoHistorico = ProposicaoHistorico(
            dataEvento = dto.dataEvento,
            faseTramitacao = dto.faseTramitacao,
            descricao = dto.descricao,
            projeto = proposicao
        )

        // 3. Salva o registro histórico no banco
        historicoRepo.save(novoHistorico)

        // 4. Lógica de Atualização de Status (A porta para automação futura)
        // Se for um evento importante (ex: "Aprovado"), atualizamos o cabeçalho da Lei.
        // Se for algo menor (ex: "Recebido no protocolo"), mantemos o status anterior.
        if (dto.atualizarStatusDaProposicao) {
            proposicao.statusTramitacao = dto.faseTramitacao
            proposicaoRepo.save(proposicao)
        }

        // 5. Retorna o DTO atualizado para o Front-end já mostrar a mudança na tela
        return ProposicaoResumoDTO(
            id = proposicao.id!!,
            tipo = proposicao.tipo,
            numero = proposicao.numero,
            titulo = proposicao.titulo,
            area = proposicao.areaTematica,
            status = proposicao.statusTramitacao, // O status novo já vem aqui
            data = proposicao.dataApresentacao,
            minhaVinculacao = TipoVinculacao.AUTOR_PRINCIPAL // Valor padrão genérico para esta visualização
        )
    }
}