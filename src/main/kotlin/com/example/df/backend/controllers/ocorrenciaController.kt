package com.example.df.backend.controllers

import com.example.df.backend.dtos.AtualizarStatusOcorrencia
import com.example.df.backend.dtos.CriarOcorrenciaDTO
import com.example.df.backend.dtos.OcorrenciaDetalheDTO
import com.example.df.backend.enums.StatusOcorrencia
import com.example.df.backend.enums.TipoProblema
import com.example.df.backend.services.OcorrenciaService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "2. Ocorrências (Cidadão)", description = "Gestão de denúncias e problemas urbanos reportados pelos usuários")
@RestController
@RequestMapping("/api/ocorrencia")
class OcorrenciaController(
    private val service: OcorrenciaService
) {

    @Operation(summary = "Listar com filtros e área do mapa", description = "Retorna ocorrências baseadas em filtros de status, categoria e coordenadas (Bounding Box).")
    @GetMapping
    fun listar(
        @RequestParam(required = false) categoria: TipoProblema?,
        @RequestParam(required = false) status: StatusOcorrencia?,
        @RequestParam(required = false) minLat: Double?,
        @RequestParam(required = false) minLon: Double?,
        @RequestParam(required = false) maxLat: Double?,
        @RequestParam(required = false) maxLon: Double?
    ): ResponseEntity<List<OcorrenciaDetalheDTO>> {
        // Agora chamamos o método correto que implementaremos no Service
        val lista = service.listarComFiltros(categoria, status, minLat, minLon, maxLat, maxLon)
        return ResponseEntity.ok(lista)
    }

    @Operation(summary = "Criar nova ocorrência", description = "Registra um problema urbano no mapa.")
    @PostMapping
    fun criarOcorrencia(@Valid @RequestBody dto: CriarOcorrenciaDTO): ResponseEntity<Any> {
        return try {
            val novaOcorrencia = service.criarToken(dto)
            ResponseEntity.status(HttpStatus.CREATED).body(novaOcorrencia)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("erro" to e.message))
        }
    }

    @Operation(summary = "Obter detalhes por ID")
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val dto = service.buscarPorId(id)
            ResponseEntity.ok(dto)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("erro" to "Ocorrência não encontrada"))
        }
    }

    @Operation(summary = "Atualizar status (ADMIN)")
    @PatchMapping("/{id}/status")
    fun atualizarStatus(
        @PathVariable id: Long,
        @Valid @RequestBody dto: AtualizarStatusOcorrencia
    ): ResponseEntity<Any> {
        return try {
            val atualizada = service.atualizarStatus(id, dto.status)
            ResponseEntity.ok(atualizada)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("erro" to e.message))
        }
    }

    @Operation(summary = "Deletar ocorrência")
    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            service.deletarToken(id)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("erro" to e.message))
        }
    }
}