# 🐛 Problema: Coleta Excluída Ainda é Detectada como Duplicata

## 📋 Descrição do Problema

Quando uma coleta é excluída no servidor (via app ou desktop), o sistema ainda detecta o item como "já coletado" ao tentar coletar novamente, impedindo nova coleta.

## 🔍 Causa Raiz

O problema ocorre devido a **cache desatualizado no banco local do app Android (Room)**:

### 1. **Fluxo Normal de Exclusão**
```
1. Usuário exclui coleta no servidor (via app ou desktop)
2. Servidor executa: DELETE FROM TABELA_COLETA WHERE ID = ?
3. Coleta é removida do PostgreSQL ✅
4. Banco local do Android (Room) NÃO é atualizado ❌
```

### 2. **Fluxo de Verificação de Duplicata**
```
1. Usuário tenta coletar o mesmo item novamente
2. App verifica duplicata no banco LOCAL primeiro
3. Query no Room: SELECT * FROM coleta WHERE idPatrimonio = ? AND idInventario = ?
4. Encontra a coleta antiga (que foi excluída no servidor mas ainda existe localmente)
5. Bloqueia nova coleta com mensagem "Item já foi coletado" ❌
```

## 🎯 Onde Está o Problema

### Arquivo: `ColetaDao.kt`
```kotlin
@Query("""
    SELECT * FROM coleta 
    WHERE idPatrimonio = :idPatrimonio 
    AND idInventario = :idInventario
    LIMIT 1
""")
suspend fun buscarColetaExistente(
    idPatrimonio: Int, 
    idInventario: Int
): ColetaEntity?
```

**Problema:** Esta query verifica apenas o banco local, sem sincronizar com o servidor.

### Arquivo: `ColetaDAO.java` (Servidor)
```java
public boolean coletaExiste(int idInventario, int idPatrimonio) throws SQLException {
    String sql = "SELECT COUNT(*) FROM TABELA_COLETA " +
                 "WHERE ID_INVENTARIO = ? AND ID_PATRIMONIO = ? AND STATUS_COLETA = 'COLETADO'";
    // ...
}
```

**Problema:** Query está correta, mas o app Android não consulta o servidor antes de verificar localmente.

## ✅ Soluções Implementadas

### 1. **Documentação Adicionada no ColetaDao.kt**
```kotlin
/**
 * Verifica se patrimônio já foi coletado neste inventário
 * CRÍTICO: Evita duplicatas
 * 
 * IMPORTANTE: Esta query verifica apenas o banco local.
 * Se uma coleta foi excluída no servidor mas ainda existe localmente,
 * ela será detectada como duplicata. Para evitar isso, o app deve
 * sincronizar coletas do servidor antes de verificar duplicatas.
 */
```

### 2. **Método de Limpeza Adicionado**
```kotlin
/**
 * Remove coleta do banco local por ID
 * Usado quando uma coleta é excluída no servidor
 */
@Query("DELETE FROM coleta WHERE id = :id")
suspend fun deletarPorId(id: Long)
```

## 🔧 Soluções Recomendadas

### Solução 1: **Sincronização Antes de Verificar Duplicata** (Recomendada)

Modificar o fluxo de verificação para sempre consultar o servidor:

```kotlin
// VerificarDuplicataColetaUseCase.kt
suspend operator fun invoke(
    numeroPatrimonio: String,
    inventarioId: Int? = null
): Result<DuplicataResult> {
    return try {
        // SEMPRE consultar o servidor (não o banco local)
        val request = VerificarDuplicataRequest(numeroPatrimonio, inventarioId)
        val response = coletaApi.verificarDuplicataColeta(request)
        
        // Processar resposta...
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Vantagens:**
- ✅ Sempre tem dados atualizados do servidor
- ✅ Não depende de sincronização prévia
- ✅ Funciona mesmo se banco local estiver desatualizado

**Desvantagens:**
- ❌ Requer conexão com internet
- ❌ Mais lento (requisição HTTP)

### Solução 2: **Sincronizar Coletas Antes de Coletar**

Forçar sincronização de coletas do servidor antes de permitir nova coleta:

```kotlin
// ColetaViewModel.kt
fun prepararParaColeta(numeroPatrimonio: String) {
    viewModelScope.launch {
        // 1. Sincronizar coletas do servidor
        sincronizarColetasDoServidorUseCase()
        
        // 2. Verificar duplicata no banco local (agora atualizado)
        verificarDuplicataColetaUseCase(numeroPatrimonio)
    }
}
```

**Vantagens:**
- ✅ Banco local sempre atualizado
- ✅ Verificação rápida (local)
- ✅ Funciona offline após sincronização

**Desvantagens:**
- ❌ Requer sincronização prévia
- ❌ Mais complexo de implementar

### Solução 3: **Webhook de Exclusão** (Avançada)

Quando uma coleta é excluída no servidor, notificar todos os apps conectados:

```java
// MobileColetaService.java
public boolean excluirColeta(Long id, String username) throws SQLException {
    coletaDAO.excluirColeta(id.intValue());
    
    // Notificar apps via WebSocket
    webSocketService.notificarExclusaoColeta(id);
    
    return true;
}
```

**Vantagens:**
- ✅ Atualização em tempo real
- ✅ Não requer sincronização manual
- ✅ Melhor experiência do usuário

**Desvantagens:**
- ❌ Requer implementação de WebSocket
- ❌ Mais complexo
- ❌ Requer conexão constante

## 🚀 Solução Imediata (Workaround)

Até implementar uma das soluções acima, o usuário pode:

1. **Forçar Sincronização Manual**
   - Abrir tela de "Coletas"
   - Puxar para baixo (pull-to-refresh)
   - Aguardar sincronização completa
   - Tentar coletar novamente

2. **Limpar Cache do App**
   - Configurações do Android
   - Apps → SiHCP
   - Armazenamento → Limpar cache
   - Fazer login novamente

3. **Reinstalar o App**
   - Desinstalar app
   - Instalar versão mais recente
   - Fazer login
   - Sincronizar dados

## 📊 Impacto

- **Severidade:** MÉDIA
- **Frequência:** Ocasional (apenas quando coleta é excluída)
- **Usuários Afetados:** Todos que excluem e tentam recoletar
- **Workaround:** Sim (sincronização manual)

## 🎯 Recomendação Final

**Implementar Solução 1** (Verificação sempre no servidor):
- Mais simples de implementar
- Mais confiável
- Não depende de sincronização prévia
- Funciona em todos os cenários

**Código a modificar:**
1. `VerificarDuplicataColetaUseCase.kt` - Já consulta servidor ✅
2. `ColetaRepositoryImpl.kt` - Remover verificação local antes de registrar
3. `ScannerActivity.kt` - Sempre verificar no servidor antes de permitir coleta

---

**Data:** 22/05/2026  
**Versão:** 2.29.3  
**Status:** 🔴 Problema Identificado - Aguardando Implementação de Solução
