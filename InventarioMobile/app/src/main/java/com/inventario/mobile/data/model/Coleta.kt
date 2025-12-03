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
    val localizacaoEncontrada: String? = null,  // Onde o item foi ENCONTRADO durante a coleta
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
    val nomeSala: String? = null,  // Localização ORIGINAL do patrimônio
    val nomeColetor: String? = null,
    
    // Campos adicionais para funcionalidade local
    val sincronizado: Boolean = false,
    val tentativasSincronizacao: Int = 0,
    val erroSincronizacao: String? = null,  // v2.6: Motivo do erro de sincronização
    
    // Campos para itens sem etiqueta
    val semEtiqueta: Boolean = false,
    val descricaoItemSemEtiqueta: String? = null,
    val categoriaItemSemEtiqueta: String? = null
) {
    companion object {
        /**
         * Converte DTO para modelo local
         */
        fun fromDto(dto: com.inventario.mobile.data.remote.dto.ColetaDto): Coleta {
            val usuarioId = dto.usuarioIdCamel ?: dto.usuarioId ?: 0
            android.util.Log.d("Coleta.fromDto", "═══════════════════════════════════════")
            android.util.Log.d("Coleta.fromDto", "Convertendo DTO - ID: ${dto.id}")
            android.util.Log.d("Coleta.fromDto", "  numeroPatrimonio: '${dto.numeroPatrimonio}'")
            android.util.Log.d("Coleta.fromDto", "  descricaoPatrimonio: '${dto.descricaoPatrimonio}'")
            android.util.Log.d("Coleta.fromDto", "  localizacaoEncontrada: '${dto.localizacaoEncontrada}'")
            android.util.Log.d("Coleta.fromDto", "  nomeSala: '${dto.nomeSala}'")
            android.util.Log.d("Coleta.fromDto", "  observacaoColeta: '${dto.observacaoColeta}'")
            android.util.Log.d("Coleta.fromDto", "  usuarioIdCamel: ${dto.usuarioIdCamel}, usuarioId: ${dto.usuarioId}")
            android.util.Log.d("Coleta.fromDto", "═══════════════════════════════════════")
            
            return Coleta(
                id = dto.id,
                patrimonioId = dto.patrimonioIdCamel ?: dto.patrimonioId ?: 0,
                usuarioId = usuarioId,
                dataColeta = dto.dataColeta ?: System.currentTimeMillis().toString(),
                localizacaoAtual = dto.nomeSala,  // Localização ORIGINAL do patrimônio
                localizacaoEncontrada = dto.localizacaoEncontrada,  // Onde foi ENCONTRADO
                observacoes = dto.observacaoColeta,
                fotoPath = dto.fotoPath,
                status = dto.statusColeta ?: "coletado",
                latitude = dto.latitude,
                longitude = dto.longitude,
                dataCriacao = dto.dataCriacao ?: System.currentTimeMillis().toString(),
                dataAtualizacao = dto.dataAtualizacao ?: System.currentTimeMillis().toString(),
                nomeInventario = dto.nomeInventario,
                nomeSala = dto.nomeSala,  // Localização ORIGINAL do patrimônio
                nomeColetor = dto.nomeColetor,
                sincronizado = dto.sincronizado ?: true,
                // Garantir que numeroPatrimonio e descricaoPatrimonio sejam sempre populados
                numeroPatrimonio = dto.numeroPatrimonio?.takeIf { it.isNotBlank() } 
                    ?: (dto.patrimonioId?.toString() ?: "0"),
                descricaoPatrimonio = dto.descricaoPatrimonio?.takeIf { it.isNotBlank() } 
                    ?: dto.observacaoColeta?.takeIf { it.isNotBlank() } 
                    ?: "Patrimônio ${dto.patrimonioId ?: 0}",
                // Campos para itens sem etiqueta
                semEtiqueta = dto.semEtiqueta ?: false,
                descricaoItemSemEtiqueta = dto.descricaoItemSemEtiqueta,
                categoriaItemSemEtiqueta = dto.categoriaItemSemEtiqueta
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
            semEtiqueta = semEtiqueta, // Usar valor do modelo
            descricaoItemSemEtiqueta = descricaoItemSemEtiqueta,
            categoriaItemSemEtiqueta = categoriaItemSemEtiqueta,
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
