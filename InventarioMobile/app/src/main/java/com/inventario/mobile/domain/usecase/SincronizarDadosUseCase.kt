package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.repository.SyncRepository
import javax.inject.Inject

/**
 * Use Case: Sincronizar todos os dados do servidor
 * 
 * Regras de negócio:
 * - Baixa patrimônios, salas e responsáveis do servidor
 * - Substitui dados locais pelos dados do servidor
 * - Registra timestamp da sincronização
 * - Retorna estatísticas da sincronização
 */
class SincronizarDadosUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    /**
     * Executa sincronização completa de dados
     * 
     * v2.2: USA ENDPOINT OTIMIZADO (1 requisição ao invés de múltiplas)
     * 
     * @return Result com resultado da sincronização ou erro
     */
    suspend operator fun invoke(): Result<SyncRepository.SyncResult> {
        return try {
            // Tentar endpoint otimizado primeiro
            syncRepository.forceSyncFromServerOptimized()
        } catch (e: Exception) {
            android.util.Log.w("SincronizarDadosUseCase", "Endpoint otimizado falhou, usando método antigo", e)
            // Fallback para método antigo se o novo falhar
            syncRepository.forceSyncFromServer()
        }
    }
    
    /**
     * Verifica se há dados locais
     */
    suspend fun hasLocalData(): Boolean {
        return syncRepository.hasLocalData()
    }
    
    /**
     * Obtém estatísticas dos dados locais
     */
    suspend fun getLocalStats(): Map<String, Int> {
        return syncRepository.getLocalStats()
    }
}
