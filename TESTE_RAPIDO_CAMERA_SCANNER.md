# ✅ Checklist de Teste - Correção da Câmera Scanner

## 🎯 Objetivo
Validar que a falha grave ao dar permissão à câmera foi corrigida.

---

## 📋 Testes Obrigatórios

### ✅ Teste 1: Primeira Instalação (Cenário Ideal)
**Passos:**
1. Desinstalar app completamente
2. Instalar APK novamente
3. Fazer login
4. Selecionar uma sala
5. Clicar em "Coleta Rápida" ou "Scanner"
6. **OBSERVAR:** Dialog pedindo permissão de câmera
7. Clicar em "Permitir"
8. **VERIFICAR:** Câmera abre sem crash
9. Escanear um código QR
10. **VERIFICAR:** Código é processado corretamente

**Resultado Esperado:** ✅ Câmera abre normalmente, sem crash

---

### ✅ Teste 2: Negação e Retry
**Passos:**
1. Abrir Scanner
2. Negar permissão quando solicitado
3. **VERIFICAR:** Dialog oferece "Tentar Novamente"
4. Clicar em "Tentar Novamente"
5. Conceder permissão
6. **VERIFICAR:** Câmera abre sem crash

**Resultado Esperado:** ✅ Retry funciona corretamente

---

### ✅ Teste 3: Negação Permanente
**Passos:**
1. Desinstalar e reinstalar app
2. Abrir Scanner
3. Negar permissão
4. Marcar "Não perguntar novamente"
5. **VERIFICAR:** Dialog oferece "Abrir Configurações"
6. Clicar em "Abrir Configurações"
7. **VERIFICAR:** Abre tela de configurações do app
8. Habilitar permissão de câmera manualmente
9. Voltar ao app (botão voltar)
10. **VERIFICAR:** Câmera abre automaticamente

**Resultado Esperado:** ✅ Fluxo de configurações funciona

---

### ✅ Teste 4: Permissão Já Concedida
**Passos:**
1. Com permissão já concedida
2. Abrir Scanner
3. **VERIFICAR:** Câmera abre imediatamente
4. Não deve pedir permissão novamente

**Resultado Esperado:** ✅ Câmera abre direto

---

### ✅ Teste 5: Rotação de Tela
**Passos:**
1. Abrir Scanner
2. Conceder permissão
3. Girar dispositivo (portrait → landscape)
4. **VERIFICAR:** Não há crash
5. Girar de volta
6. **VERIFICAR:** Scanner continua funcionando

**Resultado Esperado:** ✅ Sem crash na rotação

---

### ✅ Teste 6: Voltar e Abrir Novamente
**Passos:**
1. Abrir Scanner
2. Conceder permissão
3. Pressionar botão voltar (cancelar scan)
4. Abrir Scanner novamente
5. **VERIFICAR:** Câmera abre sem pedir permissão novamente

**Resultado Esperado:** ✅ Permissão é lembrada

---

## 🔍 Verificações de Log

### Durante os testes, verificar logs:

```bash
# Comando para ver logs em tempo real
adb logcat -s ScannerActivity:* | grep -E "VERIFICANDO|Resultado|INICIALIZANDO|sucesso|ERRO"
```

### Logs Esperados (Sucesso):

```
ScannerActivity: ═══════════════════════════════════════
ScannerActivity: VERIFICANDO PERMISSÃO DE CÂMERA (Android 14+)
ScannerActivity: ═══════════════════════════════════════
ScannerActivity: ✅ Permissão já concedida, inicializando scanner
ScannerActivity: ═══════════════════════════════════════
ScannerActivity: INICIALIZANDO SCANNER DE CÓDIGOS (Android 14+)
ScannerActivity: ═══════════════════════════════════════
ScannerActivity: Configurando ScanOptions...
ScannerActivity: Iniciando scanner com ScanContract...
ScannerActivity: ✅ Scanner iniciado com sucesso!
```

### Logs de Erro (NÃO devem aparecer):

```
❌ ERRO: Permissões de câmera não concedidas!
❌ ERRO: Câmera não está disponível!
❌ ERRO ao inicializar scanner
```

---

## 🚨 Sinais de Problema

### ❌ Crash Imediato
- App fecha ao conceder permissão
- **Ação:** Verificar logs completos com `adb logcat`

### ❌ Tela Preta
- Câmera não abre, tela fica preta
- **Ação:** Verificar se permissão foi realmente concedida

### ❌ Loop Infinito
- Continua pedindo permissão repetidamente
- **Ação:** Verificar flag `isInitializing`

### ❌ Sem Resposta
- App trava, não responde
- **Ação:** Verificar ANR logs

---

## 📱 Dispositivos para Testar

### Prioridade Alta
- [ ] Android 14 (API 34)
- [ ] Android 13 (API 33)
- [ ] Android 12 (API 31)

### Prioridade Média
- [ ] Android 11 (API 30)
- [ ] Android 10 (API 29)

### Dispositivos Físicos
- [ ] Dispositivo real (preferencial)
- [ ] Emulador (secundário)

---

## ✅ Critérios de Aceitação

Para considerar a correção bem-sucedida:

1. ✅ Nenhum crash ao conceder permissão
2. ✅ Câmera abre em menos de 2 segundos
3. ✅ Mensagens de status são claras
4. ✅ Retry funciona corretamente
5. ✅ Fluxo de configurações funciona
6. ✅ Sem crash na rotação de tela
7. ✅ Logs não mostram erros críticos

---

## 📊 Resultado dos Testes

### Teste 1: Primeira Instalação
- [ ] ✅ Passou
- [ ] ❌ Falhou - Motivo: _______________

### Teste 2: Negação e Retry
- [ ] ✅ Passou
- [ ] ❌ Falhou - Motivo: _______________

### Teste 3: Negação Permanente
- [ ] ✅ Passou
- [ ] ❌ Falhou - Motivo: _______________

### Teste 4: Permissão Já Concedida
- [ ] ✅ Passou
- [ ] ❌ Falhou - Motivo: _______________

### Teste 5: Rotação de Tela
- [ ] ✅ Passou
- [ ] ❌ Falhou - Motivo: _______________

### Teste 6: Voltar e Abrir Novamente
- [ ] ✅ Passou
- [ ] ❌ Falhou - Motivo: _______________

---

## 🎉 Conclusão

**Status Geral:** [ ] ✅ APROVADO | [ ] ❌ REPROVADO

**Observações:**
_____________________________________________
_____________________________________________
_____________________________________________

**Testado por:** _______________
**Data:** _______________
**Dispositivo:** _______________
**Android:** _______________

---

## 🔧 Comandos Úteis

### Compilar e Instalar
```bash
cd InventarioMobile
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Ver Logs
```bash
# Logs do Scanner
adb logcat -s ScannerActivity:*

# Logs de Permissão
adb logcat | grep -i "permission"

# Logs de Câmera
adb logcat | grep -i "camera"

# Limpar logs
adb logcat -c
```

### Resetar Permissões
```bash
# Resetar permissões do app
adb shell pm reset-permissions com.inventario.mobile

# Revogar permissão específica
adb shell pm revoke com.inventario.mobile android.permission.CAMERA
```

---

**Boa sorte nos testes!** 🚀
