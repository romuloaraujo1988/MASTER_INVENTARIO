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

    /**
     * Campo legado do servidor — mantido para compatibilidade de contrato JSON.
     *
     * ⚠️ NÃO use este campo para enviar o conteúdo binário da foto.
     *
     * O upload de fotos é feito de forma assíncrona pelo [PhotoSyncWorker],
     * que usa o endpoint dedicado de upload após a coleta ser sincronizada.
     * Este campo deve ser sempre `null` nos requests gerados pelo app a
     * partir da v2.21 — o servidor ignora o valor quando o upload separado
     * já foi realizado.
     *
     * Histórico: antes da v2.21 era usado para enviar Base64 da foto
     * diretamente no body da coleta. Abandonado por causar payloads grandes
     * e timeouts em conexões lentas.
     */
    @SerializedName("fotoPatrimonio")
    val caminhoFotoLocal: String? = null,

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
    val tipoScan: String? = null,
    
    /**
     * B3 (idempotência — maio/2026): UUID gerado pelo cliente no momento da coleta.
     * Serve como chave de deduplicação no servidor — se o POST for reenviado (timeout
     * de rede, retry do WorkManager), o servidor reconhece e retorna a coleta existente
     * em vez de criar duplicata.
     *
     * Formato: UUID padrão ({@code java.util.UUID#randomUUID()}), 36 caracteres.
     * Gerado em {@code ColetaMapper.toEntity}/{@code toEntitySimple} no momento da
     * criação da {@code ColetaEntity}; sempre não-nulo para coletas geradas a partir
     * da v2.27.
     */
    @SerializedName("clienteUuid")
    val clienteUuid: String? = null
)
