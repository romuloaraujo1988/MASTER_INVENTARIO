package com.inventario.mobile.domain.model

/**
 * Domain Model para estatísticas do dashboard
 * Modelo puro sem dependências Android
 */
data class DashboardStats(
    val totalPatrimonios: Int,
    val totalColetados: Int,
    val totalPendentes: Int,
    val percentualConclusao: Double,
    val coletoresAtivos: Int = 0,
    val divergencias: Int = 0,
    val valorTotal: Double = 0.0,
    val inventarioId: Int? = null,
    val inventarioNome: String? = null
) {
    /**
     * Verifica se o inventário está completo
     */
    fun isCompleto(): Boolean = percentualConclusao >= 100.0
    
    /**
     * Verifica se há divergências
     */
    fun hasDivergencias(): Boolean = divergencias > 0
    
    /**
     * Calcula percentual de divergências
     */
    fun percentualDivergencias(): Double {
        return if (totalColetados > 0) {
            (divergencias.toDouble() / totalColetados.toDouble()) * 100.0
        } else {
            0.0
        }
    }
    
    /**
     * Retorna status do inventário
     */
    fun getStatus(): InventarioStatus {
        return when {
            percentualConclusao >= 100.0 -> InventarioStatus.COMPLETO
            percentualConclusao >= 80.0 -> InventarioStatus.QUASE_COMPLETO
            percentualConclusao >= 50.0 -> InventarioStatus.EM_ANDAMENTO
            percentualConclusao > 0.0 -> InventarioStatus.INICIADO
            else -> InventarioStatus.NAO_INICIADO
        }
    }
}

/**
 * Status do inventário
 */
enum class InventarioStatus {
    NAO_INICIADO,
    INICIADO,
    EM_ANDAMENTO,
    QUASE_COMPLETO,
    COMPLETO
}

/**
 * Domain Model para evolução de coletas
 */
data class EvolucaoColeta(
    val data: String, // Formato ISO: "2025-11-15"
    val quantidade: Int,
    val coletoresAtivos: Int = 0,
    val dataFormatada: String? = null // Formato: "15/11"
) {
    /**
     * Retorna data formatada para exibição
     */
    fun getDataExibicao(): String {
        return dataFormatada ?: data.takeLast(5).replace("-", "/")
    }
}

/**
 * Domain Model para top itens
 */
data class TopItem(
    val descricao: String,
    val quantidade: Int,
    val percentual: Double = 0.0
)

/**
 * Domain Model para distribuição por sala
 */
data class DistribuicaoSala(
    val sala: String,
    val quantidade: Int,
    val percentual: Double = 0.0
)

/**
 * Domain Model para estatísticas por status
 */
data class EstatisticaStatus(
    val status: String,
    val quantidade: Int,
    val percentual: Double = 0.0
)
