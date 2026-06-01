# Implementation Plan: Filtro de Responsável Obrigatório

> ⚠️ **SPEC OBSOLETA / SUPERADA** — 31/05/2026
>
> O comportamento exigido pelos requisitos **já existe** no código, porém numa
> arquitetura diferente da descrita nas tarefas. A tela monolítica
> `InventarioActivity` + `activity_inventario.xml` que as tarefas referenciam
> **não existe mais** — foi refatorada para `TabLayout + ViewPager2` com
> fragments (spec `inventario-abas-filtro-sala`).
>
> Comportamento atual (em `InventarioViewModel` + `InventarioPorResponsavelFragment`):
> - `init` chama apenas `loadResponsaveis()` — não carrega patrimônios
>   automaticamente (Req 7.1/7.2)
> - `loadPatrimonios()` é no-op que define a mensagem
>   "Selecione um responsável para visualizar os patrimônios" (Req 7.4)
> - `loadPatrimoniosByResponsavel()` só carrega ao selecionar um responsável
> - `clearPatrimonios()` / `clearFilters()` retornam ao estado vazio
>
> As tarefas 1–7 referenciam estrutura morta (campos `hasResponsavelSelected`,
> `showEmptyStateMessage` que não existem no `InventarioUiState` atual) e foram
> **retiradas**. O único resíduo defensável seriam testes unitários do
> ViewModel (antiga tarefa 8); se desejado, criar sob a spec de testes.
