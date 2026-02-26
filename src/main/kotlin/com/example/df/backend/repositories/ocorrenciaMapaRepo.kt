package com.example.df.backend.repositories


import com.example.df.backend.entities.OcorrenciaMapa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface OcorrenciaMapaRepository : JpaRepository<OcorrenciaMapa, Long> {

    /**
     * Busca tokens do mesmo tipo e status ativo dentro de um raio (Haversine Formula).
     * Retorna a lista de vizinhos.
     * 6371000 = Raio da terra em metros.
     */
    @Query(
        value = """
        SELECT * FROM OCORRENCIA_MAPA t
        WHERE t.tipo = :tipoString
        AND t.status IN ('ATIVO', 'EM_ANALISE')
        AND (
            6371000 * acos(
                LEAST(1.0, GREATEST(-1.0, 
                    cos(radians(:lat)) * cos(radians(CAST(t.latitude AS DOUBLE PRECISION))) *
                    cos(radians(CAST(t.longitude AS DOUBLE PRECISION)) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(CAST(t.latitude AS DOUBLE PRECISION)))
                ))
            )
        ) < :raioMetros
    """, nativeQuery = true
    )
    fun encontrarDuplicidades(
        @Param("lat") latitude: BigDecimal,
        @Param("lon") longitude: BigDecimal,
        @Param("tipoString") tipoString: String, // Passamos Enum como String
        @Param("categoriaProblema") categoriaProblema: String?,
        @Param("raioMetros") raioMetros: Int,

        ): List<OcorrenciaMapa>

    @Query(
        value = """
        SELECT * FROM OCORRENCIA_MAPA t
        WHERE t.tipo = :tipo
        AND t.status IN ('ATIVO', 'EM_ANALISE')
        AND (:categoria IS NULL OR t.categoria_problema = :categoria)
        AND ST_DWithin(
            ST_SetSRID(ST_MakePoint(CAST(t.longitude AS DOUBLE PRECISION), CAST(t.latitude AS DOUBLE PRECISION)), 4326)::geography,
            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
            :raio
        )
    """, nativeQuery = true
    )
    fun encontrarDuplicidades(
        @Param("lat") latitude: Double,
        @Param("lon") longitude: Double,
        @Param("tipo") tipo: String,
        @Param("categoria") categoria: String?,
        @Param("raio") raioMetros: Double
    ): List<OcorrenciaMapa>


    @Query(value = """
        SELECT * FROM OCORRENCIA_MAPA t
        WHERE 1=1
        -- Filtro de Categoria (Só aplica se não for nulo)
        AND (:categoria IS NULL OR t.categoria_problema = :categoria)
        
        -- Filtro de Status (Só aplica se não for nulo)
        AND (:status IS NULL OR t.status = :status)
        
        -- Filtro de Área (Bounding Box)
        -- Se minLat for nulo, ignora a parte geográfica e traz tudo
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