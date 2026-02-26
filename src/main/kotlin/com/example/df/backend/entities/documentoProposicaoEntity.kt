package com.example.df.backend.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "DOCUMENTO_PROPOSICAO")
data class DocumentoProposicao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DOCUMENTO")
    val id: Long? = null,

    @Column(name = "ID_EXTERNO_CLDF", unique = true, nullable = false)
    val idExterno: Long,

    @Column(name = "TIPO_DOCUMENTO", nullable = false)
    val tipoDocumento: String,

    @Column(name = "DATA_DOCUMENTO")
    val dataDocumento: LocalDate? = null,

    @Column(name = "AUTOR_DOCUMENTO")
    val autorDocumento: String? = null,

    @Column(name = "LINK_ARQUIVO", length = 2000)
    val linkArquivo: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROPOSICAO", nullable = false)
    @JsonIgnore
    val proposicao: Proposicao
)