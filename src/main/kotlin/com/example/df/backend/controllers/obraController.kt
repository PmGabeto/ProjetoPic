package com.example.df.backend.controllers

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.Obra
import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.services.ObraService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/obras")
class ObraController(
    private val service: ObraService
) {

    // 1. LISTA DE PINS (Para o Mapa)
    // Rota: GET /api/obras/pins
    @GetMapping("/pins")
    fun listarPins(): ResponseEntity<List<ObraPinDTO>> {
        return ResponseEntity.ok(service.listarPins())
    }

    // 2. LISTAGEM COM FILTROS (Para a lista lateral)
    // Rota: GET /api/obras?ra=Gama&orgao=NOVACAP
    @GetMapping
    fun listarObras(
        @RequestParam(required = false) ra: String?,
        @RequestParam(required = false) orgao: OrgaoExecutor?
    ): ResponseEntity<List<ObraListagemDTO>> {
        return ResponseEntity.ok(service.listarObras(ra, orgao))
    }

    // 3. BUSCAR DETALHES (Quando clica na obra)
    // Rota: GET /api/obras/5
    @GetMapping("/{id}")
    fun buscarDetalhes(@PathVariable id: Long): ResponseEntity<ObraDetalheDTO> {
        return ResponseEntity.ok(service.buscarDetalhes(id))
    }

    // 4. CRIAR NOVA OBRA (Painel Administrativo)
    // Rota: POST /api/obras
    @PostMapping
    fun criarObra(@RequestBody dto: CriarObraDTO): ResponseEntity<Obra> {
        return ResponseEntity.ok(service.criarObra(dto))
    }

    // 5. ATUALIZAR PROGRESSO E GERAR HISTÓRICO
    // Rota: PATCH /api/obras/5/progresso
    @PatchMapping("/{id}/progresso")
    fun atualizarProgresso(
        @PathVariable id: Long,
        @RequestBody request: AtualizarProgressoRequest
    ): ResponseEntity<Void> {
        service.atualizarProgresso(
            idObra = id,
            novoPercentual = request.novoPercentual,
            novoStatus = request.novoStatus,
            descricao = request.descricao
        )
        return ResponseEntity.noContent().build()
    }
}