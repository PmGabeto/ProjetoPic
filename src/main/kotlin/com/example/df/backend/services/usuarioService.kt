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
        // 1. Limpeza
        val cpfLimpo = dto.cpf.replace(Regex("[^0-9]"), "")

        // 2. Validações
        if (!isCpfValido(cpfLimpo)) throw IllegalArgumentException("CPF inválido.")
        if (usuarioRepository.existsByEmail(dto.email)) throw IllegalArgumentException("E-mail já cadastrado.")
        if (usuarioRepository.existsByCpf(cpfLimpo)) throw IllegalArgumentException("CPF já cadastrado.")

        // 3. Criação
        val novoUsuario = Usuario(
            cpf = cpfLimpo,
            nomeCompleto = dto.nomeCompleto,
            email = dto.email,
            senhaHash = passwordEncoder.encode(dto.senha),
            dataNascimento = dto.dataNascimento,
            telefone = dto.telefone,
            raAdministrativa = dto.raAdministrativa,
            cep = dto.cep,
            logradouro = dto.logradouro,
            numero = dto.numero,
            bairro = dto.bairro,
            urlFoto = dto.urlFoto,
            perfil = dto.perfil,
            cidade = dto.cidade

        )

        return usuarioRepository.save(novoUsuario)
    }

    private fun isCpfValido(cpf: String): Boolean {
        if (cpf.length != 11) return false
        if (cpf.all { it == cpf[0] }) return false
        try {
            val d1 = calcularDigito(cpf.take(9))
            val d2 = calcularDigito(cpf.take(9) + d1)
            return cpf == cpf.take(9) + d1 + d2
        } catch (e: Exception) { return false }
    }

    private fun calcularDigito(str: String): Int {
        var soma = 0
        var peso = str.length + 1
        for (char in str) {
            soma += char.toString().toInt() * peso
            peso--
        }
        val resto = soma % 11
        return if (resto < 2) 0 else 11 - resto
    }
    // métodos de atualização

    @Transactional
    fun atualizarPerfil(usuario: Usuario, dto: AtualizarPerfilDTO): Usuario {
        // O Kotlin "copy" cria um novo objeto baseando-se no anterior
        // Note que NÃO passamos nomeCompleto nem CPF aqui.
        val usuarioAtualizado = usuario.copy(
            telefone = dto.telefone ?: usuario.telefone,

            // Atualização de Endereço
            cep = dto.cep ?: usuario.cep,
            raAdministrativa = dto.raAdministrativa ?: usuario.raAdministrativa,
            logradouro = dto.logradouro ?: usuario.logradouro,
            numero = dto.numero ?: usuario.numero,
            bairro = dto.bairro ?: usuario.bairro

            // O campo data de atualização seria ideal ter na entidade,
            // mas se não tiver, não tem problema.
        )
        return usuarioRepository.save(usuarioAtualizado)
    }

    // 2. Alterar Senha com Segurança
    @Transactional
    fun alterarSenha(usuario: Usuario, dto: AlterarSenhaDTO) {
        // 1. Verifica se a senha antiga bate (Segurança básica)
        if (!passwordEncoder.matches(dto.senhaAtual, usuario.senhaHash)) {
            throw IllegalArgumentException("A senha atual está incorreta.")
        }

        // 2. Encripta a nova senha
        val novaSenhaHash = passwordEncoder.encode(dto.novaSenha)

        // 3. Salva
        val usuarioComNovaSenha = usuario.copy(senhaHash = novaSenhaHash)
        usuarioRepository.save(usuarioComNovaSenha)
    }

    // 3. Alterar Foto de Perfil
    @Transactional
    fun atualizarFotoPerfil(usuario: Usuario, arquivo: MultipartFile): String {
        // 1. Limpeza: Se o usuário já tem uma foto antiga, pedimos para deletar o arquivo físico
        // Isso evita que o servidor fique cheio de fotos órfãs que ninguém usa mais.
        if (usuario.urlFoto != null) {
            fotoService.deletarFoto(usuario.urlFoto)
        }

        // 2. Salvamento: O FotoService salva na pasta "/perfis" e retorna o caminho (ex: "perfis/uuid.jpg")
        val novoCaminho = fotoService.salvarFotoPerfil(arquivo)

        // 3. Atualização no Banco: Atualizamos o campo urlFoto do usuário
        val usuarioAtualizado = usuario.copy(
            urlFoto = novoCaminho

        )

        usuarioRepository.save(usuarioAtualizado)

        // 4. Retorno: Devolvemos o caminho para o Controller mandar de volta pro Front-end
        return novoCaminho
    }
}