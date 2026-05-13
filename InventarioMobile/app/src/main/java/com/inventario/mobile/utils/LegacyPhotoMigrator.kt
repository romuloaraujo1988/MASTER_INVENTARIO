package com.inventario.mobile.utils

import android.content.Context
import android.util.Log
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.entity.ColetaEntity
import com.inventario.mobile.utils.FotoTipo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Migra fotos de coletas do layout antigo (≤ v2.20) para o layout novo (v2.21+).
 *
 * ## Contexto
 *
 * CR-03 (correcoes-seguranca — maio/2026): a build #121 mudou a estrutura
 * de armazenamento de fotos no disco. Fotos criadas por versões anteriores
 * ficaram órfãs:
 *
 * **Layout antigo** (≤ v2.20):
 * ```
 * files/
 *   fotos/
 *     IMG_{timestamp}.jpg                          ← caminho plano
 *     coleta_{id}.jpg
 * ```
 *
 * **Layout novo** (v2.21+), gerado por [PhotoHelper]:
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
 * ```
 *
 * Sem migração, dois problemas aconteciam:
 * 1. `cleanupOldPhotos()` (novo código) só varre subpastas — arquivos no
 *    caminho plano nunca eram limpos, consumindo espaço indefinidamente.
 * 2. `PhotoSyncWorker` tenta abrir `File(fotoPath)` — se o *fotoPath* do
 *    banco apontava para o caminho antigo e o arquivo foi apagado por outra
 *    rotina, a foto era perdida silenciosamente.
 *
 * ## Estratégia
 *
 * - **Idempotente:** persiste flag em `SharedPreferences` após conclusão.
 * - **Best-effort:** falhas em arquivos individuais não interrompem o lote.
 * - **Preserva dados:** move arquivos (`renameTo`); se falhar, mantém o
 *   original e apenas atualiza o `fotoPath` no banco.
 * - **Rastreável:** registra o número de fotos migradas nos logs para
 *   auditoria.
 *
 * ## Chamada
 *
 * Invocar uma única vez em ponto de *startup* seguro — idealmente dentro de
 * um `LifecycleObserver` vinculado ao primeiro `MainActivity.onStart`, já
 * que o `Application.onCreate` não tem coroutine ready e o banco pode
 * ainda estar criptografado com chave que ainda não foi carregada.
 *
 * ```kotlin
 * legacyPhotoMigrator.migrateIfNeeded()
 * ```
 */
@Singleton
class LegacyPhotoMigrator @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val coletaDao: ColetaDao
) {

    companion object {
        private const val TAG = "LegacyPhotoMigrator"
        private const val PREFS_NAME = "legacy_photo_migration"
        private const val KEY_MIGRATED = "migrated_to_layout_v221"
        private const val FOTOS_ROOT = "fotos"
    }

    /**
     * Executa a migração se ainda não foi realizada.
     *
     * @return número de fotos migradas, ou 0 se a migração já havia rodado
     *         anteriormente.
     */
    suspend fun migrateIfNeeded(): Int = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_MIGRATED, false)) {
            Log.d(TAG, "Migração de fotos legacy já foi realizada, pulando")
            return@withContext 0
        }

        Log.i(TAG, "═══════════════════════════════════════")
        Log.i(TAG, "Iniciando migração de fotos legacy (v2.20 → v2.21+)")
        Log.i(TAG, "═══════════════════════════════════════")

        val fotosRoot = File(context.filesDir, FOTOS_ROOT)
        if (!fotosRoot.exists()) {
            Log.d(TAG, "Diretório de fotos não existe — nada a migrar")
            prefs.edit().putBoolean(KEY_MIGRATED, true).apply()
            return@withContext 0
        }

        var migradas = 0
        var falhas = 0
        var ignoradas = 0

        try {
            // Fotos no caminho plano (formato antigo): arquivos .jpg diretamente
            // dentro de files/fotos/. Pastas `inventario_{id}/` são do layout
            // novo e devem ser ignoradas.
            val arquivosLegacy = fotosRoot.listFiles { file ->
                file.isFile && file.extension.equals("jpg", ignoreCase = true)
            }.orEmpty()

            if (arquivosLegacy.isEmpty()) {
                Log.d(TAG, "Nenhum arquivo de foto em layout antigo")
            } else {
                Log.i(TAG, "Encontrados ${arquivosLegacy.size} arquivos em layout antigo")
            }

            for (arquivo in arquivosLegacy) {
                try {
                    val coleta = encontrarColetaPorFotoPath(arquivo.absolutePath)
                    if (coleta == null) {
                        // Órfão: arquivo não tem coleta correspondente no banco.
                        // Mover para pasta `orphan` para não perder evidência.
                        val orphanDir = File(fotosRoot, "legacy_orphan")
                        orphanDir.mkdirs()
                        val destino = File(orphanDir, arquivo.name)
                        if (arquivo.renameTo(destino)) {
                            Log.d(TAG, "Órfão movido para legacy_orphan/: ${arquivo.name}")
                        }
                        ignoradas++
                        continue
                    }

                    val inventarioId = coleta.idInventario
                    if (inventarioId <= 0) {
                        Log.w(TAG, "Coleta ${coleta.id} sem idInventario válido, pulando")
                        ignoradas++
                        continue
                    }

                    val tipo = FotoTipo.resolver(
                        semEtiqueta = coleta.semEtiqueta,
                        divergencia = false  // campo de divergência é derivado no fluxo novo
                    )

                    val destino = gerarNovoCaminho(inventarioId, tipo, coleta)
                    destino.parentFile?.mkdirs()

                    // renameTo é atômico no mesmo volume, mas pode falhar em
                    // storage particionado. Se falhar, caímos para copy+delete.
                    val moveOk = if (arquivo.renameTo(destino)) {
                        true
                    } else {
                        try {
                            arquivo.copyTo(destino, overwrite = true)
                            arquivo.delete()
                            true
                        } catch (e: Exception) {
                            Log.w(TAG, "Falha ao mover ${arquivo.name}", e)
                            false
                        }
                    }

                    if (moveOk) {
                        coletaDao.atualizarFoto(
                            id = coleta.id,
                            fotoPath = destino.absolutePath,
                            thumbnailPath = null,
                            motivo = coleta.motivoFoto
                        )
                        migradas++
                        Log.d(TAG, "✓ Foto migrada: ${arquivo.name} → ${destino.relativeTo(fotosRoot)}")
                    } else {
                        falhas++
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao migrar ${arquivo.name}", e)
                    falhas++
                }
            }

            prefs.edit().putBoolean(KEY_MIGRATED, true).apply()

            Log.i(TAG, "═══════════════════════════════════════")
            Log.i(TAG, "Migração de fotos legacy concluída")
            Log.i(TAG, "  Migradas: $migradas")
            Log.i(TAG, "  Falhas: $falhas")
            Log.i(TAG, "  Ignoradas (órfãs/inválidas): $ignoradas")
            Log.i(TAG, "═══════════════════════════════════════")

        } catch (e: Exception) {
            Log.e(TAG, "Falha catastrófica na migração de fotos legacy", e)
            // Não marcamos como feita para permitir nova tentativa no próximo
            // boot, mas o contador `falhas` já indica o escopo do problema.
        }

        migradas
    }

    /**
     * Encontra a coleta que referencia este arquivo de foto por [fotoPath].
     * Usa busca linear nas coletas com foto pendente — suficiente para o
     * volume esperado (< 200 fotos locais segundo [PhotoHelper.MAX_LOCAL_PHOTOS]).
     */
    private suspend fun encontrarColetaPorFotoPath(fotoPath: String): ColetaEntity? {
        val candidatas = coletaDao.buscarColetasComFotoPendente()
        // Comparar por caminho absoluto E por nome simples, para tolerar casos
        // em que o `fotoPath` salvo era relativo.
        val nomeArquivo = File(fotoPath).name
        return candidatas.firstOrNull { coleta ->
            val caminho = coleta.fotoPath ?: return@firstOrNull false
            caminho == fotoPath || File(caminho).name == nomeArquivo
        }
    }

    /**
     * Gera o caminho destino no layout novo, seguindo a convenção de
     * [PhotoHelper]: `files/fotos/inventario_{id}/{tipo}/{coletaId}_{numero}_{timestamp}.jpg`.
     */
    private fun gerarNovoCaminho(
        inventarioId: Int,
        tipo: FotoTipo,
        coleta: ColetaEntity
    ): File {
        val tipoDir = File(
            context.filesDir,
            "$FOTOS_ROOT/inventario_$inventarioId/${tipo.pasta}"
        )
        val identificador = when {
            coleta.semEtiqueta -> PhotoHelper.ID_SEM_ETIQUETA
            !coleta.numeroPatrimonio.isBlank() -> coleta.numeroPatrimonio
            else -> coleta.id.toString()
        }
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.ROOT)
            .format(java.util.Date(coleta.dataColeta))
        val nome = "${coleta.id}_${identificador}_${timestamp}.jpg"
        return File(tipoDir, nome)
    }
}
