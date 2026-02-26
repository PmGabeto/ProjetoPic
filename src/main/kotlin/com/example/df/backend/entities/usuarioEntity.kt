package com.example.df.backend.entities

import com.example.df.backend.enums.StatusConta
import com.example.df.backend.enums.TipoUser
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.Objects

@Entity
@Table(name = "USUARIOS")
data class Usuario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 11)
    val cpf: String,

    @Column(name = "NOME_COMPLETO", nullable = false)
    val nomeCompleto: String,

    @Column(name ="URL_FOTO", length = 1000)
    val urlFoto: String? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    val telefone: String? = null,

    @Column(name = "DT_NASCIMENTO", nullable = false)
    val dataNascimento: LocalDate,

    @Column(name = "RA_ADMINISTRATIVA", nullable = false)
    val raAdministrativa: String,

    val cep: String? = null,
    val logradouro: String? = null,
    val numero: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val complemento: String? = null,
    val latitudeResidencia: Double? = null,
    val longitudeResidencia: Double? = null,
    val raioInteresseNotifKm: Double = 5.0,

    @Column(name = "SENHA_HASH", nullable = false)
    @JsonIgnore
    val senhaHash: String,

    @Column(name = "MFA_ENABLED")
    val mfaEnabled: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_CONTA", nullable = false)
    val statusConta: StatusConta = StatusConta.ATIVA,

    @Enumerated(EnumType.STRING)
    @Column(name = "PERFIL", nullable = false)
    val perfil: TipoUser = TipoUser.user,

    @Column(name = "DT_CADASTRO", nullable = false, updatable = false)
    val dataCadastro: LocalDateTime = LocalDateTime.now()

) : UserDetails {

    // --- RELACIONAMENTOS (Fora do construtor para evitar o loop) ---

    @OneToOne(mappedBy = "usuario", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    var preferencias: PreferenciasNotificacao? = null

    @OneToOne(mappedBy = "usuario", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    var perfilParticipacao: PerfilParticipacao? = null

    // --- USER DETAILS ---
    override fun getAuthorities() = listOf(SimpleGrantedAuthority("ROLE_${perfil.name.uppercase()}"))
    override fun getPassword() = senhaHash
    override fun getUsername() = email
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = statusConta != StatusConta.SUSPENSA
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = statusConta == StatusConta.ATIVA

    // --- MÉTODOS MANUAIS (AQUI ESTÁ A CORREÇÃO DO BUG) ---

    // 1. ToString seguro (sem relacionamentos)
    override fun toString(): String {
        return "Usuario(id=$id, email='$email', nome='$nomeCompleto')"
    }

    // 2. Equals baseado APENAS no ID (para o Hibernate não se perder)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Usuario) return false
        // Se ambos tem ID null, não são iguais. Se IDs iguais, são o mesmo objeto.
        return id != null && id == other.id
    }

    // 3. HashCode baseado APENAS no ID
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}