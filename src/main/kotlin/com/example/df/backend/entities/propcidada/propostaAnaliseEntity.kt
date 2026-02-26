package com.example.df.backend.entities.propcidada
import  java.math.BigDecimal
import jakarta.persistence.*
@Entity
@Table(name = "ANALISE_PROPOSTA")
data class AnaliseProposta(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ANALISE")
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "ID_PROPOSTA")
    val proposta: Proposta,

    @Column(name = "RESULTADO_VIABILIDADE")
    val resultado: String, // VIAVEL / INVIAVEL

    @Column(name = "JUSTIFICATIVA_TECNICA", columnDefinition = "TEXT")
    val justificativa: String,

    @Column(name = "CUSTO_ESTIMADO")
    val custoEstimado: BigDecimal? = null
)