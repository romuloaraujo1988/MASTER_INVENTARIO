package com.inventario.mobile.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Utilitário para reproduzir sons e vibrações no aplicativo
 */
object SoundUtils {
    
    private const val TAG = "SoundUtils"
    private var toneGenerator: ToneGenerator? = null
    
    /**
     * Inicializa o gerador de tons
     */
    private fun initToneGenerator() {
        if (toneGenerator == null) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50) // Volume 50%
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inicializar ToneGenerator", e)
            }
        }
    }
    
    /**
     * Toca um som suave e agradável de sucesso na coleta
     * Som de duas notas ascendentes (Dó -> Mi) que é agradável e não cansativo
     */
    fun playSuccessSound() {
        try {
            initToneGenerator()
            
            // Toca duas notas musicais suaves em sequência
            // Nota 1: Dó (C) - 523 Hz
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            
            // Pequena pausa entre as notas
            Thread.sleep(80)
            
            // Nota 2: Mi (E) - 659 Hz (intervalo de terça maior - som agradável)
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tocar som de sucesso", e)
        }
    }
    
    /**
     * Toca um som suave de sucesso (versão simplificada)
     * Apenas uma nota curta e agradável
     */
    fun playSimpleSuccessSound() {
        try {
            initToneGenerator()
            // Tom suave e curto
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tocar som simples", e)
        }
    }
    
    /**
     * Toca um som de erro (mais grave e curto)
     */
    fun playErrorSound() {
        try {
            initToneGenerator()
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tocar som de erro", e)
        }
    }
    
    /**
     * Vibração suave de feedback
     */
    fun vibrateSuccess(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Vibração suave e curta
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao vibrar", e)
        }
    }
    
    /**
     * Feedback completo: som + vibração
     */
    fun playSuccessFeedback(context: Context) {
        playSuccessSound()
        vibrateSuccess(context)
    }
    
    /**
     * Libera recursos do gerador de tons
     */
    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao liberar ToneGenerator", e)
        }
    }
}
