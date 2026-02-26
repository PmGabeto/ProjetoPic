package com.example.df.backend.dtos

import com.example.df.backend.enums.OrgaoExecutor
import com.example.df.backend.enums.StatusObra
import java.math.BigDecimal
import java.time.LocalDate

// 1. DTO LEVE PARA O MAPA (Só o necessário para o PIN)
data class ObraPinDTO(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val nome: String,
    val status: StatusObra,
    val progresso: Int // Ex: 45% (Para pintar a cor do pin ou ícone)
)

// 2. DTO PARA A LISTAGEM (Aba Lateral)
data class ObraListagemDTO(
    val id: Long,
    val nome: String,
    val orgao: OrgaoExecutor,
    val ra: String,
    val status: StatusObra,
    val progresso: Int
)

// 3. DTO COMPLETO (Quando clica para ver detalhes)
data class ObraDetalheDTO(
    val id: Long,
    val nome: String,
    val descricao: String?,
    val orgao: OrgaoExecutor,
    val ra: String,
    val status: StatusObra,
    val orcamento: BigDecimal?,
    val progresso: Int,
    val inicio: LocalDate?,
    val fim: LocalDate?,
    val linkDocumento: String?,

    val historico:List<HistoricoResumoDTO>,
    val aditivos: List<AditivoResumoDTO>
)

// 4. DTO PARA CADASTRAR (Manual para o MVP)
data class CriarObraDTO(
    val nome: String,
    val descricao: String?,
    //front manda Double
    val latitude: Double,
    val longitude: Double,
    val ra: String,
    val orgao: OrgaoExecutor,
    val status: StatusObra,
    val empresaContratada: String? ,
    val orcamentoPrevisto: BigDecimal?,
    val progresso: Int? = 0,
    //Datas
    val dataInicio: LocalDate?,
    val dataFim: LocalDate?,
    val urlDocumento: String?
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
data class AtualizarProgressoRequest(
    val novoPercentual: Int,
    val novoStatus: StatusObra?,
    val descricao: String?
)