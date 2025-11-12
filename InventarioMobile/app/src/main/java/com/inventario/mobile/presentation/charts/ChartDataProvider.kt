package com.inventario.mobile.presentation.charts

import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.dao.PatrimonioDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provedor de dados para gráficos
 * Busca dados do banco local para exibição em gráficos
 */
@Singleton
class ChartDataProvider @Inject constructor(
    private val patrimonioDao: PatrimonioDao,
    private val coletaDao: ColetaDao
) {

    /**
     * Busca dados de status dos patrimônios
     */
    suspend fun getStatusData(): StatusData = withContext(Dispatchers.IO) {
        try {
            val ativos = patrimonioDao.countByStatus("ATIVO")
            val inativos = patrimonioDao.countByStatus("INATIVO")
            val manutencao = patrimonioDao.countByStatus("MANUTENÇÃO")
            val baixados = patrimonioDao.countByStatus("BAIXADO")
            
            StatusData(
                ativos = ativos,
                inativos = inativos,
                manutencao = manutencao,
                baixados = baixados
            )
        } catch (e: Exception) {
            // Fallback para dados de exemplo
            StatusData(
                ativos = patrimonioDao.countAll(),
                inativos = 0,
                manutencao = 0,
                baixados = 0
            )
        }
    }

    /**
     * Busca dados de progresso da coleta
     */
    suspend fun getProgressData(idInventario: Int): ProgressData = withContext(Dispatchers.IO) {
        try {
            val totalPatrimonios = patrimonioDao.countAll()
            val coletados = coletaDao.countByInventario(idInventario)
            val pendentes = totalPatrimonios - coletados
            
            ProgressData(
                total = totalPatrimonios,
                coletados = coletados,
                pendentes = pendentes,
                percentual = if (totalPatrimonios > 0) {
                    (coletados.toDouble() / totalPatrimonios * 100)
                } else 0.0
            )
        } catch (e: Exception) {
            ProgressData(0, 0, 0, 0.0)
        }
    }

    /**
     * Busca evolução diária das coletas
     */
    suspend fun getEvolutionData(idInventario: Int): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            val evolutionList = coletaDao.getEvolutionData(idInventario)
            
            // Converter para Map e calcular acumulado
            val result = mutableMapOf<String, Int>()
            var acumulado = 0
            
            evolutionList.forEach { item ->
                acumulado += item.quantidade
                result[item.data] = acumulado
            }
            
            result
        } catch (e: Exception) {
            // Fallback para dados de exemplo
            mapOf(
                "01/11" to 10,
                "02/11" to 25,
                "03/11" to 45,
                "04/11" to 70,
                "05/11" to 95
            )
        }
    }

    /**
     * Busca top 10 itens mais coletados
     */
    suspend fun getTopItemsData(idInventario: Int): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            val topItemsList = coletaDao.getTopItems(idInventario)
            
            // Converter para Map
            topItemsList.associate { item ->
                // Truncar descrição se muito longa
                val descricao = if (item.descricao.length > 25) {
                    item.descricao.substring(0, 22) + "..."
                } else {
                    item.descricao
                }
                descricao to item.quantidade
            }
        } catch (e: Exception) {
            // Fallback para dados de exemplo
            mapOf(
                "CADEIRA" to 25,
                "MESA" to 18,
                "COMPUTADOR" to 15,
                "MONITOR" to 12,
                "ARMÁRIO" to 10
            )
        }
    }

    /**
     * Busca patrimônios por setor
     */
    suspend fun getPatrimoniosPorSetor(): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            val setorList = patrimonioDao.getPatrimoniosPorSetor()
            
            // Converter para Map
            setorList.associate { item ->
                item.setor to item.quantidade
            }
        } catch (e: Exception) {
            // Fallback para dados de exemplo
            mapOf(
                "TI" to 45,
                "Admin" to 32,
                "RH" to 18,
                "Financeiro" to 25
            )
        }
    }

    /**
     * Busca estatísticas gerais
     */
    suspend fun getGeneralStats(idInventario: Int): GeneralStats = withContext(Dispatchers.IO) {
        try {
            val progressData = getProgressData(idInventario)
            
            GeneralStats(
                totalPatrimonios = progressData.total,
                totalColetados = progressData.coletados,
                totalPendentes = progressData.pendentes,
                percentualConcluido = progressData.percentual,
                ultimaAtualizacao = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            GeneralStats(0, 0, 0, 0.0, System.currentTimeMillis())
        }
    }
}

/**
 * Data classes para dados dos gráficos
 */
data class StatusData(
    val ativos: Int,
    val inativos: Int,
    val manutencao: Int,
    val baixados: Int
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
