package com.example.df.backend.entities.propcidada

import jakarta.persistence.*
@Entity
@Table(name = "RESPOSTA_GOVERNO")
data class RespostaGoverno(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RESPOSTA")
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "ID_PROPOSTA")
    val proposta: Proposta,

    @Column(name = "DECISAO_FINAL")
    val decisaoFinal: String, // APROVADA / REJEITADA

    @Column(name = "STATUS_IMPLEMENTACAO")
    val statusImplementacao: String = "AGUARDANDO_INICIO"
)