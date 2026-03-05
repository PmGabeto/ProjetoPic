package com.example.df.backend.integrations.cldf

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * DTOs de Carga: Espelhos fiéis da API da CLDF.
 * Adaptados para aceitar 'publicId' como String padrão.
 */
data class CldfPageResponse<T>(
    val content: List<T>
)

data class ProposicaoCldfBaseDTO(
    @field:JsonProperty("id") val publicId: String,
    @field:JsonProperty("titulo") val titulo: String,
    @field:JsonProperty("numeroProcesso") val numeroProcesso: String,
    @field:JsonProperty("siglaTipo") val siglaTipo: String,
    @field:JsonProperty("ementa") val ementa: String?,
    @field:JsonProperty("temaId") val temaIdRaw: String?,
    @field:JsonProperty("temaNome") val temaNomeRaw: String?
)

data class HistoricoCldfDTO(
    @field:JsonProperty("id") val publicId: String,
    @field:JsonProperty("dataHistorico") val dataEvento: String?,
    @field:JsonProperty("faseTramitacao") val fase: String,
    @field:JsonProperty("unidadeResponsavel") val unidade: String?,
    @field:JsonProperty("textoHistorico") val descricao: String?
)

data class ProposicaoCldfCompletaDTO(
    @field:JsonProperty("id") val publicId: String, // Era idExterno, agora padronizado
    @field:JsonProperty("titulo") val titulo: String,
    @field:JsonProperty("numeroProcesso") val numeroProcesso: String,
    @field:JsonProperty("siglaTipo") val siglaTipo: String,
    @field:JsonProperty("ementa") val ementa: String?,
    @field:JsonProperty("dataApresentacao") val dataApresentacao: String?,
    @field:JsonProperty("dataLimite") val dataLimite: String?,
    @field:JsonProperty("statusAtual") val statusTramitacao: String?,
    @field:JsonProperty("regiaoAdministrativa") val regiaoAdministrativa: String?,
    @field:JsonProperty("urgencia") val regimeUrgencia: Boolean = false,
    @field:JsonProperty("temaId") val temaIdRaw: String?,
    @field:JsonProperty("temaNome") val temaNomeRaw: String?,
    @field:JsonProperty("linkDocumento") val linkCompleto: String?
)

data class DocumentoCldfDTO(
    @field:JsonProperty("id") val publicId: String?, // Nullable para prever a falha da API
    @field:JsonProperty("nomeArquivo") val nome: String,
    @field:JsonProperty("linkDocumento") val link: String,
    @field:JsonProperty("tipoDocumento") val tipo: String
)