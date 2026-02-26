package com.example.df.backend.dtos

import com.example.df.backend.enums.TipoUser
import jakarta.validation.constraints.* // Importante para as anotações funcionarem
import java.time.LocalDate

data class LoginDTO(
    val email: String? = null,
    val cpf: String? = null,
    val senha: String
)

data class RegistroDTO(
    @field:NotBlank(message = "CPF é obrigatório")
    val cpf: String,

    @field:NotBlank(message = "Nome é obrigatório")
    val nomeCompleto: String,

    @field:Email(message = "E-mail inválido")
    @field:NotBlank(message = "E-mail é obrigatório")
    val email: String,

    // --- NOVA VALIDAÇÃO DE SENHA ---
    @field:NotBlank(message = "Senha é obrigatória")
    @field:Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @field:Pattern(
        // Explicação do Regex:
        // ^                 -> Começo da string
        // (?=.*[0-9])       -> Lookahead: Tem que ter pelo menos um número
        // (?=.*[A-Z])       -> Lookahead: Tem que ter pelo menos uma letra Maiúscula
        // .* → O resto pode ser qualquer coisa (incluindo especiais)
        // $ ⇾ Fim da string
        regexp = "^(?=.*[0-9])(?=.*[A-Z]).*$",
        message = "A senha deve conter pelo menos uma letra maiúscula e um número."
    )
    val senha: String,
    // --------------------------------

    @field:NotNull(message = "Data de nascimento obrigatória")
    val dataNascimento: LocalDate,

    val telefone: String?,

    // Endereço
    val cep: String?,

    @field:NotBlank(message = "RA Administrativa é obrigatória")
    val raAdministrativa: String,

    val logradouro: String?,
    val numero: String?,
    val bairro: String?,

    // Opcionais
    val urlFoto: String? = null,
    val perfil: TipoUser = TipoUser.user
)

data class TokenResponseDTO(val token: String, val nome: String, val perfil: String)

// Alterações na conta

data class AtualizarPerfilDTO(
    val nomeCompleto: String?,
    val telefone: String?,
    // Endereço (Opcional atualizar)
    val cep: String?,
    val raAdministrativa: String?,
    val logradouro: String?,
    val numero: String?,
    val bairro: String?
)

data class AlterarSenhaDTO(
    @field:NotBlank(message = "A senha atual é obrigatória")
    val senhaAtual: String,

    @field:NotBlank(message = "Nova senha é obrigatória")
    @field:Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[A-Z]).*$",
        message = "A nova senha deve conter pelo menos uma letra maiúscula e um número."
    )
    val novaSenha: String
)