package com.example.df.backend.services

import com.example.df.backend.dtos.AlterarSenhaDTO
import com.example.df.backend.dtos.AtualizarPerfilDTO
import com.example.df.backend.dtos.RegistroDTO
import com.example.df.backend.entities.Usuario
import com.example.df.backend.repositories.RegiaoAdministrativaRepository
import com.example.df.backend.repositories.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val fotoService: FotoService,
    private val raRepo: RegiaoAdministrativaRepository
) {

    @Transactional
    fun registrarUsuario(dto: RegistroDTO): Usuario {
        val cpfLimpo = dto.cpf.replace(Regex("[^0-9]"), "")

        if (!isCpfValido(cpfLimpo)) throw IllegalArgumentException("CPF inválido.")
        if (usuarioRepository.existsByEmail(dto.email)) throw IllegalArgumentException("E-mail já cadastrado.")
        if (usuarioRepository.existsByCpf(cpfLimpo)) throw IllegalArgumentException("CPF já cadastrado.")
        val raVinculada = raRepo.findByNomeContainingIgnoreCase(dto.raAdministrativa.trim())
            ?: throw IllegalArgumentException("A Região Administrativa '${dto.raAdministrativa}' não foi encontrada no sistema.")
        val novoUsuario = Usuario(
            cpf = cpfLimpo,
            nomeCompleto = dto.nomeCompleto,
            email = dto.email,
            senhaHash = passwordEncoder.encode(dto.senha),
            dataNascimento = dto.dataNascimento,
            telefone = dto.telefone,
            raAdministrativa = raVinculada,
            cep = dto.cep,
            logradouro =  dto.logradouro,
             numero= dto.numero,
             bairro= dto.bairro,
             cidade  = dto.cidade,
            urlFoto = dto.urlFoto,
            perfil = dto.perfil
        )

        return usuarioRepository.save(novoUsuario)
    }

    // =========================================================================
    // ATUALIZAÇÃO DE FOTO DE PERFIL (REFATORADO)
    // =========================================================================
    @Transactional
    fun alterarFotoPerfil(usuario: Usuario, file: MultipartFile): String {
        // 1. O FotoService salva na VPS, no banco e retorna a entidade com o publicId
        val fotoEntidade = fotoService.salvarMidiaGeral(
            tipo = "PERFIL",
            targetId = usuario.id!!,
            arquivo = file
        )

        // 2. Salvamos APENAS o publicId no campo de foto do usuário
        // Isso garante flexibilidade se o endereço do seu servidor mudar
        usuario.urlFoto = fotoEntidade.publicId
        usuarioRepository.save(usuario)

        // 3. Retornamos o publicId (ou a URL se o front precisar agora, mas o banco guarda o ID)
        return fotoEntidade.publicId
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