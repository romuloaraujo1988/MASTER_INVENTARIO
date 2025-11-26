# 🔄 Como Sincronizar Coletas Pendentes no App Android

## 📱 Localização da Funcionalidade

A sincronização de coletas pendentes está disponível em **3 locais** no app:

---

## 🎯 Opção 1: Menu Lateral (Navigation Drawer) ✅ RECOMENDADO

### Como Acessar:
```
1. Abrir o app
2. Tocar no ícone ☰ (hambúrguer) no canto superior esquerdo
3. Procurar seção "Dados"
4. Tocar em "Sincronização" 🔄
```

### Estrutura do Menu:
```
☰ Menu Principal
├── 📊 Dashboard
├── 📦 Inventário
├── 📋 Minhas Coletas
├── 📷 Scanner QR Code
│
├── 📂 Dados
│   ├── 🔄 Sincronização ← AQUI!
│   ├── ⏳ Pendentes
│   └── ⚠️ Divergências
│
├── 🔧 Ferramentas
│   ├── 🔍 Busca Rápida
│   ├── 🎤 Busca por Voz
│   ├── 📈 Estatísticas
│   ├── 📄 Relatórios
│   └── 🌐 Diagnóstico de Rede
│
└── ⚙️ Sistema
    ├── ⚙️ Configurações
    ├── ℹ️ Sobre o App
    └── 🚪 Sair
```

---

## 🎤 Opção 2: Comando de Voz

### Como Usar:
```
1. No Dashboard, tocar no botão 🎤 (microfone flutuante)
2. Falar: "Sincronizar"
3. O app abre automaticamente a tela de sincronização
```

### Outros Comandos Disponíveis:
- "Buscar patrimônio [número]"
- "Mostrar sala [número]"
- "Listar divergências"
- "Listar pendentes"
- "Listar coletados"
- "Abrir scanner"
- "Voltar"

---

## 📋 Opção 3: Tela de Pendentes

### Como Acessar:
```
1. Menu ☰ → Dados → Pendentes
2. Visualizar lista de coletas não sincronizadas
3. Tocar em "Sincronizar Todas"
```

---

## 🔄 Tela de Sincronização - Funcionalidades

### Botões Disponíveis:

#### 1. 📤 Sincronizar Coletas
```
Função: Envia coletas pendentes para o servidor
Status: Requer internet
Ação: Batch sync (múltiplas coletas de uma vez)
```

#### 2. 📥 Importar do Servidor
```
Função: Baixa patrimônios, salas e responsáveis
Status: Requer internet
Ação: Atualiza banco local com dados do servidor
```

#### 3. 🗑️ Limpar Dados Locais
```
Função: Remove todos os dados do banco local
Status: Sempre disponível
Ação: Requer confirmação
```

#### 4. 🔄 Atualizar
```
Função: Recarrega estatísticas
Status: Sempre disponível
Ação: Atualiza contadores
```

### Estatísticas Exibidas:
```
📦 Patrimônios: [quantidade no banco local]
🏢 Salas: [quantidade no banco local]
👤 Responsáveis: [quantidade no banco local]
✅ Coletados: [total de coletas]
⏳ Pendentes: [coletas não sincronizadas]
```

### Indicador de Rede:
```
✓ Online (WiFi)     → Botões habilitados
✓ Online (Móvel)    → Botões habilitados
✗ Offline           → Botões desabilitados
```

---

## 🚀 Processo de Sincronização

### Sincronização de Coletas (Batch)

```
1. Usuário toca "Sincronizar Coletas"
   ↓
2. App verifica conexão de internet
   ↓
3. Busca todas as coletas pendentes (não sincronizadas)
   ↓
4. Agrupa coletas em lote (batch)
   ↓
5. Envia todas de uma vez para o servidor
   ↓
6. Servidor processa e retorna resultado
   ↓
7. App marca coletas como sincronizadas
   ↓
8. Atualiza estatísticas
   ↓
9. Mostra mensagem de sucesso
```

### Vantagens do Batch Sync:
- ✅ Envia múltiplas coletas em uma requisição
- ✅ Reduz consumo de dados
- ✅ Mais rápido que sync individual
- ✅ Fallback automático se falhar

### Fallback Inteligente:
```
Se batch sync falhar:
  ↓
Tenta sincronizar individualmente
  ↓
Marca apenas as que tiveram sucesso
  ↓
Mantém pendentes as que falharam
```

---

## 🔄 Sincronização Automática em Background

### WorkManager Configurado:
```
Frequência: A cada 30 minutos
Constraints:
  - ✅ Requer internet
  - ✅ Requer bateria não baixa
  - ✅ Retry automático se falhar
```

### Como Funciona:
```
1. App agenda WorkManager ao iniciar
2. A cada 30 minutos, verifica coletas pendentes
3. Se houver pendentes + internet disponível:
   → Sincroniza automaticamente
4. Se falhar:
   → Agenda retry com backoff exponencial
5. Notifica usuário sobre resultado (opcional)
```

### Controle Manual:
```kotlin
// Agendar sync periódico
syncManager.schedulePeriodicSync()

// Cancelar sync periódico
syncManager.cancelPeriodicSync()

// Forçar sync imediato
syncManager.forceSyncNow()
```

---

## 📊 Estados da Sincronização

### 1. Idle (Aguardando)
```
Status: Nenhuma operação em andamento
UI: Botões habilitados
Ação: Aguardando comando do usuário
```

### 2. Loading (Sincronizando)
```
Status: Operação em andamento
UI: Progress bar visível
     Botões desabilitados
     Mensagem de progresso
Ação: Aguardar conclusão
```

### 3. Success (Sucesso)
```
Status: Sincronização concluída
UI: Snackbar verde com estatísticas
     Contadores atualizados
Ação: Retorna ao estado Idle
```

### 4. Error (Erro)
```
Status: Falha na sincronização
UI: Snackbar vermelho com mensagem
     Botões reabilitados
Ação: Usuário pode tentar novamente
```

---

## ⚠️ Problemas Comuns e Soluções

### Problema 1: Botão "Sincronizar" Desabilitado
**Causa:** Sem conexão de internet  
**Solução:** 
1. Verificar WiFi/dados móveis
2. Tocar em "Atualizar" para revalidar conexão
3. Verificar indicador de rede na tela

### Problema 2: Sincronização Falha
**Causa:** Servidor offline ou timeout  
**Solução:**
1. Verificar se servidor está rodando
2. Testar endpoint: `http://IP:8081/api/mobile/coletas/batch`
3. Verificar logs do app: `adb logcat -s SyncViewModel:*`

### Problema 3: Coletas Não Aparecem como Pendentes
**Causa:** Já foram sincronizadas  
**Solução:**
1. Verificar contador "Pendentes" na tela
2. Se 0, todas já foram sincronizadas
3. Verificar no servidor se coletas chegaram

### Problema 4: Sincronização Lenta
**Causa:** Muitas coletas pendentes  
**Solução:**
1. Batch sync já otimiza isso
2. Aguardar conclusão (pode levar alguns segundos)
3. Verificar qualidade da conexão

---

## 🧪 Como Testar

### Teste 1: Sincronização Manual
```
1. Coletar 5 patrimônios offline
2. Verificar contador "Pendentes" = 5
3. Conectar internet
4. Menu → Sincronização
5. Tocar "Sincronizar Coletas"
6. Aguardar mensagem de sucesso
7. Verificar contador "Pendentes" = 0
8. Verificar no servidor se coletas chegaram
```

### Teste 2: Sincronização Automática
```
1. Coletar 3 patrimônios offline
2. Conectar internet
3. Aguardar 30 minutos (ou forçar WorkManager)
4. Verificar se coletas foram sincronizadas automaticamente
5. Verificar logs: adb logcat -s SyncWorker:*
```

### Teste 3: Fallback
```
1. Coletar 10 patrimônios offline
2. Desabilitar endpoint batch no servidor
3. Tentar sincronizar
4. Verificar que fallback para sync individual funciona
5. Verificar logs de fallback
```

---

## 📝 Logs Úteis

### Ver Logs de Sincronização:
```bash
# Logs gerais
adb logcat -s SyncActivity:* SyncViewModel:*

# Logs de WorkManager
adb logcat -s SyncWorker:* SyncManager:*

# Logs de rede
adb logcat -s okhttp.OkHttpClient:*

# Filtrar apenas erros
adb logcat -s SyncActivity:E SyncViewModel:E
```

### Logs Importantes:
```
✅ "Sincronização iniciada"
✅ "Batch sync: X coletas"
✅ "Sincronização concluída: X/Y"
⚠️ "Fallback para sync individual"
❌ "Erro na sincronização: [mensagem]"
```

---

## 🎯 Resumo Rápido

### Para Sincronizar Coletas Pendentes:

**Método 1 (Recomendado):**
```
Menu ☰ → Dados → Sincronização → Sincronizar Coletas
```

**Método 2 (Voz):**
```
Tocar 🎤 → Falar "Sincronizar"
```

**Método 3 (Automático):**
```
Aguardar 30 minutos (WorkManager faz automaticamente)
```

---

## 📱 Screenshots (Referência)

### Tela de Sincronização:
```
┌─────────────────────────────────┐
│  ← Sincronização de Dados       │
├─────────────────────────────────┤
│                                 │
│  Status da Rede                 │
│  ✓ Online (WiFi)                │
│                                 │
│  Estatísticas Locais            │
│  📦 Patrimônios: 11428          │
│  🏢 Salas: 11                   │
│  👤 Responsáveis: 45            │
│  ✅ Coletados: 150              │
│  ⏳ Pendentes: 25               │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 📤 Sincronizar Coletas  │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 📥 Importar do Servidor │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 🗑️ Limpar Dados Locais  │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 🔄 Atualizar            │   │
│  └─────────────────────────┘   │
│                                 │
└─────────────────────────────────┘
```

---

## ✅ Checklist de Verificação

Antes de sincronizar:
- [ ] Internet conectada (WiFi ou dados móveis)
- [ ] Servidor rodando (porta 8080 ou 8081)
- [ ] Existem coletas pendentes (contador > 0)
- [ ] Bateria não está baixa (para sync automático)

Após sincronizar:
- [ ] Contador "Pendentes" zerou
- [ ] Mensagem de sucesso apareceu
- [ ] Coletas aparecem no servidor
- [ ] Estatísticas atualizadas

---

**Última atualização:** 26/11/2025  
**Versão do App:** 2.0.0  
**Status:** ✅ Funcionalidade implementada e testada
