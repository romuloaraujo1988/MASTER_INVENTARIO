# Implementation Plan - Correção de Erros de Compilação Android

## Fase 1: Fundação - Data Access Objects (DAOs)

- [x] 1. Auditar e padronizar métodos de DAOs


  - Verificar todos os DAOs existentes (PatrimonioDao, SalaDao, ColetaDao, SincronizacaoDao)
  - Garantir nomenclatura consistente (buscar*, contar*, limpar*, inserir*)
  - Adicionar métodos faltantes identificados nos erros de compilação
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_



- [ ] 1.1 Corrigir PatrimonioDao
  - Verificar se todos os métodos usados no código existem
  - Garantir métodos: `buscarPorNumero()`, `buscarPorId()`, `getAllPatrimoniosList()`, `contarTodos()`, `contarColetados()`, `contarNaoColetados()`, `inserirTodos()`, `limparTodos()`


  - _Requirements: 1.1, 1.2_

- [x] 1.2 Corrigir SalaDao

  - Verificar se todos os métodos usados no código existem
  - Garantir métodos: `buscarTodas()`, `buscarPorId()`, `contar()`, `inserirTodas()`, `limparTodas()`
  - _Requirements: 1.1, 1.3_


- [ ] 1.3 Validar SincronizacaoDao
  - Verificar se o DAO criado tem todos os métodos necessários
  - Garantir métodos: `buscarPendentes()`, `getUltimaSincronizacao()`, `inserir()`, `marcarComoSincronizado()`, `limparTodas()`
  - _Requirements: 1.1, 1.4_


- [ ] 1.4 Corrigir ColetaDao (se necessário)
  - Verificar métodos usados em SyncViewModel e outros lugares
  - Padronizar nomenclatura
  - _Requirements: 1.1, 1.5_

## Fase 2: Alinhamento de Entities e DTOs



- [ ] 2. Criar classes Mapper para conversões
  - Criar PatrimonioMapper (DTO ↔ Entity ↔ Model)
  - Criar SalaMapper (DTO ↔ Entity ↔ Model)
  - Criar ColetaMapper (DTO ↔ Entity ↔ Model)



  - Documentar campos obrigatórios vs opcionais
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 2.1 Criar PatrimonioMapper
  - Implementar `dtoToEntity(MobilePatrimonioDto): PatrimonioEntity`
  - Implementar `entityToModel(PatrimonioEntity): Patrimonio`
  - Implementar `modelToDto(Patrimonio): MobilePatrimonioDto`
  - Tratar conversões de tipo (Long → Int)
  - _Requirements: 2.2, 2.5_

- [ ] 2.2 Criar SalaMapper
  - Implementar `dtoToEntity(SalaDto): SalaEntity`
  - Implementar `entityToModel(SalaEntity): Sala`
  - Usar propriedades computadas do SalaDto (nome, setorIdFinal)
  - _Requirements: 2.1, 2.4_

- [ ] 2.3 Criar ColetaMapper
  - Implementar `dtoToEntity(ColetaDto): ColetaEntity`
  - Implementar `entityToModel(ColetaEntity): Coleta`
  - Implementar `modelToDto(Coleta): MobileColetaRequest`
  - _Requirements: 2.2, 2.3_

- [ ] 2.4 Documentar estrutura de dados
  - Criar diagrama de mapeamento (DTO → Entity → Model)
  - Documentar campos obrigatórios em cada camada
  - Adicionar comentários nos Mappers explicando conversões
  - _Requirements: 10.1, 10.2_

## Fase 3: Correção de Repositories

- [ ] 3. Corrigir SyncRepository
  - Substituir mapeamentos inline por chamadas aos Mappers
  - Corrigir métodos de DAO (usar nomes corretos)
  - Corrigir criação de SincronizacaoEntity
  - Validar fluxo completo de sincronização
  - _Requirements: 4.1, 4.2, 4.3_

- [ ] 3.1 Corrigir mapeamento de Salas no SyncRepository
  - Usar `SalaMapper.dtoToEntity()` para converter SalaDto → SalaEntity
  - Usar `salaDao.limparTodas()` e `salaDao.inserirTodas()`
  - Validar que todos os campos necessários estão sendo mapeados
  - _Requirements: 4.1_

- [ ] 3.2 Corrigir mapeamento de Patrimônios no SyncRepository
  - Usar `PatrimonioMapper.dtoToEntity()` para converter DTO → Entity
  - Usar `patrimonioDao.limparTodos()` e `patrimonioDao.inserirTodos()`
  - Tratar conversão Long → Int para o campo `id`
  - _Requirements: 4.2_

- [ ] 3.3 Corrigir criação de SincronizacaoEntity
  - Usar apenas campos que existem: id, entidade, entidadeId, operacao, sincronizado, dataHora, erro
  - Remover campos inexistentes: tipo, patrimoniosSincronizados, salasSincronizadas, sucesso, mensagem, tempoDecorrido
  - Usar `sincronizacaoDao.inserir()` ao invés de `.insert()`
  - _Requirements: 4.3_

- [ ] 3.4 Corrigir outros métodos do SyncRepository
  - Corrigir `hasLocalData()` para usar `contarTodos()` e `contar()`
  - Corrigir `getLocalStats()` para usar métodos corretos de contagem
  - Corrigir `clearLocalData()` para usar `limparTodos()` e `limparTodas()`
  - _Requirements: 1.2, 1.3_

## Fase 4: Correção de Strategies

- [ ] 4. Corrigir LocalDataSourceStrategy
  - Usar métodos corretos dos DAOs
  - Usar Mappers para conversão Entity → Model
  - Corrigir tipos de retorno
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [ ] 4.1 Corrigir busca de patrimônios
  - Usar `patrimonioDao.getAllPatrimoniosList()`
  - Usar `PatrimonioMapper.entityToModel()` para cada item
  - Garantir que todos os campos do Model sejam preenchidos
  - _Requirements: 5.1_

- [ ] 4.2 Corrigir busca de patrimônio por número
  - Usar `patrimonioDao.buscarPorNumero()`
  - Usar `PatrimonioMapper.entityToModel()` se encontrado
  - _Requirements: 5.2_

- [ ] 4.3 Corrigir busca de salas
  - Usar `salaDao.buscarTodas()`
  - Usar `SalaMapper.entityToModel()` para cada item
  - _Requirements: 5.3_

- [ ] 4.4 Corrigir busca de sala por ID
  - Usar `salaDao.buscarPorId()`
  - Usar `SalaMapper.entityToModel()` se encontrado
  - _Requirements: 5.4_

- [ ] 5. Corrigir RemoteDataSourceStrategy
  - Corrigir tipos de retorno (Result<T> vs Result<List<T>>)
  - Usar Mappers para conversão DTO → Model
  - Tratar erros de API apropriadamente
  - _Requirements: 5.5_

- [ ] 5.1 Corrigir método getSalaById
  - Retornar `Result<Sala>` ao invés de `Result<List<Sala>>`
  - Usar `SalaMapper.dtoToModel()` para conversão
  - _Requirements: 5.5_

## Fase 5: Correção de ViewModels

- [ ] 6. Corrigir imports e referências em ViewModels
  - Corrigir imports de modelos (usar domain.model)
  - Remover ou implementar métodos faltantes
  - Validar uso de Use Cases
  - _Requirements: 6.1, 6.4_

- [ ] 6.1 Corrigir PatrimonioViewModel
  - Corrigir import: usar `com.inventario.mobile.domain.model.Patrimonio`
  - Verificar se todos os métodos usados existem
  - _Requirements: 6.4_

- [ ] 6.2 Corrigir LoginViewModel
  - Manter função `registrarDispositivoAutomaticamente` comentada com TODO
  - Adicionar comentário explicando que será implementado futuramente
  - _Requirements: 6.1, 10.3_

- [ ] 6.3 Corrigir SyncViewModel
  - Verificar métodos de DAO usados
  - Corrigir `getAllPatrimoniosList()` se necessário
  - Corrigir `updatePatrimonio()` se necessário
  - _Requirements: 6.5_

## Fase 6: Correção de Activities

- [ ] 7. Corrigir SyncActivity
  - Resolver problema de criação de APIs (`.create()` não existe)
  - Corrigir acesso a propriedades de SincronizacaoEntity
  - Implementar instanciação correta de APIs
  - _Requirements: 6.2, 6.3_

- [ ] 7.1 Resolver instanciação de APIs
  - Investigar como ApiService é criado no projeto
  - Usar injeção de dependência ou factory pattern
  - Remover chamadas a `.create()` que não existem
  - _Requirements: 6.2_

- [ ] 7.2 Corrigir acesso a propriedades de SyncLog
  - Usar `sincronizado` ao invés de `sucesso`
  - Usar `erro` ao invés de `mensagem`
  - Adaptar lógica de exibição conforme novos campos
  - _Requirements: 6.3_

## Fase 7: Correção de Utilitários

- [ ] 8. Corrigir classes utilitárias
  - Verificar localização de DeviceInfoHelper
  - Implementar ou corrigir NetworkLocationManager
  - Validar NetworkUtils
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 8.1 Corrigir DeviceInfoHelper
  - Verificar se classe existe em `com.inventario.mobile.utils`
  - Se não existir, criar ou mover de outro pacote
  - Garantir que DispositivoRepository e HeartbeatService possam acessar
  - _Requirements: 7.1, 7.2_

- [ ] 8.2 Corrigir NetworkLocationManager
  - Implementar método `getLocalIpAddress()` se não existir
  - Validar outros métodos usados
  - _Requirements: 7.3_

- [ ] 8.3 Validar NetworkUtils
  - Verificar se todos os métodos necessários existem
  - Adicionar métodos faltantes se necessário
  - _Requirements: 7.4_

## Fase 8: Validação e Testes

- [ ] 9. Compilar e validar correções
  - Executar compilação incremental após cada fase
  - Documentar erros restantes
  - Criar testes unitários para Mappers
  - _Requirements: 8.1, 8.2, 8.3_

- [ ] 9.1 Compilação Debug
  - Executar `gradlew compileDebugKotlin`
  - Verificar que não há erros de compilação
  - Documentar warnings aceitáveis
  - _Requirements: 8.1_

- [ ] 9.2 Build APK
  - Executar `gradlew assembleDebug`
  - Verificar que APK é gerado com sucesso
  - Testar instalação em dispositivo/emulador
  - _Requirements: 8.2_

- [ ]* 9.3 Testes Unitários de Mappers
  - Criar testes para PatrimonioMapper
  - Criar testes para SalaMapper
  - Criar testes para ColetaMapper
  - Validar conversões de tipo e campos obrigatórios
  - _Requirements: 8.3_

- [ ]* 9.4 Testes de Integração
  - Testar fluxo completo de sincronização
  - Testar modo offline
  - Testar coleta de patrimônio
  - _Requirements: 8.3_

## Fase 9: Documentação

- [ ] 10. Documentar mudanças e padrões
  - Atualizar steering rules com padrões estabelecidos
  - Criar documento de mudanças (CHANGELOG)
  - Documentar limitações conhecidas
  - Atualizar diagramas de arquitetura
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 10.1 Criar CHANGELOG.md
  - Listar todas as correções realizadas
  - Categorizar por tipo (DAO, Entity, Mapper, Repository, etc.)
  - Incluir referências aos requirements
  - _Requirements: 10.1_

- [ ] 10.2 Atualizar steering rules
  - Documentar padrão de nomenclatura de DAOs
  - Documentar padrão de Mappers
  - Documentar fluxo de dados (DTO → Entity → Model)
  - _Requirements: 10.2_

- [ ] 10.3 Criar KNOWN_ISSUES.md
  - Documentar limitações conhecidas
  - Documentar TODOs pendentes
  - Documentar workarounds temporários
  - _Requirements: 10.5_

- [ ] 10.4 Atualizar diagramas
  - Atualizar diagrama de arquitetura
  - Criar diagrama de fluxo de dados
  - Criar diagrama de mapeamento entre camadas
  - _Requirements: 10.4_

## Fase 10: Validação Final

- [ ] 11. Revisão de arquitetura Clean
  - Validar que Domain não depende de Android
  - Validar que Data implementa interfaces de Domain
  - Validar que Presentation usa apenas Use Cases
  - Verificar separação de responsabilidades
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 11.1 Auditoria de dependências
  - Verificar imports em camada Domain
  - Verificar que não há dependências circulares
  - Validar uso de interfaces vs implementações
  - _Requirements: 9.1, 9.2_

- [ ] 11.2 Revisão de código
  - Verificar duplicação de código
  - Verificar nomenclatura consistente
  - Verificar tratamento de erros
  - Verificar logging adequado
  - _Requirements: 9.3, 9.4_

- [ ] 11.3 Validação de testes
  - Garantir cobertura mínima de testes
  - Validar que testes passam
  - Documentar casos de teste importantes
  - _Requirements: 9.5_
