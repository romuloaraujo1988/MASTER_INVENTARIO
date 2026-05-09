# Instalação Manual do SIHCP

Este guia cobre a instalação completa do SIHCP passo a passo, sem uso de scripts automatizados.

---

## Passo 1 — Instalar o JDK 21

O SIHCP requer o Java Development Kit (JDK) versão 21 (LTS).

1. Acesse [https://adoptium.net](https://adoptium.net)
2. Selecione **Temurin 21 (LTS)**
3. Escolha o instalador para o seu sistema operacional (Windows `.msi` ou Linux `.tar.gz`)
4. Execute o instalador e siga as instruções

**Verificar a instalação:**

```bash
java -version
```

A saída deve mostrar algo como:
```
openjdk version "21.x.x" ...
```

> **Windows:** O instalador do Adoptium adiciona o Java ao PATH automaticamente. Se não funcionar, reinicie o terminal.

> **Linux:** Adicione ao `~/.bashrc` ou `~/.profile`:
> ```bash
> export JAVA_HOME=/opt/jdk-21
> export PATH=$JAVA_HOME/bin:$PATH
> ```

---

## Passo 2 — Instalar o PostgreSQL 12+

O SIHCP requer PostgreSQL versão 12 ou superior.

1. Acesse [https://www.postgresql.org/download/](https://www.postgresql.org/download/)
2. Selecione o seu sistema operacional
3. Baixe e execute o instalador
4. Durante a instalação, defina uma senha para o usuário `postgres`
5. Mantenha a porta padrão **5432**

**Verificar a instalação:**

```bash
psql --version
```

A saída deve mostrar algo como:
```
psql (PostgreSQL) 16.x
```

> **Windows:** O instalador do PostgreSQL inclui o pgAdmin e adiciona `psql` ao PATH. Se `psql` não for encontrado, adicione `C:\Program Files\PostgreSQL\<versao>\bin` ao PATH do sistema.

> **Linux (Ubuntu/Debian):**
> ```bash
> sudo apt install postgresql postgresql-contrib
> sudo systemctl enable postgresql
> sudo systemctl start postgresql
> ```

---

## Passo 3 — Criar o banco de dados

Abra o terminal e execute:

```bash
createdb sispatrimonio
```

Ou, se precisar especificar o usuário:

```bash
createdb -U postgres sispatrimonio
```

**Verificar a criação:**

```bash
psql -U postgres -l
```

O banco `sispatrimonio` deve aparecer na lista.

---

## Passo 4 — Executar o SQL de setup

O arquivo `sql/setup_banco_completo.sql` cria todas as tabelas, views, triggers e dados iniciais necessários.

```bash
psql -d sispatrimonio -f sql/setup_banco_completo.sql
```

Se precisar especificar o usuário e host:

```bash
psql -h localhost -U postgres -d sispatrimonio -f sql/setup_banco_completo.sql
```

> **Nota:** O script é idempotente — pode ser executado mais de uma vez sem causar erros ou duplicar dados.

**Verificar a execução:**

```bash
psql -d sispatrimonio -c "\dt"
```

Deve listar as tabelas criadas (ex.: `tabela_patrimonio`, `tabela_usuario`, `tabela_coleta`, etc.).

---

## Passo 5 — Criar o arquivo de configuração

Crie o arquivo `config/configuracao_banco.json` com as configurações do seu campus:

```json
{
  "campus": {
    "nome": "IFMT - Campus Nome",
    "sigla": "NOM",
    "cidade": "Cidade",
    "estado": "MT",
    "responsavel_tecnico": "Nome do Responsável",
    "contato": "responsavel@ifmt.edu.br"
  },
  "postgresql": {
    "host": "localhost",
    "port": 5432,
    "database": "sispatrimonio",
    "user": "postgres",
    "password": "sua_senha_aqui"
  },
  "api": {
    "porta": 8080,
    "versao": "2.7.0"
  }
}
```

**Campos obrigatórios:**
- `campus.nome` — Nome do campus (máximo 100 caracteres)
- `postgresql.host` — Endereço do servidor PostgreSQL (use `localhost` se local)
- `postgresql.port` — Porta do PostgreSQL (padrão: `5432`)
- `postgresql.database` — Nome do banco (padrão: `sispatrimonio`)
- `postgresql.user` — Usuário do PostgreSQL
- `postgresql.password` — Senha do usuário PostgreSQL
- `api.porta` — Porta da API Mobile (padrão: `8080`, intervalo: 1024–65535)

> **Segurança:** Mantenha este arquivo protegido. Ele contém a senha do banco de dados.

---

## Passo 6 — Iniciar o servidor

### Windows

```bat
scripts\iniciar-servidor.bat
```

### Linux / macOS

```bash
bash scripts/iniciar-servidor.sh
```

O script verifica automaticamente:
- Se o JDK 21 está instalado
- Se o arquivo `config/configuracao_banco.json` existe
- Se o banco de dados está acessível
- Se a porta da API está disponível

Em caso de sucesso, você verá:
```
Iniciando SIHCP Mobile Server...
Started MobileApiApplication in X.XXX seconds
```

---

## Passo 7 — Verificar a saúde do sistema

### Windows

```powershell
scripts\verificar-saude.ps1
```

### Linux / macOS

```bash
bash scripts/verificar-saude.sh
```

O verificador executa 4 testes em sequência:

| Teste | O que verifica |
|-------|----------------|
| 1 | Conectividade TCP com o banco de dados |
| 2 | Resposta do endpoint `GET /api/mobile/health` |
| 3 | Autenticação com credenciais do admin |
| 4 | Listagem de patrimônios via `GET /api/mobile/patrimonio` |

Se todos os testes passarem:
```
✅ Sistema SIHCP operacional e pronto para uso
   URL da API: http://<ip>:<porta>
```

---

## Próximos passos

Com o sistema operacional, consulte:

- **[CHECKLIST_IMPLANTACAO.md](CHECKLIST_IMPLANTACAO.md)** — Verifique todos os itens pós-instalação
- **[CONFIGURAR_APP_ANDROID.md](CONFIGURAR_APP_ANDROID.md)** — Distribua o APK aos coletores
- **[SOLUCAO_PROBLEMAS.md](SOLUCAO_PROBLEMAS.md)** — Resolva problemas comuns
