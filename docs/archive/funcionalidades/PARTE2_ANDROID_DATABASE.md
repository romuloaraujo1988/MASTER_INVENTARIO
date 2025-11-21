# 📱 Sistema de Sincronização Offline - Parte 2

## Android - Banco de Dados SQLite (Room)

### Entities (Tabelas)

```kotlin
// PatrimonioEntity.kt
@Entity(tableName = "patrimonios")
data class PatrimonioEntity(
    @PrimaryKey val id: Int,
    val numero: String,
    val descricao: String,
    val idSala: Int?,
    val nomeSala: String?,
    val idResponsavel: Int?,
    val nomeResponsavel: String?,
    val status: String?,
    val estadoConservacao: String?,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val sincronizado: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

// SalaEntity.kt
@Entity(tableName = "salas")
data class SalaEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val andar: String?,
    val bloco: String?,
    val idSetor: Int?,
    val nomeSetor: String?
)

// ResponsavelEntity.kt
@Entity(tableName = "responsaveis")
data class ResponsavelEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val cpf: String?,
    val matricula: String?,
    val email: String?,
    val telefone: String?
)

// ColetaOfflineEntity.kt
@Entity(tableName = "coletas_offline")
data class ColetaOfflineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numeroPatrimonio: String,
    val idPatrimonio: Int?,
    val idSala: Int,
    val idInventario: Int,
    val idUsuario: Int,
    val observacoes: String?,
    val estadoConservacao: String?,
    val dataColeta: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false
)

// SyncMetadataEntity.kt
@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val chave: String,
    val valor: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### DAOs (Data Access Objects)

```kotlin
// PatrimonioDao.kt
@Dao
interface PatrimonioDao {
    @Query("SELECT * FROM patrimonios")
    suspend fun getAll(): List<PatrimonioEntity>
    
    @Query("SELECT * FROM patrimonios WHERE numero = :numero LIMIT 1")
    suspend fun getByNumero(numero: String): PatrimonioEntity?
    
    @Query("SELECT * FROM patrimonios WHERE descricao LIKE '%' || :termo || '%'")
    suspend fun buscarPorDescricao(termo: String): List<PatrimonioEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patrimonios: List<PatrimonioEntity>)
    
    @Query("DELETE FROM patrimonios")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM patrimonios")
    suspend fun count(): Int
}

// SalaDao.kt
@Dao
interface SalaDao {
    @Query("SELECT * FROM salas ORDER BY nome")
    suspend fun getAll(): List<SalaEntity>
    
    @Query("SELECT * FROM salas WHERE id = :id")
    suspend fun getById(id: Int): SalaEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(salas: List<SalaEntity>)
    
    @Query("DELETE FROM salas")
    suspend fun deleteAll()
}

// ResponsavelDao.kt
@Dao
interface ResponsavelDao {
    @Query("SELECT * FROM responsaveis ORDER BY nome")
    suspend fun getAll(): List<ResponsavelEntity>
    
    @Query("SELECT * FROM responsaveis WHERE id = :id")
    suspend fun getById(id: Int): ResponsavelEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responsaveis: List<ResponsavelEntity>)
    
    @Query("DELETE FROM responsaveis")
    suspend fun deleteAll()
}

// ColetaOfflineDao.kt
@Dao
interface ColetaOfflineDao {
    @Query("SELECT * FROM coletas_offline WHERE sincronizado = 0")
    suspend fun getPendentes(): List<ColetaOfflineEntity>
    
    @Insert
    suspend fun insert(coleta: ColetaOfflineEntity): Long
    
    @Query("UPDATE coletas_offline SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizada(id: Long)
    
    @Query("DELETE FROM coletas_offline WHERE sincronizado = 1")
    suspend fun limparSincronizadas()
    
    @Query("SELECT COUNT(*) FROM coletas_offline WHERE sincronizado = 0")
    suspend fun countPendentes(): Int
}

// SyncMetadataDao.kt
@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata WHERE chave = :chave")
    suspend fun get(chave: String): SyncMetadataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: SyncMetadataEntity)
    
    @Query("DELETE FROM sync_metadata WHERE chave = :chave")
    suspend fun delete(chave: String)
}
```

### Database

```kotlin
// AppDatabase.kt
@Database(
    entities = [
        PatrimonioEntity::class,
        SalaEntity::class,
        ResponsavelEntity::class,
        ColetaOfflineEntity::class,
        SyncMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patrimonioDao(): PatrimonioDao
    abstract fun salaDao(): SalaDao
    abstract fun responsavelDao(): ResponsavelDao
    abstract fun coletaOfflineDao(): ColetaOfflineDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_offline.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## Continua na Parte 3...
