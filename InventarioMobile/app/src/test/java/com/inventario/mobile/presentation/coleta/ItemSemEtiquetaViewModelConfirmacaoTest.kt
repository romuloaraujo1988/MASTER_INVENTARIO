package com.inventario.mobile.presentation.coleta

import com.inventario.mobile.domain.model.Coleta
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
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Feature: coleta-descricao-livre-com-sugestao
 *
 * Task 15.8 — Property test **P2: Trim + validação de tamanho no app na confirmação**
 *   **Validates: Requirements 1.3, 1.4, 1.5, 1.6, 4.4, 4.5**
 *
 * Task 15.9 — Property test **P3: Descrição final é sempre `currentText.trim()`
 *             e persistida apenas no campo alvo**
 *   **Validates: Requirements 4.1, 4.2, 4.3, 9.1**
 *
 * ## Enunciado formal adaptado
 *
 * **P2 (validação + trim):** para qualquer descrição `d` informada a
 * [ItemSemEtiquetaViewModel.registrarItemSemEtiqueta], seja `t = d.trim()`:
 *
 * ```
 *  invocou RegistrarColetaUseCase.registrarItemSemEtiqueta   ⇔   3 ≤ t.length ≤ 255
 * ```
 *
 * Quando a condição é falsa, o VM define `state = ItemSemEtiquetaState.Error(...)`
 * de forma síncrona (antes de `viewModelScope.launch`), preservando o texto
 * original e sem produzir efeito colateral no domínio (Req 1.4, 1.5, 1.6,
 * 4.4, 4.5). Quando verdadeira, o VM chama o use case com `descricao = t`
 * (Req 1.3, 4.4).
 *
 * **P3 (descrição final):** o argumento `descricao` capturado no use case
 * é **sempre** igual a `textoOriginal.trim()`, independentemente de eventos
 * intercalados como seleção de sugestão via
 * [ItemSemEtiquetaViewModel.onSugestaoSelecionada] (Req 4.2). O VM não
 * mantém vínculo forte com o item selecionado: a descrição final provém
 * unicamente do parâmetro `descricao` de `registrarItemSemEtiqueta`, que
 * espelha o conteúdo do `Campo_Descricao_Livre` no momento da confirmação
 * (Req 4.1, 4.3, 9.1).
 *
 * ## Estratégia
 *
 * - `Dispatchers.setMain(UnconfinedTestDispatcher())` antes de cada teste
 *   faz com que `viewModelScope.launch { ... }` execute eagerly, permitindo
 *   inspecionar o `slot` logo após a chamada — o mock retorna imediatamente
 *   com `Result.success(...)`.
 * - `slot<String>` em mockk captura o valor de `descricao` passado ao use
 *   case. As demais dependências do VM são mocks relaxados — não nos
 *   interessa seu comportamento neste teste.
 * - Validações **síncronas** (antes de `viewModelScope.launch`) são
 *   observadas diretamente em `vm.state.value` após a chamada, sem
 *   necessidade de `runTest`/scheduler.
 *
 * P2 e P3 vivem no mesmo arquivo para compartilhar o fixture de construção
 * do VM e o `descricaoSlot` — as duas propriedades exercitam a mesma
 * superfície (`registrarItemSemEtiqueta`) sob ângulos diferentes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemSemEtiquetaViewModelConfirmacaoTest : StringSpec({

    // -----------------------------------------------------------------
    // Fixture: Main dispatcher controlado em todos os testes.
    //
    // StringSpec roda cada teste em sequência na mesma thread —
    // setMain/resetMain em beforeTest/afterTest garante isolamento.
    // -----------------------------------------------------------------

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    /**
     * Agrupa os artefatos de teste devolvidos por [buildVm]: a instância
     * do [ItemSemEtiquetaViewModel] sob teste, o mock de
     * [RegistrarColetaUseCase] (para `coVerify`) e o `slot` que captura o
     * argumento `descricao` passado ao use case.
     */
    data class Fixture(
        val vm: ItemSemEtiquetaViewModel,
        val registrar: RegistrarColetaUseCase,
        val descricaoSlot: CapturingSlot<String>
    )

    fun buildVm(): Fixture {
        val registrar = mockk<RegistrarColetaUseCase>()
        val descricaoSlot = slot<String>()

        // Coleta "stub" satisfazendo o tipo Result<Coleta>; nenhum caminho
        // no VM depende de seu conteúdo durante este teste.
        val coletaStub = Coleta(
            id = 1L,
            patrimonioId = 0L,
            usuarioId = 1L,
            status = "COLETADO"
        )

        coEvery {
            registrar.registrarItemSemEtiqueta(
                descricao = capture(descricaoSlot),
                categoria = any(),
                salaId = any(),
                localizacaoAtual = any(),
                estadoEncontrado = any(),
                observacoes = any(),
                latitude = any(),
                longitude = any(),
                fotoPath = any(),
                fotoThumbnailPath = any()
            )
        } returns Result.success(coletaStub)

        val buscar = mockk<BuscarSugestoesDescricaoUseCase>()
        coEvery { buscar(any(), any(), any(), any()) } returns Result.success(
            ResultadoSugestoes(
                sugestoes = emptyList(),
                origem = OrigemSugestoes.SERVIDOR,
                totalElements = 0L,
                hasNext = false
            )
        )

        val marcar = mockk<MarcarPatrimonioColetadoLocalmenteUseCase>(relaxed = true)
        val limpar = mockk<LimparCacheDeOutrosInventariosUseCase>(relaxed = true)
        val prefs = mockk<PreferencesManager>()
        every { prefs.getInventarioAtivoId() } returns 1
        val vib = mockk<VibrationHelper>(relaxed = true)

        val vm = ItemSemEtiquetaViewModel(
            registrarColetaUseCase = registrar,
            buscarSugestoesDescricaoUseCase = buscar,
            marcarPatrimonioColetadoLocalmenteUseCase = marcar,
            limparCacheDeOutrosInventariosUseCase = limpar,
            preferencesManager = prefs,
            vibrationHelper = vib
        )
        return Fixture(vm, registrar, descricaoSlot)
    }

    /**
     * Invoca [ItemSemEtiquetaViewModel.registrarItemSemEtiqueta] com
     * valores fixos para os campos que não estão em teste. O foco desta
     * suíte é o parâmetro `descricao`; os demais servem apenas para
     * satisfazer a assinatura.
     */
    fun invocarRegistro(vm: ItemSemEtiquetaViewModel, descricao: String) {
        vm.registrarItemSemEtiqueta(
            descricao = descricao,
            categoria = "MOVEL",
            estado = "BOM",
            localizacao = "Sala 101",
            observacoes = "",
            fotoPath = "/tmp/foto.jpg",
            fotoThumbnailPath = null
        )
    }

    // =================================================================
    // P2 — Trim + validação de tamanho na confirmação
    // =================================================================

    "P2: descricao com whitespace circundante e length valida apos trim e aceita" {
        val f = buildVm()

        invocarRegistro(f.vm, "  cadeira  ")

        // O VM aplica trim antes de delegar: "  cadeira  " -> "cadeira" (7).
        f.descricaoSlot.captured shouldBe "cadeira"
        coVerify(exactly = 1) {
            f.registrar.registrarItemSemEtiqueta(
                descricao = "cadeira",
                categoria = any(),
                salaId = any(),
                localizacaoAtual = any(),
                estadoEncontrado = any(),
                observacoes = any(),
                latitude = any(),
                longitude = any(),
                fotoPath = any(),
                fotoThumbnailPath = any()
            )
        }
    }

    "P2: descricao com trim length < 3 e rejeitada (use case nao chamado, estado Error)" {
        listOf("ab", " ", "", "  a  ", "\t\n").forEach { entrada ->
            val f = buildVm()

            invocarRegistro(f.vm, entrada)

            f.vm.state.value.shouldBeInstanceOf<ItemSemEtiquetaState.Error>()
            coVerify(exactly = 0) {
                f.registrar.registrarItemSemEtiqueta(
                    descricao = any(),
                    categoria = any(),
                    salaId = any(),
                    localizacaoAtual = any(),
                    estadoEncontrado = any(),
                    observacoes = any(),
                    latitude = any(),
                    longitude = any(),
                    fotoPath = any(),
                    fotoThumbnailPath = any()
                )
            }
        }
    }

    "P2: descricao com trim length > 255 e rejeitada (use case nao chamado, estado Error)" {
        val f = buildVm()

        val trezentos = "a".repeat(300)
        invocarRegistro(f.vm, trezentos)

        f.vm.state.value.shouldBeInstanceOf<ItemSemEtiquetaState.Error>()
        coVerify(exactly = 0) {
            f.registrar.registrarItemSemEtiqueta(
                descricao = any(),
                categoria = any(),
                salaId = any(),
                localizacaoAtual = any(),
                estadoEncontrado = any(),
                observacoes = any(),
                latitude = any(),
                longitude = any(),
                fotoPath = any(),
                fotoThumbnailPath = any()
            )
        }
    }

    // Propriedade: para todo n em {0, 1, 2, 3, 10, 100, 255, 256, 300},
    // a invocação do use case ocorre sse 3 ≤ n ≤ 255.
    "P2 property: invocacao ao use case sse 3 <= trim.length <= 255" {
        val lengths = listOf(0, 1, 2, 3, 10, 100, 255, 256, 300)

        lengths.forEach { n ->
            val f = buildVm()

            val entrada = "a".repeat(n)
            invocarRegistro(f.vm, entrada)

            val devePassar = n in 3..255
            if (devePassar) {
                coVerify(exactly = 1) {
                    f.registrar.registrarItemSemEtiqueta(
                        descricao = any(),
                        categoria = any(),
                        salaId = any(),
                        localizacaoAtual = any(),
                        estadoEncontrado = any(),
                        observacoes = any(),
                        latitude = any(),
                        longitude = any(),
                        fotoPath = any(),
                        fotoThumbnailPath = any()
                    )
                }
                // Sem whitespace circundante, trim é idempotente.
                f.descricaoSlot.captured shouldBe entrada
            } else {
                coVerify(exactly = 0) {
                    f.registrar.registrarItemSemEtiqueta(
                        descricao = any(),
                        categoria = any(),
                        salaId = any(),
                        localizacaoAtual = any(),
                        estadoEncontrado = any(),
                        observacoes = any(),
                        latitude = any(),
                        longitude = any(),
                        fotoPath = any(),
                        fotoThumbnailPath = any()
                    )
                }
                f.vm.state.value.shouldBeInstanceOf<ItemSemEtiquetaState.Error>()
            }
        }
    }

    // =================================================================
    // P3 — Descrição final é sempre `input.trim()` (Req 4.1, 4.2, 4.3, 9.1)
    // =================================================================

    /**
     * Arbitrary de whitespace ASCII (espaço, tab, newline, CR, form-feed).
     * Gera strings cujo [String.trim] resulta em string vazia — desta forma,
     * dado um `core` sem whitespace nas bordas, vale a identidade
     * `(prefixo + core + sufixo).trim() == core.trim()`.
     *
     * Usamos o builder `arbitrary { }` (padrão do projeto) para compor
     * tamanhos e caracteres sem depender de `.map` na API de Arb.
     */
    val whitespaceSegmentArb: Arb<String> = arbitrary { rs ->
        val size = Arb.int(0..5).next(rs)
        val chars = charArrayOf(' ', '\t', '\n', '\r', '\u000C')
        buildString(size) {
            repeat(size) {
                val idx = Arb.int(0 until chars.size).next(rs)
                append(chars[idx])
            }
        }
    }

    "P3 property: descricao final enviada ao use case e sempre input.trim()" {
        checkAll(
            iterations = 100,
            Arb.string(3..255),
            whitespaceSegmentArb,
            whitespaceSegmentArb
        ) { core, prefixo, sufixo ->
            // Pula casos cujo `core.trim()` cai fora do intervalo válido —
            // a validação P2 rejeita, use case não é chamado, slot fica vazio.
            val coreTrim = core.trim()
            if (coreTrim.length in 3..255) {
                val f = buildVm()

                val input = prefixo + core + sufixo
                invocarRegistro(f.vm, input)

                f.descricaoSlot.captured shouldBe input.trim()
            }
        }
    }

    "P3: onSugestaoSelecionada nao altera o que sera passado ao use case" {
        val f = buildVm()

        // Usuário seleciona uma sugestão cuja descrição é "OutraCoisa"…
        f.vm.onSugestaoSelecionada(
            SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A-001", descricao = "OutraCoisa")
        )

        // …mas a confirmação passa um texto livre diferente. O VM não mantém
        // vínculo forte com a sugestão (Req 4.2) — a descrição final é o
        // conteúdo do campo livre no momento da confirmação, após trim
        // (Req 4.1, 4.3).
        invocarRegistro(f.vm, "  texto livre  ")

        f.descricaoSlot.captured shouldBe "texto livre"
    }
})
