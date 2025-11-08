# Solução Rápida - App Não Consegue Fazer Login

## Situação Atual
- **Servidor rodando:** ✅ Sim
- **IP do computador:** 10.14.250.236
- **Porta:** 8081
- **Problema:** App Android não consegue fazer login

## Soluções Rápidas

### SOLUÇÃO 1: Reconfigurar IP no App (MAIS PROVÁVEL)

**No smartphone:**
1. Abra o app InventarioMobile
2. Vá em **Configurações** ou **Settings**
3. Configure:
   - **IP:** `10.14.250.236`
   - **Porta:** `8081`
   - **Protocolo:** HTTP
4. Clique em **Testar Conexão**
5. Se conectar, faça login novamente

---

### SOLUÇÃO 2: Liberar Firewall do Windows

**No computador (como Administrador):**

```powershell
# Verificar se regra existe
netsh advfirewall firewall show rule name="Sistema Inventario Mobile"

# Se não existir, criar regra
netsh advfirewall firewall add rule name="Sistema Inventario Mobile" dir=in action=allow protocol=TCP localport=8081

# Verificar se foi criada
netsh advfirewall firewall show rule name="Sistema Inventario Mobile"
```

---

### SOLUÇÃO 3: Testar Conectividade

**No smartphone (usando navegador):**

Acesse no navegador do celular:
```
http://10.14.250.236:8081/inventario/api/mobile/health
```

**Resultado esperado:**
- ✅ Se aparecer algo (JSON ou texto): Servidor acessível
- ❌ Se der erro de conexão: Problema de rede/firewall

---

### SOLUÇÃO 4: Verificar Mesma Rede WiFi

**Importante:**
- Smartphone e computador devem estar na **mesma rede WiFi**
- Verifique se não está usando dados móveis no celular
- Desative VPN se estiver ativa

**Verificar no smartphone:**
1. Configurações > WiFi
2. Veja o nome da rede conectada
3. Compare com a rede do computador

---

### SOLUÇÃO 5: Reinstalar APK com IP Correto

Se o IP já está no network_security_config.xml (10.14.250.236), o APK atual deve funcionar.

**Localização do APK:**
```
dist\InventarioMobile-v1.1-debug-IP-10.14.250.236.apk
```

**Se precisar recompilar:**
1. Verifique se o IP está em: `InventarioMobile/app/src/main/res/xml/network_security_config.xml`
2. Execute: `cd InventarioMobile && .\gradlew.bat assembleDebug`
3. Instale o novo APK

---

## Checklist de Diagnóstico

### No Computador:
- [ ] Servidor está rodando? (netstat -ano | findstr :8081)
- [ ] IP é 10.14.250.236? (ipconfig)
- [ ] Firewall liberado para porta 8081?
- [ ] Conectado ao WiFi?

### No Smartphone:
- [ ] Mesma rede WiFi do computador?
- [ ] IP configurado: 10.14.250.236?
- [ ] Porta configurada: 8081?
- [ ] Navegador acessa http://10.14.250.236:8081/inventario/api/mobile/health?

---

## Comandos Úteis

### Verificar servidor rodando:
```powershell
netstat -ano | Select-String ":8081"
```

### Ver IP do computador:
```powershell
ipconfig | Select-String "IPv4"
```

### Liberar firewall:
```powershell
netsh advfirewall firewall add rule name="Inventario Mobile" dir=in action=allow protocol=TCP localport=8081
```

### Testar do próprio computador:
```powershell
curl http://localhost:8081/inventario/api/mobile/health
```

---

## Logs para Verificar

### No servidor (computador):
Verifique o arquivo: `logs/sistema-inventario.log`

Procure por:
- Erros de autenticação
- Tentativas de conexão
- Erros de CORS

### No app (smartphone):
Use o Logcat do Android Studio ou:
- Abra a tela de diagnóstico no app
- Veja os logs de conexão

---

## Teste Rápido de Conectividade

### Passo 1: No computador
```powershell
# Verificar se servidor responde localmente
curl http://localhost:8081/inventario/api/mobile/health
```

### Passo 2: No smartphone
Abra o navegador e acesse:
```
http://10.14.250.236:8081/inventario/api/mobile/health
```

### Passo 3: Se funcionar no navegador mas não no app
- Problema está no app (configuração ou cache)
- Solução: Limpar dados do app ou reinstalar

---

## Credenciais Padrão

**Usuário:** admin  
**Senha:** admin123

(ou as credenciais que você configurou no banco)

---

## Contato de Suporte

Se nenhuma solução funcionar, verifique:
1. Logs do servidor
2. Logs do app
3. Configuração do banco de dados
4. Tabela de usuários no PostgreSQL

---

**Última atualização:** 28/10/2025  
**IP atual:** 10.14.250.236  
**Porta:** 8081
