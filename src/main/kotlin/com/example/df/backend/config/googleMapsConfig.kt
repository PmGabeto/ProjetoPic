package com.example.df.backend.config

import com.google.maps.GeoApiContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GoogleMapsConfig(
    private val appConfig: AppConfig
) {

    @Bean
    fun geoApiContext(): GeoApiContext {
        // Pega a chave do AppConfig
        val chaveConfigurada = appConfig.google.maps.apiKey

        // BLINDAGEM: Se a chave estiver vazia (null ou ""), usamos uma dummy.
        // Isso permite que o backend suba para testes, mesmo que o mapa falhe depois.
        val chaveFinal = if (chaveConfigurada.isNullOrBlank()) {
            "CHAVE_DUMMY_PARA_EVITAR_CRASH_NO_STARTUP"
        } else {
            chaveConfigurada
        }

        return GeoApiContext.Builder()
            .apiKey(chaveFinal)
            .build()
    }
}