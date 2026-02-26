package com.example.df.backend.entities
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "ORCAMENTOS_ADITIVOS")
data class OrcamentoAditivo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ADITIVO")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_OBRA", nullable = false)
    val obra: Obra,

    @Column(name = "DATA_APROVACAO", nullable = false)
    val dataAprovacao: LocalDate,

    @Column(name = "VALOR_ADITIVO", nullable = false)
    val valor: BigDecimal,

    @Column(columnDefinition = "TEXT")
    val justificativa: String? = null
)