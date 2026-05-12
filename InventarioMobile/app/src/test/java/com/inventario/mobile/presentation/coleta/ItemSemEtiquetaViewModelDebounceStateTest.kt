package com.inventario.mobile.presentation.coleta

import com.inventario.mobile.domain.model.OrigemSugestoes
import com.inventario.mobile.domain.model.ResultadoSugestoes
import com.inventario.mobile.domain.model.SugestaoDescricao
import com.inventario.mobile.domain.usecase.BuscarSugestoesDescricaoUseCase
import com.inventario.mobile.domain.usecase.LimparCacheDeOutrosInventariosUseCase
import com.inventario.mobile.domain.usecase.MarcarPatrimonioColetadoLocalmenteUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.presentation.coleta.state.SugestaoDescricaoState
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.VibrationHelper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Feature: coleta-descricao-livre-com-sugestao
 *
 * Task 15.12 — Property test **P6: Debounce agrupa alterações rápidas do
 * termo de busca** — **Validates: Requirements 3.7**.
 *
 * Task 15.13 — Property tests **P8: Limites de tamanho na lista exibida**
 * e **P9: Transição entre `Carregado` e `SemResultados`** —
 * **Validates: Requirements 3.3, 3.6, 7.2**.
 *
 * ## Estratégia
 *
 * Todo teste que envolve o `init { viewModelScope.launch { ... } }` do
 * [ItemSemEtiquetaViewModel] precisa controlar o tempo virtual do
 * `Dispatchers.Main` para exercitar o operador `.debounce(300)` aplicado a
 * `_termoBusca`. Adotamos `runTest { ... }`, que fornece um
 * `testScheduler` virtual, e substituímos `Dispatchers.Main` por um
 * `StandardTestDispatcher(testScheduler)` **dentro** do bloco. Assim,
 * `advanceTimeBy(ms)` e `advanceUntilIdle()` avançam simultaneamente o
 * relógio do debounce e o do `viewModelScope`, cumprindo a exigência de
 * tempo virtual da tarefa.
 *
 * Mocks:
 * - [BuscarSugestoesDescricaoUseCase] é reconfigurado por teste via
 *   `coEvery { ... }` conforme a propriedade avaliada (lista fixa,
 *   lista vazia, 50/75 itens, dinâmica por termo).
 * - [PreferencesManager.getInventarioAtivoId] devolve `1` para que o VM
 *   não trate como "sem inventário ativo" (Req 7.1 / 3.6).
 * - Demais dependências são `relaxed = true` — não são exercitadas aqui.
 *
 * Invariantes preservados em todos os testes:
 * - O campo livre **nunca** é desabilitado (Req 7.4).
 * - A descrição final persistida é sempre o conteúdo corrente do campo
 *   livre após `trim()` (Req 4.1) — não exercitado diretamente aqui, mas
 *   preservado implicitamente porque o VM não modifica o estado do campo.
 *
 * Observação sobre a "chamada inicial" do UseCase:
 * Ao ativar o toggle (`setToggleSugestao(true)`), o fluxo
 * `combine(_toggleAtivo, termoBuscaDebounced).filter { ativo }` dispara
 * uma primeira emissão com o termo vazio `""` após o debounce de 300ms
 * (inicialmente, `_termoBusca.value == ""`). Em propriedades que
 * verificam contagem de invocações (P6), essa chamada inicial é admitida
 * (limite `atLeast = 1, atMost = 2`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemSemEtiquetaViewModelDebounceStateTest : StringSpec({

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    /**
     * Constrói um [ItemSemEtiquetaViewModel] com as dependências mínimas
     * necessárias para exercitar o fluxo de sugestões. O UseCase de
     * busca é injetado por parâmetro para que cada teste possa customizar
     * o comportamento (lista fixa, por termo, exceção etc.).
     */
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

    /** Helper para montar um `Result<ResultadoSugestoes>` com [sugestoes]. */
    fun sucessoCom(
        sugestoes: List<SugestaoDescricao>,
        origem: OrigemSugestoes = OrigemSugestoes.SERVIDOR
    ): Result<ResultadoSugestoes> = Result.success(
        ResultadoSugestoes(
            sugestoes = sugestoes,
            origem = origem,
            totalElements = sugestoes.size.toLong(),
            hasNext = false
        )
    )

    /** Gera uma lista de sugestões determinísticas de tamanho [n]. */
    fun listaSugestoes(n: Int, prefixo: String = "item"): List<SugestaoDescricao> =
        (1..n).map { i ->
            SugestaoDescricao(
                idPatrimonio = i,
                numeroPatrimonio = "IFMT-%05d".format(i),
                descricao = "$prefixo-$i"
            )
        }

    // =================================================================
    // Task 15.12 — Property 6: debounce agrupa alterações rápidas
    // Validates: Requirements 3.7
    // =================================================================

    /**
     * Caso determinístico extraído literalmente do enunciado da task.
     *
     * Sequência de 5 `onTermoBuscaChange` com intervalos de 50ms (abaixo
     * do limiar de debounce de 300ms), seguida de 400ms de quietude.
     * Esperado: o UseCase é invocado no máximo duas vezes — uma vez para
     * o termo vazio inicial (ativação do toggle) e uma vez para o
     * termo final `"cadeira"`.
     */
    "P6: 5 emissoes rapidas com 50ms de intervalo colapsam em 1 chamada ao UseCase (alem da inicial vazia)" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                coEvery { buscar(any(), any(), any(), any()) } returns sucessoCom(emptyList())

                val vm = buildVm(buscar)

                // Ativação do toggle — o fluxo inicial emite o termo "".
                vm.setToggleSugestao(true)
                advanceTimeBy(500L) // inicial debounce (300ms) + folga

                // Sequência de alterações rápidas
                vm.onTermoBuscaChange("c")
                advanceTimeBy(50L)
                vm.onTermoBuscaChange("ca")
                advanceTimeBy(50L)
                vm.onTermoBuscaChange("cad")
                advanceTimeBy(50L)
                vm.onTermoBuscaChange("cadeir")
                advanceTimeBy(50L)
                vm.onTermoBuscaChange("cadeira")
                advanceTimeBy(400L) // ultrapassa o limiar de debounce de 300ms

                // 1..2 chamadas: uma para "" (inicial) + uma para "cadeira".
                // Nenhuma das emissões intermediárias ("c", "ca", "cad",
                // "cadeir") pode ter atingido o collectLatest, pois o
                // intervalo foi inferior a 300ms.
                coVerify(atLeast = 1, atMost = 2) {
                    buscar(1, any(), 0, 50)
                }
                coVerify(exactly = 1) {
                    buscar(1, "cadeira", 0, 50)
                }
                // Termos intermediários nunca foram solicitados.
                coVerify(exactly = 0) { buscar(any(), "c", any(), any()) }
                coVerify(exactly = 0) { buscar(any(), "ca", any(), any()) }
                coVerify(exactly = 0) { buscar(any(), "cad", any(), any()) }
                coVerify(exactly = 0) { buscar(any(), "cadeir", any(), any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    /**
     * Propriedade universalmente quantificada de P6.
     *
     * Para qualquer sequência não-vazia de termos com intervalos
     * inferiores a 300ms entre emissões consecutivas, seguida de um
     * período de quietude ≥ 500ms, o UseCase é invocado **no máximo duas
     * vezes** (uma eventual chamada inicial para `""` + exatamente uma
     * chamada para o último termo da sequência).
     *
     * A propriedade captura a essência funcional do `.debounce(300)`:
     * colapsar rajadas rápidas em uma única execução com o valor mais
     * recente.
     */
    "P6 property: qualquer rajada rapida (intervalo < 300ms) colapsa em 1 chamada com o ULTIMO termo" {
        // Gera sequências curtas de termos ASCII-like, sempre com 2..10
        // termos (distintos ou não) para permitir que o termo final tenha
        // valor estável após o colapso.
        checkAll(
            iterations = 25,
            Arb.list(Arb.string(1..8), 2..10)
        ) { termosRaw ->
            // Garantir que o último termo seja distinto do valor inicial
            // `""` para que sempre produza ao menos uma invocação nova.
            val termos = termosRaw.map { it.ifBlank { "x" } }

            runTest {
                Dispatchers.setMain(StandardTestDispatcher(testScheduler))
                try {
                    val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                    coEvery { buscar(any(), any(), any(), any()) } returns sucessoCom(emptyList())

                    val vm = buildVm(buscar)
                    vm.setToggleSugestao(true)
                    advanceTimeBy(500L) // processa a emissão inicial (termo vazio)

                    // Rajada: cada emissão com intervalo < 300ms (usamos 50ms).
                    for (t in termos) {
                        vm.onTermoBuscaChange(t)
                        advanceTimeBy(50L)
                    }
                    // Quietude: ultrapassa o limiar de debounce.
                    advanceTimeBy(500L)
                    advanceUntilIdle()

                    // O termo final é o único que pode ter sobrevivido ao
                    // colapso, contando a chamada inicial com "".
                    coVerify(atLeast = 1, atMost = 2) {
                        buscar(1, any(), 0, 50)
                    }
                    coVerify(exactly = 1) {
                        buscar(1, termos.last(), 0, 50)
                    }
                } finally {
                    Dispatchers.resetMain()
                }
            }
        }
    }

    // =================================================================
    // Task 15.13 — Property 8: limites de tamanho na lista exibida
    // Validates: Requirements 3.3, 7.2
    // =================================================================

    /**
     * O VM não aplica teto adicional sobre o que vem do UseCase: o
     * controle de tamanho (`size=50`) vive no contrato do endpoint
     * (Req 5.6, 6.3). Portanto, quando o UseCase devolve exatamente 50
     * itens, o estado deve ser [SugestaoDescricaoState.Carregado] com
     * `sugestoes.size == 50` (Req 3.3).
     */
    "P8: lista com exatamente 50 itens do servidor vira Carregado com size == 50" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                coEvery { buscar(any(), any(), any(), any()) } returns sucessoCom(
                    listaSugestoes(50)
                )

                val vm = buildVm(buscar)
                vm.setToggleSugestao(true)
                advanceTimeBy(500L)
                advanceUntilIdle()

                val estado = vm.sugestaoState.value
                estado.shouldBeInstanceOf<SugestaoDescricaoState.Carregado>()
                estado.sugestoes shouldHaveSize 50
                estado.origem shouldBe OrigemSugestoes.SERVIDOR
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    /**
     * Se, por algum motivo, o servidor responder com mais itens do que o
     * `size` solicitado, o VM **não** aplica truncamento adicional — o
     * contrato é do servidor/use case. Isso documenta explicitamente que
     * o teto é externo ao ViewModel e evita regressões silenciosas.
     */
    "P8: lista com 75 itens (acima do size=50) e exibida integralmente pelo VM" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                coEvery { buscar(any(), any(), any(), any()) } returns sucessoCom(
                    listaSugestoes(75)
                )

                val vm = buildVm(buscar)
                vm.setToggleSugestao(true)
                advanceTimeBy(500L)
                advanceUntilIdle()

                val estado = vm.sugestaoState.value
                estado.shouldBeInstanceOf<SugestaoDescricaoState.Carregado>()
                estado.sugestoes shouldHaveSize 75
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    // =================================================================
    // Task 15.13 — Property 9: transição entre Carregado e SemResultados
    // Validates: Requirements 3.3, 3.6, 7.2
    // =================================================================

    /**
     * Transição 1: `Oculto → Carregado`.
     *
     * Estado inicial: [SugestaoDescricaoState.Oculto]. Ao ativar o toggle
     * e o UseCase retornar uma lista não vazia, o VM deve transicionar
     * para [SugestaoDescricaoState.Carregado] com a `origem` reportada
     * pelo UseCase (Req 3.3).
     */
    "P9: Oculto -> Carregado quando UseCase retorna lista nao-vazia" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                coEvery { buscar(any(), any(), any(), any()) } returns sucessoCom(
                    listaSugestoes(3, "cad")
                )

                val vm = buildVm(buscar)

                // Estado inicial deve ser Oculto (Req 2.2).
                vm.sugestaoState.value shouldBe SugestaoDescricaoState.Oculto

                vm.setToggleSugestao(true)
                advanceTimeBy(500L)
                advanceUntilIdle()

                val estado = vm.sugestaoState.value
                estado.shouldBeInstanceOf<SugestaoDescricaoState.Carregado>()
                estado.sugestoes shouldHaveSize 3
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    /**
     * Transição 2: `Carregado → SemResultados`.
     *
     * Ao alterar o termo para um valor para o qual o UseCase retorna
     * lista vazia, o VM deve transicionar para
     * [SugestaoDescricaoState.SemResultados] com a `origem` do UseCase
     * (Req 3.6).
     */
    "P9: Carregado -> SemResultados quando filtro muda e UseCase retorna vazio" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                // Stub por termo: "cadeira" → 2 itens; outros → vazio.
                // Ordem: o mais genérico (match) vem primeiro, depois o
                // específico sobrescreve, pois mockk dá prioridade aos
                // stubs declarados mais recentes.
                coEvery { buscar(1, match { it != "cadeira" }, 0, 50) } returns sucessoCom(emptyList())
                coEvery { buscar(1, "cadeira", 0, 50) } returns sucessoCom(
                    listaSugestoes(2, "cadeira")
                )

                val vm = buildVm(buscar)
                vm.setToggleSugestao(true)
                advanceTimeBy(500L)

                vm.onTermoBuscaChange("cadeira")
                advanceTimeBy(400L)
                advanceUntilIdle()

                vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.Carregado>()

                vm.onTermoBuscaChange("xyzzz")
                advanceTimeBy(400L)
                advanceUntilIdle()

                val estadoFinal = vm.sugestaoState.value
                estadoFinal.shouldBeInstanceOf<SugestaoDescricaoState.SemResultados>()
                estadoFinal.origem shouldBe OrigemSugestoes.SERVIDOR
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    /**
     * Transição 3: `SemResultados → Carregado`.
     *
     * O VM deve recuperar-se da ausência de resultados assim que o
     * filtro volte a produzir ao menos uma sugestão (Req 3.6).
     */
    "P9: SemResultados -> Carregado quando filtro volta a encontrar" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                coEvery { buscar(1, "xyzzz", 0, 50) } returns sucessoCom(emptyList())
                coEvery { buscar(1, "mesa", 0, 50) } returns sucessoCom(
                    listaSugestoes(5, "mesa")
                )
                // fallback para chamada inicial com ""
                coEvery { buscar(1, "", 0, 50) } returns sucessoCom(emptyList())

                val vm = buildVm(buscar)
                vm.setToggleSugestao(true)
                advanceTimeBy(500L)

                vm.onTermoBuscaChange("xyzzz")
                advanceTimeBy(400L)
                advanceUntilIdle()
                vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.SemResultados>()

                vm.onTermoBuscaChange("mesa")
                advanceTimeBy(400L)
                advanceUntilIdle()

                val estado = vm.sugestaoState.value
                estado.shouldBeInstanceOf<SugestaoDescricaoState.Carregado>()
                estado.sugestoes shouldHaveSize 5
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    /**
     * Transição 4: `Carregado → Oculto` via desativação do toggle
     * (Req 2.2, 2.4).
     *
     * [ItemSemEtiquetaViewModel.setToggleSugestao]`(false)` deve levar o
     * estado para [SugestaoDescricaoState.Oculto] imediatamente,
     * independentemente de haver sugestões carregadas.
     */
    "P9: Carregado -> Oculto quando toggle e desativado" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                coEvery { buscar(any(), any(), any(), any()) } returns sucessoCom(
                    listaSugestoes(4)
                )

                val vm = buildVm(buscar)
                vm.setToggleSugestao(true)
                advanceTimeBy(500L)
                advanceUntilIdle()
                vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.Carregado>()

                vm.setToggleSugestao(false)
                // A transição para Oculto é síncrona no próprio setToggleSugestao;
                // advanceUntilIdle apenas drena quaisquer tasks pendentes.
                advanceUntilIdle()

                vm.sugestaoState.value shouldBe SugestaoDescricaoState.Oculto
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    /**
     * Transição 5: `SemResultados → Oculto` via desativação do toggle
     * (Req 2.2, 2.4).
     *
     * Complementa a transição 4: mesma invariante a partir do estado
     * `SemResultados`.
     */
    "P9: SemResultados -> Oculto quando toggle e desativado" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
                coEvery { buscar(any(), any(), any(), any()) } returns sucessoCom(emptyList())

                val vm = buildVm(buscar)
                vm.setToggleSugestao(true)
                advanceTimeBy(500L)
                advanceUntilIdle()
                // Sem resultados para o termo "" inicial.
                vm.sugestaoState.value.shouldBeInstanceOf<SugestaoDescricaoState.SemResultados>()

                vm.setToggleSugestao(false)
                advanceUntilIdle()

                vm.sugestaoState.value shouldBe SugestaoDescricaoState.Oculto
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})
