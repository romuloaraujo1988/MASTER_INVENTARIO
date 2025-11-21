# Guia de Configuração PostgreSQL para Acesso Remoto

## Problema Identificado

```
FATAL: nenhuma entrada em pg_hba.conf para o hospedeiro "10.14.250.199", 
usuário "postgres", banco de dados "sispatrimonio", sem encriptação
```

Este erro ocorre quando o PostgreSQL não está configurado para aceitar conexões do IP remoto.

---

## Solução Passo a Passo

### 1. Localizar os Arquivos de Configuração

Os arquivos estão geralmente em:
- **Windows**: `C:\Program Files\PostgreSQL\{versão}\data\`
- **Linux**: `/etc/postgresql/{versão}/main/` ou `/var/lib/pgsql/{versão}/data/`

Arquivos importantes:
- `pg_hba.conf` - Controla quem pode conectar
- `postgresql.conf` - Configurações gerais do servidor

---

### 2. Editar postgresql.conf

Abra o arquivo `postgresql.conf` e localize a linha:

```conf
#listen_addresses = 'localhost'
```

Altere para:

```conf
listen_addresses = '*'
```

Isso permite que o PostgreSQL aceite conexões de qualquer IP.

**Alternativa mais segura** (apenas IPs específicos):
```conf
listen_addresses = 'localhost,10.14.250.199'
```

---

### 3. Editar pg_hba.conf

Abra o arquivo `pg_hba.conf` e adicione as seguintes linhas **no final do arquivo**:

#### Opção 1: Permitir IP Específico (Mais Seguro)
```conf
# Permitir conexão do IP 10.14.250.199
host    sispatrimonio    postgres    10.14.250.199/32    md5
host    all              all         10.14.250.199/32    md5
```

#### Opção 2: Permitir Toda a Rede Local
```conf
# Permitir conexões da rede 10.14.250.0/24
host    sispatrimonio    postgres    10.14.250.0/24      md5
host    all              all         10.14.250.0/24      md5
```

#### Opção 3: Permitir Qualquer IP (Menos Seguro)
```conf
# Permitir conexões de qualquer IP (use com cuidado!)
host    all              all         0.0.0.0/0           md5
```

**Explicação dos campos:**
- `host` - Tipo de conexão (TCP/IP)
- `sispatrimonio` - Nome do banco (ou `all` para todos)
- `postgres` - Nome do usuário (ou `all` para todos)
- `10.14.250.199/32` - IP permitido (/32 = apenas este IP)
- `md5` - Método de autenticação (senha criptografada)

---

### 4. Reiniciar o PostgreSQL

#### Windows:
```cmd
# Via Serviços do Windows
services.msc
# Procure por "postgresql-x64-{versão}"
# Clique com botão direito > Reiniciar

# Ou via linha de comando (como Administrador)
net stop postgresql-x64-{versão}
net start postgresql-x64-{versão}
```

#### Linux:
```bash
# Ubuntu/Debian
sudo systemctl restart postgresql

# CentOS/RHEL
sudo systemctl restart postgresql-{versão}

# Ou
sudo service postgresql restart
```

---

### 5. Verificar Firewall

#### Windows Firewall:
```cmd
# Abrir porta 5432 (padrão PostgreSQL)
netsh advfirewall firewall add rule name="PostgreSQL" dir=in action=allow protocol=TCP localport=5432
```

#### Linux (UFW):
```bash
sudo ufw allow 5432/tcp
sudo ufw reload
```

#### Linux (firewalld):
```bash
sudo firewall-cmd --permanent --add-port=5432/tcp
sudo firewall-cmd --reload
```

---

### 6. Testar Conexão

#### Do Cliente (Máquina 10.14.250.199):
```bash
# Testar com psql
psql -h {IP_DO_SERVIDOR} -U postgres -d sispatrimonio

# Testar com telnet (verificar se porta está aberta)
telnet {IP_DO_SERVIDOR} 5432
```

#### Verificar se PostgreSQL está escutando:
```bash
# Linux
netstat -tuln | grep 5432

# Windows
netstat -an | findstr 5432
```

Deve mostrar algo como:
```
tcp        0      0 0.0.0.0:5432            0.0.0.0:*               LISTEN
```

---

## Exemplo Completo de pg_hba.conf

```conf
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# "local" is for Unix domain socket connections only
local   all             all                                     peer

# IPv4 local connections:
host    all             all             127.0.0.1/32            md5

# IPv6 local connections:
host    all             all             ::1/128                 md5

# ===== CONEXÕES REMOTAS =====

# Permitir IP específico do cliente
host    sispatrimonio   postgres        10.14.250.199/32        md5
host    all             all             10.14.250.199/32        md5

# Permitir toda a rede local (opcional)
host    all             all             10.14.250.0/24          md5

# Permitir qualquer IP (use apenas em desenvolvimento!)
# host    all             all             0.0.0.0/0               md5
```

---

## Troubleshooting

### Erro: "Connection refused"
- PostgreSQL não está rodando
- Firewall bloqueando a porta
- `listen_addresses` não configurado

**Solução:**
```bash
# Verificar se PostgreSQL está rodando
sudo systemctl status postgresql

# Verificar logs
tail -f /var/log/postgresql/postgresql-{versão}-main.log
```

### Erro: "FATAL: no pg_hba.conf entry"
- Configuração do `pg_hba.conf` incorreta
- Não reiniciou o PostgreSQL após mudanças

**Solução:**
- Verificar sintaxe do `pg_hba.conf`
- Reiniciar PostgreSQL

### Erro: "password authentication failed"
- Senha incorreta
- Usuário não existe

**Solução:**
```sql
-- Alterar senha do usuário
ALTER USER postgres WITH PASSWORD 'nova_senha';
```

---

## Segurança Recomendada

### 1. Usar SSL/TLS (Recomendado para Produção)
```conf
# pg_hba.conf
hostssl    all    all    10.14.250.0/24    md5
```

### 2. Criar Usuário Específico (Não usar postgres)
```sql
CREATE USER inventario_user WITH PASSWORD 'senha_forte';
GRANT ALL PRIVILEGES ON DATABASE sispatrimonio TO inventario_user;
```

### 3. Restringir IPs
- Use `/32` para IPs específicos
- Use `/24` apenas para redes confiáveis
- **NUNCA** use `0.0.0.0/0` em produção

### 4. Usar Senhas Fortes
- Mínimo 12 caracteres
- Letras maiúsculas e minúsculas
- Números e símbolos

---

## Checklist de Configuração

- [ ] Editar `postgresql.conf` → `listen_addresses = '*'`
- [ ] Editar `pg_hba.conf` → Adicionar regra para IP remoto
- [ ] Reiniciar PostgreSQL
- [ ] Abrir porta 5432 no firewall
- [ ] Testar conexão com `psql` ou `telnet`
- [ ] Configurar no sistema (botão "⚙ Configurar Banco")
- [ ] Testar conexão no sistema

---

## Configuração no Sistema SIHCP

1. Abrir o sistema
2. Clicar em "⚙ Configurar Banco"
3. Preencher:
   - **SGBD**: PostgreSQL
   - **Servidor**: IP do servidor (ex: 192.168.1.100)
   - **Porta**: 5432
   - **Nome do Banco**: sispatrimonio
   - **Usuário**: postgres (ou usuário criado)
   - **Senha**: senha do usuário
4. Clicar em "Testar Conexão"
5. Se OK, clicar em "Salvar"

---

**Última atualização:** 18/11/2025
