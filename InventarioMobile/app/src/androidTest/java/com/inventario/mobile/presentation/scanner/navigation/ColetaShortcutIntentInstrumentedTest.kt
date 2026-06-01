package com.inventario.mobile.presentation.scanner.navigation

import android.content.Context
import android.content.Intent
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.chip.Chip
import com.inventario.mobile.R
import com.inventario.mobile.presentation.coleta.ItemSemEtiquetaActivity
import com.inventario.mobile.presentation.coleta.ManualCollectionActivity
import com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste instrumentado da feature `atalhos-coleta-rapida` (Task 8).
 *
 * ## Por que NÃO usamos `ActivityScenario`/`launch(ScannerActivity)`
 *
 * A `ScannerActivity` é "pesada" e lançá-la diretamente em um teste instrumentado
 * seria frágil e exigiria infraestrutura de DI de teste que **não existe** neste
 * módulo. Concretamente:
 *
 * - **Hilt `@AndroidEntryPoint`** — a Activity injeta múltiplos `UseCase`/serviços via
 *   Hilt (`RegistrarColetaUseCase`, `BuscarPatrimonioUseCase`, `VibrationHelper`,
 *   `PhotoHelper`, `RegistrarAcessoPatrimonioUseCase`, `AuditService`). Para lançá-la
 *   num teste seria preciso um `HiltTestRunner` (`HiltAndroidRule` + `@HiltAndroidApp`
 *   de teste). O `testInstrumentationRunner` configurado em `app/build.gradle` é o
 *   `androidx.test.runner.AndroidJUnitRunner` **simples** — não há runner Hilt.
 * - **Permissão de CÂMERA** — `onCreate()` solicita `CAMERA` e o fluxo abre um
 *   `ActivityResultLauncher` do scanner ZXing automaticamente, tornando um launch
 *   determinístico inviável sem stubs/permissões concedidas previamente.
 * - **Escopo/risco** — alterar a DI do app, trocar o instrumentation runner ou mexer
 *   em produção para viabilizar o launch está **fora do escopo** desta task e é
 *   arriscado (steering `endpoints-nao-alterar.md` / Clean Architecture).
 *
 * ## O que este teste cobre (abordagem pragmática — OPTION B)
 *
 * O **contrato de navegação** dos atalhos é validado em duas frentes complementares:
 *
 *  1. **Contrato de Intent** — construímos os `Intent` exatamente como a
 *     `ScannerActivity` faz (espelhando `executarSemEtiqueta` / `executarComExtrasSala`),
 *     a partir do plano puro produzido por [ColetaShortcutNavigator], e asseguramos
 *     as chaves/tipos de extra **iguais** aos consumidos pelas telas de destino
 *     (referenciando as constantes reais `ManualCollectionActivity.EXTRA_SALA_ID`,
 *     `DescricaoSelectionActivity.EXTRA_SALA_ID`, etc.).
 *  2. **Layout/UI** — inflamos `R.layout.activity_scanner` contra o tema Material do
 *     app e verificamos a presença/estilo dos três chips do Menu_Atalhos e a
 *     preservação dos controles preexistentes.
 *
 * A **regra de decisão** pura (sala ausente, conversão Int→Long, escolha de extras,
 * fallback de descrição) é coberta de forma exaustiva pelos **property tests** JVM em
 * `app/src/test/.../ColetaShortcutNavigatorPropertyTest.kt` (P1–P4 do design.md).
 *
 * > Execução conectada (em dispositivo/emulador) **não** é executada neste ambiente —
 * > requer `connectedAndroidTest` com um device/emulador disponível.
 *
 * **Validates: Requirements 1.1, 1.2, 1.4, 2.4, 2.5, 3.2, 3.3, 4.2, 4.3, 6.1, 7.1, 9.1, 9.2**
 */
@RunWith(AndroidJUnit4::class)
class ColetaShortcutIntentInstrumentedTest {

    private val targetContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val navigator = ColetaShortcutNavigator()

    private companion object {
        // Espelham EXATAMENTE as chaves lidas por ItemSemEtiquetaActivity
        // (que usa literais, sem constantes públicas) — Req 2.4/2.5/9.1.
        const val EXTRA_DESCRICAO_SUGERIDA = "DESCRICAO_SUGERIDA"
        const val EXTRA_NUMERO_PATRIMONIO_ORIGEM = "NUMERO_PATRIMONIO_ORIGEM"

        // Default usado pelas telas de destino em getLongExtra(...).
        const val SALA_ID_DEFAULT = -1L
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers — espelham a construção de Intent da ScannerActivity
    // ─────────────────────────────────────────────────────────────────────

    /** Espelha `ScannerActivity.executarComExtrasSala(...)` (Manual/Descrição). */
    private fun buildSalaIntent(target: Class<*>, salaId: Long, salaNome: String): Intent =
        Intent(targetContext, target).apply {
            putExtra(ManualCollectionActivity.EXTRA_SALA_ID, salaId)
            putExtra(ManualCollectionActivity.EXTRA_SALA_NOME, salaNome)
        }

    /** Espelha `ScannerActivity.executarSemEtiqueta(...)`. */
    private fun buildSemEtiquetaIntent(plano: ShortcutNavigationPlan.NavigateToSemEtiqueta): Intent =
        Intent(targetContext, ItemSemEtiquetaActivity::class.java).apply {
            plano.descricaoSugerida?.let { putExtra(EXTRA_DESCRICAO_SUGERIDA, it) }
            plano.numeroPatrimonioOrigem?.let { putExtra(EXTRA_NUMERO_PATRIMONIO_ORIGEM, it) }
        }

    private fun inflateScannerLayout(): View {
        // Chips Material exigem um tema MaterialComponents/Material3. Usamos o mesmo
        // tema declarado para a ScannerActivity no AndroidManifest.
        val themed = ContextThemeWrapper(targetContext, R.style.Theme_InventarioMobile)
        return LayoutInflater.from(themed).inflate(R.layout.activity_scanner, null, false)
    }

    // ─────────────────────────────────────────────────────────────────────
    // Contrato de Intent — chaves/tipos
    // ─────────────────────────────────────────────────────────────────────

    /**
     * As constantes públicas das telas de destino batem com as chaves esperadas pelo
     * design e reutilizadas pelo Menu_Atalhos (`extra_sala_id` / `extra_sala_nome`).
     *
     * **Validates: Requirements 9.1**
     */
    @Test
    fun realConstants_matchExpectedExtraKeys() {
        assertEquals("extra_sala_id", ManualCollectionActivity.EXTRA_SALA_ID)
        assertEquals("extra_sala_nome", ManualCollectionActivity.EXTRA_SALA_NOME)
        assertEquals("extra_sala_id", DescricaoSelectionActivity.EXTRA_SALA_ID)
        assertEquals("extra_sala_nome", DescricaoSelectionActivity.EXTRA_SALA_NOME)

        // Manual e Descrição compartilham as MESMAS chaves — garante consistência
        // do contrato reutilizado pelos atalhos.
        assertEquals(ManualCollectionActivity.EXTRA_SALA_ID, DescricaoSelectionActivity.EXTRA_SALA_ID)
        assertEquals(ManualCollectionActivity.EXTRA_SALA_NOME, DescricaoSelectionActivity.EXTRA_SALA_NOME)
    }

    /**
     * Atalho_Manual: o `Intent` carrega `extra_sala_id` como Long (lido via
     * `getLongExtra`) e `extra_sala_nome` como String, exatamente como
     * `ManualCollectionActivity` consome. O id preserva o valor numérico após a
     * conversão Int→Long realizada pelo resolver.
     *
     * **Validates: Requirements 3.2, 3.3, 9.1**
     */
    @Test
    fun manualIntent_carriesSalaIdAsLong_andNome() {
        val sala = SalaContext(salaId = 4321, salaNome = "Laboratório de Informática")
        val plano = navigator.resolve(ColetaShortcut.MANUAL, sala, itemEscaneado = null)
        assertTrue(plano is ShortcutNavigationPlan.NavigateToManual)
        plano as ShortcutNavigationPlan.NavigateToManual

        val intent = buildSalaIntent(ManualCollectionActivity::class.java, plano.salaId, plano.salaNome)

        // Tipo Long (getLongExtra, como na tela de destino).
        val idLong = intent.getLongExtra(ManualCollectionActivity.EXTRA_SALA_ID, SALA_ID_DEFAULT)
        assertEquals(4321L, idLong)
        // Round-trip Int→Long→Int preserva o valor (Req 3.3).
        assertEquals(sala.salaId, idLong.toInt())
        // Nome preservado (Req 3.2).
        assertEquals("Laboratório de Informática", intent.getStringExtra(ManualCollectionActivity.EXTRA_SALA_NOME))
    }

    /**
     * Atalho_Descricao: mesmo contrato de extras de sala que o Atalho_Manual,
     * consumido por `DescricaoSelectionActivity` (também via `getLongExtra`).
     *
     * **Validates: Requirements 4.2, 4.3, 9.1**
     */
    @Test
    fun descricaoIntent_carriesSalaIdAsLong_andNome() {
        val sala = SalaContext(salaId = 7, salaNome = "Almoxarifado Central")
        val plano = navigator.resolve(ColetaShortcut.DESCRICAO, sala, itemEscaneado = null)
        assertTrue(plano is ShortcutNavigationPlan.NavigateToDescricao)
        plano as ShortcutNavigationPlan.NavigateToDescricao

        val intent = buildSalaIntent(DescricaoSelectionActivity::class.java, plano.salaId, plano.salaNome)

        val idLong = intent.getLongExtra(DescricaoSelectionActivity.EXTRA_SALA_ID, SALA_ID_DEFAULT)
        assertEquals(7L, idLong)
        assertEquals(sala.salaId, idLong.toInt())
        assertEquals("Almoxarifado Central", intent.getStringExtra(DescricaoSelectionActivity.EXTRA_SALA_NOME))
    }

    /**
     * Atalho_Sem_Etiqueta COM Item_Escaneado: o `Intent` carrega
     * `DESCRICAO_SUGERIDA` (descrição) e `NUMERO_PATRIMONIO_ORIGEM` (número) — chaves
     * idênticas às lidas por `ItemSemEtiquetaActivity`.
     *
     * **Validates: Requirements 2.4, 9.1**
     */
    @Test
    fun semEtiquetaIntent_includesOptionalExtras_whenItemPresent() {
        val sala = SalaContext(salaId = 10, salaNome = "Sala 101")
        val item = ScannedItemRef(numeroPatrimonio = "IFMT-00099", descricao = "Cadeira giratória")
        val plano = navigator.resolve(ColetaShortcut.SEM_ETIQUETA, sala, item)
        assertTrue(plano is ShortcutNavigationPlan.NavigateToSemEtiqueta)
        plano as ShortcutNavigationPlan.NavigateToSemEtiqueta

        val intent = buildSemEtiquetaIntent(plano)

        assertTrue(intent.hasExtra(EXTRA_DESCRICAO_SUGERIDA))
        assertTrue(intent.hasExtra(EXTRA_NUMERO_PATRIMONIO_ORIGEM))
        assertEquals("Cadeira giratória", intent.getStringExtra(EXTRA_DESCRICAO_SUGERIDA))
        assertEquals("IFMT-00099", intent.getStringExtra(EXTRA_NUMERO_PATRIMONIO_ORIGEM))
    }

    /**
     * Atalho_Sem_Etiqueta COM Item_Escaneado sem descrição: a descrição sugerida
     * usa o fallback `"Patrimônio {numero}"` (espelha o fluxo de duplicata).
     *
     * **Validates: Requirements 2.4**
     */
    @Test
    fun semEtiquetaIntent_usesDescriptionFallback_whenOnlyNumberPresent() {
        val sala = SalaContext(salaId = 10, salaNome = "Sala 101")
        val item = ScannedItemRef(numeroPatrimonio = "IFMT-00500", descricao = "   ")
        val plano = navigator.resolve(ColetaShortcut.SEM_ETIQUETA, sala, item)
                as ShortcutNavigationPlan.NavigateToSemEtiqueta

        val intent = buildSemEtiquetaIntent(plano)

        assertEquals("Patrimônio IFMT-00500", intent.getStringExtra(EXTRA_DESCRICAO_SUGERIDA))
        assertEquals("IFMT-00500", intent.getStringExtra(EXTRA_NUMERO_PATRIMONIO_ORIGEM))
    }

    /**
     * Atalho_Sem_Etiqueta SEM Item_Escaneado: o `Intent` é construído **sem** os
     * extras opcionais — nem `DESCRICAO_SUGERIDA` nem `NUMERO_PATRIMONIO_ORIGEM`.
     *
     * **Validates: Requirements 2.5, 9.1**
     */
    @Test
    fun semEtiquetaIntent_omitsOptionalExtras_whenNoItem() {
        val sala = SalaContext(salaId = 10, salaNome = "Sala 101")
        val plano = navigator.resolve(ColetaShortcut.SEM_ETIQUETA, sala, itemEscaneado = null)
                as ShortcutNavigationPlan.NavigateToSemEtiqueta

        val intent = buildSemEtiquetaIntent(plano)

        assertFalse("não deve carregar DESCRICAO_SUGERIDA sem scan", intent.hasExtra(EXTRA_DESCRICAO_SUGERIDA))
        assertFalse("não deve carregar NUMERO_PATRIMONIO_ORIGEM sem scan", intent.hasExtra(EXTRA_NUMERO_PATRIMONIO_ORIGEM))
    }

    // ─────────────────────────────────────────────────────────────────────
    // Layout / UI — Menu_Atalhos e controles preexistentes
    // ─────────────────────────────────────────────────────────────────────

    /**
     * O layout do scanner expõe os três chips do Menu_Atalhos, cada um sendo um
     * `Chip` Material, visível, com texto e `contentDescription` não vazios.
     *
     * **Validates: Requirements 1.1, 1.2, 6.1, 7.1**
     */
    @Test
    fun scannerLayout_exposesThreeShortcutChips_labeledAndDescribed() {
        val root = inflateScannerLayout()

        val chips = mapOf(
            "chipAtalhoSemEtiqueta" to root.findViewById<View>(R.id.chipAtalhoSemEtiqueta),
            "chipAtalhoManual" to root.findViewById<View>(R.id.chipAtalhoManual),
            "chipAtalhoDescricao" to root.findViewById<View>(R.id.chipAtalhoDescricao)
        )

        chips.forEach { (nome, view) ->
            assertNotNull("$nome deve existir no layout", view)
            assertTrue("$nome deve ser um com.google.android.material.chip.Chip", view is Chip)
            val chip = view as Chip
            assertEquals("$nome deve estar VISIBLE", View.VISIBLE, chip.visibility)
            assertTrue("$nome deve ter texto não vazio", !chip.text.isNullOrBlank())
            assertTrue("$nome deve ter contentDescription não vazio", !chip.contentDescription.isNullOrBlank())
        }
    }

    /**
     * Os controles preexistentes da Tela_Coleta_Rapida permanecem presentes no
     * layout após a adição do Menu_Atalhos (sem regressão de UI).
     *
     * **Validates: Requirements 1.4, 9.2**
     */
    @Test
    fun scannerLayout_preservesPreexistingControls() {
        val root = inflateScannerLayout()

        assertNotNull("chipSala deve permanecer no layout", root.findViewById<View>(R.id.chipSala))
        assertNotNull("chipEstado deve permanecer no layout", root.findViewById<View>(R.id.chipEstado))
        assertNotNull("fabColetarSemEtiqueta deve permanecer no layout", root.findViewById<View>(R.id.fabColetarSemEtiqueta))
        assertNotNull("buttonColetarSimilar deve permanecer no layout", root.findViewById<View>(R.id.buttonColetarSimilar))
    }
}
