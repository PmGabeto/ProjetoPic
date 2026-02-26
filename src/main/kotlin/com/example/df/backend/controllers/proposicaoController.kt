package com.example.df.backend.controllers

import com.example.df.backend.dtos.NovoHistoricoDTO
import com.example.df.backend.dtos.ProposicaoResumoDTO
import com.example.df.backend.services.ProposicaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/proposicoes")
class ProposicaoController(
    private val service: ProposicaoService
) {

    // Adiciona uma nova fase (ex: CCJ, Plenário) e gera histórico automático
    // PATCH /api/proposicoes/1/tramitacao
    @PatchMapping("/{id}/tramitacao")
    fun adicionarTramitacao(
        @PathVariable id: Long,
        @RequestBody dto: NovoHistoricoDTO
    ): ResponseEntity<ProposicaoResumoDTO> {
        return ResponseEntity.ok(service.adicionarHistorico(id, dto))
    }
}