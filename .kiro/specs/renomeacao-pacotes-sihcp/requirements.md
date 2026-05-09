# Documento de Requisitos

## Introdução

Esta feature trata da renomeação do pacote raiz Java do SIHCP (Sistema de Histórico e Coleta Patrimonial) de `com.inventario` para `com.inventario.sihcp`. O objetivo é alinhar a identidade do pacote ao nome oficial do sistema, tornando o código mais expressivo e preparando a base para futuras integrações e publicações. A mudança abrange os três módulos Maven do projeto (`sihcp-core`, `sihcp-desktop`, `sihcp-server`), os arquivos de teste em `src/test/`, os arquivos de configuração Spring Boot e os arquivos de build Maven (`pom.xml`). O app Android (`InventarioMobile/`) mantém seu pacote atual `com.ifmt.inventariomobile` e está fora do escopo desta feature.

## Glossário

- **Pacote_Raiz_Atual**: O pacote Java `com.inventario`, atualmente usado como raiz de todos os módulos Java do projeto.
- **Pacote_Raiz_Novo**: O pacote Java `com.inventario.sihcp`, destino da renomeação.
- **Módulo_Maven**: Cada um dos três subprojetos Maven do SIHCP: `sihcp-core`, `sihcp-desktop` e `sihcp-server`.
- **Ferramenta_Refatoracao**: IDE ou ferramenta de linha de comando capaz de renomear pacotes Java e atualizar todas as referências automaticamente (ex.: IntelliJ IDEA, Eclipse, OpenRewrite).
- **Arquivo_Fonte**: Qualquer arquivo `.java` ou `.kt` que contenha declaração de pacote ou importação referenciando o Pacote_Raiz_Atual.
- **Arquivo_Configuracao**: Arquivos `application*.properties`, `application*.yml` e similares que contenham referências ao Pacote_Raiz_Atual em propriedades de logging ou scan de componentes.
- **Arquivo_Build**: Arquivos `pom.xml` que contenham o `groupId` ou referências ao Pacote_Raiz_Atual.
- **Suite_Testes**: Conjunto de classes de teste localizadas em `src/test/java/com/inventario/` e nos módulos Maven.
- **Build_Verde**: Estado em que `mvn clean package -DskipTests` conclui sem erros de compilação.
- **Testes_Verdes**: Estado em que `mvn test` conclui sem falhas de teste.

---

## Requisitos

### Requisito 1: Renomeação dos Arquivos Fonte Java

**User Story:** Como desenvolvedor do SIHCP, quero que todos os arquivos `.java` dos módulos Maven usem o pacote `com.inventario.sihcp` como raiz, para que o código reflita a identidade oficial do sistema.

#### Critérios de Aceitação

1. THE Ferramenta_Refatoracao SHALL atualizar a declaração `package com.inventario` para `package com.inventario.sihcp` em todos os Arquivos_Fonte dos módulos `sihcp-core`, `sihcp-desktop` e `sihcp-server`.
2. THE Ferramenta_Refatoracao SHALL atualizar todas as instruções `import com.inventario.*` para `import com.inventario.sihcp.*` nos Arquivos_Fonte dos três Módulos_Maven.
3. WHEN a renomeação for concluída, THE Ferramenta_Refatoracao SHALL mover os Arquivos_Fonte para a estrutura de diretórios correspondente ao Pacote_Raiz_Novo (ex.: `src/main/java/com/inventario/sihcp/`).
4. THE Ferramenta_Refatoracao SHALL preservar todos os subpacotes existentes abaixo do Pacote_Raiz_Novo (ex.: `com.inventario.sihcp.model`, `com.inventario.sihcp.service`, `com.inventario.sihcp.mobile.server.controller`).
5. IF algum Arquivo_Fonte contiver referência ao Pacote_Raiz_Atual após a renomeação, THEN THE Build_Verde SHALL falhar com erro de compilação identificando o arquivo e a linha afetados.

---

### Requisito 2: Atualização dos Arquivos de Teste

**User Story:** Como desenvolvedor do SIHCP, quero que todos os arquivos de teste também usem o pacote `com.inventario.sihcp`, para que a Suite_Testes continue compilando e executando corretamente após a renomeação.

#### Critérios de Aceitação

1. THE Ferramenta_Refatoracao SHALL atualizar a declaração de pacote e as importações em todos os Arquivos_Fonte localizados em `src/test/java/com/inventario/` e nos diretórios de teste dos três Módulos_Maven.
2. WHEN a renomeação dos testes for concluída, THE Suite_Testes SHALL compilar sem erros ao executar `mvn test-compile`.
3. WHEN a renomeação dos testes for concluída, THE Suite_Testes SHALL executar sem falhas ao executar `mvn test`, produzindo o mesmo número de testes aprovados que antes da renomeação.
4. IF um teste referenciar uma classe pelo nome qualificado completo com o Pacote_Raiz_Atual, THEN THE Ferramenta_Refatoracao SHALL atualizar essa referência para o Pacote_Raiz_Novo.

---

### Requisito 3: Atualização dos Arquivos de Configuração Spring Boot

**User Story:** Como operador do sistema, quero que os arquivos de configuração Spring Boot referenciem o novo pacote, para que o logging e o scan de componentes funcionem corretamente após a renomeação.

#### Critérios de Aceitação

1. THE Ferramenta_Refatoracao SHALL substituir todas as ocorrências de `logging.level.com.inventario` por `logging.level.com.inventario.sihcp` em todos os Arquivos_Configuracao dos módulos `sihcp-core`, `sihcp-desktop`, `sihcp-server` e na raiz do projeto.
2. THE Ferramenta_Refatoracao SHALL substituir todas as ocorrências de `logging.level.com.inventario.mobile.server` por `logging.level.com.inventario.sihcp.mobile.server` nos Arquivos_Configuracao que contenham referências a subpacotes específicos.
3. WHEN o servidor for iniciado após a renomeação, THE Sistema SHALL emitir entradas de log no nível configurado para o pacote `com.inventario.sihcp`, confirmando que o logging está ativo para o novo pacote.
4. IF um Arquivo_Configuracao contiver referência ao Pacote_Raiz_Atual após a renomeação, THEN THE Sistema SHALL registrar um aviso no log de inicialização indicando que a configuração de logging pode estar desatualizada.

---

### Requisito 4: Atualização dos Arquivos de Build Maven

**User Story:** Como desenvolvedor do SIHCP, quero que os arquivos `pom.xml` reflitam o novo pacote raiz, para que o `groupId` do projeto esteja alinhado com a identidade do sistema.

#### Critérios de Aceitação

1. THE Ferramenta_Refatoracao SHALL atualizar o `groupId` de `com.inventario` para `com.inventario.sihcp` no `pom.xml` raiz e nos `pom.xml` de cada Módulo_Maven.
2. WHEN o `groupId` for atualizado, THE Ferramenta_Refatoracao SHALL atualizar a referência `<groupId>` dentro da seção `<parent>` de cada `pom.xml` filho para `com.inventario.sihcp`.
3. THE Ferramenta_Refatoracao SHALL atualizar todas as referências ao `groupId` `com.inventario` em dependências internas declaradas entre os Módulos_Maven.
4. WHEN a atualização dos `pom.xml` for concluída, THE Build_Verde SHALL ser atingido ao executar `mvn clean package -DskipTests` a partir da raiz do projeto.
5. IF o `groupId` de um Módulo_Maven não for atualizado, THEN THE Build_Verde SHALL falhar com erro de resolução de dependência identificando o módulo afetado.

---

### Requisito 5: Atualização dos Perfis e Classes de Entrada da Aplicação

**User Story:** Como operador do sistema, quero que os perfis Maven e as classes de entrada (`mainClass`) referenciem o novo pacote, para que a aplicação possa ser iniciada corretamente após a renomeação.

#### Critérios de Aceitação

1. THE Ferramenta_Refatoracao SHALL atualizar todas as referências a `com.inventario.SistemaInventarioApplication` para `com.inventario.sihcp.SistemaInventarioApplication` nos `pom.xml` e scripts de execução.
2. THE Ferramenta_Refatoracao SHALL atualizar todas as referências a `com.inventario.MobileApiApplication` para `com.inventario.sihcp.MobileApiApplication` nos `pom.xml` e scripts de execução.
3. WHEN os perfis Maven forem atualizados, THE Build_Verde SHALL ser atingido ao executar `mvn clean package -P fat-jar` e `mvn clean package -P mobile`.
4. IF uma referência a `mainClass` contiver o Pacote_Raiz_Atual após a renomeação, THEN THE Build_Verde SHALL falhar com erro de classe não encontrada ao tentar executar o JAR gerado.

---

### Requisito 6: Preservação do Comportamento Funcional

**User Story:** Como usuário do SIHCP, quero que todas as funcionalidades do sistema continuem operando normalmente após a renomeação dos pacotes, para que a mudança seja transparente do ponto de vista operacional.

#### Critérios de Aceitação

1. WHEN a renomeação for concluída, THE Sistema SHALL iniciar a aplicação desktop sem erros ao executar `mvn exec:java -Dexec.mainClass="com.inventario.sihcp.SistemaInventarioApplication"`.
2. WHEN a renomeação for concluída, THE Sistema SHALL iniciar o servidor mobile sem erros ao executar com o perfil `mobile`.
3. THE Sistema SHALL manter todas as anotações Spring (`@Component`, `@Service`, `@Repository`, `@Controller`, `@Configuration`) funcionais após a renomeação, sem necessidade de reconfiguração manual do scan de componentes.
4. WHEN a renomeação for concluída, THE Suite_Testes SHALL produzir resultado idêntico ao estado anterior à renomeação, sem regressões.
5. IF qualquer funcionalidade previamente operacional deixar de funcionar após a renomeação, THEN THE Sistema SHALL registrar no log de erro a causa raiz com referência ao pacote ou classe afetada.

---

### Requisito 7: Rastreabilidade e Documentação da Mudança

**User Story:** Como mantenedor do projeto, quero que a renomeação seja documentada e rastreável, para que futuros desenvolvedores entendam a origem e o propósito da mudança.

#### Critérios de Aceitação

1. THE Ferramenta_Refatoracao SHALL produzir um relatório listando todos os arquivos modificados durante a renomeação, com o número de ocorrências substituídas por arquivo.
2. WHEN a renomeação for concluída, THE Sistema SHALL ter o arquivo `CHANGELOG.md` atualizado com uma entrada descrevendo a mudança de pacote, a versão afetada e a data de execução.
3. THE Sistema SHALL manter o arquivo `README.md` atualizado com as novas instruções de execução referenciando o Pacote_Raiz_Novo.
4. IF o app Android (`InventarioMobile/`) for afetado pela renomeação do pacote Java, THEN THE Ferramenta_Refatoracao SHALL registrar um aviso explícito indicando que o pacote Android `com.ifmt.inventariomobile` está fora do escopo desta feature e não deve ser alterado.
