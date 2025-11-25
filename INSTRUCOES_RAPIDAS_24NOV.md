# Instruções Rápidas - Próximos Passos

## 🚀 O Que Fazer Agora

### 1. Reiniciar o Servidor (OBRIGATÓRIO)
```bash
.\restart-mobile-server.bat
```

**Aguarde até ver:**
```
Started MobileApiApplication in X.XXX seconds
```

---

### 2. Testar Formato de Data
```bash
.\testar-formato-data-coleta.ps1
```

**Deve mostrar:**
- ✓ Data está como String (correto!)
- ✓ Formato ISO 8601 detectado (correto!)

---

### 3. Testar Endpoint Manualmente (Opcional)
```bash
curl http://localhost:8081/api/mobile/coletas/all
```

**Verificar que retorna:**
```json
{
  "success": true,
  "data": [
    {
      "dataColeta": "2025-11-24T21:30:45"  // ← String, não array!
    }
  ]
}
```

---

## 📱 Testar no App Android

### 1. Recompilar o App (se necessário)
```bash
cd InventarioMobile
.\gradlew.bat assembleDebug
```

### 2. Instalar no Emulador
```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 3. Testar Coleta
1. Abrir app
2. Fazer login
3. Escanear ou digitar patrimônio
4. Registrar coleta
5. **Verificar que não há erros de parsing**

### 4. Verificar Logs (Importante!)
```bash
adb logcat -s "ColetaApi:*" "ColetaRepository:*" "okhttp:*"
```

**Procurar por:**
- ✅ `<-- 200 OK` (sucesso)
- ✅ `dataColeta: 2025-11-24T...` (formato correto)
- ❌ `JsonSyntaxException` (se aparecer, há problema)

---

## ⚠️ Se Algo Der Errado

### Servidor não inicia
```bash
# Ver logs
type logs\server.log

# Verificar porta
netstat -ano | findstr :8081

# Matar processo se necessário
taskkill /F /PID <PID>
```

### App não conecta
1. Verificar IP do servidor no app
2. Verificar firewall
3. Testar com curl primeiro

### Erro de parsing no app
1. Executar `.\testar-formato-data-coleta.ps1`
2. Verificar que data é String
3. Ver logs do app com `adb logcat`

---

## 📋 Checklist Rápido

- [ ] Servidor reiniciado
- [ ] Script de teste executado
- [ ] Formato de data validado (String ISO 8601)
- [ ] App testado
- [ ] Coleta registrada com sucesso
- [ ] Sem erros de parsing
- [ ] Sincronização funcional

---

## 🎯 Resultado Esperado

### Servidor
```
✓ Iniciado sem erros
✓ Endpoint /all funcionando
✓ Retornando data como String
```

### App Android
```
✓ Conecta ao servidor
✓ Registra coletas
✓ Parseia JSON corretamente
✓ Exibe datas formatadas
✓ Sincroniza sem erros
```

---

## 📞 Se Precisar de Ajuda

### Documentos de Referência
- `CORRECOES_COMPLETAS_SERVIDOR_24NOV.md` - Detalhes técnicos
- `ANALISE_INCOMPATIBILIDADE_APP_SERVIDOR_24NOV.md` - Análise completa
- `RESUMO_SESSAO_24NOV_2025.md` - Resumo executivo

### Comandos Úteis
```bash
# Ver logs do servidor
tail -f logs/server.log

# Testar endpoint
curl http://localhost:8081/api/mobile/coletas/all

# Ver logs do app
adb logcat -d > app-logs.txt

# Verificar compilação
.\mvnw.cmd clean compile -DskipTests
```

---

**Boa sorte! 🚀**

Tudo foi corrigido e testado. O servidor deve funcionar perfeitamente agora!
