package com.example.df.backend.services

import com.example.df.backend.config.AppConfig
import com.example.df.backend.entities.OcorrenciaFoto
import com.example.df.backend.repositories.OcorrenciaFotoRepository
import com.example.df.backend.repositories.OcorrenciaMapaRepository
import net.coobird.thumbnailator.Thumbnails
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class FotoService(
    private val fotoRepository: OcorrenciaFotoRepository,
    private val ocorrenciaRepository: OcorrenciaMapaRepository,
    private val appConfig: AppConfig
) {

    // =========================================================================
    // 1. UPLOAD UNIFICADO (Obras, Perfis, Deputados, Ocorrências)
    // =========================================================================

    @Transactional
    fun salvarMidiaGeral(tipo: String, targetId: Long, arquivo: MultipartFile): OcorrenciaFoto {
        // 1. Gera ID Público único para a URL (Segurança)
        val publicId = gerarPublicId()

        // 2. Define extensão e nome físico do arquivo
        val extensao = arquivo.originalFilename?.substringAfterLast(".", "jpg") ?: "jpg"
        val nomeFisico =
            "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))}.$publicId.$extensao"

        // 3. Define a subpasta baseada no tipo (Requisito do Sistema)
        val subpasta = when (tipo.uppercase()) {
            "OBRA" -> "obras"
            "PERFIL" -> "perfis"
            "DEPUTADO" -> "deputados"
            "OCORRENCIA" -> "ocorrencias"
            else -> throw IllegalArgumentException("Tipo de mídia '$tipo' não é suportado.")
        }

        // 4. Salva e Comprime fisicamente na VPS
        salvarEComprimirImagem(arquivo, subpasta, nomeFisico)

        // 5. Prepara a Entidade para persistência
        val novaFoto = OcorrenciaFoto(
            publicId = publicId,
            caminhoArquivo = nomeFisico,
            tipoMidia = tipo.uppercase(),
            idRelacionado = targetId,
            nomeOriginal = arquivo.originalFilename,
            tamanho = arquivo.size,
            contentType = arquivo.contentType,
            dataUpload = LocalDateTime.now()
        )

        // 6. Lógica de relacionamento específica para Ocorrências (ID_TOKEN)
        if (tipo.uppercase() == "OCORRENCIA") {
            val ocorrencia = ocorrenciaRepository.findById(targetId)
                .orElseThrow { RuntimeException("Ocorrência com ID $targetId não encontrada.") }
            novaFoto.token = ocorrencia
        }

        return fotoRepository.save(novaFoto)
    }

    // =========================================================================
    // 2. BUSCA E LEITURA (Visualização)
    // =========================================================================

    /**
     * Busca a foto pelo publicId e determina a pasta correta para leitura
     */
    fun buscarRecursoFisico(publicId: String): Pair<Resource, String> {
        val foto = fotoRepository.findByPublicId(publicId)
            ?: throw RuntimeException("Arquivo não encontrado no banco de dados.")

        val subpasta = when (foto.tipoMidia?.uppercase()) {
            "OBRA" -> "obras"
            "PERFIL" -> "perfis"
            "DEPUTADO" -> "deputados"
            else -> "ocorrencias"
        }

        val caminhoAbsoluto = Paths.get(appConfig.storage.localPath).resolve(subpasta).resolve(foto.caminhoArquivo)
        val resource = UrlResource(caminhoAbsoluto.toUri())

        if (!resource.exists() || !resource.isReadable) {
            throw RuntimeException("Arquivo físico não encontrado no servidor: $subpasta/${foto.caminhoArquivo}")
        }

        return Pair(resource, foto.contentType ?: "image/jpeg")
    }

    // =========================================================================
    // 3. AUXILIARES TÉCNICOS
    // =========================================================================

    private fun salvarEComprimirImagem(arquivo: MultipartFile, subpasta: String, nomeFinal: String) {
        val diretorioDestino = Paths.get(appConfig.storage.localPath).resolve(subpasta)

        if (!Files.exists(diretorioDestino)) {
            Files.createDirectories(diretorioDestino)
        }

        val arquivoDestino = diretorioDestino.resolve(nomeFinal).toFile()

        // Compressão estilo WhatsApp para economizar espaço na VPS
        Thumbnails.of(arquivo.inputStream)
            .size(1280, 1280)
            .keepAspectRatio(true)
            .outputQuality(0.75)
            .toFile(arquivoDestino)
    }

    private fun gerarPublicId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..12).map { chars.random() }.joinToString("")
    }

    /**
     * Deleta o arquivo físico e o registro no banco de dados
     */
    @Transactional // IMPORTANTE: Necessário para persistir a exclusão no banco
    fun deletarArquivoFisico(publicId: String) {
        // 1. Busca a foto no banco para saber o caminho e o tipo
        val foto = fotoRepository.findByPublicId(publicId) ?: return

        // 2. Lógica para encontrar a subpasta correta
        val subpasta = when (foto.tipoMidia?.uppercase()) {
            "OBRA" -> "obras"
            "PERFIL" -> "perfis"
            "DEPUTADO" -> "deputados"
            else -> "ocorrencias"
        }

        // 3. Tenta deletar o arquivo físico no disco
        try {
            val caminho = Paths.get(appConfig.storage.localPath)
                .resolve(subpasta)
                .resolve(foto.caminhoArquivo.substringAfterLast("/"))

            Files.deleteIfExists(caminho)
        } catch (e: Exception) {
            // Logar erro de arquivo, mas prosseguir para limpar o banco
            println("Erro ao deletar arquivo físico: ${e.message}")
        }

        // 4. O PULO DO GATO: Deletar o registro do banco de dados!
        fotoRepository.delete(foto)
    }
}