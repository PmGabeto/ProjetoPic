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

    @Column(name = "PUBLIC_ID", nullable = false, unique = true, length = 12)
    val publicId: String, // O ShortID aleatório para URLs seguras (Ex: k9B2xP7mL1)

    @Column(name = "CAMINHO_ARQUIVO", nullable = false)
    val caminhoArquivo: String, // Nome do arquivo físico no disco (Ex: 20240321_1030.abc.jpg)

    @Column(name = "TIPO_MIDIA")
    var tipoMidia: String? = null, // Identificador do dono: "OBRA", "PERFIL", "DEPUTADO", "OCORRENCIA"

    @Column(name = "ID_RELACIONADO")
    var idRelacionado: Long? = null, // ID numérico da Obra, do Usuário ou do Deputado

    @Column(name = "NOME_ORIGINAL")
    val nomeOriginal: String?, // Nome original que o usuário subiu (Ex: minha_foto.jpg)

    @Column(name = "TAMANHO_BYTES")
    val tamanho: Long,

    @Column(name = "CONTENT_TYPE")
    val contentType: String?, // MimeType (Ex: image/jpeg)

    @Column(name = "DT_UPLOAD")
    val dataUpload: LocalDateTime = LocalDateTime.now(),

    /**
     * Relacionamento legado com OcorrenciaMapa.
     * Mantido para compatibilidade com o Mapa, mas agora é opcional (nullable = true)
     * para permitir que fotos de Perfil/Obras existam sem um token de ocorrência.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TOKEN", nullable = true)
    @JsonIgnore
    var token: OcorrenciaMapa? = null
)