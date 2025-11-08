package com.inventario.mobile.data.model

/**
 * Modelo simples para Usuário (sem Room)
 * Usado para armazenar dados do usuário logado
 */
data class Usuario(
    val id: Long,
    val username: String,
    val nome: String,
    val email: String,
    val ativo: Boolean = true,
    val perfil: String? = null,
    val setorId: Long? = null,
    val setorNome: String? = null,
    
    // Campos adicionais para sessão local
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenExpiry: Long? = null
) {
    companion object {
        /**
         * Converte DTO para modelo local
         */
        fun fromDto(dto: com.inventario.mobile.data.remote.dto.UsuarioDto): Usuario {
            return Usuario(
                id = dto.id,
                username = dto.username,
                nome = dto.nome,
                email = dto.email,
                ativo = dto.ativo,
                perfil = dto.perfil,
                setorId = dto.setorId,
                setorNome = dto.setorNome
            )
        }
        
        /**
         * Cria usuário com tokens de autenticação
         */
        fun fromLoginResponse(
            dto: com.inventario.mobile.data.remote.dto.UsuarioDto,
            accessToken: String,
            refreshToken: String,
            expiresIn: Long
        ): Usuario {
            return Usuario(
                id = dto.id,
                username = dto.username,
                nome = dto.nome,
                email = dto.email,
                ativo = dto.ativo,
                perfil = dto.perfil,
                setorId = dto.setorId,
                setorNome = dto.setorNome,
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000)
            )
        }
    }
    
    /**
     * Propriedade computada para compatibilidade
     */
    val login: String
        get() = username
    
    /**
     * Verifica se o token ainda é válido
     */
    fun isTokenValid(): Boolean {
        return tokenExpiry?.let { it > System.currentTimeMillis() } ?: false
    }
    
    /**
     * Verifica se o token expira em breve (próximos 5 minutos)
     */
    fun shouldRefreshToken(): Boolean {
        return tokenExpiry?.let { 
            it - System.currentTimeMillis() < 5 * 60 * 1000 // 5 minutos
        } ?: true
    }
    
    /**
     * Converte modelo local para DTO
     */
    fun toDto(): com.inventario.mobile.data.remote.dto.UsuarioDto {
        return com.inventario.mobile.data.remote.dto.UsuarioDto(
            id = id,
            username = username,
            nome = nome,
            email = email,
            ativo = ativo,
            perfil = perfil,
            setorId = setorId,
            setorNome = setorNome
        )
    }
}