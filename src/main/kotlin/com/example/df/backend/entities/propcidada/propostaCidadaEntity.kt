package com.example.df.backend.entities.propcidada

import com.example.df.backend.entities.Usuario
import com.example.df.backend.enums.StatusProposta
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "PROPOSTAS")
data class Proposta(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROPOSTA")
    val id: Long? = null,

    @Column(nullable = false, length = 500)
    val titulo: String,

    @Column(columnDefinition = "TEXT")
    val descricao: String? = null,

    @ManyToOne
    @JoinColumn(name = "ID_AUTOR", nullable = false)
    val autor: Usuario,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: StatusProposta,

    @Column(name = "DT_CRIACAO")
    val dataCriacao: LocalDateTime = LocalDateTime.now(),

    @Column(name = "IS_ATIVA")
    val ativa: Boolean = true,

    // Relacionamentos para Análise e Resposta Governamental
    @OneToOne(mappedBy = "proposta", cascade = [CascadeType.ALL])
    val analiseTecnica: AnaliseProposta? = null,

    @OneToOne(mappedBy = "proposta", cascade = [CascadeType.ALL])
    val respostaGoverno: RespostaGoverno? = null
)