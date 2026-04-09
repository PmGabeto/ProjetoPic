package com.example.df.backend.dtos

import com.example.df.backend.entities.*
import com.example.df.backend.enums.*
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

// --- DTOs AUXILIARES ---
// DTOs temas CLDF
data class TemaDTO(
    @field: JsonProperty("value") // Isso diz ao Jackson: "Leia 'value' e salve em 'id'"
    @field:Schema(description = "ID do Tema", example = "10")
    val id: Long,
    @field: JsonProperty("label")
    @field:Schema(description = "Nome do Tema", example = "Educação")
    val nome: String
)
fun Tema.toDTO() = TemaDTO(
    id = this.id,
    nome = this.nome
)
@Schema(description = "Resultado da operação de sincronização de temas")
data class SincronizacaoResponseDTO(

    @field:Schema(description = "Mensagem descritiva do status da operação", example = "Sincronização realizada com sucesso.")
    val mensagem: String,

    @field:Schema(description = "Data e hora em que a sincronização foi executada")
    val timestamp: LocalDateTime = LocalDateTime.now()
)
// Dtos Proposicao
data class TipoProposicaoDTO(
    @field:Schema(description = "Sigla do projeto", example = "PL")
    val sigla: String,
    @field:Schema(description = "Nome completo do tipo", example = "Projeto de Lei")
    val nome: String,
   @field:Schema(description = "Explicação fácil para o cidadão", example = "Criação de novas leis para o DF.")
    val descricaoPedagogica: String
)
fun TipoProjetoLei.toDTO() = TipoProposicaoDTO(
    sigla = this.sigla,
    nome = this.nome,
    descricaoPedagogica = this.descricaoPedagogica
)



data class HistoricoDTO(
    @field:Schema(description = "Data do evento", example = "2024-02-15")
    val data: LocalDateTime,
    @field:Schema(description = "Fase atual da tramitação", example = "Aguardando Votação")
    val fase: String?,
    @field:Schema(description = "Unidade que está avaliando", example = "Plenário")
    val unidadeResponsavel: String?,
    @field:Schema(description = "Descrição da movimentação", example = "Encaminhado para votação.")
    val descricao: String?
)
fun ProposicaoHistorico.toDTO() = HistoricoDTO(
    data = this.dataEvento , // Ajuste conforme os nomes da sua Entity
    fase = this.faseTramitacao,
    unidadeResponsavel = this.unidadeResponsavel,
    descricao = this.descricao
)

// --- DTOs DE POLÍTICO ---

data class PoliticoResumoDTO(
    @field:Schema(description = "ID interno", example = "1")
    val id: Long,
    @field:Schema(description = "ID na CLDF", example = "12345")
    val publicId: String,
    @field:Schema(description = "Nome público", example = "João Deputado")
    val nome: String,
    @field:Schema(description = "Partido atual", example = "PDB")
    val partido: String?,
    @field:Schema(description = "Status atual", example = "ATIVO")
    val status: StatusPolitico,
    @field:Schema(description = "Função", example = "PARLAMENTAR")
    val tipoAutor: TipoAutor? = null,
    @field:Schema(description = "Foto oficial", example = "https://link.com/foto.jpg")
    val foto: String?

)
fun Politico.toResumoDTO() = PoliticoResumoDTO(
    id = this.id!!,
    publicId = this.publicId ?: "",
    nome = this.nomeUrna ?: this.nomeCompleto,
    partido = this.partidoAtual,
    status = this.status,
    tipoAutor = this.tipoAutor,
    foto = this.urlFoto
)

data class PoliticoDetalheDTO(
    @field:Schema(description = "ID interno", example = "1")
    val id: Long,
    @field:Schema(description = "Nome Completo", example = "João Batista Silva")
    val nomeCompleto: String,
    @field:Schema(description = "Nome nas urnas", example = "João Deputado")
    val nomeUrna: String?,
    @field:Schema(description = "Status atual", example = "ATIVO")
    val status: StatusPolitico,
    @field:Schema(description = "Tipo", example = "PARLAMENTAR")
    val tipoAutor: TipoAutor? = null,
    @field:Schema(description = "Partido", example = "PDB")
    val partido: String?,
    @field:Schema(description = "Foto", example = "https://link.com/foto.jpg")
    val foto: String?,
    @field:Schema(description = "História resumida", example = "Nasceu em Brasília, atuante na área da saúde.")
    val biografia: String?,


    @field:Schema(description = "Projetos em autoria", implementation = ProposicaoResumoDTO::class)
    val proposicoes: List<ProposicaoResumoDTO>
)
fun Politico.toDetalheDTO(proposicoesMapeadas: List<ProposicaoResumoDTO>) = PoliticoDetalheDTO(
    id = this.id!!,
    nomeCompleto = this.nomeCompleto,
    nomeUrna = this.nomeUrna,
    status = this.status,
    tipoAutor = this.tipoAutor,
    partido = this.partidoAtual,
    foto = this.urlFoto,
    biografia = this.biografiaResumida,
    proposicoes = proposicoesMapeadas
)
// --- DTOs DE PROPOSIÇÃO (SAÍDA) ---

data class ProposicaoResumoDTO(
    @field:Schema(description = "ID interno da proposição no banco de dados", example = "1")
    val id: Long,

    @field:Schema(description = "ID da proposição na API da CLDF", example = "45678")
    val publicId: String,

    @field:Schema(description = "Tipo da proposição (ex: PL, PDL)", implementation = TipoProposicaoDTO::class)
    val tipo: TipoProposicaoDTO,

    @field:Schema(description = "Número da proposição e ano", example = "123/2024")
    val numero: String,

    @field:Schema(description = "Título descritivo da proposição", example = "Criação de novos parques ecológicos no DF")
    val titulo: String,

    @field:Schema(description = "Lista de temas vinculados à proposição", implementation = TemaDTO::class)
    val tema: List<TemaDTO>,

    @field:Schema(description = "Status atual de tramitação", example = "Em Análise")
    val status: String?,

    val ementa: String?,

    @field:Schema(description = "Data de apresentação da proposição", example = "2024-02-27")
    val data: LocalDate,

    val autores: List<String?> = emptyList(),

    val regioesAdministrativas: List<String> = emptyList(),

    val linkCompleto: String?
)
fun Proposicao.toResumoDTO() = ProposicaoResumoDTO(
    id = this.id!!,
    publicId = this.publicId,
    tipo = this.tipo.toDTO(),
    numero = this.numeroProcesso,
    titulo = this.titulo,
    tema = this.temas.map { it.toDTO() },
    status = this.statusTramitacao,
    ementa = this.ementa,
    data = this.dataApresentacao,
    autores = this.autores.map { it.politico.nomeUrna ?: it.politico.nomeCompleto },
    regioesAdministrativas = this.regioesAdministrativas.map { it.nome }, // Assume que RA tem o campo "nome"
    linkCompleto = this.linkCompleto
)
data class ProposicaoDetalheDTO(
    @field:Schema(description = "ID interno da proposição", example = "1")
    val id: Long,

    @field:Schema(description = "ID da proposição na API da CLDF", example = "45678")
    val publicId: String,

    @field:Schema(description = "Tipo da proposição detalhada", implementation = TipoProposicaoDTO::class)
    val tipo: TipoProposicaoDTO,

    @field:Schema(description = "Número do processo interno", example = "0001-000234/2024")
    val numeroProcesso: String,

    @field:Schema(description = "Número definitivo após virar lei (se aplicável)", example = "Lei 7345/2024")
    val numeroDefinitivo: String?,

    @field:Schema(description = "Título descritivo da proposição", example = "Criação de novos parques ecológicos no DF")
    val titulo: String,

    @field:Schema(description = "Texto da ementa detalhando o objetivo do projeto", example = "Dispõe sobre a obrigatoriedade de lixeiras ecológicas em praças públicas do DF.")
    val ementa: String?,

    @field:Schema(description = "Status atual detalhado da tramitação", example = "Aguardando Parecer na CCJ")
    val statusTramitacao: String?,

    @field:Schema(description = "Região Administrativa alvo do projeto (se for específico)", example = "PLANO_PILOTO")
    val regiaoAdministrativa: List<String>,

    @field:Schema(description = "Indica se o projeto tramita em regime de urgência", example = "false")
    val regimeUrgencia: Boolean,

    @field:Schema(description = "Data oficial da apresentação", example = "2024-02-27")
    val dataApresentacao: LocalDate,

    @field:Schema(description = "Data limite para votação/apreciação (se houver)", example = "2024-12-31")
    val dataLimite: LocalDate?,

    @field:Schema(description = "Temas relacionados", implementation = TemaDTO::class)
    val tema: List<TemaDTO>,

    @field:Schema(description = "Documentos oficiais (PDFs, pareceres) anexados ao projeto", implementation = DocumentoResponseDTO::class)
    val documentos: List<DocumentoResponseDTO>,

    @field:Schema(description = "Histórico de todas as movimentações do projeto", implementation = HistoricoDTO::class)
    val historicos: List<HistoricoDTO>,

    @field:Schema(description = "Lista de políticos que são autores ou coautores", implementation = PoliticoResumoDTO::class)
    val autores: List<PoliticoResumoDTO>,

    val linkCompleto: String?
)
fun Proposicao.toDetalheDTO(documentosMapeados: List<DocumentoResponseDTO> = emptyList()) = ProposicaoDetalheDTO(
id = this.id!!,
publicId = this.publicId,
tipo = this.tipo.toDTO(),
numeroProcesso = this.numeroProcesso,
numeroDefinitivo = this.numeroDefinitivo,
titulo = this.titulo,
ementa = this.ementa,
statusTramitacao = this.statusTramitacao,
regiaoAdministrativa = this.regioesAdministrativas.map { it.nome },
regimeUrgencia = this.regimeUrgencia,
dataApresentacao = this.dataApresentacao,
dataLimite = this.dataLimite,
tema = this.temas.map { it.toDTO() },
documentos = documentosMapeados, // <-- Mapeia todos os documentos automaticamente!
historicos = this.historico.map { it.toDTO() }, // <-- Mapeia todos os históricos automaticamente!
autores = this.autores.map { it.politico.toResumoDTO() }, // <-- Mapeia os autores automaticamente!
linkCompleto = this.linkCompleto
)
// --- DTOs DE ENTRADA ---

data class CriarPoliticoDTO(
    @field:Schema(description = "ID da API da CLDF (se houver)", example = "12345")
    val publicId:String,
    @field:Schema(description = "Nome de registro", example = "João Batista Silva")
    val nomeCompleto: String,
    @field:Schema(description = "Como aparece na urna", example = "João Deputado")
    val nomeUrna: String?,
    @field:Schema(description = "Função", example = "PARLAMENTAR")
    val tipoAutor: TipoAutor = TipoAutor.PARLAMENTAR,
    @field:Schema(description = "Sigla Partidária", example = "PDB")
    val partidoAtual: String?,
    @field:Schema(description = "URL da Imagem", example = "https://link.com/foto.jpg")
    val urlFoto: String?,

    @field:Schema(description = "Breve biografia", example = "Defensor da educação...")
    val biografiaResumida: String?,

)

data class AtualizarPoliticoDTO(
    @field:Schema(description = "Nome na urna", example = "João Deputado")
    val nomeUrna: String?,
    @field:Schema(description = "Novo partido em caso de mudança", example = "NOVO_PARTIDO")
    val partidoAtual: String?,
    @field:Schema(description = "Nova foto", example = "https://link.com/foto_nova.jpg")
    val urlFoto: String?,

    @field:Schema(description = "Atualizar Biografia", example = "Agora atuando também no Gama.")
    val biografiaResumida: String?,
    @field:Schema(description = "Atualizar Status", example = "INATIVO")
    val status: StatusPolitico?,

)

data class CriarProposicaoDTO(
    @field:Schema(description = "ID de integração com a CLDF (Opcional)", example = "45678")
    val publicId: String,

    @field:Schema(description = "Sigla do tipo de proposição", example = "PL")
    val tipoSigla: String,

    @field:Schema(description = "Número do projeto", example = "123/2024")
    val numero: String,

    @field:Schema(description = "Título da proposição", example = "Criação de parques ecológicos")
    val titulo: String,

    @field:Schema(description = "Ementa do projeto", example = "Dispõe sobre a criação de áreas verdes...")
    val ementa: String?,

    @field:Schema(description = "Status inicial de tramitação", example = "Apresentado ao Plenário")
    val statusTramitacao: String,

    @field:Schema(description = "Data de apresentação", example = "2024-02-27")
    val dataApresentacao: LocalDate,

    @field:Schema(description = "Lista de IDs dos temas", example = "[1, 4, 7]")
    val temaId: List<Long>,
val linkCompleto : String?,



)

data class NovoHistoricoDTO(
    @field:Schema(description = "Data em que a movimentação ocorreu", example = "2024-02-27")
    val dataEvento: LocalDate,

    @field:Schema(description = "Nova fase ou status do projeto", example = "Aguardando Parecer")
    val faseTramitacao: String,

    @field:Schema(description = "Qual comissão ou unidade está com o projeto agora", example = "Comissão de Constituição e Justiça (CCJ)")
    val unidadeResponsavel: String? = null,

    @field:Schema(description = "Descrição detalhada do que aconteceu nessa etapa", example = "O projeto foi encaminhado para avaliação técnica da CCJ.")
    val descricao: String,

    @field:Schema(description = "Se 'true', atualiza o status principal da proposição para refletir esta fase", example = "true")
    val atualizarStatusDaProposicao: Boolean = true
)
