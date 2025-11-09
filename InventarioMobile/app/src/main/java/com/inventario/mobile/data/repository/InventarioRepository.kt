package com.inventario.mobile.data.repository

import android.app.Application
import android.content.Context
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.data.remote.api.ApiClient

/**
 * InventarioRepository stub - Mantido para compatibilidade temporária
 * TODO: Substituir por repositórios Clean Architecture específicos
 */
class InventarioRepository private constructor(
    private val apiService: ApiService,
    private val localDataManager: LocalDataManager
) {
    
    // Construtor alternativo para aceitar Application
    constructor(application: Application) : this(
        ApiClient.getApiService(application.applicationContext),
        LocalDataManager.getInstance(application.applicationContext)
    )
    
    companion object {
        @Volatile
        private var INSTANCE: InventarioRepository? = null
        
        fun getInstance(context: Context, apiService: ApiService): InventarioRepository {
            return INSTANCE ?: synchronized(this) {
                val localDataManager = LocalDataManager.getInstance(context)
                INSTANCE ?: InventarioRepository(apiService, localDataManager).also { INSTANCE = it }
            }
        }
    }
    
    // Métodos stub - retornam valores padrão ou lançam exceção
    suspend fun getAllPatrimoniosList(): List<Patrimonio> = emptyList()
    
    suspend fun getPatrimoniosColetados(): List<Patrimonio> = emptyList()
    
    suspend fun getPatrimoniosNaoColetados(): List<Patrimonio> = emptyList()
    
    suspend fun findPatrimonioByNumero(numero: String): Result<Patrimonio?> {
        return Result.success(null)
    }
    
    suspend fun coletarPatrimonio(
        patrimonio: Patrimonio,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> {
        return Result.failure(Exception("Método não implementado - use ColetaRepository"))
    }
    
    suspend fun coletarPatrimonioComSala(
        patrimonio: Patrimonio,
        salaNome: String,
        estadoEncontrado: String = "BOM",
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<Coleta> {
        return Result.failure(Exception("Método não implementado - use ColetaRepository"))
    }
    
    suspend fun getColetas(): List<Coleta> = emptyList()
    
    suspend fun getColetasPaginadas(page: Int = 0, size: Int = 20, useCache: Boolean = true): Result<PagedColetasResult> {
        return Result.success(PagedColetasResult(emptyList(), 0, 0, 0, 0, false, false))
    }
    
    suspend fun removeColeta(coletaId: Int): Result<Unit> {
        return Result.success(Unit)
    }
    
    suspend fun isPatrimonioColetado(patrimonioId: Long): Boolean = false
    
    suspend fun sincronizarTodosDados(): Int = 0
    
    suspend fun getCurrentUser(): com.inventario.mobile.data.model.Usuario? = null
    
    suspend fun obterInventarioAtivo(): Result<Int> = Result.success(1)
    
    suspend fun getDashboardStats(): com.inventario.mobile.presentation.dashboard.DashboardStats {
        return com.inventario.mobile.presentation.dashboard.DashboardStats(
            patrimoniosColetados = 0,
            patrimoniosPendentes = 0,
            divergencias = 0,
            coletoresAtivos = 0,
            totalPatrimonios = 0,
            percentualConcluido = 0f
        )
    }
}

/**
 * Resultado paginado de coletas
 */
data class PagedColetasResult(
    val coletas: List<Coleta>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
