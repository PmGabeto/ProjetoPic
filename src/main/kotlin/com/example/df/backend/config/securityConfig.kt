package com.example.df.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val securityFilter: SecurityFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) } // <--- AQUI LIGAMOS O CORS
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                // 1. O QUE É PÚBLICO (Ninguém precisa conectar)
                auth.requestMatchers(
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/api-docs",
                    "/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error"
                ).permitAll() // Documentação
                auth.requestMatchers(HttpMethod.POST, "/auth/**").permitAll() // Login e Registro

                // 2. O QUE É EXCLUSIVO DE ADMIN (Dados Oficiais)
                // "Só Admin pode CRIAR, EDITAR ou DELETAR Políticos, Obras e Proposições"
                // O asterisco duplo (**) pega tudo que vem depois da barra.
                auth.requestMatchers(HttpMethod.POST, "/politicos/**", "/obras/**", "/proposicoes/**").hasRole("ADMIN")
                auth.requestMatchers(HttpMethod.PUT, "/politicos/**", "/obras/**", "/proposicoes/**").hasRole("ADMIN")
                auth.requestMatchers(HttpMethod.DELETE, "/politicos/**", "/obras/**", "/proposicoes/**").hasRole("ADMIN")



                // 3. O RESTO (Regra Geral)
                // Aqui entra:
                // - GET de tudo (Políticos, Obras, etc.)
                // - POST/PUT/DELETE de Ocorrências e Comentários (Coisas de User)
                // - Mexer no próprio perfil
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    // Configuração detalhada do CORS
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        // Libera as portas comuns de Front-end (React, Angular, Flutter Web, etc)
        configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:4200", "http://localhost:8081","https://vigiaDF.pmhub.cloud","http://72.60.62.205:8080")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration) // <--- O PULO DO GATO: "/**" libera tudo
        return source
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}