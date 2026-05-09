# Plano de Implementação: Implantação em Novos Campus (implantacao-campus)

## Visão Geral

Implementação do Pacote de Implantação SIHCP — conjunto de artefatos, scripts e documentação que permite a qualquer campus do IFMT instalar e operar o sistema de forma autônoma. O pacote cobre duas modalidades (manual e automatizada), verificação de saúde, atualização de versão, diagnóstico e as adequações necessárias no código Java/Spring Boot para suporte multi-campus.

## Tarefas

- [x] 1. Criar a classe `CampusConfig` e estender o `DatabaseConfigManager`
  - [x] 1.1 Criar `src/main/java/com/inventario/config/CampusConfig.java` com campos `nome`, `sigla`, `cidade`, `estado`, `responsavelTecnico`, `contato` e getters/setters
    - Validar que `nome` não é nulo e tem no máximo 100 caracteres no construtor/setter
    - _Requisitos: 6.1, 6.3_
  - [ ]* 1.2 Escrever testes unitários para `CampusConfig`
    - Testar parsing de JSON com todos os campos presentes
    - Testar parsing com campos opcionais ausentes
    - Testar rejeição de nome vazio e nome com mais de 100 caracteres
    - _Requisitos: 6.3_
  - [x] 1.3 Adicionar método `getCampusConfig()` em `DatabaseConfigManager` que lê a seção `campus` do `configuracao_banco.json`
    - Retornar `CampusConfig` populado; retornar objeto com campos nulos se a seção não existir (compatibilidade retroativa)
    - _Requisitos: 6.1, 6.2_
  - [ ]* 1.4 Escrever testes unitários para `DatabaseConfigManager.getCampusConfig()`
    - Testar leitura correta quando seção `campus` está presente
    - Testar comportamento quando seção `campus` está ausente
    - _Requisitos: 6.1_

- [x] 2. Implementar `QrCodeGenerator` como utilitário standalone
  - [x] 2.1 Criar `src/main/java/com/inventario/util/QrCodeGenerator.java` usando ZXing 3.5.2 (já no projeto)
    - Método `generate(String content, String outputPath, int size)` que grava PNG via `MatrixToImageWriter`
    - Método `main(String[] args)` para invocação via linha de comando pelo script de setup
    - _Requisitos: 7.4_
  - [ ]* 2.2 Escrever testes unitários para `QrCodeGenerator`
    - Gerar PNG para uma URL de exemplo e verificar que o arquivo existe e tem tamanho > 0
    - Decodificar o PNG gerado com ZXing e verificar que o conteúdo bate com o input
    - _Requisitos: 7.4_

- [x] 3. Implementar endpoint REST de informações do campus
  - [x] 3.1 Criar `CampusInfoDTO` em `src/main/java/com/inventario/mobile/server/dto/` com campos `nome`, `sigla`, `cidade`
    - _Requisitos: 6.1_
  - [x] 3.2 Criar `MobileCampusController` em `src/main/java/com/inventario/mobile/server/controller/` com `GET /api/mobile/campus/info` (público, sem autenticação)
    - Injetar `DatabaseConfigManager`, chamar `getCampusConfig()` e retornar `CampusInfoDTO` encapsulado em `ApiResponse`
    - Registrar o endpoint como público no `SecurityConfig` da API Mobile
    - _Requisitos: 6.1_
  - [ ]* 3.3 Escrever testes de integração para `GET /api/mobile/campus/info`
    - Testar resposta 200 com dados corretos quando `configuracao_banco.json` tem seção `campus`
    - Testar resposta 200 com campos nulos quando seção `campus` está ausente
    - _Requisitos: 6.1_

- [x] 4. Checkpoint — compilar e testar o código Java
  - Executar `mvn clean package -DskipTests` e garantir que não há erros de compilação
  - Executar `mvn test` e garantir que todos os testes das tarefas 1–3 passam
  - Perguntar ao usuário se há dúvidas antes de prosseguir para os scripts

- [x] 5. Criar o SQL consolidado `setup_banco_completo.sql`
  - [x] 5.1 Criar o script `sql/setup_banco_completo.sql` concatenando, na ordem correta, os scripts SQL existentes em `sql/`
    - Incluir cabeçalho com versão e data
    - Adicionar `CREATE EXTENSION IF NOT EXISTS "uuid-ossp"` e `"pgcrypto"`
    - Incluir na ordem: tabelas base → views → triggers → índices
    - Usar `CREATE TABLE IF NOT EXISTS` e `INSERT ... ON CONFLICT DO NOTHING` em todos os statements para garantir idempotência
    - Criar tabela `schema_version` com `IF NOT EXISTS`
    - Inserir setores padrão (Administração, TI, Biblioteca) com `ON CONFLICT DO NOTHING`
    - Inserir registro de versão inicial em `schema_version`
    - _Requisitos: 2.5_
  - [ ]* 5.2 Escrever teste de propriedade para idempotência do SQL (Propriedade 1)
    - **Propriedade 1: Idempotência do SQL de Setup**
    - **Valida: Requisito 2.5**
    - Usando jqwik: executar `setup_banco_completo.sql` duas vezes em banco PostgreSQL de teste e verificar que o número de tabelas é igual após a segunda execução e que não há erros
    - _Requisitos: 2.5_

- [x] 6. Criar o template de configuração e o arquivo `configuracao_banco.json`
  - [x] 6.1 Criar `config/application.properties.template` com placeholders para porta, nome do campus e perfis Spring (`mobile,prod`)
    - _Requisitos: 4.3_
  - [x] 6.2 Criar a classe Java `SetupConfig` em `src/main/java/com/inventario/config/` para representar as entradas coletadas pelo setup (nome, sigla, cidade, estado, responsavel, contato, portaApi, hostPg, portaPg, banco, usuarioPg, senhaPg, senhaAdmin)
    - _Requisitos: 4.2, 4.3_
  - [x] 6.3 Criar a classe Java `ConfigGenerator` em `src/main/java/com/inventario/config/` com método estático `generate(SetupConfig input): String` que serializa o JSON do `configuracao_banco.json` usando Jackson
    - _Requisitos: 4.3_
  - [ ]* 6.4 Escrever teste de propriedade para round-trip de configuração (Propriedade 2)
    - **Propriedade 2: Geração de Configuração a partir de Entradas**
    - **Valida: Requisito 4.3**
    - Usando jqwik: para qualquer `nomeCampus` (1–100 chars) e `porta` (1024–65535), verificar que `ConfigGenerator.generate()` produz JSON que, quando lido por `DatabaseConfigManager`, retorna os mesmos valores
    - _Requisitos: 4.3_

- [x] 7. Criar os scripts de inicialização manual (`iniciar-servidor.bat` e `iniciar-servidor.sh`)
  - [x] 7.1 Criar `scripts/iniciar-servidor.bat` (Windows) com as verificações de pré-condição e o comando de inicialização do JAR
    - Verificar JDK 21 via `java -version`; se ausente, exibir mensagem com link `https://adoptium.net` e `exit /b 1`
    - Verificar existência de `config\configuracao_banco.json`; se ausente, exibir mensagem e `exit /b 1`
    - Verificar conectividade TCP com o banco lendo host/porta do JSON (via PowerShell inline ou `Test-NetConnection`); se inacessível, exibir `"Banco de dados inacessível. Verifique as configurações em configuracao_banco.json"` e `exit /b 1`
    - Verificar se a porta da API está em uso via `netstat -ano | findstr :{porta}`; se em uso, exibir mensagem e `exit /b 1`
    - Iniciar o JAR com `java -Xms256m -Xmx1g -Dinventario.config.mode=APP_DIR -jar bin\mobile-server.jar --spring.profiles.active=mobile,prod --spring.config.additional-location=config\`
    - _Requisitos: 2.2, 2.3, 2.4, 2.6, 6.4_
  - [x] 7.2 Criar `scripts/iniciar-servidor.sh` (Linux/macOS) com lógica equivalente ao `.bat`
    - Usar `ss -tlnp | grep :{porta}` para verificar porta em uso
    - Usar `nc -z` ou `/dev/tcp` para verificar conectividade com o banco
    - _Requisitos: 2.2, 2.3, 2.4, 2.6, 6.4_

- [x] 8. Criar o script de setup automatizado (`setup.ps1` e `setup.sh`)
  - [x] 8.1 Criar `scripts/setup.ps1` (Windows) com as 6 etapas interativas
    - Etapa 1/6: Validar pré-requisitos (`java -version` para JDK 21, `psql --version` para PostgreSQL); encerrar com mensagem descritiva se ausentes
    - Etapa 2/6: Coletar dados interativamente (nome do campus com validação de 1–100 chars, sigla, cidade, estado, porta da API com validação 1024–65535 e verificação de disponibilidade, host/porta/banco/usuário/senha do PostgreSQL, senha do admin SIHCP)
    - Etapa 3/6: Gerar `config/configuracao_banco.json` e `config/application.properties` a partir do template usando os valores coletados
    - Etapa 4/6: Executar `setup_banco_completo.sql` via `psql`; em caso de falha, exibir etapa, erro e instruções sem apagar configurações já geradas
    - Etapa 5/6: Criar usuário `admin` no banco com senha BCrypt via INSERT SQL (usar `pgcrypto` já habilitado no SQL de setup)
    - Etapa 6/6: Invocar `QrCodeGenerator` via `java -cp` para gerar `qrcode/api-qrcode.png`; exibir resumo com URL da API, credenciais do admin e caminho do APK
    - _Requisitos: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 6.1, 6.2, 6.3, 6.4, 7.3_
  - [x] 8.2 Criar `scripts/setup.sh` (Linux/macOS) com lógica equivalente ao `setup.ps1`
    - _Requisitos: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

- [x] 9. Criar o script de verificação de saúde (`verificar-saude.ps1` e `verificar-saude.sh`)
  - [x] 9.1 Criar `scripts/verificar-saude.ps1` (Windows) executando os 4 testes em sequência
    - Teste 1: Conectividade TCP com o banco (lendo host/porta do `configuracao_banco.json`)
    - Teste 2: `GET /api/mobile/health` — verificar HTTP 200
    - Teste 3: `POST /api/mobile/auth/login` com credenciais do admin — verificar token retornado
    - Teste 4: `GET /api/mobile/patrimonio` com o token — verificar HTTP 200
    - Exibir `✅ Sistema SIHCP operacional e pronto para uso` e URL se todos passarem
    - Exibir `❌ Falha no teste: [componente]` com erro e link para `SOLUCAO_PROBLEMAS.md#ancora` se algum falhar
    - Gravar resultado em `relatorio-saude-{YYYYMMDD-HHmm}.txt`
    - _Requisitos: 5.1, 5.2, 5.3, 5.4, 5.5_
  - [x] 9.2 Criar `scripts/verificar-saude.sh` (Linux/macOS) com lógica equivalente usando `curl`
    - _Requisitos: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 10. Checkpoint — testar scripts de setup e verificação de saúde
  - Executar `scripts/setup.ps1` (ou `.sh`) em ambiente de desenvolvimento e verificar que os arquivos de configuração são gerados corretamente
  - Executar `scripts/verificar-saude.ps1` (ou `.sh`) com a API rodando e verificar saída `✅ OPERACIONAL`
  - Perguntar ao usuário se há ajustes necessários antes de prosseguir

- [x] 11. Criar o script de atualização de versão (`atualizar.ps1` e `atualizar.sh`)
  - [x] 11.1 Criar `scripts/atualizar.ps1` (Windows) com as 6 etapas de atualização
    - Etapa 1: Criar backup via `pg_dump` → `backups/sispatrimonio_backup_{data}.backup`; encerrar sem alterações se o backup falhar
    - Etapa 2: Parar o servidor se estiver em execução como serviço
    - Etapa 3: Substituir JARs em `bin/`
    - Etapa 4: Aplicar apenas scripts SQL de migração novos (verificando tabela `schema_version`); em caso de falha, restaurar backup via `pg_restore` e exibir erro
    - Etapa 5: Reiniciar o servidor
    - Etapa 6: Exibir versão anterior → versão atual e número de migrações aplicadas
    - _Requisitos: 8.1, 8.2, 8.3, 8.4, 8.5_
  - [x] 11.2 Criar `scripts/atualizar.sh` (Linux/macOS) com lógica equivalente
    - _Requisitos: 8.1, 8.2, 8.3, 8.4, 8.5_
  - [ ]* 11.3 Escrever teste de propriedade para backup antes de atualização (Propriedade 4)
    - **Propriedade 4: Backup Antes de Atualização**
    - **Valida: Requisito 8.2**
    - Verificar que, para qualquer execução do script de atualização que chegue à etapa de SQL, existe um arquivo de backup válido criado nessa mesma execução no diretório `backups/`
    - _Requisitos: 8.2_

- [x] 12. Criar o script de diagnóstico (`coletar-diagnostico.ps1` e `coletar-diagnostico.sh`)
  - [x] 12.1 Criar `scripts/coletar-diagnostico.ps1` (Windows) que gera `diagnostico-{campus}-{data}.zip`
    - Coletar últimas 1000 linhas dos logs da API Mobile em `logs/`
    - Coletar `java -version` e `psql --version`
    - Coletar configuração anonimizada (host, porta, banco — **sem senha**) do `configuracao_banco.json`
    - Executar `verificar-saude.ps1` e incluir o relatório gerado
    - Coletar `systeminfo` (Windows)
    - Compactar tudo em `diagnostico-{campus}-{data}.zip` usando `Compress-Archive`
    - _Requisitos: 9.2, 9.3_
  - [x] 12.2 Criar `scripts/coletar-diagnostico.sh` (Linux/macOS) com lógica equivalente usando `zip` e `uname -a`, `free -h`, `df -h`
    - _Requisitos: 9.2, 9.3_
  - [ ]* 12.3 Escrever teste de propriedade para diagnóstico sem credenciais (Propriedade 5)
    - **Propriedade 5: Diagnóstico Não Expõe Credenciais**
    - **Valida: Requisito 9.3**
    - Usando jqwik: para qualquer senha gerada aleatoriamente, verificar que nenhum arquivo dentro do ZIP de diagnóstico contém a senha em texto plano
    - _Requisitos: 9.3_

- [x] 13. Criar a documentação do pacote de implantação
  - [x] 13.1 Criar `README.md` na raiz do pacote com guia de início rápido em português (escolha de modalidade, pré-requisitos, primeiros passos)
    - _Requisitos: 1.2_
  - [x] 13.2 Criar `INSTALACAO_MANUAL.md` com guia passo a passo cobrindo instalação do JDK 21, PostgreSQL 12+, criação do banco e execução dos JARs
    - _Requisitos: 2.1_
  - [x] 13.3 Criar `CHECKLIST_IMPLANTACAO.md` com todas as etapas de verificação pós-instalação
    - _Requisitos: 1.3_
  - [x] 13.4 Criar `CONFIGURAR_APP_ANDROID.md` com instruções para habilitar fontes desconhecidas, instalar o APK, configurar o IP do servidor e usar o QR Code
    - Incluir seção para campus em rede interna sem acesso externo
    - _Requisitos: 7.2, 7.5_
  - [x] 13.5 Criar `SOLUCAO_PROBLEMAS.md` cobrindo: falha de conexão com banco, porta em uso, JDK não encontrado, APK não instalando
    - Incluir âncoras HTML (`#banco`, `#porta`, `#jdk`, `#apk`) para links diretos dos scripts de verificação
    - Incluir instruções de contato com a equipe de suporte (e-mail e repositório)
    - _Requisitos: 9.1, 9.4_

- [x] 14. Criar o script de build do pacote (`build-pacote-implantacao.ps1`)
  - [x] 14.1 Criar `build-pacote-implantacao.ps1` na raiz do projeto com as 6 etapas de build
    - Etapa 1/6: `mvn clean package -DskipTests -P thin-jar` para gerar `mobile-server.jar`
    - Etapa 2/6: `mvn package -DskipTests -P desktop-jar` para gerar `sihcp-desktop.jar`
    - Etapa 3/6: `cd InventarioMobile && .\gradlew.bat assembleRelease` para gerar o APK
    - Etapa 4/6: Concatenar scripts SQL na ordem correta para gerar `sql/setup_banco_completo.sql` (se não gerado manualmente)
    - Etapa 5/6: Montar estrutura do pacote em `dist/pacote-campus/` copiando todos os artefatos, scripts e documentação
    - Etapa 6/6: Compactar em `dist/SIHCP-Campus-v{versao}.zip` e verificar tamanho (exibir aviso se > 200 MB)
    - _Requisitos: 1.1, 1.4, 1.5_

- [x] 15. Implementar detecção de mudança de configuração e reinicialização automática da API Mobile
  - [x] 15.1 Criar `MobileServerProcessManager` em `src/main/java/com/inventario/config/` com métodos `start()`, `stop()` e `restart()` que gerenciam o processo da API Mobile via `ProcessBuilder`
    - _Requisitos: 6.5_
  - [x] 15.2 Adicionar listener de `WatchService` (Java NIO) no App Desktop que monitora mudanças em `configuracao_banco.json` e chama `MobileServerProcessManager.restart()` quando o arquivo é modificado
    - Registrar o listener na inicialização do App Desktop quando o modo `APP_DIR` estiver ativo
    - _Requisitos: 6.5_

- [x] 16. Checkpoint final — validar pacote completo
  - Executar `build-pacote-implantacao.ps1` e verificar que o ZIP é gerado sem erros
  - Verificar que todos os arquivos obrigatórios estão presentes no ZIP (README, scripts, JARs, SQL, documentação)
  - Verificar que o tamanho do ZIP está dentro do limite de 200 MB
  - Executar `mvn test` e garantir que todos os testes de propriedade e unitários passam
  - Perguntar ao usuário se há ajustes finais antes de considerar a feature concluída

## Notas

- Tarefas marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- Os testes de propriedade usam **jqwik** (Java) com mínimo de 100 iterações cada
- Os scripts `.ps1` e `.sh` devem ter permissão de execução (`chmod +x` no Linux/macOS)
- O `setup_banco_completo.sql` deve ser idempotente — seguro para re-execução sem erros
- A variável `INVENTARIO_CONFIG_MODE=APP_DIR` instrui o sistema a ler `configuracao_banco.json` do diretório `config/` local, isolando cada campus
- Cada tarefa referencia requisitos específicos para rastreabilidade completa
