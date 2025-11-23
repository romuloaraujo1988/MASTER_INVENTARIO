package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.repository.PatrimonioRepository
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Use Case: Buscar patrimônio por número
 * 
 * v2.0: Fallback automático para dados locais quando servidor inacessível
 */
class BuscarPatrimonioUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository
) {
    
    companion object {
        private const val TAG = "BuscarPatrimonioUseCase"
    }
    
    suspend operator fun invoke(numero: String): Result<Patrimonio> {
        return try {
            if (numero.isBlank()) {
                return Result.failure(Exception("Número do patrimônio é obrigatório"))
            }
            
            Log.d(TAG, "Buscando patrimônio: $numero")
            
            // Tentar buscar (repository já tem estratégia offline-first)
            val patrimonio = patrimonioRepository.buscarPorNumero(numero)
            
            if (patrimonio != null) {
                Log.d(TAG, "✓ Patrimônio encontrado: ${patrimonio.descricao}")
                Result.success(patrimonio)
            } else {
                Log.w(TAG, "✗ Patrimônio não encontrado")
                Result.failure(Exception("Patrimônio não encontrado"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônio", e)
            
            // Verificar se é erro de rede
            val isNetworkError = e is UnknownHostException || 
                                e is SocketTimeoutException ||
                                e.message?.contains("failed to connect", ignoreCase = true) == true
            
            if (isNetworkError) {
                Log.w(TAG, "⚠️ Erro de rede - o repositório já deve ter tentado fallback local")
                Result.failure(Exception("Patrimônio não encontrado (offline)"))
            } else {
                Result.failure(e)
            }
        }
    }
}
