package com.inventario.mobile.data.remote.dto

/**
 * DTO para requisição de registro de coletas em lote
 * Usado para sincronizar múltiplas coletas pendentes de uma vez
 */
data class MobileColetaBatchRequest(
    val coletas: List<MobileColetaRequest>
)
