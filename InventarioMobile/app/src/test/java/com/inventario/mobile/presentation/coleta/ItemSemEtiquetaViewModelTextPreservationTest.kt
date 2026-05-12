package com.inventario.mobile.presentation.coleta

import com.inventario.mobile.domain.model.OrigemSugestoes
import com.inventario.mobile.domain.model.ResultadoSugestoes
import com.inventario.mobile.domain.model.SugestaoDescricao
import com.inventario.mobile.domain.usecase.BuscarSugestoesDescricaoUseCase
import com.inventario.mobile.domain.usecase.LimparCacheDeOutrosInventariosUseCase
import com.inventario.mobile.domain.usecase.MarcarPatrimonioColetadoLocalmenteUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.VibrationHelper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Feature: coleta-descricao-livre-com-sugestao
 * Task 15.7 — Property test **P1: Preservação do texto do campo livre sob
 * transições arbitrárias**.
 *
 * **Validates: Requirements 1.2, 2.3, 2.4, 7.4**
 *
 * ## Enunciado formal adaptado
 *
 * O texto do campo livre (a `Descricao_Final` em formação) é a única fonte
 * de verdade do que será persistido (Req 4.1) — nenhuma transição do
 * `Toggle_Sugestao` (Req 2.3, 2.4), seleção de sugestão (Req 3.5, 4.2),
 * nem mudança de conectividade (Req 7.4) deve modificar esse texto dentro
 * do [ItemSemEtiquetaViewModel].
 *
 * O VM atual NÃO expõe um `campoLivreText: StateFlow<String>` — o texto
 * vive no próprio `EditText` da Activity. O único espelho interno do
 * ViewModel é o `_termoBusca: MutableStateFlow<String>`, alimentado por
 * [ItemSemEtiquetaViewModel.onTermoBuscaChange]. Logo, a forma
 * **observável** da propriedade P1 no nível do VM é:
 *
 * > Para qualquer sequência de eventos do VM
 * > (`setToggleSugestao`, `onTermoBuscaChange`, `onSugestaoSelecionada`),
 * > o valor corrente de `_termoBusca` após a sequência é EXATAMENTE igual
 * > ao argumento da última chamada a `onTermoBuscaChange`. Eventos
 * > `setToggleSugestao` e `onSugestaoSelecionada` são **no-ops** sobre
 * > `_termoBusca`.
 *
 * Isto corresponde diretamente ao comportamento auditado na
 * [ItemSemEtiquetaViewModel]: `setToggleSugestao` só muta
 * `_toggleAtivo` (e `_sugestaoState` no caso `false`), e
 * `onSugestaoSelecionada` só emite um evento one-shot via `_events` — a
 * UI é responsável por aplicar o texto ao `EditText` e, só depois, o
 * `TextWatcher` chamará `onTermoBuscaChange` (fora do escopo deste teste).
 *
 * ## Estratégia de verificação
 *
 * Como `_termoBusca` é `private`, lemos seu valor por reflexão após cada
 * sequência. Isso preserva o encapsulamento do ViewModel em produção e
 * evita introduzir superfícies de teste (`@VisibleForTesting`) que
 * acabariam expostas também a código de aplicação.
 *
 * Mocks:
 * - [BuscarSugestoesDescricaoUseCase] retorna sucesso com lista vazia —
 *   o teste não depende do resultado do autocomplete, apenas que o
 *   coroutine de debounce não quebre.
 * - [PreferencesManager] retorna `null` para `getInventarioAtivoId()` para
 *   manter o `init` do VM fechado num caminho simples.
 * - Demais dependências são relaxadas (sem comportamento exercitado).
 *
 * Como o `init` do ViewModel dispara corrotinas via `viewModelScope`
 * (Main dispatcher), configuramos `Dispatchers.setMain` com um
 * [UnconfinedTestDispatcher] antes de cada teste.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemSemEtiquetaViewModelTextPreservationTest : StringSpec({

    // -----------------------------------------------------------------
    // Fixture: Main dispatcher controlado em todos os testes.
    //
    // StringSpec executa cada test em sequência na mesma thread —
    // setMain/resetMain dentro do bloco DSL garante isolamento.
    // -----------------------------------------------------------------
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    fun buildVm(): ItemSemEtiquetaViewModel {
        val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
        coEvery { buscar(any(), any(), any(), any()) } returns Result.success(
            ResultadoSugestoes(
                sugestoes = emptyList(),
                origem = OrigemSugestoes.SERVIDOR,
                totalElements = 0L,
                hasNext = false
            )
        )

        val registrar = mockk<RegistrarColetaUseCase>(relaxed = true)
        val marcar = mockk<MarcarPatrimonioColetadoLocalmenteUseCase>(relaxed = true)
        val limpar = mockk<LimparCacheDeOutrosInventariosUseCase>(relaxed = true)
        val prefs = mockk<PreferencesManager>()
        every { prefs.getInventarioAtivoId() } returns null
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

    /**
     * Lê por reflexão o valor corrente do campo privado `_termoBusca`.
     *
     * Justificativa: manter o campo privado em produção (apenas a Activity
     * deve escrever nele via [ItemSemEtiquetaViewModel.onTermoBuscaChange])
     * e evitar introduzir getters de teste na superfície pública do VM.
     */
    @Suppress("UNCHECKED_CAST")
    fun termoBuscaValor(vm: ItemSemEtiquetaViewModel): String {
        val field = ItemSemEtiquetaViewModel::class.java.getDeclaredField("_termoBusca")
        field.isAccessible = true
        val flow = field.get(vm) as MutableStateFlow<String>
        return flow.value
    }

    // -----------------------------------------------------------------
    // Casos unitários determinísticos (complementam a propriedade)
    // -----------------------------------------------------------------

    "P1: setToggleSugestao não altera o texto interno (preservação sob toggle)" {
        val vm = buildVm()

        vm.onTermoBuscaChange("cadeira giratória preta")
        vm.setToggleSugestao(true)
        vm.setToggleSugestao(false)
        vm.setToggleSugestao(true)

        termoBuscaValor(vm) shouldBe "cadeira giratória preta"
    }

    "P1: onSugestaoSelecionada não altera o texto interno (preservação sob seleção)" {
        val vm = buildVm()

        vm.onTermoBuscaChange("mesa de reunião")
        // O VM apenas emite evento one-shot; quem atualiza o texto é a UI,
        // que depois chamará onTermoBuscaChange novamente (não simulado aqui
        // de propósito — testamos que sozinho o evento NÃO muda _termoBusca).
        vm.onSugestaoSelecionada(
            SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A-001", descricao = "Cadeira")
        )

        termoBuscaValor(vm) shouldBe "mesa de reunião"
    }

    "P1: texto inicial é string vazia antes de qualquer evento" {
        val vm = buildVm()
        termoBuscaValor(vm) shouldBe ""
    }

    "P1: texto vazio permanece vazio sob toggles e seleções" {
        val vm = buildVm()

        vm.setToggleSugestao(true)
        vm.onSugestaoSelecionada(SugestaoDescricao(1, "A-001", "X"))
        vm.setToggleSugestao(false)

        termoBuscaValor(vm) shouldBe ""
    }

    // -----------------------------------------------------------------
    // Propriedade principal (quantificação universal)
    // -----------------------------------------------------------------

    "P1 property: última chamada de onTermoBuscaChange determina o valor corrente, independente de eventos intercalados" {
        checkAll(
            iterations = 100,
            // Sequência não-vazia de termos arbitrários (0..64 chars, qualquer caractere Unicode)
            Arb.list(Arb.string(0..64), 1..20)
        ) { termos ->
            val vm = buildVm()

            for (termo in termos) {
                vm.onTermoBuscaChange(termo)
                // Intercalar eventos que NÃO devem alterar o texto interno
                vm.setToggleSugestao(true)
                vm.setToggleSugestao(false)
                vm.onSugestaoSelecionada(
                    SugestaoDescricao(idPatrimonio = 42, numeroPatrimonio = "IFMT-42", descricao = "X")
                )
            }

            termoBuscaValor(vm) shouldBe termos.last()
        }
    }

    "P1 property: sem chamadas a onTermoBuscaChange, nenhuma combinação de toggles/seleções muda o texto" {
        checkAll(
            iterations = 50,
            // Sequência de flags booleanos representando pares de eventos (toggle/selecao)
            Arb.list(Arb.string(1..8), 0..30)
        ) { ruidos ->
            val vm = buildVm()

            // Aplica apenas eventos que NÃO deveriam alterar _termoBusca
            for (n in ruidos) {
                vm.setToggleSugestao(true)
                vm.setToggleSugestao(false)
                vm.onSugestaoSelecionada(
                    SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = n, descricao = n)
                )
            }

            termoBuscaValor(vm) shouldBe ""
        }
    }
})
