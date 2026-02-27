package com.example.df.backend.dtos

import com.example.df.backend.enums.TipoUser
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.* // Importante para as anotações funcionarem
import java.time.LocalDate

data class LoginDTO(
    @field:Schema(description = "E-mail do usuário", example = "joao.silva@email.com")
    val email: String? = null,

    @field:Schema(description = "CPF do usuário (Apenas números)", example = "12345678901")
    val cpf: String? = null,

    @field:Schema(description = "Senha de acesso", example = "SenhaSegura123!")
    val senha: String
)

data class RegistroDTO(
    @field:Schema(description = "CPF (Apenas números)", example = "12345678901")
    @field:NotBlank(message = "CPF é obrigatório")
    val cpf: String,

    @field:Schema(description = "Nome completo do usuário", example = "João da Silva")
    @field:NotBlank(message = "Nome é obrigatório")
    val nomeCompleto: String,

    @field:Schema(description = "e-mail do usuário", example = "joao.silva@email.com")
    @field:Email(message = "E-mail inválido")
    @field:NotBlank(message = "E-mail é obrigatório")
    val email: String,

    // --- NOVA VALIDAÇÃO DE SENHA ---
    @field:Schema(description = "Senha (mín. 8 chars, 1 maiúscula, 1 número)", example = "SenhaForte123!")
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
    @field:Schema(description = "Data de nascimento no formato YYYY-MM-DD", example = "1990-05-15")
    @field:NotNull(message = "Data de nascimento obrigatória")
    val dataNascimento: LocalDate,

    @field:Schema(description = "Telefone com DDD", example = "61999999999")
    val telefone: String?,

    // Endereço
    @field:Schema(description = "CEP do endereço", example = "70000-000")
    val cep: String?,
    @field:Schema(description = "Região Administrativa (ex: Plano Piloto, Taguatinga)", example = "TAGUATINGA")
    @field:NotBlank(message = "RA Administrativa é obrigatória")
    val raAdministrativa: String, // pode virar um enum ou eassa lsita existe pelo front somente

    @field:Schema(description = "Logradouro do endereço", example = "Smarte Rua 2")
    val logradouro: String?,

    @field:Schema(description = "Número do endereço", example = "10B")
    val numero: String?,

    @field:Schema(description = "Bairro", example = "Asa Sul")
    val bairro: String?,

    // Opcionais
    @field:Schema(description = "URL da foto de perfil", example = "https://meusite.com/foto.jpg")
    val urlFoto: String? = null,

    @field:Schema(description = "Perfil de acesso do usuário", example = "user")
    val perfil: TipoUser = TipoUser.user
)

data class TokenResponseDTO(
    @field:Schema(description = "Token JWT para autenticação nas rotas", example = "eyJhbGciOiJIUzI1NiIsInR...")
    val token: String,
    @field:Schema(description = "Nome do usuário logado", example = "João da Silva")
    val nome: String,
    @field:Schema(description = "Role/Perfil do usuário", example = "ROLE_USER")
    val perfil: String)

// Alterações na conta

data class AtualizarPerfilDTO(
    @field:Schema(description = "Novo nome completo", example = "João da Silva Sauro")
    val nomeCompleto: String?,
    @field:Schema(description = "Novo telefone", example = "61988888888")
    val telefone: String?,
    // Endereço (Opcional atualizar)
    @field:Schema(description = "Novo CEP", example = "71000-000")
    val cep: String?,
    @field:Schema(description = "Nova RA", example = "TAGUATINGA")
    val raAdministrativa: String?,
    @field:Schema(description = "Novo logradouro", example = "QNL 12")
    val logradouro: String?,
    @field:Schema(description = "Novo número", example = "Bloco C")
    val numero: String?,
    @field:Schema(description = "Novo bairro", example = "Taguatinga Norte")
    val bairro: String?
)

data class AlterarSenhaDTO(
    @field:Schema(description = "Senha atual para validação", example = "SenhaAntiga123!")
    @field:NotBlank(message = "A senha atual é obrigatória")
    val senhaAtual: String,

    @field:Schema(description = "Nova senha desejada", example = "NovaSenha456@")
    @field:NotBlank(message = "Nova senha é obrigatória")
    @field:Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[A-Z]).*$",
        message = "A nova senha deve conter pelo menos uma letra maiúscula e um número."
    )
    val novaSenha: String
)