package com.example.df.backend.entities
import com.example.df.backend.enums.StatusObra
import jakarta.persistence.*
import java.time.LocalDateTime
@Entity
@Table(name = "OBRAS_HISTORICO")
data class ObraHistorico(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTORICO_OBRA")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_OBRA", nullable = false)
    val obra: Obra,
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_ANTERIOR")
    val statusAnterior: StatusObra,
@Enumerated(EnumType.STRING)
    @Column(name = "STATUS_NOVO")
    val statusNovo : StatusObra,

    @Column(name = "PERCENTUAL_ANTERIOR")
    val percentualAnterior: Int? = null,

    @Column(name = "DESCRICAO_MUDANCA", columnDefinition = "TEXT")
    val descricaoMudanca: String? = null,

    @Column(name = "DATA_ATUALIZACAO")
    val dataAtualizacao: LocalDateTime = LocalDateTime.now()
)