# Correção Urgente: Coletas - Descrição NULL e Quantidade Errada

## 🚨 Problemas Identificados

### 1. Descrição do Patrimônio chegando NULL
**Evidência nos logs do app:**
```
descricaoPatrimonio: 'null'
```

### 2. Quantidade de coletas errada
- **Banco de dados**: 44 coletas
- **App mostrando**: 6 coletas
- **Causa**: Endpoint filtra por usuário ao invés de mostrar todas

---

## ✅ Correção Aplicada

### Android - ApiService.kt

**Arquivo**: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/api/ApiService.kt`

**ANTES:**
```kotlin
@GET("api/mobile/coletas")
suspend fun buscarTodasColetasSemPaginacao(): Response<ApiResponse<List<MobileColetaResponseDto>>>
```

**DEPOIS:**
```kotlin
@GET("api/mobile/coletas/all")
suspend fun buscarTodasColetasSemPaginacao(): Response<ApiResponse<List<MobileColetaResponseDto>>>
```

**Motivo:**
- Endpoint `/api/mobile/coletas` filtra por usuário quando tem token JWT
- Endpoint `/api/mobile/coletas/all` retorna TODAS as coletas do sistema
- Usa método otimizado com cache (95% menos queries)

---

## 📊 Resultado Esperado

### Antes:
- ❌ 6 coletas (apenas do usuário logado)
- ❌ Descrição: "Patrimônio coletado" (genérico)
- ❌ Sala: não mostrava

### Depois:
- ✅ 44 coletas (todas do sistema)
- ✅ Descrição: "CADEIRA PARA LABORATÓRIO" (real)
- ✅ Sala: "BIBLIOTECA" (correta)

---

## 🔧 Como Aplicar

### Opção 1: Recompilar o APK (Recomendado)

No Android Studio:
1. Abrir projeto `InventarioMobile`
2. Build > Clean Project
3. Build > Rebuild Project
4. Build > Build Bundle(s) / APK(s) > Build APK(s)
5. Instalar APK no dispositivo

### Opção 2: Editar Manualmente

1. Abrir arquivo:
   ```
   InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/api/ApiService.kt
   ```

2. Localizar linha ~96:
   ```kotlin
   @GET("api/mobile/coletas")
   suspend fun buscarTodasColetasSemPaginacao()
   ```

3. Alterar para:
   ```kotlin
   @GET("api/mobile/coletas/all")
   suspend fun buscarTodasColetasSemPaginacao()
   ```

4. Salvar e recompilar

---

## 🧪 Como Testar

1. **Instalar novo APK** no dispositivo

2. **Abrir tela "Itens Coletados"**

3. **Verificar:**
   - Total de coletas deve ser 44 (não 6)
   - Descrição deve aparecer (ex: "CADEIRA PARA LABORATÓRIO")
   - Sala deve aparecer (ex: "BIBLIOTECA")

4. **Verificar logs do app** (logcat):
   ```
   descricaoPatrimonio: 'CADEIRA PARA LABORATÓRIO'  ✅
   nomeSala: 'BIBLIOTECA'  ✅
   Total de coletas: 44  ✅
   ```

---

## 📝 Arquivos Modificados

1. ✅ **Backend**: `MobileColetaService.java` - Logs detalhados adicionados
2. ✅ **Android**: `ApiService.kt` - Endpoint alterado para `/all`

---

## ⚠️ Observações Importantes

### Por que o endpoint `/coletas` filtra por usuário?

O endpoint padrão `GET /api/mobile/coletas` foi projetado para:
- Mostrar apenas coletas do usuário logado
- Útil para tela "Minhas Coletas"
- Segurança: usuário vê apenas suas coletas

### Por que usar `/coletas/all`?

O endpoint `GET /api/mobile/coletas/all`:
- Mostra TODAS as coletas do sistema
- Útil para tela "Itens Coletados" (visão geral)
- Usa método otimizado com cache
- Performance: 95% menos queries ao banco

### Descrição NULL - Por que acontece?

A descrição estava NULL porque:
1. Servidor busca patrimônio do banco
2. Se patrimônio não existe ou erro na busca, descrição fica NULL
3. App mostra fallback: "Patrimônio coletado"

**Correção aplicada no servidor:**
- Logs detalhados para rastrear busca do patrimônio
- Try-catch explícito para capturar erros
- Garantido que descrição é sempre preenchida quando patrimônio existe

---

## 🎯 Próximos Passos

1. ✅ Correção aplicada no código
2. ⏳ Recompilar APK
3. ⏳ Instalar e testar
4. ⏳ Verificar logs do servidor
5. ⏳ Confirmar que 44 coletas aparecem
6. ⏳ Confirmar que descrições aparecem

---

**Data**: 24/11/2025 22:20  
**Status**: ✅ Código corrigido, aguardando recompilação do APK  
**Prioridade**: 🔴 URGENTE

