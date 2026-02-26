package com.example.df.backend.dtos

import com.example.df.backend.enums.AreaTematica
import com.example.df.backend.enums.StatusPolitico
import com.example.df.backend.enums.TipoProjetoLei
import com.example.df.backend.enums.TipoVinculacao
import java.time.LocalDate

// PARA LEITURA (SAÍDA) //falta contato e endereço no simples
data class PoliticoResumoDTO(
    val id: Long,
    val nome: String,
    val partido: String?,
    val status: StatusPolitico, // Mostra se está ativo
    val foto: String?
)

data class PoliticoDetalheDTO(
    val id: Long,
    val nomeCompleto: String,
    val nomeUrna: String?,
    val status: StatusPolitico,
    val partido: String?,
    val foto: String?,
    val biografia: String?,
    val baseEleitoral: String?,
    val proposicoes: List<ProposicaoResumoDTO>
)

data class ProposicaoResumoDTO(
    val id: Long,
    val tipo: TipoProjetoLei,
    val numero: String,
    val titulo: String,
    val area: AreaTematica, // Novo enum
    val status: String,
    val data: LocalDate,
    val minhaVinculacao: TipoVinculacao
)

// PARA CRIAÇÃO (ENTRADA)
data class CriarPoliticoDTO(
    val nomeCompleto: String,
    val nomeUrna: String?,
    val partidoAtual: String?,
    val urlFoto: String?,
    val raBaseEleitoral: String?,
    val biografiaResumida: String?
)

// NOVO: PARA EDIÇÃO (ENTRADA)
data class AtualizarPoliticoDTO(
    val nomeUrna: String?,
    val partidoAtual: String?,
    val urlFoto: String?,
    val raBaseEleitoral: String?,
    val biografiaResumida: String?,
    val entidadesVinculadas: String?
)

data class CriarProposicaoDTO(
    val tipo: TipoProjetoLei,
    val numero: String,
    val titulo: String,
    val ementa: String?,
    val statusTramitacao: String,
    val dataApresentacao: LocalDate,
    val areaTematica: AreaTematica, // Novo enum
    val linkCompleto: String?,
    val tipoVinculacao: TipoVinculacao
)
data class NovoHistoricoDTO(
    val dataEvento: LocalDate, // Quando aconteceu?
    val faseTramitacao: String, // Ex: "CCJ", "Plenário"
    val descricao: String, // Ex: "Aprovado com 15 votos a favor"
    val atualizarStatusDaProposicao: Boolean = true // Se true, muda o status principal
)