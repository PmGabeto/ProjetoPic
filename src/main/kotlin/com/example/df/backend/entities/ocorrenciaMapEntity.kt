package com.example.df.backend.entities

import com.example.df.backend.enums.StatusOcorrencia
import com.example.df.backend.enums.TipoProblema
import com.example.df.backend.enums.TIpoOcorrencia
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "OCORRENCIA_MAPA")
class OcorrenciaMapa(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_OCORRENCIA")
    val id: Long? = null,

    // --- ENUMS COM VALOR PADRÃO (Essencial para o Hibernate) ---
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 20)
    val tipo: TIpoOcorrencia = TIpoOcorrencia.PROBLEMA_URBANO,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    var status: StatusOcorrencia = StatusOcorrencia.ATIVO,

    @Column(name = "QTD_REPORTES", nullable = false)
    var quantidadeReportes: Int = 1,

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORIA_PROBLEMA", length = 30)
    var categoriaProblema: TipoProblema? = null,


    @Column(name = "LATITUDE", nullable = false, precision = 10, scale = 7)
    val latitude: BigDecimal = BigDecimal.ZERO,

    @Column(name = "LONGITUDE", nullable = false, precision = 10, scale = 7)
    val longitude: BigDecimal = BigDecimal.ZERO,

    // --- DADOS TEXTUAIS ---
    @Column(name = "NOME", length = 255)
    var nome: String? = null,

    @Column(name = "DESCRICAO", columnDefinition = "TEXT")
    var descricao: String? = null,

    @Column(name = "DT_CRIACAO", nullable = false, updatable = false)
    val dataCriacao: LocalDateTime = LocalDateTime.now(),

    // --- RELACIONAMENTOS ---
    @Column(name = "ID_USUARIO_CRIADOR")
    val idUsuarioCriador: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_OBRA")
    val obra: Obra? = null,

    @OneToMany(mappedBy = "token", cascade = [CascadeType.ALL], orphanRemoval = true)
    var fotos: MutableList<OcorrenciaFoto> = mutableListOf()

) {
    // --- MÉTODOS AUXILIARES ---

    fun adicionarFoto(foto: OcorrenciaFoto) {
        fotos.add(foto)
        foto.token = this
    }

    fun removerFoto(foto: OcorrenciaFoto) {
        fotos.remove(foto)
        foto.token = null
    }

    // --- OVERRIDES (Mantidos para segurança do JPA e Logs) ---

    override fun toString(): String {
        return "OcorrenciaMapa(id=$id, tipo=$tipo, status=$status, nome=$nome, lat=$latitude, long=$longitude)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OcorrenciaMapa) return false

        // Se ambos tiverem ID nulo, não são iguais (ainda não persistidos)
        if (id == null || other.id == null) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        // Retorna o hash do ID ou 0 se for nulo (padrão comum em JPA)
        return id?.hashCode() ?: 0
    }
}