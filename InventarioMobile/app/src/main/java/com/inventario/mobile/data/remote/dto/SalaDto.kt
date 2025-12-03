package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para transferência de dados de Sala
 */
data class SalaDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("idSala")
    val idSala: Int?,
    
    @SerializedName("numeroSala")
    val numeroSala: String?,
    
    @SerializedName("numero")
    val numero: String?,
    
    @SerializedName("descricao")
    val descricao: String?,
    
    @SerializedName("ativo")
    val ativo: Boolean,
    
    @SerializedName("ativa")
    val ativa: Boolean?,
    
    @SerializedName("idSetor")
    val idSetor: Int?,
    
    @SerializedName("setor_id")
    val setorId: Int?,
    
    @SerializedName("nomeSetor")
    val nomeSetor: String?,
    
    @SerializedName("andar")
    val andar: Int?,
    
    @SerializedName("bloco")
    val bloco: String?,
    
    @SerializedName("tipoSala")
    val tipoSala: String?,
    
    @SerializedName("dataCadastro")
    val dataCadastro: String?,
    
    @SerializedName("data_criacao")
    val dataCriacao: String?,
    
    @SerializedName("data_atualizacao")
    val dataAtualizacao: String?
) {
    // Propriedades computadas para facilitar o acesso
    val nome: String
        get() = numeroSala ?: numero ?: descricao ?: "Sala ${id}"
    
    val codigo: String
        get() = numero ?: numeroSala ?: id.toString()
    
    val setorIdFinal: Int
        get() = idSetor ?: setorId ?: 0
}
