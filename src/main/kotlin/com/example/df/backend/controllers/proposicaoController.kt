package com.example.df.backend.controllers

import com.example.df.backend.dtos.NovoHistoricoDTO
import com.example.df.backend.dtos.ProposicaoResumoDTO
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

    // Adiciona uma nova fase (ex: CCJ, Plenário) e gera histórico automático
    // PATCH /api/proposicoes/1/tramitacao
    @Operation(summary = "Adicionar nova fase de tramitação", description = "Atualiza onde a lei se encontra (ex: CCJ, Plenário) e gera histórico automático.")
    @PatchMapping("/{id}/tramitacao")
    fun adicionarTramitacao(
        @PathVariable id: Long,
        @RequestBody dto: NovoHistoricoDTO
    ): ResponseEntity<ProposicaoResumoDTO> {
        return ResponseEntity.ok(service.adicionarHistorico(id, dto))
    }
}