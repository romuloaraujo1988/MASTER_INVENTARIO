package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para transferência de dados de Usuário
 */
data class UsuarioDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("ativo")
    val ativo: Boolean,
    
    @SerializedName("perfil")
    val perfil: String?,
    
    @SerializedName("setorId")
    val setorId: Long?,
    
    @SerializedName("setorNome")
    val setorNome: String?
) {
    // Propriedade computada para compatibilidade com código existente
    val login: String
        get() = username
}
