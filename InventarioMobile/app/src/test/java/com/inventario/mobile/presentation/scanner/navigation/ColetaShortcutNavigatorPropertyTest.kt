package com.inventario.mobile.presentation.scanner.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-Based Tests para [ColetaShortcutNavigator].
 *
 * Feature: atalhos-coleta-rapida (task 3)
 *
 * A decisão de navegação `resolve` é uma função pura e determinística: dado um
 * [SalaContext], um [ColetaShortcut] e uma referência opcional ao [ScannedItemRef],
 * retorna um [ShortcutNavigationPlan] sem efeitos colaterais ou dependência de
 * Android/rede. Isso a torna ideal para testes baseados em propriedades.
 *
 * Cada propriedade roda no mínimo 100 iterações (PropTestConfig(iterations = 100)),
 * cobrindo as quatro propriedades de correção descritas em `design.md`.
 */
class ColetaShortcutNavigatorPropertyTest : FunSpec({

    val navigator = ColetaShortcutNavigator()

    // ---------------------------------------------------------------------
    // Geradores (Arb)
    // ---------------------------------------------------------------------

    /**
     * Nomes de sala não-brancos, incluindo acentos/unicode. Combina uma lista
     * curada (variedade realista com diacríticos pt-BR) com strings aleatórias
     * garantidamente não-brancas (sufixo não-espaço).
     */
    val nomesSalaCurados = listOf(
        "Sala 101", "Laboratório", "Almoxarifado Çentral", "Pátio",
        "Secretaria Acadêmica", "Auditório", "Depósito de Materiais",
        "Sala dos Professores", "Coordenação", "Biblioteca Central"
    )

    val nomeNaoBrancoArb: Arb<String> = arbitrary { rs ->
        if (rs.random.nextBoolean()) {
            nomesSalaCurados[rs.random.nextInt(nomesSalaCurados.size)]
        } else {
            // String aleatória + caractere não-espaço para garantir isNotBlank().
            Arb.string(minSize = 0, maxSize = 20).next(rs) + "X"
        }
    }

    /** Strings em branco/somente espaços/tabs/quebras de linha. */
    val nomeBrancoArb: Arb<String> = arbitrary { rs ->
        val brancos = listOf("", " ", "   ", "\t", "\n", " \t ", "\t\n", "  \t  ")
        brancos[rs.random.nextInt(brancos.size)]
    }

    /** [SalaContext] válido: salaId em 1..Int.MAX_VALUE e salaNome não-branco. */
    val salaValidaArb: Arb<SalaContext> = arbitrary { rs ->
        SalaContext(
            salaId = Arb.int(1, Int.MAX_VALUE).next(rs),
            salaNome = nomeNaoBrancoArb.next(rs)
        )
    }

    /**
     * [SalaContext] inválido exercitando ambas as fontes de invalidez:
     *  (a) salaId <= 0 (inclui 0 e negativos) com qualquer nome;
     *  (b) salaId > 0 com nome em branco/somente espaços.
     */
    val salaInvalidaArb: Arb<SalaContext> = arbitrary { rs ->
        if (rs.random.nextBoolean()) {
            // (a) id <= 0 — qualquer nome (mesmo não-branco) resulta em inválido.
            SalaContext(
                salaId = Arb.int(Int.MIN_VALUE, 0).next(rs),
                salaNome = nomeNaoBrancoArb.next(rs)
            )
        } else {
            // (b) id > 0 mas nome em branco.
            SalaContext(
                salaId = Arb.int(1, Int.MAX_VALUE).next(rs),
                salaNome = nomeBrancoArb.next(rs)
            )
        }
    }

    /** numeroPatrimonio: null, vazio, whitespace ou alfanumérico. */
    val numeroArb: Arb<String?> = arbitrary { rs ->
        val opcoes = listOf<String?>(null, "", "   ", "\t", "12345", "PAT-001", "ABC123", "987654")
        opcoes[rs.random.nextInt(opcoes.size)]
    }

    /** descricao: null, vazio, whitespace ou texto. */
    val descricaoArb: Arb<String?> = arbitrary { rs ->
        val opcoes = listOf<String?>(null, "", "   ", "\t", "Cadeira Giratória", "Mesa Grande", "Notebook Dell", "Projetor")
        opcoes[rs.random.nextInt(opcoes.size)]
    }

    /**
     * [ScannedItemRef] opcional: combinações de numero/descricao mais o caso
     * `null` ("sem scan", ~1 em 5).
     */
    val itemArb: Arb<ScannedItemRef?> = arbitrary { rs ->
        if (rs.random.nextInt(5) == 0) {
            null
        } else {
            ScannedItemRef(
                numeroPatrimonio = numeroArb.next(rs),
                descricao = descricaoArb.next(rs)
            )
        }
    }

    /** Gerador uniforme sobre os três valores do enum. */
    val shortcutArb: Arb<ColetaShortcut> = Arb.of(ColetaShortcut.values().toList())

    // ---------------------------------------------------------------------
    // Propriedades
    // ---------------------------------------------------------------------

    // Feature: atalhos-coleta-rapida, Property 1: Sala válida sempre mapeia cada atalho ao seu destino correspondente
    test("Property 1: sala válida mapeia cada atalho ao Navigate* correspondente e nunca RequireSalaSelection") {
        checkAll(PropTestConfig(iterations = 100), shortcutArb, salaValidaArb, itemArb) { shortcut, sala, item ->
            val plan = navigator.resolve(shortcut, sala, item)

            (plan is ShortcutNavigationPlan.RequireSalaSelection) shouldBe false
            when (shortcut) {
                ColetaShortcut.SEM_ETIQUETA -> plan.shouldBeInstanceOf<ShortcutNavigationPlan.NavigateToSemEtiqueta>()
                ColetaShortcut.MANUAL -> plan.shouldBeInstanceOf<ShortcutNavigationPlan.NavigateToManual>()
                ColetaShortcut.DESCRICAO -> plan.shouldBeInstanceOf<ShortcutNavigationPlan.NavigateToDescricao>()
            }
        }
    }

    // Feature: atalhos-coleta-rapida, Property 2: Preservação do contexto da sala e conversão Int→Long
    test("Property 2: preserva salaNome e converte salaId Int->Long (round-trip) para MANUAL/DESCRICAO; salaParaPersistir == sala para SEM_ETIQUETA") {
        checkAll(PropTestConfig(iterations = 100), shortcutArb, salaValidaArb, itemArb) { shortcut, sala, item ->
            when (val plan = navigator.resolve(shortcut, sala, item)) {
                is ShortcutNavigationPlan.NavigateToManual -> {
                    plan.salaNome shouldBe sala.salaNome
                    plan.salaId shouldBe sala.salaId.toLong()
                    plan.salaId.toInt() shouldBe sala.salaId
                }
                is ShortcutNavigationPlan.NavigateToDescricao -> {
                    plan.salaNome shouldBe sala.salaNome
                    plan.salaId shouldBe sala.salaId.toLong()
                    plan.salaId.toInt() shouldBe sala.salaId
                }
                is ShortcutNavigationPlan.NavigateToSemEtiqueta -> {
                    plan.salaParaPersistir shouldBe sala
                }
                ShortcutNavigationPlan.RequireSalaSelection ->
                    error("Sala válida não deveria produzir RequireSalaSelection")
            }
        }
    }

    // Feature: atalhos-coleta-rapida, Property 3: Extras dependentes do item escaneado, com fallback de descrição
    test("Property 3: SEM_ETIQUETA propaga numero/descricao do item (com fallback) ou ambos null sem scan") {
        checkAll(PropTestConfig(iterations = 100), salaValidaArb, itemArb) { sala, item ->
            val plan = navigator.resolve(ColetaShortcut.SEM_ETIQUETA, sala, item)
            val semEtiqueta = plan.shouldBeInstanceOf<ShortcutNavigationPlan.NavigateToSemEtiqueta>()

            if (item == null) {
                semEtiqueta.descricaoSugerida shouldBe null
                semEtiqueta.numeroPatrimonioOrigem shouldBe null
            } else {
                semEtiqueta.numeroPatrimonioOrigem shouldBe item.numeroPatrimonio
                semEtiqueta.descricaoSugerida shouldBe item.descricaoOuFallback()
            }
        }
    }

    // Feature: atalhos-coleta-rapida, Property 4: Ausência de sala bloqueia qualquer atalho
    test("Property 4: sala inválida sempre resulta em RequireSalaSelection para qualquer atalho/item") {
        checkAll(PropTestConfig(iterations = 100), shortcutArb, salaInvalidaArb, itemArb) { shortcut, sala, item ->
            val plan = navigator.resolve(shortcut, sala, item)
            plan shouldBe ShortcutNavigationPlan.RequireSalaSelection
        }
    }
})
