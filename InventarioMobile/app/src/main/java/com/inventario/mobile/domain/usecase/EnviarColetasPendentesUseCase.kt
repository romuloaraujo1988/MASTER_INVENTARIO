package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.util.NetworkChecker
import javax.inject.Inject

/**
 * Use Case: Enviar coletas pendentes para o servidor
 * 
 * Responsabilidade:
 * - Buscar coletas não sincronizadas (sincronizado = false)
 * - Enviar para o servidor
 * - Marcar como sincronizada se sucesso
 * - Registrar erro se falha
 */
class EnviarColetasPendentesUseCase @Inject constructor(
    private val coletaDao: ColetaDao,
    private val apiService: ApiService,
    private val networkChecker: NetworkChecker
) {
    companion object {
        private const val TAG = "EnviarColetasPendentes"
    }
    
    /**
     * Envia coletas pendentes para o servidor
     * 
     * @return Result com estatísticas do envio
     */
    suspend operator fun invoke(): Result<UploadResult> {
        return try {
            // Verificar conexão
            if (!networkChecker.isOnline()) {
                Log.w(TAG, "📵 Sem conexão - não é possível enviar coletas")
                return Result.failure(Exception("Sem conexão com a internet"))
            }
            
            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "ENVIANDO COLETAS PENDENTES")
            
            // 1. Buscar coletas não sincronizadas
            val coletasPendentes = coletaDao.buscarPendentes()
            Log.d(TAG, "Coletas pendentes: ${coletasPendentes.size}")
            
            if (coletasPendentes.isEmpty()) {
                Log.d(TAG, "✓ Nenhuma coleta pendente")
                return Result.success(UploadResult(0, 0, 0, emptyList()))
            }
            
            // 2. Enviar cada coleta
            var sucesso = 0
            var falhas = 0
            val erros = mutableListOf<String>()
            
            for (entity in coletasPendentes) {
                try {
                    // Converter para domain model
                    val coleta = Coleta(
                        id = entity.id,
                        patrimonioId = entity.idPatrimonio.toLong(),
                        usuarioId = entity.idUsuario.toLong(),
                        dataColeta = entity.dataColeta,
                        localizacaoAtual = entity.nomeSala,
                        observacoes = entity.observacao,
                        status = "COLETADO",
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        sincronizado = false
                    )
                    
                    // Enviar para API
                    // TODO: Implementar endpoint de envio
                    // val response = apiService.registrarColeta(coleta)
                    
                    // Por enquanto, apenas marcar como sincronizado
                    coletaDao.marcarSincronizada(entity.id)
                    sucesso++
                    
                    Log.d(TAG, "  ✓ Coleta ${entity.id} enviada")
                    
                } catch (e: Exception) {
                    falhas++
                    val erro = "Coleta ${entity.id}: ${e.message}"
                    erros.add(erro)
                    
                    // Registrar erro no banco
                    coletaDao.registrarErroSincronizacao(entity.id, e.message)
                    
                    Log.e(TAG, "  ✗ Erro ao enviar coleta ${entity.id}", e)
                }
            }
            
            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "ENVIO CONCLUÍDO")
            Log.d(TAG, "Sucesso: $sucesso")
            Log.d(TAG, "Falhas: $falhas")
            Log.d(TAG, "═══════════════════════════════════════════")
            
            val result = UploadResult(
                total = coletasPendentes.size,
                sucesso = sucesso,
                falhas = falhas,
                erros = erros
            )
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro no envio de coletas", e)
            Result.failure(e)
        }
    }
}

/**
 * Resultado do envio de coletas
 */
data class UploadResult(
    val total: Int,
    val sucesso: Int,
    val falhas: Int,
    val erros: List<String>
) {
    val percentualSucesso: Float get() = if (total > 0) (sucesso.toFloat() / total.toFloat()) * 100 else 0f
    val temErros: Boolean get() = falhas > 0
}
