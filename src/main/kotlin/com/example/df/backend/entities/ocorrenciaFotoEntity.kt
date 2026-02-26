package com.example.df.backend.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "OCORRENCIA_FOTOS")
class OcorrenciaFoto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "CAMINHO_ARQUIVO", nullable = false)
    val caminhoArquivo: String, // Ex: ./uploads/foto123.jpg

    @Column(name = "NOME_ORIGINAL")
    val nomeOriginal: String?, // Ex: buraco_na_rua.jpg

    @Column(name = "TAMANHO_BYTES")
    val tamanho: Long,

    @Column(name = "CONTENT_TYPE")
    val contentType: String?, // Ex: image/jpeg

    @Column(name = "DT_UPLOAD")
    val dataUpload: LocalDateTime = LocalDateTime.now(),

    // Relacionamento com o OcorrenciaMapa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TOKEN", nullable = false)
    @JsonIgnore // Evita loop infinito no JSON (Foto -> Token -> Foto...)
    var token: OcorrenciaMapa? = null
)