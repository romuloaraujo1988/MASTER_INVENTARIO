package com.inventario.mobile.domain.usecase

import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.data.repository.PatrimonioRepositoryImpl
import com.inventario.mobile.domain.model.FiltroColetaSala
import com.inventario.mobile.data.model.Patrimonio
import javax.inject.Inject

/**
 * Use Case para buscar patrimônios de uma sala específica.
 * Suporta filtro por status de coleta e paginação.
 * Busca do servidor primeiro, com fallback para banco local.
 * 
 * Regra: Contém apenas lógica de negócio, sem dependências Android.
 */
class BuscarPatrimoniosPorSalaUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepositoryImpl,
    private val patrimonioApi: PatrimonioApi
) {
    
    companion object {
        private const val TAG = "BuscarPatrimoniosPorSalaUseCase"
    }
    
    /**
     * Busca patrimônios de uma sala com filtro e paginação.
     * Tenta buscar do servidor primeiro, com fallback para banco local.
     * 
     * @param salaId ID da sala
     * @param filtro Filtro de status de coleta (TODOS, COLETADOS, NAO_COLETADOS)
     * @param page Número da página (0-indexed)
     * @param pageSize Quantidade de itens por página
     * @return Result com lista de patrimônios ou erro
     */
    suspend operator fun invoke(
        salaId: Int,
        filtro: FiltroColetaSala = FiltroColetaSala.TODOS,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<Patrimonio>> {
        // Validar parâmetros
        if (salaId <= 0) {
            return Result.failure(IllegalArgumentException("ID da sala inválido"))
        }
        
        if (page < 0) {
            return Result.failure(IllegalArgumentException("Número da página inválido"))
        }
        
        if (pageSize <= 0 || pageSize > 100) {
            return Result.failure(IllegalArgumentException("Tamanho da página deve ser entre 1 e 100"))
        }
        
        // Converter filtro para booleano
        val coletado = filtro.toBoolean()
        
        // Tentar buscar do servidor primeiro
        try {
            android.util.Log.d(TAG, "Buscando patrimônios da sala $salaId do SERVIDOR...")
            val response = patrimonioApi.buscarPorSala(
                salaId = salaId,
                page = page,
                size = pageSize,
                coletado = coletado
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val patrimonios = response.body()?.data ?: emptyList()
                android.util.Log.d(TAG, "✓ ${patrimonios.size} patrimônios do servidor")
                return Result.success(patrimonios)
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Falha ao buscar do servidor, tentando local: ${e.message}")
        }
        
        // Fallback: buscar do banco local
        android.util.Log.d(TAG, "Buscando patrimônios da sala $salaId do banco LOCAL...")
        return patrimonioRepository.buscarPorSala(salaId, coletado, page, pageSize)
    }
    
    /**
     * Busca patrimônios de uma sala usando booleano diretamente.
     * 
     * @param salaId ID da sala
     * @param coletado null = todos, true = coletados, false = não coletados
     * @param page Número da página (0-indexed)
     * @param pageSize Quantidade de itens por página
     * @return Result com lista de patrimônios ou erro
     */
    suspend fun buscar(
        salaId: Int,
        coletado: Boolean? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<Patrimonio>> {
        // Tentar buscar do servidor primeiro
        try {
            val response = patrimonioApi.buscarPorSala(
                salaId = salaId,
                page = page,
                size = pageSize,
                coletado = coletado
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val patrimonios = response.body()?.data ?: emptyList()
                return Result.success(patrimonios)
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Falha ao buscar do servidor: ${e.message}")
        }
        
        // Fallback: buscar do banco local
        return patrimonioRepository.buscarPorSala(salaId, coletado, page, pageSize)
    }
}
