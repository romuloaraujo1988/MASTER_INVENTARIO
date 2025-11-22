# 🧪 Roteiro de Testes - Modo Offline

**Data:** 22/11/2025  
**Objetivo:** Validar correção do modo offline

---

## 📋 **Checklist de Testes**

### **PRÉ-REQUISITOS**
- [ ] App compilado com sucesso
- [ ] Dispositivo/Emulador disponível
- [ ] Acesso ao logcat para ver logs

---

## 🧪 **TESTE 1: Sincronização Inicial (Online)**

### **Objetivo:** Garantir que dados são salvos no SQLite

**Passos:**
1. [ ] Conectar dispositivo à internet
2. [ ] Abrir app
3. [ ] Fazer login
4. [ ] Ir para "Nova Coleta"
5. [ ] Aguardar carregamento de salas

**Resultado Esperado:**
- ✅ Lista de salas aparece
- ✅ Logs mostram: "salas recebidas do servidor"
- ✅ Logs mostram: "salas salvas no SQLite"

**Logs Esperados:**
```
D/SalaSelectionViewModel: 📱 PASSO 1: Buscando salas do SQLite local...
D/SalaSelectionViewModel: ⚠️ Nenhuma sala no banco local, buscando da API...
D/SalaSelectionViewModel: 🌐 Buscando salas da API...
D/SalaSelectionViewModel: ✅ 150 salas recebidas do servidor
D/SalaSelectionViewModel: 💾 150 salas salvas no SQLite
```

**Status:** [ ] PASSOU [ ] FALHOU

---

## 🧪 **TESTE 2: Modo Offline Puro**

### **Objetivo:** Verificar se app funciona 100% offline

**Passos:**
1. [ ] Desconectar internet (Modo Avião)
2. [ ] Fechar app completamente
3. [ ] Abrir app novamente
4. [ ] Fazer login (se necessário)
5. [ ] Ir para "Nova Coleta"
6. [ ] Verificar lista de salas

**Resultado Esperado:**
- ✅ Lista de salas aparece (do SQLite)
- ✅ Banner "Modo offline" aparece
- ✅ Logs mostram: "salas encontradas no banco local"
- ✅ Não trava ou mostra erro

**Logs Esperados:**
```
D/SalaSelectionViewModel: 📱 PASSO 1: Buscando salas do SQLite local...
D/SalaSelectionViewModel: ✅ 150 salas encontradas no banco local
D/SalaSelectionViewModel: ✅ Salas carregadas do SQLite e exibidas
D/SalaSelectionViewModel: 🔄 Atualizando salas do servidor em background...
D/SalaSelectionViewModel: ℹ️ Não foi possível atualizar do servidor (modo offline)
```

**Status:** [ ] PASSOU [ ] FALHOU

---

## 🧪 **TESTE 3: Seleção de Sala Offline**

### **Objetivo:** Verificar fluxo completo de coleta offline

**Passos:**
1. [ ] Estar em modo offline (Teste 2)
2. [ ] Selecionar uma sala da lista
3. [ ] Verificar se abre tela de scanner
4. [ ] Escanear um QR Code (ou digitar número)
5. [ ] Verificar se patrimônio é encontrado
6. [ ] Salvar coleta

**Resultado Esperado:**
- ✅ Sala é selecionada
- ✅ Scanner abre normalmente
- ✅ Patrimônio é encontrado (se estiver no SQLite)
- ✅ Coleta é salva localmente
- ✅ Mensagem de sucesso aparece

**Status:** [ ] PASSOU [ ] FALHOU

---

## 🧪 **TESTE 4: Primeiro Acesso Offline**

### **Objetivo:** Verificar comportamento sem dados locais

**Passos:**
1. [ ] Desinstalar app
2. [ ] Instalar app novamente
3. [ ] Desconectar internet (Modo Avião)
4. [ ] Abrir app
5. [ ] Tentar fazer login
6. [ ] Ir para "Nova Coleta"

**Resultado Esperado:**
- ✅ Mensagem clara: "Sem dados locais. Conecte-se para sincronizar"
- ✅ Não trava
- ✅ Não mostra erro genérico

**Logs Esperados:**
```
D/SalaSelectionViewModel: 📱 PASSO 1: Buscando salas do SQLite local...
D/SalaSelectionViewModel: ⚠️ Nenhuma sala no banco local, buscando da API...
D/SalaSelectionViewModel: ❌ Erro ao buscar da API: Unable to resolve host
D/SalaSelectionViewModel: 🔄 Tentando fallback para SQLite...
D/SalaSelectionViewModel: ❌ Sem dados locais disponíveis
```

**Status:** [ ] PASSOU [ ] FALHOU

---

## 🧪 **TESTE 5: Reconexão e Sincronização**

### **Objetivo:** Verificar sincronização ao reconectar

**Passos:**
1. [ ] Estar offline com coletas pendentes
2. [ ] Reconectar internet
3. [ ] Abrir tela de sincronização
4. [ ] Sincronizar coletas pendentes
5. [ ] Verificar se coletas foram enviadas

**Resultado Esperado:**
- ✅ Coletas são sincronizadas
- ✅ Contador de pendentes diminui
- ✅ Mensagem de sucesso aparece
- ✅ Dados do servidor são atualizados

**Status:** [ ] PASSOU [ ] FALHOU

---

## 🧪 **TESTE 6: Atualização em Background**

### **Objetivo:** Verificar atualização silenciosa

**Passos:**
1. [ ] Ter salas no SQLite
2. [ ] Conectar internet
3. [ ] Abrir app
4. [ ] Ir para "Nova Coleta"
5. [ ] Observar comportamento

**Resultado Esperado:**
- ✅ Salas aparecem IMEDIATAMENTE (do SQLite)
- ✅ Atualização do servidor acontece em background
- ✅ UI não trava durante atualização
- ✅ Logs mostram atualização em background

**Logs Esperados:**
```
D/SalaSelectionViewModel: ✅ 150 salas encontradas no banco local
D/SalaSelectionViewModel: ✅ Salas carregadas do SQLite e exibidas
D/SalaSelectionViewModel: 🔄 Atualizando salas do servidor em background...
D/SalaSelectionViewModel: ✅ 150 salas recebidas do servidor
```

**Status:** [ ] PASSOU [ ] FALHOU

---

## 🧪 **TESTE 7: Performance**

### **Objetivo:** Verificar velocidade de carregamento

**Passos:**
1. [ ] Ter salas no SQLite
2. [ ] Desconectar internet
3. [ ] Abrir app
4. [ ] Cronometrar tempo até lista aparecer

**Resultado Esperado:**
- ✅ Salas aparecem em < 500ms
- ✅ Muito mais rápido que buscar da API
- ✅ UI responsiva

**Status:** [ ] PASSOU [ ] FALHOU

---

## 🧪 **TESTE 8: Múltiplas Coletas Offline**

### **Objetivo:** Verificar múltiplas coletas sem internet

**Passos:**
1. [ ] Estar offline
2. [ ] Fazer 5 coletas diferentes
3. [ ] Verificar se todas são salvas
4. [ ] Reconectar e sincronizar
5. [ ] Verificar se todas foram enviadas

**Resultado Esperado:**
- ✅ Todas as 5 coletas são salvas localmente
- ✅ Contador de pendentes mostra 5
- ✅ Ao sincronizar, todas são enviadas
- ✅ Nenhuma coleta é perdida

**Status:** [ ] PASSOU [ ] FALHOU

---

## 📊 **Resumo dos Testes**

| Teste | Status | Observações |
|-------|--------|-------------|
| 1. Sincronização Inicial | [ ] | |
| 2. Modo Offline Puro | [ ] | |
| 3. Seleção de Sala Offline | [ ] | |
| 4. Primeiro Acesso Offline | [ ] | |
| 5. Reconexão e Sincronização | [ ] | |
| 6. Atualização em Background | [ ] | |
| 7. Performance | [ ] | |
| 8. Múltiplas Coletas Offline | [ ] | |

**Total:** 0/8 testes passaram

---

## 🐛 **Problemas Encontrados**

### **Problema 1:**
- **Teste:** 
- **Descrição:** 
- **Logs:** 
- **Solução:** 

### **Problema 2:**
- **Teste:** 
- **Descrição:** 
- **Logs:** 
- **Solução:** 

---

## ✅ **Critérios de Aceitação**

Para considerar o modo offline funcional:

- [ ] Todos os 8 testes passaram
- [ ] Nenhum crash ou erro crítico
- [ ] Performance aceitável (< 500ms)
- [ ] Mensagens de erro claras
- [ ] Sincronização funciona corretamente
- [ ] Dados não são perdidos

---

## 📝 **Comandos Úteis**

### **Ver Logs em Tempo Real:**
```bash
adb logcat -s SalaSelectionViewModel:* InventarioRepository:* ColetaRepositoryImpl:*
```

### **Limpar Dados do App:**
```bash
adb shell pm clear com.inventario.mobile
```

### **Verificar Banco SQLite:**
```bash
adb shell
cd /data/data/com.inventario.mobile/databases
sqlite3 inventario.db
.tables
SELECT COUNT(*) FROM sala;
SELECT COUNT(*) FROM patrimonio;
.quit
```

### **Simular Modo Offline:**
```bash
# Desabilitar dados móveis e WiFi
adb shell svc wifi disable
adb shell svc data disable

# Habilitar novamente
adb shell svc wifi enable
adb shell svc data enable
```

---

## 🎯 **Próximos Passos Após Testes**

### **Se TODOS os testes passarem:**
1. [ ] Marcar correção como validada
2. [ ] Documentar resultados
3. [ ] Fazer commit das mudanças
4. [ ] Gerar APK de teste
5. [ ] Distribuir para beta testers

### **Se ALGUM teste falhar:**
1. [ ] Documentar problema
2. [ ] Analisar logs
3. [ ] Corrigir código
4. [ ] Repetir testes

---

**Criado em:** 22/11/2025  
**Status:** ⏳ AGUARDANDO EXECUÇÃO
