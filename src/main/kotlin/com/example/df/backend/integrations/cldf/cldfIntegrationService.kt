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
class CldfIntegrationService(
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

    override fun processarTemasProposicao(idsRaw: String?, nomesRaw: String?): List<Tema> {
        if (idsRaw.isNullOrBlank() || nomesRaw.isNullOrBlank()) return emptyList()

        val ids = idsRaw.split("#").filter { it.isNotBlank() }.map { it.toLong() }
        val nomes = nomesRaw.split("#").filter { it.isNotBlank() }

        return ids.zip(nomes).map { (id, nome) -> garantirTemaIndividual(id, nome) }
    }

    override fun garantirTemaIndividual(id: Long, nome: String): Tema {
        return temaRepo.findById(id).orElseGet {
            temaRepo.save(Tema(id = id, nome = nome.trim()))
        }
    }

    override fun varrerProposicoesRecentes(ano: Int, pagina: Int, tamanho: Int): List<ProposicaoCldfBaseDTO> {
        return try {
            // Criamos o objeto que vai virar o JSON no Body
            // Se no futuro você quiser filtrar por tipo ou nome, é só adicionar aqui:
            // mapOf("ano" to ano, "tipo" to "PL", "autor" to "Chico")
            val filtroJson = mapOf(
                "ano" to ano.toString()
            )

            webClient.post() // Garante que é POST
                .uri { it.path("/proposicao/filter")
                    // Mantemos a paginação na URL (padrão do Spring Pageable)
                    .queryParam("page", pagina)
                    .queryParam("size", tamanho)
                    .queryParam("sort", "dataApresentacao,DESC")
                    .build()
                }
                .header("Content-Type", "application/json") // 3. Forçando o formato                .bodyValue(filtroJson) // Injeta o JSON no Body da requisição
                .retrieve()
                .bodyToMono<CldfPageResponse<ProposicaoCldfBaseDTO>>()
                .map { it.content }
                .block() ?: emptyList()

        } catch (e: Exception) {
            logger.error("Falha na varredura (Pág $pagina): ${e.message}")
            throw e
        }
    }

    override fun buscarDetalhesCompletos(publicId: String): ProposicaoCldfCompletaDTO? {
        return try {
            webClient.get()
                .uri("/proposicao/$publicId/detalhe")
                .retrieve()
                .bodyToMono<ProposicaoCldfCompletaDTO>()
                .block()
        } catch (e: Exception) {
            logger.error("Erro ao buscar detalhes da proposição $publicId: ${e.message}")
            null
        }
    }

    override fun buscarHistorico(publicId: String): List<HistoricoCldfDTO> {
        return try {
            webClient.post()
                .uri("/historico-proposicao/$publicId?sort=base.dataHistorico,DESC")
                .retrieve()
                .bodyToMono<CldfPageResponse<HistoricoCldfDTO>>()
                .map { it.content }
                .block() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Erro ao buscar histórico $publicId: ${e.message}")
            emptyList()
        }
    }

    override fun buscarDocumentos(publicId: String): List<DocumentoCldfDTO> {
        return try {
            webClient.post()
                .uri("/proposicao/$publicId/documento/ativas/order-by-pageable")
                .retrieve()
                .bodyToMono<CldfPageResponse<DocumentoCldfDTO>>()
                .map { it.content }
                .block() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Erro ao buscar documentos $publicId: ${e.message}")
            emptyList()
        }
    }
}