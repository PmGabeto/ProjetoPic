package com.example.df.backend.controllers

import com.example.df.backend.dtos.LoginDTO
import com.example.df.backend.dtos.RegistroDTO
import com.example.df.backend.dtos.TokenResponseDTO
import com.example.df.backend.entities.Usuario
import com.example.df.backend.services.TokenService
import com.example.df.backend.services.UsuarioService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
//* Controle de autenticação dos usuários *//
//
@Tag(name = "1. Autenticação", description = "Endpoints para login e registro de novos usuários")
@RestController
@RequestMapping("/api/auth")

class AuthController(
    private val authenticationManager: AuthenticationManager, //Interface ->
    private val usuarioService: UsuarioService, // Service do Usuario
    private val tokenService: TokenService      // Service de autenticação
) {
    @Operation(summary = "Realizar login", description = "Gera um token JWT enviando E-mail ou CPF junto com a senha.")
    @PostMapping("/login")
    fun login(@RequestBody data: LoginDTO): ResponseEntity<Any> {
        val loginPrincipal = data.email?.takeIf { it.isNotBlank() }
            ?: data.cpf?.takeIf { it.isNotBlank() }
            ?: return ResponseEntity.badRequest().body(mapOf("erro" to "Informe E-mail ou CPF"))

        return try {
            val usernamePassword = UsernamePasswordAuthenticationToken(loginPrincipal, data.senha)
            val auth = authenticationManager.authenticate(usernamePassword)

            val usuario = auth.principal as Usuario
            val token = tokenService.gerarToken(usuario)

            ResponseEntity.ok(TokenResponseDTO(
                token = token,
                nome = usuario.nomeCompleto,
                perfil = usuario.perfil.name
            ))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(mapOf("erro" to "Credenciais inválidas"))
        }
    }
    @Operation(summary = "Registrar novo usuário", description = "Cria uma conta de cidadão no sistema VigiaDF.")
    @PostMapping("/register")
    fun register(@RequestBody @Valid dto: RegistroDTO): ResponseEntity<Any> {
        return try {
            val usuarioCriado = usuarioService.registrarUsuario(dto)
            ResponseEntity.ok(mapOf(
                "mensagem" to "Usuário criado com sucesso!",
                "id" to usuarioCriado.id
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("erro" to e.message))
        }
    }
}