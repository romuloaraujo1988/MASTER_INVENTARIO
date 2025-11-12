package com.inventario.mobile.data.local.dao

/**
 * Data classes para queries de gráficos
 */

/**
 * Dados de evolução diária das coletas
 */
data class EvolutionData(
    val data: String,
    val quantidade: Int
)

/**
 * Dados de top itens coletados
 */
data class TopItemData(
    val descricao: String,
    val quantidade: Int
)

/**
 * Dados de patrimônios por setor
 */
data class SetorData(
    val setor: String,
    val quantidade: Int
)

/**
 * Dados de status dos patrimônios
 */
data class StatusData(
    val status: String,
    val quantidade: Int
)
