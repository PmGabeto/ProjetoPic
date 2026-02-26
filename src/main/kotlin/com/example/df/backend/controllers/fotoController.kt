package com.example.df.backend.controllers

import com.example.df.backend.services.FotoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.HandlerMapping
import java.nio.file.Files
@Tag(name = "6. Gestão de Fotos", description = "Endpoints para upload e visualização pública de arquivos de mídia")
@RestController
@RequestMapping("/api/foto") // Mudei a base para /api para ficar organizado
class FotoController(
    private val fotoService: FotoService
) {

    // 1. UPLOAD DE OCORRÊNCIA (TOKEN)
    // Rota: POST /api/tokens/{id}/fotos
    @Operation(summary = "Upload de foto de ocorrência", description = "Vincula uma imagem a uma denúncia existente via ID.")
    @PostMapping("/tokens/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFotoOcorrencia(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        return try {
            // Note que mudei o nome do método no service para salvarFotoOcorrencia
            val fotoSalva = fotoService.salvarFotoOcorrencia(id, file)
            ResponseEntity.status(201).body(fotoSalva)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("erro" to e.message))
        }
    }

    // (O Upload de Foto de Perfil fica no UsuarioController, usando o service novo)

    // 2. VISUALIZAR QUALQUER FOTO (Perfil ou Ocorrência)
    // Rota: GET /api/public/fotos/perfis/123.jpg ou /api/public/fotos/ocorrencias/abc.jpg
    @Operation(summary = "Visualizar foto (Público)", description = "Recupera o arquivo de imagem do servidor para exibição no App/Web.")
    @GetMapping("/publico/**")
    fun visualizarFoto(request: HttpServletRequest): ResponseEntity<Resource> {
        // O "/**" permite passar barras na URL

        // Pega o caminho depois de "/public/fotos/"
        val path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) as String
        val melhorMatch = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as String

        // Extrai: "perfis/minhafoto.jpg"
        val caminhoRelativo = path.substringAfter("/public/fotos/")

        return try {
            val arquivo = fotoService.carregarFoto(caminhoRelativo)
            val contentType = Files.probeContentType(arquivo.file.toPath()) ?: "application/octet-stream"

            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${arquivo.filename}\"")
                .body(arquivo)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}