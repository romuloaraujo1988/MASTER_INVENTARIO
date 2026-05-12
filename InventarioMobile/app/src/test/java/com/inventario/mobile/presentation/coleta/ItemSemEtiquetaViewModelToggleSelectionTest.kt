package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import com.inventario.mobile.domain.model.SugestaoDescricao
import com.inventario.mobile.domain.usecase.BuscarSugestoesDescricaoUseCase
import com.inventario.mobile.domain.usecase.LimparCacheDeOutrosInventariosUseCase
import com.inventario.mobile.domain.usecase.MarcarPatrimonioColetadoLocalmenteUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.presentation.coleta.state.SugestaoDescricaoState
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.VibrationHelper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Feature: coleta-descricao-livre-com-sugestao
 *
 * Testes unitários e baseados em propriedades para
 * [ItemSemEtiquetaViewModel], cobrindo:
 *
 *  - **Property 4** (task 15.10): Toggle desativado ⇒ zero consultas ao
 *    [BuscarSugestoesDescricaoUseCase] e estado permanece
 *    [SugestaoDescricaoState.Oculto].
 *    Validates: Requirements 2.2.
 *
 *  - **Property 5** (task 15.11): `onSugestaoSelecionada(s)` emite
 *    [ItemSemEtiquetaViewModel.Event.PreencherCampoLivre] carregando
 *    exatamente `s.descricao` — o ViewModel não guarda vínculo forte
 *    com a sugestão e a UI permanece editável.
 *    Validates: Requirements 3.5.
 *
 * Estratégia:
 *  - O `viewModelScope` do [ItemSemEtiquetaViewModel] usa
 *    `Dispatchers.Main.immediate`. Configuramos `Dispatchers.setMain(...)`
 *    com um único [StandardTestDispatcher] compartilhado com `runTest`
 *    para manter tempo virtual consistente entre as coroutines do VM
 *    (debounce de 300ms) e o corpo de teste (`advanceTimeBy`).
 *  - As coroutines `collectLatest` do bloco `init` do VM jamais
 *    terminam por conta própria (StateFlows são hot). Ao final de cada
 *    cenário, chamamos [clearViewModel] (reflexão sobre
 *    `ViewModel.clear()`) para cancelar o scope e evitar o
 *    `UncompletedCoroutinesError` do `runTest`.
 *  - O [_events][ItemSemEtiquetaViewModel.events] é um `SharedFlow` sem
 *    replay — nos cenários de P5 assinamos o fluxo antes de chamar
 *    `onSugestaoSelecionada(...)` para garantir captura.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemSemEtiquetaViewModelToggleSelectionTest : StringSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Constrói um [ItemSemEtiquetaViewModel] com dependências mínimas para
     *  exercitar apenas o autocomplete e o fluxo de eventos. */
    fun buildVm(buscar: BuscarSugestoesDescricaoUseCase): ItemSemEtiquetaViewModel {
        val registrar = mockk<RegistrarColetaUseCase>(relaxed = true)
        val marcar = mockk<MarcarPatrimonioColetadoLocalmenteUseCase>(relaxed = true)
        val limpar = mockk<LimparCacheDeOutrosInventariosUseCase>(relaxed = true)
        val prefs = mockk<PreferencesManager>(relaxed = true)
        every { prefs.getInventarioAtivoId() } returns 1
        val vib = mockk<VibrationHelper>(relaxed = true)
        return ItemSemEtiquetaViewModel(
            registrarColetaUseCase = registrar,
            buscarSugestoesDescricaoUseCase = buscar,
            marcarPatrimonioColetadoLocalmenteUseCase = marcar,
            limparCacheDeOutrosInventariosUseCase = limpar,
            preferencesManager = prefs,
            vibrationHelper = vib
        )
    }

    /** Invoca `ViewModel.clear()` via reflexão para cancelar o
     *  `viewModelScope` — necessário porque os coletores no `init` do VM
     *  ficam suspensos em StateFlows eternos e impediriam o `runTest`
     *  de completar. */
    fun clearViewModel(vm: ViewModel) {
        val method = ViewModel::class.java.getDeclaredMethod("clear")
        method.isAccessible = true
        method.invoke(vm)
    }

    // ------------------------------------------------------------------
    // Property 4 (Task 15.10)
    // ------------------------------------------------------------------

    "P4: toggle sempre off ⇒ zero consultas ao UseCase e estado permanece Oculto" {
        runTest(testDispatcher) {
            val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
            val vm = buildVm(buscar)
            try {
                // Sequência determinística de mudanças de termo; o toggle
                // permanece `false` (valor inicial), portanto o gate
                // `filter { ativo }` bloqueia qualquer `carregarSugestoes`.
                vm.onTermoBuscaChange("cadeira")
                advanceTimeBy(400L)
                vm.onTermoBuscaChange("mesa")
                advanceTimeBy(400L)
                vm.onTermoBuscaChange("armario")
                advanceTimeBy(1000L)
                advanceUntilIdle()

                coVerify(exactly = 0) { buscar(any(), any(), any(), any()) }
                vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.Oculto>()
            } finally {
                clearViewModel(vm)
            }
        }
    }

    "P4: toggle on → off → on → off termina em Oculto após cada transição off" {
        runTest(testDispatcher) {
            // UseCase relaxed para o caso de o teste exercitar brevemente o
            // caminho on; o foco é o estado final após cada off.
            val buscar = mockk<BuscarSugestoesDescricaoUseCase>(relaxed = true)
            val vm = buildVm(buscar)
            try {
                vm.setToggleSugestao(true)
                advanceTimeBy(100L)
                vm.setToggleSugestao(false)
                advanceUntilIdle()
                vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.Oculto>()

                vm.setToggleSugestao(true)
                advanceTimeBy(100L)
                vm.setToggleSugestao(false)
                advanceUntilIdle()
                vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.Oculto>()
            } finally {
                clearViewModel(vm)
            }
        }
    }

    "P4 property: sequência arbitrária de termos com toggle sempre off ⇒ 0 invocações" {
        runTest(testDispatcher) {
            checkAll(iterations = 20, Arb.list(Arb.string(0..20), 1..10)) { termos ->
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                val vm = buildVm(buscar)
                try {
                    termos.forEach { termo ->
                        vm.onTermoBuscaChange(termo)
                        advanceTimeBy(50L)
                    }
                    // Pós-debounce: ultrapassa os 300ms; mesmo assim, `filter`
                    // garante que nada se propague ao UseCase.
                    advanceTimeBy(500L)
                    advanceUntilIdle()

                    coVerify(exactly = 0) { buscar(any(), any(), any(), any()) }
                    vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.Oculto>()
                } finally {
                    clearViewModel(vm)
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Property 5 (Task 15.11)
    // ------------------------------------------------------------------

    "P5: onSugestaoSelecionada emite Event.PreencherCampoLivre com a descrição escolhida" {
        runTest(testDispatcher) {
            val buscar = mockk<BuscarSugestoesDescricaoUseCase>(relaxed = true)
            val vm = buildVm(buscar)
            try {
                val recebidos = mutableListOf<ItemSemEtiquetaViewModel.Event>()
                val job = backgroundScope.launch { vm.events.collect { recebidos += it } }
                advanceUntilIdle() // garante assinatura ativa antes da emissão

                vm.onSugestaoSelecionada(
                    SugestaoDescricao(
                        idPatrimonio = 1,
                        numeroPatrimonio = "A-001",
                        descricao = "Cadeira Giratória"
                    )
                )
                advanceUntilIdle()

                recebidos.size shouldBe 1
                recebidos[0] shouldBe
                    ItemSemEtiquetaViewModel.Event.PreencherCampoLivre("Cadeira Giratória")
                job.cancel()
            } finally {
                clearViewModel(vm)
            }
        }
    }

    "P5: seleção NÃO modifica o estado do autocomplete (permanece Oculto com toggle off)" {
        // Reforça Req 3.5: seleção preenche o campo via evento, mas não
        // toca em _termoBusca nem força uma consulta; o estado do
        // autocomplete depende apenas do toggle e do fluxo de debounce.
        runTest(testDispatcher) {
            val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
            val vm = buildVm(buscar)
            try {
                vm.onSugestaoSelecionada(
                    SugestaoDescricao(
                        idPatrimonio = 42,
                        numeroPatrimonio = "B-042",
                        descricao = "Mesa de reunião"
                    )
                )
                advanceUntilIdle()

                // Toggle está off desde o início — nenhuma consulta deve
                // acontecer nem o estado deve sair de Oculto em função
                // de uma seleção.
                coVerify(exactly = 0) { buscar(any(), any(), any(), any()) }
                vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.Oculto>()
            } finally {
                clearViewModel(vm)
            }
        }
    }

    "P5 property: descrição emitida é idêntica à descrição da sugestão selecionada" {
        runTest(testDispatcher) {
            checkAll(iterations = 50, Arb.string(1..100)) { descricao ->
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>(relaxed = true)
                val vm = buildVm(buscar)
                try {
                    val recebidos = mutableListOf<ItemSemEtiquetaViewModel.Event>()
                    val job = backgroundScope.launch { vm.events.collect { recebidos += it } }
                    advanceUntilIdle()

                    vm.onSugestaoSelecionada(
                        SugestaoDescricao(
                            idPatrimonio = 7,
                            numeroPatrimonio = "A-007",
                            descricao = descricao
                        )
                    )
                    advanceUntilIdle()

                    recebidos.size shouldBe 1
                    val ev = recebidos[0] as ItemSemEtiquetaViewModel.Event.PreencherCampoLivre
                    ev.texto shouldBe descricao
                    job.cancel()
                } finally {
                    clearViewModel(vm)
                }
            }
        }
    }
})
