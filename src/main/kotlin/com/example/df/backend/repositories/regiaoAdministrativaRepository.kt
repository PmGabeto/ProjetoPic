package com.example.df.backend.repositories

import com.example.df.backend.entities.RegiaoAdministrativa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RegiaoAdministrativaRepository : JpaRepository<RegiaoAdministrativa, Long> {

    // Busca exata pelo ID da API
    fun findByPublicId(publicId: String): RegiaoAdministrativa?

    // Busca por nome (útil caso alguma API mande só o texto sem o ID)
    fun findByNomeContainingIgnoreCase(nome: String): RegiaoAdministrativa?
}