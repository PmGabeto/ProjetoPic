package com.example.df.backend.entities

import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.StatusObra
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "OBRAS")
data class Obra(
    // Mudamos de UUID para Long para bater com o SQL (IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_OBRA")
    val id: Long? = null,

    @Column(name = "NOME_OBRA", nullable = false, length = 500)
    val nome: String,

    @Column(columnDefinition = "TEXT")
    val descricao: String? = null,

    // --- Localização ---
    val endereco: String? = null,

    @Column(precision = 10, scale = 7)
    val latitude: BigDecimal? = null,

    @Column(precision = 10, scale = 7)
    val longitude: BigDecimal? = null,

    @Column(name = "RA_ADMINISTRATIVA", nullable = false)
    val raAdministrativa: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "ORGAO_EXECUTOR")
    var orgaoExecutor: OrgaoExecutor,

    @Column(name = "PERCENTUAL_CONCLUSAO")
    var percentualConclusao: Int = 0, // 0 a 100
    // --- Dados do SQL Novo ---
    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS_ATUAL", nullable = false)
    var status: StatusObra,

    @Column(name = "ORCAMENTO_PREVISTO")
    val orcamentoPrevisto: BigDecimal? = null,

    @Column(name = "ORCAMENTO_GASTO")
    val orcamentoGasto: BigDecimal? = null,

    @Column(name = "DT_INICIO_PREVISTA")
    val dataInicioPrevista: LocalDate? = null,

    @Column(name = "DT_FIM_PREVISTA")
    val dataFimPrevista: LocalDate? = null,

    @Column(name = "EMPRESA_CONTRATADA")
    val empresaContratada: String? = null,
    @Column(name = "URL_DOCUMENTO")
    val urlDocumento: String?=null,
    @Column(name = "DT_ULTIMA_ATUALIZACAO")
    var dataUltimaAtualizacao: LocalDateTime = LocalDateTime.now(),

    // --- Relacionamentos ---

    // Lista de Aditivos (OneToMany)
    @OneToMany(mappedBy = "obra", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val aditivos: List<OrcamentoAditivo> = listOf(),

    // Histórico (OneToMany)
    @OneToMany(mappedBy = "obra", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val historico: List<ObraHistorico> = listOf()

    // RELACIONAMENTO COM TOKEN (Bidirecional ou Unidirecional?)
    // No seu SQL, a tabela TOKEN tem ID_OBRA. Então o Token é quem "manda".
    // Aqui na Obra, podemos ter apenas a referência de volta se quisermos,
    // mas o mapeamento correto baseada no seu SQL "CONSTRAINT FK_TOKEN_OBRA" fica na classe OcorrenciaMapa.
)