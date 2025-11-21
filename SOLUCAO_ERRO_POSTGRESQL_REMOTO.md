# Solução Rápida - Erro PostgreSQL Remoto

## ❌ Erro Encontrado

```
FATAL: nenhuma entrada em pg_hba.conf para o hospedeiro "10.14.250.199", 
usuário "postgres", banco de dados "sispatrimonio", sem encriptação
```

---

## ✅ Solução Rápida (5 Passos)

### 1️⃣ Editar pg_hba.conf

**Localização:**
- Windows: `C:\Program Files\PostgreSQL\{versão}\data\pg_hba.conf`
- Linux: `/etc/postgresql/{versão}/main/pg_hba.conf`

**Adicionar no final do arquivo:**
```conf
# Permitir conexão remota
host    all    all    10.14.250.199/32    md5
```

### 2️⃣ Editar postgresql.conf

**Localização:** Mesma pasta do pg_hba.conf

**Alterar a linha:**
```conf
listen_addresses = '*'
```

### 3️⃣ Reiniciar PostgreSQL

**Windows:**
```cmd
net stop postgresql-x64-{versão}
net start postgresql-x64-{versão}
```

**Linux:**
```bash
sudo systemctl restart postgresql
```

### 4️⃣ Abrir Firewall

**Windows:**
```cmd
netsh advfirewall firewall add rule name="PostgreSQL" dir=in action=allow protocol=TCP localport=5432
```

**Linux:**
```bash
sudo ufw allow 5432/tcp
```

### 5️⃣ Testar Conexão

Execute o script:
```cmd
diagnostico-postgresql.ps1
```

Ou teste manualmente:
```cmd
psql -h {IP_SERVIDOR} -U postgres -d sispatrimonio
```

---

## 🔧 Ferramentas Disponíveis

### Scripts de Diagnóstico

1. **diagnostico-postgresql.ps1** (Recomendado)
   - Diagnóstico completo
   - Verifica porta, serviço, firewall
   - Testa conexão
   
   ```powershell
   .\diagnostico-postgresql.ps1 -Host "192.168.1.100" -Port 5432
   ```

2. **testar-conexao-postgresql.bat**
   - Teste simples de conexão
   - Usa psql
   
   ```cmd
   testar-conexao-postgresql.bat
   ```

### No Sistema SIHCP

1. Abrir o sistema
2. Clicar em **"⚙ Configurar Banco"**
3. Clicar em **"📖 Ajuda"** para ver o guia completo
4. Preencher os dados:
   - Servidor: IP do servidor PostgreSQL
   - Porta: 5432
   - Banco: sispatrimonio
   - Usuário: postgres
   - Senha: sua senha
5. Clicar em **"Testar Conexão"**
6. Se OK, clicar em **"Salvar"**

---

## 📚 Documentação Completa

Para instruções detalhadas, consulte:
- **GUIA_CONFIGURACAO_POSTGRESQL_REMOTO.md** - Guia completo passo a passo

---

## 🆘 Problemas Comuns

### "Connection refused"
- PostgreSQL não está rodando
- Firewall bloqueando
- Porta incorreta

### "Password authentication failed"
- Senha incorreta
- Usuário não existe

### "Database does not exist"
- Banco não foi criado
- Nome do banco incorreto

### "Timeout"
- Problemas de rede
- Firewall bloqueando
- Servidor lento

---

## 📞 Suporte

Se o problema persistir:
1. Execute `diagnostico-postgresql.ps1`
2. Copie o resultado
3. Consulte o administrador do servidor
4. Verifique logs: `{pasta_postgresql}\data\log\`

---

**Última atualização:** 18/11/2025
