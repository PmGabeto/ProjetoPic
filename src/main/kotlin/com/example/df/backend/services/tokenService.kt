package com.example.df.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.example.df.backend.config.AppConfig
import com.example.df.backend.entities.Usuario
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class TokenService(
    private val appConfig: AppConfig
) {

    // 1. Gera o Token (Chamado pelo AuthController no Login)
    fun gerarToken(usuario: Usuario): String {
        return try {
            val algoritmo = Algorithm.HMAC256(appConfig.jwt.secret)

            JWT.create()
                .withIssuer("MapModuleAPI")
                .withSubject(usuario.id.toString()) // ID vira o Subject
                .withClaim("email", usuario.email)
                .withClaim("perfil", usuario.perfil.name)
                .withExpiresAt(dataExpiracao())
                .sign(algoritmo)

        } catch (exception: JWTCreationException) {
            throw RuntimeException("Erro ao gerar token JWT", exception)
        }
    }

    // 2. Valida o Token (Chamado pelo SecurityFilter) <--- O MÉTODO QUE ESTAVA FALTANDO
    fun validarToken(tokenJWT: String): Long {
        return try {
            val algoritmo = Algorithm.HMAC256(appConfig.jwt.secret)

            // Decodifica e verifica a assinatura
            val idString = JWT.require(algoritmo)
                .withIssuer("MapModuleAPI")
                .build()
                .verify(tokenJWT)
                .subject // Pega o ID que guardamos no subject

            // Retorna como Long para o SecurityFilter buscar no banco
            idString.toLong()

        } catch (exception: JWTVerificationException) {
            // Se o token for falso ou expirado, lança erro
            throw RuntimeException("Token JWT inválido ou expirado!")
        }
    }

    private fun dataExpiracao(): Instant {
        val horas = appConfig.jwt.expirationMs / 3600000
        return LocalDateTime.now().plusHours(horas).toInstant(ZoneOffset.of("-03:00"))
    }
}