package com.example.df.backend.repositories

import com.example.df.backend.entities.Obra
import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.RaAdministrativa
import com.example.df.backend.enums.StatusObra
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ObraRepository : JpaRepository<Obra, Long> {

    // Busca para a Lista Lateral com filtros dinâmicos
    @Query("""
        SELECT o FROM Obra o 
        WHERE (:raId IS NULL OR o.raAdministrativa.id = :raId)
        AND (:orgao IS NULL OR o.orgaoExecutor = :orgao)
        AND (:status IS NULL OR o.status IN :status)
        ORDER BY 
            (CASE WHEN o.status = 'CONCLUIDA' THEN 2 ELSE 1 END) ASC, 
            o.nome ASC
    """)
    fun buscarComFiltros(
        raId: Long?,
        orgao: OrgaoExecutor?,
        status: List<StatusObra>?
    ): List<Obra>

    // Projeção para o Mapa (Busca ultra-rápida)
    @Query("""
        SELECT o.id as id, o.latitude as latitude, o.longitude as longitude, 
           o.nome as nome, o.endereco as endereco, o.status as status, 
           o.orgaoExecutor as orgaoExecutor, o.raAdministrativa as raAdministrativa,
           o.percentualConclusao as progresso
        FROM Obra o 
        WHERE o.status != 'CONCLUIDA' OR :incluirConcluidas = true
    """)
    fun buscarDadosSimplificadosMapa(incluirConcluidas: Boolean): List<ObraPinProjection>
}

// Interface para carregar apenas o necessário para o PIN
interface ObraPinProjection {
    val id: Long
    val latitude: Double
    val longitude: Double
    val nome: String
    val endereco: String?
    val status: StatusObra
    val orgaoExecutor: OrgaoExecutor
    val raAdministrativa: RaAdministrativa
    val progresso: Int
}