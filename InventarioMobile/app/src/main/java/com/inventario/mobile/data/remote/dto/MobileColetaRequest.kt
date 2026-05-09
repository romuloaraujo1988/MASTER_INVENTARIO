package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para requisição de registro de coleta mobile
 * Corresponde exatamente ao MobileColetaRequest.java do servidor
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
data class MobileColetaRequest(
    @SerializedName("numeroPatrimonio")
    val numeroPatrimonio: String,
    
    @SerializedName("idInventario")
    val idInventario: Int,
    
    @SerializedName("usuarioId")
    val usuarioId: Int,
    
    @SerializedName("idSala")
    val idSala: Int? = null,
    
    @SerializedName("localizacaoEncontrada")
    val localizacaoEncontrada: String? = null,
    
    @SerializedName("estadoEncontrado")
    val estadoEncontrado: String,
    
    @SerializedName("observacaoColeta")
    val observacaoColeta: String? = null,
    
    @SerializedName("dataColeta")
    val dataColeta: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("fotoPatrimonio")
    val fotoPatrimonio: String? = null,
    
    @SerializedName("semEtiqueta")
    val semEtiqueta: Boolean? = false,
    
    @SerializedName("descricaoItemSemEtiqueta")
    val descricaoItemSemEtiqueta: String? = null,
    
    @SerializedName("categoriaItemSemEtiqueta")
    val categoriaItemSemEtiqueta: String? = null,
    
    @SerializedName("deviceId")
    val deviceId: String? = null,
    
    @SerializedName("appVersion")
    val appVersion: String? = null,
    
    @SerializedName("divergencia")
    val divergencia: Boolean? = false,
    
    @SerializedName("motivoDivergencia")
    val motivoDivergencia: String? = null,
    
    // ========== METRICAS V2 ==========
    @SerializedName("tempoColetaSegundos")
    val tempoColetaSegundos: Int? = null,
    
    @SerializedName("tempoScanSegundos")
    val tempoScanSegundos: Int? = null,
    
    @SerializedName("tempoPreenchimentoSegundos")
    val tempoPreenchimentoSegundos: Int? = null,
    
    @SerializedName("metodoColeta")
    val metodoColeta: String? = null,
    
    @SerializedName("tipoScan")
    val tipoScan: String? = null
)
