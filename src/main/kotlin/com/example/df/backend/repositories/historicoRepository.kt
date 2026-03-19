package com.example.df.backend.repositories

import com.example.df.backend.entities.Proposicao
import com.example.df.backend.entities.ProposicaoHistorico
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface HistoricoRepository : JpaRepository<ProposicaoHistorico, Long> {

    // Usado no buscarDetalhe() para listar a linha do tempo
    fun findByProjetoIdOrderByDataEventoDesc(projetoId: Long): List<ProposicaoHistorico>
    // Usado na sincronização incremental: Verifica se aquele exato evento já foi salvo
    fun existsByProjetoAndDataEventoAndFaseTramitacao(
        projeto: Proposicao, dataEvento: LocalDateTime, faseTramitacao: String
    ): Boolean
}