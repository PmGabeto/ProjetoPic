package com.example.df.backend.integrations.cldf

import com.example.df.backend.dtos.TemaDTO
import com.example.df.backend.entities.Tema
import com.example.df.backend.repositories.TemaRepository
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.slf4j.LoggerFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit
@Service
open class CldfIntegrationService(
    private val temaRepo: TemaRepository,

    webClientBuilder: WebClient.Builder
) : CldfInterface {

    private val logger = LoggerFactory.getLogger(CldfIntegrationService::class.java)

    private val httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
        .responseTimeout(Duration.ofSeconds(30))
        .doOnConnected { conn ->
            conn.addHandlerLast(ReadTimeoutHandler(30, TimeUnit.SECONDS))
            conn.addHandlerLast(WriteTimeoutHandler(30, TimeUnit.SECONDS))
        }

    private val webClient = webClientBuilder
        .baseUrl("https://ple.cl.df.gov.br/pleservico/api/public")
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .build()

    @Transactional
    override fun sincronizarTemasCldf() {
        try {
            val temasExternos = webClient.get()
                .uri("/tema")
                .retrieve()
                .bodyToFlux<TemaDTO>()
                .collectList()
                .block() ?: emptyList()

            temasExternos.forEach { dto ->
                if (!temaRepo.existsById(dto.id)) {
                    temaRepo.save(Tema(id = dto.id, nome = dto.nome))
                }
            }
        } catch (e: Exception) {
            logger.error("Erro ao sincronizar temas: ${e.message}")
        }
    }
@Transactional
    override fun processarTemasProposicao(temasIds: List<Int>): List<Tema> {
        // 1. Proteção inicial: Se não vier nenhum tema (lista vazia), retorna lista vazia imediatamente.
        if (temasIds.isEmpty()) {
            return emptyList()
        }

        // 2. O Spring Data JPA (temaRepo) geralmente usa 'Long' como chave primária (ID).
        // Então convertemos a lista de Int que veio da API para uma lista de Long.
        val idsLong = temasIds.map { it.toLong() }

        // 3. Busca de Alta Performance: Em vez de fazer um "for" e bater no banco várias vezes,
        // o findAllById faz um único SELECT buscando todos os temas de uma vez só!
        return temaRepo.findAllById(idsLong).toList()
    }

    override fun garantirTemaIndividual(id: Long, nome: String): Tema {
        return temaRepo.findById(id).orElseGet {
            temaRepo.save(Tema(id = id, nome = nome.trim()))
        }
    }

    override fun varrerProposicoesRecentes(filtros: Map<String, Any>, pagina: Int, tamanho: Int): List<ProposicaoCldfBaseDTO> {
        return try {
            webClient.post()
                .uri("/proposicao/filter?size=$tamanho&page=$pagina")
                .header("Content-Type", "application/json")
                .bodyValue(filtros) // <--- O mapa de filtros entra direto aqui!
                .retrieve()
                .bodyToMono<CldfPageResponse<ProposicaoCldfBaseDTO>>()
                .map { it.content }
                .block() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Erro na varredura com filtros $filtros, pág $pagina: ${e.message}")
            emptyList()
        }
    }


    override fun buscarDetalhesCompletos(publicId: String): CldfDetalheResponse? { // Alterado o retorno para a Capa
        return try {
            webClient.get()
                .uri("/proposicao/$publicId/detalhe")
                .retrieve()
                .bodyToMono<CldfDetalheResponse>() // Agora retorna o objeto pai (proposicao + historico)
                .block() // Removido o .map que descartava o histórico
        } catch (e: Exception) {
            logger.error("Erro ao buscar detalhes da proposicao $publicId: ${e.message}")
            null
        }
    }


    override fun buscarDocumentos(publicId: String): List<DocumentoCldfDTO> {
        return try {
            webClient.post()
                .uri("/proposicao/$publicId/documento/ativas/order-by-pageable")
                .header("Content-Type", "application/json") // Vacina contra erro 400
                .header("Accept", "*/*")
                .bodyValue(emptyMap<String, Any>()) // Manda um JSON vazio: {}
                .retrieve()
                // 1. Lê a "capa" da resposta paginada
                .bodyToMono<CldfPageResponse<DocumentoCldfDTO>>()
                // 2. Extrai a lista do array "content"
                .map { it.content }
                .block() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Erro ao buscar documentos $publicId: ${e.message}")
            emptyList()
        }
    }
    override fun buscarHtmlDocumento(idProposicao: String, idDocumento: String): String? {
        return try {
            webClient.get() // Atenção aqui: o PDF era POST, o HTML é GET!
                .uri("/proposicao/$idProposicao/documento/$idDocumento/html")
                .header("Accept", "text/html,application/xhtml+xml") // Pedimos especificamente por HTML
                .retrieve()
                .bodyToMono<String>() // Puxa o corpo da resposta como uma String gigante
                .block()
        } catch (e: Exception) {
            logger.error("Erro ao buscar HTML do documento $idDocumento da proposição $idProposicao: ${e.message}")
            null
        }
    }

    override fun baixarDocumentos(idProposicao: String, idDocumento: String): ByteArray? {
        return try {
            webClient.post()
                .uri("/proposicao/exportar/$idProposicao/pdf")
                .header("Content-Type", "application/json")
                // Converte a String de volta para Long apenas para o JSON da CLDF entender
                .bodyValue(listOf(idDocumento.toLong()))
                .retrieve()
                .bodyToMono<ByteArray>()
                .block()
        } catch (e: Exception) {
            logger.error("Erro ao descarregar PDF do documento $idDocumento da proposição $idProposicao: ${e.message}")
            null
        }
    }
}