package com.inventario.mobile.domain.usecase

import com.inventario.mobile.api.PatrimonioApi
import javax.inject.Inject

/**
 * Use Case: Verificar se patrimônio já foi coletado
 * 
 * Regras de negócio:
 * - Verifica status de coleta no inventário
 * - Retorna informações completas da coleta se já foi coletado
 * - Útil para consultas rápidas de status
 */
class VerificarSePatrimonioFoiColetadoUseCase @Inject constructor(
    private val patrimonioApi: PatrimonioApi
) {
    /**
     * Verifica se patrimônio foi coletado
     * 
     * @param numeroPatrimonio número do patrimônio
     * @param inventarioId ID do inventário (opcional, usa ativo se null)
     * @return Result com informações da coleta
     */
    suspend operator fun invoke(
        numeroPatrimonio: String,
        inventarioId: Int? = null
    ): Result<ColetaInfo> {
        return try {
            if (numeroPatrimonio.isBlank()) {
                return Result.failure(Exception("Número do patrimônio não pode estar vazio"))
            }
            
            val response = patrimonioApi.verificarSePatrimonioFoiColetado(
                numeroPatrimonio,
                inventarioId
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyMap()
                val coletado = data["coletado"] as? Boolean ?: false
                
                if (coletado) {
                    Result.success(
                        ColetaInfo.Coletado(
                            numeroPatrimonio = data["numeroPatrimonio"] as? String ?: numeroPatrimonio,
                            patrimonioId = (data["patrimonioId"] as? Number)?.toInt() ?: 0,
                            inventarioId = (data["inventarioId"] as? Number)?.toInt() ?: 0,
                            inventarioNome = data["inventarioNome"] as? String,
                            dataColeta = data["dataColeta"] as? String,
                            coletadoPor = data["coletadoPor"] as? String,
                            coletaId = (data["coletaId"] as? Number)?.toInt(),
                            observacoes = data["observacoes"] as? String,
                            localizacaoEncontrada = data["localizacaoEncontrada"] as? String,
                            estadoEncontrado = data["estadoEncontrado"] as? String
                        )
                    )
                } else {
                    Result.success(
                        ColetaInfo.NaoColetado(
                            numeroPatrimonio = data["numeroPatrimonio"] as? String ?: numeroPatrimonio,
                            patrimonioId = (data["patrimonioId"] as? Number)?.toInt() ?: 0,
                            inventarioId = (data["inventarioId"] as? Number)?.toInt() ?: 0,
                            inventarioNome = data["inventarioNome"] as? String
                        )
                    )
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erro ao verificar coleta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Informações sobre coleta de patrimônio
 */
sealed class ColetaInfo {
    /**
     * Patrimônio já foi coletado
     */
    data class Coletado(
        val numeroPatrimonio: String,
        val patrimonioId: Int,
        val inventarioId: Int,
        val inventarioNome: String?,
        val dataColeta: String?,
        val coletadoPor: String?,
        val coletaId: Int?,
        val observacoes: String?,
        val localizacaoEncontrada: String?,
        val estadoEncontrado: String?
    ) : ColetaInfo()
    
    /**
     * Patrimônio ainda não foi coletado
     */
    data class NaoColetado(
        val numeroPatrimonio: String,
        val patrimonioId: Int,
        val inventarioId: Int,
        val inventarioNome: String?
    ) : ColetaInfo()
}
