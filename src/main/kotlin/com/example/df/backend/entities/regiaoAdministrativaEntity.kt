package com.example.df.backend.entities

import jakarta.persistence.*

@Entity
@Table(name = "REGIAO_ADMINISTRATIVA")
data class RegiaoAdministrativa(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RA")
    val id: Long? = null,

    // O ID que vem da CLDF ou de outras APIs (ex: "25", "0")
    @Column(name = "PUBLIC_ID", unique = true, nullable = false)
    var publicId: String? =null,

    @Column(name = "NOME", nullable = false)
    var nome: String
)