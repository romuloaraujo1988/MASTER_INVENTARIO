package com.inventario.mobile.presentation.scanner.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Testes unitários de borda (exemplos concretos) para os modelos puros de navegação
 * dos atalhos da Tela_Coleta_Rapida: [ScannedItemRef], [SalaContext] e o caso
 * "sem scan" de [ColetaShortcutNavigator].
 *
 * Feature: atalhos-coleta-rapida (task 4)
 *
 * Diferente do [ColetaShortcutNavigatorPropertyTest] (propriedades universais sobre
 * muitas entradas), este arquivo verifica exemplos concretos e casos de borda com
 * entradas fixas, complementando a cobertura.
 *
 * Validates: Requirements 2.4, 2.5, 5.1
 */
class ColetaShortcutModelsTest : FunSpec({

    val navigator = ColetaShortcutNavigator()

    // ---------------------------------------------------------------------
    // ScannedItemRef.descricaoOuFallback()
    // ---------------------------------------------------------------------

    test("descricaoOuFallback: descrição presente (não vazia) retorna a descrição exata, mesmo com número presente") {
        val ref = ScannedItemRef(numeroPatrimonio = "12345", descricao = "Cadeira Giratória")
        ref.descricaoOuFallback() shouldBe "Cadeira Giratória"
    }

    test("descricaoOuFallback: descrição vazia + número presente retorna 'Patrimônio {numero}'") {
        val ref = ScannedItemRef(numeroPatrimonio = "12345", descricao = "")
        ref.descricaoOuFallback() shouldBe "Patrimônio 12345"
    }

    test("descricaoOuFallback: descrição com whitespace + número presente retorna 'Patrimônio {numero}'") {
        val ref = ScannedItemRef(numeroPatrimonio = "12345", descricao = "   ")
        ref.descricaoOuFallback() shouldBe "Patrimônio 12345"
    }

    test("descricaoOuFallback: descrição null + número presente retorna 'Patrimônio {numero}'") {
        val ref = ScannedItemRef(numeroPatrimonio = "PAT-001", descricao = null)
        ref.descricaoOuFallback() shouldBe "Patrimônio PAT-001"
    }

    test("descricaoOuFallback: ambos null retorna null") {
        val ref = ScannedItemRef(numeroPatrimonio = null, descricao = null)
        ref.descricaoOuFallback() shouldBe null
    }

    test("descricaoOuFallback: ambos vazios retorna null") {
        val ref = ScannedItemRef(numeroPatrimonio = "", descricao = "")
        ref.descricaoOuFallback() shouldBe null
    }

    test("descricaoOuFallback: ambos whitespace retorna null") {
        val ref = ScannedItemRef(numeroPatrimonio = "  ", descricao = "\t")
        ref.descricaoOuFallback() shouldBe null
    }

    test("descricaoOuFallback: descrição null + número vazio retorna null") {
        val ref = ScannedItemRef(numeroPatrimonio = "", descricao = null)
        ref.descricaoOuFallback() shouldBe null
    }

    test("descricaoOuFallback: descrição null + número whitespace retorna null") {
        val ref = ScannedItemRef(numeroPatrimonio = "   ", descricao = null)
        ref.descricaoOuFallback() shouldBe null
    }

    // ---------------------------------------------------------------------
    // SalaContext.isValid — tabela de casos de borda
    // ---------------------------------------------------------------------

    test("SalaContext.isValid: id=0 com nome válido é inválido") {
        SalaContext(salaId = 0, salaNome = "Sala 101").isValid shouldBe false
    }

    test("SalaContext.isValid: id=-1 com nome válido é inválido") {
        SalaContext(salaId = -1, salaNome = "Sala 101").isValid shouldBe false
    }

    test("SalaContext.isValid: id>0 com nome vazio é inválido") {
        SalaContext(salaId = 10, salaNome = "").isValid shouldBe false
    }

    test("SalaContext.isValid: id>0 com nome só com espaços é inválido") {
        SalaContext(salaId = 10, salaNome = "   ").isValid shouldBe false
    }

    test("SalaContext.isValid: id>0 com nome não-vazio é válido") {
        SalaContext(salaId = 10, salaNome = "Laboratório").isValid shouldBe true
    }

    // ---------------------------------------------------------------------
    // Caso "sem scan": resolve(SEM_ETIQUETA, salaVálida, null)
    // ---------------------------------------------------------------------

    test("resolve sem scan: SEM_ETIQUETA com sala válida e item null retorna NavigateToSemEtiqueta com extras null") {
        val sala = SalaContext(salaId = 7, salaNome = "Almoxarifado")

        val plan = navigator.resolve(ColetaShortcut.SEM_ETIQUETA, sala, null)

        val semEtiqueta = plan.shouldBeInstanceOf<ShortcutNavigationPlan.NavigateToSemEtiqueta>()
        semEtiqueta.descricaoSugerida shouldBe null
        semEtiqueta.numeroPatrimonioOrigem shouldBe null
        semEtiqueta.salaParaPersistir shouldBe sala
    }
})
