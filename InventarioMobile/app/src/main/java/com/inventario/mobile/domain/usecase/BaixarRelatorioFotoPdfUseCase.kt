package com.inventario.mobile.domain.usecase

import android.content.Context
import com.inventario.mobile.data.remote.api.RelatorioFotoApi
import com.inventario.mobile.domain.model.PermissaoNegadaException
import com.inventario.mobile.domain.model.SemFotosException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Use Case: Baixar o relatório fotográfico de itens sem etiqueta em PDF.
 *
 * Chama o endpoint `GET api/mobile/relatorios/fotos/{inventarioId}?tipo=sem_etiqueta`,
 * grava os bytes recebidos em um arquivo temporário no cache do app e retorna
 * o [File] resultante.
 *
 * Requer role SUPERVISOR ou superior no servidor.
 *
 * Regras de negócio:
 * - HTTP 204 → [Result.failure] com [SemFotosException] (Req 5.3, 3.4).
 * - HTTP 403 → [Result.failure] com [PermissaoNegadaException] (Req 5.4, 3.5).
 * - Corpo nulo → [Result.failure] com mensagem genérica.
 * - Arquivo gravado com buffer de 8 KB para evitar [OutOfMemoryError]
 *   em PDFs grandes (Req 5.5).
 * - Nome do arquivo: `relatorio_sem_etiqueta_{inventarioId}_{timestamp}.pdf`
 *   dentro de [Context.getCacheDir], sobrescrevendo qualquer arquivo
 *   com o mesmo nome (Req 8.2).
 * - Toda a lógica é envolvida em try/catch; qualquer exceção não tratada
 *   é retornada como [Result.failure] (Req 5.2).
 *
 * @param api     Interface Retrofit para os endpoints de relatório fotográfico.
 * @param context Contexto da aplicação, usado para obter [Context.getCacheDir].
 *
 * Requirements: 5.2, 5.3, 5.4, 5.5, 3.2, 8.2
 */
class BaixarRelatorioFotoPdfUseCase @Inject constructor(
    private val api: RelatorioFotoApi,
    @ApplicationContext private val context: Context
) {
    /**
     * Executa o download do PDF para o inventário informado.
     *
     * @param inventarioId ID do inventário ativo.
     * @return [Result] contendo o [File] salvo em cache em caso de sucesso,
     *         ou uma exceção tipada ([SemFotosException], [PermissaoNegadaException])
     *         ou genérica em caso de falha.
     */
    suspend operator fun invoke(inventarioId: Int): Result<File> {
        return try {
            val response = api.baixarPdf(inventarioId)

            when (response.code()) {
                204 -> return Result.failure(SemFotosException())
                403 -> return Result.failure(PermissaoNegadaException())
            }

            val body = response.body()
                ?: return Result.failure(Exception("Resposta vazia do servidor"))

            val timestamp = System.currentTimeMillis()
            val arquivo = File(
                context.cacheDir,
                "relatorio_sem_etiqueta_${inventarioId}_${timestamp}.pdf"
            )

            // Gravar com buffer de 8 KB para evitar OOM em PDFs grandes (Req 5.5)
            arquivo.outputStream().buffered(8 * 1024).use { out ->
                body.byteStream().use { input ->
                    input.copyTo(out)
                }
            }

            Result.success(arquivo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
