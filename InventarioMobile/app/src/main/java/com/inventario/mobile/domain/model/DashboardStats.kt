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
    val inventarioNome: String? = null,
    val coletasHoje: Int = 0,
    val coletasSemana: Int = 0,
    val coletasMes: Int = 0,
    val tempoMedioColeta: Double = 0.0,
    val isOfflineData: Boolean = false,  // Flag indicando se são dados offline/locais
    // Timestamp (ms UTC) da última sincronização bem-sucedida do servidor;
    // null quando ainda não houve sincronização (Req 3.6, 4.3, 4.4).
    val timestampUltimaSincronizacao: Long? = null
) {
    init {
        // Invariantes de não-negatividade (Property 2 / Req 2.12).
        require(totalColetados >= 0) { "totalColetados deve ser >= 0 (recebido: $totalColetados)" }
        require(totalPendentes >= 0) { "totalPendentes deve ser >= 0 (recebido: $totalPendentes)" }
        require(totalPatrimonios >= 0) { "totalPatrimonios deve ser >= 0 (recebido: $totalPatrimonios)" }
    }

    companion object {
        /**
         * Retorna um [DashboardStats] zerado, usado como valor inicial da
         * FonteEstatisticas e como fallback em caminhos de erro do repositório.
         *
         * @param inventarioId id do inventário ativo, se conhecido (opcional).
         * @param isOfflineData marca a instância como dado offline; default `false`
         *                      mantém retrocompatibilidade com chamadores antigos.
         */
        fun empty(
            inventarioId: Int? = null,
            isOfflineData: Boolean = false
        ): DashboardStats = DashboardStats(
            totalPatrimonios = 0,
            totalColetados = 0,
            totalPendentes = 0,
            percentualConclusao = 0.0,
            inventarioId = inventarioId,
            isOfflineData = isOfflineData,
            timestampUltimaSincronizacao = null
        )
    }

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
