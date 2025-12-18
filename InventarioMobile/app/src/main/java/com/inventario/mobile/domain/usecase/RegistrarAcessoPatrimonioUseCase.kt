package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.model.TipoAcesso
import com.inventario.mobile.domain.repository.HistoricoScanRepository
import javax.inject.Inject

/**
 * Use Case para registrar acesso a um patrimônio no histórico
 * 
 * Chamado automaticamente quando:
 * - Usuário escaneia QR Code
 * - Usuário busca patrimônio manualmente
 * - Usuário consulta detalhes de um patrimônio
 * 
 * @since v2.11.0
 */
class RegistrarAcessoPatrimonioUseCase @Inject constructor(
    private val historicoRepository: HistoricoScanRepository
) {
    
    /**
     * Registra acesso a um patrimônio
     * 
     * @param patrimonio Patrimônio acessado
     * @param tipoAcesso Tipo de acesso (QR, manual, consulta)
     * @return ID do registro criado
     */
    suspend operator fun invoke(
        patrimonio: Patrimonio,
        tipoAcesso: TipoAcesso
    ): Result<Long> {
        return try {
            val id = historicoRepository.registrarAcesso(
                numeroPatrimonio = patrimonio.numeroPatrimonio,
                descricao = patrimonio.descricao,
                nomeSala = patrimonio.nomeSala,
                salaId = patrimonio.idSala,
                tipoAcesso = tipoAcesso,
                estadoPatrimonio = patrimonio.estado,
                jaEstaColetado = patrimonio.coletado
            )
            Result.success(id)
        } catch (e: Exception) {
            android.util.Log.e("RegistrarAcessoUC", "Erro ao registrar acesso", e)
            Result.failure(e)
        }
    }
    
    /**
     * Registra acesso com dados básicos (quando não tem objeto Patrimonio completo)
     */
    suspend fun registrarBasico(
        numeroPatrimonio: String,
        descricao: String?,
        nomeSala: String?,
        salaId: Int?,
        tipoAcesso: TipoAcesso,
        jaColetado: Boolean = false
    ): Result<Long> {
        return try {
            val id = historicoRepository.registrarAcesso(
                numeroPatrimonio = numeroPatrimonio,
                descricao = descricao,
                nomeSala = nomeSala,
                salaId = salaId,
                tipoAcesso = tipoAcesso,
                estadoPatrimonio = null,
                jaEstaColetado = jaColetado
            )
            Result.success(id)
        } catch (e: Exception) {
            android.util.Log.e("RegistrarAcessoUC", "Erro ao registrar acesso básico", e)
            Result.failure(e)
        }
    }
}
