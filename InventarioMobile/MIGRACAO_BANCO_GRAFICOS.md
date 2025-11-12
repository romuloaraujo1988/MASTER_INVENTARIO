# 🔄 Migração do Banco de Dados - Suporte a Gráficos

## Alterações Necessárias

### 1. Tabela `coleta` - Adicionar campo `idInventario`

**Campo Adicionado**:
```kotlin
val idInventario: Int = 0  // ID do inventário ativo
```

**Índice Adicionado**:
```kotlin
Index(value = ["idInventario"])
```

---

## 📝 Script de Migração Room

### Opção 1: Migração Automática (Desenvolvimento)

Se estiver em desenvolvimento, pode simplesmente incrementar a versão do banco e usar `fallbackToDestructiveMigration()`:

```kotlin
// No DatabaseModule.kt
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "inventario_db"
    )
    .fallbackToDestructiveMigration() // ⚠️ Apaga dados! Apenas para dev
    .build()
}
```

### Opção 2: Migração Manual (Produção)

Para preservar dados em produção, criar migração manual:

```kotlin
// No AppDatabase.kt
@Database(
    entities = [
        PatrimonioEntity::class,
        ColetaEntity::class,
        SalaEntity::class,
        SetorEntity::class,
        ResponsavelEntity::class,
        UsuarioEntity::class,
        SincronizacaoEntity::class
    ],
    version = 2, // ← Incrementar versão
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    // DAOs...
}

// Criar migração
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Adicionar coluna idInventario com valor padrão 0
        database.execSQL(
            "ALTER TABLE coleta ADD COLUMN idInventario INTEGER NOT NULL DEFAULT 0"
        )
        
        // Criar índice para melhor performance
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_coleta_idInventario ON coleta(idInventario)"
        )
    }
}

// No DatabaseModule.kt
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "inventario_db"
    )
    .addMigrations(MIGRATION_1_2) // ← Adicionar migração
    .build()
}
```

---

## 🔍 Verificação Pós-Migração

### 1. Verificar Estrutura da Tabela

```kotlin
// Query para verificar estrutura
val cursor = database.query("PRAGMA table_info(coleta)")
while (cursor.moveToNext()) {
    val columnName = cursor.getString(cursor.getColumnIndex("name"))
    val columnType = cursor.getString(cursor.getColumnIndex("type"))
    Log.d("Migration", "Column: $columnName, Type: $columnType")
}
```

### 2. Verificar Índices

```kotlin
// Query para verificar índices
val cursor = database.query("PRAGMA index_list(coleta)")
while (cursor.moveToNext()) {
    val indexName = cursor.getString(cursor.getColumnIndex("name"))
    Log.d("Migration", "Index: $indexName")
}
```

### 3. Testar Queries de Gráficos

```kotlin
// Testar query de evolução
val evolution = coletaDao.getEvolutionData(idInventario = 1)
Log.d("Migration", "Evolution data: $evolution")

// Testar query de top itens
val topItems = coletaDao.getTopItems(idInventario = 1)
Log.d("Migration", "Top items: $topItems")
```

---

## ⚠️ Importante

### Para Desenvolvimento
- Use `fallbackToDestructiveMigration()` para limpar e recriar o banco
- Dados serão perdidos, mas é mais rápido para testes

### Para Produção
- **SEMPRE** use migrações manuais
- Teste a migração em ambiente de staging primeiro
- Faça backup dos dados antes de atualizar

---

## 🚀 Passos para Aplicar

1. **Atualizar versão do banco**:
   ```kotlin
   version = 2 // Era 1, agora é 2
   ```

2. **Escolher estratégia**:
   - Dev: `fallbackToDestructiveMigration()`
   - Prod: Criar `MIGRATION_1_2`

3. **Limpar e recompilar**:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

4. **Desinstalar app antigo** (se usar fallback):
   ```bash
   adb uninstall com.inventario.mobile
   ```

5. **Instalar nova versão**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

6. **Testar gráficos**:
   - Abrir ChartsFragment
   - Verificar se dados aparecem
   - Verificar logs no Logcat

---

## 📊 Impacto nas Queries

### Queries Afetadas

Todas as queries que usam `idInventario` agora funcionarão corretamente:

```kotlin
// ✅ Agora funciona
coletaDao.countByInventario(idInventario)
coletaDao.getEvolutionData(idInventario)
coletaDao.getTopItems(idInventario)
```

### Queries Não Afetadas

Queries que não usam `idInventario` continuam funcionando normalmente:

```kotlin
// ✅ Continua funcionando
coletaDao.buscarPendentes()
coletaDao.contarTodas()
patrimonioDao.countAll()
```

---

## 🐛 Troubleshooting

### Erro: "no such column: idInventario"
**Solução**: Migração não foi aplicada. Desinstale o app e reinstale.

### Erro: "table coleta has no column named idInventario"
**Solução**: Versão do banco não foi incrementada. Verifique `@Database(version = 2)`.

### Dados antigos não aparecem
**Solução**: Se usou `fallbackToDestructiveMigration()`, dados foram apagados. Use migração manual.

### App crasha ao abrir
**Solução**: Verifique Logcat para erro de migração. Pode ser necessário desinstalar app.

---

**Versão**: 1.0.0  
**Data**: 11/11/2025  
**Tipo**: Migração de Schema
