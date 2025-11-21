# 📱 Instruções para Testar Sincronização

## 🎯 Objetivo
Testar a sincronização do app Android e identificar o problema.

---

## 📋 Pré-requisitos

- ✅ Servidor rodando (porta 8081)
- ✅ App instalado no dispositivo/emulador
- ✅ Usuário logado no app

---

## 🚀 Passo a Passo

### Passo 1: Verificar Servidor
```powershell
# Execute este comando para confirmar que o servidor está funcionando:
.\test-sync-direct.ps1
```

**Resultado Esperado:**
```
✓ Servidor está rodando
✓ Login bem-sucedido
✓ Endpoint funcionando - 50 patrimônios retornados
✓ Endpoint funcionando - 10 salas retornadas
```

---

### Passo 2: Iniciar Monitoramento
```bash
# Execute este comando em um terminal separado:
.\monitorar-sync-simples.bat
```

**O que vai acontecer:**
- Terminal ficará aguardando logs
- Deixe este terminal aberto

---

### Passo 3: Abrir o App

**No dispositivo/emulador:**
1. Abra o app "SIHCP" ou "Inventário Mobile"
2. Certifique-se de estar logado

---

### Passo 4: Navegar para Sincronização

**Opção A - Via Menu Lateral:**
1. Toque no ícone ☰ (menu hambúrguer) no canto superior esquerdo
2. Role até a seção "Dados"
3. Toque em "Sincronização"

**Opção B - Via Bottom Navigation:**
1. Na barra inferior, toque no ícone de sincronização (🔄)

---

### Passo 5: Executar Sincronização

**Na tela de Sincronização:**
1. Observe os dados locais exibidos
2. Toque no botão **"Sincronizar Agora"**
3. Aguarde o processo

---

### Passo 6: Observar Logs

**No terminal de monitoramento, você deve ver:**

#### ✅ Cenário 1: Sucesso
```
D/SyncRepository: INICIANDO SINCRONIZAÇÃO COMPLETA
D/SyncRepository: 1. Baixando patrimônios do servidor...
D/SyncRepository: ✓ 50 patrimônios recebidos do servidor
D/SyncRepository: ✓ 50 patrimônios salvos no banco local
D/SyncRepository: 2. Baixando salas do servidor...
D/SyncRepository: ✓ 10 salas recebidas do servidor
D/SyncRepository: ✓ 10 salas salvas no banco local
D/SyncRepository: SINCRONIZAÇÃO CONCLUÍDA
```

**No app:**
- Mensagem de sucesso aparece
- Contadores são atualizados
- Patrimônios: 50
- Salas: 10

---

#### ⚠️ Cenário 2: Erro de Token
```
W/SyncRepository: ⚠ Erro ao baixar patrimônios: 401 Unauthorized
E/SyncRepository: ✗ ERRO CRÍTICO ao sincronizar patrimônios
```

**Solução:**
1. Fazer logout no app
2. Fazer login novamente
3. Tentar sincronizar novamente

---

#### ⚠️ Cenário 3: Erro de Conexão
```
E/SyncRepository: ✗ ERRO CRÍTICO: Unable to resolve host
```

**Solução:**
1. Verificar se servidor está rodando
2. Verificar URL no código (deve ser `10.0.2.2` para emulador)
3. Verificar firewall

---

#### ❌ Cenário 4: Nenhum Log
**Sintoma:** Terminal não mostra nenhum log

**Possíveis Causas:**
1. Botão não está conectado ao ViewModel
2. Exceção sendo capturada silenciosamente
3. ViewModel não está sendo injetado

**Solução:**
1. Verificar se o app foi recompilado após mudanças
2. Adicionar logs extras no código
3. Verificar se Hilt está configurado corretamente

---

## 🔍 Verificações Adicionais

### Verificar Token no App
```bash
# Execute para ver o token atual:
adb logcat -d | Select-String -Pattern "LocalDataManager - Token:" | Select-Object -Last 1
```

### Verificar URL Base
```bash
# Execute para ver requisições HTTP:
adb logcat -d | Select-String -Pattern "NetworkModule.*URL:" | Select-Object -Last 5
```

### Verificar Erros Gerais
```bash
# Execute para ver todos os erros:
adb logcat -d *:E | Select-Object -Last 20
```

---

## 📊 Interpretando Resultados

### ✅ Sucesso Total
- Logs mostram sincronização completa
- App mostra 50 patrimônios e 10 salas
- Mensagem de sucesso aparece

**Ação:** Nenhuma, está funcionando!

---

### ⚠️ Sucesso Parcial
- Patrimônios sincronizados, mas salas não
- Ou vice-versa

**Ação:** Verificar endpoint específico que falhou

---

### ❌ Falha Total
- Nenhum dado sincronizado
- Mensagem de erro aparece

**Ação:** Identificar erro específico nos logs e corrigir

---

### 🤔 Sem Resposta
- Botão não responde
- Nenhum log aparece

**Ação:** Verificar código do botão e ViewModel

---

## 📝 Reportar Problema

Se encontrar um problema, anote:

1. **Cenário:** Qual dos 4 cenários acima ocorreu?
2. **Logs:** Copie os logs do terminal
3. **Mensagem no App:** Qual mensagem apareceu (se houver)?
4. **Dados Locais:** Quantos patrimônios/salas estavam antes?
5. **Conexão:** WiFi ou dados móveis?

---

## 🎯 Resultado Esperado

**Após sincronização bem-sucedida:**
- ✅ 50 patrimônios no banco local
- ✅ 10 salas no banco local
- ✅ Mensagem "Sincronização concluída!"
- ✅ Contadores atualizados na tela
- ✅ Logs completos no terminal

---

## 🆘 Problemas Comuns

### "Servidor não responde"
- Verificar se servidor está rodando
- Executar `.\test-sync-direct.ps1`

### "Token inválido"
- Fazer logout e login novamente
- Token expira após 24 horas

### "Sem conexão"
- Verificar WiFi/dados
- Emulador deve usar `10.0.2.2` ao invés de `localhost`

### "App trava"
- Verificar logs de erro: `adb logcat *:E`
- Pode ser problema de memória ou banco de dados

---

**Boa sorte com o teste!** 🚀

Se precisar de ajuda, tenha os logs em mãos.
