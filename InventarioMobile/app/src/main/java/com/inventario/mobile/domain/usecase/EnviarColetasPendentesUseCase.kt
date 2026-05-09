package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.data.remote.dto.MobileColetaRequest
import com.inventario.mobile.util.NetworkChecker
import javax.inject.Inject

/**
 * Use Case: Enviar coletas pendentes para o servidor
 *
 * Responsabilidade:
 * - Buscar coletas não sincronizadas (sincronizado = false)
 * - Enviar para o servidor via ColetaApi
 * - Marcar como sincronizada se sucesso
 * - Registrar erro se falha
 *
 * v2.0: Implementado envio real ao servidor (corrige Bug 2 — coletas eram marcadas
 * como sincronizadas sem serem enviadas)
 */
class EnviarColetasPendentesUseCase @Inject constructor(
    private val coletaDao: ColetaDao,
    private val coletaApi: ColetaApi,
    private val networkChecker: NetworkChecker
) {
    companion object {
        private const val TAG = "EnviarColetasPendentes"
    }

    /**
     * Envia coletas pendentes para o servidor
     *
     * @return Result com estatísticas do envio
     */
    suspend operator fun invoke(): Result<UploadResult> {
        return try {
            // Verificar conexão
            if (!networkChecker.isOnline()) {
                Log.w(TAG, "📵 Sem conexão - não é possível enviar coletas")
                return Result.failure(Exception("Sem conexão com a internet"))
            }

            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "ENVIANDO COLETAS PENDENTES")

            // 1. Buscar coletas não sincronizadas
            val coletasPendentes = coletaDao.buscarPendentes()
            Log.d(TAG, "Coletas pendentes: ${coletasPendentes.size}")

            if (coletasPendentes.isEmpty()) {
                Log.d(TAG, "✓ Nenhuma coleta pendente")
                return Result.success(UploadResult(0, 0, 0, emptyList()))
            }

            // 2. Enviar cada coleta individualmente
            var sucesso = 0
            var falhas = 0
            val erros = mutableListOf<String>()

            for (entity in coletasPendentes) {
                try {
                    // Validar idInventario antes de enviar — não usar fallback perigoso
                    val inventarioId = entity.idInventario.takeIf { it > 0 } ?: run {
                        Log.w(TAG, "  ⚠️ Coleta ${entity.id} ignorada: idInventario inválido (0)")
                        falhas++
                        val erro = "idInventario inválido (0) — coleta não pode ser enviada sem inventário definido"
                        erros.add("Coleta ${entity.id}: $erro")
                        coletaDao.registrarErroSincronizacao(entity.id, erro)
                        return@run null
                    } ?: continue

                    // Montar request com dados da entity
                    val request = MobileColetaRequest(
                        numeroPatrimonio = entity.numeroPatrimonio,
                        idInventario = inventarioId,
                        usuarioId = entity.idUsuario,
                        idSala = entity.idSala,
                        localizacaoEncontrada = entity.nomeSala,
                        estadoEncontrado = entity.estadoPatrimonio ?: "BOM",
                        observacaoColeta = entity.observacao,
                        dataColeta = java.text.SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(entity.dataColeta)),
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        fotoPatrimonio = null,
                        semEtiqueta = entity.semEtiqueta,
                        descricaoItemSemEtiqueta = entity.descricaoItemSemEtiqueta,
                        categoriaItemSemEtiqueta = entity.categoriaItemSemEtiqueta,
                        deviceId = android.os.Build.MODEL,
                        appVersion = "1.2",
                        divergencia = false,
                        motivoDivergencia = null,
                        tempoColetaSegundos = entity.tempoColetaSegundos,
                        tempoScanSegundos = entity.tempoScanSegundos,
                        tempoPreenchimentoSegundos = entity.tempoPreenchimentoSegundos,
                        metodoColeta = entity.metodoColeta,
                        tipoScan = entity.tipoScan
                    )

                    // Enviar para o servidor
                    val response = coletaApi.registrarColeta(request)

                    if (response.success) {
                        coletaDao.marcarSincronizada(entity.id)
                        sucesso++
                        Log.d(TAG, "  ✓ Coleta ${entity.id} (${entity.numeroPatrimonio}) enviada e marcada como sincronizada")
                    } else {
                        falhas++
                        val erro = response.message ?: "Servidor retornou success=false"
                        erros.add("Coleta ${entity.id} (${entity.numeroPatrimonio}): $erro")
                        coletaDao.registrarErroSincronizacao(entity.id, erro)
                        Log.w(TAG, "  ⚠️ Coleta ${entity.id} falhou: $erro")
                    }

                } catch (e: Exception) {
                    falhas++
                    val erro = e.message ?: "Erro de conexão desconhecido"
                    erros.add("Coleta ${entity.id}: $erro")
                    coletaDao.registrarErroSincronizacao(entity.id, e.message)
                    Log.e(TAG, "  ✗ Erro ao enviar coleta ${entity.id}", e)
                }
            }

            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "ENVIO CONCLUÍDO")
            Log.d(TAG, "Sucesso: $sucesso")
            Log.d(TAG, "Falhas: $falhas")
            Log.d(TAG, "═══════════════════════════════════════════")

            val result = UploadResult(
                total = coletasPendentes.size,
                sucesso = sucesso,
                falhas = falhas,
                erros = erros
            )

            Result.success(result)

        } catch (e: Exception) {
            Log.e(TAG, "Erro no envio de coletas", e)
            Result.failure(e)
        }
    }
}

/**
 * Resultado do envio de coletas
 */
data class UploadResult(
    val total: Int,
    val sucesso: Int,
    val falhas: Int,
    val erros: List<String>
) {
    val percentualSucesso: Float get() = if (total > 0) (sucesso.toFloat() / total.toFloat()) * 100 else 0f
    val temErros: Boolean get() = falhas > 0
}
