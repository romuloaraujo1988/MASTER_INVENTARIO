# Guia de Conexão do Smartphone ao Servidor

## Pré-requisitos

### No Computador (Servidor)
1. ✅ PostgreSQL rodando (porta 5432)
2. ✅ Java JDK 17+ instalado
3. ✅ Maven instalado (ou usar mvnw.cmd)
4. ✅ Porta 8081 livre

### No Smartphone
1. ✅ App instalado (app-debug.apk ou app-release.apk)
2. ✅ Conectado na mesma rede Wi-Fi do servidor
3. ✅ Permissões concedidas (câmera, localização, armazenamento)

## Passo a Passo

### 1. Iniciar o Servidor

```powershell
# Opção 1: Script automático (recomendado)
.\iniciar-servidor-mobile.ps1

# Opção 2: Maven direto
mvn spring-boot:run -Dspring-boot.run.profiles=mobile

# Opção 3: Maven Wrapper
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mobile
```

### 2. Verificar se o Servidor Iniciou

Aguarde até ver a mensagem:
```
Started InventarioApplication in X.XXX seconds
```

Ou acesse no navegador:
```
http://localhost:8081/inventario/api/mobile/health
```

### 3. Descobrir o IP do Computador

```powershell
# No PowerShell
ipconfig | findstr "IPv4"
```

Exemplo de resultado:
```
IPv4 Address. . . . . . . . . . . : 192.168.1.100
```

### 4. Configurar o App no Smartphone

1. Abra o app
2. Na tela de configuração, insira:
   - **URL do Servidor**: `http://192.168.1.100:8081/inventario`
   - (Substitua `192.168.1.100` pelo IP do seu computador)
3. Clique em "Testar Conexão"
4. Se aparecer "✓ Conexão OK", clique em "Salvar"

### 5. Fazer Login

Use as credenciais do sistema:
- **Usuário**: seu login do sistema
- **Senha**: sua senha do sistema

## Solução de Problemas

### Erro: "Porta 8081 já está em uso"

**Solução 1: Usar o script de gerenciamento**
```powershell
.\gerenciar-servidor.ps1 stop
.\gerenciar-servidor.ps1 start
```

**Solução 2: Encerrar processo manualmente**
```powershell
# Descobrir o PID
netstat -ano | findstr :8081

# Encerrar (substitua XXXX pelo PID)
taskkill /PID XXXX /F
```

### Erro: "jdbcUrl is required with driverClassName"

**Causa**: Configuração do banco de dados incorreta

**Solução**: Verificar se o PostgreSQL está rodando
```powershell
Get-Service -Name "*postgres*"
```

Se não estiver rodando, inicie o serviço:
```powershell
Start-Service postgresql-x64-17
```

### Erro: "Connection refused" no smartphone

**Possíveis causas**:

1. **Firewall bloqueando a porta 8081**
   ```powershell
   # Adicionar regra no firewall
   New-NetFirewallRule -DisplayName "Inventario Mobile API" -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow
   ```

2. **IP incorreto no app**
   - Verifique o IP do computador novamente
   - Certifique-se de usar o IP da rede local (192.168.x.x ou 10.0.x.x)
   - NÃO use 127.0.0.1 ou localhost

3. **Smartphone e computador em redes diferentes**
   - Conecte ambos na mesma rede Wi-Fi
   - Desative dados móveis no smartphone

4. **Servidor não iniciou completamente**
   - Aguarde mais tempo (pode levar 1-2 minutos)
   - Verifique os logs em `logs/sistema-inventario.log`

### Erro: "Timeout" ao conectar

**Solução**: Aumentar o timeout no app ou verificar a velocidade da rede

### Erro: "Unauthorized" ou "401"

**Causa**: Credenciais incorretas

**Solução**: 
- Verifique usuário e senha
- Certifique-se de que o usuário existe no banco de dados
- Verifique se o usuário está ativo

## Testar a API Manualmente

### No navegador do computador:

```
http://localhost:8081/inventario/api/mobile/health
```

Deve retornar:
```json
{
  "status": "UP"
}
```

### Testar login (usando curl ou Postman):

```bash
curl -X POST http://localhost:8081/inventario/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

## Endpoints Disponíveis

- `POST /api/mobile/auth/login` - Login
- `GET /api/mobile/patrimonios` - Listar patrimônios
- `GET /api/mobile/setores` - Listar setores
- `GET /api/mobile/salas` - Listar salas
- `POST /api/mobile/coletas` - Registrar coleta
- `GET /api/mobile/sync/status` - Status de sincronização

## Configuração de Rede

### Descobrir IP do Computador

**Windows:**
```powershell
ipconfig
```

Procure por "Adaptador de Rede sem Fio" ou "Ethernet" e anote o IPv4.

### Testar Conectividade

**Do smartphone para o servidor:**
1. Instale um app de ping (ex: "Network Utilities")
2. Ping para o IP do servidor
3. Se não responder, há problema de rede/firewall

## Logs e Debug

### Logs do Servidor
```
logs/sistema-inventario.log
```

### Logs do App
No app, vá em:
```
Menu > Configurações > Logs > Exportar Logs
```

## Checklist de Verificação

Antes de reportar um problema, verifique:

- [ ] PostgreSQL está rodando
- [ ] Servidor iniciou sem erros
- [ ] Porta 8081 está livre
- [ ] Firewall permite conexões na porta 8081
- [ ] Smartphone e computador na mesma rede Wi-Fi
- [ ] IP do servidor está correto no app
- [ ] Credenciais de login estão corretas
- [ ] App tem todas as permissões necessárias

## Suporte

Se o problema persistir:

1. Exporte os logs do servidor e do app
2. Anote a mensagem de erro exata
3. Verifique a versão do app e do servidor
4. Documente os passos que levaram ao erro
