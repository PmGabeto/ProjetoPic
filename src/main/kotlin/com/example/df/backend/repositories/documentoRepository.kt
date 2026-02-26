package com.example.df.backend.repositories

import com.example.df.backend.entities.DocumentoProposicao

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentoRepository : JpaRepository<DocumentoProposicao, Long>    // O JpaRepository já nos dá o save(), findById(), findAll() e delete()
    // Como usamos o ID da CLDF como @Id, o findById já buscará pelo código deles.
