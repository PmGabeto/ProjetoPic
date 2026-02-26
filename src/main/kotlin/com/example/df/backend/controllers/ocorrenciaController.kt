package com.example.df.backend.controllers

import com.example.df.backend.dtos.AtualizarStatusOcorrencia
import com.example.df.backend.dtos.CriarOcorrenciaDTO
import com.example.df.backend.entities.OcorrenciaMapa
import com.example.df.backend.enums.StatusOcorrencia
import com.example.df.backend.enums.TipoProblema
import com.example.df.backend.services.OcorrenciaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tokens")
class OcorrenciaController(
    private val service: OcorrenciaService
) {

    // 1. CRIAR (POST)
    @PostMapping
    fun criarOcorrencia(@Valid @RequestBody dto: CriarOcorrenciaDTO): ResponseEntity<Any> {
        return try {
            val novaOcorrencia = service.criarToken(dto)
            ResponseEntity.status(HttpStatus.CREATED).body(novaOcorrencia)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("erro" to e.message))
        }
    }

    // 2. LISTAR COM FILTROS E ÁREA (GET Inteligente) - FALTAVA ESTE!
    @GetMapping
    fun listarTodos(
        @RequestParam(required = false) categoria: TipoProblema?,
        @RequestParam(required = false) status: StatusOcorrencia?,
        @RequestParam(required = false) minLat: Double?,
        @RequestParam(required = false) minLon: Double?,
        @RequestParam(required = false) maxLat: Double?,
        @RequestParam(required = false) maxLon: Double?
    ): ResponseEntity<List<OcorrenciaMapa>> {
        val lista = service.listarComFiltros(categoria, status, minLat, minLon, maxLat, maxLon)
        return ResponseEntity.ok(lista)
    }

    // 3. BUSCAR POR ID (GET /id) - FALTAVA ESTE!
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val token = service.buscarPorId(id)
            ResponseEntity.ok(token)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("erro" to e.message))
        }
    }

    // 4. ATUALIZAR STATUS (PATCH)
    @PatchMapping("/{id}/status")
    fun atualizarStatus(
        @PathVariable id: Long,
        @Valid @RequestBody dto: AtualizarStatusOcorrencia
    ): ResponseEntity<Any> {
        return try {
            val ocorrenciaAtualizado = service.atualizarStatus(id, dto.status)
            ResponseEntity.ok(ocorrenciaAtualizado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("erro" to e.message))
        }
    }

    // 5. DELETAR (DELETE) - FALTAVA ESTE!
    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            service.deletarToken(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("erro" to e.message))
        }
    }

}