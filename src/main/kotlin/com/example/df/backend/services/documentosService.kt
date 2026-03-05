package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.DocumentosArquivos
import com.example.df.backend.repositories.DocumentoRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class DocumentoService(
    private val repository: DocumentoRepository,
    @Value("\${app.storage.root:publico}") private val storageRoot: String
) {

    private val documentoPath = "$storageRoot/documentos"

    init {
        // Garante que a pasta física exista na inicialização
        val directory = File(documentoPath)
        if (!directory.exists()) directory.mkdirs()
    }

    // =========================================================================
    // 1. UPLOAD DE ARQUIVOS FÍSICOS (Ex: Obras, Cidadão, ou PDFs baixados)
    // =========================================================================
    @Transactional
    fun fazerUpload(
        file: MultipartFile,
        tipoRelacionado: String,
        idRelacionado: Long,
        nomeExibicao: String,
        tipoDocumento: String,
        autor: String?,
        raOuContexto: String,
        publicIdSugerido: String? = null // Se vier da CLDF e você baixar o PDF, passa o ID aqui
    ): DocumentosArquivos {

        // A MÁGICA DO ID: Usa o ID sugerido (ex: "185") OU gera um UUID aleatório de 12 chars
        val publicIdFinal = publicIdSugerido ?: UUID.randomUUID().toString().replace("-", "").substring(0, 12)

        // Higienização para o nome do arquivo no Storage
        val dataFormatada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val contextoLimpo = raOuContexto.replace("\\s+".toRegex(), "_").uppercase()
        val nomeExibicaoLimpo = nomeExibicao.replace("\\s+".toRegex(), "_")
        val extensao = file.originalFilename?.substringAfterLast(".", "pdf") ?: "pdf"

        // ESTRUTURA: {TIPO}_{DATA}_{CONTEXTO}_{NOME_EXIBICAO}_{publicId}.ext
        val nomeStorage = "${tipoRelacionado.uppercase()}_${dataFormatada}_${contextoLimpo}_${nomeExibicaoLimpo}_${publicIdFinal}.$extensao"

        // Salva fisicamente na pasta da VPS
        val targetPath = Paths.get(documentoPath).resolve(nomeStorage)
        Files.copy(file.inputStream, targetPath)

        val novoDoc = DocumentosArquivos(
            publicId = publicIdFinal,
            nomeExibicao = nomeExibicao,
            nomeStorage = nomeStorage,
            tipoDocumento = tipoDocumento,
            mimeType = file.contentType ?: "application/pdf",
            tipoRelacionado = tipoRelacionado.uppercase(),
            idRelacionado = idRelacionado,
            autor = autor,
            dataCadastro = LocalDateTime.now()
        )

        return repository.save(novoDoc)
    }

    // =========================================================================
    // 2. REGISTRO DE LINKS EXTERNOS (Ex: Arquivos hospedados na CLDF)
    // =========================================================================
    @Transactional
    fun registrarDocumentoExterno(
        publicIdObrigatorio: String, // Para a CLDF, o ID é obrigatório (ex: "185")
        linkDireto: String,
        tipoRelacionado: String,
        idRelacionado: Long,
        nomeExibicao: String,
        tipoDocumento: String,
        autor: String?
    ): DocumentosArquivos {

        val novoDoc = DocumentosArquivos(
            publicId = publicIdObrigatorio,
            nomeExibicao = nomeExibicao,
            // Como não há arquivo no nosso disco, o nomeStorage vira apenas um identificador virtual
            nomeStorage = "EXTERNO_${tipoRelacionado.uppercase()}_${publicIdObrigatorio}",
            linkDireto = linkDireto,
            tipoDocumento = tipoDocumento,
            mimeType = "application/pdf", // Assumimos PDF para links externos, ou pode passar por parâmetro
            tipoRelacionado = tipoRelacionado.uppercase(),
            idRelacionado = idRelacionado,
            autor = autor,
            dataCadastro = LocalDateTime.now()
        )

        return repository.save(novoDoc)
    }

    // =========================================================================
    // 3. CONSULTAS E DOWNLOAD
    // =========================================================================

    fun buscarPorPublicId(publicId: String): DocumentosArquivos {
        // Agora usamos o campo real publicId da entidade
        // Requer que você tenha criado a função findByPublicId no Repository
        return repository.findByPublicId(publicId)
            ?: throw NoSuchElementException("Documento com ID $publicId não encontrado.")
    }

    fun listarDocumentos(tipo: String, id: Long): List<DocumentoResponseDTO> {
        return repository.findByTipoRelacionadoAndIdRelacionado(tipo.uppercase(), id).map { doc ->

            // Se tem link direto (CLDF), a URL de download é o próprio site deles.
            // Se for arquivo nosso, a URL aponta para o nosso Controller (ex: /api/documentos/v/a1b2c3d4e5f6)
            val url = doc.linkDireto ?: "/api/documentos/v/${doc.publicId}"

            DocumentoResponseDTO(
                id = doc.id!!,
                nomeExibicao = doc.nomeExibicao,
                tipoDocumento = doc.tipoDocumento,
                urlDownload = url,
                extensao = doc.nomeStorage.substringAfterLast(".", "pdf"),
                dataCadastro = doc.dataCadastro,
                autor = doc.autor
            )
        }
    }

    // =========================================================================
    // 4. DELEÇÃO
    // =========================================================================
    @Transactional
    fun deletarDocumento(id: Long): DocumentoDeletadoDTO {
        val doc = repository.findById(id).orElseThrow { NoSuchElementException("ID não encontrado.") }

        // Remove do banco
        repository.delete(doc)

        // Se NÃO for um link externo, apaga o arquivo físico da VPS
        if (doc.linkDireto == null) {
            val file = File("$documentoPath/${doc.nomeStorage}")
            if (file.exists()) file.delete()
        }

        return DocumentoDeletadoDTO(id = id)
    }
}