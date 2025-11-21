---
inclusion: always
---

# ⚠️ REGRA CRÍTICA: NÃO ALTERAR ENDPOINTS FUNCIONAIS

## 🚨 ATENÇÃO: LEIA ANTES DE MODIFICAR QUALQUER CÓDIGO DE API

Esta é uma regra de steering **SEMPRE ATIVA** que deve ser seguida rigorosamente.

---

## ❌ PROIBIDO

### Nunca Altere URLs de Endpoints Funcionais

**NUNCA faça mudanças como estas sem validação completa:**

```kotlin
// ❌ PROIBIDO - Alterar URL de endpoint funcionando
@POST("api/mobile/coletas")  // Estava funcionando
↓
@POST("coletas")  // QUEBRA O APP!

// ❌ PROIBIDO - Remover prefixo
@GET("api/mobile/patrimonio")  // Estava funcionando
↓
@GET("patrimonio")  // QUEBRA O APP!

// ❌ PROIBIDO - Mudar estrutura
@GET("api/mobile/salas")  // Estava funcionando
↓
@GET("salas/listar")  // QUEBRA O APP!
```

---

## ✅ REGRAS OBRIGATÓRIAS

### 1. Antes de Alterar Qualquer Endpoint

```
1. Verificar se o endpoint está em uso
2. Verificar logs recentes de sucesso
3. Testar no Postman/curl
4. Documentar a mudança
5. Criar backup do código
6. Testar após mudança
7. Validar no app Android
```

### 2. Padrão de URLs Mobile

**SEMPRE use este padrão para APIs mobile:**

```kotlin
// ✅ CORRETO - URL completa com prefixo
@POST("api/mobile/coletas")
@GET("api/mobile/patrimonio")
@GET("api/mobile/salas")
@POST("api/mobile/auth/login")
@POST("api/mobile/coletas/batch")
```

**NUNCA use URLs relativas sem prefixo:**

```kotlin
// ❌ ERRADO - URL sem prefixo
@POST("coletas")
@GET("patrimonio")
@GET("salas")
```

### 3. Verificação Obrigatória

Antes de fazer commit, verificar:

```bash
# Verificar se URLs estão corretas
grep -r "@POST\|@GET\|@PUT\|@DELETE" InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/api/

# Deve retornar URLs com "api/mobile/"
```

---

## 📋 Endpoints Críticos (NÃO ALTERAR)

### Autenticação
```kotlin
@POST("api/mobile/auth/login")           // ✅ FUNCIONANDO
@POST("api/mobile/auth/refresh")         // ✅ FUNCIONANDO
```

### Coletas
```kotlin
@POST("api/mobile/coletas")              // ✅ FUNCIONANDO
@POST("api/mobile/coletas/batch")        // ✅ FUNCIONANDO
@POST("api/mobile/coletas/verificar-duplicata")  // ✅ FUNCIONANDO
```

### Patrimônios
```kotlin
@GET("api/mobile/patrimonio")            // ✅ FUNCIONANDO
@GET("api/mobile/patrimonio/{id}")       // ✅ FUNCIONANDO
@GET("api/mobile/patrimonio/numero/{numero}")  // ✅ FUNCIONANDO
@GET("api/mobile/patrimonio/numero/{numero}/validar")  // ✅ FUNCIONANDO
@GET("api/mobile/patrimonio/numero/{numero}/coletado")  // ✅ FUNCIONANDO
```

### Salas
```kotlin
@GET("api/mobile/salas")                 // ✅ FUNCIONANDO
@GET("api/mobile/salas/{id}")            // ✅ FUNCIONANDO
```

### Inventário
```kotlin
@GET("api/mobile/inventario/ativo")     // ✅ FUNCIONANDO
@GET("api/mobile/inventario/{id}")       // ✅ FUNCIONANDO
```

### Descrições
```kotlin
@GET("api/mobile/descricoes/nao-coletadas")  // ✅ FUNCIONANDO
```

---

## 🔍 Como Verificar se Endpoint Está Funcionando

### 1. Verificar Logs do App
```bash
adb logcat -s okhttp.OkHttpClient:*
# Procurar por: <-- 200 OK
```

### 2. Testar com curl
```bash
curl -X POST http://localhost:8081/api/mobile/coletas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{...}'
```

### 3. Verificar no Backend
```bash
# Procurar controller
grep -r "@PostMapping.*coletas" src/main/java/
```

---

## 📝 Histórico de Problemas

### Problema 1: Coletas Não Salvavam (18/11/2025)
**Causa**: URL alterada de `api/mobile/coletas` para `coletas`  
**Impacto**: App não salvava coletas (HTTP 404)  
**Solução**: Revertido para `api/mobile/coletas`  
**Lição**: NUNCA alterar URLs sem testar

---

## 🎯 Processo de Mudança Segura

Se REALMENTE precisar alterar um endpoint:

### 1. Criar Novo Endpoint (Não Alterar o Antigo)
```kotlin
// ✅ Manter o antigo funcionando
@POST("api/mobile/coletas")
suspend fun registrarColeta(...)

// ✅ Criar novo se necessário
@POST("api/mobile/v2/coletas")
suspend fun registrarColetaV2(...)
```

### 2. Deprecar Gradualmente
```kotlin
@Deprecated("Use registrarColetaV2")
@POST("api/mobile/coletas")
suspend fun registrarColeta(...)
```

### 3. Remover Apenas Após Validação
- Testar novo endpoint por 1 semana
- Verificar que nenhum cliente usa o antigo
- Documentar a remoção
- Fazer backup antes de remover

---

## ⚡ Ações Imediatas em Caso de Quebra

Se um endpoint parar de funcionar:

### 1. Verificar Logs
```bash
adb logcat -d | grep "HTTP 404\|HTTP 500"
```

### 2. Comparar com Versão Anterior
```bash
git diff HEAD~1 -- InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/api/
```

### 3. Reverter Mudança
```bash
git checkout HEAD~1 -- arquivo_modificado.kt
```

### 4. Recompilar e Reinstalar
```bash
cd InventarioMobile
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 📊 Checklist de Segurança

Antes de fazer commit de mudanças em APIs:

- [ ] URLs mantêm prefixo `api/mobile/`
- [ ] Endpoints testados com Postman/curl
- [ ] Logs do app mostram 200 OK
- [ ] App Android testado em emulador
- [ ] Documentação atualizada
- [ ] Backup do código criado
- [ ] Equipe notificada sobre mudança

---

## 🚨 RESUMO

### ❌ NUNCA FAÇA
- Alterar URLs de endpoints funcionando
- Remover prefixo `api/mobile/`
- Mudar estrutura sem testar
- Fazer commit sem validar

### ✅ SEMPRE FAÇA
- Manter URLs completas com `api/mobile/`
- Testar antes de commitar
- Documentar mudanças
- Criar novos endpoints ao invés de alterar

---

**Esta regra é CRÍTICA e deve ser seguida SEMPRE.**

**Última atualização**: 18/11/2025  
**Motivo**: Correção urgente de endpoints quebrados  
**Status**: ⚠️ REGRA ATIVA PERMANENTEMENTE
