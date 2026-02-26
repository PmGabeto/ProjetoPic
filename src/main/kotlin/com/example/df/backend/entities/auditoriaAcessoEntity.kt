package com.example.df.backend.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "AUDITORIA_ACESSOS")
data class AuditoriaAcesso(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LOG")
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    val usuario: Usuario? = null, // Pode ser null se falhou o login antes de saber quem é

    @Column(nullable = false)
    val acao: String, // LOGIN, VOTO, CADASTRO

    @Column(columnDefinition = "JSONB")
    val detalhes: String? = null, // JSON armazenado como String

    @Column(nullable = false)
    val sucesso: Boolean = true,

    @Column(name = "DATA_ACESSO", nullable = false)
    val dataAcesso: LocalDateTime = LocalDateTime.now(),

    @Column(name = "IP_ORIGEM")
    val ipOrigem: String? = null,

    @Column(name = "USER_AGENT")
    val userAgent: String? = null
)