package com.example.df.backend.repositories

import com.example.df.backend.entities.Autoria
import com.example.df.backend.entities.AutoriaId
import com.example.df.backend.entities.Politico
import com.example.df.backend.entities.Proposicao
import com.example.df.backend.enums.TipoProjetoLei
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface PoliticoRepository : JpaRepository<Politico, Long> {
    // Busca por nome (case insensitive) para busca rápida
    fun findByNomeUrnaContainingIgnoreCase(nome: String): List<Politico>
    fun findByNomeCompletoContainingIgnoreCase(nome: String): List<Politico>    fun findByPublicId(publicId: String): Politico?}

@Repository
interface ProposicaoRepository : JpaRepository<Proposicao, Long> {
    fun findByPublicId(publicId: String): Proposicao?
    fun existsByPublicId(publicId: String): Boolean
    @Query("""
    SELECT DISTINCT p FROM Proposicao p
    JOIN p.autores a
    LEFT JOIN p.temas t
    LEFT JOIN p.regioesAdministrativas ra
    WHERE a.politico.id = :politicoId
      AND (:temaId IS NULL OR t.id = :temaId)
      AND (:raId IS NULL OR ra.id = :raId)
      AND (:tipo IS NULL OR p.tipo = :tipo)
      AND (cast(:dataInicio as date) IS NULL OR p.dataApresentacao >= :dataInicio)
      AND (cast(:dataFim as date) IS NULL OR p.dataApresentacao <= :dataFim)
AND (cast(:numero as string) IS NULL OR LOWER(p.numeroDefinitivo) LIKE LOWER(CONCAT('%', cast(:numero as string), '%')) OR LOWER(p.numeroProcesso) LIKE LOWER(CONCAT('%', cast(:numero as string), '%')))""")
    fun buscarProposicoesDoPoliticoComFiltros(
        @Param("politicoId") politicoId: Long,
        @Param("temaId") temaId: Long?,
        @Param("raId") raId: Long?,
        @Param("tipo") tipo: TipoProjetoLei?,
        @Param("dataInicio") dataInicio: LocalDate?,
        @Param("dataFim") dataFim: LocalDate?,
        @Param ("numero") numero: String?,
        pageable: Pageable
    ): Page<Proposicao>
}

@Repository
interface AutoriaRepository : JpaRepository<Autoria, AutoriaId> {
    fun findByPolitico(politico: Politico): List<Autoria>
    fun findByProposicaoAndPolitico(proposicao: Proposicao, politico: Politico): Autoria?
}