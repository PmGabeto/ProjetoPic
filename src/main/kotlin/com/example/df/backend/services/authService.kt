package com.example.df.backend.services

import com.example.df.backend.repositories.UsuarioRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory // Importante para log profissional ou use println

@Service
class AutenticacaoService(
    private val repository: UsuarioRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        println("\n--- DEBUG LOGIN: Iniciando busca por: $username ---")

        // 1. Tenta achar por E-mail
        var usuarioOpt = repository.findByEmail(username)

        // 2. Se não achou, tenta por CPF
        if (usuarioOpt.isEmpty) {
            val cpfLimpo = username.replace(Regex("[^0-9]"), "")
            println("--- DEBUG LOGIN: E-mail não encontrado. Tentando CPF: $cpfLimpo ---")
            usuarioOpt = repository.findByCpf(cpfLimpo)
        }

        if (usuarioOpt.isPresent) {
            val user = usuarioOpt.get()
            println("--- DEBUG LOGIN: Usuário ENCONTRADO! ID: ${user.id}, Perfil: ${user.perfil} ---")
            println("--- DEBUG LOGIN: Authorities geradas: ${user.authorities} ---")
            return user
        } else {
            println("--- DEBUG LOGIN: Usuário NÃO encontrado no banco. Lançando Exception. ---")
            throw UsernameNotFoundException("Usuário não encontrado: $username")
        }
    }
}