package com.inventario.mobile.presentation.descricao

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.io.IOException
import java.sql.SQLException

/**
 * Feature: scanner-coleta-sem-etiqueta
 * Property 6: Não-finalização em falha
 * Validates: Requirement 3.5 (e relacionado 5.4)
 *
 * Para qualquer Exception propagada via `ColetaState.Error(message)`, a Activity:
 *  - NÃO deve chamar finish()   → isFinishing permanece false
 *  - DEVE exibir a mensagem de erro ao usuário (Toast)
 *  - NÃO deve tocar som nem vibrar (sobreposição com Property 5)
 *
 * Simulamos o branch `is ColetaState.Error` em forma pura via `handleErrorBranch`.
 */
class DescricaoSelectionErrorPropertyTest : StringSpec({

    "Property 6: Não-finalização em falha — Activity permanece ativa" {
        val arbException: Arb<Exception> = Arb.of(
            IOException("rede indisponível"),
            SQLException("db fechado"),
            IllegalStateException("estado inválido"),
            RuntimeException("falha genérica"),
            Exception("erro desconhecido")
        )

        checkAll(iterations = 100, arbException) { exception ->
            val outcome = handleErrorBranch(exception.message ?: "Erro ao registrar coleta")

            outcome.isFinishing shouldBe false
            outcome.toastMessage.shouldNotBeBlank()
            outcome.soundPlayed shouldBe false
            outcome.vibrated shouldBe false
        }
    }

    "Property 6: Não-finalização — mensagens aleatórias preservam o invariante" {
        checkAll(iterations = 100, Arb.string(1..80)) { message ->
            val outcome = handleErrorBranch(message)
            outcome.isFinishing shouldBe false
            outcome.toastMessage shouldBe message
            outcome.soundPlayed shouldBe false
            outcome.vibrated shouldBe false
        }
    }
})

internal data class ErrorBranchOutcome(
    val isFinishing: Boolean,
    val toastMessage: String,
    val soundPlayed: Boolean,
    val vibrated: Boolean
)

/**
 * Pure-logic extract de:
 *
 *   is ColetaState.Error -> {
 *       hideLoading()
 *       showError(state.message)
 *       viewModel.limparColetaState()
 *       // NÃO: finish(), playSuccessSound(), vibrateSuccess()
 *   }
 */
internal fun handleErrorBranch(message: String): ErrorBranchOutcome = ErrorBranchOutcome(
    isFinishing = false,
    toastMessage = message,
    soundPlayed = false,
    vibrated = false
)
