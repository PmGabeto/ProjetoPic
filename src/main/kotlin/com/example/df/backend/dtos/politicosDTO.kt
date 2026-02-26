package com.example.df.backend.dtos

import com.example.df.backend.enums.*
import java.time.LocalDate

// --- DTOs AUXILIARES ---

data class TemaDTO(
    val id: Long,
    val nome: String
)

data class TipoProposicaoDTO(
    val sigla: String,
    val nome: String,
    val descricaoPedagogica: String
)

data class DocumentoDTO(
    val idExterno: Long,
    val tipo: String,
    val data: LocalDate?,
    val link: String?,
    val autor: String?
)

data class HistoricoDTO(
    val data: LocalDate,
    val fase: String,
    val unidadeResponsavel: String?,
    val descricao: String?
)

// --- DTOs DE POLÍTICO ---

data class PoliticoResumoDTO(
    val id: Long,
    val idExterno: Long? = null,
    val nome: String,
    val partido: String?,
    val status: StatusPolitico,
    val tipoAutor: TipoAutor? = null,
    val foto: String?
)

data class PoliticoDetalheDTO(
    val id: Long,
    val nomeCompleto: String,
    val nomeUrna: String?,
    val status: StatusPolitico,
    val tipoAutor: TipoAutor? = null,
    val partido: String?,
    val foto: String?,
    val biografia: String?,
    val baseEleitoral: String?,
    val entidadesVinculadas: String? = null,
    val proposicoes: List<ProposicaoResumoDTO>
)

// --- DTOs DE PROPOSIÇÃO (SAÍDA) ---

data class ProposicaoResumoDTO(
    val id: Long,
    val idExterno: Long? = null,
    val tipo: TipoProposicaoDTO,
    val numero: String,
    val titulo: String,
    val tema: List<TemaDTO>, // Refatorado para Lista
    val status: String,
    val data: LocalDate,
    val minhaVinculacao: TipoVinculacao?
)

data class ProposicaoDetalheDTO(
    val id: Long,
    val idExterno: Long,
    val tipo: TipoProposicaoDTO,
    val numeroProcesso: String,
    val numeroDefinitivo: String?,
    val titulo: String,
    val ementa: String?,
    val statusTramitacao: String?,
    val regiaoAdministrativa: String?,
    val regimeUrgencia: Boolean,
    val dataApresentacao: LocalDate,
    val dataLimite: LocalDate?,
    val tema: List<TemaDTO>, // Refatorado para Lista
    val documentos: List<DocumentoDTO>,
    val historicos: List<HistoricoDTO>,
    val autores: List<PoliticoResumoDTO>
)

// --- DTOs DE ENTRADA ---

data class CriarPoliticoDTO(
    val idExterno: Long? = null,
    val nomeCompleto: String,
    val nomeUrna: String?,
    val tipoAutor: TipoAutor = TipoAutor.PARLAMENTAR,
    val partidoAtual: String?,
    val urlFoto: String?,
    val raBaseEleitoral: String?,
    val biografiaResumida: String?,
    val entidadesVinculadas: String? = null
)

data class AtualizarPoliticoDTO(
    val nomeUrna: String?,
    val partidoAtual: String?,
    val urlFoto: String?,
    val raBaseEleitoral: String?,
    val biografiaResumida: String?,
    val status: StatusPolitico?,
    val entidadesVinculadas: String?
)

data class CriarProposicaoDTO(
    val idExterno: Long? = null,
    val tipoSigla: String,
    val numero: String,
    val titulo: String,
    val ementa: String?,
    val statusTramitacao: String,
    val dataApresentacao: LocalDate,
    val temaId: List<Long>, // Refatorado para receber lista de IDs
    val linkCompleto: String?,
    val tipoVinculacao: TipoVinculacao
)

data class NovoHistoricoDTO(
    val dataEvento: LocalDate,
    val faseTramitacao: String,
    val unidadeResponsavel: String? = null,
    val descricao: String,
    val atualizarStatusDaProposicao: Boolean = true
)