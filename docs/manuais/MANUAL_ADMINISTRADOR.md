# Manual do Administrador — SIHCP

**Servidor da API Mobile, infraestrutura e operação**

Versão do sistema: 2.7.0 (servidor) — 2.21.0-security (app Android)
Stack: Java 21 + Spring Boot 3.2 + PostgreSQL 12+
Instituição: Instituto Federal de Mato Grosso

---

## Sumário

1. [Responsabilidades do administrador](#1-responsabilidades-do-administrador)
2. [Arquitetura do sistema](#2-arquitetura-do-sistema)
3. [Pré-requisitos de infraestrutura](#3-pré-requisitos-de-infraestrutura)
4. [Instalação do servidor](#4-instalação-do-servidor)
5. [Configuração de segredos](#5-configuração-de-segredos)
6. [Banco de dados PostgreSQL](#6-banco-de-dados-postgresql)
7. [Perfis Spring e variáveis de ambiente](#7-perfis-spring-e-variáveis-de-ambiente)
8. [Inicialização em produção](#8-inicialização-em-produção)
9. [Gestão de usuários e perfis](#9-gestão-de-usuários-e-perfis)
10. [Monitoramento e logs](#10-monitoramento-e-logs)
11. [Atualizações e deploy](#11-atualizações-e-deploy)
12. [Backup e restauração](#12-backup-e-restauração)
13. [Build e distribuição do APK](#13-build-e-distribuição-do-apk)
14. [Procedimentos de segurança](#14-procedimentos-de-segurança)
15. [Plano de contingência](#15-plano-de-contingência)
16. [Checklist operacional](#16-checklist-operacional)

---

## 1. Responsabilidades do administrador

Como administrador do SIHCP você responde por:

- Manter o servidor da API Mobile rodando
- Gerenciar segredos (senhas de banco, chaves JWT, keystore do APK)
- Criar e desativar contas de usuários (coletores, supervisores, administradores)
- Configurar inventários ativos no desktop
- Garantir backup do PostgreSQL
- Distribuir APKs assinados aos usuários
- Monitorar logs e métricas de uso
- Aplicar correções de segurança

---

## 2. Arquitetura do sistema

O SIHCP é um projeto Maven multi-módulo com três componentes ativos:

```
sistema-inventario/ (pom parent, v2.7.0)
├── sihcp-core/           Código compartilhado (entidades JPA, DAOs, utilitários)
├── sihcp-server/         API REST Spring Boot (porta 8081)
└── sihcp-desktop/        Aplicativo Swing para operadores
```

Os três módulos compartilham um único banco PostgreSQL. O app Android comunica-se exclusivamente com o `sihcp-server` via HTTP/HTTPS.

### Comunicação

```
┌──────────────────┐                ┌──────────────────────┐
│ App Android      │                │ sihcp-server         │
│ Retrofit client  │── HTTPS ──────►│ porta 8081           │
│ port: any        │◄──── JSON ─────│ context: /inventario │
└──────────────────┘                │ path base API Mobile:│
                                    │   /api/mobile/**     │
                                    └──────────┬───────────┘
                                               │ JDBC + HikariCP (max 30)
                                               ▼
                                    ┌──────────────────────┐
                                    │ PostgreSQL 12+       │
                                    │ database: sispatrimonio
                                    └──────────────────────┘
```

### Endpoints principais

Todos com base `/inventario/api/mobile/`:

| Categoria | Endpoints (exemplos) | Role mínima |
|---|---|---|
| Autenticação | `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` | público (login), COLETOR (refresh/logout) |
| Coletas | `POST /coletas`, `POST /coletas/batch`, `GET /coletas/pendentes` | COLETOR |
| Patrimônio | `GET /patrimonio/**` | CONSULTA |
| Salas/Setores | `GET /salas`, `GET /setores` | CONSULTA |
| Sincronização | `GET /sync/patrimonios`, `GET /sync/salas` | COLETOR |
| Usuários | `GET /usuarios`, `GET /usuarios/{id}` | ADMIN |
| Dashboard | `GET /dashboard/**` | CONSULTA |

A lista completa está no documento `steering/security-roles.md` e nos controllers em `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/controller/`.

---

## 3. Pré-requisitos de infraestrutura

### Servidor de aplicação

- Java 21 (LTS) instalado (`java -version` deve mostrar 21.x)
- Maven 3.6+ (para build em produção — opcional se rodando apenas o JAR)
- 4 GB RAM mínimos; 8 GB recomendados
- 10 GB de disco para aplicação + logs
- Porta 8081 livre (configurável)
- Acesso de rede aos clientes mobile

### Servidor de banco

- PostgreSQL 12 ou superior
- 8 GB RAM dedicados ao banco
- 50 GB de disco inicial (crescimento esperado ~1 GB/ano por campus)

### Rede

- Acesso HTTPS do app aos endpoints `/api/mobile/**`
- Em produção, use reverse proxy (nginx, Apache) com TLS
- Firewall permite tráfego das subnets dos coletores para a porta 8081 (ou 443 via proxy)

---

## 4. Instalação do servidor

### 4.1 Build do JAR

```powershell
cd C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO
.\mvnw.cmd -pl sihcp-server -am clean package -DskipTests
```

O JAR fica em `sihcp-server/target/sihcp-server-2.7.0.jar`.

### 4.2 Diretório de execução

Crie uma estrutura separada do código-fonte:

```
C:\SIHCP-PRODUCAO\
├── sihcp-server-2.7.0.jar
├── application-mobile.properties   (cópia do versionado)
├── application-prod.properties     (cópia do versionado)
├── logs\
└── data\                            (se usar SQLite offline)
```

### 4.3 Serviço Windows (opcional)

Para rodar como serviço, use [NSSM](https://nssm.cc/) ou `sc create`:

```powershell
nssm install SIHCP-Server "C:\Program Files\Java\jdk-21\bin\java.exe" "-jar -Xms512m -Xmx2g C:\SIHCP-PRODUCAO\sihcp-server-2.7.0.jar --spring.profiles.active=mobile,prod"
nssm set SIHCP-Server AppDirectory "C:\SIHCP-PRODUCAO"
nssm set SIHCP-Server Start SERVICE_AUTO_START
```

---

## 5. Configuração de segredos

A partir da versão 2.7.0, o SIHCP **não aceita mais segredos hardcoded** no `application-mobile.properties` versionado. Dois segredos são obrigatórios:

| Variável | Conteúdo | Onde vive |
|---|---|---|
| `DATASOURCE_PASSWORD` | Senha do usuário PostgreSQL | Variável de ambiente |
| `JWT_SECRET` | Chave HMAC-SHA256 para tokens JWT (mín. 32 chars) | Variável de ambiente |

Se ambas não estiverem definidas, o servidor aborta a inicialização com um banner claro:

```
============================================================
FALHA DE BOOTSTRAP — CONFIGURAÇÃO DE SEGREDOS INCOMPLETA
============================================================
  ✗ Variável de ambiente DATASOURCE_PASSWORD não definida
  ✗ Variável de ambiente JWT_SECRET não definida
```

### 5.1 Gerar um JWT_SECRET forte

```powershell
# PowerShell — gera 48 bytes base64 (64 caracteres)
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Max 256 }))
```

Ou em Linux/macOS:
```bash
openssl rand -base64 48
```

Guarde o valor no seu cofre de senhas institucional.

### 5.2 Definir as variáveis

#### Windows (prompt administrativo)

```powershell
[System.Environment]::SetEnvironmentVariable('DATASOURCE_PASSWORD', 'senha_nova_postgres', 'Machine')
[System.Environment]::SetEnvironmentVariable('JWT_SECRET', 'segredo_gerado_acima', 'Machine')
[System.Environment]::SetEnvironmentVariable('SPRING_PROFILES_ACTIVE', 'mobile,prod', 'Machine')
```

Reinicie a sessão ou o serviço depois disso.

#### Linux (systemd unit)

No arquivo `/etc/systemd/system/sihcp-server.service`:

```ini
[Service]
Environment="DATASOURCE_PASSWORD=senha_nova_postgres"
Environment="JWT_SECRET=segredo_gerado_acima"
Environment="SPRING_PROFILES_ACTIVE=mobile,prod"
ExecStart=/usr/bin/java -jar -Xms512m -Xmx2g /opt/sihcp/sihcp-server-2.7.0.jar
```

#### Docker

Nunca hardcode no Dockerfile. Use `docker run -e` ou `docker-compose.yml` com `env_file` apontando para um `.env` fora do Git.

### 5.3 Rotação de segredos

Rotacione o `JWT_SECRET` periodicamente (sugestão: a cada 6 meses) e imediatamente após qualquer suspeita de vazamento.

**Impacto**: trocar o `JWT_SECRET` invalida todos os tokens ativos. Todos os usuários do app serão deslogados no próximo request e precisarão refazer login. Comunique janela de manutenção com antecedência.

Procedimento:
1. Gere novo `JWT_SECRET` (ver 5.1)
2. Armazene no cofre
3. Defina a nova variável de ambiente
4. Reinicie o serviço
5. Confirme no log: `SecretsBootstrap OK — DATASOURCE_PASSWORD e JWT_SECRET carregados do ambiente`

---

## 6. Banco de dados PostgreSQL

### 6.1 Criação inicial

```sql
CREATE DATABASE sispatrimonio;
CREATE USER inventario WITH PASSWORD 'senha_forte_aqui';
GRANT ALL PRIVILEGES ON DATABASE sispatrimonio TO inventario;
```

### 6.2 Scripts de estrutura

Os scripts SQL estão em `sql/` na raiz do projeto. Execute na ordem:

```powershell
psql -h localhost -U inventario -d sispatrimonio -f sql\criar_tabelas_sispatrimonio.sql
psql -h localhost -U inventario -d sispatrimonio -f sql\criar_tabela_usuario.sql
psql -h localhost -U inventario -d sispatrimonio -f sql\criar_tabelas_sistema_inventario.sql
psql -h localhost -U inventario -d sispatrimonio -f sql\criar_tabelas_mobile.sql
```

Scripts complementares (perfis, triggers, views) estão na mesma pasta.

### 6.3 Tabelas principais

| Tabela | Conteúdo | Crescimento |
|---|---|---|
| `tabela_patrimonio` | Cadastro de todos os bens (origem: SUAP) | Estático/baixo |
| `tabela_sala` | Salas dos campi | Estático |
| `tabela_setor` | Setores | Estático |
| `tabela_responsavel` | Responsáveis por patrimônio | Baixo |
| `tabela_inventario` | Campanhas de inventário | Uma por ano |
| `tabela_participante_inventario` | Quem participa de cada inventário | Dezenas por inventário |
| `tabela_coleta` | **Coletas feitas em campo** | Cresce rápido durante inventário |
| `tabela_usuario` | Contas do sistema | Baixo |
| `tabela_coletor` | Metadados dos coletores | Baixo |

Consulte `database-verification.md` em `.kiro/steering/` para comandos de verificação via MCP PostgreSQL.

### 6.4 Manutenção

```sql
-- Estatísticas
VACUUM ANALYZE;

-- Reindexação (trimestral)
REINDEX DATABASE sispatrimonio;

-- Tamanho do banco
SELECT pg_size_pretty(pg_database_size('sispatrimonio'));

-- Coletas pendentes de sincronização (debug)
SELECT COUNT(*) FROM tabela_coleta WHERE sincronizado = false;
```

---

## 7. Perfis Spring e variáveis de ambiente

### Perfis disponíveis

| Perfil | Arquivo | Uso |
|---|---|---|
| `mobile` | `application-mobile.properties` | Perfil base — ativa a API Mobile |
| `prod` | `application-prod.properties` | Endurece logs, rate limit, JWT curto |
| `test` | `application-test.properties` | Testes unitários com SQLite |

Em produção sempre ative `mobile,prod`. Em dev local pode ser só `mobile`.

### Variáveis de ambiente suportadas

| Variável | Obrigatória? | Default | Descrição |
|---|---|---|---|
| `DATASOURCE_PASSWORD` | **Sim** | — | Senha do PostgreSQL |
| `JWT_SECRET` | **Sim** | — | Chave HMAC-SHA256 (mín. 32 chars) |
| `SPRING_PROFILES_ACTIVE` | Recomendada | `default` | Deve ser `mobile,prod` em produção |
| `JWT_EXPIRATION` | Não | 3600 (prod) / 172800 (dev) | Validade do access token, em segundos |
| `JWT_REFRESH_EXPIRATION` | Não | 604800 (7 dias) | Validade do refresh token |
| `API_MOBILE_CORS_ALLOWED_ORIGINS` | Não | lista interna IFMT | Origens permitidas (CSV) |

### Arquivo local para dev

Para desenvolvimento local sem definir variáveis de ambiente, crie `src/main/resources/application-mobile-local.properties` (já ignorado pelo Git):

```properties
spring.datasource.password=sua_senha_dev
jwt.secret=coloque-aqui-um-segredo-de-pelo-menos-32-caracteres-forte
```

E rode com `--spring.profiles.active=mobile,local`.

---

## 8. Inicialização em produção

### 8.1 Comando completo

```bash
java -jar \
  -Xms512m -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/sihcp/logs/heapdump.hprof \
  /opt/sihcp/sihcp-server-2.7.0.jar \
  --spring.profiles.active=mobile,prod
```

### 8.2 O que esperar no log de sucesso

```
INFO  SecretsBootstrap: SecretsBootstrap OK — DATASOURCE_PASSWORD e JWT_SECRET carregados do ambiente
INFO  MobileSecurityConfig: MOBILE SECURITY CONFIG v2.0 - SEGURANÇA POR ROLES ATIVA
INFO  MobileApiApplication: Started MobileApiApplication in X.XXX seconds
INFO  Tomcat: Tomcat started on port(s): 8081 (http) with context path '/inventario'
```

Se não aparecer a linha `SecretsBootstrap OK`, algo está errado. O servidor aborta antes de subir o Tomcat.

### 8.3 Teste de saúde

```powershell
curl http://localhost:8081/inventario/api/mobile/health
# Resposta esperada: {"success":true,"message":"Servidor disponível"...}
```

### 8.4 Teste de autenticação

```powershell
curl -X POST http://localhost:8081/inventario/api/mobile/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"senha_admin"}'
```

Resposta de sucesso inclui `accessToken`, `refreshToken`, `expiresIn` e dados do usuário. Rate limit ativo após 10 tentativas inválidas no mesmo IP em 1 minuto (retorna HTTP 429 com `Retry-After`).

---

## 9. Gestão de usuários e perfis

### Hierarquia de perfis

| Perfil | Nível | Pode fazer |
|---|---|---|
| **ADMIN** | 4 | Gerenciar usuários, excluir coletas, configurações |
| **SUPERVISOR** | 3 | Listar todos inventários, relatórios gerenciais, supervisionar coletas |
| **COLETOR** | 2 | Registrar coletas, sincronizar, buscar pendentes |
| **CONSULTA** | 1 | Apenas visualização (dashboard, consultar patrimônios) |

Cada perfil superior herda as permissões do inferior.

### Criar um usuário

Usuários são criados exclusivamente no **aplicativo Desktop** (módulo Swing). O servidor não expõe endpoint para criação pública. Ver [MANUAL_OPERADOR.md](MANUAL_OPERADOR.md) seção sobre gestão de usuários.

Alternativa via SQL direto (emergencial):

```sql
INSERT INTO tabela_usuario (login, senha, nome, email, perfil, ativo)
VALUES (
  'novo.usuario',
  crypt('senha_inicial', gen_salt('bf')),  -- BCrypt
  'Nome Completo',
  'email@ifmt.edu.br',
  'COLETOR',
  true
);
```

O campo `senha` armazena o hash BCrypt. Nunca armazene senha em texto.

### Desativar um usuário

Não apague — desative:
```sql
UPDATE tabela_usuario SET ativo = false WHERE login = 'usuario.antigo';
```

---

## 10. Monitoramento e logs

### 10.1 Localização dos logs

```
/opt/sihcp/logs/mobile-server.log          (atual)
/opt/sihcp/logs/mobile-server-YYYY-MM-DD.log.gz   (rotacionados)
```

Rotação automática: 5 MB por arquivo, máximo 3 arquivos (configurado em `application-mobile.properties`).

### 10.2 Nível de log

No perfil `prod`:
- `root`: WARN
- `com.inventario`: INFO
- `org.springframework.security`: WARN

Tokens JWT **não aparecem em logs INFO ou superior** (implementado em `MobileJwtAuthenticationFilter` e `MobileAuthController`).

Para debugar um problema em produção:
- Altere temporariamente via `JAVA_TOOL_OPTIONS` ou re-exporte `SPRING_PROFILES_ACTIVE=mobile` (sem prod) e reinicie
- Ou use `management.endpoints.web.exposure.include=loggers` e mude via endpoint Actuator (requer configuração adicional)

### 10.3 Alertas importantes a monitorar

| Padrão no log | Significado | Ação |
|---|---|---|
| `RATE_LIMIT_ALERT ip=... excess_events_last_5min=3+` | Possível ataque de força bruta | Investigar IP, considerar bloqueio no firewall |
| `FALHA DE BOOTSTRAP` | Servidor não subiu | Conferir variáveis de ambiente |
| `Failed to load driver class` | Problema no classpath ou config | Verificar `application.properties` |
| `OutOfMemoryError` | Memória insuficiente | Aumentar `-Xmx` ou investigar memory leak |
| `HikariPool-1 - Connection is not available` | Pool de conexões esgotado | Aumentar `hikari.maximum-pool-size` ou investigar leaks |

### 10.4 Health check

Spring Boot Actuator está parcialmente habilitado (só `/health`):

```
GET http://servidor:8081/inventario/actuator/health
```

Resposta `{"status":"UP"}` em operação normal.

### 10.5 Métricas customizadas

Para produção séria, considere adicionar Prometheus/Grafana ou integrar com ferramenta corporativa do IFMT. O stack atual não coleta métricas de negócio automaticamente — isso pode ser adicionado.

---

## 11. Atualizações e deploy

### 11.1 Estratégia de deploy

Atualizações do servidor são **destrutivas para sessões ativas**. Planeje janela de manutenção.

```
1. Anuncie janela aos usuários
2. Pare o serviço
3. Faça backup do JAR antigo e do banco
4. Copie o novo JAR para o diretório de produção
5. Atualize arquivos de configuração (se mudaram)
6. Suba o serviço
7. Monitore logs por 15-30 min
8. Se tudo ok, libere para uso
```

### 11.2 Migrações de schema

Quando uma nova versão exige mudança no banco, há script SQL em `sql/` com nome `migrar_vX_para_vY.sql`. Execute **antes** de subir a nova versão da aplicação.

### 11.3 Rollback

Sempre mantenha o JAR anterior. Em caso de problema:

```bash
# Parar serviço
systemctl stop sihcp-server

# Substituir JAR
cp /opt/sihcp/backup/sihcp-server-2.6.0.jar /opt/sihcp/sihcp-server.jar

# Subir de novo
systemctl start sihcp-server
```

Se houve migração de schema, será necessário rollback também do banco — mantenha backup pré-deploy.

---

## 12. Backup e restauração

### 12.1 Backup do PostgreSQL

Backup completo diário:

```powershell
pg_dump -h localhost -U inventario -d sispatrimonio -F c -f C:\SIHCP-BACKUPS\sispatrimonio_2026-05-08.backup
```

Agende via Task Scheduler (Windows) ou cron (Linux):

```bash
# /etc/cron.d/sihcp-backup
0 2 * * * postgres pg_dump -h localhost -U inventario -F c -d sispatrimonio -f /var/backups/sihcp/sispatrimonio_$(date +\%Y-\%m-\%d).backup
```

Retenção sugerida: 30 dias diários, 12 meses mensais.

### 12.2 Restauração

```powershell
# Parar servidor primeiro
systemctl stop sihcp-server

# Recriar banco
psql -U postgres -c "DROP DATABASE sispatrimonio;"
psql -U postgres -c "CREATE DATABASE sispatrimonio OWNER inventario;"

# Restaurar
pg_restore -h localhost -U inventario -d sispatrimonio -F c C:\SIHCP-BACKUPS\sispatrimonio_2026-05-07.backup

# Subir servidor
systemctl start sihcp-server
```

### 12.3 Teste de restauração

Faça teste de restauração em ambiente separado **pelo menos uma vez por trimestre**. Backup que nunca foi testado é backup que pode não funcionar.

---

## 13. Build e distribuição do APK

### 13.1 Pré-requisitos

- JDK 21 no PATH
- Android SDK (via Android Studio)
- `InventarioMobile/keystore.properties` configurado localmente (NÃO versionado)
- Arquivo `.keystore` referenciado pelo `keystore.properties`

### 13.2 Keystore

O keystore assina digitalmente o APK. Sem a mesma assinatura, usuários não conseguem atualizar sobre a versão antiga.

**Arquivo**: `InventarioMobile/inventario-release.keystore`
**Senha**: armazenada em `InventarioMobile/keystore.properties` (arquivo local, fora do Git)

⚠️ **Guarde o keystore em múltiplos locais seguros.** Se perder, não conseguirá mais atualizar o app para usuários existentes — eles teriam que desinstalar e reinstalar.

### 13.3 Gerar novo APK release

```powershell
cd C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO\InventarioMobile
.\gradlew.bat assembleRelease
```

Tempo esperado: 3-5 minutos (ProGuard/R8 ativo). APK gerado em:
```
InventarioMobile\app\build\outputs\apk\release\SiHCP-release-vX.X.X.apk
```

### 13.4 Verificar APK

Antes de distribuir:

```powershell
# Verificar assinatura
jarsigner -verify -verbose -certs caminho\para\app.apk

# Verificar arquitetura
aapt dump badging caminho\para\app.apk | Select-String "package|versionCode|versionName"
```

### 13.5 Distribuição

- Canais: intranet do IFMT, portal institucional, e-mail com link
- Não use Google Play (app interno)
- Forneça hash SHA-256 para usuários verificarem integridade
- Registre cada build em `docs/historico_e_reunioes/BUILD_HISTORY.md`

### 13.6 Compatibilidade entre app e servidor

O app se adapta a mudanças de endpoints se e somente se forem adições. Mudanças **quebradoras** exigem coordenação:

| Mudança no servidor | Impacto no app | Solução |
|---|---|---|
| Novo endpoint | App mais antigo ignora | Ok, adicionar na próxima versão do app |
| Novo campo opcional em resposta | App mais antigo ignora | Ok |
| Novo campo obrigatório em request | App quebra | Atualize o app primeiro |
| Remover endpoint | App quebra | Mantenha endpoint antigo por 2 versões |
| Alterar URL de endpoint | App quebra | **Nunca faça** — regra do projeto |

Consulte `.kiro/steering/endpoints-nao-alterar.md` para detalhes.

---

## 14. Procedimentos de segurança

Esta seção documenta os controles de segurança implementados na versão 2.7.0 e como operá-los.

### 14.1 Autenticação e autorização

- **BCrypt** para armazenamento de senhas (factor 10)
- **JWT** com HMAC-SHA256 para tokens
- **Access token** válido por 1 hora em produção (configurável)
- **Refresh token** válido por 7 dias
- **RBAC** com 4 perfis via `@PreAuthorize` em controllers

### 14.2 Rate limiting

- Aplicado exclusivamente ao `POST /api/mobile/auth/login`
- Limite: **10 tentativas por IP em 1 minuto**
- Resposta em excesso: HTTP 429 com header `Retry-After`
- Alerta no log após 3 excessos em 5 minutos (padrão `RATE_LIMIT_ALERT`)

Para ajustar, edite `LoginRateLimiter.java`. Desabilitar só em dev: `api.mobile.rate-limit.enabled=false` no `application-mobile.properties`.

### 14.3 CORS

- Lista explícita de origens em `MobileSecurityConfig.corsConfigurationSource()`
- `allow-credentials=false`
- Em produção, defina `API_MOBILE_CORS_ALLOWED_ORIGINS` via variável de ambiente

### 14.4 Network Security

O app Android não aceita HTTP em endereços fora da allowlist IFMT. Se o servidor mudar de IP, o APK precisa ser atualizado.

IPs atualmente liberados em `network_security_config.xml`:
- `localhost`, `127.0.0.1`, `10.0.2.2` (dev)
- `10.14.250.228`, `10.14.250.236`, `10.14.250.238`
- `192.168.10.107`, `192.168.11.136`

Para produção real, configure HTTPS com certificado válido e remova cleartext da allowlist.

### 14.5 Logs de segurança

O servidor **não loga tokens** em INFO ou superior. Logs relevantes de segurança:

```
RATE_LIMIT_ALERT ip=X.X.X.X excess_events_last_5min=3
Login bem-sucedido: usuario=joao expiresIn=3600s
Falha de autenticação mobile: usuario=X motivo=Bad credentials
```

### 14.6 Controle de acesso ao servidor

- Firewall limita acesso à porta 8081 às subnets dos coletores
- SSH ao servidor usa chaves, nunca senha
- Usuários com acesso ao servidor: apenas TI do IFMT
- Logs de acesso ao SO devem ir para auditoria centralizada

### 14.7 Auditoria

A tabela `tabela_historico` no PostgreSQL armazena alterações em patrimônios (origem: triggers configurados em `sql/triggers_historico_patrimonio.sql`).

Para pesquisa rápida:
```sql
SELECT * FROM tabela_historico
WHERE data_alteracao > NOW() - INTERVAL '7 days'
ORDER BY data_alteracao DESC;
```

---

## 15. Plano de contingência

### 15.1 Servidor fora do ar

Sintomas: coletores reportam "sem conexão", `/health` não responde.

1. Verifique processo: `ps aux | grep java` ou Task Manager
2. Verifique logs: últimas 100 linhas do `mobile-server.log`
3. Se crashed: reinicie e monitore
4. Se não subir: ver seção 8.2 (bootstrap de segredos) e 10.3 (alertas)

Enquanto resolve, os coletores continuam trabalhando **offline**. Não há perda de dados desde que o banco do app esteja íntegro.

### 15.2 Banco fora do ar

Sintomas: servidor sobe mas endpoints de dados retornam 500.

1. `pg_isready -h localhost`
2. Logs do PostgreSQL (padrão: `C:\Program Files\PostgreSQL\XX\data\log\`)
3. Disco cheio? `df -h` ou propriedades da unidade
4. Se necessário, reinicie PostgreSQL

### 15.3 Senha comprometida

Se suspeitar que senha do banco ou JWT_SECRET vazou:

1. **Não hesite** — rotacione imediatamente
2. Gere nova senha/segredo (seção 5)
3. Atualize variáveis de ambiente
4. Reinicie servidor
5. Se foi o `JWT_SECRET`, todos os usuários serão deslogados (esperado)
6. Investigue origem do vazamento

### 15.4 APK comprometido

Se suspeitar que o keystore foi comprometido:

1. **Gere novo keystore** (obrigatoriamente com nova identidade)
2. Atualize `keystore.properties`
3. Anuncie aos usuários: "versão de segurança; desinstale e reinstale"
4. Distribua novo APK
5. Usuários perderão dados offline (sincronizem antes)

### 15.5 Dados corrompidos no banco

1. Pare o servidor
2. Faça backup do estado corrompido (para forense)
3. Restaure do backup mais recente válido (seção 12.2)
4. Suba servidor
5. Comunique janela aos usuários — coletas não sincronizadas que entraram após o backup serão perdidas

---

## 16. Checklist operacional

### Diário

- [ ] Verificar health check (`/actuator/health` retorna UP)
- [ ] Revisar alertas no log (`RATE_LIMIT_ALERT`, `ERROR`, `OOM`)
- [ ] Confirmar backup noturno executado

### Semanal

- [ ] Tamanho do banco (`pg_database_size`)
- [ ] Uso de disco do servidor
- [ ] Número de coletas pendentes muito antigas (> 7 dias)
  ```sql
  SELECT COUNT(*) FROM tabela_coleta WHERE sincronizado = false AND data_coleta < NOW() - INTERVAL '7 days';
  ```
- [ ] Testar login de coletor pelo app

### Mensal

- [ ] Revisar usuários ativos vs. inativos
- [ ] Rotacionar logs antigos
- [ ] Teste de restauração de backup (ambiente paralelo)
- [ ] Atualização de patches do SO

### Trimestral

- [ ] `VACUUM ANALYZE` no banco
- [ ] `REINDEX` no banco
- [ ] Revisão das regras de firewall
- [ ] Auditoria de quem tem acesso ao servidor e ao banco

### Semestral

- [ ] Rotação do `JWT_SECRET` (com janela de manutenção)
- [ ] Revisão de dependências (atualizações de Spring Boot e PostgreSQL)
- [ ] Teste completo de plano de contingência

---

## Referências internas

- Código do servidor: `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/`
- Configurações: `sihcp-server/src/main/resources/application-*.properties`
- Steering rules: `.kiro/steering/`
  - `product.md` — descrição do produto
  - `tech.md` — stack técnica
  - `structure.md` — estrutura do projeto
  - `security-roles.md` — matriz RBAC completa
  - `endpoints-nao-alterar.md` — regras de compatibilidade de API
  - `build-tracking.md` — rastreamento de builds
- Especificações: `.kiro/specs/correcoes-seguranca/` — Documentação completa das correções de segurança implementadas em 2.21.0

---

**Última atualização**: maio/2026 (após implementação da spec `correcoes-seguranca`).
