package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case: Reenviar uma coleta específica
 * Limpa erro e tenta sincronizar novamente
 * 
 * Usado quando uma coleta pendente falhou e precisa ser reenviada
 */
class ReenviarColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(coletaId: Long): Result<Boolean> {
        return try {
            android.util.Log.d("ReenviarColetaUseCase", "🔄 Reenviando coleta $coletaId")
            
            // 1. Limpar erro da coleta para permitir nova tentativa
            coletaRepository.limparErroColeta(coletaId)
            
            // 2. Tentar sincronizar
            val sucesso = coletaRepository.sincronizarColetaEspecifica(coletaId)
            
            if (sucesso) {
                android.util.Log.d("ReenviarColetaUseCase", "✅ Coleta $coletaId reenviada com sucesso")
                Result.success(true)
            } else {
                android.util.Log.w("ReenviarColetaUseCase", "⚠️ Falha ao reenviar coleta $coletaId")
                Result.failure(Exception("Falha ao reenviar coleta"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ReenviarColetaUseCase", "❌ Erro ao reenviar coleta $coletaId", e)
            Result.failure(e)
        }
    }
}
