package com.inventario.mobile.data.model

/**
 * Modelo simples para Coleta (sem Room)
 * Usado para armazenar dados de coleta localmente
 */
data class Coleta(
    val id: Int? = null,
    val patrimonioId: Int,
    val usuarioId: Int,
    val dataColeta: String,
    val localizacaoAtual: String? = null,
    val estadoEncontrado: String? = "BOM",
    val observacoes: String? = null,
    val fotoPath: String? = null,
    val status: String? = "COLETADO",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null,
    
    // Campos de divergência
    val divergencia: Boolean = false,
    val motivoDivergencia: String? = null,
    
    // Campos adicionais do servidor
    val numeroPatrimonio: String? = null,
    val descricaoPatrimonio: String? = null,
    val nomeInventario: String? = null,
    val nomeSala: String? = null,
    val nomeColetor: String? = null,
    
    // Campos adicionais para funcionalidade local
    val sincronizado: Boolean = false,
    val tentativasSincronizacao: Int = 0
) {
    companion object {
        /**
         * Converte DTO para modelo local
         */
        fun fromDto(dto: com.inventario.mobile.data.remote.dto.ColetaDto): Coleta {
            val usuarioId = dto.usuarioIdCamel ?: dto.usuarioId ?: 0
            android.util.Log.d("Coleta.fromDto", "Convertendo DTO - ID: ${dto.id}, usuarioIdCamel: ${dto.usuarioIdCamel}, usuarioId: ${dto.usuarioId}, resultado: $usuarioId")
            android.util.Log.d("Coleta.fromDto", "  localizacaoEncontrada: '${dto.localizacaoEncontrada}'")
            android.util.Log.d("Coleta.fromDto", "  nomeSala: '${dto.nomeSala}'")
            
            return Coleta(
                id = dto.id,
                patrimonioId = dto.patrimonioIdCamel ?: dto.patrimonioId ?: 0,
                usuarioId = usuarioId,
                dataColeta = dto.dataColeta ?: System.currentTimeMillis().toString(),
                localizacaoAtual = dto.localizacaoEncontrada,
                observacoes = dto.observacaoColeta,
                fotoPath = dto.fotoPath,
                status = dto.statusColeta ?: "coletado",
                latitude = dto.latitude,
                longitude = dto.longitude,
                dataCriacao = dto.dataCriacao ?: System.currentTimeMillis().toString(),
                dataAtualizacao = dto.dataAtualizacao ?: System.currentTimeMillis().toString(),
                nomeInventario = dto.nomeInventario,
                nomeSala = dto.nomeSala,
                nomeColetor = dto.nomeColetor,
                sincronizado = dto.sincronizado ?: true,
                // Garantir que numeroPatrimonio e descricaoPatrimonio sejam sempre populados
                numeroPatrimonio = dto.numeroPatrimonio?.takeIf { it.isNotBlank() } 
                    ?: (dto.patrimonioId?.toString() ?: "0"),
                descricaoPatrimonio = dto.descricaoPatrimonio?.takeIf { it.isNotBlank() } 
                    ?: dto.observacaoColeta?.takeIf { it.isNotBlank() } 
                    ?: "Patrimônio ${dto.patrimonioId ?: 0}"
            )
        }
        
        /**
         * Cria uma nova coleta local
         */
        fun createLocal(
            patrimonioId: Int,
            usuarioId: Int,
            observacoes: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            numeroPatrimonio: String? = null,
            descricaoPatrimonio: String? = null,
            nomeSala: String? = null
        ): Coleta {
            val currentTime = System.currentTimeMillis().toString()
            return Coleta(
                patrimonioId = patrimonioId,
                usuarioId = usuarioId,
                dataColeta = currentTime,
                observacoes = observacoes,
                latitude = latitude,
                longitude = longitude,
                dataCriacao = currentTime,
                dataAtualizacao = currentTime,
                numeroPatrimonio = numeroPatrimonio,
                descricaoPatrimonio = descricaoPatrimonio,
                nomeSala = nomeSala
            )
        }
    }
    
    /**
     * Converte modelo local para DTO (para enviar ao servidor)
     */
    fun toDto(patrimonio: com.inventario.mobile.data.model.Patrimonio? = null, idInventario: Int? = null): com.inventario.mobile.data.remote.dto.ColetaDto {
        return com.inventario.mobile.data.remote.dto.ColetaDto(
            id = id,
            numeroPatrimonio = patrimonio?.numeroPatrimonio,
            descricaoPatrimonio = patrimonio?.descricao,
            idInventario = idInventario,
            nomeInventario = null, // Será preenchido pelo servidor
            idSala = null, // Será preenchido pelo servidor se necessário
            nomeSala = null, // Será preenchido pelo servidor se necessário
            localizacaoEncontrada = localizacaoAtual,
            estadoEncontrado = "ENCONTRADO", // Estado padrão para coletas manuais
            observacaoColeta = observacoes,
            dataColeta = dataCriacao,
            statusColeta = status,
            nomeColetor = null, // Será preenchido pelo servidor
            semEtiqueta = false, // Para coletas manuais normais
            descricaoItemSemEtiqueta = null,
            categoriaItemSemEtiqueta = null,
            sincronizado = sincronizado,
            patrimonioId = patrimonioId,
            usuarioId = usuarioId,
            localizacaoAtual = localizacaoAtual,
            observacoes = observacoes,
            fotoPath = fotoPath,
            status = status,
            latitude = latitude,
            longitude = longitude,
            dataCriacao = dataCriacao,
            dataAtualizacao = dataAtualizacao
        )
    }
    
    /**
     * Converte modelo local para MobileColetaRequest (formato correto do servidor)
     */
    fun toMobileColetaRequest(
        patrimonio: com.inventario.mobile.data.model.Patrimonio,
        idInventario: Int,
        deviceId: String? = null,
        appVersion: String? = null
    ): com.inventario.mobile.data.remote.dto.MobileColetaRequest {
        return com.inventario.mobile.data.remote.dto.MobileColetaRequest(
            numeroPatrimonio = patrimonio.numeroPatrimonio,
            idInventario = idInventario,
            usuarioId = usuarioId,
            idSala = patrimonio.salaId?.toInt(),
            localizacaoEncontrada = localizacaoAtual ?: nomeSala,
            estadoEncontrado = this.estadoEncontrado ?: "BOM",
            observacaoColeta = observacoes,
            dataColeta = dataColeta,
            latitude = latitude,
            longitude = longitude,
            fotoPatrimonio = fotoPath,
            semEtiqueta = false,
            descricaoItemSemEtiqueta = null,
            categoriaItemSemEtiqueta = null,
            deviceId = deviceId,
            appVersion = appVersion,
            divergencia = this.divergencia,
            motivoDivergencia = this.motivoDivergencia
        )
    }
}