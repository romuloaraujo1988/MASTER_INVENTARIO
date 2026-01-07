package com.inventario.mobile.presentation.charts

import android.util.Log
import com.inventario.mobile.domain.repository.DashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provedor de dados para gráficos
 * Busca dados do backend via Repository
 */
@Singleton
class ChartDataProvider @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    
    companion object {
        private const val TAG = "ChartDataProvider"
    }

    /**
     * Busca dados de estado de conservação dos patrimônios coletados
     * Estados conforme legislação: BOM, OCIOSO, RECUPERÁVEL, ANTIECONÔMICO, IRRECUPERÁVEL
     */
    suspend fun getStatusData(idInventario: Int = 0): StatusData = withContext(Dispatchers.IO) {
        try {
            val inventarioIdParam = if (idInventario == 0) null else idInventario
            Log.d(TAG, "Buscando dados de estado de conservação para inventário $inventarioIdParam")
            
            val result = dashboardRepository.buscarEstatisticasPorStatus(inventarioIdParam)
            
            result.fold(
                onSuccess = { estatisticas ->
                    Log.d(TAG, "Estatísticas por estado recebidas: ${estatisticas.size} estados")
                    
                    // Mapear os estados de conservação conforme legislação
                    var bom = 0
                    var ocioso = 0
                    var recuperavel = 0
                    var antieconomico = 0
                    var irrecuperavel = 0
                    var semInfo = 0
                    
                    estatisticas.forEach { stat ->
                        when (stat.status.uppercase()) {
                            "BOM" -> bom += stat.quantidade
                            "OCIOSO" -> ocioso += stat.quantidade
                            "RECUPERAVEL", "RECUPERÁVEL" -> recuperavel += stat.quantidade
                            "ANTIECONOMICO", "ANTIECONÔMICO" -> antieconomico += stat.quantidade
                            "IRRECUPERAVEL", "IRRECUPERÁVEL" -> irrecuperavel += stat.quantidade
                            else -> semInfo += stat.quantidade
                        }
                    }
                    
                    Log.d(TAG, "Estados: Bom=$bom, Ocioso=$ocioso, Recuperável=$recuperavel, Antieconômico=$antieconomico, Irrecuperável=$irrecuperavel, Sem Info=$semInfo")
                    
                    StatusData(
                        bom = bom,
                        ocioso = ocioso,
                        recuperavel = recuperavel,
                        antieconomico = antieconomico,
                        irrecuperavel = irrecuperavel,
                        semInfo = semInfo
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao buscar estados de conservação", error)
                    StatusData(0, 0, 0, 0, 0, 0)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao buscar estados de conservação", e)
            StatusData(0, 0, 0, 0, 0, 0)
        }
    }

    /**
     * Busca dados de progresso da coleta do backend
     * Se idInventario for 0 ou null, busca do inventário ativo
     */
    suspend fun getProgressData(idInventario: Int): ProgressData = withContext(Dispatchers.IO) {
        try {
            // Se idInventario for 0, passar null para usar inventário ativo
            val inventarioIdParam = if (idInventario == 0) null else idInventario
            Log.d(TAG, "Buscando dados de progresso para inventário $inventarioIdParam (original: $idInventario)")
            
            val result = dashboardRepository.buscarEstatisticas(inventarioIdParam)
            
            result.fold(
                onSuccess = { stats ->
                    Log.d(TAG, "Dados recebidos: total=${stats.totalPatrimonios}, coletados=${stats.totalColetados}, pendentes=${stats.totalPendentes}")
                    
                    ProgressData(
                        total = stats.totalPatrimonios,
                        coletados = stats.totalColetados,
                        pendentes = stats.totalPendentes,
                        percentual = stats.percentualConclusao
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao buscar dados de progresso", error)
                    ProgressData(0, 0, 0, 0.0)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao buscar dados de progresso", e)
            ProgressData(0, 0, 0, 0.0)
        }
    }

    /**
     * Busca evolução diária das coletas do backend
     */
    suspend fun getEvolutionData(idInventario: Int): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando dados de evolução para inventário $idInventario")
            
            val result = dashboardRepository.buscarEvolucaoColetas(idInventario, 30)
            
            result.fold(
                onSuccess = { evolucaoList ->
                    Log.d(TAG, "Evolução recebida: ${evolucaoList.size} dias")
                    
                    // Converter para Map, filtrando nulls
                    evolucaoList
                        .filter { it.dataFormatada != null }
                        .associate { evolucao ->
                            evolucao.dataFormatada!! to evolucao.quantidade
                        }
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao buscar evolução", error)
                    emptyMap()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao buscar evolução", e)
            emptyMap()
        }
    }

    /**
     * Busca top 10 itens mais coletados
     * TODO: Implementar endpoint no backend
     */
    suspend fun getTopItemsData(idInventario: Int): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando top itens (mock)")
            
            // TODO: Buscar do backend quando endpoint estiver disponível
            // Por enquanto, retorna dados mockados
            emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar top itens", e)
            emptyMap()
        }
    }

    /**
     * Busca patrimônios por setor
     * TODO: Implementar endpoint no backend
     */
    suspend fun getPatrimoniosPorSetor(): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando patrimônios por setor (mock)")
            
            // TODO: Buscar do backend quando endpoint estiver disponível
            emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios por setor", e)
            emptyMap()
        }
    }

    /**
     * Busca estatísticas gerais do backend
     */
    suspend fun getGeneralStats(idInventario: Int): GeneralStats = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando estatísticas gerais para inventário $idInventario")
            
            val result = dashboardRepository.buscarEstatisticas(idInventario)
            
            result.fold(
                onSuccess = { stats ->
                    Log.d(TAG, "Estatísticas recebidas: ${stats.totalColetados}/${stats.totalPatrimonios}")
                    
                    GeneralStats(
                        totalPatrimonios = stats.totalPatrimonios,
                        totalColetados = stats.totalColetados,
                        totalPendentes = stats.totalPendentes,
                        percentualConcluido = stats.percentualConclusao,
                        ultimaAtualizacao = System.currentTimeMillis()
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao buscar estatísticas gerais", error)
                    GeneralStats(0, 0, 0, 0.0, System.currentTimeMillis())
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao buscar estatísticas gerais", e)
            GeneralStats(0, 0, 0, 0.0, System.currentTimeMillis())
        }
    }
}

/**
 * Data classes para dados dos gráficos
 */
data class StatusData(
    val bom: Int,
    val ocioso: Int,
    val recuperavel: Int,
    val antieconomico: Int,
    val irrecuperavel: Int,
    val semInfo: Int = 0
)

data class ProgressData(
    val total: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentual: Double
)

data class GeneralStats(
    val totalPatrimonios: Int,
    val totalColetados: Int,
    val totalPendentes: Int,
    val percentualConcluido: Double,
    val ultimaAtualizacao: Long
)
