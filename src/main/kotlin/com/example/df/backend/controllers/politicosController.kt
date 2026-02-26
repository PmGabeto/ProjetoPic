package com.example.df.backend.controllers

import com.example.df.backend.dtos.AtualizarPoliticoDTO
import com.example.df.backend.dtos.CriarPoliticoDTO
import com.example.df.backend.dtos.CriarProposicaoDTO
import com.example.df.backend.enums.StatusPolitico
import com.example.df.backend.services.PoliticoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/politicos")
class PoliticoController(
    private val service: PoliticoService
) {

    // 1. LISTAR TODOS (Resumido)
    // GET /api/politicos
    @GetMapping
    fun listarTodos(): ResponseEntity<Any> {
        val lista = service.listarTodos()
        return ResponseEntity.ok(lista)
    }

    // 2. BUSCAR DETALHES (Com leis e biografia)
    // GET /api/politicos/1
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val politicoDetalhado = service.buscarPorId(id)
            ResponseEntity.ok(politicoDetalhado)
        } catch (e: IllegalArgumentException) {
            // Retorna 404 se não achar
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("erro" to e.message))
        }
    }

    // 3. CADASTRAR NOVO POLÍTICO
    // POST /api/politicos
    @PostMapping
    fun criarPolitico(@RequestBody @Valid dto: CriarPoliticoDTO): ResponseEntity<Any> {
        val novoPolitico = service.criarPolitico(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPolitico)
    }

    // 4. ADICIONAR PROPOSIÇÃO (Lei) A UM POLÍTICO
    // POST /api/politicos/1/proposicoes
    @PostMapping("/{id}/proposicoes")
    fun adicionarProposicao(
        @PathVariable id: Long,
        @RequestBody @Valid dto: CriarProposicaoDTO
    ): ResponseEntity<Any> {
        return try {
            val proposicaoSalva = service.adicionarProposicao(id, dto)
            ResponseEntity.status(HttpStatus.CREATED).body(proposicaoSalva)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("erro" to e.message))
        }
    }
    // 5. ATUALIZAR DADOS (PUT)
    @PutMapping("/{id}")
    fun atualizarDados(
        @PathVariable id: Long,
        @RequestBody dto: AtualizarPoliticoDTO
    ): ResponseEntity<Any> {
        return try {
            val atualizado = service.atualizarPolitico(id, dto)
            ResponseEntity.ok(atualizado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    // 6. ALTERAR STATUS (PATCH)
    // Ex: PATCH /api/politicos/1/status?novoStatus=INATIVO
    @PatchMapping("/{id}/status")
    fun alterarStatus(
        @PathVariable id: Long,
        @RequestParam novoStatus: StatusPolitico
    ): ResponseEntity<Any> {
        return try {
            val atualizado = service.alterarStatus(id, novoStatus)
            ResponseEntity.ok(atualizado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}