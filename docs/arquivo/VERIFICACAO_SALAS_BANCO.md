# Verificação de Salas no Banco de Dados

## Resposta: SIM, o aplicativo carrega as salas do banco de dados

O aplicativo **SIM** carrega as salas que estão presentes no banco de dados local (SQLite/Room).

---

## Como Funciona

### 1. Fluxo de Carregamento de Salas

```
SalaSelectionActivity
    ↓
SalaSelectionViewModel.loadSalas()
    ↓
SalaRepository.getAllSalas()
    ↓
SalaDao.getAllSalas()
    ↓
Query: "SELECT * FROM sala ORDER BY nome ASC"
    ↓
Retorna Flow<List<Sala>>
```

### 2. Código Responsável

**SalaSelectionViewModel.kt:**
```kotlin
fun loadSalas() {
    viewModelScope.launch {
        salaRepository.getAllSalas().collect { salas ->
            Log.d(TAG, "loadSalas: ${salas.size} salas carregadas")
            _uiState.value = _uiState.value.copy(
                salas = salas,
                isLoading = false
            )
        }
    }
}
```

**SalaDao.kt:**
```kotlin
@Query("SELECT * FROM sala ORDER BY nome ASC")
fun getAllSalas(): Flow<List<Sala>>
```

---

## Como Verificar se Há Salas no Banco

### Opção 1: Via ADB (Android Debug Bridge)

```bash
# Conectar ao dispositivo
adb shell

# Acessar o banco de dados do app
cd /data/data/com.inventario.mobile/databases/

# Abrir o banco
sqlite3 inventario_database

# Listar todas as salas
SELECT * FROM sala;

# Contar salas
SELECT COUNT(*) FROM sala;

# Listar apenas salas ativas
SELECT * FROM sala WHERE ativo = 1;
```

### Opção 2: Via Android Studio Database Inspector

1. Abra o Android Studio
2. Execute o app no emulador ou dispositivo
3. Vá em: **View > Tool Windows > App Inspection**
4. Selecione a aba **Database Inspector**
5. Selecione o processo do app
6. Navegue até a tabela `sala`
7. Visualize os dados

### Opção 3: Via Logs do Aplicativo

Adicione logs no código para verificar:

```bash
# Filtrar logs do app
adb logcat | grep "SalaSelectionViewModel"

# Você verá algo como:
# D/SalaSelectionViewModel: loadSalas: 5 salas carregadas
```

---

## Como Popular o Banco com Salas

Se o banco estiver vazio, você precisa popular as salas. Existem 3 formas:

### Forma 1: Sincronização com o Servidor (Recomendado)

O aplicativo deve sincronizar com o servidor para baixar as salas:

```kotlin
// No código do app (já implementado)
salaRepository.sincronizarSalas()
```

**Endpoint esperado no servidor:**
```
GET /api/salas
```

**Resposta esperada:**
```json
[
  {
    "id": 1,
    "nome": "Laboratório de Informática 1",
    "codigo": "LAB-INFO-01",
    "descricao": "Laboratório com 30 computadores",
    "ativo": true,
    "setorId": 1,
    "dataCriacao": "2024-01-01T10:00:00",
    "dataAtualizacao": "2024-01-01T10:00:00"
  },
  {
    "id": 2,
    "nome": "Sala de Aula 101",
    "codigo": "SALA-101",
    "descricao": "Sala de aula com 40 lugares",
    "ativo": true,
    "setorId": 1,
    "dataCriacao": "2024-01-01T10:00:00",
    "dataAtualizacao": "2024-01-01T10:00:00"
  }
]
```

### Forma 2: Inserção Manual via SQL

```sql
-- Inserir salas de exemplo
INSERT INTO sala (nome, codigo, descricao, ativo, setorId, sincronizado, dataCriacao, dataAtualizacao) 
VALUES 
('Laboratório de Informática 1', 'LAB-INFO-01', 'Laboratório com 30 computadores', 1, 1, 0, datetime('now'), datetime('now')),
('Laboratório de Informática 2', 'LAB-INFO-02', 'Laboratório com 25 computadores', 1, 1, 0, datetime('now'), datetime('now')),
('Sala de Aula 101', 'SALA-101', 'Sala de aula com 40 lugares', 1, 1, 0, datetime('now'), datetime('now')),
('Sala de Aula 102', 'SALA-102', 'Sala de aula com 35 lugares', 1, 1, 0, datetime('now'), datetime('now')),
('Biblioteca', 'BIBLIOTECA', 'Biblioteca principal', 1, 2, 0, datetime('now'), datetime('now')),
('Auditório', 'AUDITORIO', 'Auditório com 200 lugares', 1, 2, 0, datetime('now'), datetime('now'));
```

### Forma 3: Criar Script de Inicialização

Crie um script no app para popular dados de teste:

```kotlin
// DatabaseInitializer.kt
object DatabaseInitializer {
    
    suspend fun initializeSalas(database: InventarioDatabase) {
        val salaDao = database.salaDao()
        
        // Verificar se já existem salas
        val count = salaDao.getAllSalas().first().size
        if (count > 0) {
            Log.d("DatabaseInitializer", "Banco já possui salas")
            return
        }
        
        // Criar salas de exemplo
        val salas = listOf(
            Sala(
                nome = "Laboratório de Informática 1",
                codigo = "LAB-INFO-01",
                descricao = "Laboratório com 30 computadores",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = Date(),
                dataAtualizacao = Date()
            ),
            Sala(
                nome = "Laboratório de Informática 2",
                codigo = "LAB-INFO-02",
                descricao = "Laboratório com 25 computadores",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = Date(),
                dataAtualizacao = Date()
            ),
            Sala(
                nome = "Sala de Aula 101",
                codigo = "SALA-101",
                descricao = "Sala de aula com 40 lugares",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = Date(),
                dataAtualizacao = Date()
            ),
            Sala(
                nome = "Sala de Aula 102",
                codigo = "SALA-102",
                descricao = "Sala de aula com 35 lugares",
                ativo = true,
                setorId = 1,
                sincronizado = false,
                dataCriacao = Date(),
                dataAtualizacao = Date()
            ),
            Sala(
                nome = "Biblioteca",
                codigo = "BIBLIOTECA",
                descricao = "Biblioteca principal",
                ativo = true,
                setorId = 2,
                sincronizado = false,
                dataCriacao = Date(),
                dataAtualizacao = Date()
            )
        )
        
        salaDao.insertSalas(salas)
        Log.d("DatabaseInitializer", "${salas.size} salas inseridas")
    }
}
```

**Chamar no Application ou SplashActivity:**
```kotlin
class InventarioMobileApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar banco com dados de teste
        lifecycleScope.launch {
            val database = InventarioDatabase.getDatabase(this@InventarioMobileApplication)
            DatabaseInitializer.initializeSalas(database)
        }
    }
}
```

---

## Estrutura da Tabela Sala

```sql
CREATE TABLE sala (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    nome TEXT NOT NULL,
    codigo TEXT NOT NULL,
    descricao TEXT,
    ativo INTEGER NOT NULL DEFAULT 1,
    setorId INTEGER NOT NULL,
    sincronizado INTEGER NOT NULL DEFAULT 0,
    dataCriacao INTEGER NOT NULL,
    dataAtualizacao INTEGER NOT NULL,
    servidorId INTEGER
);
```

**Campos:**
- `id`: ID local (autoincrement)
- `nome`: Nome da sala (ex: "Laboratório de Informática 1")
- `codigo`: Código único da sala (ex: "LAB-INFO-01")
- `descricao`: Descrição opcional
- `ativo`: 1 = ativa, 0 = inativa
- `setorId`: ID do setor ao qual pertence
- `sincronizado`: 1 = sincronizado com servidor, 0 = pendente
- `dataCriacao`: Timestamp de criação
- `dataAtualizacao`: Timestamp de última atualização
- `servidorId`: ID da sala no servidor (após sincronização)

---

## Problemas Comuns e Soluções

### Problema 1: Lista de Salas Vazia

**Sintomas:**
- Tela de seleção de sala mostra "Nenhuma sala encontrada"
- Log mostra: "0 salas carregadas"

**Causas:**
- Banco de dados vazio
- Sincronização não foi executada
- Dados não foram populados

**Soluções:**
1. Verificar se há salas no banco (ver seção "Como Verificar")
2. Executar sincronização com servidor
3. Popular banco manualmente (ver seção "Como Popular")

### Problema 2: Salas Não Aparecem Após Sincronização

**Sintomas:**
- Sincronização executada mas lista continua vazia
- Logs mostram sucesso mas sem dados

**Causas:**
- Servidor não retorna dados
- Erro no mapeamento DTO → Entity
- Problema na query do DAO

**Soluções:**
1. Verificar resposta do servidor
2. Verificar logs de erro
3. Testar query SQL diretamente

### Problema 3: Salas Duplicadas

**Sintomas:**
- Mesma sala aparece múltiplas vezes
- IDs diferentes para mesma sala

**Causas:**
- Sincronização executada múltiplas vezes
- Conflito entre ID local e servidor

**Soluções:**
1. Usar `OnConflictStrategy.REPLACE` no DAO (já implementado)
2. Limpar banco e sincronizar novamente
3. Implementar lógica de merge por código único

---

## Verificação Rápida

Execute este comando para verificar rapidamente:

```bash
# Verificar se há salas
adb shell "run-as com.inventario.mobile sqlite3 /data/data/com.inventario.mobile/databases/inventario_database 'SELECT COUNT(*) FROM sala;'"

# Listar salas
adb shell "run-as com.inventario.mobile sqlite3 /data/data/com.inventario.mobile/databases/inventario_database 'SELECT id, nome, ativo FROM sala;'"
```

---

## Próximos Passos

### Se o banco estiver vazio:

1. **Implementar sincronização com servidor:**
   - Criar endpoint `/api/salas` no backend
   - Testar sincronização no app
   - Verificar se dados foram salvos

2. **OU popular manualmente para testes:**
   - Usar script SQL de inserção
   - Criar DatabaseInitializer
   - Testar fluxo de coleta

3. **Verificar funcionamento:**
   - Abrir tela de seleção de sala
   - Verificar se salas aparecem
   - Testar seleção e navegação

---

## Logs Úteis para Debug

Adicione estes logs para debug:

```kotlin
// No SalaSelectionViewModel
fun loadSalas() {
    Log.d(TAG, "=== INICIANDO CARREGAMENTO DE SALAS ===")
    viewModelScope.launch {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            salaRepository.getAllSalas().collect { salas ->
                Log.d(TAG, "Salas carregadas: ${salas.size}")
                salas.forEachIndexed { index, sala ->
                    Log.d(TAG, "[$index] ID: ${sala.id}, Nome: ${sala.nome}, Ativo: ${sala.ativo}")
                }
                
                _uiState.value = _uiState.value.copy(
                    salas = salas,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERRO ao carregar salas", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Erro: ${e.message}"
            )
        }
    }
}
```

---

## Conclusão

✅ **O aplicativo CARREGA as salas do banco de dados local**

✅ **O fluxo está implementado corretamente**

⚠️ **Você precisa garantir que o banco tenha salas populadas**

**Opções:**
1. Sincronizar com servidor (produção)
2. Popular manualmente (desenvolvimento/testes)
3. Criar script de inicialização (desenvolvimento)

---

## Suporte

Para mais informações:
- Verifique os logs: `adb logcat | grep Sala`
- Inspecione o banco: Android Studio Database Inspector
- Consulte a documentação do Room: https://developer.android.com/training/data-storage/room
