package com.inventario.mobile.presentation.dashboard

import com.inventario.mobile.domain.model.DashboardStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.jqwik.api.Example

/**
 * Teste de EXEMPLO (não-PBT) para o re-render do Dashboard após uma nova emissão da
 * `fonteEstatisticas` (Req 1.6, 2.4).
 *
 * Abordagem — fronteira do mapper (mapper-boundary):
 *  O `DashboardFragment` é uma camada fina que apenas renderiza; toda a lógica de
 *  apresentação vive em [DashboardUiMapper] (Clean Architecture + MVVM, Req 9.1).
 *  Como o módulo Android não configura Robolectric/Espresso (não há tais dependências
 *  em `app/build.gradle`), e para evitar adicionar novos frameworks de teste, este
 *  teste valida o comportamento de re-render de forma determinística na fronteira do
 *  mapper:
 *
 *   1. modela-se `fonteEstatisticas` como um `MutableStateFlow<DashboardStats>` (mesmo
 *      tipo exposto por `DashboardViewModelClean.fonteEstatisticas`);
 *   2. simula-se uma emissão inicial (stats antigos) e depois uma NOVA emissão (stats
 *      novos);
 *   3. aplica-se exatamente o que o `render()` do Fragment aplicaria — `kpiValues`,
 *      `clampPercent` e `completionLabel` — a cada emissão;
 *   4. verifica-se que os valores derivados (textos de KPI, percentual e rótulo de
 *      conclusão) passam a refletir a ÚLTIMA emissão, e não a anterior.
 *
 *  Isso cobre o Req 1.6 (o texto do Cartão_KPI reflete o novo valor da fonte) e o
 *  Req 2.4 (o Indicador_Conclusão e seu rótulo refletem o novo `percentualConclusao`)
 *  sem depender de instrumentação de UI.
 *
 * Biblioteca: jqwik (@Example) na JUnit 5 Platform, alinhada aos testes irmãos do
 * mapper. Os `@Example` retornam `Boolean` (false reprova o teste).
 */
class DashboardReRenderExampleTest {

    /**
     * Snapshot dos valores que o `render()` do Fragment derivaria de um
     * [DashboardStats], na fronteira do mapper. Equality estrutural (data class)
     * permite comparar "antes" e "depois" de uma re-emissão.
     */
    private data class RenderedDashboard(
        val kpis: KpiValues,
        val percent: Int,
        val completionLabel: String
    )

    /**
     * Reproduz fielmente o trecho relevante de `DashboardFragment.render(stats, ...)`:
     * mapeia os KPIs e deriva o percentual/rótulo de conclusão a partir dos `stats`.
     */
    private fun render(stats: DashboardStats): RenderedDashboard = RenderedDashboard(
        kpis = DashboardUiMapper.kpiValues(stats),
        percent = DashboardUiMapper.clampPercent(stats.percentualConclusao),
        completionLabel = DashboardUiMapper.completionLabel(stats.percentualConclusao)
    )

    private val statsAntigos = DashboardStats(
        totalPatrimonios = 100,
        totalColetados = 20,
        totalPendentes = 80,
        percentualConclusao = 20.0,
        coletoresAtivos = 2,
        divergencias = 1,
        inventarioId = 7,
        inventarioNome = "Inventário 2025"
    )

    private val statsNovos = DashboardStats(
        totalPatrimonios = 100,
        totalColetados = 65,
        totalPendentes = 35,
        percentualConclusao = 65.4,
        coletoresAtivos = 5,
        divergencias = 3,
        inventarioId = 7,
        inventarioNome = "Inventário 2025"
    )

    // -----------------------------------------------------------------------------------
    // Req 1.6 / 2.4 — verificação direta via StateFlow.value (conflation garante o último)
    // -----------------------------------------------------------------------------------

    @Example
    fun `nova emissao atualiza KPIs percentual e rotulo para o valor mais recente`(): Boolean {
        val fonteEstatisticas = MutableStateFlow(statsAntigos)

        val antes = render(fonteEstatisticas.value)

        // Nova emissão na fonte única de verdade.
        fonteEstatisticas.value = statsNovos
        val depois = render(fonteEstatisticas.value)

        val antesRefleteEmissaoAntiga =
            antes.kpis.coletados == "20" &&
                antes.kpis.pendentes == "80" &&
                antes.kpis.divergencias == "1" &&
                antes.kpis.coletores == "2" &&
                antes.percent == 20 &&
                antes.completionLabel == "20% concluído"

        val depoisRefleteEmissaoNova =
            depois.kpis.coletados == "65" &&
                depois.kpis.pendentes == "35" &&
                depois.kpis.divergencias == "3" &&
                depois.kpis.coletores == "5" &&
                depois.percent == 65 && // 65.4 arredonda para 65
                depois.completionLabel == "65% concluído"

        // O re-render deve ter mudado de fato (não ficou preso na emissão anterior).
        return antesRefleteEmissaoAntiga && depoisRefleteEmissaoNova && antes != depois
    }

    // -----------------------------------------------------------------------------------
    // Req 1.6 / 2.4 — coletando emissões sucessivas (demonstra o re-render por emissão)
    // -----------------------------------------------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    @Example
    fun `coletor recebe re-render apos cada emissao refletindo a ultima`(): Boolean {
        var resultado = false

        runTest {
            val fonteEstatisticas = MutableStateFlow(statsAntigos)
            val renderizacoes = mutableListOf<RenderedDashboard>()

            // Coleta eager (Unconfined) — cada emissão dispara um "render" imediato,
            // como o bloco repeatOnLifecycle(STARTED) do Fragment faria.
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                fonteEstatisticas.collect { stats ->
                    renderizacoes.add(render(stats))
                }
            }

            // Emite um novo valor de estatísticas (pull-to-refresh / atualização do servidor).
            fonteEstatisticas.value = statsNovos
            runCurrent()

            job.cancel()

            val primeiro = renderizacoes.first()
            val ultimo = renderizacoes.last()

            resultado =
                renderizacoes.size >= 2 &&
                    // Primeira renderização reflete os stats antigos.
                    primeiro.percent == 20 &&
                    primeiro.kpis.coletados == "20" &&
                    // Última renderização reflete a NOVA emissão (Req 1.6, 2.4).
                    ultimo.percent == 65 &&
                    ultimo.completionLabel == "65% concluído" &&
                    ultimo.kpis.coletados == "65" &&
                    ultimo.kpis.pendentes == "35" &&
                    ultimo != primeiro
        }

        return resultado
    }

    // -----------------------------------------------------------------------------------
    // Req 2.4 — clamp aplicado a cada nova emissão (valor fora de faixa vira limite)
    // -----------------------------------------------------------------------------------

    @Example
    fun `nova emissao com percentual fora de faixa e clampado no re-render`(): Boolean {
        val fonteEstatisticas = MutableStateFlow(statsAntigos)
        val antes = render(fonteEstatisticas.value)

        // Emissão com percentual acima de 100 deve ser limitada a 100 (Req 2.5) e o
        // rótulo deve refletir o novo valor clampado após a re-emissão (Req 2.4).
        fonteEstatisticas.value = statsNovos.copy(percentualConclusao = 142.0)
        val depois = render(fonteEstatisticas.value)

        return antes.percent == 20 &&
            depois.percent == 100 &&
            depois.completionLabel == "100% concluído" &&
            antes != depois
    }
}
