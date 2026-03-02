package com.example.df.backend.services

import com.example.df.backend.dtos.AlterarSenhaDTO
import com.example.df.backend.dtos.AtualizarPerfilDTO
import com.example.df.backend.dtos.RegistroDTO
import com.example.df.backend.entities.Usuario
import com.example.df.backend.repositories.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val fotoService: FotoService
) {

    @Transactional
    fun registrarUsuario(dto: RegistroDTO): Usuario {
        val cpfLimpo = dto.cpf.replace(Regex("[^0-9]"), "")

        if (!isCpfValido(cpfLimpo)) throw IllegalArgumentException("CPF inválido.")
        if (usuarioRepository.existsByEmail(dto.email)) throw IllegalArgumentException("E-mail já cadastrado.")
        if (usuarioRepository.existsByCpf(cpfLimpo)) throw IllegalArgumentException("CPF já cadastrado.")

        val novoUsuario = Usuario(
            cpf = cpfLimpo,
            nomeCompleto = dto.nomeCompleto,
            email = dto.email,
            senhaHash = passwordEncoder.encode(dto.senha),
            dataNascimento = dto.dataNascimento,
            telefone = dto.telefone,
            raAdministrativa = dto.raAdministrativa
        )

        return usuarioRepository.save(novoUsuario)
    }

    // =========================================================================
    // ATUALIZAÇÃO DE FOTO DE PERFIL (REFATORADO)
    // =========================================================================
    @Transactional
    fun atualizarFotoPerfil(usuario: Usuario, arquivo: MultipartFile): String {
        // 1. Limpeza: Se o usuário já tem uma URL de foto, extraímos o publicId para deletar
        // Exemplo de url: https://vigiadf.pmhub.cloud/api/foto/v/ABC123XYZ456
        usuario.urlFoto?.let { urlAntiga ->
            val publicIdAntigo = urlAntiga.substringAfterLast("/")
            try {
                fotoService.deletarArquivoFisico(publicIdAntigo)
            } catch (e: Exception) {
                // Logar erro mas permitir continuar se o arquivo físico já não existir
            }
        }

        // 2. Salvamento: Usamos o novo método unificado do FotoService
        // O tipo "PERFIL" garante que caia na pasta /perfis
        val fotoEntidade = fotoService.salvarMidiaGeral(
            tipo = "PERFIL",
            targetId = usuario.id ?: 0L,
            arquivo = arquivo
        )

        // 3. Montamos a nova URL padrão
        val novaUrl = "https://vigiadf.pmhub.cloud/api/foto/v/${fotoEntidade.publicId}"

        // 4. Atualizamos o usuário no banco
        usuario.urlFoto = novaUrl
        usuarioRepository.save(usuario)

        return novaUrl
    }

    @Transactional
    fun atualizarPerfil(usuario: Usuario, dto: AtualizarPerfilDTO): Usuario {
        usuario.nomeCompleto = dto.nomeCompleto ?: usuario.nomeCompleto
        usuario.telefone = dto.telefone ?: usuario.telefone
        return usuarioRepository.save(usuario)
    }

    @Transactional
    fun alterarSenha(usuario: Usuario, dto: AlterarSenhaDTO) {
        if (!passwordEncoder.matches(dto.senhaAtual, usuario.senhaHash)) {
            throw IllegalArgumentException("A senha atual está incorreta.")
        }
        usuario.senhaHash = passwordEncoder.encode(dto.novaSenha)
        usuarioRepository.save(usuario)
    }

    private fun isCpfValido(cpf: String): Boolean {
        if (cpf.length != 11 || cpf.all { it == cpf[0] }) return false
        val dv1 = calcularDigito(cpf.substring(0, 9), intArrayOf(10, 9, 8, 7, 6, 5, 4, 3, 2))
        val dv2 = calcularDigito(cpf.substring(0, 9) + dv1, intArrayOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2))
        return cpf.endsWith("$dv1$dv2")
    }

    private fun calcularDigito(str: String, pesos: IntArray): Int {
        val soma = str.indices.sumOf { (str[it] - '0') * pesos[it] }
        val resto = soma % 11
        return if (resto < 2) 0 else 11 - resto
    }
}