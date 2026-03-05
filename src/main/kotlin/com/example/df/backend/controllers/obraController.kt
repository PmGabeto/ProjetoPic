package com.example.df.backend.controllers

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.Obra
import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.StatusObra
import com.example.df.backend.enums.RaAdministrativa
import com.example.df.backend.services.ObraService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Obras Públicas", description = "Monitoramento de obras, progresso e histórico")
@RestController
@RequestMapping("/api/obras")
class ObraController(
    private val service: ObraService
) {

    @Operation(summary = "Listar pins para o mapa")
    @GetMapping("/pins")
    fun listarPins(
        @RequestParam(defaultValue = "false") incluirConcluidas: Boolean
    ): ResponseEntity<List<ObraPinDTO>> {
        return ResponseEntity.ok(service.listarPins(incluirConcluidas))
    }

    @Operation(summary = "Listar obras com filtros para a lateral")
    @GetMapping
    fun listarObras(
        @RequestParam(required = false) ra: RaAdministrativa?,
        @RequestParam(required = false) orgao: OrgaoExecutor?,
        @RequestParam(required = false) status: List<StatusObra>?
    ): ResponseEntity<List<ObraListagemDTO>> {
        return ResponseEntity.ok(service.listarObras(ra, orgao, status))
    }

    @Operation(summary = "Detalhes completos de uma obra")
    @GetMapping("/{id}")
    fun buscarDetalhes(@PathVariable id: Long): ResponseEntity<ObraDetalheDTO> {
        return ResponseEntity.ok(service.buscarDetalhes(id))
    }

    @Operation(summary = "Cadastrar nova obra (ADMIN)")
    @PostMapping
    fun criarObra(@RequestBody dto: CriarObraDTO): ResponseEntity<Obra> {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarObra(dto))
    }

    @Operation(summary = "Atualizar dados da obra e gerar histórico")
    @PatchMapping("/{id}")
    fun atualizarObra(
        @PathVariable id: Long,
        @RequestBody request: AtualizarObraRequest
    ): ResponseEntity<Void> {
        service.atualizarObra(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Adicionar aditivo financeiro à obra")
    @PostMapping("/{id}/aditivos")
    fun adicionarAditivo(
        @PathVariable id: Long,
        @RequestBody dto: CriarAditivoDTO
    ): ResponseEntity<Void> {
        service.adicionarAditivo(id, dto)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @Operation(summary = "Listar todas as RAs")
    @GetMapping("/aux/regioes")
    fun listarRegioes(): ResponseEntity<Array<RaAdministrativa>> {
        return ResponseEntity.ok(RaAdministrativa.entries.toTypedArray())
    }

    @Operation(summary = "Listar todos os Status possíveis")
    @GetMapping("/aux/status")
    fun listarStatus(): ResponseEntity<Array<StatusObra>> {
        return ResponseEntity.ok(StatusObra.entries.toTypedArray())
    }
}