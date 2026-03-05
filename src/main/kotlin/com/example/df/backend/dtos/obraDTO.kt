package com.example.df.backend.dtos

import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.StatusObra
import com.example.df.backend.enums.RaAdministrativa
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

// =========================================================================
// 1. DTO PARA O MAPA (PINS) - Mantido sem alterações
// =========================================================================
data class ObraPinDTO(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val nome: String,
    val endereco: String?,
    val status: StatusObra,
    val orgao: OrgaoExecutor,
    val ra: RaAdministrativa,
    val progresso: Int
)

// =========================================================================
// 2. DTO PARA A LISTAGEM (ABA LATERAL) - Mantido sem alterações
// =========================================================================
data class ObraListagemDTO(
    val id: Long,
    val nome: String,
    val ra: RaAdministrativa,
    val orgao: OrgaoExecutor,
    val status: StatusObra,
    val progresso: Int,
    val dataAtualizacao: LocalDate
)

// =========================================================================
// 3. DTO COMPLETO (DETALHES) - MODIFICADO
// =========================================================================
data class ObraDetalheDTO(
    val id: Long,
    val nome: String,
    val descricao: String?,
    val endereco: String?,
    val ra: RaAdministrativa,
    val orgao: OrgaoExecutor,
    val status: StatusObra,
    val progresso: Int,
    val orcamentoPrevisto: BigDecimal?,
    val dataInicio: LocalDate?,
    val dataFim: LocalDate?,
    val empresaContratada: String?,

    val documentos: List<DocumentoResponseDTO> = listOf(), // Substituiu urlDocumento

    val fotos: List<FotoResponseDTO> = listOf(),
    val historico: List<HistoricoResumoDTO> = listOf(),
    val aditivos: List<AditivoResumoDTO> = listOf()
)

// =========================================================================
// 4. DTOS DE ATUALIZAÇÃO E CRIAÇÃO - MODIFICADOS
// =========================================================================

@Schema(description = "DTO para atualizar campos da obra. A justificativa é obrigatória para o histórico.")
data class AtualizarObraRequest(
    @field:Schema(description = "Justificativa da mudança para o histórico", example = "Atualização do cronograma físico-financeiro.")
    val descricaoMudanca: String,

    val novoPercentual: Int? = null,
    val novoStatus: StatusObra? = null,
    val novoNome: String? = null,
    val novaDescricao: String? = null,
    val novoEndereco: String? = null,
    val novaDataFim: LocalDate? = null,
    val novoOrcamentoBase: BigDecimal? = null,
    val novaEmpresa: String? = null,
    val novaRa: RaAdministrativa? = null
)

data class CriarObraDTO(
    val nome: String,
    val descricao: String?,
    val endereco: String?,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val ra: RaAdministrativa,
    val orgao: OrgaoExecutor,
    val status: StatusObra,
    val progresso: Int? = 0,
    val orcamentoPrevisto: BigDecimal?,
    val empresaContratada: String?,
    val dataInicio: LocalDate?,
    val dataFim: LocalDate?
)

// =========================================================================
// 5. AUXILIARES (Financeiro, Histórico, etc.) - Mantidos
// =========================================================================
data class CriarAditivoDTO(
    val valor: BigDecimal,
    val dataAprovacao: LocalDate,
    val justificativa: String
)

data class HistoricoResumoDTO(
    val data: LocalDate,
    val descricao: String?,
    val statusNovo: StatusObra
)

data class AditivoResumoDTO(
    val data: LocalDate,
    val valor: BigDecimal,
    val justificativa: String?
)