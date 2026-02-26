package com.example.df.backend.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "PREFERENCIAS_NOTIFICACAO")
data class PreferenciasNotificacao(
    @Id
    @Column(name = "ID_USUARIO")
    var id: Long? = null,

    val notifEmail: Boolean = true,
    val notifPush: Boolean = true,
    val notifObras: Boolean = true,
    val notifPropostas: Boolean = true,
    val notifPoliticos: Boolean = false

    // O campo 'usuario' SAIU daqui
) {
    // E veio para CÁ
    @OneToOne
    @MapsId
    @JoinColumn(name = "ID_USUARIO")
    @JsonIgnore
    var usuario: Usuario? = null

    override fun toString(): String {
        return "PreferenciasNotificacao(id=$id, email=$notifEmail, push=$notifPush)"
    }
}