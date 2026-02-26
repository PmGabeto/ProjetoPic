package com.example.df.backend.repositories

import com.example.df.backend.entities.Autoria
import com.example.df.backend.entities.AutoriaId
import com.example.df.backend.entities.Politico
import com.example.df.backend.entities.Proposicao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PoliticoRepository : JpaRepository<Politico, Long> {
    // Exemplo: Buscar por nome (case insensitive) para busca rápida
    fun findByNomeUrnaContainingIgnoreCase(nome: String): List<Politico>
}

@Repository
interface ProposicaoRepository : JpaRepository<Proposicao, Long>

@Repository
interface AutoriaRepository : JpaRepository<Autoria, AutoriaId>