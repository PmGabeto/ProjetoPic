package com.example.df.backend.integrations.cldf

import com.example.df.backend.entities.Tema

interface CldfInterface {
    fun sincronizarTemasCldf()
    fun processarTemasProposicao(idsRaw: String?, nomesRaw: String?): List<Tema>
    fun garantirTemaIndividual(id: Long, nome: String): Tema

    fun varrerProposicoesRecentes(ano: Int, pagina: Int, tamanho: Int): List<ProposicaoCldfBaseDTO>

    fun buscarDetalhesCompletos(publicId: String): ProposicaoCldfCompletaDTO?
    fun buscarHistorico(publicId: String): List<HistoricoCldfDTO>
    fun buscarDocumentos(publicId: String): List<DocumentoCldfDTO>
}