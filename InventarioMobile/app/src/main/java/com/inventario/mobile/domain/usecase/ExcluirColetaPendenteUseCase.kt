package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case: Excluir uma coleta pendente
 * 
 * Usado quando uma coleta não pode ser sincronizada e precisa ser removida
 * IMPORTANTE: Só permite excluir coletas NÃO sincronizadas
 */
class ExcluirColetaPendenteUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(coletaId: Long): Result<Unit> {
        return try {
            android.util.Log.d("ExcluirColetaPendenteUseCase", "🗑️ Excluindo coleta pendente $coletaId")
            
            // Verificar se coleta está pendente antes de excluir
            val coleta = coletaRepository.getColetaById(coletaId)
            
            if (coleta == null) {
                android.util.Log.e("ExcluirColetaPendenteUseCase", "❌ Coleta $coletaId não encontrada")
                return Result.failure(Exception("Coleta não encontrada"))
            }
            
            if (coleta.sincronizado) {
                android.util.Log.e("ExcluirColetaPendenteUseCase", "❌ Não é possível excluir coleta já sincronizada")
                return Result.failure(Exception("Não é possível excluir coleta já sincronizada"))
            }
            
            // Excluir coleta pendente
            coletaRepository.removerColetaPendente(coletaId)
            
            android.util.Log.d("ExcluirColetaPendenteUseCase", "✅ Coleta $coletaId excluída com sucesso")
            Result.success(Unit)
            
        } catch (e: Exception) {
            android.util.Log.e("ExcluirColetaPendenteUseCase", "❌ Erro ao excluir coleta $coletaId", e)
            Result.failure(e)
        }
    }
}
