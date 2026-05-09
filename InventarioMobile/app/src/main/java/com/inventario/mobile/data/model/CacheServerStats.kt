package com.inventario.mobile.data.model

/**
 * Cached last successful server response for dashboard statistics;
 * used by `DashboardRepositoryImpl.buscarEstatisticasLocais` when offline
 * (Req 3.6, 3.8, 3.9).
 *
 * Plain POJO — no Android dependencies, no annotations (not a Room entity,
 * not a Retrofit DTO). It is an in-memory representation of what is
 * persisted in `EncryptedSharedPreferences` via `PreferencesManager`.
 *
 * @property totalPatrimonios Total de patrimônios do inventário (vindo do servidor).
 * @property totalColetados Total de coletados reportado pelo servidor na última sincronização.
 * @property divergencias Quantidade de divergências reportada pelo servidor.
 * @property coletoresAtivos Quantidade de coletores ativos reportada pelo servidor.
 * @property valorTotal Valor total agregado reportado pelo servidor.
 * @property inventarioNome Nome do inventário ativo na última sincronização (nullable).
 * @property inventarioId Id do inventário ativo na última sincronização (nullable).
 * @property timestamp Momento (ms UTC, `System.currentTimeMillis()`) da última sincronização bem-sucedida.
 */
data class CacheServerStats(
    val totalPatrimonios: Int,
    val totalColetados: Int,
    val divergencias: Int,
    val coletoresAtivos: Int,
    val valorTotal: Double,
    val inventarioNome: String?,
    val inventarioId: Int?,
    val timestamp: Long
)
