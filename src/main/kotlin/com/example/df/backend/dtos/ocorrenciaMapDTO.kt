package com.example.df.backend.dtos

import com.example.df.backend.enums.StatusOcorrencia
import com.example.df.backend.enums.TIpoOcorrencia
import com.example.df.backend.enums.TipoProblema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CriarOcorrenciaDTO(
    @field:Schema(description = "Tipo principal da ocorrência", example = "PROBLEMA_URBANO")
    @field:NotNull(message = "O tipo do token é obrigatório")
    var tipo: TIpoOcorrencia,

    @field:Schema(description = "Título da ocorrência", example = "Buraco perigoso na via")
    @field:NotBlank(message = "O nome/título é obrigatório")
    val nome: String,
    @field:Schema(description = "Descrição detalhada", example = "Buraco abriu após as fortes chuvas de ontem.")
    val descricao: String? = null,

    // Categoria é opcional (só para Problema Urbano)
    @field:Schema(description = "Sub-categoria do problema", example = "BOCA_DE_LOBO")
    val categoriaProblema: TipoProblema? = null,

    // Usuário pode mandar coordenadas OU endereço
    @field:Schema(description = "Latitude coordenada (GPS)", example = "-15.834456")
    val latitude: BigDecimal? = null,
    @field:Schema(description = "Longitude coordenada (GPS)", example = "-15.834456")
    val longitude: BigDecimal? = null,
    @field:Schema(description = "Endereço por extenso (se não usar GPS)", example = "QNL 12, Taguatinga")
    val endereco: String? = null
)

// atualizando o status dos tokens.
data class AtualizarStatusOcorrencia(
    @field:Schema(description = "Novo status da ocorrência", example = "RESOLVIDO")
    @field:NotNull(message= "O novo status é obrigatório")
    var status: StatusOcorrencia
)