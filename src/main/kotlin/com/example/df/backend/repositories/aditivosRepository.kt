package com.example.df.backend.repositories

import com.example.df.backend.entities.OrcamentoAditivo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AditivosRepository : JpaRepository<OrcamentoAditivo, Long>