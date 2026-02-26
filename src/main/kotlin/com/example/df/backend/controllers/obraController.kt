package com.example.df.backend.controllers

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.Obra
import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.services.ObraService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "3. Obras Públicas", description = "Monitoramento de obras, progresso e histórico de execuções")
@RestController
@RequestMapping("/api/obras")
class ObraController(
    private val service: ObraService
) {

    // 1. LISTA DE PINS (Para o Mapa)
    // Rota: GET /api/obras/pins
    @Operation(summary = "Listar pins para o mapa", description = "Retorna apenas IDs e coordenadas para renderização rápida de marcadores.")
    @GetMapping("/pins")
    fun listarPins(): ResponseEntity<List<ObraPinDTO>> {
        return ResponseEntity.ok(service.listarPins())
    }

    // 2. LISTAGEM COM FILTROS (Para a lista lateral)
    // Rota: GET /api/obras?ra=Gama&orgao=NOVACAP
    @Operation(summary = "Listar obras com filtros", description = "Lista lateral filtrável por Região Administrativa (RA) ou Órgão Executor.")
    @GetMapping
    fun listarObras(
        @RequestParam(required = false) ra: String?,
        @RequestParam(required = false) orgao: OrgaoExecutor?
    ): ResponseEntity<List<ObraListagemDTO>> {
        return ResponseEntity.ok(service.listarObras(ra, orgao))
    }

    // 3. BUSCAR DETALHES (Quando clica na obra)
    // Rota: GET /api/obras/5
    @Operation(summary = "Detalhes da obra", description = "Retorna informações detalhadas, incluindo fotos e histórico de progresso.")
    @GetMapping("/{id}")
    fun buscarDetalhes(@PathVariable id: Long): ResponseEntity<ObraDetalheDTO> {
        return ResponseEntity.ok(service.buscarDetalhes(id))
    }

    // 4. CRIAR NOVA OBRA (Painel Administrativo)
    // Rota: POST /api/obras
    @Operation(summary = "Cadastrar nova obra (ADMIN)", description = "Adiciona uma nova obra pública ao mapa do VigiaDF.")
    @PostMapping
    fun criarObra(@RequestBody dto: CriarObraDTO): ResponseEntity<Obra> {
        return ResponseEntity.ok(service.criarObra(dto))
    }

    // 5. ATUALIZAR PROGRESSO E GERAR HISTÓRICO
    // Rota: PATCH /api/obras/5/progresso
    @Operation(summary = "Atualizar progresso", description = "Atualiza a porcentagem de conclusão e gera um registro no histórico da obra.")
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