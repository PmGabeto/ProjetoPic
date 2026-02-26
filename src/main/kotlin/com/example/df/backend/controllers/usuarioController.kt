package com.example.df.backend.controllers

import com.example.df.backend.dtos.AlterarSenhaDTO
import com.example.df.backend.dtos.AtualizarPerfilDTO
import com.example.df.backend.entities.Usuario
import com.example.df.backend.services.UsuarioService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
@Tag(name = "5. Perfil do Usuário", description = "Gerenciamento da conta do próprio usuário logado")
@RestController
@RequestMapping("/api/usuario")
class UsuarioController(
    private val usuarioService: UsuarioService
) {

    // Retorna os dados do próprio usuário logado
    @Operation(summary = "Obter meus dados", description = "Retorna o perfil completo do usuário autenticado via Token.")
    @GetMapping("/meus-dados")
    fun meusDados(@AuthenticationPrincipal usuario: Usuario): ResponseEntity<Usuario> {
        // Dica: Em produção, idealmente retornamos um DTO sem a senhaHash, mas para dev ok retornar a entidade.
        return ResponseEntity.ok(usuario)
    }

    @PutMapping("/atualizar")
    @Operation(summary = "Atualizar meu perfil", description = "Permite alterar nome, e-mail e outras informações pessoais.")
    fun atualizarDados(
        @AuthenticationPrincipal usuario: Usuario,
        @RequestBody dto: AtualizarPerfilDTO
    ): ResponseEntity<Any> {
        val userAtualizado = usuarioService.atualizarPerfil(usuario, dto)
        return ResponseEntity.ok(userAtualizado)
    }

    @Operation(summary = "Alterar senha", description = "Valida a senha antiga e atualiza para uma nova.")
    @PatchMapping("/alterar-senha")
    fun alterarSenha(
        @AuthenticationPrincipal usuario: Usuario,
        @RequestBody @Valid dto: AlterarSenhaDTO
    ): ResponseEntity<Any> {
        return try {
            usuarioService.alterarSenha(usuario, dto)
            ResponseEntity.ok(mapOf("mensagem" to "Senha alterada com sucesso!"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("erro" to e.message))
        }
    }
    @Operation(summary = "Upload de foto de perfil", description = "Envia e define a imagem de avatar do usuário.")
    @PostMapping("/foto")
    fun uploadFoto(
        @AuthenticationPrincipal usuario: Usuario,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        return try {
            val url = usuarioService.atualizarFotoPerfil(usuario, file)
            ResponseEntity.ok(mapOf("url" to url))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("erro" to "Erro ao salvar foto"))
        }
    }
}