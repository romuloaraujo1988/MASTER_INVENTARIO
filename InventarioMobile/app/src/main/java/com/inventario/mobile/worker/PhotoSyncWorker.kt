package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.remote.api.FotoColetaApi
import com.inventario.mobile.utils.PhotoHelper
import com.inventario.mobile.utils.PreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronização de fotos de coleta em background.
 *
 * Estratégia:
 * - Executa apenas em Wi-Fi (via `Constraints.setRequiredNetworkType(UNMETERED)`).
 * - Bateria > 20%.
 * - Sincroniza apenas fotos de coletas que **já têm `servidorId`** (o servidor
 *   só aceita fotos referenciando uma coleta existente no banco).
 * - Envia a foto original (arquivo em disco) via multipart/form-data.
 * - Marca `fotoSincronizada = 1` no banco local APENAS se o servidor
 *   respondeu `success=true`.
 * - Limpa fotos antigas após sync bem-sucedido.
 *
 * BUGFIX F6 (07/05/2026): implementação real do upload. A versão anterior tinha
 * apenas `// TODO: Enviar para servidor via API` e marcava a foto como sincronizada
 * localmente sem enviar nada, causando PERDA SILENCIOSA de todas as fotos de
 * itens sem etiqueta após o `cleanupOldPhotos()` remover os arquivos locais.
 *
 * @author Sistema de Inventário v2.11 + bugfix v2.20.2
 */
@HiltWorker
class PhotoSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val coletaDao: ColetaDao,
    private val photoHelper: PhotoHelper,
    private val preferencesManager: PreferencesManager,
    private val fotoColetaApi: FotoColetaApi
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "PhotoSyncWorker"
        private const val WORK_NAME = "photo_sync_work"

        /**
         * Agenda sincronização periódica de fotos.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // Apenas Wi-Fi
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<PhotoSyncWorker>(
                6, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

            Log.d(TAG, "✓ Sincronização de fotos agendada (a cada 6h, apenas Wi-Fi)")
        }

        /**
         * Cancela sincronização periódica.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Sincronização de fotos cancelada")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "INICIANDO SINCRONIZAÇÃO DE FOTOS")
        Log.d(TAG, "═══════════════════════════════════════")

        return try {
            val coletasComFoto = coletaDao.buscarColetasComFotoPendente()

            if (coletasComFoto.isEmpty()) {
                Log.d(TAG, "✓ Nenhuma foto pendente para sincronizar")
                photoHelper.cleanupOldPhotos()
                return Result.success()
            }

            Log.d(TAG, "📷 ${coletasComFoto.size} fotos pendentes para sincronizar")

            var syncCount = 0
            var skipCount = 0
            var errorCount = 0

            for (coleta in coletasComFoto) {
                val fotoPath = coleta.fotoPath
                if (fotoPath.isNullOrBlank()) {
                    Log.w(TAG, "⚠️ Coleta ${coleta.id} sem fotoPath, pulando")
                    continue
                }

                // BUGFIX F6: só enviar foto se a coleta já foi sincronizada e tem
                // `servidorId`. O endpoint de upload referencia uma coleta existente
                // pelo ID do servidor; enviar antes da coleta sincronizar geraria
                // erro "coleta não encontrada".
                val servidorId = coleta.servidorId
                if (!coleta.sincronizado || servidorId == null || servidorId <= 0) {
                    Log.d(
                        TAG,
                        "⏭️ Coleta ${coleta.id} ainda não sincronizada (servidorId=$servidorId), " +
                                "pulando upload de foto para aguardar sync principal"
                    )
                    skipCount++
                    continue
                }

                val inventarioId = coleta.idInventario
                if (inventarioId <= 0) {
                    Log.w(TAG, "⚠️ Coleta ${coleta.id} sem idInventario válido, pulando")
                    skipCount++
                    continue
                }

                try {
                    val file = File(fotoPath)
                    if (!file.exists() || !file.canRead()) {
                        Log.w(TAG, "⚠️ Arquivo de foto não encontrado ou ilegível: $fotoPath")
                        errorCount++
                        continue
                    }

                    val sucessoUpload = uploadFoto(servidorId.toInt(), inventarioId, file)

                    if (sucessoUpload) {
                        coletaDao.marcarFotoSincronizada(coleta.id)
                        syncCount++
                        Log.d(TAG, "✓ Foto sincronizada: coleta local=${coleta.id} servidor=$servidorId")
                    } else {
                        Log.w(TAG, "⚠️ Upload falhou para coleta ${coleta.id}, tentar novamente depois")
                        errorCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao sincronizar foto da coleta ${coleta.id}", e)
                    errorCount++
                }
            }

            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "SINCRONIZAÇÃO DE FOTOS CONCLUÍDA")
            Log.d(TAG, "  Sincronizadas: $syncCount")
            Log.d(TAG, "  Adiadas (aguardando sync da coleta): $skipCount")
            Log.d(TAG, "  Erros: $errorCount")
            Log.d(TAG, "═══════════════════════════════════════")

            // BUGFIX F6: limpar fotos antigas APENAS se pelo menos uma foto foi
            // efetivamente enviada — evita apagar arquivos cujo upload falhou.
            if (syncCount > 0) {
                photoHelper.cleanupOldPhotos()
            }

            when {
                errorCount > 0 && syncCount == 0 -> Result.retry()
                errorCount > 0 -> Result.retry() // retry para completar as que falharam
                else -> Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização de fotos", e)
            Result.retry()
        }
    }

    /**
     * Envia a foto via multipart/form-data para o endpoint
     * `POST /api/mobile/fotos/upload`.
     *
     * @return `true` se o servidor respondeu `success = true`; `false` caso contrário
     *         (falha de rede, resposta negativa, exceção).
     */
    private suspend fun uploadFoto(coletaServidorId: Int, inventarioId: Int, file: File): Boolean {
        return try {
            val mediaType = when (file.extension.lowercase()) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }.toMediaTypeOrNull()

            val requestFile: RequestBody = file.asRequestBody(mediaType)
            val fotoPart = MultipartBody.Part.createFormData(
                name = "foto",
                filename = file.name,
                body = requestFile
            )

            val textMedia = "text/plain".toMediaTypeOrNull()
            val coletaIdBody = coletaServidorId.toString().toRequestBody(textMedia)
            val inventarioIdBody = inventarioId.toString().toRequestBody(textMedia)

            val response = fotoColetaApi.uploadFoto(
                foto = fotoPart,
                coletaId = coletaIdBody,
                inventarioId = inventarioIdBody
            )

            if (response.success) {
                Log.d(
                    TAG,
                    "✓ Servidor aceitou foto (coletaId=$coletaServidorId, tamanho=${file.length() / 1024}KB)"
                )
                true
            } else {
                Log.w(TAG, "⚠ Servidor rejeitou foto: ${response.message} (code=${response.errorCode})")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro no upload da foto (coletaId=$coletaServidorId)", e)
            false
        }
    }
}
