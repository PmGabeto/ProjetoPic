package com.example.df.backend.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "AUTORIA_PROPOSICAO", indexes = [
    Index(name = "idx_autoria_politico", columnList = "ID_POLITICO")
])
class Autoria(

    @EmbeddedId
    val id: AutoriaId = AutoriaId(),

    @Column(name = "tipo_vinculacao", nullable = false)
    var tipoVinculacao: String = "AUTOR",
    // --- LADO DO POLÍTICO ---
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPolitico") // Conecta com o campo da chave composta
    @JoinColumn(name = "ID_POLITICO")
    @JsonIgnore // Evita loop JSON
    val politico: Politico,

    // --- LADO DA PROPOSIÇÃO ---
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProposicao") // Conecta com o campo da chave composta
    @JoinColumn(name = "ID_PROPOSICAO")
    val proposicao: Proposicao
)

// =================================================================
// CLASSE AUXILIAR (CHAVE COMPOSTA)
// =================================================================

@Embeddable
data class AutoriaId(
    var idPolitico: Long? = null,
    var idProposicao: Long? = null
) : Serializable
