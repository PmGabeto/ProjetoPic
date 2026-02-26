
package com.example.df.backend.repositories

import com.example.df.backend.entities.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Optional<Usuario>
    fun findByCpf(cpf: String): Optional<Usuario>
    fun existsByEmail(email: String): Boolean
    fun existsByCpf(cpf: String): Boolean
}