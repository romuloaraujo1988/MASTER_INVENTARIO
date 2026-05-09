package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.utils.PreferencesManager
import javax.inject.Inject

/**
 * Use Case: Buscar coletas do servidor
 *
 * Responsabilidade: Buscar coletas do servidor com todos os campos preenchidos,
 * filtrando pelo inventário ativo.
 */
class BuscarColetasUseCase @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        private const val TAG = "BuscarColetasUseCase"
    }

    /**
     * Busca coletas do servidor filtradas pelo inventário ativo.
     *
     * @return Result com lista de coletas ou erro
     */
    suspend operator fun invoke(): Result<List<Coleta>> {
        return try {
            val inventarioId = preferencesManager.getInventarioAtivoId()
            Log.d(TAG, "Buscando coletas do servidor (inventárioId=$inventarioId)...")

            val response = apiService.buscarTodasColetasSemPaginacao(inventarioId)
            
            if (response.isSuccessful && response.body() != null) {
                val coletasAllResponse = response.body()!!
                
                if (coletasAllResponse.success && coletasAllResponse.data != null) {
                    val pagedData = coletasAllResponse.data
                    Log.d(TAG, "Resposta do servidor: ${pagedData.content.size} coletas de ${pagedData.totalElements} total")
                    
                    // Contar itens sem etiqueta para debug
                    val itensSemEtiqueta = pagedData.content.count { it.semEtiqueta == true }
                    Log.d(TAG, "📊 Itens sem etiqueta no servidor: $itensSemEtiqueta de ${pagedData.content.size}")
                    
                    val coletas = pagedData.content.map { dto ->
                        Log.d(TAG, "═══════════════════════════════════════")
                        Log.d(TAG, "DTO Recebido - ID: ${dto.id}")
                        Log.d(TAG, "  numeroPatrimonio: '${dto.numeroPatrimonio}'")
                        Log.d(TAG, "  nomeSala: '${dto.nomeSala}'")
                        Log.d(TAG, "  localizacaoEncontrada: '${dto.localizacaoEncontrada}'")
                        Log.d(TAG, "  localizacaoAtual: '${dto.localizacaoAtual}'")
                        Log.d(TAG, "  observacoes: '${dto.observacoes}'")
                        Log.d(TAG, "  semEtiqueta: ${dto.semEtiqueta}")
                        Log.d(TAG, "  descricaoItemSemEtiqueta: '${dto.descricaoItemSemEtiqueta}'")
                        Log.d(TAG, "  categoriaItemSemEtiqueta: '${dto.categoriaItemSemEtiqueta}'")
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
                            status = dto.statusColeta,
                            // Campos para itens sem etiqueta
                            semEtiqueta = dto.semEtiqueta ?: false,
                            descricaoItemSemEtiqueta = dto.descricaoItemSemEtiqueta,
                            categoriaItemSemEtiqueta = dto.categoriaItemSemEtiqueta
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
