# Instruções para Testar Sincronização

## ⚠️ IMPORTANTE: Dados do App Foram Limpos

Os dados do app foram limpos para forçar o uso da nova URL correta.

## 📋 Passos para Testar

### 1. Fazer Login Novamente
O app foi reiniciado e precisa de login:
- **Usuário:** admin
- **Senha:** (sua senha de administrador)

### 2. Após Login, Ir para Sincronização
- Menu → Sincronização
- OU
- Configurações → Sincronização

### 3. Clicar em "Sincronizar do Servidor"

### 4. Aguardar Resultado

## 🔍 Monitorar Logs

Em outro terminal, execute:
```bash
adb logcat -s "SyncRepository:*" -v time
```

## ✅ Resultado Esperado

### Logs de Sucesso:
```
11-18 00:XX:XX.XXX D/SyncRepository: ═══════════════════════════════════════════
11-18 00:XX:XX.XXX D/SyncRepository: INICIANDO SINCRONIZAÇÃO COMPLETA
11-18 00:XX:XX.XXX D/SyncRepository: 1. Baixando patrimônios do servidor...
11-18 00:XX:XX.XXX D/SyncRepository: ✓ 150 patrimônios recebidos do servidor
11-18 00:XX:XX.XXX D/SyncRepository: ✓ 150 patrimônios salvos no banco local
11-18 00:XX:XX.XXX D/SyncRepository: 2. Baixando salas do servidor...
11-18 00:XX:XX.XXX D/SyncRepository: ✓ 25 salas recebidas do servidor
11-18 00:XX:XX.XXX D/SyncRepository: ✓ 25 salas salvas no banco local
11-18 00:XX:XX.XXX D/SyncRepository: SINCRONIZAÇÃO CONCLUÍDA
11-18 00:XX:XX.XXX D/SyncRepository: Patrimônios: 150
11-18 00:XX:XX.XXX D/SyncRepository: Salas: 25
```

### UI do App:
- Mensagem: "X patrimônios e Y salas sincronizados"
- Estatísticas atualizadas na tela

## ❌ Se Ainda Falhar

### Verificar URL no SharedPreferences
```bash
adb shell "run-as com.inventario.mobile.debug cat /data/data/com.inventario.mobile.debug/shared_prefs/inventario_mobile_prefs.xml" | Select-String "server_url"
```

**Deve mostrar:**
```xml
<string name="server_url">http://10.0.2.2:8081</string>
```

**NÃO deve ter `/inventario`!**

### Verificar Token
```bash
adb shell "run-as com.inventario.mobile.debug cat /data/data/com.inventario.mobile.debug/shared_prefs/inventario_mobile_prefs.xml" | Select-String "access_token"
```

Deve ter um token JWT válido.

### Testar Endpoint Manualmente
```powershell
# Pegar o token do app
$token = "eyJhbGciOiJIUzUxMiJ9..." # Token do SharedPreferences

# Testar endpoint
Invoke-WebRequest -Uri "http://localhost:8081/api/mobile/patrimonio" `
    -Headers @{"Authorization"="Bearer $token"} `
    -Method GET
```

Deve retornar 200 OK com lista de patrimônios.

## 🐛 Debug Adicional

### Ver Todos os Logs do App
```bash
adb logcat -s "InventarioMobile:*" "SyncRepository:*" "SyncViewModel:*" "OkHttp:*" -v time
```

### Ver Requisições HTTP
```bash
adb logcat -s "OkHttp:*" -v time
```

Deve mostrar:
```
--> GET http://10.0.2.2:8081/api/mobile/patrimonio
Authorization: Bearer eyJ...
<-- 200 OK
```

## 📞 Se Precisar de Ajuda

Envie os logs completos:
```bash
adb logcat -d > logs_sincronizacao.txt
```

---

**Atualizado em:** 18/11/2025 00:56  
**Status:** Aguardando teste após limpeza de dados
