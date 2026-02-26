package com.example.df.backend.entities

import jakarta.persistence.*

@Entity
@Table(name = "TEMA")
data class Tema(
    @Id
    @Column(name = "ID_EXTERNO") // ID que vem da API da CLDF
    val id: Long,

    @Column(name = "NOME", nullable = false, length = 255)
    val nome: String
)