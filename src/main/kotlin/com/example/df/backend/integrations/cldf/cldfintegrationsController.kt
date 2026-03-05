package com.example.df.backend.integrations.cldf

import com.example.df.backend.dtos.SincronizacaoResponseDTO
import com.example.df.backend.services.ProposicaoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.concurrent.thread // Importante para rodar em background

@RestController
@RequestMapping("/api/i/cldf")
@Tag(name = "Integração CLDF", description = "Endpoints para sincronização manual de dados com a API da CLDF")
class CldfIntegrationController(
    private val cldfService: CldfInterface,
    private val proposicaoService: ProposicaoService // Injetamos o service de proposições aqui
) {

    @PostMapping("/temas/sincronizar")
    @Operation(
        summary = "1. Sincronizar Temas",
        description = "Bate na API pública da CLDF, busca todos os temas oficiais e atualiza o banco de dados local."
    )
    fun sincronizarTemas(): ResponseEntity<SincronizacaoResponseDTO> {
        cldfService.sincronizarTemasCldf()

        val response = SincronizacaoResponseDTO(
            mensagem = "Processo de sincronização de temas finalizado com sucesso."
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/proposicoes/iniciar-varredura/{ano}")
    @Operation(
        summary = "2. INICIAR VARREDURA DE PROPOSIÇÕES",
        description = "⚠️ ATENÇÃO: Rode isso APENAS após cadastrar os 24 deputados manualmente e sincronizar os temas. O processo rodará em background para não dar Timeout no Swagger."
    )
    fun iniciarVarreduraProposicoes(
        @PathVariable ano: Int,
        @RequestParam(defaultValue = "0") paginaInicial: Int
    ): ResponseEntity<SincronizacaoResponseDTO> {

        // Dispara a varredura em uma Thread separada (Background)
        // Assim o Swagger recebe a resposta 200 OK na hora e não trava a sua tela.
        thread(start = true) {
            proposicaoService.sincronizarCargaTotal(ano, paginaInicial)
        }

        val response = SincronizacaoResponseDTO(
            mensagem = "🚀 Varredura do ano $ano iniciada em background a partir da página $paginaInicial. Acompanhe os logs no terminal da sua IDE para ver o progresso!"
        )

        return ResponseEntity.ok(response)
    }
}