package com.example.df.backend.repositories

import com.example.df.backend.entities.OcorrenciaMapa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OcorrenciaMapaRepository : JpaRepository<OcorrenciaMapa, Long> {

    /**
     * Busca tokens próximos para evitar duplicidade usando PostGIS.
     * Usamos Double para latitude e longitude na query nativa para melhor compatibilidade.
     */
    @Query(value = """
        SELECT * FROM OCORRENCIA_MAPA t
        WHERE t.status IN ('ATIVO', 'EM_ANALISE')
        AND ST_DistanceSphere(
            ST_MakePoint(CAST(t.longitude AS DOUBLE PRECISION), CAST(t.latitude AS DOUBLE PRECISION)),
            ST_MakePoint(:lon, :lat)
        ) <= :raio
    """, nativeQuery = true)
    fun buscarTokensProximos(
        @Param("lat") latitude: Double,
        @Param("lon") longitude: Double,
        @Param("raio") raioMetros: Double
    ): List<OcorrenciaMapa>

    /**
     * Busca avançada com filtros dinâmicos e Bounding Box para o Mapa.
     */
    @Query(value = """
        SELECT * FROM OCORRENCIA_MAPA t
        WHERE 1=1
        AND (:categoria IS NULL OR t.categoria_problema = :categoria)
        AND (:status IS NULL OR t.status = :status)
        AND (
            CAST(:minLat AS DOUBLE PRECISION) IS NULL 
            OR 
            ST_Within(
                ST_SetSRID(ST_MakePoint(CAST(t.longitude AS DOUBLE PRECISION), CAST(t.latitude AS DOUBLE PRECISION)), 4326),
                ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
            )
        )
        ORDER BY t.dt_criacao DESC
    """, nativeQuery = true)
    fun buscarComFiltros(
        @Param("categoria") categoria: String?,
        @Param("status") status: String?,
        @Param("minLat") minLat: Double?,
        @Param("minLon") minLon: Double?,
        @Param("maxLat") maxLat: Double?,
        @Param("maxLon") maxLon: Double?
    ): List<OcorrenciaMapa>
}