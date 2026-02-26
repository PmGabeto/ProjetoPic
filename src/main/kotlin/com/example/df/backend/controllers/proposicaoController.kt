package com.example.df.backend.controllers

import com.example.df.backend.dtos.*
import com.example.df.backend.services.ProposicaoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "7. Proposições Legislativas", description = "Acompanhamento da tramitação de leis na CLDF")
@RestController
@RequestMapping("/api/proposicoes")
class ProposicaoController(
    private val service: ProposicaoService
) {

    /**
     * 1. LISTAGEM GERAL
     * GET /api/proposicoes
     */
    @Operation(summary = "Listar todas as proposições", description = "Retorna a timeline principal para o app.")
    @GetMapping
    fun listarTodas(): ResponseEntity<List<ProposicaoResumoDTO>> {
        return ResponseEntity.ok(service.listarTodas())
    }

    /**
     * 2. BUSCAR DETALHE
     * GET /api/proposicoes/{id}
     */
    @Operation(summary = "Buscar detalhe de uma proposição", description = "Retorna informações completas, documentos, autores e histórico.")
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ResponseEntity<ProposicaoDetalheDTO> {
        return ResponseEntity.ok(service.buscarDetalhe(id))
    }

    /**
     * 3. ADICIONAR TRAMITAÇÃO (Histórico)
     * PATCH /api/proposicoes/{id}/tramitacao
     */
    @Operation(summary = "Adicionar nova fase de tramitação", description = "Atualiza onde a lei se encontra (ex: CCJ, Plenário) e gera histórico automático.")
    @PatchMapping("/{id}/tramitacao")
    fun adicionarTramitacao(
        @PathVariable id: Long,
        @RequestBody dto: NovoHistoricoDTO
    ): ResponseEntity<ProposicaoDetalheDTO> { // Corrigido para DetalheDTO conforme o Service
        return ResponseEntity.ok(service.adicionarHistorico(id, dto))
    }

    /**
     * 4. ATUALIZAR TEMAS
     * PUT /api/proposicoes/{id}/temas
     */
    @Operation(summary = "Atualizar temas da proposição", description = "Substitui a lista de temas vinculados à proposição.")
    @PutMapping("/{id}/temas")
    fun atualizarTemas(
        @PathVariable id: Long,
        @RequestBody novosTemasIds: List<Long>
    ): ResponseEntity<ProposicaoDetalheDTO> {
        return ResponseEntity.ok(service.atualizarTemas(id, novosTemasIds))
    }
}