# Guia de Testes - Ajustes no Salvamento de Coletas

## 🎯 Objetivo

Validar que os ajustes implementados estão funcionando corretamente e que as coletas estão sendo salvas com os dados corretos.

---

## 📋 Pré-requisitos

### 1. Compilar o App

```bash
cd InventarioMobile
.\gradlew.bat clean assembleDebug
```

### 2. Instalar no Dispositivo

```bash
.\gradlew.bat installDebug
```

### 3. Habilitar Logs

No Android Studio:
- Abrir Logcat
- Filtrar por: `ColetaMapper`, `ColetaRepositoryImpl`, `PreferencesManager`

---

## 🧪 Roteiro de Testes

### Teste 1: Verificar Salvamento do Inventário Ativo no Login

#### Objetivo
Garantir que o inventário ativo é salvo corretamente no `PreferencesManager` durante o login.

#### Passos
1. Abrir o app
2. Fazer login com usuário válido
3. Observar logs do Logcat

#### Logs Esperados
```
PreferencesManager: Inventário ativo salvo: ID=5, Nome=Inventário 2024
```

#### Verificação Manual
```kotlin
// Adicionar temporariamente no LoginActivity após login
val inventarioId = preferencesManager.getInventarioAtivoId()
val inventarioNome = preferencesManager.getInventarioAtivoNome()
Log.d("TEST_LOGIN", "Inventário: ID=$inventarioId, Nome=$inventarioNome")
```

#### Resultado Esperado
- ✅ `inventarioId` deve ser > 0 (ex: 5, 10, etc)
- ✅ `inventarioNome` deve ter o nome real (ex: "Inventário 2024")

#### Se Falhar
- Verificar se endpoint `/api/mobile/inventario/ativo` está funcionando
- Verificar se `saveInventarioAtivo()` está sendo chamado após buscar inventário
- Adicionar chamada em `LoginActivity` ou `MainActivity`

---

### Teste 2: Verificar Salvamento do Nome do Usuário no Login

#### Objetivo
Garantir que o nome do usuário é salvo corretamente no `PreferencesManager` durante o login.

#### Passos
1. Fazer login com usuário válido
2. Observar logs do Logcat

#### Verificação Manual
```kotlin
// Adicionar temporariamente no LoginActivity após login
val nomeUsuario = preferencesManager.getUserName()
Log.d("TEST_LOGIN", "Nome do usuário: $nomeUsuario")
```

#### Resultado Esperado
- ✅ `nomeUsuario` deve ter o nome real (ex: "Maria Santos")
- ❌ NÃO deve ser vazio ou "Usuário 1"

#### Se Falhar
- Verificar se `saveUserData()` está sendo chamado após login
- Adicionar chamada explícita:
```kotlin
preferencesManager.putString("user_name", usuario.nome)
```

---

### Teste 3: Registrar Coleta e Verificar Dados Salvos

#### Objetivo
Validar que a coleta é salva com `idInventario` e `nomeUsuario` corretos.

#### Passos
1. Fazer login
2. Escanear QR Code ou digitar número de patrimônio
3. Registrar coleta
4. Observar logs do Logcat

#### Logs Esperados
```
ColetaMapper: Inventário ativo: ID=5
ColetaRepositoryImpl: Enviando coleta para servidor: MobileColetaRequest(idInventario=5, ...)
ColetaRepositoryImpl: ✓ Coleta sincronizada com sucesso
```

#### Verificação no Banco de Dados

##### Opção A: Via Android Studio Database Inspector
1. Abrir Android Studio
2. View → Tool Windows → App Inspection
3. Selecionar app em execução
4. Abrir tabela `coleta`
5. Verificar última coleta inserida

##### Opção B: Via ADB Shell
```bash
adb shell
run-as com.inventario.mobile
cd databases
sqlite3 inventario.db

SELECT 
    id,
    numeroPatrimonio,
    idInventario,
    nomeSala,
    nomeUsuario,
    datetime(dataColeta/1000, 'unixepoch', 'localtime') as data,
    sincronizado
FROM coleta
ORDER BY dataColeta DESC
LIMIT 5;
```

#### Resultado Esperado
```
id | numeroPatrimonio | idInventario | nomeSala  | nomeUsuario    | data                | sincronizado
---|------------------|--------------|-----------|----------------|---------------------|-------------
1  | 12345           | 5            | Sala 101  | Maria Santos   | 2024-11-16 10:30:00 | 1
```

#### Verificar
- ✅ `idInventario` = ID do inventário ativo (não 2 ou 0)
- ✅ `nomeUsuario` = Nome real do usuário (não "Usuário 1")
- ✅ `numeroPatrimonio` = Número correto
- ✅ `nomeSala` = Nome da sala (se patrimônio tem sala)
- ✅ `sincronizado` = 1 (se sincronizou) ou 0 (se offline)

#### Se Falhar
- Verificar logs de warning:
```
ColetaMapper: ⚠️ Inventário ativo não encontrado! Usando 0 como fallback
```
- Se aparecer este warning, voltar ao Teste 1

---

### Teste 4: Sincronização com Servidor

#### Objetivo
Validar que a sincronização envia `idInventario` correto para o servidor.

#### Passos
1. Coletar 3 patrimônios offline (desconectar internet)
2. Reconectar internet
3. Abrir tela de Sincronização
4. Clicar em "Sincronizar Coletas"
5. Observar logs do Logcat

#### Logs Esperados
```
ColetaRepositoryImpl: Sincronizando 3 coletas pendentes
ColetaRepositoryImpl: Batch sync: 3 sucesso de 3
ColetaRepositoryImpl: ✓ 3 coletas sincronizadas com sucesso
```

#### Verificação no Servidor (Backend)

##### Opção A: Logs do Servidor
```
MobileColetaController: Registrando coleta: numeroPatrimonio=12345, idInventario=5
MobileColetaService: Coleta registrada com sucesso: ID=123
```

##### Opção B: Banco de Dados do Servidor
```sql
SELECT 
    id,
    numero_patrimonio,
    id_inventario,
    nome_usuario,
    data_coleta
FROM coleta
WHERE id_inventario = 5  -- ID do inventário ativo
ORDER BY data_coleta DESC
LIMIT 5;
```

#### Resultado Esperado
- ✅ Coletas sincronizadas com `idInventario` correto
- ✅ Servidor aceita e salva as coletas
- ✅ App marca coletas como sincronizadas

#### Se Falhar
- Verificar se servidor está rodando
- Verificar endpoint `/api/mobile/coletas/registrar`
- Verificar logs de erro no servidor

---

### Teste 5: Múltiplos Usuários

#### Objetivo
Validar que cada usuário tem suas coletas salvas com seu nome correto.

#### Passos
1. Fazer login com Usuário A
2. Registrar 2 coletas
3. Fazer logout
4. Fazer login com Usuário B
5. Registrar 2 coletas
6. Verificar banco de dados

#### Query SQL
```sql
SELECT 
    id,
    numeroPatrimonio,
    nomeUsuario,
    datetime(dataColeta/1000, 'unixepoch', 'localtime') as data
FROM coleta
ORDER BY dataColeta DESC
LIMIT 10;
```

#### Resultado Esperado
```
id | numeroPatrimonio | nomeUsuario    | data
---|------------------|----------------|--------------------
4  | 12348           | João Silva     | 2024-11-16 11:00:00
3  | 12347           | João Silva     | 2024-11-16 10:55:00
2  | 12346           | Maria Santos   | 2024-11-16 10:45:00
1  | 12345           | Maria Santos   | 2024-11-16 10:30:00
```

#### Verificar
- ✅ Cada coleta tem o nome do usuário que a registrou
- ✅ Não há mistura de nomes

---

### Teste 6: Múltiplos Inventários

#### Objetivo
Validar que coletas são salvas com o inventário ativo correto.

#### Passos
1. Fazer login
2. Verificar inventário ativo (ex: Inventário 2024, ID=5)
3. Registrar 2 coletas
4. Trocar inventário ativo (se possível)
5. Registrar 2 coletas
6. Verificar banco de dados

#### Query SQL
```sql
SELECT 
    id,
    numeroPatrimonio,
    idInventario,
    datetime(dataColeta/1000, 'unixepoch', 'localtime') as data
FROM coleta
ORDER BY dataColeta DESC
LIMIT 10;
```

#### Resultado Esperado
```
id | numeroPatrimonio | idInventario | data
---|------------------|--------------|--------------------
4  | 12348           | 10           | 2024-11-16 11:00:00
3  | 12347           | 10           | 2024-11-16 10:55:00
2  | 12346           | 5            | 2024-11-16 10:45:00
1  | 12345           | 5            | 2024-11-16 10:30:00
```

#### Verificar
- ✅ Coletas do Inventário 2024 (ID=5) têm `idInventario=5`
- ✅ Coletas do Inventário 2025 (ID=10) têm `idInventario=10`

---

## 🐛 Troubleshooting

### Problema 1: idInventario sempre 0

**Sintoma:**
```sql
SELECT idInventario, COUNT(*) FROM coleta GROUP BY idInventario;
-- Resultado: idInventario=0, count=10
```

**Causa:** Inventário ativo não foi salvo no login

**Solução:**
```kotlin
// Adicionar em LoginActivity ou MainActivity
val inventarioAtivo = buscarInventarioAtivoUseCase()
if (inventarioAtivo.isSuccess) {
    val inv = inventarioAtivo.getOrNull()
    preferencesManager.saveInventarioAtivo(inv.id, inv.nome)
}
```

---

### Problema 2: nomeUsuario sempre genérico

**Sintoma:**
```sql
SELECT DISTINCT nomeUsuario FROM coleta;
-- Resultado: "Usuário 1", "Usuário 2"
```

**Causa:** Nome do usuário não foi salvo no login

**Solução:**
```kotlin
// Adicionar em LoginActivity após login
val usuario = response.data.usuario
preferencesManager.putString("user_name", usuario.nome)
```

---

### Problema 3: Logs de warning

**Sintoma:**
```
ColetaMapper: ⚠️ Inventário ativo não encontrado! Usando 0 como fallback
```

**Causa:** `getInventarioAtivoId()` retorna null

**Solução:** Voltar ao Teste 1 e garantir que inventário é salvo

---

### Problema 4: Sincronização falha

**Sintoma:**
```
ColetaRepositoryImpl: ✗ Erro ao sincronizar coleta: 400 Bad Request
```

**Causa:** Servidor rejeita `idInventario=0`

**Solução:** Corrigir salvamento do inventário ativo (Teste 1)

---

## ✅ Checklist de Validação Final

### Dados Locais (SQLite)
- [ ] `idInventario` > 0 em todas as coletas
- [ ] `nomeUsuario` com nome real em todas as coletas
- [ ] `numeroPatrimonio` preenchido em todas as coletas
- [ ] `nomeSala` preenchido quando patrimônio tem sala

### Sincronização
- [ ] Coletas sincronizam com sucesso
- [ ] Servidor recebe `idInventario` correto
- [ ] Servidor recebe `nomeUsuario` correto
- [ ] Coletas marcadas como sincronizadas no app

### Múltiplos Cenários
- [ ] Funciona com múltiplos usuários
- [ ] Funciona com múltiplos inventários
- [ ] Funciona offline e online
- [ ] Funciona com batch sync e sync individual

---

## 📊 Relatório de Testes

### Template

```
Data: ___/___/______
Testador: _________________
Versão do App: _________________

Teste 1 - Inventário Ativo: [ ] ✅ [ ] ❌
Teste 2 - Nome do Usuário: [ ] ✅ [ ] ❌
Teste 3 - Registro de Coleta: [ ] ✅ [ ] ❌
Teste 4 - Sincronização: [ ] ✅ [ ] ❌
Teste 5 - Múltiplos Usuários: [ ] ✅ [ ] ❌
Teste 6 - Múltiplos Inventários: [ ] ✅ [ ] ❌

Observações:
_________________________________________________
_________________________________________________
_________________________________________________

Status Final: [ ] APROVADO [ ] REPROVADO
```

---

**Data:** 16/11/2025  
**Versão:** 2.0.0  
**Status:** ✅ Guia de Testes Completo
