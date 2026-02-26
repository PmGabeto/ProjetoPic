package com.example.df.backend.services

import com.example.df.backend.config.AppConfig
import com.example.df.backend.dtos.CriarOcorrenciaDTO
import com.example.df.backend.entities.OcorrenciaMapa
import com.example.df.backend.enums.StatusOcorrencia
import com.example.df.backend.enums.TipoProblema
import com.example.df.backend.repositories.OcorrenciaMapaRepository // Ajuste se a pasta for minuscula
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OcorrenciaService(
    private val repository: OcorrenciaMapaRepository,
    private val googleMapsService: GoogleMapsService,
    private val appConfig: AppConfig
) {

    // =========================================================================
    // 1. CRIAR ocorrência (Com Validação PostGIS)
    // =========================================================================
    @Transactional
    fun criarToken(dto: CriarOcorrenciaDTO): OcorrenciaMapa {
        // A. Resolver Coordenadas
        val (latBigDecimal, lonBigDecimal) = resolverCoordenadas(dto)

        // B. Conversão para Double (Necessário apenas para a query do PostGIS)
        val lat = latBigDecimal.toDouble()
        val lon = lonBigDecimal.toDouble()
        val raio = appConfig.token.duplicata.raioMetros.toDouble()

        // C. Busca Duplicidade (Usando a Query Nativa com PostGIS)
        // Atenção: Os nomes dos parâmetros aqui (lat, lon) devem bater com os da Interface Repository
        val duplicatas = repository.encontrarDuplicidades(
            latitude = lat,
            longitude = lon,
            tipo = dto.tipo.name,
            categoria = dto.categoriaProblema?.name,
            raioMetros = raio
        )

        // D. Se a lista não estiver vazia, bloqueia
        if (duplicatas.isNotEmpty()) {
            val tokenExistente = duplicatas[0]

            tokenExistente.quantidadeReportes +=1
            return repository.save(tokenExistente)
        }

        // E. Montar a Entidade para Salvar
        // Aqui usamos o BigDecimal original para garantir precisão máxima no armazenamento
        val novoToken = OcorrenciaMapa(
            tipo = dto.tipo,
            nome = dto.nome,
            descricao = dto.descricao,
            latitude = latBigDecimal,
            longitude = lonBigDecimal,
            categoriaProblema = dto.categoriaProblema,
            status = StatusOcorrencia.ATIVO,
            quantidadeReportes = 1
        )

        // F. Salvar no Banco
        return repository.save(novoToken)
    }
    // =========================================================================
    // 1.1 atualizar status do token
    // ===========================================
    @Transactional
    fun atualizarStatus(id: Long, novoStatus: StatusOcorrencia): OcorrenciaMapa {
        // 1. Busca o token (se não achar, lança erro)
        val token = buscarPorId(id)

        // 2. Regra de Negócio (Opcional): Impedir reabertura de chamados
        // if (token.status == StatusOcorrencia.RESOLVIDO && novoStatus == StatusOcorrencia.ATIVO) {
        //     throw IllegalArgumentException("Não é possível reabrir um chamado resolvido.")
        // }

        // 3. Atualiza na memória
        token.status = novoStatus

        // 4. Salva no banco
        return repository.save(token)
    }
    // =========================================================================
    // 2. FUNÇÕES DE CRUD (Listar, Buscar, Deletar)
    // =========================================================================

    fun listarTokens(): List<OcorrenciaMapa> {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "dataCriacao"))
    }

    fun buscarPorId(id: Long): OcorrenciaMapa {
        return repository.findById(id)
            .orElseThrow { IllegalArgumentException("Token não encontrado com ID: $id") }
    }

    @Transactional
    fun deletarToken(id: Long) {
        if (!repository.existsById(id)) {
            throw IllegalArgumentException("Token não encontrado para deletar.")
        }
        repository.deleteById(id)
    }
    fun listarComFiltros(
        categoria: TipoProblema?,
        status: StatusOcorrencia?,
        minLat: Double?,
        minLon: Double?,
        maxLat: Double?,
        maxLon: Double?
    ): List<OcorrenciaMapa> {

        // Aqui chamamos a query poderosa do Repository
        return repository.buscarComFiltros(
            categoria = categoria?.name, // Converte Enum para String (ou null)
            status = status?.name,       // Converte Enum para String (ou null)
            minLat = minLat,
            minLon = minLon,
            maxLat = maxLat,
            maxLon = maxLon
        )
    }
    // =========================================================================
    // 3. AUXILIARES
    // =========================================================================

    private fun resolverCoordenadas(dto: CriarOcorrenciaDTO): Pair<BigDecimal, BigDecimal> {
        // Prioridade 1: GPS do Celular
        if (dto.latitude != null && dto.longitude != null) {
            return Pair(dto.latitude, dto.longitude)
        }

        // Prioridade 2: Endereço escrito (Google Maps)
        if (!dto.endereco.isNullOrBlank()) {
            val coords = googleMapsService.geocodificarEndereco(dto.endereco)
            if (coords != null) {
                return coords
            }
            throw IllegalArgumentException("Endereço não encontrado pelo Google Maps.")
        }

        // Se não mandou nada
        throw IllegalArgumentException("É necessário informar Latitude/Longitude OU um Endereço válido.")
    }
}