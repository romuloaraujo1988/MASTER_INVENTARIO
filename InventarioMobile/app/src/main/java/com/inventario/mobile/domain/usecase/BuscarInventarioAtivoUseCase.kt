package com.inventario.mobile.domain.usecase

import com.inventario.mobile.api.InventarioApi
import com.inventario.mobile.api.InventarioDTO
import javax.inject.Inject

/**
 * Use Case: Buscar inventário ativo
 * 
 * Regras de negócio:
 * - Busca o inventário com status "EM_ANDAMENTO"
 * - Retorna dados completos com estatísticas
 * - Salva ID do inventário ativo localmente
 */
class BuscarInventarioAtivoUseCase @Inject constructor(
    private val inventarioApi: InventarioApi
) {
    /**
     * Busca inventário ativo
     * 
     * @return Result com inventário ativo ou erro
     */
    suspend operator fun invoke(): Result<InventarioDTO> {
        return try {
            val response = inventarioApi.buscarInventarioAtivo()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val inventario = response.body()?.data
                
                if (inventario != null) {
                    Result.success(inventario)
                } else {
                    Result.failure(Exception("Inventário ativo não encontrado"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Erro ao buscar inventário ativo"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
