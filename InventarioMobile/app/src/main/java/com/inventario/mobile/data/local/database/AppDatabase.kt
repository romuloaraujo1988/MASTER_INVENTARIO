package com.inventario.mobile.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.inventario.mobile.data.local.dao.*
import com.inventario.mobile.data.local.entity.*

/**
 * Banco de dados Room para modo offline
 * Armazena patrimônios, salas, responsáveis e coletas pendentes
 */
@Database(
    entities = [
        PatrimonioEntity::class,
        SalaEntity::class,
        ResponsavelEntity::class,
        ColetaEntity::class,
        SincronizacaoEntity::class,
        SyncLogEntity::class,
        LogColetaEntity::class  // v2.2: Log de auditoria
    ],
    version = 6,  // v2.2: Incrementado para incluir log_coleta
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun patrimonioDao(): PatrimonioDao
    abstract fun salaDao(): SalaDao
    abstract fun responsavelDao(): ResponsavelDao
    abstract fun coletaDao(): ColetaDao
    abstract fun sincronizacaoDao(): SincronizacaoDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun logColetaDao(): LogColetaDao  // v2.2: DAO de auditoria
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Migração da versão 5 para 6
         * Adiciona tabela de log de auditoria
         */
        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Criar tabela log_coleta
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS log_coleta (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        coletaId INTEGER NOT NULL,
                        acao TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        detalhes TEXT,
                        usuarioId INTEGER NOT NULL,
                        nomeUsuario TEXT NOT NULL,
                        deviceId TEXT,
                        appVersion TEXT,
                        tipoRede TEXT,
                        qualidadeRede TEXT,
                        sucesso INTEGER NOT NULL DEFAULT 1,
                        mensagemErro TEXT
                    )
                """)
                
                // Criar índices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_log_coleta_coletaId ON log_coleta(coletaId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_log_coleta_acao ON log_coleta(acao)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_log_coleta_timestamp ON log_coleta(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_log_coleta_usuarioId ON log_coleta(usuarioId)")
            }
        }
        
        /**
         * Migração da versão 4 para 5
         * Adiciona campos de métricas na tabela coleta
         */
        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Adicionar novos campos de métricas (com valores padrão para dados existentes)
                database.execSQL("ALTER TABLE coleta ADD COLUMN tempoColetaSegundos INTEGER")
                database.execSQL("ALTER TABLE coleta ADD COLUMN tempoScanSegundos INTEGER")
                database.execSQL("ALTER TABLE coleta ADD COLUMN tempoPreenchimentoSegundos INTEGER")
                database.execSQL("ALTER TABLE coleta ADD COLUMN metodoColeta TEXT")
                database.execSQL("ALTER TABLE coleta ADD COLUMN horaColeta INTEGER")
                database.execSQL("ALTER TABLE coleta ADD COLUMN diaSemana INTEGER")
                database.execSQL("ALTER TABLE coleta ADD COLUMN periodoColeta TEXT")
                database.execSQL("ALTER TABLE coleta ADD COLUMN tipoScan TEXT")
                database.execSQL("ALTER TABLE coleta ADD COLUMN tentativasScan INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE coleta ADD COLUMN errosScan INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE coleta ADD COLUMN qualidadeEtiqueta TEXT")
                
                // Criar índices para os novos campos
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coleta_metodoColeta ON coleta(metodoColeta)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coleta_tipoScan ON coleta(tipoScan)")
            }
        }
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_offline.db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)  // v2.2: Adicionar migração 5->6
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
