package com.example.df.backend.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "PROPOSICAO_HISTORICO")
data class ProposicaoHistorico(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTORICO")
    val id: Long? = null,

    @Column(name = "DT_EVENTO", nullable = false)
    val dataEvento: LocalDate,

    @Column(name = "FASE_TRAMITACAO", nullable = false)
    val faseTramitacao: String,

    @Column(name = "UNIDADE_RESPONSAVEL")
    val unidadeResponsavel: String? = null, // Ex: "Comissão de Constituição e Justiça"

    @Lob
    @Column(name = "DESCRICAO")
    val descricao: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROJETO", nullable = false)
    @JsonIgnore
    val projeto: Proposicao
)