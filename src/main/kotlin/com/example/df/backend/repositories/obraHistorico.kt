package com.example.df.backend.repositories

import com.example.df.backend.entities.ObraHistorico
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ObraHistoricoRepository : JpaRepository<ObraHistorico, Long>