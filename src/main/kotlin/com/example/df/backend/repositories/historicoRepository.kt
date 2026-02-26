package com.example.df.backend.repositories

import com.example.df.backend.entities.ProposicaoHistorico
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoricoRepository : JpaRepository<ProposicaoHistorico, Long>