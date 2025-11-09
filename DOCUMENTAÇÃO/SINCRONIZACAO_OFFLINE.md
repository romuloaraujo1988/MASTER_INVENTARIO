# Sincronização Offline - Sistema de Inventário Mobile

## 🎯 Objetivo

Permitir que o app Android funcione completamente offline, sincronizando dados do PostgreSQL para o banco local SQLite quando houver conexão.

---

## 📦 Componentes Implementados

### 1. SyncRepository
**Localização**: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/SyncRepository.kt`

**Responsabilidades**:
- Sincronização forçada do servidor para banco local
- Verificação de dados locais
- Estatísticas do banco local
- Limpeza de dados locais

**Métodos Principais**:
```kotlin
// Força sincronização completa
suspend fun forceSyncFromServer(): Result<SyncResult>

// Verifica se há dados locais
suspend fun hasLocalData(): Boolean

// Obtém estatísticas locais
suspend fun getLocalStats(): Map<String, Int>

// Obtém última sincronização
suspend fun getLastSync(): SincronizacaoEntity?

// Limpa dados locais
suspend fun clearLocalData(): Result<Unit>
```

### 2. NetworkUtils
**Localização**: `InventarioMobile/app/src/main/java/com/inventario/mobile/utils/NetworkUtils.kt`

**Funcionalidades**:
- Verificar se há conexão com internet
- Detectar tipo de conexão (WiFi, Dados Móveis, Ethernet)
- Verificar conexão WiFi específica
- Verificar dados móveis

### 3. SyncActivity
**Localização**: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/sync/SyncActivity.kt`

**Interface para**:
- Visualizar status de conexão
- Ver estatísticas de dados locais
- Forçar sincronização
- Limpar dados locais
- Ver histórico de sincronizações

### 4. APIs
**PatrimonioApi**: `InventarioMobile/app/src/main/java/com/inventario/mobile/api/PatrimonioApi.kt`
**SalaApi**: `InventarioMobile/app/src/main/java/com/inventario/mobile/api/SalaApi.kt`

---

## 🔄 Fluxo de Sincronização

```
┌─────────────────────────────────────┐
│  Usuário abre SyncActivity          │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Verifica Status de Rede            │
│  - Online (WiFi/Dados/Ethernet)     │
│  - Offline                          │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Exibe Estatísticas Locais          │
│  - Total de Patrimônios             │
│  - Total de Salas                   │
│  - Coletados / Pendentes            │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Usuário clica "Sincronizar Agora"  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  SyncRepository.forceSyncFromServer()│
└──────────────┬──────────────────────┘
               │
               ├──→ 1. Sincroniza Salas
               │    - GET /api/mobile/salas
               │    - Limpa tabela local
               │    - Insere novas salas
               │
               ├──→ 2. Sincroniza Patrimônios
               │    - GET /api/mobile/patrimonios
               │    - Limpa tabela local
               │    - Insere novos patrimônios
               │
               └──→ 3. Registra Sincronização
                    - Salva em SincronizacaoEntity
                    - Atualiza timestamp
                    - Exibe resultado
```

---

## 🎨 Interface do Usuário

### SyncActivity - Tela de Sincronização

**Seções**:

1. **Status da Conexão**
   - ✓ Online (WiFi) - Verde
   - ✓ Online (Dados Móveis) - Verde
   - ✗ Offline - Vermelho

2. **Dados Locais**
   - Patrimônios (total)
   - Salas (total)
   - Coletados
   - Pendentes

3. **Última Sincronização**
   - Data e hora
   - Status (Sucesso/Falha)
   - Detalhes (quantidade sincronizada)

4. **Ações**
   - Botão "Sincronizar Agora" (desabilitado se offline)
   - Botão "Atualizar Estatísticas"
   - Botão "Limpar Dados Locais" (vermelho)

5. **Informações**
   - Dicas sobre funcionamento offline

---

## 💾 Banco de Dados Local (Room)

### Tabelas Existentes

#### PatrimonioEntity
```kotlin
@Entity(tableName = "patrimonios")
data class PatrimonioEntity(
    @PrimaryKey val id: Int,
    val numero: String,
    val descricao: String,
    val idSala: Int?,
    val nomeSala: String?,
    val estado: String?,
    val valor: Double?,
    val dataAquisicao: String?,
    val coletado: Boolean = false,
    val dataColeta: Long? = null
)
```

#### SalaEntity
```kotlin
@Entity(tableName = "salas")
data class SalaEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val descricao: String?,
    val andar: String?,
    val bloco: String?,
    val capacidade: Int?,
    val ativa: Boolean = true
)
```

#### SincronizacaoEntity
```kotlin
@Entity(tableName = "sincronizacoes")
data class SincronizacaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String, // FULL ou INCREMENTAL
    val dataHora: Date,
    val patrimoniosSincronizados: Int,
    val salasSincronizadas: Int,
    val sucesso: Boolean,
    val mensagem: String,
    val tempoDecorrido: Long
)
```

---

## 🚀 Como Usar

### 1. Acessar Tela de Sincronização

No app Android, adicionar item de menu ou botão para abrir:

```kotlin
val intent = Intent(this, SyncActivity::class.java)
startActivity(intent)
```

### 2. Sincronizar Dados

1. Abrir SyncActivity
2. Verificar se está online
3. Clicar em "Sincronizar Agora"
4. Aguardar conclusão
5. Ver estatísticas atualizadas

### 3. Usar Offline

Após sincronizar:
1. App detecta automaticamente quando está offline
2. Usa dados do banco local (SQLite)
3. Coletas são salvas localmente
4. Quando voltar online, sincroniza coletas pendentes

---

## 🔒 Segurança e Validações

### Validações Implementadas

✅ Verifica conexão antes de sincronizar
✅ Registra todas as sincronizações (sucesso/falha)
✅ Limpa dados antigos antes de inserir novos
✅ Tratamento de erros em cada etapa
✅ Logs detalhados para debug

### Tratamento de Erros

```kotlin
try {
    // Sincronizar salas
} catch (e: Exception) {
    Log.e(TAG, "Erro ao sincronizar salas", e)
    // Continua para patrimônios
}

try {
    // Sincronizar patrimônios
} catch (e: Exception) {
    Log.e(TAG, "Erro ao sincronizar patrimônios", e)
    throw e // Patrimônios são críticos
}
```

---

## 📊 Estatísticas e Monitoramento

### Dados Rastreados

- Total de patrimônios no banco local
- Total de salas no banco local
- Patrimônios coletados
- Patrimônios pendentes
- Data/hora da última sincronização
- Status da última sincronização
- Tempo de execução da sincronização

### Logs

```
═══════════════════════════════════════
INICIANDO SINCRONIZAÇÃO FORÇADA
═══════════════════════════════════════
1. Sincronizando salas...
✓ 25 salas sincronizadas
2. Sincronizando patrimônios...
✓ 150 patrimônios sincronizados
═══════════════════════════════════════
SINCRONIZAÇÃO CONCLUÍDA COM SUCESSO
Patrimônios: 150
Salas: 25
Tempo: 2500ms
═══════════════════════════════════════
```

---

## 🎯 Benefícios

### Para Usuários
✅ Funciona completamente offline
✅ Sincronização rápida e fácil
✅ Feedback visual claro
✅ Não perde dados coletados

### Para o Sistema
✅ Reduz carga no servidor
✅ Melhora performance do app
✅ Permite trabalho em áreas sem sinal
✅ Sincronização controlada pelo usuário

---

## 🔄 Próximas Melhorias (Opcional)

- [ ] Sincronização automática em background
- [ ] Sincronização incremental (apenas mudanças)
- [ ] Sincronização apenas via WiFi (opção)
- [ ] Notificações de sincronização
- [ ] Resolução de conflitos
- [ ] Compressão de dados
- [ ] Sincronização de imagens/anexos

---

## 📚 Referências

### Backend
- **Endpoints necessários**:
  - GET `/api/mobile/patrimonios` - Lista todos os patrimônios
  - GET `/api/mobile/salas` - Lista todas as salas

### Android
- **SyncRepository**: `data/repository/SyncRepository.kt`
- **NetworkUtils**: `utils/NetworkUtils.kt`
- **SyncActivity**: `presentation/sync/SyncActivity.kt`
- **Layout**: `res/layout/activity_sync.xml`
- **AppDatabase**: `data/local/database/AppDatabase.kt`

---

## ⚠️ Notas Importantes

1. **Primeira Sincronização**: Usuário DEVE sincronizar pelo menos uma vez antes de usar offline
2. **Limpeza de Dados**: Ao limpar dados locais, usuário precisa sincronizar novamente
3. **Coletas Offline**: São salvas localmente e sincronizadas quando voltar online
4. **Conexão**: App detecta automaticamente e usa banco apropriado

---

**Versão**: 1.0.0  
**Data**: 09/11/2025  
**Status**: ✅ Implementação Completa  
**Autor**: Sistema de Inventário
