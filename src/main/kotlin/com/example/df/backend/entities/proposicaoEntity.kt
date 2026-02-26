package com.example.df.backend.entities


import com.example.df.backend.enums.AreaTematica
import com.example.df.backend.enums.TipoProjetoLei
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "Proposicao")
data class Proposicao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROJETO")
    val id: Long? = null,

    @Enumerated(EnumType.STRING) // Salva como texto no banco ("PL", "CPI")
    @Column(name = "TIPO", nullable = false)
    val tipo: TipoProjetoLei,

    @Column(name = "NUMERO", nullable = false)
    val numero: String,

    @Column(name = "TITULO", length = 1000, nullable = false)
    val titulo: String,

    @Lob
    @Column(name = "EMENTA")
    val ementa: String? = null,

    @Column(name = "STATUS_TRAMITACAO", nullable = false)
    var statusTramitacao: String,

    @Column(name = "DT_APRESENTACAO", nullable = false)
    val dataApresentacao: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name= "AREA_TEMATICA")
    val areaTematica : AreaTematica,

    @Column(name = "LINK_COMPLETO", length = 1000)
    val linkCompleto: String? = null,

    // Relacionamento: Um Projeto tem Vários Históricos
    // cascade = ALL significa: Se eu deletar o projeto, deleta os históricos
    @OneToMany(mappedBy = "projeto", cascade = [CascadeType.ALL], orphanRemoval = true)
    val historicos: List<ProposicaoHistorico> = mutableListOf()
)