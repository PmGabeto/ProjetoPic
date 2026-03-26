package com.example.df.backend.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "DOCUMENTO_GERAIS")
data class DocumentosArquivos(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DOCUMENTO")
    val id: Long? = null ,
    @Column(name = "publicId", unique = true, nullable = false)
    val publicId: String,
    // Mantemos para compatibilidade com a CLDF, mas deixamos opcional


    @Column(name = "TIPO_DOCUMENTO", nullable = false)
    val tipoDocumento: String, // Ex: "PROJETO_LEI", "CONTRATO_OBRA", "EDITAL"

    @Column(name = "NOME_EXIBICAO", nullable = false)
    val nomeExibicao: String, // Nome que o usuário vê

    @Column(name = "NOME_STORAGE", nullable = true, unique = true)
    val nomeStorage: String?=null, // Nome único do arquivo no seu servidor (ex: UUID.pdf)

    @Column(name = "LINK_DIRETO", length = 2000)
    val linkDireto: String? = null, // Caso o arquivo seja externo (como o da CLDF)

    @Column(name = "MIME_TYPE")
    val mimeType: String? = "application/pdf",

    // --- LOGICA GENÉRICA DE VINCULO ---
    @Column(name = "TIPO_RELACIONADO", nullable = false)
    val tipoRelacionado: String, // Ex: "PROPOSICAO", "OBRA", "CIDADAO"

    @Column(name = "ID_RELACIONADO", nullable = false)
    val idRelacionado: Long, // ID da Obra ou ID da Proposição

    @Column(name = "DATA_CADASTRO")
    val dataCadastro: LocalDateTime,

    @Column(name = "AUTOR")
    val autor: String? = null,

    @Column(name = "VALIDO_DESDE")
    val validoDesde: LocalDateTime
)