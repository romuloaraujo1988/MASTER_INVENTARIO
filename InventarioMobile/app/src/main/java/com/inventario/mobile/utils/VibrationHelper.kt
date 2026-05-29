package com.inventario.mobile.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.PowerManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper para gerenciar vibração do dispositivo.
 *
 * ### Por que vibração "simples" não funciona em vários cenários
 * Desde Android 8 (Oreo), `Vibrator.vibrate(VibrationEffect)` **sem
 * `VibrationAttributes`/`AudioAttributes`** é silenciada quando:
 *
 * - O telefone está em **Modo Silencioso** ou **Não Perturbe**
 * - O **Modo Economia de Bateria** está ativo
 * - O app está em **background** (a partir do Android 12 com restrição mais
 *   estrita)
 * - O fabricante adiciona políticas próprias (Xiaomi, Samsung, OnePlus)
 *
 * A solução oficial é declarar a "usage" da vibração:
 *
 * - **API 33+ (Tiramisu)**: usar [VibrationAttributes] com
 *   [VibrationAttributes.USAGE_HARDWARE_FEEDBACK] — feedback de uma ação que
 *   o usuário acabou de executar (coleta confirmada). Bypassa DND.
 * - **API 26-32**: usar [AudioAttributes] com
 *   [AudioAttributes.USAGE_ASSISTANCE_SONIFICATION] e
 *   [AudioAttributes.CONTENT_TYPE_SONIFICATION] — equivalente legado, bypassa
 *   o ringer mode silencioso.
 * - **API < 26**: API antiga não tinha esse problema; vibração funciona
 *   diretamente.
 *
 * ### Nuances adicionais
 * - `hasAmplitudeControl()` é falso em muitos aparelhos antigos — passar
 *   amplitude 255 nesses casos faz a vibração não tocar nada. Detectamos e
 *   caímos para [VibrationEffect.DEFAULT_AMPLITUDE].
 * - Em Power Save Mode, mesmo com tudo certo, alguns ROMs cortam a vibração.
 *   [isPowerSaveMode] permite a UI avisar o usuário.
 */
@Singleton
class VibrationHelper @Inject constructor(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {

    companion object {
        private const val TAG = "VibrationHelper"

        // Padrão de coleta: dois pulsos curtos com pausa — feedback distinto
        // de notificação genérica do sistema.
        private val PADRAO_COLETA = longArrayOf(0, 120, 80, 120)
        private const val DURACAO_CURTA = 120L
        private const val DURACAO_MEDIA = 200L
        private const val DURACAO_LONGA = 400L
        private const val AMPLITUDE_FORTE = 255
    }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /** AudioAttributes para API 26-32 — contorna ringer silencioso/DND. */
    private val audioAttributesLegacy: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    /** VibrationAttributes para API 33+ — explicita "feedback de ação do usuário". */
    @get:androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val vibrationAttributesModern: VibrationAttributes by lazy {
        VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_HARDWARE_FEEDBACK)
            .build()
    }

    fun isVibrationEnabled(): Boolean = preferencesManager.isVibrationOnCollectionEnabled()

    fun hasVibrator(): Boolean = vibrator?.hasVibrator() == true

    /**
     * Indica se o dispositivo está em Power Save Mode — alguns ROMs
     * silenciam vibrações neste estado. Usar para mostrar um aviso na UI.
     */
    fun isPowerSaveMode(): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isPowerSaveMode == true
        } catch (e: Exception) {
            false
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Vibração ao coletar um patrimônio (curta, padrão duplo).
     * Respeita a preferência do usuário em [PreferencesManager].
     */
    fun vibrateOnCollection() {
        if (!isVibrationEnabled()) {
            android.util.Log.d(TAG, "Vibração desabilitada — pulando")
            return
        }
        executarPadraoColeta()
    }

    /**
     * Vibração de sucesso — equivalente a [vibrateOnCollection] mas mantida
     * separada para uso em fluxos que não querem checar a preferência
     * (ex.: chamadas diretas no scanner).
     */
    fun vibrateSuccess() {
        if (!isVibrationEnabled()) {
            android.util.Log.d(TAG, "Vibração desabilitada — pulando vibrateSuccess")
            return
        }
        executarPadraoColeta()
    }

    fun vibrateShort() = vibrateOneShot(DURACAO_CURTA)
    fun vibrateMedium() = vibrateOneShot(DURACAO_MEDIA)
    fun vibrateLong() = vibrateOneShot(DURACAO_LONGA)

    fun vibrateError() {
        if (!isVibrationEnabled()) return
        vibrateOneShot(DURACAO_LONGA)
    }

    /**
     * Versão "test fire" — ignora a preferência e vibra uma vez para o
     * usuário confirmar nas configurações que o feedback funciona no
     * dispositivo. Retorna `true` se conseguiu disparar a vibração.
     */
    fun testarVibracao(): Boolean {
        val v = vibrator
        if (v == null || !v.hasVibrator()) {
            android.util.Log.w(TAG, "Dispositivo sem vibrador — testarVibracao() falhou")
            return false
        }
        executarPadraoColeta()
        return true
    }

    fun cancel() {
        vibrator?.cancel()
    }

    // ─────────────────────────────────────────────────────────────────────
    // Execução de fato
    // ─────────────────────────────────────────────────────────────────────

    /** Vibração padrão de "coleta confirmada" — dois pulsos curtos. */
    private fun executarPadraoColeta() {
        val v = vibrator
        if (v == null || !v.hasVibrator()) {
            android.util.Log.w(TAG, "Dispositivo não suporta vibração")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Amplitude máxima só funciona se o vibrador suporta. Caso
                // contrário, devolve [VibrationEffect.DEFAULT_AMPLITUDE] que
                // ainda toca alguma vibração (em vez de nada).
                val amplitudes = if (v.hasAmplitudeControl()) {
                    intArrayOf(0, AMPLITUDE_FORTE, 0, AMPLITUDE_FORTE)
                } else {
                    intArrayOf(
                        0,
                        VibrationEffect.DEFAULT_AMPLITUDE,
                        0,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                }
                val effect = VibrationEffect.createWaveform(PADRAO_COLETA, amplitudes, -1)
                disparar(v, effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(PADRAO_COLETA, -1)
            }
            android.util.Log.d(TAG, "✓ Padrão de coleta disparado (powerSave=${isPowerSaveMode()})")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Falha ao executar vibração de coleta", e)
        }
    }

    /** Vibração simples de duração N. */
    private fun vibrateOneShot(duracao: Long) {
        val v = vibrator
        if (v == null || !v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = if (v.hasAmplitudeControl()) AMPLITUDE_FORTE
                                else VibrationEffect.DEFAULT_AMPLITUDE
                val effect = VibrationEffect.createOneShot(duracao, amplitude)
                disparar(v, effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(duracao)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Falha em vibrateOneShot($duracao)", e)
        }
    }

    /**
     * Dispara o effect com os atributos corretos de "usage" para garantir
     * que o sistema não silencie a vibração em DND/silent/power save.
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private fun disparar(v: Vibrator, effect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ — VibrationAttributes.USAGE_HARDWARE_FEEDBACK
            v.vibrate(effect, vibrationAttributesModern)
        } else {
            // API 26-32 — AudioAttributes legado
            v.vibrate(effect, audioAttributesLegacy)
        }
    }
}
