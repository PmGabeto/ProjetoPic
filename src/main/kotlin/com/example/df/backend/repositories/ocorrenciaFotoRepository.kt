package com.example.df.backend.repositories

import com.example.df.backend.entities.OcorrenciaFoto
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository

@Repository
interface OcorrenciaFotoRepository : JpaRepository<OcorrenciaFoto, Long> {

    // Busca para visualização segura via URL
    fun findByPublicId(publicId: String): OcorrenciaFoto?

    // Busca todas as fotos de um destino específico (Ex: Todas as fotos da Obra 10)
    fun findByTipoMidiaAndIdRelacionado(tipoMidia: String, idRelacionado: Long): List<OcorrenciaFoto>

    // Caso queira deletar via publicId
    @Modifying // Necessário para operações de DELETE personalizadas
    @Transactional
    fun deleteByPublicId(publicId: String)
}