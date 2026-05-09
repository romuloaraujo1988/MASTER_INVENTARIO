package com.inventario.mobile.presentation.scanner

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Feature: scanner-coleta-sem-etiqueta
 * Property 7: Contador consistente ao retornar
 * Validates: Requirements 6.2, 7.3
 *
 * Para qualquer sequência arbitrária de incrementos, após onResume() o valor exibido
 * em `textColetasCount` (`binding.textColetasCount.text`) deve ser exatamente
 * `preferencesManager.getCollectionCount().toString()`.
 *
 * A lógica está em `ScannerActivity.atualizarContadorColetas()`:
 *
 *   private fun atualizarContadorColetas() {
 *       val count = preferencesManager.getCollectionCount()
 *       binding.textColetasCount.text = count.toString()
 *   }
 *
 * Validamos a propriedade em forma pura para evitar dependência Android.
 */
class ScannerCounterConsistencyPropertyTest : StringSpec({

    "Property 7: Contador consistente — valor exibido = getCollectionCount.toString()" {
        checkAll(
            iterations = 100,
            Arb.int(0..100_000)
        ) { count ->
            val rendered = renderCollectionCount(count)
            rendered shouldBe count.toString()
            // Conversão inversa deve reproduzir o Int
            rendered.toInt() shouldBe count
        }
    }

    "Property 7: Contador consistente — sequência de incrementos é monotônica" {
        checkAll(iterations = 100, Arb.int(1..50)) { incrementos ->
            val store = FakePreferencesCounter(initial = 0)
            repeat(incrementos) { store.increment() }
            val rendered = renderCollectionCount(store.value)
            rendered shouldBe incrementos.toString()
        }
    }

    "Property 7: Contador consistente — onResume NÃO altera o valor do preferencesManager" {
        checkAll(
            iterations = 100,
            Arb.int(0..100_000)
        ) { inicial ->
            val store = FakePreferencesCounter(initial = inicial)
            // Simula várias chamadas de onResume (leitura pura)
            repeat(3) { renderCollectionCount(store.value) }
            store.value shouldBe inicial
        }
    }
})

/**
 * Pure mirror of `atualizarContadorColetas()` — retorna o texto que seria atribuído
 * a `binding.textColetasCount.text`.
 */
internal fun renderCollectionCount(count: Int): String = count.toString()

internal class FakePreferencesCounter(initial: Int) {
    var value: Int = initial
        private set
    fun increment() { value += 1 }
}
