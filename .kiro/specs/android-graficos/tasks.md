# Implementation Plan - Gráficos Android

> ⚠️ **SPEC OBSOLETA / SUPERADA** — 31/05/2026
>
> Os gráficos do app Android **já foram implementados** por outras specs
> (`dashboard-redesign`, `dashboard-refactor-clean`), com arquitetura diferente
> da prescrita aqui. Evidências no código:
> - `build.gradle`: `implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'`
> - `presentation/charts/ChartHelper.kt`: PieChart, BarChart e LineChart
>   (status, progresso, evolução, top itens)
> - `presentation/dashboard/DashboardFragment.kt`: LineChart de evolução com
>   estados loading/vazio/erro
> - `presentation/statistics/`: `StatisticsActivity` com abas Visão Geral /
>   Gráficos / Rankings / Exportar (`ChartsFragment`, `RankingsFragment`)
> - Backend já serve os dados via `MobileDashboardController`
>   (`/stats`, `/evolucao`, `/top-itens`, `/status`, `/distribuicao-sala`,
>   `/top-coletores`, `/setores-pendentes`, `/projecao-termino`)
>
> Diferenças vs. a spec (não bloqueantes): rankings (Req 5) e status por sala
> (Req 7) foram feitos com RecyclerView/adapters em vez de HorizontalBarChart/
> StackedBarChart; a arquitetura DashboardApi/Entity/Dao/cache não foi seguida
> literalmente.
>
> As tarefas foram **retiradas** por estarem superadas. Caso se queira o gráfico
> de barras empilhadas para status por sala (Req 7) ou barras horizontais reais
> para ranking (Req 5), abrir um item focado — são melhorias cosméticas de baixa
> prioridade, não trabalho pendente de base.
