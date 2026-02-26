package com.example.df.backend.services

import com.example.df.backend.config.AppConfig
import com.example.df.backend.entities.OcorrenciaFoto
import com.example.df.backend.repositories.OcorrenciaFotoRepository
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
    private val tokenService: OcorrenciaService, // Service de Ocorrencias
    private val appConfig: AppConfig
) {

    // 1. GERA UM NOME CURTO E LEGÍVEL (Ex: 20260225_153310_A7X.jpg)
    private fun gerarNomeCurto(extensao: String): String {
        val dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val sufixoAleatorio = (1..3).map { caracteres.random() }.joinToString("")

        return "${dataHora}_${sufixoAleatorio}.${extensao}"
    }

    // 2. SALVA E COMPRIME APENAS IMAGENS
    private fun salvarEComprimirImagem(arquivo: MultipartFile, pastaDestino: String): String {
        // Validação de Segurança: Bloqueia qualquer coisa que não seja imagem
        val contentType = arquivo.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            throw IllegalArgumentException("Este serviço aceita apenas arquivos de imagem (JPEG, PNG, etc).")
        }

        // Pega a extensão da imagem de forma segura (ou usa jpg como padrão)
        val extensao = arquivo.originalFilename?.substringAfterLast(".", "jpg")?.lowercase() ?: "jpg"

        // Gera o nome limpo e curto
        val nomeArquivo = gerarNomeCurto(extensao)

        // Configura as pastas
        val pastaRaiz = Paths.get(appConfig.storage.localPath)
        val pastaCompleta = pastaRaiz.resolve(pastaDestino)

        if (!Files.exists(pastaCompleta)) {
            Files.createDirectories(pastaCompleta)
        }

        val arquivoDestino = pastaCompleta.resolve(nomeArquivo)

        // Aplica a compressão estilo "WhatsApp"
        Thumbnails.of(arquivo.inputStream)
            .size(1280, 1280) // Limite máximo de resolução
            .outputQuality(0.75) // Reduz o tamanho do arquivo sem perder qualidade visível
            .toFile(arquivoDestino.toFile())

        // Retorna o caminho relativo (Ex: "ocorrencias/20260225_153310_A7X.jpg")
        return Paths.get(pastaDestino, nomeArquivo).toString()
    }

    // =========================================================================
    // MÉTODOS PÚBLICOS DO SERVICE
    // =========================================================================

    @Transactional
    fun salvarFotoOcorrencia(idToken: Long, arquivo: MultipartFile): OcorrenciaFoto {
        val token = tokenService.buscarPorId(idToken)

        // O arquivo é processado, comprimido e salvo com nome limpo
        val caminhoRelativo = salvarEComprimirImagem(arquivo, "ocorrencias")

        // Calcula o tamanho final em bytes após a compressão para salvar no banco
        val tamanhoComprimido = Files.size(Paths.get(appConfig.storage.localPath).resolve(caminhoRelativo))

        val novaFoto = OcorrenciaFoto(
            caminhoArquivo = caminhoRelativo,
            nomeOriginal = arquivo.originalFilename, // Mantemos o original só como registro no banco, não no arquivo físico
            tamanho = tamanhoComprimido,
            contentType = arquivo.contentType,
            token = token
        )

        return fotoRepository.save(novaFoto)
    }

    fun salvarFotoPerfil(arquivo: MultipartFile): String {
        // Reaproveita a mesma lógica segura e comprimida para fotos de perfil
        return salvarEComprimirImagem(arquivo, "perfis")
    }

    fun carregarFoto(caminhoRelativo: String): Resource {
        val caminhoAbsoluto = Paths.get(appConfig.storage.localPath).resolve(caminhoRelativo)
        val resource = UrlResource(caminhoAbsoluto.toUri())

        if (resource.exists() || resource.isReadable) {
            return resource
        } else {
            throw RuntimeException("Não foi possível ler a foto: $caminhoRelativo")
        }
    }
    // Adicione este método dentro do FotoService
    fun deletarFoto(caminhoRelativo: String?) {
        if (caminhoRelativo.isNullOrBlank()) return

        try {
            val caminhoAbsoluto = Paths.get(appConfig.storage.localPath).resolve(caminhoRelativo)
            Files.deleteIfExists(caminhoAbsoluto)
        } catch (e: Exception) {
            println("Aviso: Não foi possível deletar a foto antiga: ${e.message}")
        }
    }
}