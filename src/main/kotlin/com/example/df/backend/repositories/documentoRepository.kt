package com.example.df.backend.repositories

import com.example.df.backend.entities.DocumentosArquivos

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentoRepository : JpaRepository<DocumentosArquivos, Long>{    // O JpaRepository já nos dá o save(), findById(), findAll() e delete()
    // Como usamos o ID da CLDF como @Id, o findById já buscará pelo código deles.
// Busca todos os documentos de uma Obra específica
    // Uso: findByTipoRelacionadoAndIdRelacionado("OBRA", 5)
    fun findByTipoRelacionadoAndIdRelacionado(tipo: String, id: Long): List<DocumentosArquivos>

// Busca documentos vindo da CLDF (para manter a compatibilidade que você já tinha)
    fun findByPublicId(publicId: String): DocumentosArquivos?


}