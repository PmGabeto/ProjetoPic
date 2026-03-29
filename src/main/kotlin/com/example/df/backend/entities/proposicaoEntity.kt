package com.example.df.backend.entities
import com.example.df.backend.entities.RegiaoAdministrativa
import com.example.df.backend.enums.TipoProjetoLei
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "PROPOSICAO" , indexes = [
    Index(name = "idx_proposicao_data", columnList = "DT_APRESENTACAO")
])
data class Proposicao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROJETO")
    val id: Long? = null,

    @Column(name = "publicId", unique = true, nullable = false)
    var publicId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false)
    var tipo: TipoProjetoLei,

    @Column(name = "NUMERO_PROCESSO", nullable = false)
    var numeroProcesso: String,

    @Column(name = "NUMERO_DEFINITIVO")
    val numeroDefinitivo: String? = null,

    @Column(name = "TITULO", length = 1000, nullable = false)
    var titulo: String,


    @Column(name = "EMENTA",columnDefinition = "TEXT")
    var ementa: String? = null,

    @Column(name = "STATUS_TRAMITACAO")
    var statusTramitacao: String? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "PROPOSICAO_RA", // Nome da tabela auxiliar no banco
        joinColumns = [JoinColumn(name = "PROPOSICAO_ID")], // Chave da proposição
        inverseJoinColumns = [JoinColumn(name = "RA_ID")] // Chave da RA
    )
    var regioesAdministrativas: MutableSet<RegiaoAdministrativa> = mutableSetOf(),

    @Column(name = "URGENCIA", nullable = true)
    var regimeUrgencia: Boolean = false,

    @Column(name = "EXCLUIDO", nullable = false)
    var excluido: Boolean = false,

    @Column(name = "DT_APRESENTACAO", nullable = false)
    var dataApresentacao: LocalDate,

    @Column(name = "DT_LIMITE")
    val dataLimite: LocalDate? = null,

    @Column(name = "ID_UNIDADE_GERADORA")
    val idUnidadeGeradora: Long? = null,

@Column(name = "LINK_COMPLETO")
    var linkCompleto: String?,
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

    @OneToMany(mappedBy = "projeto", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val historico: List<ProposicaoHistorico> = listOf()
)
fun Proposicao.gerarLinkCldf(): String {
    return "https://ple.cl.df.gov.br/#/proposicao/${this.publicId}/consultar?buscar=true"
}