package com.example.df.backend.dtos

import com.example.df.backend.enums.StatusOcorrencia
import com.example.df.backend.enums.TIpoOcorrencia
import com.example.df.backend.enums.TipoProblema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * DTO para criação de novas ocorrências via API.
 */
data class CriarOcorrenciaDTO(
    @field:Schema(description = "Tipo principal da ocorrência", example = "PROBLEMA_URBANO")
    @field:NotNull(message = "O tipo do token é obrigatório")
    var tipo: TIpoOcorrencia,

    @field:Schema(description = "Título da ocorrência", example = "Buraco perigoso na via")
    @field:NotBlank(message = "O nome/título é obrigatório")
    val nome: String,

    @field:Schema(description = "Descrição detalhada", example = "Buraco abriu após as fortes chuvas de ontem.")
    val descricao: String? = null,

    @field:Schema(description = "Sub-categoria do problema", example = "BOCA_DE_LOBO")
    val categoriaProblema: TipoProblema? = null,

    @field:Schema(description = "Latitude coordenada (GPS)", example = "-15.834456")
    val latitude: BigDecimal? = null,

    @field:Schema(description = "Longitude coordenada (GPS)", example = "-47.912345")
    val longitude: BigDecimal? = null,

    @field:Schema(description = "Endereço por extenso (se não usar GPS)", example = "QNL 12, Taguatinga")
    val endereco: String? = null
)

/**
 * DTO para atualização de status por administradores.
 */
data class AtualizarStatusOcorrencia(
    @field:Schema(description = "Novo status da ocorrência", example = "RESOLVIDO")
    @field:NotNull(message = "O novo status é obrigatório")
    var status: StatusOcorrencia
)

/**
 * DTO completo para retorno de detalhes (usado no Mapa e Listas).
 */
data class OcorrenciaDetalheDTO(
    val id: Long,
    val tipo: TIpoOcorrencia,
    var status: StatusOcorrencia,
    val nome: String,
    val descricao: String?,
    val categoriaProblema: TipoProblema?,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val quantidadeReportes: Int,
    val dataCriacao: LocalDateTime,
    val fotos: List<FotoResponseDTO>
)

/**
 * DTO de resposta para mídias, agora incluindo o publicId seguro.
 */
data class FotoResponseDTO(
    @field:Schema(description = "ID interno do banco", example = "1")
    val id: Long,

    @field:Schema(description = "ID público de 12 caracteres (usado para URLs)", example = "k9B2xP7mL1")
    val publicId: String,

    @field:Schema(description = "URL completa para renderização", example = "https://vigiadf.pmhub.cloud/api/foto/v/k9B2xP7mL1")
    val url: String,

    @field:Schema(description = "Nome original do arquivo subido", example = "foto_da_rua.jpg")
    val nomeOriginal: String?
)