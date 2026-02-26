package com.example.df.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SwaggerConfig : WebMvcConfigurer { // Adicionamos a interface aqui

    // --- PARTE 1: Configuração visual e Documentação ---
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("VigiaDF - Sistema de Gestão e Fiscalização Urbana")
                    .version("0.1")
                    .description("""
                        API para gerenciamento de tokens no mapa (problemas urbanos e obras públicas).
                        
                        **Funcionalidades:**
                        - Criar tokens de problemas urbanos
                        - Consultar tokens no mapa
                        - Detectar duplicatas automaticamente
                        - Upload de fotos
                    """.trimIndent())

                    .contact(
                        Contact()
                            .name("Equipe de Desenvolvimento")
                            .email("dev@seuapp.com")
                    )
            )
            .servers(listOf(
                Server().url("http://localhost:8080").description("servidor Local"),
                Server().url("http://72.60.62.205:8080").description("servidor de produção"),
                Server().url("https://vigiaDF.pmhub.cloud").description("Servidor Oficial (HTTPS)")
            ))
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
            .components(
                Components().addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
    }

    // --- PARTE 2: Ajuste para o Motor Moderno (PathPatternParser) ---
    // Este método ensina o Spring a encontrar os arquivos do Swagger UI
    // sem causar conflitos de "Invalid mapping pattern"
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")

        registry.addResourceHandler("/api-docs/**")
            .addResourceLocations("classpath:/META-INF/resources/")
    }
}