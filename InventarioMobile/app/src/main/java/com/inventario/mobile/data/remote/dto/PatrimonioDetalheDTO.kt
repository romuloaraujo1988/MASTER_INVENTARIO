package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.Date

/**
 * DTO para resposta da API de detalhes de patrimônio
 * Mapeia a resposta do endpoint /api/mobile/consulta/patrimonio/{id}/detalhes
 */
data class PatrimonioDetalheDTO(
    // Dados básicos
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("codigo")
    val codigo: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("marca")
    val marca: String? = null,
    
    @SerializedName("modelo")
    val modelo: String? = null,
    
    @SerializedName("numeroSerie")
    val numeroSerie: String? = null,
    
    @SerializedName("estado")
    val estado: String? = null,
    
    @SerializedName("valor")
    val valor: BigDecimal? = null,
    
    @SerializedName("observacoes")
    val observacoes: String? = null,
    
    // Dados da sala
    @SerializedName("salaId")
    val salaId: Int? = null,
    
    @SerializedName("salaNome")
    val salaNome: String? = null,
    
    @SerializedName("salaBloco")
    val salaBloco: String? = null,
    
    @SerializedName("salaAndar")
    val salaAndar: String? = null,
    
    // Dados do responsável
    @SerializedName("responsavelId")
    val responsavelId: Int? = null,
    
    @SerializedName("responsavelNome")
    val responsavelNome: String? = null,
    
    @SerializedName("responsavelMatricula")
    val responsavelMatricula: String? = null,
    
    @SerializedName("responsavelSetor")
    val responsavelSetor: String? = null,
    
    @SerializedName("responsavelEmail")
    val responsavelEmail: String? = null,
    
    @SerializedName("responsavelTelefone")
    val responsavelTelefone: String? = null,
    
    // Status de coleta
    @SerializedName("coletado")
    val coletado: Boolean = false,
    
    @SerializedName("dataColeta")
    val dataColeta: Date? = null,
    
    @SerializedName("coletadoPor")
    val coletadoPor: String? = null,
    
    @SerializedName("localizacaoEncontrada")
    val localizacaoEncontrada: String? = null,
    
    @SerializedName("estadoEncontrado")
    val estadoEncontrado: String? = null,
    
    @SerializedName("observacoesColeta")
    val observacoesColeta: String? = null,
    
    // Histórico
    @SerializedName("totalColetas")
    val totalColetas: Int = 0,
    
    @SerializedName("ultimaColeta")
    val ultimaColeta: Date? = null,
    
    // Foto
    @SerializedName("fotoUrl")
    val fotoUrl: String? = null
)
