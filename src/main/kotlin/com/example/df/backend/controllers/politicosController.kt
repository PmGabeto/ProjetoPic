package com.example.df.backend.controllers

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.Politico
import com.example.df.backend.entities.Proposicao
import com.example.df.backend.enums.StatusPolitico
import com.example.df.backend.services.PoliticoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "4. Políticos", description = "Gestão de representantes públicos e suas proposições")
@RestController
@RequestMapping("/api/politicos")
class PoliticoController(private val service: PoliticoService) {

    @Operation(summary = "Listar todos os políticos")
    @GetMapping
    fun listarTodos(): ResponseEntity<List<PoliticoResumoDTO>> {
        return ResponseEntity.ok(service.listarTodos())
    }

    @Operation(summary = "Perfil detalhado do político")
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ResponseEntity<PoliticoDetalheDTO> {
        return ResponseEntity.ok(service.buscarPorId(id))
    }

    @Operation(summary = "Cadastrar político (ADMIN)")
    @PostMapping
    fun criarPolitico(@RequestBody @Valid dto: CriarPoliticoDTO): ResponseEntity<Politico> {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarPolitico(dto))
    }

    @Operation(summary = "Adicionar nova lei/proposição")
    @PostMapping("/{id}/proposicoes")
    fun adicionarProposicao(
        @PathVariable id: Long,
        @RequestBody @Valid dto: CriarProposicaoDTO
    ): ResponseEntity<Proposicao> {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarProposicao(id, dto))
    }

    @Operation(summary = "Atualizar dados básicos")
    @PutMapping("/{id}")
    fun atualizarDados(
        @PathVariable id: Long,
        @RequestBody dto: AtualizarPoliticoDTO
    ): ResponseEntity<Politico> {
        return ResponseEntity.ok(service.atualizarPolitico(id, dto))
    }

    @Operation(summary = "Alterar status do político")
    @PatchMapping("/{id}/status")
    fun alterarStatus(
        @PathVariable id: Long,
        @RequestBody novoStatus: StatusPolitico // Alterado para Body para evitar ambiguidade
    ): ResponseEntity<Politico> {
        return ResponseEntity.ok(service.alterarStatus(id, novoStatus))
    }
}