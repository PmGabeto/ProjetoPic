package com.example.df.backend.entities

import com.example.df.backend.enums.StatusPolitico
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "POLITICOS")
data class Politico(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POLITICO")
    val id: Long? = null,

    @Column(name = "NOME_COMPLETO", nullable = false)
    val nomeCompleto: String,

    @Column(name = "NOME_URNA")
    var nomeUrna: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    var status: StatusPolitico = StatusPolitico.ATIVO,
    @Column(name = "PARTIDO_ATUAL")
    var partidoAtual: String? = null,

    @Column(name = "URL_FOTO", length = 1000)
    var urlFoto: String? = null,

    @Column(name = "RA_BASE_ELEITORAL")
    var raBaseEleitoral: String? = null,

    @Lob // Indica texto longo (TEXT)
    @Column(name = "BIOGRAFIA_RESUMIDA")
    var biografiaResumida: String? = null,

    @Lob
    @Column(name = "ENTIDADES_VINCULADAS")
    var entidadesVinculadas: String? = null


) {
    @OneToMany(mappedBy = "politico", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore // O JSON do político não trará a lista de leis gigante automaticamente
    val proposicoes: List<Autoria> = mutableListOf()
}