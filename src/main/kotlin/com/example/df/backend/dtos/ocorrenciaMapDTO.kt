package com.example.df.backend.dtos

import com.example.df.backend.enums.StatusOcorrencia
import com.example.df.backend.enums.TIpoOcorrencia
import com.example.df.backend.enums.TipoProblema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CriarOcorrenciaDTO(
    @field:NotNull(message = "O tipo do token é obrigatório")
    val tipo: TIpoOcorrencia,

    @field:NotBlank(message = "O nome/título é obrigatório")
    val nome: String,

    val descricao: String? = null,

    // Categoria é opcional (só para Problema Urbano)
    val categoriaProblema: TipoProblema? = null,

    // Usuário pode mandar coordenadas OU endereço
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val endereco: String? = null
)

// atualizando o status dos tokens.
data class AtualizarStatusOcorrencia(
    @field:NotNull(message= "O novo status é obrigatório")
    val status: StatusOcorrencia
)