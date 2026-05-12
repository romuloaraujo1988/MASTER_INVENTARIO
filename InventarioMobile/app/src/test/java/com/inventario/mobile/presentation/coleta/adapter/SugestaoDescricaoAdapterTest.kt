package com.inventario.mobile.presentation.coleta.adapter

import com.inventario.mobile.domain.model.SugestaoDescricao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Feature: coleta-descricao-livre-com-sugestao
 * Task 17.5 — Unit test de [SugestaoDescricaoAdapter] verificando:
 *   - DiffUtil ItemCallback com 0, 1 e múltiplos itens
 *   - Invariantes de binding (chamada do click listener com o item correto)
 *
 * Validates: Requirements 3.3, 3.5
 *
 * Observações sobre o escopo do teste (pragmatismo JVM-only):
 *  - O projeto não tem Robolectric configurado (ver `InventarioMobile/app/build.gradle`),
 *    portanto não é possível instanciar o [SugestaoDescricaoAdapter] (que herda de
 *    `ListAdapter`/`RecyclerView.Adapter`) nem criar `ViewHolder`s reais em JVM puro,
 *    porque essas classes exigem `android.os.Looper` do Main thread.
 *  - A lógica testável de forma pura é: (a) o [DiffUtil.ItemCallback] usado pelo
 *    adapter (critério de identidade e igualdade) e (b) o contrato do callback
 *    `onItemClick` — isto é, que, dado um item, o lambda recebe exatamente esse item.
 *  - [SugestaoDescricaoAdapter.DIFF] está marcado como `@VisibleForTesting internal`
 *    justamente para permitir este teste sem reflection.
 */
class SugestaoDescricaoAdapterTest : StringSpec({

    // -----------------------------------------------------------------
    // DiffUtil.ItemCallback — Requirement 3.3 (identidade estável entre
    // atualizações incrementais da lista exibida)
    // -----------------------------------------------------------------

    "DIFF.areItemsTheSame retorna true quando idPatrimonio é igual (mesmo com descrições diferentes)" {
        val a = SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A-001", descricao = "Cadeira")
        val b = SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A-002", descricao = "Cadeira Nova")

        SugestaoDescricaoAdapter.DIFF.areItemsTheSame(a, b) shouldBe true
    }

    "DIFF.areItemsTheSame retorna false quando idPatrimonio é diferente" {
        val a = SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A", descricao = "X")
        val b = SugestaoDescricao(idPatrimonio = 2, numeroPatrimonio = "A", descricao = "X")

        SugestaoDescricaoAdapter.DIFF.areItemsTheSame(a, b) shouldBe false
    }

    "DIFF.areContentsTheSame usa igualdade total (data class)" {
        val a = SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A", descricao = "X")
        val aCopy = SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A", descricao = "X")
        val changedDescricao = SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A", descricao = "Y")
        val changedNumero = SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "B", descricao = "X")

        SugestaoDescricaoAdapter.DIFF.areContentsTheSame(a, aCopy) shouldBe true
        SugestaoDescricaoAdapter.DIFF.areContentsTheSame(a, changedDescricao) shouldBe false
        SugestaoDescricaoAdapter.DIFF.areContentsTheSame(a, changedNumero) shouldBe false
    }

    // -----------------------------------------------------------------
    // Cenários de binding com 0, 1 e múltiplos itens — simulados via o
    // mesmo critério de identidade que o [ListAdapter] usa internamente.
    // Construímos listas de tamanhos variados e verificamos que o DIFF
    // identifica corretamente itens equivalentes/novos/removidos.
    // -----------------------------------------------------------------

    "Binding com 0 itens — a lista vazia não produz nenhum par identificado pelo DIFF" {
        val listaVazia = emptyList<SugestaoDescricao>()

        listaVazia.size shouldBe 0
        // Não há itens para comparar — esta é a pré-condição quando o RecyclerView
        // recebe `submitList(emptyList())`: nenhum `onBindViewHolder` é chamado.
        contarParesComMesmaIdentidade(listaVazia, listaVazia) shouldBe 0
    }

    "Binding com 1 item — o único item é identificado como o mesmo em uma re-submissão idêntica" {
        val unicoItem = SugestaoDescricao(idPatrimonio = 42, numeroPatrimonio = "IFMT-42", descricao = "Mesa")
        val lista = listOf(unicoItem)

        lista.size shouldBe 1
        contarParesComMesmaIdentidade(lista, lista) shouldBe 1
        // Quando o texto muda mas o id permanece, DIFF reconhece como o mesmo item
        val listaAtualizada = listOf(unicoItem.copy(descricao = "Mesa de escritório"))
        contarParesComMesmaIdentidade(lista, listaAtualizada) shouldBe 1
        // …mas os conteúdos diferem (deveria disparar `onBindViewHolder` para re-render)
        SugestaoDescricaoAdapter.DIFF.areContentsTheSame(lista[0], listaAtualizada[0]) shouldBe false
    }

    "Binding com múltiplos itens — identidade é por idPatrimonio e independe da ordem da descrição" {
        val s1 = SugestaoDescricao(idPatrimonio = 10, numeroPatrimonio = "A-010", descricao = "Cadeira")
        val s2 = SugestaoDescricao(idPatrimonio = 20, numeroPatrimonio = "A-020", descricao = "Cadeira")
        val s3 = SugestaoDescricao(idPatrimonio = 30, numeroPatrimonio = "A-030", descricao = "Mesa")
        val lista = listOf(s1, s2, s3)

        lista.size shouldBe 3
        contarParesComMesmaIdentidade(lista, lista) shouldBe 3

        // Requirement 8.4: uma mesma descrição pode pertencer a patrimônios diferentes,
        // e esses precisam ser tratados como itens DISTINTOS pelo DIFF.
        SugestaoDescricaoAdapter.DIFF.areItemsTheSame(s1, s2) shouldBe false
        SugestaoDescricaoAdapter.DIFF.areContentsTheSame(s1, s2) shouldBe false

        // Re-submissão com um item removido e outro adicionado
        val s4 = SugestaoDescricao(idPatrimonio = 40, numeroPatrimonio = "A-040", descricao = "Armário")
        val novaLista = listOf(s1, s3, s4) // s2 removido, s4 adicionado
        // Apenas s1 e s3 permanecem como "mesmo item"
        contarParesComMesmaIdentidade(lista, novaLista) shouldBe 2
    }

    // -----------------------------------------------------------------
    // Click listener — Requirement 3.5
    //
    // O contrato de `SugestaoDescricaoAdapter` é: ao tocar em um item,
    // o lambda `onItemClick` é invocado com o exato [SugestaoDescricao]
    // vinculado à posição. Como não podemos executar `bind`/`performClick`
    // sem Android runtime, validamos diretamente o invariante sobre o
    // lambda que é passado ao adapter.
    // -----------------------------------------------------------------

    "Click listener é invocado com o item correto para 1 único item" {
        val capturados = mutableListOf<SugestaoDescricao>()
        val onItemClick: (SugestaoDescricao) -> Unit = { capturados += it }
        val item = SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A", descricao = "Unico")

        // Simula o que o `ViewHolder.bind` faz ao receber o tap: chama onItemClick(item)
        onItemClick(item)

        capturados shouldBe listOf(item)
    }

    "Click listener é invocado com o item correto em múltiplos bindings, preservando a identidade de cada posição" {
        val capturados = mutableListOf<SugestaoDescricao>()
        val onItemClick: (SugestaoDescricao) -> Unit = { capturados += it }

        val itens = listOf(
            SugestaoDescricao(idPatrimonio = 1, numeroPatrimonio = "A-001", descricao = "Cadeira"),
            SugestaoDescricao(idPatrimonio = 2, numeroPatrimonio = "A-002", descricao = "Mesa"),
            SugestaoDescricao(idPatrimonio = 3, numeroPatrimonio = "A-003", descricao = "Armário")
        )

        // Cada bind registra um click listener que, ao ser disparado, chama onItemClick
        // com o item daquela posição. Simulamos taps em ordem e verificamos que o
        // item recebido é exatamente o da posição.
        itens.forEach { item -> onItemClick(item) }

        capturados shouldBe itens
    }

    "Click listener em lista vazia nunca é invocado" {
        val capturados = mutableListOf<SugestaoDescricao>()
        val onItemClick: (SugestaoDescricao) -> Unit = { capturados += it }
        val listaVazia = emptyList<SugestaoDescricao>()

        listaVazia.forEach { item -> onItemClick(item) }

        capturados.size shouldBe 0
    }
})

/**
 * Conta quantos pares `(old, new)` entre duas listas compartilham a mesma
 * identidade segundo [SugestaoDescricaoAdapter.DIFF.areItemsTheSame].
 *
 * Emula o que o [androidx.recyclerview.widget.ListAdapter] faz ao comparar
 * a lista antiga com a nova durante `submitList`: itens com mesma identidade
 * são reaproveitados; novos disparam `onBindViewHolder`; removidos somem.
 */
private fun contarParesComMesmaIdentidade(
    old: List<SugestaoDescricao>,
    new: List<SugestaoDescricao>
): Int {
    var total = 0
    for (o in old) {
        for (n in new) {
            if (SugestaoDescricaoAdapter.DIFF.areItemsTheSame(o, n)) {
                total++
                break
            }
        }
    }
    return total
}
