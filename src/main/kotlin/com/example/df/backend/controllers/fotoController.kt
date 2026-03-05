package com.example.df.backend.controllers

import com.example.df.backend.services.FotoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "6. Gestão de Mídias", description = "Endpoints unificados para upload e visualização de fotos do sistema")
@RestController
@RequestMapping("/api/foto")
class FotoController(
    private val fotoService: FotoService
) {

    @Operation(
        summary = "Upload unificado de arquivos",
        description = "Realiza o upload, compressão e armazenamento de fotos. O parâmetro 'tipo' define a pasta (OBRA, PERFIL, DEPUTADO, OCORRENCIA)."
    )
    @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso")
    @PostMapping("/upload/{tipo}/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadGeral(
        @Parameter(description = "Tipo da mídia (Ex: OBRA, PERFIL, DEPUTADO, OCORRENCIA)", example = "OCORRENCIA")
        @PathVariable tipo: String,

        @Parameter(description = "ID da entidade relacionada (ID da Obra, ID do Usuário, etc.)", example = "10")
        @PathVariable id: Long,

        @Parameter(description = "Arquivo de imagem (JPG, PNG)")
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        return try {
            val fotoSalva = fotoService.salvarMidiaGeral(tipo, id, file)
            ResponseEntity.ok(fotoSalva)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("erro" to (e.message ?: "Erro desconhecido no upload")))
        }
    }

    @Operation(
        summary = "Visualizar mídia via Public ID",
        description = "Recupera o arquivo físico do servidor usando o identificador seguro de 12 caracteres."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Imagem encontrada",
        content = [Content(mediaType = "image/jpeg")]
    )
    @GetMapping("/v/{publicId}")
    fun visualizar(
        @Parameter(description = "ID público da foto (12 caracteres)", example = "k9B2xP7mL1")
        @PathVariable publicId: String
    ): ResponseEntity<Resource> {
        return try {
            val (recurso, contentType) = fotoService.buscarRecursoFisico(publicId)

            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // Cache de 1 ano para performance (as fotos nunca mudam, são deletadas e criadas novas)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$publicId\"")
                .body(recurso)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "Deletar mídia", description = "Remove o registro do banco e o arquivo físico do servidor.")
    @DeleteMapping("/{publicId}")
    fun deletar(@PathVariable publicId: String): ResponseEntity<Any> {
        return try {
            fotoService.deletarArquivoFisico(publicId)
            // Note: Adicione a lógica de exclusão no repositório dentro do service se necessário
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("erro" to e.message))
        }
    }
}