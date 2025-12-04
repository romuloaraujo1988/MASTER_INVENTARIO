package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para patrimônio recebido do servidor
 * Corresponde ao MobilePatrimonioDto.java do backend
 */
data class MobilePatrimonioDto(
    @SerializedName("id")
    val id: Long? = null,
    
    @SerializedName("codigo")
    val codigo: String? = null,
    
    @SerializedName("descricao")
    val descricao: String? = null,
    
    @SerializedName("marca")
    val marca: String? = null,
    
    @SerializedName("modelo")
    val modelo: String? = null,
    
    @SerializedName("numeroSerie")
    val numeroSerie: String? = null,
    
    @SerializedName("estado")
    val estado: String? = null,
    
    @SerializedName("valor")
    val valor: Double? = null,
    
    @SerializedName("setorId")
    val setorId: Long? = null,
    
    @SerializedName("setorNome")
    val setorNome: String? = null,
    
    @SerializedName("salaId")
    val salaId: Long? = null,
    
    @SerializedName("salaNome")
    val salaNome: String? = null,
    
    @SerializedName("responsavelId")
    val responsavelId: Long? = null,
    
    @SerializedName("responsavelNome")
    val responsavelNome: String? = null,
    
    @SerializedName("qrCode")
    val qrCode: String? = null,
    
    @SerializedName("coletado")
    val coletado: Boolean = false,
    
    @SerializedName("dataColeta")
    val dataColeta: String? = null,
    
    @SerializedName("observacoes")
    val observacoes: String? = null,
    
    @SerializedName("coletadoPor")
    val coletadoPor: String? = null,
    
    @SerializedName("dataColetaFormatada")
    val dataColetaFormatada: String? = null,
    
    @SerializedName("ed")
    val ed: String? = null,
    
    @SerializedName("numeroNotaFiscal")
    val numeroNotaFiscal: String? = null,
    
    @SerializedName("fornecedor")
    val fornecedor: String? = null,
    
    @SerializedName("localizacaoEncontrada")
    val localizacaoEncontrada: String? = null,
    
    @SerializedName("estadoEncontrado")
    val estadoEncontrado: String? = null,
    
    @SerializedName("temDivergencia")
    val temDivergencia: Boolean? = false
) {
    /**
     * Converte para modelo de domínio
     */
    fun toDomain(): com.inventario.mobile.domain.model.Patrimonio {
        return com.inventario.mobile.domain.model.Patrimonio(
            id = id?.toInt() ?: 0,
            numeroPatrimonio = codigo ?: "",
            descricao = descricao,
            marca = marca,
            modelo = modelo,
            numeroSerie = numeroSerie,
            estado = estado,
            valor = valor,
            idSala = salaId?.toInt(),
            nomeSala = salaNome,
            idResponsavel = responsavelId?.toInt(),
            nomeResponsavel = responsavelNome,
            idSetor = setorId?.toInt(),
            nomeSetor = setorNome,
            qrCode = qrCode,
            coletado = coletado,
            dataColeta = dataColeta,
            coletadoPor = coletadoPor,
            dataColetaFormatada = dataColetaFormatada,
            localizacaoEncontrada = localizacaoEncontrada,
            estadoEncontrado = estadoEncontrado,
            observacoes = observacoes
        )
    }
}
