package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.remote.api.RelatorioFotoApi
import com.inventario.mobile.domain.model.RelatorioFotoInfo
import javax.inject.Inject

/**
 * Use Case: Buscar informações de fotos do relatório fotográfico.
 *
 * Chama o endpoint `GET api/mobile/relatorios/fotos/{inventarioId}/info`
 * e mapeia a resposta para o modelo de domínio [RelatorioFotoInfo].
 *
 * Requer role CONSULTA ou superior no servidor.
 *
 * Regras de negócio:
 * - Recebe o [inventarioId] como parâmetro e repassa à API.
 * - Mapeia os campos numéricos com cast seguro para evitar [ClassCastException]
 *   em respostas JSON onde números podem chegar como [Double] ou [Int].
 * - Retorna [Result.failure] se `response.success == false` ou em caso de exceção.
 *
 * @param api Interface Retrofit para os endpoints de relatório fotográfico.
 *
 * Requirements: 5.1
 */
class BuscarInfoRelatorioFotoUseCase @Inject constructor(
    private val api: RelatorioFotoApi
) {
    /**
     * Executa a busca de informações de fotos para o inventário informado.
     *
     * @param inventarioId ID do inventário ativo.
     * @return [Result] contendo [RelatorioFotoInfo] em caso de sucesso,
     *         ou uma exceção em caso de falha de rede ou resposta inválida.
     */
    suspend operator fun invoke(inventarioId: Int): Result<RelatorioFotoInfo> {
        return try {
            val response = api.buscarInfo(inventarioId)
            if (response.success && response.data != null) {
                val data = response.data
                Result.success(
                    RelatorioFotoInfo(
                        inventarioId  = inventarioId,
                        totalFotos    = (data["totalFotos"] as? Number)?.toInt() ?: 0,
                        semEtiqueta   = (data["semEtiqueta"] as? Number)?.toInt() ?: 0,
                        patrimonio    = (data["patrimonio"] as? Number)?.toInt() ?: 0,
                        divergencia   = (data["divergencia"] as? Number)?.toInt() ?: 0,
                        temFotos      = data["temFotos"] as? Boolean ?: false
                    )
                )
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
