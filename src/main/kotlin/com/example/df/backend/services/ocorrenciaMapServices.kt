package com.example.df.backend.services

import com.example.df.backend.config.AppConfig
import com.example.df.backend.dtos.CriarOcorrenciaDTO
import com.example.df.backend.entities.OcorrenciaMapa
import com.example.df.backend.enums.StatusOcorrencia
import com.example.df.backend.enums.TipoProblema
import com.example.df.backend.repositories.OcorrenciaMapaRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import com.example.df.backend.services.FotoService
import com.example.df.backend.dtos.OcorrenciaDetalheDTO
import com.example.df.backend.dtos.FotoResponseDTO
import java.time.LocalDateTime

@Service
class OcorrenciaService(
    private val repository: OcorrenciaMapaRepository,
    private val googleMapsService: GoogleMapsService,
    private val appConfig: AppConfig,
    private val fotoservice: FotoService
) {

    // =========================================================================
    // 1. CRIAR Ocorrência
    // =========================================================================
    @Transactional
    fun criarToken(dto: CriarOcorrenciaDTO): OcorrenciaMapa {
        val (latBigDecimal, lonBigDecimal) = resolverCoordenadas(dto)

        val lat = latBigDecimal.toDouble()
        val lon = lonBigDecimal.toDouble()
        val raio = appConfig.token.duplicata.raioMetros.toDouble()

        // Verifica duplicatas próximas usando PostGIS
        val duplicatas = repository.buscarTokensProximos(lat, lon, raio)
        if (duplicatas.isNotEmpty()) {
            val existente = duplicatas[0]
            existente.quantidadeReportes += 1
            return repository.save(existente)
        }

        val novaOcorrencia = OcorrenciaMapa(
            tipo = dto.tipo,
            nome = dto.nome,
            descricao = dto.descricao,
            categoriaProblema = dto.categoriaProblema,
            latitude = latBigDecimal,
            longitude = lonBigDecimal,
            status = StatusOcorrencia.ATIVO,
            dataCriacao = LocalDateTime.now()
        )

        return repository.save(novaOcorrencia)
    }

    // =========================================================================
    // 2. BUSCAS E LISTAGENS
    // =========================================================================
    fun listarTodas(): List<OcorrenciaDetalheDTO> {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "dataCriacao"))
            .map { converterParaDetalheDTO(it) }
    }

    fun buscarPorId(id: Long): OcorrenciaDetalheDTO {
        val entidade = repository.findById(id)
            .orElseThrow { IllegalArgumentException("Ocorrência com ID $id não encontrada.") }
        return converterParaDetalheDTO(entidade)
    }

    // =========================================================================
    // 3. ATUALIZAÇÕES E EXCLUSÃO
    // =========================================================================
    @Transactional
    fun atualizarStatus(id: Long, novoStatus: StatusOcorrencia): OcorrenciaMapa {
        val ocorrencia = repository.findById(id)
            .orElseThrow { IllegalArgumentException("Ocorrência não encontrada.") }
        ocorrencia.status = novoStatus
        return repository.save(ocorrencia)
    }

    @Transactional
    fun deletarToken(id: Long) {
        // 1. Busca a entidade completa com as fotos
        val ocorrencia = repository.findById(id)
            .orElseThrow { IllegalArgumentException("Ocorrência não encontrada.") }

        // 2. Loop para apagar cada foto associada (Disco + Banco)
        // Chamamos o fotoService para cada publicId da lista de fotos
        ocorrencia.fotos.forEach { foto ->
            try {
                fotoservice.deletarArquivoFisico(foto.publicId)
            } catch (e: Exception) {
                // Logamos o erro mas continuamos para não travar a exclusão da ocorrência
                println("Aviso: Falha ao remover foto física ${foto.publicId}: ${e.message}")
            }
        }

        // 3. Agora que as fotos (filhos) foram removidas, apagamos a ocorrência (pai)
        repository.delete(ocorrencia)
    }
    fun listarComFiltros(
        categoria: TipoProblema?,
        status: StatusOcorrencia?,
        minLat: Double?,
        minLon: Double?,
        maxLat: Double?,
        maxLon: Double?
    ): List<OcorrenciaDetalheDTO> {
        // Chama o repositório (convertendo enums para String se necessário)
        val entidades = repository.buscarComFiltros(
            categoria?.name,
            status?.name,
            minLat, minLon, maxLat, maxLon
        )

        // Converte a lista de entidades para a lista de DTOs (usando o conversor que já criamos)
        return entidades.map { converterParaDetalheDTO(it) }
    }

    // =========================================================================
    // 4. CONVERSORES (Mapeamento de URL de Fotos)
    // =========================================================================
    private fun converterParaDetalheDTO(entidade: OcorrenciaMapa): OcorrenciaDetalheDTO {
        val fotosDto = entidade.fotos.map { foto ->
            FotoResponseDTO(
                id = foto.id ?: 0,
                publicId = foto.publicId, // CAMPO QUE ESTAVA FALTANDO
                url = "https://vigiadf.pmhub.cloud/api/foto/v/${foto.publicId}", // Rota simplificada
                nomeOriginal = foto.nomeOriginal
            )
        }

        return OcorrenciaDetalheDTO(
            id = entidade.id ?: 0,
            tipo = entidade.tipo,
            status = entidade.status,
            nome = entidade.nome ?: "Sem Nome",
            descricao = entidade.descricao,
            categoriaProblema = entidade.categoriaProblema,
            latitude = entidade.latitude,
            longitude = entidade.longitude,
            quantidadeReportes = entidade.quantidadeReportes,
            dataCriacao = entidade.dataCriacao,
            fotos = fotosDto
        )
    }

    private fun resolverCoordenadas(dto: CriarOcorrenciaDTO): Pair<BigDecimal, BigDecimal> {
        if (dto.latitude != null && dto.longitude != null) {
            return Pair(dto.latitude, dto.longitude)
        }

        if (!dto.endereco.isNullOrBlank()) {
            val coords = googleMapsService.geocodificarEndereco(dto.endereco)
            if (coords != null) return coords
            throw IllegalArgumentException("Endereço não encontrado pelo Google Maps.")
        }

        throw IllegalArgumentException("É necessário informar Latitude/Longitude OU um Endereço válido.")
    }
}