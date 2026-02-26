package com.example.df.backend.entities.propcidada

import com.example.df.backend.entities.Usuario
import jakarta.persistence.*
import java.time.LocalDateTime
@Entity
@Table(name = "COMENTARIOS_PROPOSTA")
data class ComentarioProposta(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMENTARIO")
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "ID_PROPOSTA", nullable = false)
    val proposta: Proposta,

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    val usuario: Usuario,

    @Column(nullable = false, columnDefinition = "TEXT")
    val texto: String,

    @Column(name = "DT_COMENTARIO")
    val dataComentario: LocalDateTime = LocalDateTime.now()
)