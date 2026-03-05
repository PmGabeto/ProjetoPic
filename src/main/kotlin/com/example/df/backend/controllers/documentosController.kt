package com.example.df.backend.controllers

import com.example.df.backend.dtos.DocumentoDeletadoDTO
import com.example.df.backend.dtos.DocumentoResponseDTO
import com.example.df.backend.services.DocumentoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

@RestController
@RequestMapping("/api/documentos")
@Tag(name = "Documentos", description = "Gerenciamento universal de documentos (Obras, Proposições, etc.)")
class DocumentoController(
    private val service: DocumentoService,
    @param:Value("\${app.storage.root:publico}") private val storageRoot: String
) {

    // =========================================================================
    // 1. UPLOAD DE ARQUIVO FÍSICO
    // =========================================================================
    @Operation(
        summary = "Fazer upload de um arquivo físico",
        description = "Salva um PDF ou outro arquivo no disco do servidor e gera um registro no banco. Retorna os dados do documento criado."
    )
    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun fazerUpload(
        @Parameter(description = "Arquivo a ser enviado")
        @RequestParam("file") file: MultipartFile,
        @Parameter(description = "Módulo. Ex: OBRA, CIDADAO")
        @RequestParam("tipoRelacionado") tipoRelacionado: String,
        @Parameter(description = "ID da Obra ou Entidade")
        @RequestParam("idRelacionado") idRelacionado: Long,
        @Parameter(description = "Nome legível. Ex: Edital de Licitação")
        @RequestParam("nomeExibicao") nomeExibicao: String,
        @Parameter(description = "Tipo do Documento. Ex: EDITAL, PLANILHA")
        @RequestParam("tipoDocumento") tipoDocumento: String,
        @Parameter(description = "Nome do autor ou órgão")
        @RequestParam(value = "autor", required = false) autor: String?,
        @Parameter(description = "RA ou Contexto para o nome do arquivo")
        @RequestParam("raOuContexto") raOuContexto: String,
        @Parameter(description = "Deixe nulo para gerar UUID, ou passe ID para sobrescrever") @RequestParam(value = "publicIdSugerido", required = false) publicIdSugerido: String?
    ): ResponseEntity<DocumentoResponseDTO> {

        val docSalvo = service.fazerUpload(
            file,
            tipoRelacionado,
            idRelacionado,
            nomeExibicao,
            tipoDocumento,
            autor,
            raOuContexto,
            publicIdSugerido
        )

        val responseDTO = DocumentoResponseDTO(
            id = docSalvo.id!!,
            nomeExibicao = docSalvo.nomeExibicao,
            tipoDocumento = docSalvo.tipoDocumento,
            urlDownload = "/api/documentos/v/${docSalvo.publicId}",
            extensao = docSalvo.nomeStorage.substringAfterLast(".", "pdf"),
            dataCadastro = docSalvo.dataCadastro,
            autor = docSalvo.autor
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO)
    }

    // =========================================================================
    // 2. REGISTRO DE LINK EXTERNO (CLDF)
    // =========================================================================
    @Operation(
        summary = "Registrar um documento externo (Apenas Link)",
        description = "Não faz upload de arquivo físico. Apenas salva o link direto de outra fonte (ex: CLDF) usando o ID externo como publicId."
    )
    @PostMapping("/externo")
    fun registrarLinkExterno(
        @Parameter(description = "ID do site de origem (Ex: 185)") @RequestParam("publicIdObrigatorio") publicIdObrigatorio: String,
        @Parameter(description = "URL original do arquivo") @RequestParam("linkDireto") linkDireto: String,
        @RequestParam("tipoRelacionado") tipoRelacionado: String,
        @RequestParam("idRelacionado") idRelacionado: Long,
        @RequestParam("nomeExibicao") nomeExibicao: String,
        @RequestParam("tipoDocumento") tipoDocumento: String,
        @RequestParam(value = "autor", required = false) autor: String?
    ): ResponseEntity<DocumentoResponseDTO> {

        val docSalvo = service.registrarDocumentoExterno(
            publicIdObrigatorio, linkDireto, tipoRelacionado, idRelacionado, nomeExibicao, tipoDocumento, autor
        )

        val responseDTO = DocumentoResponseDTO(
            id = docSalvo.id!!,
            nomeExibicao = docSalvo.nomeExibicao,
            tipoDocumento = docSalvo.tipoDocumento,
            urlDownload = linkDireto, // Vai direto para a fonte original
            extensao = "pdf",
            dataCadastro = docSalvo.dataCadastro,
            autor = docSalvo.autor
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO)
    }

    // =========================================================================
    // 3. LISTAR DOCUMENTOS
    // =========================================================================
    @Operation(
        summary = "Listar documentos de uma entidade",
        description = "Busca todos os arquivos vinculados a uma Obra, Proposição, etc."
    )
    @GetMapping("/{tipoRelacionado}/{idRelacionado}")
    fun listar(
        @PathVariable tipoRelacionado: String,
        @PathVariable idRelacionado: Long
    ): ResponseEntity<List<DocumentoResponseDTO>> {
        return ResponseEntity.ok(service.listarDocumentos(tipoRelacionado, idRelacionado))
    }

    // =========================================================================
    // 4. VISUALIZAR / BAIXAR COM MÁSCARA
    // =========================================================================
    @Operation(
        summary = "Visualizar ou baixar um documento (Rota Pública)",
        description = "Busca o arquivo físico pelo publicId. Aplica uma máscara para que o usuário baixe o arquivo com o 'nome de exibição' limpo, escondendo o nome real do servidor."
    )
    @GetMapping("/v/{publicId}")
    fun baixarDocumento(@PathVariable publicId: String): ResponseEntity<Resource> {
        val doc = service.buscarPorPublicId(publicId)

        // Se for um link externo que bateu aqui por engano, redirecionamos para o link correto
        if (doc.linkDireto != null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(doc.linkDireto))
                .build()
        }

        // Busca o arquivo físico na VPS
        val filePath: Path = Paths.get("$storageRoot/documentos").resolve(doc.nomeStorage).normalize()
        val resource: Resource = UrlResource(filePath.toUri())

        if (!resource.exists() || !resource.isReadable) {
            return ResponseEntity.notFound().build()
        }

        // MÁSCARA: O arquivo no servidor se chama "OBRA_2024_...pdf", mas o usuário baixa como "Edital.pdf"
        val extensao = doc.nomeStorage.substringAfterLast(".", "pdf")
        val nomeMascarado = "${doc.nomeExibicao.replace(" ", "_")}.$extensao"

        // Usamos "inline" para abrir no navegador, ou "attachment" para forçar o download.
        // Inline é melhor para PDFs no celular.
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(doc.mimeType ?: "application/octet-stream"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$nomeMascarado\"")
            .body(resource)
    }

    // =========================================================================
    // 5. DELETAR
    // =========================================================================
    @Operation(summary = "Deletar documento", description = "Remove o registro do banco e apaga o arquivo físico da VPS se não for um link externo.")
    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long): ResponseEntity<DocumentoDeletadoDTO> {
        val resposta = service.deletarDocumento(id)
        return ResponseEntity.ok(resposta)
    }
}