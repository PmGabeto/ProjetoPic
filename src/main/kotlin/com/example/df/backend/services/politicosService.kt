package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.*
import com.example.df.backend.enums.StatusPolitico
import com.example.df.backend.enums.TipoProjetoLei
import com.example.df.backend.repositories.AutoriaRepository
import com.example.df.backend.repositories.PoliticoRepository
import com.example.df.backend.repositories.ProposicaoRepository
import com.example.df.backend.repositories.TemaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PoliticoService(
    private val politicoRepo: PoliticoRepository,
    private val proposicaoRepo: ProposicaoRepository,
    private val autoriaRepo: AutoriaRepository,
    private val temaRepo: TemaRepository
) {

    /**
     * 1. LISTAR TODOS
     * Retorna uma lista simplificada para a visualização principal.
     */
    @Transactional(readOnly = true)
    fun listarTodos(): List<PoliticoResumoDTO> {
        return politicoRepo.findAll().map { p ->
            PoliticoResumoDTO(
                id = p.id!!,
                nome = p.nomeUrna ?: p.nomeCompleto,
                partido = p.partidoAtual,
                status = p.status,
                foto = p.urlFoto
            )
        }
    }

    /**
     * 2. BUSCAR POR ID (DETALHE)
     * Retorna o perfil completo do político incluindo sua lista de proposições/leis.
     */
    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): PoliticoDetalheDTO {
        val politico = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Político não encontrado: ID $id") }

        val listaProposicoes = politico.proposicoes.map { autoria ->
            val prop = autoria.proposicao
            ProposicaoResumoDTO(
                id = prop.id!!,
                idExterno = prop.idExterno,
                tipo = TipoProposicaoDTO(prop.tipo.sigla, prop.tipo.nome, prop.tipo.descricaoPedagogica),
                numero = prop.numeroProcesso,
                titulo = prop.titulo,
                // Mapeia a lista de temas da proposição
                tema = prop.temas.map { TemaDTO(it.id, it.nome) },
                status = prop.statusTramitacao ?: "Em tramitação",
                data = prop.dataApresentacao,
                minhaVinculacao = autoria.tipoVinculacao
            )
        }

        return PoliticoDetalheDTO(
            id = politico.id!!,
            nomeCompleto = politico.nomeCompleto,
            nomeUrna = politico.nomeUrna,
            status = politico.status,
            partido = politico.partidoAtual,
            foto = politico.urlFoto,
            biografia = politico.biografiaResumida,
            baseEleitoral = politico.raBaseEleitoral,
            entidadesVinculadas = politico.entidadesVinculadas,
            proposicoes = listaProposicoes
        )
    }

    /**
     * 3. CRIAR POLÍTICO
     * Persiste um novo político no banco de dados.
     */
    @Transactional
    fun criarPolitico(dto: CriarPoliticoDTO): Politico {
        val novoPolitico = Politico(
            idExterno = dto.idExterno,
            nomeCompleto = dto.nomeCompleto,
            nomeUrna = dto.nomeUrna,
            partidoAtual = dto.partidoAtual,
            urlFoto = dto.urlFoto,
            raBaseEleitoral = dto.raBaseEleitoral,
            biografiaResumida = dto.biografiaResumida,
            status = StatusPolitico.ATIVO // Define como ativo por padrão na criação
        )
        return politicoRepo.save(novoPolitico)
    }

    /**
     * 4. ADICIONAR PROPOSIÇÃO
     * Cria uma nova lei e estabelece o vínculo de autoria com o político.
     */
    @Transactional
    fun adicionarProposicao(idPolitico: Long, dto: CriarProposicaoDTO): Proposicao {
        val politico = politicoRepo.findById(idPolitico)
            .orElseThrow { IllegalArgumentException("Político não encontrado") }

        val temasEncontrados = temaRepo.findAllById(dto.temaId)

        val novaProposicao = Proposicao(
            idExterno = dto.idExterno ?: 0L,
            tipo = TipoProjetoLei.fromSigla(dto.tipoSigla) ?: TipoProjetoLei.PL,
            numeroProcesso = dto.numero,
            titulo = dto.titulo,
            ementa = dto.ementa,
            statusTramitacao = dto.statusTramitacao,
            dataApresentacao = dto.dataApresentacao,
            linkCompleto = dto.linkCompleto
        )

        novaProposicao.temas.addAll(temasEncontrados)
        val propSalva = proposicaoRepo.save(novaProposicao)

        // Cria a chave composta e a entidade de ligação Autoria
        val autoriaId = AutoriaId(politico.id!!, propSalva.id!!)
        val novaAutoria = Autoria(
            id = autoriaId,
            politico = politico,
            proposicao = propSalva,
            tipoVinculacao = dto.tipoVinculacao
        )
        autoriaRepo.save(novaAutoria)

        return propSalva
    }

    /**
     * 5. ATUALIZAR POLÍTICO
     * Atualiza campos específicos de forma segura (null-safe).
     */
    @Transactional
    fun atualizarPolitico(id: Long, dto: AtualizarPoliticoDTO): Politico {
        val politico = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Político não encontrado") }

        dto.nomeUrna?.let { politico.nomeUrna = it }
        dto.partidoAtual?.let { politico.partidoAtual = it }
        dto.urlFoto?.let { politico.urlFoto = it }
        dto.raBaseEleitoral?.let { politico.raBaseEleitoral = it }
        dto.biografiaResumida?.let { politico.biografiaResumida = it }
        dto.entidadesVinculadas?.let { politico.entidadesVinculadas = it }
        dto.status?.let { politico.status = it }

        return politicoRepo.save(politico)
    }

    /**
     * 6. ALTERAR STATUS
     * Permite ativar ou inativar o político rapidamente.
     */
    @Transactional
    fun alterarStatus(id: Long, novoStatus: StatusPolitico): Politico {
        val politico = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Político não encontrado") }

        politico.status = novoStatus
        return politicoRepo.save(politico)
    }
}