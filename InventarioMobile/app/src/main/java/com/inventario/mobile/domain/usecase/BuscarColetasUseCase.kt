package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.remote.api.ApiService
import javax.inject.Inject

/**
 * Use Case: Buscar coletas do servidor
 * 
 * Responsabilidade: Buscar coletas do servidor com todos os campos preenchidos
 * 
 * MUDANÇA: Agora busca do servidor ao invés do banco local para garantir dados corretos
 */
class BuscarColetasUseCase @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "BuscarColetasUseCase"
    }
    
    /**
     * Busca todas as coletas do servidor com dados completos
     * 
     * @return Result com lista de coletas (data.model) ou erro
     */
    suspend operator fun invoke(): Result<List<Coleta>> {
        return try {
            Log.d(TAG, "Buscando coletas do servidor...")
            
            val response = apiService.buscarTodasColetasSemPaginacao()
            
            if (response.isSuccessful && response.body() != null) {
                val coletasAllResponse = response.body()!!
                
                if (coletasAllResponse.success && coletasAllResponse.data != null) {
                    val pagedData = coletasAllResponse.data
                    Log.d(TAG, "Resposta do servidor: ${pagedData.content.size} coletas de ${pagedData.totalElements} total")
                    
                    val coletas = pagedData.content.map { dto ->
                        Log.d(TAG, "═══════════════════════════════════════")
                        Log.d(TAG, "DTO Recebido - ID: ${dto.id}")
                        Log.d(TAG, "  numeroPatrimonio: '${dto.numeroPatrimonio}'")
                        Log.d(TAG, "  nomeSala: '${dto.nomeSala}'")
                        Log.d(TAG, "  localizacaoEncontrada: '${dto.localizacaoEncontrada}'")
                        Log.d(TAG, "  localizacaoAtual: '${dto.localizacaoAtual}'")
                        Log.d(TAG, "  observacoes: '${dto.observacoes}'")
                        Log.d(TAG, "═══════════════════════════════════════")
                        
                        Coleta(
                            id = dto.id?.toInt(),
                            patrimonioId = dto.patrimonioId,
                            numeroPatrimonio = dto.numeroPatrimonio,
                            descricaoPatrimonio = dto.descricaoPatrimonio,
                            usuarioId = dto.usuarioId,
                            nomeColetor = dto.nomeColetor,
                            dataColeta = dto.dataColeta ?: "",
                            nomeSala = dto.nomeSala,  // Localização ORIGINAL do patrimônio
                            localizacaoAtual = dto.nomeSala,  // Localização ORIGINAL do patrimônio
                            localizacaoEncontrada = dto.localizacaoEncontrada,  // Onde foi ENCONTRADO
                            observacoes = dto.observacoes,
                            sincronizado = true, // Dados do servidor são sempre sincronizados
                            estadoEncontrado = dto.estadoEncontrado,
                            status = dto.statusColeta
                        )
                    }
                    
                    Log.d(TAG, "✓ ${coletas.size} coletas carregadas do servidor")
                    Result.success(coletas)
                } else {
                    Log.w(TAG, "API retornou success=false ou data=null")
                    Result.failure(Exception("Erro ao buscar coletas do servidor"))
                }
            } else {
                Log.e(TAG, "Erro HTTP ${response.code()}")
                Result.failure(Exception("Erro HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar coletas", e)
            Result.failure(e)
        }
    }
}
