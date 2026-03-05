package com.example.df.backend.entities

import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.RaAdministrativa
import com.example.df.backend.enums.StatusObra
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "OBRAS")
data class Obra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_OBRA")
    val id: Long? = null,

    @Column(name = "NOME_OBRA", nullable = false, length = 500)
    var nome: String,

    @Column(columnDefinition = "TEXT")
    var descricao: String? = null,

    // --- Localização ---
    var endereco: String? = null,

    @Column(precision = 10, scale = 7)
    val latitude: BigDecimal? = null,

    @Column(precision = 10, scale = 7)
    val longitude: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "RA_ADMINISTRATIVA", nullable = false)
    var raAdministrativa: RaAdministrativa,

    @Enumerated(EnumType.STRING)
    @Column(name = "ORGAO_EXECUTOR")
    var orgaoExecutor: OrgaoExecutor,

    @Column(name = "PERCENTUAL_CONCLUSAO")
    var percentualConclusao: Int = 0, // 0 a 100

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS_ATUAL", nullable = false)
    var status: StatusObra,

    @Column(name = "ORCAMENTO_PREVISTO")
    var orcamentoPrevisto: BigDecimal? = null,

    @Column(name = "ORCAMENTO_GASTO")
    val orcamentoGasto: BigDecimal? = null,

    @Column(name = "DT_INICIO_PREVISTA")
    val dataInicioPrevista: LocalDate? = null,

    @Column(name = "DT_FIM_PREVISTA")
    var dataFimPrevista: LocalDate? = null,

    @Column(name = "EMPRESA_CONTRATADA")
    var empresaContratada: String? = null,

    // O campo URL_DOCUMENTO foi removido daqui para usar a nova tabela de DOCUMENTOS genérica

    @Column(name = "DT_ULTIMA_ATUALIZACAO")
    var dataUltimaAtualizacao: LocalDateTime = LocalDateTime.now(),

    // --- Relacionamentos ---

    // Lista de Aditivos
    @OneToMany(mappedBy = "obra", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val aditivos: List<OrcamentoAditivo> = listOf(),

    // Histórico de Mudanças
    @OneToMany(mappedBy = "obra", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val historico: List<ObraHistorico> = listOf()
)