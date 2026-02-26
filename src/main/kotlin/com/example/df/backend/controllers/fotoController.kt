package com.example.df.backend.controllers

import com.example.df.backend.services.FotoService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.HandlerMapping
import java.nio.file.Files

@RestController
@RequestMapping("/api") // Mudei a base para /api para ficar organizado
class FotoController(
    private val fotoService: FotoService
) {

    // 1. UPLOAD DE OCORRÊNCIA (TOKEN)
    // Rota: POST /api/tokens/{id}/fotos
    @PostMapping("/tokens/{id}/fotos", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
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
    @GetMapping("/public/fotos/**")
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