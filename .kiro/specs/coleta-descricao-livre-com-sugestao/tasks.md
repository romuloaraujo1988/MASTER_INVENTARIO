# Implementation Plan: Coleta com Descrição Livre e Sugestão

## Overview

Esta feature adiciona, na `ItemSemEtiquetaActivity` do App_Android, um campo de descrição livre sempre visível combinado a um toggle que exibe sugestões de descrições de patrimônios **não coletados** do inventário ativo. O plano cobre:

- **Servidor (Java 21 / Spring Boot 3.2)**: novo método DAO, novo service, novo DTO de paginação, novo endpoint `GET /api/mobile/descricoes/sugestoes` (sem tocar em `/nao-coletadas`, `/coletas`, `/coletas/batch`) e reforço de validação `3..255` na entrada de coleta.
- **App Android (Kotlin / Clean Architecture + MVVM + Hilt + Room)**: nova entidade Room com migração `16→17`, novo DAO, novo Retrofit API, novo Repository offline-first com fallback para cache, novos Use Cases, evolução do `ItemSemEtiquetaViewModel` com debounce 300ms e `SugestaoDescricaoState`, atualizações no layout/Activity e wiring Hilt.
- **Testes**: property-based tests (Kotest Property no app, jqwik no servidor) para as 18 propriedades descritas em `design.md`, complementados por unit tests e testes de migração Room.

Cada tarefa é incremental, constrói sobre a anterior e termina com wiring. Todos os endpoints existentes sob `/api/mobile/*` permanecem intocados (regra `endpoints-nao-alterar.md`). O schema da `TABELA_COLETA` não é alterado.

## Tasks

- [x] 1. Preparar índices de performance no PostgreSQL (pré-requisito do servidor)
  - Criar `sql/adicionar_indices_sugestoes_descricao.sql` com:
    - `CREATE EXTENSION IF NOT EXISTS unaccent;`
    - Índice funcional `idx_patrimonio_descricao_unaccent ON tabela_patrimonio (unaccent(lower(descricao)))`
    - Índice composto `idx_coleta_patrimonio_inventario ON tabela_coleta (id_patrimonio, id_inventario)` (criar se ainda não existir)
    - Índice `idx_patrimonio_status ON tabela_patrimonio (status)`
  - Usar `CREATE INDEX IF NOT EXISTS` para idempotência
  - Documentar no cabeçalho do arquivo que não altera colunas — apenas índices
  - _Requirements: 3.4, 5.4, 6.1, 6.4, 9.2, 9.3_

- [x] 2. Criar DTOs e record de retorno do DAO no servidor
  - [x] 2.1 Criar `SugestaoDescricaoDTO` (record) em `sihcp-server/.../mobile/server/dto/`
    - Campos: `Integer idPatrimonio`, `String numeroPatrimonio`, `String descricao`
    - _Requirements: 5.1, 8.4_

  - [x] 2.2 Criar `PagedResponseDTO<T>` (record) em `sihcp-server/.../mobile/server/dto/`
    - Campos: `List<T> content`, `int page`, `int size`, `long totalElements`, `int totalPages`, `boolean hasNext`, `boolean semInventarioAtivo`
    - Incluir fábrica `PagedResponseDTO.empty(semInventarioAtivo)` para o caso `semInventarioAtivo=true`
    - _Requirements: 5.5, 5.6, 5.8_

  - [x] 2.3 Criar `SugestaoDescricaoRow` e `PagedResult<T>` em `sihcp-core/.../dao/` como objetos de transporte entre DAO e service
    - `SugestaoDescricaoRow` com `id`, `numero`, `descricao`
    - `PagedResult<T>` com `List<T> items`, `long totalElements`, `int page`, `int size`
    - _Requirements: 5.6_

- [x] 3. Implementar acesso a dados no servidor (`PatrimonioDAO`)
  - [x] 3.1 Adicionar método `buscarSugestoesNaoColetadasPaginado(int idInventario, String termoBusca, int page, int size)` a `PatrimonioDAO`
    - Usar exatamente o SQL descrito em `design.md` com `NOT EXISTS` na `TABELA_COLETA` e filtro por `unaccent(lower(...))`
    - Tratar `termoBusca` vazio/null corretamente (sem `LIKE`)
    - Ordenação `ORDER BY unaccent(lower(p.DESCRICAO)) ASC, p.NUMERO ASC` para estabilidade (Prop 12)
    - Aplicar `LIMIT ? OFFSET ?` com `offset = page * size`
    - Executar também a consulta `COUNT(*)` com os mesmos filtros para `totalElements`
    - Usar `PreparedStatement` parametrizado (sem interpolação de strings)
    - NÃO modificar `buscarDescricoesNaoColetadasAgrupadas` (mantida para o endpoint legado)
    - _Requirements: 5.2, 5.4, 5.5, 5.6, 6.4, 9.9_

  - [x]* 3.2 Escrever property test P13 para `PatrimonioDAO.buscarSugestoesNaoColetadasPaginado`
    - **Property 13: Servidor retorna somente patrimônios não coletados do inventário ativo**
    - **Validates: Requirements 5.2, 5.8**
    - Usar jqwik + Testcontainers PostgreSQL
    - Gerar datasets sintéticos (patrimônios com e sem coleta, status variados, descrições com e sem diacríticos)
    - Asserções (a)–(d) exatamente como especificadas na propriedade

  - [x]* 3.3 Escrever property test P7 (variante servidor) para filtro acento/caso-insensível
    - **Property 7: Filtro acento/caso-insensível é consistente entre app e servidor**
    - **Validates: Requirements 3.4, 5.4**
    - Gerar listas de descrições com combinações NFD/acentos/maiúsculas; termos com variações
    - Verificar `∀ item ∈ resultado, unaccent(lower(item)).contains(unaccent(lower(termo)))`

  - [x]* 3.4 Escrever property tests P11 e P12 (paginação e ordenação estável)
    - **Property 11: Paginação completa e `size` respeitado**
    - **Property 12: Ordenação estável entre páginas**
    - **Validates: Requirements 5.5, 5.6, 5.7, 6.2, 6.3, 6.4**
    - Gerar datasets de 1–500 patrimônios, variar `size ∈ [1,100]`
    - Concatenar todas as páginas e verificar (i) igualdade de conjunto com o dataset esperado, (ii) sem duplicatas, (iii) ordem crescente monótona

- [x] 4. Implementar `MobileSugestaoDescricaoService`
  - [x] 4.1 Criar `sihcp-server/.../mobile/server/service/MobileSugestaoDescricaoService.java`
    - `@Service` Spring
    - Método `listarSugestoes(String termoBusca, Integer page, Integer size, Integer idInventarioSolicitado)` retornando `PagedResponseDTO<SugestaoDescricaoDTO>`
    - Normalização: `termoBusca.trim().take(100)` (truncar silenciosamente), null → ""
    - Sanitização: `page = max(page ?: 0, 0)`, `size = coerceIntOrDefault(size, 50) ∈ [1, 100]` — caso inválido, aplicar 50 silenciosamente (Req 5.7)
    - Resolução de inventário: se `idInventarioSolicitado == null`, usar `InventarioDAO.buscarInventarioAtivo()`; se não houver ativo, retornar `PagedResponseDTO.empty(semInventarioAtivo=true)` (Req 5.8)
    - Mapear `SugestaoDescricaoRow → SugestaoDescricaoDTO`
    - Calcular `totalPages = ceil(totalElements / size)` e `hasNext = (page+1) * size < totalElements`
    - _Requirements: 5.3, 5.5, 5.6, 5.7, 5.8, 6.2, 6.3_

  - [x]* 4.2 Escrever unit test para `MobileSugestaoDescricaoService` com mock de `PatrimonioDAO` e `InventarioDAO`
    - Testar cenário "sem inventário ativo" retorna `semInventarioAtivo=true, content=[]` (Req 5.8)
    - Testar sanitização silenciosa de `size` inválido (Req 5.7) e `page < 0`
    - Testar truncamento silencioso de `q.length > 100`

- [x] 5. Adicionar endpoint `GET /sugestoes` em `MobileDescricaoController`
  - [x] 5.1 Adicionar método `listarSugestoes` em `MobileDescricaoController.java`
    - **NÃO alterar nenhum endpoint existente** (regra steering `endpoints-nao-alterar.md`)
    - `@GetMapping("/sugestoes")` com parâmetros `q`, `page`, `size`, `idInventario` (todos opcionais conforme `design.md`)
    - Retornar `ResponseEntity<ApiResponse<PagedResponseDTO<SugestaoDescricaoDTO>>>`
    - Delegar integralmente ao `MobileSugestaoDescricaoService`
    - Log de tempo de processamento na mensagem de `ApiResponse`
    - Segurança: herdar `@RequireColetor` do nível de classe (não adicionar anotação sobrescrevendo)
    - _Requirements: 5.1, 5.9, 10.1, 10.2, 10.3_

  - [x]* 5.2 Escrever property test P18 (autorização JWT) via `MockMvc`
    - **Property 18: Autorização baseada em role do JWT**
    - **Validates: Requirements 10.1, 10.2, 10.3**
    - Gerar tokens aleatórios (válidos, expirados, malformados, assinatura inválida)
    - Gerar combinações de roles e verificar a tabela de decisão (401 / 403 / 200)

  - [x]* 5.3 Escrever smoke test de contrato preservado para `GET /api/mobile/descricoes/nao-coletadas`
    - Comparar response body com snapshot pré-feature
    - **Validates: Requirements 5.9**

- [x] 6. Reforçar validação de tamanho de descrição no `MobileColetaController` (sem quebrar compatibilidade)
  - [x] 6.1 Adicionar validação `3 ≤ trim(descricaoItemSemEtiqueta).length ≤ 255` nos endpoints `POST /api/mobile/coletas` e `POST /api/mobile/coletas/batch`
    - **NÃO alterar URL, método, campos obrigatórios/opcionais existentes**
    - Se o campo está ausente (cliente legado), aceitar normalmente (Req 9.8)
    - Se presente e fora do intervalo após `trim`, responder `400` com `ApiResponse.error(...)` indicando o intervalo 3–255
    - No endpoint batch, retornar erro apenas para os itens inválidos, seguindo a semântica atual de batch
    - _Requirements: 1.7, 9.7, 9.8_

  - [x]* 6.2 Escrever property test P14 para validação server-side e compat retroativa
    - **Property 14: Validação de tamanho no servidor + compatibilidade retroativa de coletas**
    - **Validates: Requirements 1.7, 9.8**
    - Gerar `s` com `length ∈ [0, 300]` e variações de whitespace; gerar payloads sem o campo
    - Asserções conforme a propriedade (400 quando fora; aceitação nos demais casos)

- [x] 7. Checkpoint - Servidor pronto
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Android: criar modelos de domínio e contrato do repositório
  - [x] 8.1 Criar `domain/model/SugestaoDescricao.kt`
    - Campos: `idPatrimonio: Int`, `numeroPatrimonio: String`, `descricao: String`
    - _Requirements: 3.1, 3.5_

  - [x] 8.2 Criar `domain/model/OrigemSugestoes.kt` e `domain/model/ResultadoSugestoes.kt`
    - `enum class OrigemSugestoes { SERVIDOR, CACHE, VAZIO_SEM_CACHE }`
    - `data class ResultadoSugestoes(sugestoes, origem, totalElements, hasNext)`
    - _Requirements: 3.2, 7.2, 7.3_

  - [x] 8.3 Criar `domain/repository/SugestaoDescricaoRepository.kt` (interface) com assinaturas exatas do `design.md`
    - `buscarSugestoes`, `atualizarCache`, `buscarOffline`, `marcarColetadoLocalmente`, `limparCacheDeOutrosInventarios`
    - Retornar `Result<ResultadoSugestoes>` onde aplicável
    - _Requirements: 3.1, 3.2, 7.1, 8.1_

- [x] 9. Android: camada de normalização de texto
  - [x] 9.1 Criar `data/util/TextNormalizer.kt` com `@Inject` constructor
    - `fun normalize(input: String): String` aplicando `Normalizer.Form.NFD` + `\p{InCombiningDiacriticalMarks}+` removidos + `lowercase(Locale.forLanguageTag("pt-BR"))`
    - _Requirements: 3.4, 7.2_

  - [x]* 9.2 Escrever property test de idempotência e preservação ASCII do `TextNormalizer`
    - **Property auxiliar (suporte a P7): `normalize(normalize(x)) == normalize(x)` e `normalize` preserva caracteres ASCII-alfanuméricos em minúsculas**
    - **Validates: Requirements 3.4**
    - Kotest Property: `checkAll(Arb.string()) { s -> normalize(normalize(s)) shouldBe normalize(s) }`

- [x] 10. Android: camada de dados local (Room)
  - [x] 10.1 Criar `data/local/entity/SugestaoDescricaoEntity.kt`
    - `@Entity(tableName = "sugestao_descricao", primaryKeys = ["idInventario", "idPatrimonio"], indices = [...])`
    - Campos exatos do `design.md` (inclui `descricaoNormalizada`, `coletadoLocal`, `dataAtualizacao`)
    - _Requirements: 7.1, 7.2, 8.1_

  - [x] 10.2 Criar `data/local/dao/SugestaoDescricaoDao.kt`
    - Métodos: `buscarFiltrado(idInventario, termoNormalizado, limit)`, `upsertAll(entities)`, `marcarColetado(idInventario, idPatrimonio)`, `limparOutrosInventarios(idInventarioAtivo)`, `contarPorInventario(idInventario)`
    - Queries exatas do `design.md` com `ORDER BY descricaoNormalizada ASC, numeroPatrimonio ASC` e filtro `coletadoLocal = 0`
    - _Requirements: 7.2, 8.3, 8.4_

  - [x] 10.3 Adicionar migração Room `MIGRATION_16_17` ao `AppDatabase`
    - `inventario_offline_secure.db` (nome canônico — consistente com steering de padronização)
    - Incrementar `version = 17` e registrar `addMigrations(MIGRATION_16_17)`
    - `CREATE TABLE sugestao_descricao` com os três índices listados no `design.md`
    - Adicionar `abstract fun sugestaoDescricaoDao(): SugestaoDescricaoDao`
    - _Requirements: 7.1, 9.2_

  - [x]* 10.4 Escrever teste de migração Room 16→17 usando `MigrationTestHelper`
    - Criar banco na versão 16 com dados fictícios; aplicar migração; verificar schema e índices; verificar que tabelas pré-existentes permanecem íntegras (Req 9.2)
    - **Validates: Requirements 7.1, 9.2**

  - [x]* 10.5 Escrever property test P16 (consistência filtro offline) sobre `SugestaoDescricaoDao`
    - **Property 16: Consistência do cache + filtro offline com patrimônios coletados**
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4**
    - `Room.inMemoryDatabaseBuilder` + Kotest Property
    - Gerar caches com patrimônios, descrições duplicadas entre patrimônios, combinações `coletadoLocal` 0/1; termos variados
    - Asserções de inclusão bidirecional como descritas na propriedade

- [x] 11. Android: camada de dados remota (Retrofit)
  - [x] 11.1 Criar `data/remote/dto/SugestaoDescricaoDto.kt` e `PagedResponseDto.kt`
    - Espelhar exatamente o contrato JSON do `design.md` (incluindo `semInventarioAtivo`)
    - _Requirements: 5.5, 5.6, 5.8_

  - [x] 11.2 Criar `data/remote/api/DescricaoSugestaoApi.kt`
    - `@GET("api/mobile/descricoes/sugestoes")` (URL completa com prefixo `api/mobile/` — regra steering `endpoints-nao-alterar.md`)
    - Parâmetros `@Query("q")`, `@Query("page")`, `@Query("size")`, `@Query("idInventario")` com defaults do `design.md`
    - Retorno `ApiResponse<PagedResponseDto<SugestaoDescricaoDto>>`
    - _Requirements: 5.1_

  - [x] 11.3 Criar `data/mapper/SugestaoDescricaoMapper.kt`
    - `toDomain(dto: SugestaoDescricaoDto): SugestaoDescricao`
    - `toEntity(domain: SugestaoDescricao, idInventario: Int, normalizer: TextNormalizer): SugestaoDescricaoEntity`
    - `toDomain(entity: SugestaoDescricaoEntity): SugestaoDescricao`
    - _Requirements: 7.1_

- [x] 12. Android: implementação do repositório offline-first
  - [x] 12.1 Criar `data/repository/SugestaoDescricaoRepositoryImpl.kt`
    - Construtor com `SugestaoDescricaoDao`, `DescricaoSugestaoApi`, `SugestaoDescricaoMapper`, `TextNormalizer`
    - `buscarSugestoes`: usar `withTimeout(10_000)`; em sucesso, chamar `atualizarCache` e retornar `origem=SERVIDOR`; em falha (IOException / HttpException / Timeout), fazer fallback `buscarOffline` com `limit=10`; se cache vazio, retornar `origem=VAZIO_SEM_CACHE`
    - `atualizarCache`: mapear e chamar `dao.upsertAll`; em caso de exceção, logar e NÃO propagar (Req 8.7)
    - `buscarOffline`: normalizar termo via `TextNormalizer` e delegar ao DAO
    - `marcarColetadoLocalmente` e `limparCacheDeOutrosInventarios`: delegar ao DAO
    - _Requirements: 3.2, 7.1, 7.2, 7.3, 7.7, 8.1, 8.2, 8.7_

  - [x]* 12.2 Escrever property test P10 (fallback determinístico para cache)
    - **Property 10: Fallback determinístico para o cache em falhas**
    - **Validates: Requirements 3.2, 7.2, 7.3**
    - Mock `DescricaoSugestaoApi` com `mockk` para lançar `IOException`, `HttpException 500`, `TimeoutCancellationException`
    - Verificar que `Result.Success` é retornado com origem correta (CACHE ou VAZIO_SEM_CACHE)

  - [x]* 12.3 Escrever property test P15 (consistência do `atualizarCache`)
    - **Property 15: `atualizarCache` garante consistência em sucesso e preserva em falha**
    - **Validates: Requirements 7.1, 8.7**
    - Caso sucesso: após `atualizarCache(I, L)`, cache contém exatamente `L` mapeado com `descricaoNormalizada` correta
    - Caso falha (DAO lança exceção): cache anterior preservado

- [x] 13. Android: Use Cases de domínio
  - [x] 13.1 Criar `domain/usecase/BuscarSugestoesDescricaoUseCase.kt`
    - `suspend operator fun invoke(idInventario, termoBusca, page=0, size=50): Result<ResultadoSugestoes>`
    - Injetar `SugestaoDescricaoRepository` e `ConnectivityMonitor` existente
    - Delegar diretamente ao repositório (a decisão online/offline está no repo)
    - _Requirements: 3.1, 3.2, 7.2_

  - [x] 13.2 Criar `domain/usecase/MarcarPatrimonioColetadoLocalmenteUseCase.kt`
    - `suspend operator fun invoke(idInventario: Int, idPatrimonio: Int?)`
    - Guardar contra `idPatrimonio == null` (caso item sem etiqueta sem vínculo) — no-op
    - _Requirements: 8.1, 8.2_

  - [x] 13.3 Criar `domain/usecase/SincronizarSugestoesDescricaoUseCase.kt`
    - Recarregar sugestões do servidor e chamar `atualizarCache` + `limparCacheDeOutrosInventarios`
    - Usado por `SyncWorker` em background e pelo refresh manual (Req 8.5, 8.6)
    - Se falhar (timeout 10s ou erro de rede), retornar `Result.failure` sem limpar cache anterior (Req 8.7)
    - _Requirements: 8.5, 8.6, 8.7_

- [x] 14. Android: injeção de dependência (Hilt)
  - [x] 14.1 Adicionar provider do `SugestaoDescricaoDao` ao `DatabaseModule.kt`
    - `@Provides fun provide(db: AppDatabase): SugestaoDescricaoDao = db.sugestaoDescricaoDao()`
    - _Requirements: 7.1_

  - [x] 14.2 Adicionar provider do `DescricaoSugestaoApi` ao `ApiModule.kt`
    - Usar o `Retrofit` já configurado
    - _Requirements: 5.1_

  - [x] 14.3 Adicionar binding em `RepositoryModule.kt`
    - `@Binds abstract fun bindSugestaoRepo(impl: SugestaoDescricaoRepositoryImpl): SugestaoDescricaoRepository`
    - _Requirements: arquitetura (steering `clean-architecture.md`)_

  - [x] 14.4 Adicionar provider de `TextNormalizer` e `SugestaoDescricaoMapper` no `MapperModule.kt` (se não forem `@Inject constructor()` puros)
    - _Requirements: 3.4_

- [x] 15. Android: evoluir `ItemSemEtiquetaViewModel`
  - [x] 15.1 Adicionar `SugestaoDescricaoState` sealed class em `presentation/coleta/state/`
    - Subclasses: `Oculto`, `Carregando`, `Carregado(sugestoes, origem)`, `SemResultados(origem)`, `Erro(mensagem)`
    - _Requirements: 2.2, 3.2, 3.3, 3.6_

  - [x] 15.2 Injetar `BuscarSugestoesDescricaoUseCase` e `MarcarPatrimonioColetadoLocalmenteUseCase` em `ItemSemEtiquetaViewModel` (já `@HiltViewModel`)
    - Adicionar `MutableStateFlow<SugestaoDescricaoState>` com estado inicial `Oculto`
    - Adicionar `_toggleAtivo: MutableStateFlow<Boolean>` e `_termoBusca: MutableStateFlow<String>`
    - _Requirements: 2.1, 2.2_

  - [x] 15.3 Implementar debounce 300ms via `termoBuscaDebounced = _termoBusca.debounce(300).distinctUntilChanged()`
    - `combine(_toggleAtivo, termoBuscaDebounced)` em `init { viewModelScope.launch { ... } }` usando `.filter { ativo }.collectLatest { (_, termo) -> carregarSugestoes(termo) }`
    - _Requirements: 3.7_

  - [x] 15.4 Implementar métodos públicos
    - `setToggleSugestao(ativo: Boolean)` — se off, definir estado `Oculto` e cancelar carregamento pendente (Req 2.2, 2.4)
    - `onTermoBuscaChange(termo: String)`
    - `onSugestaoSelecionada(s: SugestaoDescricao)` — emitir evento `Event.PreencherCampoLivre(s.descricao)` via `SharedFlow` e NÃO guardar vínculo forte (Req 3.5, 4.2)
    - `private suspend fun carregarSugestoes(termo: String)` — emitir `Carregando` → chamar UseCase → mapear para `Carregado`/`SemResultados`/`Erro`
    - _Requirements: 2.2, 2.3, 2.4, 3.5, 3.6, 3.7, 4.2_

  - [x] 15.5 Estender `confirmarColeta(descricaoLivre, ...)` para aplicar `trim()`, validar `3..255`, emitir erro preservando texto, e, em sucesso, chamar `MarcarPatrimonioColetadoLocalmenteUseCase(idInventario, idPatrimonioVinculado?)`
    - NÃO alterar chamadas existentes a `RegistrarColetaUseCase` (sempre enviar `descricao = texto.trim()`)
    - _Requirements: 1.3, 1.4, 1.5, 1.6, 4.1, 4.3, 4.4, 4.5, 8.1_

  - [x] 15.6 Tratar erro de exceção ao alternar toggle (reverter estado + snackbar) no ViewModel/Activity (Req 2.5)
    - Capturar exceção inesperada em `setToggleSugestao` e emitir `Erro` transitório
    - _Requirements: 2.5_

  - [x]* 15.7 Escrever property test P1 (preservação do texto livre)
    - **Property 1: Preservação do texto do campo livre sob transições arbitrárias**
    - **Validates: Requirements 1.2, 2.3, 2.4, 7.4**
    - Kotest Property + Turbine: gerar sequências de eventos (`setText`, toggles, mudanças de conectividade simuladas) e verificar que `viewModel.campoLivreText.value == últimoSetText`

  - [x]* 15.8 Escrever property test P2 (validação + trim na confirmação)
    - **Property 2: Trim + validação de tamanho no app na confirmação**
    - **Validates: Requirements 1.3, 1.4, 1.5, 1.6, 4.4, 4.5**
    - Mock de `RegistrarColetaUseCase` e verificar invocação ↔ `3 ≤ trim.length ≤ 255`

  - [x]* 15.9 Escrever property test P3 (descrição final é `currentText.trim()`)
    - **Property 3: Descrição final é sempre `currentText.trim()` e persistida apenas no campo alvo**
    - **Validates: Requirements 4.1, 4.2, 4.3, 9.1**
    - Simular trajetórias `setText / onSugestaoSelecionada / edição / toggle / confirmar` e verificar argumento do UseCase

  - [x]* 15.10 Escrever property test P4 (toggle off ⇒ zero API + UI Oculto)
    - **Property 4: Toggle desativado implica zero consultas e UI oculta**
    - **Validates: Requirements 2.2**
    - `mockk verify(exactly = 0) { useCase(any(), any(), any(), any()) }` após sequências arbitrárias com toggle off

  - [x]* 15.11 Escrever property test P5 (seleção preenche e mantém editável)
    - **Property 5: Seleção de sugestão preenche o campo e mantém edição habilitada**
    - **Validates: Requirements 3.5**

  - [x]* 15.12 Escrever property test P6 (debounce agrupa alterações rápidas)
    - **Property 6: Debounce agrupa alterações rápidas do termo de busca**
    - **Validates: Requirements 3.7**
    - Usar `kotlinx-coroutines-test` + `TestScheduler.advanceTimeBy` para controlar tempo virtual
    - Gerar sequências de emissões com intervalos aleatórios e contar invocações do UseCase

  - [x]* 15.13 Escrever property tests P8 e P9 (limites de exibição e transição Carregado/SemResultados)
    - **Property 8: Limites de tamanho na lista exibida**
    - **Property 9: Transição entre `Carregado` e `SemResultados`**
    - **Validates: Requirements 3.3, 3.6, 7.2**

- [x] 16. Checkpoint - ViewModel e camadas Android core prontas
  - Ensure all tests pass, ask the user if questions arise.

- [x] 17. Android: UI (layout XML e Activity)
  - [x] 17.1 Atualizar `res/layout/activity_item_sem_etiqueta.xml`
    - Reforçar `Campo_Descricao_Livre` (`TextInputEditText`): `android:maxLength="255"`, `android:inputType="textCapSentences"`, `requestFocus` no `onCreate` programático
    - Adicionar `SwitchMaterial` `toggleSugestao` com label "Sugerir descrições cadastradas", estado inicial desabilitado
    - Adicionar `RecyclerView` `rvSugestoes` (vertical, `maxHeight=240dp`, `nestedScrollingEnabled=true`, `visibility=gone`)
    - Manter todos os demais campos/fotos atuais da tela (Req 9.4)
    - _Requirements: 1.1, 1.5, 2.1, 2.3, 2.4, 3.3, 3.6, 9.4_

  - [x] 17.2 Criar `SugestaoDescricaoAdapter` (`ListAdapter` + `DiffUtil`) em `presentation/coleta/adapter/`
    - Item layout simples com `TextView` exibindo `sugestao.descricao`
    - Click listener chama `viewModel.onSugestaoSelecionada(item)`
    - _Requirements: 3.3, 3.5_

  - [x] 17.3 Atualizar `ItemSemEtiquetaActivity.kt`
    - Focar `Campo_Descricao_Livre` em `onCreate` (Req 1.1)
    - Ligar `toggleSugestao.setOnCheckedChangeListener { viewModel.setToggleSugestao(it) }` (Req 2.1, 2.3, 2.4)
    - `TextWatcher` no `EditText` → `viewModel.onTermoBuscaChange(text)` (Req 3.7) E manter atualização do `currentText` interno do VM (para Prop 1/3)
    - `lifecycleScope.launch { viewModel.sugestaoState.collect { render(it) } }` controlando visibilidade do `RecyclerView`, snackbars de `origem=CACHE` (Req 3.2), mensagem de "sem resultados" (Req 3.6) e `Erro`
    - Escutar `SharedFlow` de `PreencherCampoLivre` e aplicar `editText.setText(...)` + `setSelection(end)` mantendo foco (Req 3.5)
    - Em `confirmarColeta`, passar `edtDescricao.text.toString()` ao VM (que aplica trim + validação)
    - _Requirements: 1.1, 2.1, 2.3, 2.4, 3.2, 3.3, 3.5, 3.6, 3.7, 4.1, 4.2_

  - [x] 17.4 Garantir que o campo permanece habilitado em qualquer estado de rede/cache
    - Nunca desabilitar `edtDescricao` em função do estado de sugestão (Req 7.4)
    - _Requirements: 7.4, 7.7_

  - [x]* 17.5 Escrever unit test de `SugestaoDescricaoAdapter` verificando binding de 0, 1 e múltiplos itens e chamada do click listener
    - **Validates: Requirements 3.3, 3.5**

- [x] 18. Android: integração com fluxo offline-first existente
  - [x] 18.1 Garantir que `RegistrarColetaUseCase` recebe `descricao = texto.trim()` sem transformações adicionais no `ItemSemEtiquetaViewModel`
    - Não alterar `ColetaRepositoryImpl` nem `MobileColetaBatchRequest`
    - _Requirements: 7.5, 7.6, 9.1, 9.7_

  - [x] 18.2 No `SyncWorker`, após sync bem-sucedido, disparar `SincronizarSugestoesDescricaoUseCase` para o `idInventario` ativo (Req 8.6)
    - Em caso de falha do reload, preservar cache anterior (Req 8.7) — já garantido por `atualizarCache`
    - _Requirements: 8.5, 8.6, 8.7_

  - [x] 18.3 Ao iniciar `ItemSemEtiquetaActivity`, disparar `limparCacheDeOutrosInventarios(idInventarioAtivo)` via `SugestaoDescricaoRepository`
    - _Requirements: 7.1 (isolamento por inventário)_

  - [x]* 18.4 Escrever property test P17 (sincronização preserva descrição ipsis litteris)
    - **Property 17: Sincronização preserva a descrição ipsis litteris**
    - **Validates: Requirements 7.6**
    - Gerar descrições arbitrárias, persistir localmente, capturar payload de sync (mock do Retrofit) e verificar igualdade exata

- [x] 19. Servidor: smoke tests de schema e contrato
  - [x]* 19.1 Escrever smoke test de schema de `TABELA_COLETA` (diff estrutural com baseline pré-feature)
    - **Validates: Requirements 9.2, 9.3**
    - Verificar que as colunas e tipos permanecem idênticos ao baseline

  - [x]* 19.2 Escrever smoke test de contrato `POST /api/mobile/coletas` e `POST /api/mobile/coletas/batch`
    - **Validates: Requirements 9.7, 9.8**
    - Enviar payload "legado" (sem campos novos) e um payload "novo" com descrição válida; ambos devem ter status 2xx e estrutura de resposta inalterada

- [x] 20. Checkpoint final - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tarefas marcadas com `*` são opcionais (testes). Podem ser puladas para um MVP rápido, mas são fortemente recomendadas para garantir as 18 propriedades de correção.
- Todo código de implementação (não opcional) é obrigatório e referencia requisitos específicos para rastreabilidade.
- Checkpoints agrupam validação incremental — servidor (tarefa 7), ViewModel core (tarefa 16), integração final (tarefa 20).
- Property tests cobrem P1–P18 do `design.md`. Unit tests cobrem critérios `EXAMPLE` do prework. Testes de carga (Req 6.1) ficam fora deste plano por exigirem ambiente staging e ferramenta externa (JMeter/Gatling) — não são tarefas de codificação.
- Toda URL Retrofit mantém prefixo `api/mobile/` (regra crítica `endpoints-nao-alterar.md`).
- `TABELA_COLETA` não tem alteração de schema (apenas novos índices auxiliares em `TABELA_PATRIMONIO` / `TABELA_COLETA` para performance).
- O banco canônico Room permanece `inventario_offline_secure.db`, apenas com versão bumpada para 17 e nova tabela `sugestao_descricao`.
- Após concluir os artefatos, siga a steering `build-tracking.md` para registrar builds geradas.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1", "2.1", "2.2", "2.3", "8.1", "8.2", "9.1", "10.1"] },
    { "id": 1, "tasks": ["3.1", "8.3", "9.2", "10.2", "11.1"] },
    { "id": 2, "tasks": ["3.2", "3.3", "3.4", "4.1", "10.3", "10.4", "10.5", "11.2", "11.3"] },
    { "id": 3, "tasks": ["4.2", "5.1", "12.1"] },
    { "id": 4, "tasks": ["5.2", "5.3", "6.1", "12.2", "12.3", "13.1", "13.2", "13.3"] },
    { "id": 5, "tasks": ["6.2", "14.1", "14.2", "14.3", "14.4"] },
    { "id": 6, "tasks": ["15.1", "15.2"] },
    { "id": 7, "tasks": ["15.3", "15.4", "15.5", "15.6"] },
    { "id": 8, "tasks": ["15.7", "15.8", "15.9", "15.10", "15.11", "15.12", "15.13", "17.1", "17.2"] },
    { "id": 9, "tasks": ["17.3", "17.4", "17.5"] },
    { "id": 10, "tasks": ["18.1", "18.2", "18.3", "18.4", "19.1", "19.2"] }
  ]
}
```
