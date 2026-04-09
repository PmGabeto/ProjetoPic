package com.example.df.backend.dtos
import com.example.df.backend.entities.*
import java.time.LocalDateTime

// ==========================================
// 1. DTO DE RESPOSTA (O QUE O CLIENTE VÊ)
// ==========================================
data class DocumentoResponseDTO(
    val id: Long,
    val publicId: String,
    val tipoRelacionado: String,
    val nomeExibicao: String,
    val tipoDocumento: String, // Ex: "Edital", "Projeto de Lei", "Contrato"
    val urlDownload: String?,   // Rota da API para baixar o arquivo interno ou link externo
    val extensao: String?,     // pdf, docx, etc.
    val dataCadastro: LocalDateTime,
    val autor: String?,
    val validoDesde :LocalDateTime,
    val siglaUnidadeCriacao :String?
)
fun DocumentosArquivos.toDTO() = DocumentoResponseDTO(
    id = this.id ?: 0L,
    publicId = this.publicId,
    tipoRelacionado = this.tipoRelacionado, // Vem direto da sua lógica genérica!
    nomeExibicao = this.nomeExibicao,
    tipoDocumento = this.tipoDocumento,

    // Se tiver linkDireto manda ele, senão manda o nomeStorage para a API resolver
    urlDownload = this.linkDireto ?: this.nomeStorage,

    // Pega o "application/pdf" e extrai só o "pdf"
    extensao = this.mimeType?.substringAfterLast("/") ?: "pdf",


    dataCadastro = this.dataCadastro,
    autor = this.autor,
    validoDesde = this.validoDesde,
    siglaUnidadeCriacao = this.siglaUnidadeCriacao
)

// ==========================================
// 2. DTO DE CRIAÇÃO/VINCULAÇÃO
// ==========================================
data class VincularDocumentosRequest(
    val tipoRelacionado: String, // "OBRA", "PROPOSICAO", "CIDADAO"
    val idRelacionado: Long,
    val documentos: List<NovoDocumentoInfoDTO>
)

data class NovoDocumentoInfoDTO(
    val nomeExibicao: String,
    val tipoDocumento: String,
    val descricao: String? = null,
    val autor: String? = null
)

// ==========================================
// 3. DTO DE DELEÇÃO E STATUS
// ==========================================
data class DocumentoDeletadoDTO(
    val id: Long,
    val mensagem: String = "Documento removido com sucesso tanto do banco quanto do storage físico.",
    val dataDelecao: LocalDateTime = LocalDateTime.now()
)