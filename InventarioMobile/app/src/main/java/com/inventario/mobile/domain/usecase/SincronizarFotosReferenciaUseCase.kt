package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.domain.repository.FotoReferenciaRepository
import com.inventario.mobile.domain.repository.FotoReferenciaStats
import com.inventario.mobile.utils.PreferencesManager
import javax.inject.Inject

/**
 * Use Case para sincronização de fotos de referência
 * 
 * Orquestra a sincronização de fotos do servidor para o cache local.
 * Prioriza fotos do inventário ativo e gerencia o armazenamento.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
class SincronizarFotosReferenciaUseCase @Inject constructor(
    private val fotoReferenciaRepository: FotoReferenciaRepository,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        private const val TAG = "SincronizarFotosUC"
    }
    
    /**
     * Executa sincronização de fotos de referência
     * 
     * @param forcarCompleta Se true, força sincronização completa (ignora delta)
     * @return Result com estatísticas da sincronização
     */
    suspend operator fun invoke(forcarCompleta: Boolean = false): Result<SyncFotosResult> {
        return try {
            Log.d(TAG, "Iniciando sincronização de fotos (forçar completa: $forcarCompleta)")
            
            // Verificar se sincronização está habilitada
            if (!preferencesManager.isFotoReferenciaSyncEnabled()) {
                Log.d(TAG, "Sincronização de fotos desabilitada nas configurações")
                return Result.success(SyncFotosResult(
                    sincronizadas = 0,
                    mensagem = "Sincronização de fotos desabilitada"
                ))
            }
            
            // Verificar espaço disponível antes de sincronizar
            val limiteMB = preferencesManager.getFotoReferenciaStorageLimitMB()
            val tamanhoAtual = fotoReferenciaRepository.calcularTamanhoCache()
            val tamanhoAtualMB = tamanhoAtual / (1024 * 1024)
            
            Log.d(TAG, "Espaço usado: ${tamanhoAtualMB}MB / ${limiteMB}MB")
            
            // Se estiver próximo do limite, limpar fotos antigas primeiro
            if (tamanhoAtualMB > limiteMB * 0.9) {
                Log.d(TAG, "Cache próximo do limite, limpando fotos antigas...")
                val removidas = fotoReferenciaRepository.limparParaLiberarEspaco(limiteMB)
                Log.d(TAG, "$removidas fotos removidas para liberar espaço")
            }
            
            // Executar sincronização
            val resultado = if (forcarCompleta) {
                fotoReferenciaRepository.sincronizarCompleto()
            } else {
                fotoReferenciaRepository.sincronizar()
            }
            
            resultado.fold(
                onSuccess = { sincronizadas ->
                    Log.d(TAG, "✓ Sincronização concluída: $sincronizadas fotos")
                    
                    // Obter estatísticas atualizadas
                    val stats = fotoReferenciaRepository.obterEstatisticas()
                    
                    Result.success(SyncFotosResult(
                        sincronizadas = sincronizadas,
                        totalNoCache = stats.ativas,
                        tamanhoTotalBytes = stats.tamanhoTotalBytes,
                        mensagem = if (sincronizadas > 0) {
                            "$sincronizadas fotos sincronizadas"
                        } else {
                            "Nenhuma foto nova para sincronizar"
                        }
                    ))
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro na sincronização", error)
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado na sincronização de fotos", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se há atualizações de fotos disponíveis
     * 
     * @return true se há fotos novas/atualizadas no servidor
     */
    suspend fun verificarAtualizacoes(): Boolean {
        return try {
            fotoReferenciaRepository.verificarAtualizacoes()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar atualizações", e)
            false
        }
    }
    
    /**
     * Obtém estatísticas do cache de fotos
     * 
     * @return Estatísticas do cache
     */
    suspend fun obterEstatisticas(): FotoReferenciaStats {
        return fotoReferenciaRepository.obterEstatisticas()
    }
    
    /**
     * Limpa todo o cache de fotos
     */
    suspend fun limparCache() {
        Log.d(TAG, "Limpando cache de fotos...")
        fotoReferenciaRepository.limparCache()
        Log.d(TAG, "✓ Cache limpo")
    }
}

/**
 * Resultado da sincronização de fotos
 */
data class SyncFotosResult(
    val sincronizadas: Int,
    val totalNoCache: Int = 0,
    val tamanhoTotalBytes: Long = 0,
    val mensagem: String = ""
) {
    /**
     * Tamanho formatado em MB
     */
    val tamanhoFormatado: String
        get() = String.format("%.2f MB", tamanhoTotalBytes / (1024.0 * 1024.0))
    
    /**
     * Verifica se houve sincronização
     */
    val houveSincronizacao: Boolean
        get() = sincronizadas > 0
}
