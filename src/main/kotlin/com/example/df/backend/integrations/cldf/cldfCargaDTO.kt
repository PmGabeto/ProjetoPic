package com.example.df.backend.integrations.cldf

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTOs de Carga: Espelhos fiéis da API da CLDF.
 * Adaptados com valores padrão para evitar nulos e NullPointerExceptions no banco.
 */

//! capa - encapsulamento das respostas do JSON da CLDF
//* response geral
data class CldfPageResponse<T>(
    val content: List<T> = emptyList()
)

//* proposicao/detalhes
data class CldfDetalheResponse(
    val proposicao: ProposicaoCldfCompletaDTO?,
    val historico: List<HistoricoCldfDTO> = emptyList()
)
//! DTOS auxiliares que são de listas .
// * AUTOR (Objeto interno da Proposição)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AutorCldfDTO(
    @field:JsonProperty("id") val id: Int? =null,
    @field:JsonProperty("nome") val nome: String? =null,
    @field:JsonProperty("tipoAutor") val tipoAutor: String ? =null,
    @field:JsonProperty(value = "situacao")
    val situacao: String? = null
)

// 6. TIPO DE PROPOSIÇÃO (Objeto interno da Proposição)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TipoProposicaoCldfDTO(
    @field:JsonProperty("id") val id: Int? = null ,
    @field:JsonProperty("nome") val nome: String ?=null,
    @field:JsonProperty("sigla") val sigla: String ?= null
)

// 7. HISTÓRICO DA PROPOSIÇÃO
@JsonIgnoreProperties(ignoreUnknown = true)
data class HistoricoCldfDTO( // precisa no dto ter o id da proposição para vincular ou pode ser dinamico do sistema ?
    @field:JsonProperty("id") val publicId: String? = null,
    @field:JsonProperty("idProposicao") val idProposicaoVinculada: String? = null,
    @field:JsonProperty("dataHistorico") val dataHistorico: LocalDateTime? = null ,
    @field:JsonProperty("acao") val acao: String?,
    @field:JsonProperty("nomeUnidade") val nomeUnidade: String? = null,
    @field:JsonProperty("sigla") val sigla: String? = null,
    @field:JsonProperty("descricao") val descricao: String? = null
)

//! DTOs de informações para pegr da requisição cldf para banco
// 2. DTO BASE (Usado na primeira listagem da varredura)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProposicaoCldfBaseDTO(
    @field:JsonProperty("id") val publicId: String? = null,
    @field:JsonProperty("siglaNumeroAno") val siglaNumeroAno: String? = null,
    @field:JsonProperty("tipoProposicao") val nomeProposicao: String? = null,
    @field:JsonProperty("ementa") val ementa: String? = null,
    @field:JsonProperty("dataLeitura") val dataCadastro: LocalDate? = null,
    @field:JsonProperty("etapa") val etapa: String? = null,

    @field:JsonProperty("temaId") val temaId: String? = null,
    @field:JsonProperty("temaNome") val temaNome: String? = null,
    @field:JsonProperty("autoria") val autoria: String? = null,
    @field:JsonProperty("regiaoAdiminstrativaNome") val regiaoAdiminstrativaNome: String? = null,

    ){
    // Funções dinâmicas para limpar as strings de lista que vêm com "#"
    val autoriaLista: List<String>
        get() = autoria?.split(",")?.map { it.trim() } ?: emptyList()

    val regiaoAdiminstrativaNomeLista: List<String>
        get() = regiaoAdiminstrativaNome?.split("#")?.filter { it.isNotBlank() } ?: emptyList()

    val temaIdLista: List<String>
        get() = temaId?.split("#")?.filter { it.isNotBlank() } ?: emptyList()

    val temaNomeLista: List<String>
        get() = temaNome?.split("#")?.filter { it.isNotBlank() } ?: emptyList()
}

// 4. PROPOSIÇÃO COMPLETA (A lupa com os detalhes)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProposicaoCldfCompletaDTO(
    @field:JsonProperty("id") val publicId: String? = null ,
    @field:JsonProperty("siglaNumero") val siglaNumero: String? = null, // Título formatado: "IND 9910/2026"
    @field:JsonProperty("tipoProposicao") val tipoProposicao: TipoProposicaoCldfDTO? = TipoProposicaoCldfDTO(),
    @field:JsonProperty("dataCadastro") val dataCadastro: String?, // a mesma data de dataLeitura do outro dto
    @field:JsonProperty("statusAtual") val statusTramitacao: String?,
    @field:JsonProperty("status") val statusLista: List<String> ?= emptyList(), // Às vezes a API manda os status aqui
    @field:JsonProperty("regiaoAdministrativa") val regiaoAdministrativa: List<Int>? = emptyList(),
    @field:JsonProperty("regimeUrgencia") val regimeUrgencia: Boolean? = null,
    @field:JsonProperty("tema") val temasIds: List<Int>? = emptyList(), // Traz o ID do Tema em Array: [25]
    @field:JsonProperty("autores") val autores: List<AutorCldfDTO>? = null,
    @field:JsonProperty("excluido") val excluido : Boolean? = null ,
    @field:JsonProperty("tipoAutor") val tipoAutor: String? = null,
    @field:JsonProperty("numeroDefinitivo") val numeroDefinitivo: String? = null,
    @field:JsonProperty("idUnidadeGeradora") val idUnidadeGeradora: Long? = null, // antes de ativar preciso saber de onde ver esse id não são todos que aparecem no autor/listar
    @field:JsonProperty("historico") val historico : List<HistoricoCldfDTO>? =emptyList()

)




// 8. DOCUMENTO ATIVO (Lista do endpoint de documentos)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DocumentoCldfDTO(
    @field:JsonProperty("id") val idArquivo: String? = null,
    @field:JsonProperty("nomeTipoDocumento") val nome: String? = null,
    @field:JsonProperty("numeroDefinitivo") val numero: String? = null,
    @field:JsonProperty("dataDocumento") val dataDocumento: LocalDateTime, // Data precisa com hora -"dataDocumento": "2021-03-12T11:23:58",

    @field:JsonProperty("autoria") val autoria: String? = null, // Quem gerou o doc - pode ser visto como ula lista de nomes?
    @field:JsonProperty("siglaUnidadeCriacao") val siglaUnidadeCriacao: String? = null,// Gabinete
    @field:JsonProperty("validoDesde") val validoDesde: LocalDateTime? = null
){
    val autoriaLista: List<String>
        get() = autoria?.split(",")?.map { it.trim() } ?: emptyList()

}

/*
O que estou percenboo do que fizemos do dto , existem algumas informaçẽos  que podem ser encontradas de outras forma , não preciso pegar informaçẽos repetidas em respostas diferentes, n preciso trabalahr com a lista de tipoDocumento só quero o nome e sigla, n preciso trabalhar com todos os dados dos autores só o nome e no máximo id para melhorar a pesquisa e vinculação,
os documentos só salvam as informações - detalhes em texto mas não trabalha com o arquivo em si ,
se eu quero o arquivo de alguma proposição para download eu preciso de um POST:
https://ple.cl.df.gov.br/pleservico/api/public/proposicao/exportar/{idproposicao}/pdf
body : [id documento a ser baixado]
mas se caso eu quiser trabalhar com a informação direta do aplicativo a gente tem o formato html queentrega já as div mas precisa de organizar o css.
GET : https://ple.cl.df.gov.br/pleservico/api/public/proposicao/{idProposicao}/documento/{idDocumento}/html
nõa preciso de body !
por fim essas informações de arquivo de documento não necessáriamente precisa estar no banco com o id e uma função dinaica para api do front deve resolver 99% dos casos e economizar espaço para o que realmente precisa
*/