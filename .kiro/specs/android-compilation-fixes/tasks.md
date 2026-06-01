# Implementation Plan - Correção de Erros de Compilação Android

> ⚠️ **SPEC OBSOLETA / SUPERADA** — 31/05/2026
>
> Esta spec planejava destravar a compilação do app Android corrigindo
> DAOs, Mappers, Repositories, Strategies, ViewModels e Activities. **O app
> compila atualmente** (builds #024–#026 no `BUILD_HISTORY.md`, incluindo
> release com R8).
>
> Os artefatos que a spec pedia para criar já existem e evoluíram:
> - Mappers: `PatrimonioMapper`, `SalaMapper`, `ColetaMapper` (+ Dashboard,
>   HistoricoScan, Sincronizacao) em `data/mapper/`
> - DAOs, Repositories e Strategies (`LocalDataSourceStrategy`,
>   `RemoteDataSourceStrategy`) presentes e funcionais em `data/strategy/`
> - ViewModels Clean operando com Hilt
> - Os arquivos problemáticos `DispositivoRepository.kt` e `HeartbeatService.kt`
>   foram **desabilitados** (renomeados para `.disabled`), contornando o
>   problema original
>
> A spec descreve um estado de quebra que não corresponde mais ao código.
> As tarefas pendentes foram **retiradas**. Eventuais testes unitários de
> Mappers (antigas 9.3/9.4) podem ser recriados sob a spec de testes, se
> desejado, mas não fazem parte do escopo de "corrigir compilação".
