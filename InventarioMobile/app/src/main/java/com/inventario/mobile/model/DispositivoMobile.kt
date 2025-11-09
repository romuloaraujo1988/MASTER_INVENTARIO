package com.inventario.mobile.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dispositivo mobile
 */
data class DispositivoMobile(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("idUsuario")
    val idUsuario: Int,
    
    @SerializedName("nomeUsuario")
    val nomeUsuario: String?,
    
    @SerializedName("modelo")
    val modelo: String,
    
    @SerializedName("fabricante")
    val fabricante: String,
    
    @SerializedName("versaoAndroid")
    val versaoAndroid: String,
    
    @SerializedName("versaoApp")
    val versaoApp: String,
    
    @SerializedName("enderecoIp")
    val enderecoIp: String?,
    
    @SerializedName("enderecoMac")
    val enderecoMac: String?,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("dataRegistro")
    val dataRegistro: String,
    
    @SerializedName("dataUltimaConexao")
    val dataUltimaConexao: String?,
    
    @SerializedName("dataUltimaSincronizacao")
    val dataUltimaSincronizacao: String?,
    
    @SerializedName("ativo")
    val ativo: Boolean,
    
    @SerializedName("observacoes")
    val observacoes: String?
) {
    fun isAprovado(): Boolean = status == "APROVADO" && ativo
    fun isPendente(): Boolean = status == "PENDENTE"
    fun isBloqueado(): Boolean = status == "BLOQUEADO"
    
    fun getStatusDescricao(): String = when (status) {
        "PENDENTE" -> "Pendente de Aprovação"
        "APROVADO" -> "Aprovado"
        "BLOQUEADO" -> "Bloqueado"
        "REJEITADO" -> "Rejeitado"
        else -> status
    }
}

/**
 * Request para registro de dispositivo
 */
data class DispositivoRegistroRequest(
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("idUsuario")
    val idUsuario: Int,
    
    @SerializedName("modelo")
    val modelo: String,
    
    @SerializedName("fabricante")
    val fabricante: String,
    
    @SerializedName("versaoAndroid")
    val versaoAndroid: String,
    
    @SerializedName("versaoApp")
    val versaoApp: String,
    
    @SerializedName("enderecoIp")
    val enderecoIp: String,
    
    @SerializedName("enderecoMac")
    val enderecoMac: String?
)
