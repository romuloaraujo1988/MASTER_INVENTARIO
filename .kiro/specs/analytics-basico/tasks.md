# Implementation Plan - Analytics Básico

> ⚠️ **SPEC OBSOLETA / SUPERADA** — 31/05/2026
>
> Esta é a spec de analytics mais ampla e antiga. Foi **superada** por specs
> mais focadas, tanto no desktop quanto no mobile:
>
> - **Desktop** (Fases 8–12: DashboardFrame/ChartsPanel/DivergenciasFrame/
>   PerformanceFrame com JFreeChart) → superado por
>   `analytics-divergencias-metricas`, já implementado em
>   `com.inventario.sihcp.analytics` (`AnalyticsDashboardFrame`,
>   `DivergenciasAnalyticsFrame`, `MetricasTempoAnalyticsFrame`,
>   `AnalyticsService`, `AnalyticsDAO`), integrado ao `MainFrame`.
> - **Mobile** (Fases 13–17: AnalyticsActivity/ViewModel/Repository +
>   MPAndroidChart) → superado pelo dashboard/charts/statistics já existentes
>   no app (ver spec `android-graficos`).
> - **Backend** (Fases 1–7: endpoints `/api/analytics/...`, CacheService com
>   Caffeine, materialized views, ExportService) → não há controller
>   `/api/analytics` separado; as métricas equivalentes são servidas por
>   `/api/mobile/dashboard/*`.
>
> As tarefas foram **retiradas** por duplicação/superação. Itens residuais que
> NÃO foram cobertos por outras specs e poderiam virar trabalho futuro próprio,
> se houver interesse de negócio:
> - Comparação entre inventários (Req 10)
> - Sistema de alertas/notificações automáticas (Req 9)
> - Análise de valor patrimonial / depreciação (Req 11)
> - Materialized views + cache (Caffeine) para performance
>
> Recomendação: se algum desses itens for priorizado, criar uma spec enxuta
> dedicada em vez de reabrir este plano.
