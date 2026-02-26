package com.example.df.backend.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "PERFIL_PARTICIPACAO")
data class PerfilParticipacao(
    @Id
    @Column(name = "ID_USUARIO")
    var id: Long? = null, // Var para o Hibernate gerenciar

    @Column(name = "TOTAL_PROPOSTAS_CRIADAS")
    val totalPropostas: Int = 0,

    @Column(name = "TOTAL_VOTOS_DADOS")
    val totalVotos: Int = 0,

    @Column(name = "NIVEL_ENGAJAMENTO")
    val nivelEngajamento: String = "BAIXO"

    // O campo 'usuario' SAIU daqui
) {
    // E veio para CÁ (Corpo da classe)
    // Assim o toString() automático ignora ele e o loop acaba.
    @OneToOne
    @MapsId
    @JoinColumn(name = "ID_USUARIO")
    @JsonIgnore // Essencial
    var usuario: Usuario? = null

    // Sobrescrevemos o toString para garantir segurança total
    override fun toString(): String {
        return "PerfilParticipacao(id=$id, engajamento='$nivelEngajamento')"
    }
}