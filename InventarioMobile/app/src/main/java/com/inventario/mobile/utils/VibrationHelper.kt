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
 */
@Singleton
class VibrationHelper @Inject constructor(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        // Duração da vibração em milissegundos
        private const val VIBRATION_DURATION_SHORT = 50L   // Vibração curta
        private const val VIBRATION_DURATION_MEDIUM = 100L // Vibração média
        private const val VIBRATION_DURATION_LONG = 200L   // Vibração longa
        
        // Amplitude da vibração (1-255, ou DEFAULT_AMPLITUDE)
        private const val VIBRATION_AMPLITUDE = 128 // Amplitude média
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
     * Vibração de sucesso - padrão curto-pausa-curto
     */
    fun vibrateSuccess() {
        if (!isVibrationEnabled()) return
        
        val pattern = longArrayOf(0, 50, 50, 50) // delay, vibrate, pause, vibrate
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = intArrayOf(0, VIBRATION_AMPLITUDE, 0, VIBRATION_AMPLITUDE)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
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
