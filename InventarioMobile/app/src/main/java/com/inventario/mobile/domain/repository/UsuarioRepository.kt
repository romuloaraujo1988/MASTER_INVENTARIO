package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Usuário
 */
interface UsuarioRepository {

    // Operações de consulta
    fun getAllUsuariosAtivos(): Flow<List<Usuario>>
    fun getAllUsuarios(): Flow<List<Usuario>>
    suspend fun getUsuarioById(id: Long): Usuario?
    suspend fun getUsuarioByLogin(login: String): Usuario?
    suspend fun getUsuarioByEmail(email: String): Usuario?
    suspend fun autenticarUsuario(login: String, senha: String): Usuario?
    suspend fun getUsuariosNaoSincronizados(): List<Usuario>

    // Operações de inserção
    suspend fun insertUsuario(usuario: Usuario): Long
    suspend fun insertUsuarios(usuarios: List<Usuario>)

    // Operações de atualização
    suspend fun updateUsuario(usuario: Usuario)
    suspend fun marcarComoSincronizado(id: Long, servidorId: Long)

    // Operações de exclusão
    suspend fun deleteUsuario(usuario: Usuario)

    // Sincronização
    suspend fun sincronizarUsuarios(): Result<Unit>
    suspend fun enviarUsuariosParaServidor(): Result<Unit>
}