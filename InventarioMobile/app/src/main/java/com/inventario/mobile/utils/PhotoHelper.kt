package com.inventario.mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper para captura, compressão e organização de fotos de patrimônios.
 *
 * ## Estrutura de pastas no dispositivo
 *
 * ```
 * files/
 *   fotos/
 *     inventario_{id}/
 *       patrimonio/
 *         {coletaId}_{numeroPatrimonio}_{yyyyMMdd_HHmmss}.jpg
 *       sem_etiqueta/
 *         {coletaId}_SE_{yyyyMMdd_HHmmss}.jpg
 *       divergencia/
 *         {coletaId}_{numeroPatrimonio}_{yyyyMMdd_HHmmss}.jpg
 *   thumbnails/
 *     inventario_{id}/
 *       patrimonio/
 *         {coletaId}_{numeroPatrimonio}_{yyyyMMdd_HHmmss}.jpg
 *       sem_etiqueta/
 *         {coletaId}_SE_{yyyyMMdd_HHmmss}.jpg
 *       divergencia/
 *         {coletaId}_{numeroPatrimonio}_{yyyyMMdd_HHmmss}.jpg
 * ```
 *
 * - `coletaId` = ID local da `ColetaEntity` no Room (0 antes de inserir → usar timestamp como fallback)
 * - `numeroPatrimonio` = número do patrimônio ou `SE` para itens sem etiqueta
 * - Sempre `.jpg` após compressão
 *
 * ## Compressão
 * - Resolução máxima: 800×600
 * - Qualidade JPEG inicial: 65 %
 * - Redução progressiva até ≤ 100 KB
 * - Thumbnail: 200×200, qualidade 50 %
 *
 * @author Sistema de Inventário v2.22
 */
@Singleton
class PhotoHelper @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "PhotoHelper"

        // Compressão
        const val MAX_WIDTH = 800
        const val MAX_HEIGHT = 600
        const val JPEG_QUALITY = 65
        const val THUMBNAIL_SIZE = 200
        const val THUMBNAIL_QUALITY = 50
        const val MAX_FILE_SIZE = 100 * 1024   // 100 KB

        // Diretórios raiz
        private const val FOTOS_ROOT = "fotos"
        private const val THUMBNAILS_ROOT = "thumbnails"

        // Limite de fotos locais por inventário/tipo
        const val MAX_LOCAL_PHOTOS = 200

        /** Identificador usado no nome do arquivo para itens sem etiqueta. */
        const val ID_SEM_ETIQUETA = "SE"
    }

    // ─── Diretórios ──────────────────────────────────────────────────────────

    /** Retorna (criando se necessário) o diretório de fotos para o par inventário/tipo. */
    private fun fotosDir(inventarioId: Int, tipo: FotoTipo): File =
        File(context.filesDir, "$FOTOS_ROOT/inventario_$inventarioId/${tipo.pasta}").also {
            if (!it.exists()) it.mkdirs()
        }

    /** Retorna (criando se necessário) o diretório de thumbnails para o par inventário/tipo. */
    private fun thumbnailsDir(inventarioId: Int, tipo: FotoTipo): File =
        File(context.filesDir, "$THUMBNAILS_ROOT/inventario_$inventarioId/${tipo.pasta}").also {
            if (!it.exists()) it.mkdirs()
        }

    // ─── Nome de arquivo ─────────────────────────────────────────────────────

    /**
     * Gera o nome canônico do arquivo de foto.
     *
     * Formato: `{coletaId}_{identificador}_{yyyyMMdd_HHmmss}.jpg`
     *
     * @param coletaId          ID local da coleta (Room). Use 0 se ainda não inserida.
     * @param identificador     Número do patrimônio ou [ID_SEM_ETIQUETA] para sem etiqueta.
     * @param timestamp         Timestamp formatado `yyyyMMdd_HHmmss`.
     */
    fun nomeArquivo(coletaId: Long, identificador: String, timestamp: String): String =
        "${coletaId}_${identificador}_$timestamp.jpg"

    /** Timestamp atual no formato `yyyyMMdd_HHmmss`. */
    fun timestampAgora(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    // ─── Captura e compressão ────────────────────────────────────────────────

    /**
     * Comprime e salva foto capturada pela câmera.
     *
     * @param sourceUri      URI do arquivo temporário criado pelo FileProvider.
     * @param inventarioId   ID do inventário ativo.
     * @param tipo           Tipo da coleta ([FotoTipo]).
     * @param identificador  Número do patrimônio ou [ID_SEM_ETIQUETA].
     * @param coletaId       ID local da coleta (0 se ainda não inserida).
     * @return [PhotoResult] com caminhos absolutos, ou `null` em caso de falha.
     */
    fun compressAndSavePhoto(
        sourceUri: Uri,
        inventarioId: Int,
        tipo: FotoTipo,
        identificador: String,
        coletaId: Long = 0L
    ): PhotoResult? {
        return try {
            Log.d(TAG, "Comprimindo foto — inventario=$inventarioId tipo=${tipo.pasta} id=$identificador")

            // Carregar com sample size otimizado
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
            options.inJustDecodeBounds = false

            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: return null

            val resized = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)
            val timestamp = timestampAgora()
            val nome = nomeArquivo(coletaId, identificador, timestamp)

            val photoFile = File(fotosDir(inventarioId, tipo), nome)
            val fileSize = compressToFile(resized, photoFile)

            val thumbnailPath = createThumbnail(resized, inventarioId, tipo, coletaId, identificador, timestamp)

            if (resized != bitmap) resized.recycle()
            bitmap.recycle()

            Log.d(TAG, "✓ Foto salva: ${photoFile.absolutePath} (${fileSize / 1024} KB)")

            PhotoResult(
                fullPath = photoFile.absolutePath,
                thumbnailPath = thumbnailPath,
                sizeBytes = fileSize,
                inventarioId = inventarioId,
                tipo = tipo,
                identificador = identificador
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao comprimir foto", e)
            null
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemory ao processar foto", e)
            null
        }
    }

    /**
     * Sobrecarga de compatibilidade para código legado que não passa inventarioId/tipo.
     * Usa inventário 0 e tipo PATRIMONIO como fallback.
     */
    fun compressAndSavePhoto(sourceUri: Uri, identificador: String): PhotoResult? =
        compressAndSavePhoto(
            sourceUri = sourceUri,
            inventarioId = 0,
            tipo = if (identificador == ID_SEM_ETIQUETA) FotoTipo.SEM_ETIQUETA else FotoTipo.PATRIMONIO,
            identificador = identificador,
            coletaId = 0L
        )

    /**
     * Comprime bitmap diretamente (câmera sem FileProvider).
     */
    fun compressAndSaveBitmap(
        bitmap: Bitmap,
        inventarioId: Int,
        tipo: FotoTipo,
        identificador: String,
        coletaId: Long = 0L
    ): PhotoResult? {
        return try {
            val resized = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)
            val timestamp = timestampAgora()
            val nome = nomeArquivo(coletaId, identificador, timestamp)

            val photoFile = File(fotosDir(inventarioId, tipo), nome)
            val fileSize = compressToFile(resized, photoFile)

            val thumbnailPath = createThumbnail(resized, inventarioId, tipo, coletaId, identificador, timestamp)

            if (resized != bitmap) resized.recycle()

            PhotoResult(
                fullPath = photoFile.absolutePath,
                thumbnailPath = thumbnailPath,
                sizeBytes = fileSize,
                inventarioId = inventarioId,
                tipo = tipo,
                identificador = identificador
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao comprimir bitmap", e)
            null
        }
    }

    // ─── Utilitários ─────────────────────────────────────────────────────────

    fun photoToBase64(photoPath: String): String? = try {
        val file = File(photoPath)
        if (!file.exists()) null
        else Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao converter para Base64", e)
        null
    }

    fun base64ToPhoto(
        base64: String,
        inventarioId: Int,
        tipo: FotoTipo,
        identificador: String,
        coletaId: Long = 0L
    ): String? = try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val nome = nomeArquivo(coletaId, identificador, timestampAgora())
        val photoFile = File(fotosDir(inventarioId, tipo), nome)
        FileOutputStream(photoFile).use { it.write(bytes) }
        photoFile.absolutePath
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao salvar Base64", e)
        null
    }

    fun loadThumbnail(thumbnailPath: String?): Bitmap? {
        if (thumbnailPath == null) return null
        return try {
            val file = File(thumbnailPath)
            if (!file.exists()) null else BitmapFactory.decodeFile(thumbnailPath)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar thumbnail", e)
            null
        }
    }

    /**
     * Deleta foto e seu thumbnail correspondente.
     * Infere o diretório de thumbnail a partir do caminho da foto.
     */
    fun deletePhoto(photoPath: String?) {
        if (photoPath == null) return
        try {
            val photoFile = File(photoPath)
            if (photoFile.exists()) {
                photoFile.delete()
                Log.d(TAG, "Foto deletada: $photoPath")
            }
            // Thumbnail está no mesmo subdiretório, mas sob thumbnails/
            val thumbPath = photoPath.replace(
                "/$FOTOS_ROOT/",
                "/$THUMBNAILS_ROOT/"
            )
            val thumbFile = File(thumbPath)
            if (thumbFile.exists()) thumbFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar foto", e)
        }
    }

    /**
     * Remove fotos antigas mantendo apenas as [MAX_LOCAL_PHOTOS] mais recentes
     * por inventário/tipo. Só apaga fotos que já foram sincronizadas
     * (o chamador deve garantir isso passando apenas arquivos sincronizados).
     */
    fun cleanupOldPhotos() {
        try {
            val fotosRoot = File(context.filesDir, FOTOS_ROOT)
            if (!fotosRoot.exists()) return

            // Percorrer inventario_X/tipo/
            fotosRoot.listFiles()?.forEach { invDir ->
                invDir.listFiles()?.forEach { tipoDir ->
                    val fotos = tipoDir.listFiles()
                        ?.filter { it.isFile && it.extension == "jpg" }
                        ?.sortedByDescending { it.lastModified() }
                        ?: return@forEach

                    if (fotos.size > MAX_LOCAL_PHOTOS) {
                        fotos.drop(MAX_LOCAL_PHOTOS).forEach { file ->
                            file.delete()
                            Log.d(TAG, "Foto antiga removida: ${file.name}")
                        }
                    }
                }
            }

            cleanupOrphanThumbnails()
            Log.d(TAG, "✓ Limpeza de fotos concluída")
        } catch (e: Exception) {
            Log.e(TAG, "Erro na limpeza de fotos", e)
        }
    }

    fun getStorageStats(): PhotoStorageStats {
        val fotosRoot = File(context.filesDir, FOTOS_ROOT)
        val thumbsRoot = File(context.filesDir, THUMBNAILS_ROOT)

        fun countAndSize(root: File): Pair<Int, Long> {
            var count = 0; var size = 0L
            root.walkTopDown().filter { it.isFile }.forEach { count++; size += it.length() }
            return count to size
        }

        val (pc, ps) = if (fotosRoot.exists()) countAndSize(fotosRoot) else 0 to 0L
        val (tc, ts) = if (thumbsRoot.exists()) countAndSize(thumbsRoot) else 0 to 0L

        return PhotoStorageStats(pc, tc, ps + ts, ps, ts)
    }

    // ─── Privados ────────────────────────────────────────────────────────────

    /** Comprime bitmap para arquivo com qualidade decrescente até ≤ MAX_FILE_SIZE. */
    private fun compressToFile(bitmap: Bitmap, file: File): Long {
        var quality = JPEG_QUALITY
        var fileSize: Long
        do {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            fileSize = file.length()
            if (fileSize > MAX_FILE_SIZE && quality > 30) {
                quality -= 10
                Log.d(TAG, "Reduzindo qualidade para $quality% (${fileSize / 1024} KB)")
            }
        } while (fileSize > MAX_FILE_SIZE && quality > 30)
        return fileSize
    }

    private fun createThumbnail(
        bitmap: Bitmap,
        inventarioId: Int,
        tipo: FotoTipo,
        coletaId: Long,
        identificador: String,
        timestamp: String
    ): String? = try {
        val thumb = resizeBitmap(bitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE)
        val nome = nomeArquivo(coletaId, identificador, timestamp)
        val thumbFile = File(thumbnailsDir(inventarioId, tipo), nome)
        FileOutputStream(thumbFile).use { out ->
            thumb.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, out)
        }
        if (thumb != bitmap) thumb.recycle()
        thumbFile.absolutePath
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao criar thumbnail", e)
        null
    }

    private fun resizeBitmap(bitmap: Bitmap, maxW: Int, maxH: Int): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        if (w <= maxW && h <= maxH) return bitmap
        val ratio = minOf(maxW.toFloat() / w, maxH.toFloat() / h)
        return try {
            Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
        } catch (e: OutOfMemoryError) { bitmap }
    }

    private fun calculateInSampleSize(opts: BitmapFactory.Options, rW: Int, rH: Int): Int {
        val h = opts.outHeight; val w = opts.outWidth
        var s = 1
        if (h > rH || w > rW) {
            val hh = h / 2; val hw = w / 2
            while ((hh / s) >= rH && (hw / s) >= rW) s *= 2
        }
        return s
    }

    private fun cleanupOrphanThumbnails() {
        val fotosRoot = File(context.filesDir, FOTOS_ROOT)
        val thumbsRoot = File(context.filesDir, THUMBNAILS_ROOT)
        if (!fotosRoot.exists() || !thumbsRoot.exists()) return

        // Coletar todos os nomes de arquivo de fotos existentes
        val fotoNomes = mutableSetOf<String>()
        fotosRoot.walkTopDown().filter { it.isFile }.forEach { fotoNomes.add(it.name) }

        // Remover thumbnails cujo arquivo de foto não existe mais
        thumbsRoot.walkTopDown().filter { it.isFile }.forEach { thumb ->
            if (thumb.name !in fotoNomes) {
                thumb.delete()
                Log.d(TAG, "Thumbnail órfão removido: ${thumb.name}")
            }
        }
    }
}

// ─── Data classes ─────────────────────────────────────────────────────────────

/**
 * Resultado da compressão/salvamento de uma foto.
 */
data class PhotoResult(
    val fullPath: String,
    val thumbnailPath: String?,
    val sizeBytes: Long,
    val inventarioId: Int = 0,
    val tipo: FotoTipo = FotoTipo.PATRIMONIO,
    val identificador: String = ""
) {
    val sizeKB: Long get() = sizeBytes / 1024
}

/**
 * Estatísticas de armazenamento de fotos.
 */
data class PhotoStorageStats(
    val photoCount: Int,
    val thumbnailCount: Int,
    val totalSizeBytes: Long,
    val photosSizeBytes: Long,
    val thumbnailsSizeBytes: Long
) {
    val totalSizeMB: Double get() = totalSizeBytes / (1024.0 * 1024.0)
    val photosSizeMB: Double get() = photosSizeBytes / (1024.0 * 1024.0)
}
