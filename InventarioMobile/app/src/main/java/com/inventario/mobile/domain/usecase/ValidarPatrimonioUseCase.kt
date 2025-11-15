package com.inventario.mobile.domain.usecase

import com.inventario.mobile.api.PatrimonioApi
import javax.inject.Inject

/**
 * Use Case: Validar patrimônio antes de coletar
 * 
 * Regras de negócio:
 * - Verifica se patrimônio existe
 * - Verifica se está ativo
 * - Verifica se já foi coletado
 * - Retorna dados completos do patrimônio
 */
class ValidarPatrimonioUseCase @Inject constructor(
    private val patrimonioApi: PatrimonioApi
) {
    /**
     * Valida um número de patrimônio
     * 
     * @param numeroPatrimonio número do patrimônio a validar
     * @return Result com resultado da validação
     */
    suspend operator fun invoke(numeroPatrimonio: String): Result<ValidationResult> {
        return try {
            if (numeroPatrimonio.isBlank()) {
                return Result.failure(Exception("Número do patrimônio não pode estar vazio"))
            }
            
            val response = patrimonioApi.validarPatrimonio(numeroPatrimonio)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyMap()
                val valido = data["valido"] as? Boolean ?: false
                
                if (valido) {
                    val jaColetado = data["jaColetado"] as? Boolean ?: false
                    Result.success(ValidationResult.Valid(data, jaColetado))
                } else {
                    val motivo = data["motivo"] as? String ?: "UNKNOWN"
                    val mensagem = data["mensagem"] as? String ?: "Erro desconhecido"
                    Result.success(ValidationResult.Invalid(motivo, mensagem))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erro ao validar patrimônio"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Resultado da validação de patrimônio
 */
sealed class ValidationResult {
    /**
     * Patrimônio válido e pode ser coletado
     * @param data dados completos do patrimônio
     * @param jaColetado indica se já foi coletado anteriormente
     */
    data class Valid(
        val data: Map<String, Any>,
        val jaColetado: Boolean
    ) : ValidationResult()
    
    /**
     * Patrimônio inválido ou não pode ser coletado
     * @param motivo código do motivo (PATRIMONIO_NAO_ENCONTRADO, PATRIMONIO_INATIVO, etc)
     * @param mensagem mensagem descritiva do erro
     */
    data class Invalid(
        val motivo: String,
        val mensagem: String
    ) : ValidationResult()
}
