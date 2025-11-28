package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import com.inventario.mobile.data.local.LocalDataManager
import javax.inject.Inject

/**
 * Use Case: Registrar coleta por descrição (sem número de patrimônio)
 * Usado para coleta de itens sem etiqueta
 * 
 * O número do patrimônio será null e a descrição será salva no campo correspondente
 */
class RegistrarColetaPorDescricaoUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository,
    private val localDataManager: LocalDataManager
) {
    companion object {
        private const val TAG = "RegistrarColetaDescUC"
    }
    
    suspend operator fun invoke(
        descricao: String,
        salaId: Int,
        localizacaoAtual: String?,
        estadoEncontrado: String? = null
    ): Result<Coleta> {
        return try {
            // 1. Validar entrada
            if (descricao.isBlank()) {
                return Result.failure(Exception("Descrição é obrigatória"))
            }
            
            // 2. Obter usuário atual
            val usuarioAtual = localDataManager.getCurrentUser()
            if (usuarioAtual == null) {
                return Result.failure(Exception("[USUARIO_NAO_IDENTIFICADO] Usuário não está logado. Faça login novamente."))
            }
            
            Log.d(TAG, "✓ Usuário identificado: ${usuarioAtual.nome} (ID: ${usuarioAtual.id})")
            
            // 3. Criar coleta SEM número de patrimônio, apenas com descrição
            // IMPORTANTE: Marcar como semEtiqueta=true para que o servidor salve corretamente
            val coleta = Coleta(
                id = 0,
                patrimonioId = 0, // Sem patrimônio específico
                numeroPatrimonio = "", // Vazio - coleta por descrição
                descricaoPatrimonio = descricao, // Descrição do item
                usuarioId = usuarioAtual.id.toLong(),
                salaId = salaId,
                dataColeta = System.currentTimeMillis(),
                localizacaoAtual = localizacaoAtual,
                observacoes = "Coleta por descrição: $descricao",
                status = estadoEncontrado ?: "COLETADO",
                latitude = null,
                longitude = null,
                sincronizado = false,
                // v2.7: Campos de item sem etiqueta
                semEtiqueta = true,
                descricaoItemSemEtiqueta = descricao,
                categoriaItemSemEtiqueta = "COLETA_POR_DESCRICAO"
            )
            
            Log.d(TAG, "✓ Coleta por descrição criada: '$descricao', Usuário ${coleta.usuarioId}, Sala ${coleta.salaId}, semEtiqueta=true")
            
            // 4. Registrar coleta usando método específico para sem etiqueta
            coletaRepository.registrarColetaSemEtiqueta(coleta)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao registrar coleta por descrição", e)
            Result.failure(e)
        }
    }
}
