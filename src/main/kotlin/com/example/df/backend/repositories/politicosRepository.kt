package com.example.df.backend.repositories

import com.example.df.backend.entities.Autoria
import com.example.df.backend.entities.AutoriaId
import com.example.df.backend.entities.Politico
import com.example.df.backend.entities.Proposicao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PoliticoRepository : JpaRepository<Politico, Long> {
    // Busca por nome (case insensitive) para busca rápida
    fun findByNomeUrnaContainingIgnoreCase(nome: String): List<Politico>
    fun findByNomeCompletoContainingIgnoreCase(nome: String): List<Politico>    fun findByPublicId(publicId: String): Politico?}

@Repository
interface ProposicaoRepository : JpaRepository<Proposicao, Long> {
    fun findByPublicId(publicId: String): Proposicao?
    fun existsByPublicId(publicId: String): Boolean
}

@Repository
interface AutoriaRepository : JpaRepository<Autoria, AutoriaId> {
    fun findByPolitico(politico: Politico): List<Autoria>
    fun findByProposicaoAndPolitico(proposicao: Proposicao, politico: Politico): Autoria?
}