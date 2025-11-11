# Requirements Document - Correção de Erros de Compilação Android

## Introduction

O aplicativo Android InventarioMobile apresenta múltiplos erros de compilação devido a inconsistências entre camadas de arquitetura (Data, Domain, Presentation), mapeamentos incorretos entre DTOs/Entities/Models, e métodos/classes faltantes. Este documento define os requisitos para corrigir sistematicamente todos os erros de compilação e garantir que o aplicativo compile e execute corretamente.

## Glossary

- **DAO (Data Access Object)**: Interface Room que define operações de banco de dados
- **Entity**: Classe de dados Room que representa uma tabela do banco de dados local
- **DTO (Data Transfer Object)**: Classe de dados usada para transferência via API REST
- **Model/Domain Model**: Classe de domínio pura (sem dependências Android) usada na camada de negócio
- **Repository**: Implementação que coordena fontes de dados (local e remota)
- **Strategy**: Padrão de design para alternar entre fontes de dados (local/remota)
- **ViewModel**: Classe que gerencia estado da UI e coordena casos de uso
- **ApiService**: Interface Retrofit que define endpoints da API REST
- **Mapper**: Classe responsável por converter entre diferentes representações de dados

## Requirements

### Requirement 1: Corrigir Inconsistências de DAOs

**User Story:** Como desenvolvedor, quero que todos os DAOs tenham métodos consistentes e corretamente nomeados, para que os repositórios possam acessar o banco de dados sem erros.

#### Acceptance Criteria

1. WHEN o código chama métodos de DAO, THE Sistema SHALL usar os nomes corretos definidos nas interfaces DAO
2. WHEN um DAO precisa contar registros, THE Sistema SHALL usar métodos como `contarTodos()`, `contarColetados()`, `contarNaoColetados()`
3. WHEN um DAO precisa limpar dados, THE Sistema SHALL usar métodos como `limparTodos()`, `limparTodas()`
4. WHEN um DAO precisa inserir múltiplos registros, THE Sistema SHALL usar métodos como `inserirTodos()`, `inserirTodas()`
5. WHEN um DAO precisa buscar registros, THE Sistema SHALL usar métodos como `buscarPorId()`, `buscarPorNumero()`, `buscarTodas()`

### Requirement 2: Alinhar Estruturas de Entities com DTOs

**User Story:** Como desenvolvedor, quero que as Entities do Room tenham campos compatíveis com os DTOs da API, para que o mapeamento entre camadas seja consistente e sem erros.

#### Acceptance Criteria

1. WHEN o sistema mapeia SalaDto para SalaEntity, THE Sistema SHALL usar apenas campos que existem em ambas as classes
2. WHEN o sistema mapeia PatrimonioDto para PatrimonioEntity, THE Sistema SHALL usar apenas campos que existem em ambas as classes
3. WHEN uma Entity tem campos obrigatórios, THE Sistema SHALL garantir que o DTO forneça valores para esses campos
4. WHEN um DTO tem múltiplos campos para o mesmo conceito, THE Sistema SHALL usar propriedades computadas para normalizar o acesso
5. WHERE campos têm tipos diferentes entre DTO e Entity, THE Sistema SHALL realizar conversão de tipo apropriada

### Requirement 3: Implementar Classes e Métodos Faltantes

**User Story:** Como desenvolvedor, quero que todas as classes e métodos referenciados no código existam e estejam implementados, para que não haja erros de "Unresolved reference".

#### Acceptance Criteria

1. WHEN o código referencia DeviceInfoHelper, THE Sistema SHALL ter a classe disponível no pacote correto
2. WHEN o código referencia métodos de API, THE Sistema SHALL ter interfaces Retrofit apropriadas
3. WHEN o código referencia métodos de DAO, THE Sistema SHALL ter os métodos definidos nas interfaces DAO
4. WHEN o código referencia classes de modelo, THE Sistema SHALL usar o pacote correto (domain.model vs data.model)
5. WHEN o código referencia métodos utilitários, THE Sistema SHALL ter as classes utilitárias implementadas

### Requirement 4: Corrigir Mapeamentos em Repositories

**User Story:** Como desenvolvedor, quero que os repositórios mapeiem corretamente entre DTOs, Entities e Models, para que os dados fluam corretamente entre as camadas.

#### Acceptance Criteria

1. WHEN SyncRepository mapeia SalaDto para SalaEntity, THE Sistema SHALL usar campos: id, nome, idSetor, nomeSetor
2. WHEN SyncRepository mapeia PatrimonioDto para PatrimonioEntity, THE Sistema SHALL usar campos: id, numero, descricao, idSala, nomeSala, idResponsavel, nomeResponsavel, status, coletado
3. WHEN SyncRepository cria SincronizacaoEntity, THE Sistema SHALL usar campos: id, entidade, entidadeId, operacao, sincronizado, dataHora, erro
4. WHEN LocalDataSourceStrategy mapeia Entity para Model, THE Sistema SHALL garantir que todos os campos obrigatórios sejam fornecidos
5. WHEN RemoteDataSourceStrategy mapeia DTO para Model, THE Sistema SHALL garantir que todos os campos obrigatórios sejam fornecidos

### Requirement 5: Corrigir Estratégias de Fonte de Dados

**User Story:** Como desenvolvedor, quero que as estratégias de fonte de dados (Local e Remote) funcionem corretamente, para que o app possa operar em modo offline e online.

#### Acceptance Criteria

1. WHEN LocalDataSourceStrategy busca patrimônios, THE Sistema SHALL usar `getAllPatrimoniosList()` do DAO
2. WHEN LocalDataSourceStrategy busca patrimônio por número, THE Sistema SHALL usar `buscarPorNumero()` do DAO
3. WHEN LocalDataSourceStrategy busca salas, THE Sistema SHALL usar `buscarTodas()` do DAO
4. WHEN LocalDataSourceStrategy busca sala por ID, THE Sistema SHALL usar `buscarPorId()` do DAO
5. WHEN RemoteDataSourceStrategy retorna resultado, THE Sistema SHALL usar o tipo correto (Result<T> não Result<List<T>> quando esperado T)

### Requirement 6: Corrigir ViewModels e Activities

**User Story:** Como desenvolvedor, quero que ViewModels e Activities usem corretamente os casos de uso e repositórios, para que a camada de apresentação funcione sem erros.

#### Acceptance Criteria

1. WHEN LoginViewModel precisa registrar dispositivo, THE Sistema SHALL ter implementação completa ou comentário TODO claro
2. WHEN SyncActivity precisa criar APIs, THE Sistema SHALL usar método correto de instanciação (não `.create()`)
3. WHEN SyncActivity acessa propriedades de SyncLog, THE Sistema SHALL usar nomes corretos: `sincronizado` (não `sucesso`), `erro` (não `mensagem`)
4. WHEN PatrimonioViewModel importa Patrimonio, THE Sistema SHALL usar `domain.model.Patrimonio`
5. WHEN ViewModel observa estado, THE Sistema SHALL usar StateFlow ou LiveData corretamente

### Requirement 7: Corrigir Utilitários e Helpers

**User Story:** Como desenvolvedor, quero que todas as classes utilitárias estejam disponíveis e funcionando, para que funcionalidades auxiliares não causem erros de compilação.

#### Acceptance Criteria

1. WHEN DispositivoRepository usa DeviceInfoHelper, THE Sistema SHALL ter a classe no pacote `com.inventario.mobile.utils`
2. WHEN HeartbeatService usa DeviceInfoHelper, THE Sistema SHALL ter a classe no pacote `com.inventario.mobile.utils`
3. WHEN NetworkLocationManager usa getLocalIpAddress, THE Sistema SHALL ter o método implementado
4. WHEN código usa NetworkUtils, THE Sistema SHALL ter todos os métodos necessários implementados
5. WHERE classes utilitárias estão em pacotes incorretos, THE Sistema SHALL mover para o pacote correto

### Requirement 8: Garantir Compilação Limpa

**User Story:** Como desenvolvedor, quero que o projeto compile sem erros, para que eu possa executar e testar o aplicativo.

#### Acceptance Criteria

1. WHEN executo `gradlew compileDebugKotlin`, THE Sistema SHALL compilar sem erros
2. WHEN executo `gradlew assembleDebug`, THE Sistema SHALL gerar APK sem erros
3. WHEN há warnings de compilação, THE Sistema SHALL documentar warnings conhecidos e aceitáveis
4. WHEN há dependências faltantes, THE Sistema SHALL adicionar ao build.gradle
5. WHEN há conflitos de versão, THE Sistema SHALL resolver usando versões compatíveis

### Requirement 9: Validar Arquitetura Clean

**User Story:** Como desenvolvedor, quero que o código siga os princípios de Clean Architecture, para que o projeto seja mantível e testável.

#### Acceptance Criteria

1. WHEN camada Domain é modificada, THE Sistema SHALL não ter dependências de Android ou frameworks
2. WHEN camada Data é modificada, THE Sistema SHALL implementar interfaces definidas em Domain
3. WHEN camada Presentation é modificada, THE Sistema SHALL depender apenas de Domain (Use Cases)
4. WHEN novos repositórios são criados, THE Sistema SHALL seguir padrão Repository com interface em Domain
5. WHEN novos casos de uso são criados, THE Sistema SHALL estar em `domain.usecase` e ser independente de frameworks

### Requirement 10: Documentar Mudanças e Padrões

**User Story:** Como desenvolvedor, quero documentação clara das correções realizadas, para que futuras manutenções sejam mais fáceis.

#### Acceptance Criteria

1. WHEN uma correção é aplicada, THE Sistema SHALL documentar o problema e a solução
2. WHEN um padrão é estabelecido, THE Sistema SHALL documentar em steering rules
3. WHEN há código temporário ou TODO, THE Sistema SHALL ter comentário explicativo claro
4. WHEN há decisões arquiteturais, THE Sistema SHALL documentar o raciocínio
5. WHEN há limitações conhecidas, THE Sistema SHALL documentar em arquivo KNOWN_ISSUES.md
