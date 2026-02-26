package com.example.df.backend.repositories

import com.example.df.backend.entities.OcorrenciaFoto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OcorrenciaFotoRepository : JpaRepository<OcorrenciaFoto, Long>