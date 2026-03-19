package com.example.df.backend.services

import com.example.df.backend.dtos.*
import com.example.df.backend.entities.Politico
import com.example.df.backend.enums.StatusPolitico
import com.example.df.backend.repositories.AutoriaRepository
import com.example.df.backend.repositories.PoliticoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Suppress("unused")
class PoliticoService(
    private val politicoRepo: PoliticoRepository,
    private val autoriaRepo: AutoriaRepository // Injetado para buscar as leis do político
) {

    private val logger = LoggerFactory.getLogger(PoliticoService::class.java)

    // =========================================================================
    // 1. APOIO À SINCRONIZAÇÃO
    // =========================================================================

    @Transactional(readOnly = true)
    fun buscarAutorParaSincronizacao(publicId: String?, nomeAutor: String?): Politico? {
        if (!publicId.isNullOrBlank()) {
            val politicoPorId = politicoRepo.findByPublicId(publicId)
            if (politicoPorId != null) return politicoPorId
        }

        if (!nomeAutor.isNullOrBlank()) {
            val existentes = politicoRepo.findByNomeUrnaContainingIgnoreCase(nomeAutor.trim())
            if (existentes.isNotEmpty()) {
                return existentes.first()
            }
        }

        logger.warn(
            "⚠️ ALERTA DE SINCRONIZAÇÃO: Autor não encontrado no banco -> [ID CLDF: ${publicId ?: "N/A"} | Nome: ${nomeAutor ?: "N/A"}]. " +
                    "A proposição será salva sem autoria. Por favor, cadastre este autor manualmente."
        )
        return null
    }

    // =========================================================================
    // 2. CRUD MANUAL
    // =========================================================================

    @Transactional(readOnly = true)
    fun listarTodos(): List<PoliticoResumoDTO> {
        return politicoRepo.findAll().map { p ->
            PoliticoResumoDTO(
                id = p.id!!,
                publicId = p.publicId,
                nome = p.nomeUrna ?: p.nomeCompleto,
                partido = p.partidoAtual,
                status = p.status,
                foto = p.urlFoto,
            )
        }
    }

    // CORREÇÃO: Agora retorna o DTO detalhado e busca as proposições via AutoriaRepository
    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): PoliticoDetalheDTO {
        val p = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Autor/Político não encontrado com o ID interno: $id") }

        // Busca as relações de autoria onde este político está envolvido
        val autorias = autoriaRepo.findByPolitico(p)

        // Converte as proposições do político para o DTO de resumo
        val proposicoesDTO = autorias.map { autoria ->
            val prop = autoria.proposicao
            ProposicaoResumoDTO(
                id = prop.id!!,
                publicId = prop.publicId,
                titulo = prop.titulo,
                ementa = prop.ementa,
                status = prop.statusTramitacao ?: "Aguardando",
                data = prop.dataApresentacao,
                numero = prop.numeroProcesso,
                tipo = TipoProposicaoDTO(prop.tipo.name, prop.tipo.name),
                linkCompleto = prop.linkCompleto,
                tema = prop.temas.map { TemaDTO(it.id, it.nome) }
            )
        }

        return PoliticoDetalheDTO(
            id = p.id!!,
            nomeCompleto = p.nomeCompleto,
            nomeUrna = p.nomeUrna,
            status = p.status,
            tipoAutor = p.tipoAutor,
            partido = p.partidoAtual,
            foto = p.urlFoto,
            biografia = p.biografiaResumida,
            proposicoes = proposicoesDTO
        )
    }

    @Transactional
    fun criarPolitico(dto: CriarPoliticoDTO): Politico {
        if (politicoRepo.findByPublicId(dto.publicId) != null) {
            throw IllegalArgumentException("Já existe um autor cadastrado com o ID da CLDF: ${dto.publicId}")
        }

        val novoPolitico = Politico(
            publicId = dto.publicId,
            nomeCompleto = dto.nomeCompleto,
            nomeUrna = dto.nomeUrna,
            tipoAutor = dto.tipoAutor,
            partidoAtual = dto.partidoAtual,
            urlFoto = dto.urlFoto,
            biografiaResumida = dto.biografiaResumida,
            status = StatusPolitico.ATIVO
        )
        return politicoRepo.save(novoPolitico)
    }

    @Transactional
    fun atualizarPolitico(id: Long, dto: AtualizarPoliticoDTO): Politico {
        // CORREÇÃO: Busca a Entidade direto do repositório para evitar conflito com o DTO do buscarPorId
        val politico = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Autor/Político não encontrado com o ID interno: $id") }

        dto.nomeUrna?.let { politico.nomeUrna = it }
        dto.partidoAtual?.let { politico.partidoAtual = it }
        dto.urlFoto?.let { politico.urlFoto = it }
        dto.biografiaResumida?.let { politico.biografiaResumida = it }
        dto.status?.let { politico.status = it }

        return politicoRepo.save(politico)
    }

    @Transactional
    fun alterarStatus(id: Long, novoStatus: StatusPolitico): Politico {
        // CORREÇÃO: Busca a Entidade direto do repositório
        val politico = politicoRepo.findById(id)
            .orElseThrow { IllegalArgumentException("Autor/Político não encontrado com o ID interno: $id") }

        politico.status = novoStatus
        return politicoRepo.save(politico)
    }
}