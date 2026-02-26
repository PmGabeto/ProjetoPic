package com.example.df.backend.services


import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.GeocodingResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class GoogleMapsService(
    private val geoApiContext: GeoApiContext // O Bean que você configurou é injetado aqui
) {

    private val logger = LoggerFactory.getLogger(GoogleMapsService::class.java)

    /**
     * Recebe um endereço em texto e retorna as coordenadas (Lat, Long).
     * Retorna null se não encontrar nada ou der erro.
     */
    fun geocodificarEndereco(endereco: String): Pair<BigDecimal, BigDecimal>? {
        if (endereco.isBlank()) return null

        try {
            // Chama a API do Google (Sincrono/Bloqueante)
            val resultados: Array<GeocodingResult> = GeocodingApi.geocode(geoApiContext, endereco).await()

            if (resultados.isNotEmpty()) {
                val location = resultados[0].geometry.location

                logger.info("Endereço '$endereco' encontrado: ${location.lat}, ${location.lng}")

                // Convertemos para BigDecimal para bater com sua Entity OcorrenciaMapa
                return Pair(
                    BigDecimal.valueOf(location.lat),
                    BigDecimal.valueOf(location.lng)
                )
            }
        } catch (e: Exception) {
            logger.error("Erro ao geocodificar endereço: $endereco", e)
            // Em produção, você pode querer lançar uma exceção customizada aqui
        }

        return null
    }
}