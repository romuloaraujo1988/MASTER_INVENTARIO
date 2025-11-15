package com.inventario.mobile.data.model

/**
 * Modelo simples para Patrimônio (sem Room)
 * Usado para armazenar dados localmente de forma temporária
 */
data class Patrimonio(
    val id: Long = 0,
    @com.google.gson.annotations.SerializedName("codigo")
    val numeroPatrimonio: String,
    val descricao: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val estado: String? = null,
    val valor: Double? = null,
    val setorId: Long? = null,
    val setorNome: String? = null,
    val salaId: Long? = null,
    val salaNome: String? = null,
    val responsavelId: Long? = null,
    val responsavelNome: String? = null,
    val qrCode: String? = null,
    val observacoes: String? = null,
    
    // Campos adicionais para funcionalidade local
    val coletado: Boolean = false,
    val dataColeta: String? = null,
    val coletadoPor: String? = null,
    val dataColetaFormatada: String? = null,
    val observacoesColeta: String? = null,
    val sincronizado: Boolean = false,
    val servidorId: Long? = null
) {
    companion object {
        /**
         * Converte MobilePatrimonioDto para modelo local
         */
        fun fromMobileDto(dto: com.inventario.mobile.data.remote.dto.MobilePatrimonioDto): Patrimonio {
            return Patrimonio(
                id = dto.id,
                numeroPatrimonio = dto.codigo,
                descricao = dto.descricao,
                marca = dto.marca,
                modelo = dto.modelo,
                numeroSerie = dto.numeroSerie,
                estado = dto.estado,
                valor = dto.valor,
                setorId = dto.setorId,
                setorNome = dto.setorNome,
                salaId = dto.salaId,
                salaNome = dto.salaNome,
                responsavelId = dto.responsavelId,
                responsavelNome = dto.responsavelNome,
                qrCode = dto.qrCode,
                observacoes = dto.observacoes,
                coletado = dto.coletado,
                dataColeta = dto.dataColeta,
                coletadoPor = dto.coletadoPor,
                dataColetaFormatada = dto.dataColetaFormatada
            )
        }
        
        /**
         * Converte DTO antigo para modelo local (compatibilidade)
         */
        fun fromDto(dto: com.inventario.mobile.data.remote.dto.PatrimonioDto): Patrimonio {
            return Patrimonio(
                id = dto.id.toLong(),
                numeroPatrimonio = dto.numeroPatrimonio,
                descricao = dto.descricao,
                setorId = dto.setorId?.toLong(),
                salaId = dto.salaId?.toLong(),
                qrCode = dto.qrCode
            )
        }
    }
    
    /**
     * Converte modelo local para DTO antigo (compatibilidade)
     */
    fun toDto(): com.inventario.mobile.data.remote.dto.PatrimonioDto {
        return com.inventario.mobile.data.remote.dto.PatrimonioDto(
            id = id.toInt(),
            numeroPatrimonio = numeroPatrimonio,
            descricao = descricao,
            setorId = setorId?.toInt(),
            salaId = salaId?.toInt(),
            qrCode = qrCode,
            dataCriacao = null,
            dataAtualizacao = null
        )
    }
}