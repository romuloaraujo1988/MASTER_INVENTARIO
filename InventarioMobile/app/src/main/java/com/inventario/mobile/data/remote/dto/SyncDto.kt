package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para sincronização de dados
 */
data class SyncStatusResponse(
    @SerializedName("last_sync")
    val lastSync: String?,
    
    @SerializedName("server_time")
    val serverTime: String,
    
    @SerializedName("pending_changes")
    val pendingChanges: Int
)

data class SyncDataRequest(
    @SerializedName("patrimonios")
    val patrimonios: List<PatrimonioDto>?,
    
    @SerializedName("coletas")
    val coletas: List<ColetaDto>?,
    
    @SerializedName("usuarios")
    val usuarios: List<UsuarioDto>?,
    
    @SerializedName("setores")
    val setores: List<SetorDto>?,
    
    @SerializedName("salas")
    val salas: List<SalaDto>?,
    
    @SerializedName("last_sync")
    val lastSync: String?
)

data class SyncDataResponse(
    @SerializedName("patrimonios")
    val patrimonios: List<PatrimonioDto>?,
    
    @SerializedName("coletas")
    val coletas: List<ColetaDto>?,
    
    @SerializedName("usuarios")
    val usuarios: List<UsuarioDto>?,
    
    @SerializedName("setores")
    val setores: List<SetorDto>?,
    
    @SerializedName("salas")
    val salas: List<SalaDto>?,
    
    @SerializedName("sync_time")
    val syncTime: String,
    
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?
)