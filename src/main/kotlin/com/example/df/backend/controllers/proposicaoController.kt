package com.example.df.backend.controllers

import com.example.df.backend.dtos.*
import com.example.df.backend.services.ProposicaoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@Tag(name = "7. Proposições Legislativas", description = "Acompanhamento da tramitação de leis na CLDF")
@RestController
@RequestMapping("/api/proposicoes")
class ProposicaoController(
    private val service: ProposicaoService
) {


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
    /**
     * 5. BAIXAR DOCUMENTO (PDF) DA CLDF
     * GET /api/proposicoes/{idProposicao}/documentos/{idDocumento}/pdf
     */
    @Operation(summary = "Baixar PDF da Proposição", description = "Faz a ponte com a CLDF para baixar o PDF original da lei/documento.")
    @GetMapping("/{idProposicao}/documentos/{idDocumento}/pdf")
    fun baixarPdf(
        @PathVariable idProposicao: String,
        @PathVariable idDocumento: String
    ): ResponseEntity<ByteArray> {
        val pdfBytes = service.baixarPdfDocumento(idProposicao, idDocumento)

        return if (pdfBytes != null && pdfBytes.isNotEmpty()) {
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                // attachment força o download no navegador/telemóvel
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documento_${idDocumento}.pdf\"")
                .body(pdfBytes)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 6. VISUALIZAR DOCUMENTO (HTML) DA CLDF
     * GET /api/proposicoes/{idProposicao}/documentos/{idDocumento}/html
     */
    @Operation(summary = "Visualizar texto HTML da Proposição", description = "Busca o corpo do texto da lei em HTML para exibição nativa no App.")
    @GetMapping("/{idProposicao}/documentos/{idDocumento}/html")
    fun visualizarHtml(
        @PathVariable idProposicao: String,
        @PathVariable idDocumento: String
    ): ResponseEntity<String> {
        val htmlString = service.buscarHtmlDocumento(idProposicao, idDocumento)

        return if (!htmlString.isNullOrBlank()) {
            ResponseEntity.ok()
                // Retorna como HTML puro para o seu App/Frontend renderizar na tela
                .contentType(MediaType.TEXT_HTML)
                .body(htmlString)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}