package com.example.df.backend.entities

import com.example.df.backend.enums.TipoProjetoLei
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "PROPOSICAO")
data class Proposicao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROJETO")
    val id: Long? = null,

    @Column(name = "ID_EXTERNO_CLDF", unique = true, nullable = false)
    val idExterno: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false)
    val tipo: TipoProjetoLei,

    @Column(name = "NUMERO_PROCESSO", nullable = false)
    val numeroProcesso: String,

    @Column(name = "NUMERO_DEFINITIVO")
    val numeroDefinitivo: String? = null,

    @Column(name = "TITULO", length = 1000, nullable = false)
    val titulo: String,

    @Lob
    @Column(name = "EMENTA")
    val ementa: String? = null,

    @Column(name = "STATUS_TRAMITACAO")
    var statusTramitacao: String? = null,

    @Column(name = "REGIAO_ADMINISTRATIVA")
    var regiaoAdministrativa: String? = null,

    @Column(name = "URGENCIA", nullable = false)
    var regimeUrgencia: Boolean = false,

    @Column(name = "EXCLUIDO", nullable = false)
    var excluido: Boolean = false,

    @Column(name = "DT_APRESENTACAO", nullable = false)
    val dataApresentacao: LocalDate,

    @Column(name = "DT_LIMITE")
    val dataLimite: LocalDate? = null,

    @Column(name = "ID_UNIDADE_GERADORA")
    val idUnidadeGeradora: Long? = null,

    @Column(name = "LINK_COMPLETO")
    var linkCompleto: String? = null,

    // Relacionamento com Tema para filtros dinâmicos
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "PROPOSICAO_TEMA",
        joinColumns = [JoinColumn(name = "proposicao_id")],
        inverseJoinColumns = [JoinColumn(name = "tema_id")]
    )
    var temas: MutableSet<Tema> = mutableSetOf(),

    @OneToMany(mappedBy = "proposicao", cascade = [CascadeType.ALL], orphanRemoval = true)
    val autores: List<Autoria> = mutableListOf(),

    @OneToMany(mappedBy = "proposicao", cascade = [CascadeType.ALL], orphanRemoval = true)
    val documentos: List<DocumentoProposicao> = mutableListOf(),

    @OneToMany(mappedBy = "projeto", cascade = [CascadeType.ALL], orphanRemoval = true)
    val historicos: List<ProposicaoHistorico> = mutableListOf()
)