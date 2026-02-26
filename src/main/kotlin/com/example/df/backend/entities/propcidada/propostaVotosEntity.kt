package com.example.df.backend.entities.propcidada

import com.example.df.backend.entities.Usuario
import jakarta.persistence.*
import java.math.BigDecimal
@Entity
@Table(name = "VOTOS_PROPOSTA")
data class VotoProposta(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_VOTO_PROPOSTA")
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "ID_PROPOSTA", nullable = false)
    val proposta: Proposta,

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    val usuario: Usuario,

    @Column(nullable = false)
    val voto: Int, // 1 = Sim, 2 = Não (conforme Constraint do SQL)

    @Column(name = "PESO_GEOGRAFICO")
    val pesoGeografico: BigDecimal = BigDecimal.ONE
)