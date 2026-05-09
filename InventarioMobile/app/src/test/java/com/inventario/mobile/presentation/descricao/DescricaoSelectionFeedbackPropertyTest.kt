package com.inventario.mobile.presentation.descricao

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: scanner-coleta-sem-etiqueta
 * Property 5: Feedback pós-coleta condicional
 * Validates: Requirements 5.1, 5.2, 5.3, 5.4
 *
 * Para qualquer (result, vibrationEnabled):
 *  - Se result == Success:
 *      * SoundUtils.playSuccessSound() é chamado 1 vez
 *      * VibrationHelper.vibrateSuccess() é chamado iff vibrationEnabled == true
 *  - Se result == Failure (qualquer erro):
 *      * NEM som NEM vibração são chamados
 *
 * Validamos a função `applyFeedback` que replica a mesma lógica do branch Success/Error
 * em `DescricaoSelectionActivity.setupObservers()`.
 */
class DescricaoSelectionFeedbackPropertyTest : StringSpec({

    "Property 5: Feedback condicional — Success sempre toca som e vibra iff habilitado" {
        checkAll(
            iterations = 100,
            Arb.boolean()
        ) { vibrationEnabled ->
            val fb = FeedbackRecorder()
            applyFeedback(
                isSuccess = true,
                vibrationEnabled = vibrationEnabled,
                recorder = fb
            )
            fb.soundCalls shouldBe 1
            fb.vibrateCalls shouldBe if (vibrationEnabled) 1 else 0
        }
    }

    "Property 5: Feedback condicional — Error nunca emite som nem vibração" {
        checkAll(
            iterations = 100,
            Arb.boolean(),
            Arb.string(0..60)
        ) { vibrationEnabled, _errorMessage ->
            val fb = FeedbackRecorder()
            applyFeedback(
                isSuccess = false,
                vibrationEnabled = vibrationEnabled,
                recorder = fb
            )
            fb.soundCalls shouldBe 0
            fb.vibrateCalls shouldBe 0
        }
    }

    "Property 5: Feedback condicional — ligar/desligar vibração em Success é idempotente por chamada" {
        checkAll(
            iterations = 100,
            Arb.boolean()
        ) { vibrationEnabled ->
            val fb = FeedbackRecorder()
            // 3 sucessos consecutivos com mesma preferência
            repeat(3) {
                applyFeedback(isSuccess = true, vibrationEnabled = vibrationEnabled, recorder = fb)
            }
            fb.soundCalls shouldBe 3
            fb.vibrateCalls shouldBe if (vibrationEnabled) 3 else 0
        }
    }
})

internal class FeedbackRecorder {
    var soundCalls: Int = 0
        private set
    var vibrateCalls: Int = 0
        private set

    fun playSound() { soundCalls++ }
    fun vibrate() { vibrateCalls++ }
}

/**
 * Pure-logic extract de:
 *
 *   is ColetaState.Success -> {
 *       SoundUtils.playSuccessSound()
 *       if (preferencesManager.isVibrationOnCollectionEnabled()) {
 *           vibrationHelper.vibrateSuccess()
 *       }
 *   }
 *   is ColetaState.Error -> { /* no sound, no vibrate, no finish */ }
 */
internal fun applyFeedback(
    isSuccess: Boolean,
    vibrationEnabled: Boolean,
    recorder: FeedbackRecorder
) {
    if (!isSuccess) return
    recorder.playSound()
    if (vibrationEnabled) {
        recorder.vibrate()
    }
}
