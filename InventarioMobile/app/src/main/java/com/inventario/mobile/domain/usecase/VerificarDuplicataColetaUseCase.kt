package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.data.remote.api.VerificarDuplicataRequest
import javax.inject.Inject

/**
 * Use Case: Verificar se coleta seria duplicada
 * 
 * Regras de negócio:
 * - Verifica se patrimônio já foi coletado no inventário
 * - Retorna informações da coleta existente se duplicado
 * - Previne registro de coletas duplicadas
 */
class VerificarDuplicataColetaUseCase @Inject constructor(
    private val coletaApi: ColetaApi
) {
    /**
     * Verifica se uma coleta seria duplicada
     * 
     * @param numeroPatrimonio número do patrimônio
     * @param inventarioId ID do inventário (opcional, usa ativo se null)
     * @return Result com resultado da verificação
     */
    suspend operator fun invoke(
        numeroPatrimonio: String,
        inventarioId: Int? = null
    ): Result<DuplicataResult> {
        return try {
            if (numeroPatrimonio.isBlank()) {
                return Result.failure(Exception("Número do patrimônio não pode estar vazio"))
            }
            
            val request = VerificarDuplicataRequest(numeroPatrimonio, inventarioId)
            val response = coletaApi.verificarDuplicataColeta(request)
            
            if (response.success) {
                val data = response.data ?: emptyMap()
                val duplicado = data["duplicado"] as? Boolean ?: false
                val podeRegistrar = data["podeRegistrar"] as? Boolean ?: true
                
                if (duplicado) {
                    val coletaExistente = data["coletaExistente"] as? Map<String, Any>
                    Result.success(
                        DuplicataResult.Duplicado(
                            mensagem = data["mensagem"] as? String ?: "Coleta duplicada",
                            coletaExistente = coletaExistente
                        )
                    )
                } else if (!podeRegistrar) {
                    val motivo = data["motivo"] as? String ?: "UNKNOWN"
                    val mensagem = data["mensagem"] as? String ?: "Não pode registrar"
                    Result.success(DuplicataResult.NaoPodeRegistrar(motivo, mensagem))
                } else {
                    Result.success(DuplicataResult.PodeRegistrar)
                }
            } else {
                Result.failure(Exception(response.message ?: "Erro ao verificar duplicata"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Resultado da verificação de duplicata
 */
sealed class DuplicataResult {
    /**
     * Pode registrar a coleta (não é duplicada)
     */
    object PodeRegistrar : DuplicataResult()
    
    /**
     * Coleta duplicada - já existe no inventário
     * @param mensagem mensagem descritiva
     * @param coletaExistente dados da coleta existente
     */
    data class Duplicado(
        val mensagem: String,
        val coletaExistente: Map<String, Any>?
    ) : DuplicataResult()
    
    /**
     * Não pode registrar por outro motivo (patrimônio não encontrado, etc)
     * @param motivo código do motivo
     * @param mensagem mensagem descritiva
     */
    data class NaoPodeRegistrar(
        val motivo: String,
        val mensagem: String
    ) : DuplicataResult()
}
