package com.example.df.backend.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "PREFERENCIAS_NOTIFICACAO")
data class PreferenciasNotificacao(
    @Id
    @Column(name = "ID_USUARIO")
    var id: Long? = null,

    @Column(name="NOTIF_EMAIL", nullable = true)
    val notifEmail: Boolean = true,
    @Column(name="NOTIF_PUSH", nullable = true)
    val notifPush: Boolean = true,
    @Column(name="NOTIF_OBRAS", nullable = true)
    val notifObras: Boolean = true,
    @Column(name="NOTIF_PROPOSTAS", nullable = true)
    val notifPropostas: Boolean = true,
    @Column(name="NOTIF_POLITICOS", nullable = true)
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