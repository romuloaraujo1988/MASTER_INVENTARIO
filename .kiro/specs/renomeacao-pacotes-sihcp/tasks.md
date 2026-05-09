# Plano de Implementação: Renomeação de Pacotes SIHCP

## Visão Geral

Renomear o pacote raiz Java do SIHCP de `com.inventario` para `com.inventario.sihcp` nos três módulos Maven (`sihcp-core`, `sihcp-desktop`, `sihcp-server`), seguindo a sequência: atualização dos `pom.xml` → renomeação dos arquivos Java → atualização das configurações → verificação e documentação.

## Tarefas

- [x] 1. Atualizar os arquivos `pom.xml` (Fase 1)
  - [x] 1.1 Atualizar o `pom.xml` raiz
    - Substituir `<groupId>com.inventario</groupId>` por `<groupId>com.inventario.sihcp</groupId>`
    - Atualizar todas as referências a `mainClass` nos perfis `fat-jar`, `mobile`, `thin-jar` e `producao`:
      - `com.inventario.SistemaInventarioApplication` → `com.inventario.sihcp.SistemaInventarioApplication`
      - `com.inventario.MobileApiApplication` → `com.inventario.sihcp.MobileApiApplication`
    - _Requirements: 4.1, 5.1, 5.2_

  - [x] 1.2 Atualizar os `pom.xml` dos módulos filhos (`sihcp-core`, `sihcp-desktop`, `sihcp-server`)
    - Em cada módulo filho, atualizar a seção `<parent>`: `<groupId>com.inventario</groupId>` → `<groupId>com.inventario.sihcp</groupId>`
    - Em `sihcp-desktop` e `sihcp-server`, atualizar o `<groupId>` das dependências internas que referenciam `sihcp-core`
    - Em `sihcp-desktop/pom.xml`, atualizar `<mainClass>` para `com.inventario.sihcp.SistemaInventarioApplication`
    - Em `sihcp-server/pom.xml`, atualizar `<mainClass>` para `com.inventario.sihcp.MobileApiApplication`
    - _Requirements: 4.1, 4.2, 4.3, 5.1, 5.2_

  - [ ]* 1.3 Escrever teste de propriedade para ausência de referências ao pacote antigo nos `pom.xml`
    - **Propriedade 5: Ausência de referências ao pacote antigo nos pom.xml**
    - Para qualquer `pom.xml` nos módulos Maven, não deve existir ocorrência de `com.inventario` sem o sufixo `.sihcp` nos elementos `<groupId>`, `<mainClass>`, `<parent>` e `<dependency>`
    - Implementar como teste JUnit que varre os 4 arquivos `pom.xml` e verifica a invariante
    - **Validates: Requirements 4.1, 4.2, 4.3, 5.1, 5.2**

- [x] 2. Checkpoint — Verificar build após atualização dos `pom.xml`
  - Executar `mvn clean package -DskipTests` a partir da raiz do projeto e confirmar exit code 0.
  - Garantir que todos os testes passam, perguntar ao usuário se surgirem dúvidas.

- [x] 3. Renomear os arquivos Java dos módulos Maven (Fase 2)
  - [x] 3.1 Mover os arquivos-fonte de `sihcp-core` para o novo diretório de pacote
    - Criar a estrutura `sihcp-core/src/main/java/com/inventario/sihcp/` replicando todos os subpacotes existentes
    - Mover todos os arquivos `.java` de `sihcp-core/src/main/java/com/inventario/` para `sihcp-core/src/main/java/com/inventario/sihcp/`
    - Atualizar a declaração `package com.inventario` → `package com.inventario.sihcp` (e subpacotes) em cada arquivo movido
    - Atualizar todas as instruções `import com.inventario.` → `import com.inventario.sihcp.` em cada arquivo
    - Atualizar FQNs em strings e anotações (ex.: `@SpringBootApplication(scanBasePackages = "com.inventario")` → `"com.inventario.sihcp"`)
    - Remover o diretório `sihcp-core/src/main/java/com/inventario/` após a migração completa
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 3.2 Escrever teste de propriedade para ausência de declarações de pacote antigas em `sihcp-core`
    - **Propriedade 1: Ausência de declarações de pacote com prefixo antigo**
    - Para qualquer arquivo `.java` em `sihcp-core/src/main/java/`, a declaração `package` não deve começar com `package com.inventario` sem o sufixo `.sihcp`
    - Usar `JavaSourceFileGenerator` para enumerar os arquivos e verificar com regex `(?m)^package com\.inventario(?!\.sihcp).*$`
    - **Validates: Requirements 1.1**

  - [ ]* 3.3 Escrever teste de propriedade para ausência de importações antigas em `sihcp-core`
    - **Propriedade 2: Ausência de importações com prefixo antigo**
    - Para qualquer arquivo `.java` em `sihcp-core/src/main/java/`, nenhuma linha de importação deve começar com `import com.inventario.` sem o sufixo `sihcp`
    - Verificar com regex `(?m)^import com\.inventario\.(?!sihcp).*$`
    - **Validates: Requirements 1.2**

  - [ ]* 3.4 Escrever teste de propriedade para consistência pacote/diretório em `sihcp-core`
    - **Propriedade 3: Consistência entre declaração de pacote e localização no sistema de arquivos**
    - Para qualquer arquivo `.java` com declaração `com.inventario.sihcp.X`, o caminho relativo deve conter o segmento `com/inventario/sihcp/X/`
    - **Validates: Requirements 1.3**

  - [x] 3.5 Mover os arquivos-fonte de `sihcp-desktop` para o novo diretório de pacote
    - Aplicar o mesmo processo da tarefa 3.1 para `sihcp-desktop/src/main/java/com/inventario/`
    - Garantir que a classe `SistemaInventarioApplication` seja movida para `com.inventario.sihcp`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 6.1_

  - [x] 3.6 Mover os arquivos-fonte de `sihcp-server` para o novo diretório de pacote
    - Aplicar o mesmo processo da tarefa 3.1 para `sihcp-server/src/main/java/com/inventario/`
    - Garantir que a classe `MobileApiApplication` seja movida para `com.inventario.sihcp`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 6.2_

  - [ ]* 3.7 Escrever testes de propriedade para `sihcp-desktop` e `sihcp-server`
    - **Propriedade 1 (módulos desktop e server): Ausência de declarações de pacote antigas**
    - **Propriedade 2 (módulos desktop e server): Ausência de importações antigas**
    - **Propriedade 3 (módulos desktop e server): Consistência pacote/diretório**
    - Reutilizar o `JavaSourceFileGenerator` parametrizando o diretório raiz de cada módulo
    - **Validates: Requirements 1.1, 1.2, 1.3**

  - [ ]* 3.8 Escrever teste de propriedade para preservação dos subpacotes
    - **Propriedade 4: Preservação dos subpacotes**
    - Para cada subpacote `com.inventario.X` que existia antes (representado por diretório no sistema de arquivos), deve existir o diretório correspondente `com.inventario.sihcp.X` após a renomeação
    - Verificar nos três módulos Maven
    - **Validates: Requirements 1.4**

- [x] 4. Atualizar os arquivos de teste (src/test/)
  - [x] 4.1 Atualizar declarações de pacote e importações nos arquivos de teste dos três módulos
    - Aplicar as mesmas substituições das tarefas 3.1, 3.5 e 3.6 nos diretórios `src/test/java/com/inventario/` de cada módulo
    - Atualizar referências a FQNs com o pacote antigo em strings de teste
    - _Requirements: 2.1, 2.4_

  - [ ]* 4.2 Escrever testes de propriedade para ausência de referências antigas nos arquivos de teste
    - **Propriedade 1 (test): Ausência de declarações de pacote antigas nos arquivos de teste**
    - **Propriedade 2 (test): Ausência de importações antigas nos arquivos de teste**
    - Parametrizar o `JavaSourceFileGenerator` para varrer `src/test/java/` de cada módulo
    - **Validates: Requirements 2.1, 2.4**

- [x] 5. Checkpoint — Verificar compilação dos testes
  - Executar `mvn test-compile` e confirmar exit code 0.
  - Garantir que todos os testes passam, perguntar ao usuário se surgirem dúvidas.

- [x] 6. Atualizar os arquivos de configuração Spring Boot (Fase 3)
  - [x] 6.1 Atualizar `sihcp-server/src/main/resources/application.properties`
    - Substituir `logging.level.com.inventario` por `logging.level.com.inventario.sihcp`
    - _Requirements: 3.1_

  - [x] 6.2 Atualizar `sihcp-server/src/main/resources/application-mobile.properties`
    - Substituir `logging.level.com.inventario=` por `logging.level.com.inventario.sihcp=`
    - Substituir `logging.level.com.inventario.mobile.server.controller` por `logging.level.com.inventario.sihcp.mobile.server.controller`
    - Substituir `logging.level.com.inventario.mobile.server.service` por `logging.level.com.inventario.sihcp.mobile.server.service`
    - _Requirements: 3.1, 3.2_

  - [x] 6.3 Verificar e atualizar demais arquivos de configuração
    - Inspecionar e atualizar `application-prod.properties` (raiz do projeto)
    - Inspecionar e atualizar `sihcp-server/src/main/resources/application-test.properties` (se existir)
    - Inspecionar e atualizar `sihcp-server/src/main/resources/application-performance.properties` (se existir)
    - Inspecionar e atualizar `sihcp-core/src/main/resources/log4j2.xml` (se contiver referências ao pacote)
    - _Requirements: 3.1, 3.2_

  - [ ]* 6.4 Escrever teste de propriedade para ausência de referências ao pacote antigo nos arquivos de configuração
    - **Propriedade 6: Ausência de referências ao pacote antigo nos arquivos de configuração**
    - Para qualquer arquivo `.properties` ou `.yml` nos módulos Maven, nenhuma chave deve conter `com.inventario` sem o sufixo `.sihcp`
    - **Validates: Requirements 3.1, 3.2**

  - [ ]* 6.5 Escrever teste de propriedade para scan de componentes Spring
    - **Propriedade 7: Scan de componentes Spring abrange o novo pacote raiz**
    - Para qualquer classe anotada com `@SpringBootApplication` nos módulos Maven, o pacote base de scan (implícito ou explícito via `scanBasePackages`) deve ser `com.inventario.sihcp` ou um de seus subpacotes
    - Verificar que nenhuma anotação `@SpringBootApplication` referencia `com.inventario` sem `.sihcp`
    - **Validates: Requirements 6.3**

- [x] 7. Checkpoint — Build verde e testes verdes
  - Executar `mvn clean package -DskipTests` e confirmar exit code 0 (Build Verde).
  - Executar `mvn test` e confirmar que o número de testes aprovados é igual ao estado anterior à renomeação (Testes Verdes).
  - Executar `mvn clean package -P fat-jar -DskipTests` e confirmar exit code 0.
  - Executar `mvn clean package -P mobile -DskipTests` e confirmar exit code 0.
  - Garantir que todos os testes passam, perguntar ao usuário se surgirem dúvidas.

- [x] 8. Verificar proteção do app Android
  - [x] 8.1 Confirmar que nenhum arquivo em `InventarioMobile/` foi modificado
    - Executar `git diff --name-only InventarioMobile/` e verificar que o resultado é vazio
    - Executar `grep -r "com.inventario.sihcp" InventarioMobile/` e verificar que o resultado é vazio
    - _Requirements: 7.4_

- [x] 9. Atualizar documentação e gerar relatório de mudanças
  - [x] 9.1 Atualizar `CHANGELOG.md`
    - Adicionar entrada descrevendo a mudança de pacote, a versão afetada e a data de execução
    - _Requirements: 7.2_

  - [x] 9.2 Atualizar `README.md`
    - Atualizar instruções de execução para referenciar `com.inventario.sihcp.SistemaInventarioApplication` e `com.inventario.sihcp.MobileApiApplication`
    - _Requirements: 7.3_

  - [x] 9.3 Gerar relatório de arquivos modificados
    - Produzir (ou registrar no `CHANGELOG.md`) a lista de todos os arquivos modificados durante a renomeação com o número de ocorrências substituídas por arquivo
    - Pode ser obtido via `git diff --stat` após a refatoração
    - _Requirements: 7.1_

- [x] 10. Checkpoint final — Garantir que todos os testes passam
  - Executar `mvn test` uma última vez e confirmar resultado idêntico ao estado anterior à renomeação.
  - Garantir que todos os testes passam, perguntar ao usuário se surgirem dúvidas.

## Notas

- Tarefas marcadas com `*` são opcionais e podem ser puladas para uma entrega mais rápida
- Os testes de propriedade usam **JUnit-Quickcheck 1.0** (já declarado no `pom.xml` raiz)
- O diretório `InventarioMobile/` deve ser **explicitamente excluído** de qualquer ferramenta de busca e substituição em massa
- A ordem das fases (pom.xml → Java → configurações) garante que o build permaneça compilável ao final de cada fase
- Em caso de erros de compilação residuais, executar `mvn clean compile 2>&1 | grep "com.inventario"` para identificar referências não atualizadas
- Cada tarefa referencia os requisitos específicos para rastreabilidade
