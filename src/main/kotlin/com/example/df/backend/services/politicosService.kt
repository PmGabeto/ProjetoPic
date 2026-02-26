package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.*
import com.example.df.backend.enums.StatusPolitico
import com.example.df.backend.repositories.AutoriaRepository
import com.example.df.backend.repositories.PoliticoRepository
import com.example.df.backend.repositories.ProposicaoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PoliticoService(
    private val politicoRepo: PoliticoRepository,
    private val proposicaoRepo: ProposicaoRepository,
    private val autoriaRepo: AutoriaRepository
) {

    // 1. LISTAR TODOS (Resumido para a tela de listagem)
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

    // 2. BUSCAR DETALHE (Com as leis vinculadas)
    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): PoliticoDetalheDTO {
        val politico = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Político não encontrado: ID $id") }

        // Mapeia as proposições através da tabela Autoria
        val listaProposicoes = politico.proposicoes.map { autoria ->
            val prop = autoria.proposicao
            ProposicaoResumoDTO(
                id = prop.id!!,
                tipo = prop.tipo,
                numero = prop.numero,
                titulo = prop.titulo,
                area = prop.areaTematica,
                status = prop.statusTramitacao,
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
            proposicoes = listaProposicoes
        )
    }

    // 3. CRIAR POLÍTICO
    @Transactional
    fun criarPolitico(dto: CriarPoliticoDTO): Politico {
        val entity = Politico(
            nomeCompleto = dto.nomeCompleto,
            nomeUrna = dto.nomeUrna,
            partidoAtual = dto.partidoAtual,
            urlFoto = dto.urlFoto,
            raBaseEleitoral = dto.raBaseEleitoral,
            biografiaResumida = dto.biografiaResumida
        )
        return politicoRepo.save(entity)
    }

    // 4. ADICIONAR PROPOSIÇÃO A UM POLÍTICO
    @Transactional
    fun adicionarProposicao(idPolitico: Long, dto: CriarProposicaoDTO): Proposicao {
        val politico = politicoRepo.findById(idPolitico)
            .orElseThrow { IllegalArgumentException("Político não encontrado") }

        // A. Salva a Proposição primeiro
        val novaProposicao = Proposicao(
            tipo = dto.tipo,
            numero = dto.numero,
            titulo = dto.titulo,
            ementa = dto.ementa,
            statusTramitacao = dto.statusTramitacao,
            dataApresentacao = dto.dataApresentacao,
            areaTematica = dto.areaTematica,
            linkCompleto = dto.linkCompleto
        )
        val propSalva = proposicaoRepo.save(novaProposicao)

        // B. Cria o vínculo na tabela de ligação (Autoria)
        val autoriaId = AutoriaId(politico.id, propSalva.id)

        val novaAutoria = Autoria(
            id = autoriaId,
            politico = politico,
            proposicao = propSalva,
            tipoVinculacao = dto.tipoVinculacao
        )
        autoriaRepo.save(novaAutoria)

        return propSalva
    }
    // 5. ATUALIZAR DADOS DO POLÍTICO (NOVO)
    @Transactional
    fun atualizarPolitico(id: Long, dto: AtualizarPoliticoDTO): Politico {
        val politico = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Político não encontrado") }

        // Atualiza apenas se o campo não for nulo (para não apagar dados sem querer)
        dto.nomeUrna?.let { politico.nomeUrna = it }
        dto.partidoAtual?.let { politico.partidoAtual = it }
        dto.urlFoto?.let { politico.urlFoto = it }
        dto.raBaseEleitoral?.let { politico.raBaseEleitoral = it }
        dto.biografiaResumida?.let { politico.biografiaResumida = it }
        dto.entidadesVinculadas?.let { politico.entidadesVinculadas = it }

        return politicoRepo.save(politico)
    }

    // 6. ALTERAR STATUS (ATIVO / INATIVO) (NOVO)
    @Transactional
    fun alterarStatus(id: Long, novoStatus: StatusPolitico): Politico {
        val politico = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Político não encontrado") }

        politico.status = novoStatus
        return politicoRepo.save(politico)
    }
}