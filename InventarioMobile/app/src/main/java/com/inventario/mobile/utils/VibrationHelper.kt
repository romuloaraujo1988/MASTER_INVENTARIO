package com.inventario.mobile.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper para gerenciar vibração do dispositivo
 * Usado para feedback tátil ao coletar patrimônios
 * 
 * ✅ v2.20: Vibração com PRIORIDADE MÁXIMA e amplitude forte
 */
@Singleton
class VibrationHelper @Inject constructor(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        // Duração da vibração em milissegundos
        private const val VIBRATION_DURATION_SHORT = 120L   // ✅ Aumentado de 80ms para 120ms
        private const val VIBRATION_DURATION_MEDIUM = 200L  // ✅ Aumentado de 150ms para 200ms
        private const val VIBRATION_DURATION_LONG = 400L    // ✅ Aumentado de 300ms para 400ms
        
        // ✅ AMPLITUDE MÁXIMA (255) para vibração forte e perceptível
        // Antes usava -1 (DEFAULT_AMPLITUDE) que era muito fraco
        private const val VIBRATION_AMPLITUDE = 255  // Máximo = vibração mais forte
    }
    
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    /**
     * Verifica se a vibração está habilitada nas configurações
     */
    fun isVibrationEnabled(): Boolean {
        return preferencesManager.isVibrationOnCollectionEnabled()
    }
    
    /**
     * Verifica se o dispositivo suporta vibração
     */
    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() == true
    }
    
    /**
     * Vibra ao coletar um patrimônio (se habilitado)
     * Vibração curta e suave para feedback tátil
     */
    fun vibrateOnCollection() {
        if (!isVibrationEnabled()) {
            android.util.Log.d("VibrationHelper", "Vibração desabilitada nas configurações")
            return
        }
        
        vibrateShort()
    }
    
    /**
     * Vibração curta (50ms) - para feedback de coleta
     */
    fun vibrateShort() {
        vibrate(VIBRATION_DURATION_SHORT)
    }
    
    /**
     * Vibração média (100ms) - para confirmações
     */
    fun vibrateMedium() {
        vibrate(VIBRATION_DURATION_MEDIUM)
    }
    
    /**
     * Vibração longa (200ms) - para alertas
     */
    fun vibrateLong() {
        vibrate(VIBRATION_DURATION_LONG)
    }
    
    /**
     * Vibração de sucesso — padrão FORTE e PERCEPTÍVEL
     * ✅ v2.20: Amplitude MÁXIMA (255) + duração aumentada para 120ms
     * Padrão: vibra-pausa-vibra para feedback tátil claro
     * 
     * Chamada ao coletar patrimônio com sucesso
     */
    fun vibrateSuccess() {
        if (!isVibrationEnabled()) {
            android.util.Log.d("VibrationHelper", "⚠️ Vibração desabilitada nas configurações")
            return
        }
        if (vibrator == null || !hasVibrator()) {
            android.util.Log.w("VibrationHelper", "⚠️ Dispositivo não suporta vibração")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // ✅ Padrão FORTE: 120ms vibra, 80ms pausa, 120ms vibra
                // Amplitude MÁXIMA (255) para garantir que seja perceptível
                val pattern = longArrayOf(0, 120, 80, 120)
                val amplitudes = intArrayOf(0, 255, 0, 255)  // ✅ MÁXIMO
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                android.util.Log.d("VibrationHelper", "✅ vibrateSuccess executado (AMPLITUDE MÁXIMA 255)")
            } else {
                // API < 26: usar padrão simples (sem controle de amplitude)
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 120, 80, 120), -1)
                android.util.Log.d("VibrationHelper", "✅ vibrateSuccess executado (API < 26)")
            }
        } catch (e: Exception) {
            android.util.Log.e("VibrationHelper", "❌ Erro ao vibrar (success)", e)
        }
    }
    
    /**
     * Vibração de erro - vibração mais longa
     */
    fun vibrateError() {
        if (!isVibrationEnabled()) return
        vibrate(VIBRATION_DURATION_LONG)
    }
    
    /**
     * Executa vibração com duração específica
     */
    private fun vibrate(duration: Long) {
        if (vibrator == null || !hasVibrator()) {
            android.util.Log.w("VibrationHelper", "Dispositivo não suporta vibração")
            return
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(duration, VIBRATION_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
            android.util.Log.d("VibrationHelper", "✓ Vibração executada: ${duration}ms")
        } catch (e: Exception) {
            android.util.Log.e("VibrationHelper", "Erro ao vibrar", e)
        }
    }
    
    /**
     * Cancela qualquer vibração em andamento
     */
    fun cancel() {
        vibrator?.cancel()
    }
}
