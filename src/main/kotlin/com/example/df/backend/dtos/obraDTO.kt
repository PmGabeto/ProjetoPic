package com.example.df.backend.dtos

import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.StatusObra
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

// 1. DTO LEVE PARA O MAPA (Só o necessário para o PIN)
data class ObraPinDTO(
    @field:Schema(description = "ID da Obra", example = "1")
    val id: Long,
    @field:Schema(description = "Latitude", example = "-15.8000")
    val latitude: Double,
    @field:Schema(description = "Longitude", example = "-15.8000")
    val longitude: Double,
    @field:Schema(description = "Nome da Obra", example = "Viaduto do Recanto das Emas")
    val nome: String,
    @field:Schema(description = "Status atual", example = "EM_ANDAMENTO")
    val status: StatusObra,
    @field:Schema(description = "Progresso em porcentagem", example = "45")
    val progresso: Int // Ex: 45% (Para pintar a cor do pin ou ícone)
)

// 2. DTO PARA A LISTAGEM (Aba Lateral)
data class ObraListagemDTO(
    @field:Schema(description = "ID da Obra", example = "1")
    val id: Long,
    @field:Schema(description = "Nome da Obra", example = "Viaduto do Recanto das Emas")
    val nome: String,
    @field:Schema(description = "Órgão responsável", example = "NOVACAP")
    val orgao: OrgaoExecutor,
    @field:Schema(description = "Região Administrativa", example = "RECANTO_DAS_EMAS")
    val ra: String,
    @field:Schema(description = "Status atual", example = "EM_ANDAMENTO")
    val status: StatusObra,
    @field:Schema(description = "Progresso em porcentagem", example = "45")
    val progresso: Int
)

// 3. DTO COMPLETO (Quando clica para ver detalhes)
data class ObraDetalheDTO(
    @field:Schema(description = "ID da Obra", example = "1")
    val id: Long,
    @field:Schema(description = "Nome completo da Obra", example = "Viaduto do Recanto das Emas")
    val nome: String,
    @field:Schema(description = "Descrição detalhada", example = "Construção de viaduto para desafogar trânsito.")
    val descricao: String?,
    @field:Schema(description = "Órgão executor", example = "NOVACAP")
    val orgao: OrgaoExecutor,
    @field:Schema(description = "Região Administrativa", example = "RECANTO_DAS_EMAS")
    val ra: String,
    @field:Schema(description = "Status atual", example = "EM_ANDAMENTO")
    val status: StatusObra,
    @field:Schema(description = "Orçamento estimado", example = "15000000.00")
    val orcamento: BigDecimal?,
    @field:Schema(description = "Progresso em %", example = "45")
    val progresso: Int,
    @field:Schema(description = "Data de Início", example = "2023-01-15")
    val inicio: LocalDate?,
    @field:Schema(description = "Data de Fim prevista", example = "2024-12-30")
    val fim: LocalDate?,
    @field:Schema(description = "Link do documento no DODF", example = "https://dodf.df.gov.br/...")
    val linkDocumento: String?,
    @field:Schema(description = "Histórico de mudanças", implementation = HistoricoResumoDTO::class)
    val historico: List<HistoricoResumoDTO>,
    @field:Schema(description = "Aditivos de contrato", implementation = AditivoResumoDTO::class)
    val aditivos: List<AditivoResumoDTO>
)

// 4. DTO PARA CADASTRAR (Manual para o MVP)
data class CriarObraDTO(
    @field:Schema(description = "Nome da Obra", example = "Viaduto do Recanto das Emas")
    val nome: String,
    @field:Schema(description = "Descrição", example = "Construção de viaduto para desafogar o trânsito da região.")
    val descricao: String?,
    @field:Schema(description = "Latitude", example = "-15.8000")
    val latitude: Double,
    @field:Schema(description = "Longitude", example = "-47.9000")
    val longitude: Double,
    @field:Schema(description = "Região Administrativa", example = "RECANTO_DAS_EMAS")
    val ra: String,
    @field:Schema(description = "Órgão executor", example = "NOVACAP")
    val orgao: OrgaoExecutor,
    @field:Schema(description = "Status inicial", example = "LICITACAO")
    val status: StatusObra,
    @field:Schema(description = "Empresa ganhadora", example = "Construtora Alfa")
    val empresaContratada: String?,
    @field:Schema(description = "Orçamento previsto", example = "15000000.00")
    val orcamentoPrevisto: BigDecimal?,
    @field:Schema(description = "Progresso inicial", example = "0")
    val progresso: Int? = 0,
    @field:Schema(description = "Data de Início", example = "2023-01-15")
    val dataInicio: LocalDate?,
    @field:Schema(description = "Data final prevista", example = "2024-12-30")
    val dataFim: LocalDate?,
    @field:Schema(description = "URL do contrato ou diário oficial", example = "https://dodf.df.gov.br/contrato_123")
    val urlDocumento: String?
)
data class HistoricoResumoDTO(
    @field:Schema(description = "Data em que o evento/mudança ocorreu", example = "2023-12-30")
    val data: LocalDate,

    @field:Schema(description = "Descrição detalhada do que ocorreu", example = "A obra foi paralisada devido a fortes chuvas na região.")
    val descricao: String?,

    @field:Schema(description = "Novo status atribuído à obra após o evento", example = "PARALISADA")
    val statusNovo: StatusObra
)

data class AditivoResumoDTO(
    @field:Schema(description = "Data de aprovação do aditivo", example = "2024-01-15")
    val data: LocalDate,

    @field:Schema(description = "Valor financeiro adicionado ao contrato", example = "1500000.00")
    val valor: BigDecimal,

    @field:Schema(description = "Motivo legal ou técnico do aditivo", example = "Reajuste anual de preços dos materiais de construção civil.")
    val justificativa: String?
)

data class AtualizarProgressoRequest(
    @field:Schema(description = "Novo percentual de conclusão da obra (0 a 100)", example = "75")
    val novoPercentual: Int,

    @field:Schema(description = "Novo status da obra (enviar apenas se houve mudança de fase)", example = "EM_ANDAMENTO")
    val novoStatus: StatusObra?,

    @field:Schema(description = "Nota ou relatório técnico sobre a atualização", example = "Fase de fundação e pilares finalizada com sucesso.")
    val descricao: String?
)