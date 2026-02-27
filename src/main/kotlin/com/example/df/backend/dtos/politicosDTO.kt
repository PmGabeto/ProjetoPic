package com.example.df.backend.dtos

import com.example.df.backend.enums.*
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

// --- DTOs AUXILIARES ---

data class TemaDTO(
    @field:Schema(description = "ID do Tema", example = "10")
    val id: Long,
    @field:Schema(description = "Nome do Tema", example = "Educação")
    val nome: String
)

data class TipoProposicaoDTO(
    @field:Schema(description = "Sigla do projeto", example = "PL")
    val sigla: String,
    @field:Schema(description = "Nome completo do tipo", example = "Projeto de Lei")
    val nome: String,
    @field:Schema(description = "Explicação fácil para o cidadão", example = "Criação de novas leis para o DF.")
    val descricaoPedagogica: String
)

data class DocumentoDTO(
    @field:Schema(description = "ID do documento na CLDF", example = "9876")
    val idExterno: Long,
    @field:Schema(description = "Tipo do Documento", example = "Parecer")
    val tipo: String,
    @field:Schema(description = "Data de emissão", example = "2024-02-10")
    val data: LocalDate?,
    @field:Schema(description = "Link do PDF", example = "https://cldf.gov.br/doc.pdf")
    val link: String?,
    @field:Schema(description = "Autor do documento", example = "Comissão de Justiça")
    val autor: String?
)

data class HistoricoDTO(
    @field:Schema(description = "Data do evento", example = "2024-02-15")
    val data: LocalDate,
    @field:Schema(description = "Fase atual da tramitação", example = "Aguardando Votação")
    val fase: String,
    @field:Schema(description = "Unidade que está avaliando", example = "Plenário")
    val unidadeResponsavel: String?,
    @field:Schema(description = "Descrição da movimentação", example = "Encaminhado para votação.")
    val descricao: String?
)

// --- DTOs DE POLÍTICO ---

data class PoliticoResumoDTO(
    @field:Schema(description = "ID interno", example = "1")
    val id: Long,
    @field:Schema(description = "ID na CLDF", example = "12345")
    val idExterno: Long? = null,
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
    @field:Schema(description = "Região principal de atuação", example = "CEILANDIA")
    val baseEleitoral: String?,
    @field:Schema(description = "Grupos ou sindicatos apoiados", example = "Sindicato dos Médicos")
    val entidadesVinculadas: String? = null,
    @field:Schema(description = "Projetos em autoria", implementation = ProposicaoResumoDTO::class)
    val proposicoes: List<ProposicaoResumoDTO>
)

// --- DTOs DE PROPOSIÇÃO (SAÍDA) ---

data class ProposicaoResumoDTO(
    @field:Schema(description = "ID interno da proposição no banco de dados", example = "1")
    val id: Long,

    @field:Schema(description = "ID da proposição na API da CLDF", example = "45678")
    val idExterno: Long? = null,

    @field:Schema(description = "Tipo da proposição (ex: PL, PDL)", implementation = TipoProposicaoDTO::class)
    val tipo: TipoProposicaoDTO,

    @field:Schema(description = "Número da proposição e ano", example = "123/2024")
    val numero: String,

    @field:Schema(description = "Título descritivo da proposição", example = "Criação de novos parques ecológicos no DF")
    val titulo: String,

    @field:Schema(description = "Lista de temas vinculados à proposição", implementation = TemaDTO::class)
    val tema: List<TemaDTO>,

    @field:Schema(description = "Status atual de tramitação", example = "Em Análise")
    val status: String,

    @field:Schema(description = "Data de apresentação da proposição", example = "2024-02-27")
    val data: LocalDate,

    @field:Schema(description = "Tipo de vinculação do autor (se for autor principal ou coautor)", example = "AUTOR_PRINCIPAL")
    val minhaVinculacao: TipoVinculacao?
)
data class ProposicaoDetalheDTO(
    @field:Schema(description = "ID interno da proposição", example = "1")
    val id: Long,

    @field:Schema(description = "ID da proposição na API da CLDF", example = "45678")
    val idExterno: Long,

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
    val regiaoAdministrativa: String?,

    @field:Schema(description = "Indica se o projeto tramita em regime de urgência", example = "false")
    val regimeUrgencia: Boolean,

    @field:Schema(description = "Data oficial da apresentação", example = "2024-02-27")
    val dataApresentacao: LocalDate,

    @field:Schema(description = "Data limite para votação/apreciação (se houver)", example = "2024-12-31")
    val dataLimite: LocalDate?,

    @field:Schema(description = "Temas relacionados", implementation = TemaDTO::class)
    val tema: List<TemaDTO>,

    @field:Schema(description = "Documentos oficiais (PDFs, pareceres) anexados ao projeto", implementation = DocumentoDTO::class)
    val documentos: List<DocumentoDTO>,

    @field:Schema(description = "Histórico de todas as movimentações do projeto", implementation = HistoricoDTO::class)
    val historicos: List<HistoricoDTO>,

    @field:Schema(description = "Lista de políticos que são autores ou coautores", implementation = PoliticoResumoDTO::class)
    val autores: List<PoliticoResumoDTO>
)

// --- DTOs DE ENTRADA ---

data class CriarPoliticoDTO(
    @field:Schema(description = "ID da API da CLDF (se houver)", example = "12345")
    val idExterno: Long? = null,
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
    @field:Schema(description = "RA Base", example = "CEILANDIA")
    val raBaseEleitoral: String?,
    @field:Schema(description = "Breve biografia", example = "Defensor da educação...")
    val biografiaResumida: String?,
    @field:Schema(description = "Entidades de apoio", example = "APAE")
    val entidadesVinculadas: String? = null
)

data class AtualizarPoliticoDTO(
    @field:Schema(description = "Nome na urna", example = "João Deputado")
    val nomeUrna: String?,
    @field:Schema(description = "Novo partido em caso de mudança", example = "NOVO_PARTIDO")
    val partidoAtual: String?,
    @field:Schema(description = "Nova foto", example = "https://link.com/foto_nova.jpg")
    val urlFoto: String?,
    @field:Schema(description = "Nova base de atuação", example = "PLANO_PILOTO")
    val raBaseEleitoral: String?,
    @field:Schema(description = "Atualizar Biografia", example = "Agora atuando também no Gama.")
    val biografiaResumida: String?,
    @field:Schema(description = "Atualizar Status", example = "INATIVO")
    val status: StatusPolitico?,
    @field:Schema(description = "Atualizar entidades", example = "APAE, Sindicato XYZ")
    val entidadesVinculadas: String?
)

data class CriarProposicaoDTO(
    @field:Schema(description = "ID de integração com a CLDF (Opcional)", example = "45678")
    val idExterno: Long? = null,

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

    @field:Schema(description = "Link para o PDF da proposição", example = "https://cldf.gov.br/doc.pdf")
    val linkCompleto: String?,

    @field:Schema(description = "Tipo de autoria do político ao cadastrar", example = "AUTOR_PRINCIPAL")
    val tipoVinculacao: TipoVinculacao
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