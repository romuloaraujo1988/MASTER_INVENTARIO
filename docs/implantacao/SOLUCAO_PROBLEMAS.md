# Solução de Problemas — SIHCP

Este guia cobre os erros mais comuns durante a implantação e operação do SIHCP. Use os links abaixo para ir diretamente ao problema:

- [Falha de conexão com banco de dados](#banco)
- [Porta em uso](#porta)
- [JDK não encontrado](#jdk)
- [APK não instalando no Android](#apk)
- [API não respondendo](#api)
- [Falha de autenticação](#autenticacao)

---

<a name="banco"></a>
## Falha de Conexão com Banco de Dados

**Sintoma:** O script de inicialização exibe `"Banco de dados inacessível. Verifique as configurações em configuracao_banco.json"` ou a API não inicia.

### Diagnóstico

1. Verifique se o PostgreSQL está em execução:

   **Windows:**
   ```powershell
   Get-Service -Name postgresql*
   ```

   **Linux:**
   ```bash
   systemctl status postgresql
   # ou
   pg_isready
   ```

2. Verifique se as credenciais no `config/configuracao_banco.json` estão corretas:
   - `host` — endereço do servidor PostgreSQL (use `localhost` se local)
   - `port` — porta do PostgreSQL (padrão: `5432`)
   - `database` — nome do banco (padrão: `sispatrimonio`)
   - `user` — usuário do PostgreSQL
   - `password` — senha do usuário

3. Teste a conexão manualmente:
   ```bash
   psql -h <host> -p <porta> -U <usuario> -d <banco>
   ```

### Soluções

**PostgreSQL não está em execução:**

```bash
# Linux
sudo systemctl start postgresql

# Windows (PowerShell como Administrador)
Start-Service -Name postgresql-x64-16
```

**Credenciais incorretas:**

Edite o arquivo `config/configuracao_banco.json` e corrija os valores. Depois reinicie o servidor.

**Banco não existe:**

```bash
createdb -U postgres sispatrimonio
psql -d sispatrimonio -f sql/setup_banco_completo.sql
```

**Firewall bloqueando a porta 5432:**

```bash
# Linux (UFW)
sudo ufw allow 5432/tcp

# Windows (PowerShell como Administrador)
New-NetFirewallRule -DisplayName "PostgreSQL" -Direction Inbound -Protocol TCP -LocalPort 5432 -Action Allow
```

---

<a name="porta"></a>
## Porta em Uso

**Sintoma:** O script de inicialização exibe `"Porta X já está em uso"` ou a API não inicia.

### Diagnóstico

Verifique qual processo está usando a porta:

**Windows:**
```powershell
netstat -ano | findstr :<porta>
# Anote o PID e verifique o processo:
Get-Process -Id <PID>
```

**Linux:**
```bash
ss -tlnp | grep :<porta>
# ou
lsof -i :<porta>
```

### Soluções

**Opção 1 — Encerrar o processo que usa a porta:**

```powershell
# Windows (substitua <PID> pelo número encontrado)
Stop-Process -Id <PID> -Force
```

```bash
# Linux
kill -9 <PID>
```

**Opção 2 — Alterar a porta da API:**

Edite `config/configuracao_banco.json` e altere o valor de `api.porta` para uma porta disponível (ex.: `8081`). Depois reinicie o servidor.

> **Lembre-se:** Se alterar a porta, atualize também o endereço configurado no app Android e regenere o QR Code.

---

<a name="jdk"></a>
## JDK Não Encontrado

**Sintoma:** O script exibe `"JDK 21 não encontrado. Instale em: https://adoptium.net"` ou `java: command not found`.

### Diagnóstico

```bash
java -version
echo $JAVA_HOME   # Linux/macOS
echo %JAVA_HOME%  # Windows
```

### Soluções

**JDK não instalado:**

1. Acesse [https://adoptium.net](https://adoptium.net)
2. Baixe o **Temurin 21 (LTS)** para o seu sistema operacional
3. Execute o instalador

**JDK instalado mas não no PATH:**

**Windows:**
1. Abra **Painel de Controle** → **Sistema** → **Configurações avançadas do sistema**
2. Clique em **Variáveis de Ambiente**
3. Em **Variáveis do sistema**, edite `Path` e adicione `C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot\bin`
4. Crie a variável `JAVA_HOME` com o valor `C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot`
5. Reinicie o terminal

**Linux / macOS:**
```bash
# Adicione ao ~/.bashrc ou ~/.profile
export JAVA_HOME=/usr/lib/jvm/temurin-21
export PATH=$JAVA_HOME/bin:$PATH

# Recarregue o perfil
source ~/.bashrc
```

**Versão incorreta do Java (não é a 21):**

O SIHCP requer especificamente o Java 21. Se você tem outra versão instalada, instale o JDK 21 e configure o `JAVA_HOME` para apontar para ele.

---

<a name="apk"></a>
## APK Não Instalando no Android

**Sintoma:** Mensagem de erro ao tentar instalar o `bin/sihcp-mobile.apk`.

### Diagnóstico e Soluções

**"Instalação bloqueada" ou "Fonte desconhecida":**

Habilite a instalação de fontes desconhecidas. Consulte [CONFIGURAR_APP_ANDROID.md](CONFIGURAR_APP_ANDROID.md) para instruções detalhadas por versão do Android.

**"Aplicativo não instalado" (erro genérico):**

- Verifique se há espaço suficiente no armazenamento (mínimo 100 MB livre)
- Desinstale versões anteriores do app antes de instalar a nova
- Verifique se o arquivo APK não está corrompido (tente baixar novamente)

**"Aplicativo incompatível com este dispositivo":**

- Verifique se o Android é versão 6.0 (API 23) ou superior
- Verifique se a arquitetura do processador é compatível (ARM64 ou ARM)

**"Análise do pacote falhou":**

O arquivo APK pode estar corrompido. Copie novamente o arquivo `bin/sihcp-mobile.apk` do pacote de implantação.

**Instalação trava ou demora muito:**

- Reinicie o dispositivo e tente novamente
- Limpe o cache do instalador de pacotes: **Configurações** → **Aplicativos** → **Instalador de pacotes** → **Limpar cache**

---

<a name="api"></a>
## API Não Respondendo

**Sintoma:** `curl http://localhost:<porta>/api/mobile/health` não retorna resposta, ou o verificador de saúde falha no teste da API.

### Diagnóstico

1. Verifique se o processo Java está em execução:

   **Windows:**
   ```powershell
   Get-Process -Name java
   ```

   **Linux:**
   ```bash
   ps aux | grep java
   ```

2. Verifique os logs da API:
   ```bash
   # Logs ficam em logs/ no diretório de instalação
   tail -n 100 logs/sistema-inventario-prod.log
   ```

3. Verifique se a porta está sendo escutada:

   **Windows:**
   ```powershell
   netstat -ano | findstr :<porta>
   ```

   **Linux:**
   ```bash
   ss -tlnp | grep :<porta>
   ```

### Soluções

**API não iniciou:**

Verifique os logs para identificar o erro. Causas comuns:
- Banco de dados inacessível (veja [#banco](#banco))
- Porta em uso (veja [#porta](#porta))
- JDK não encontrado (veja [#jdk](#jdk))
- Arquivo `config/configuracao_banco.json` ausente ou inválido

**API iniciou mas não responde:**

- Verifique se o firewall não está bloqueando a porta da API
- Verifique se está usando o endereço correto (IP local, não `localhost`, para acesso externo)

**Reiniciar a API:**

```bash
# Linux
bash scripts/iniciar-servidor.sh

# Windows
scripts\iniciar-servidor.bat
```

---

<a name="autenticacao"></a>
## Falha de Autenticação

**Sintoma:** Login com usuário `admin` retorna erro 401 ou "Credenciais inválidas".

### Diagnóstico

1. Verifique se o usuário `admin` foi criado durante o setup:
   ```bash
   psql -d sispatrimonio -c "SELECT login, ativo FROM tabela_usuario WHERE login = 'admin';"
   ```

2. Verifique se a API está respondendo (veja [#api](#api))

### Soluções

**Usuário `admin` não existe:**

O usuário deve ter sido criado durante o setup automatizado (Etapa 5/6). Se não foi criado, execute manualmente:

```sql
-- Conecte ao banco e execute:
psql -d sispatrimonio

-- Crie o usuário admin com senha BCrypt
-- Substitua 'HASH_BCRYPT_AQUI' pelo hash da senha desejada
INSERT INTO tabela_usuario (login, senha, nome, perfil, ativo)
VALUES ('admin', crypt('sua_senha', gen_salt('bf')), 'Administrador', 'ADMIN', true)
ON CONFLICT (login) DO NOTHING;
```

> **Nota:** O PostgreSQL deve ter a extensão `pgcrypto` habilitada (incluída no `setup_banco_completo.sql`).

**Senha incorreta:**

Redefina a senha do admin:

```sql
psql -d sispatrimonio -c "UPDATE tabela_usuario SET senha = crypt('nova_senha', gen_salt('bf')) WHERE login = 'admin';"
```

**Usuário inativo:**

```sql
psql -d sispatrimonio -c "UPDATE tabela_usuario SET ativo = true WHERE login = 'admin';"
```

**Token JWT expirado:**

Faça login novamente para obter um novo token. O token tem validade de 24 horas por padrão.

---

## Coletar Diagnóstico para Suporte

Se o problema persistir, colete informações de diagnóstico para enviar ao suporte:

**Windows:**
```powershell
scripts\coletar-diagnostico.ps1
```

**Linux / macOS:**
```bash
bash scripts/coletar-diagnostico.sh
```

O script gera um arquivo `diagnostico-<campus>-<data>.zip` com logs, versões de software e configurações anonimizadas (sem senhas).

---

## Contato com Suporte

Se não conseguir resolver o problema com este guia:

- **E-mail:** suporte@sihcp.ifmt.edu.br
- **Repositório:** https://github.com/ifmt/sihcp

Ao entrar em contato, inclua:
1. O arquivo de diagnóstico gerado pelo script `coletar-diagnostico`
2. Descrição detalhada do problema e quando ocorreu
3. Versão do SIHCP instalada (encontrada em `config/configuracao_banco.json`, campo `api.versao`)
4. Sistema operacional do servidor
