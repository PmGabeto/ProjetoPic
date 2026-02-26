package com.example.df.backend.repositories

import com.example.df.backend.entities.Obra
import com.example.df.backend.enums.OrgaoExecutor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ObraRepository : JpaRepository<Obra, Long> {

    @Query("""
        SELECT o FROM Obra o 
        WHERE (:ra IS NULL OR o.raAdministrativa = :ra)
        AND (:orgao IS NULL OR o.orgaoExecutor = :orgao)
        ORDER BY 
            (CASE WHEN o.status = 'CONCLUIDA' THEN 2 ELSE 1 END) ASC, 
            o.nome ASC
    """)
    fun buscarComFiltros(ra: String?, orgao: OrgaoExecutor?): List<Obra>
}