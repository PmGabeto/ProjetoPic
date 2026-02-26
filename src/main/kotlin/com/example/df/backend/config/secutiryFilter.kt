package com.example.df.backend.config

import com.example.df.backend.repositories.UsuarioRepository
import com.example.df.backend.services.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SecurityFilter(
    private val tokenService: TokenService,
    private val usuarioRepository: UsuarioRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path= request.requestURI
        if (path.contains("/swagger") || path.contains("/api-docs") || path.contains("/auth")) {
            filterChain.doFilter(request, response)
            return
        }


        val token = recuperarToken(request)

        if (token != null) {
            try {
                // 1. Valida o token e extrai o ID
                val idUsuario = tokenService.validarToken(token)

                // 2. Busca o usuário pela PK (Primary Key)
                val usuario = usuarioRepository.findById(idUsuario).orElse(null)

                if (usuario != null) {
                    val authentication = UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.authorities
                    )
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (e: Exception) {
                // Token inválido ou usuário não existe mais
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun recuperarToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "")
        }
        return null
    }
}