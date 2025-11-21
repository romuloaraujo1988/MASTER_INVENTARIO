package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para sincronização offline completa
 * Espelha o DTO do servidor Java
 * 
 * v2.2: DTO para endpoint dedicado de sincronização
 */
data class MobileOfflineDataDTO(
    @SerializedName("patrimonios")
    val patrimonios: List<PatrimonioOfflineDTO>,
    
    @SerializedName("salas")
    val salas: List<SalaOfflineDTO>,
    
    @SerializedName("responsaveis")
    val responsaveis: List<ResponsavelOfflineDTO>,
    
    @SerializedName("metadata")
    val metadata: MetadataDTO
) {
    /**
     * DTO simplificado de patrimônio
     */
    data class PatrimonioOfflineDTO(
        @SerializedName("id")
        val id: Long,
        
        @SerializedName("numeroPatrimonio")
        val numeroPatrimonio: String,
        
        @SerializedName("descricao")
        val descricao: String,
        
        @SerializedName("marca")
        val marca: String?,
        
        @SerializedName("modelo")
        val modelo: String?,
        
        @SerializedName("estado")
        val estado: String?,
        
        @SerializedName("salaId")
        val salaId: Int?,
        
        @SerializedName("salaNome")
        val salaNome: String?,
        
        @SerializedName("responsavelId")
        val responsavelId: Int?,
        
        @SerializedName("responsavelNome")
        val responsavelNome: String?,
        
        @SerializedName("coletado")
        val coletado: Boolean = false
    )
    
    /**
     * DTO simplificado de sala
     */
    data class SalaOfflineDTO(
        @SerializedName("id")
        val id: Int,
        
        @SerializedName("nome")
        val nome: String,
        
        @SerializedName("ativa")
        val ativa: Boolean = true
    )
    
    /**
     * DTO simplificado de responsável
     */
    data class ResponsavelOfflineDTO(
        @SerializedName("id")
        val id: Int,
        
        @SerializedName("nome")
        val nome: String,
        
        @SerializedName("cpf")
        val cpf: String?
    )
    
    /**
     * Metadados da sincronização
     */
    data class MetadataDTO(
        @SerializedName("timestamp")
        val timestamp: Long,
        
        @SerializedName("totalPatrimonios")
        val totalPatrimonios: Int,
        
        @SerializedName("totalSalas")
        val totalSalas: Int,
        
        @SerializedName("totalResponsaveis")
        val totalResponsaveis: Int,
        
        @SerializedName("inventarioAtivoId")
        val inventarioAtivoId: Int?,
        
        @SerializedName("inventarioAtivoNome")
        val inventarioAtivoNome: String?,
        
        @SerializedName("versaoServidor")
        val versaoServidor: String?
    )
}
