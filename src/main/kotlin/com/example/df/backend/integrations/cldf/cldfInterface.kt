package com.example.df.backend.integrations.cldf

import com.example.df.backend.entities.Tema

interface CldfInterface {
    fun sincronizarTemasCldf()
    fun processarTemasProposicao(temasIds: List<Int>): List<Tema>    fun garantirTemaIndividual(id: Long, nome: String): Tema

    fun varrerProposicoesRecentes(filtros: Map<String, Any>, pagina: Int, tamanho: Int): List<ProposicaoCldfBaseDTO>
    fun buscarDetalhesCompletos(publicId: String): CldfDetalheResponse?
    fun buscarDocumentos(publicId: String): List<DocumentoCldfDTO>
    fun baixarDocumentos(idProposicao: String, idDocumento: String): ByteArray?
    fun buscarHtmlDocumento(idProposicao: String, idDocumento: String): String?
}