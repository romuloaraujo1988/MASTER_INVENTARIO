# Implementation Plan: Paginação e Busca de Salas

> ⚠️ **SPEC SUPERADA POR DECISÃO ARQUITETURAL** — 31/05/2026
>
> O objetivo de UX (busca de salas rápida, sem travar a tela) **já está
> atendido**, porém por um caminho diferente do prescrito. A tela ativa
> `SalaSelectionActivity` usa o `SalaSelectionViewModel`, que carrega **todas**
> as salas de uma vez (offline-first do Room + `SalaCache`) e filtra em memória
> com debounce de 500ms. Para o volume de salas do IFMT isso é rápido e simples.
>
> A infraestrutura Paging 3 chegou a ser criada, mas está **morta** (não
> integrada a nenhuma Activity):
> - `data/paging/SalaPagingSource.kt`
> - `presentation/sala/SalaPagingAdapter.kt`, `SalaLoadStateAdapter.kt`
> - `data/repository/SalaRepositoryImpl.kt` (configura o `Pager`)
> - `api/SalaApi.listarSalasPaginado()` / `buscarSalasPorNome()`
>
> A `SalaSelectionActivity` traz o comentário `// TODO: Migrar completamente
> para SalaSelectionViewModelClean com Paging 3`, mas a abordagem "carregar
> tudo" foi adotada conscientemente e tornou a paginação desnecessária.
>
> **Decisão:** as tarefas de paginação foram **retiradas** (não há problema de
> performance real a resolver hoje). Só reabrir se a lista de salas crescer a
> ponto de o carregamento total ficar lento — nesse caso, terminar a integração
> do `SalaPagingSource`/`SalaPagingAdapter` na Activity. Evitar manter as duas
> abordagens coexistindo: se a paginação não for retomada, considerar remover a
> infra morta acima para reduzir código não utilizado.
