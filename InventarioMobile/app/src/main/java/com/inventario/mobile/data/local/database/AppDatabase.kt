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
        LogColetaEntity::class,  // v2.2: Log de auditoria
        HistoricoScanEntity::class,  // v2.11: Histórico de scans
        FotoReferenciaEntity::class  // v2.9: Fotos de referência por descrição
    ],
    version = 11,  // v2.9: Adicionada tabela de fotos de referência
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
    abstract fun dashboardDao(): DashboardDao  // v2.4: DAO reativo para estatísticas
    abstract fun historicoScanDao(): HistoricoScanDao  // v2.11: DAO de histórico de scans
    abstract fun fotoReferenciaDao(): FotoReferenciaDao  // v2.9: DAO de fotos de referência
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Migração da versão 7 para 8
         * Adiciona campos para item sem etiqueta na tabela coleta
         */
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Adicionar campos de item sem etiqueta
                database.execSQL("ALTER TABLE coleta ADD COLUMN semEtiqueta INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE coleta ADD COLUMN descricaoItemSemEtiqueta TEXT")
                database.execSQL("ALTER TABLE coleta ADD COLUMN categoriaItemSemEtiqueta TEXT")
                database.execSQL("ALTER TABLE coleta ADD COLUMN fotoPatrimonio TEXT")
                
                // Criar índice para semEtiqueta
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coleta_semEtiqueta ON coleta(semEtiqueta)")
            }
        }
        
        /**
         * Migração da versão 8 para 9
         * Adiciona campos para foto opcional otimizada na coleta
         * v2.11: Foto por exceção (divergência, estado ruim, atenção)
         */
        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Adicionar campos de foto otimizada
                database.execSQL("ALTER TABLE coleta ADD COLUMN fotoPath TEXT")
                database.execSQL("ALTER TABLE coleta ADD COLUMN fotoThumbnailPath TEXT")
                database.execSQL("ALTER TABLE coleta ADD COLUMN fotoSincronizada INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE coleta ADD COLUMN motivoFoto TEXT")
                
                // Criar índice para fotos pendentes de sincronização
                database.execSQL("CREATE INDEX IF NOT EXISTS index_coleta_fotoSincronizada ON coleta(fotoSincronizada)")
            }
        }
        
        /**
         * Migração da versão 9 para 10
         * Adiciona tabela de histórico de scans
         * v2.11: Histórico de patrimônios escaneados/consultados
         */
        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Criar tabela historico_scan
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS historico_scan (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        numeroPatrimonio TEXT NOT NULL,
                        descricao TEXT,
                        nomeSala TEXT,
                        tipoAcesso TEXT NOT NULL,
                        foiColetado INTEGER NOT NULL DEFAULT 0,
                        jaEstaColetado INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                // Criar índices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_historico_scan_numeroPatrimonio ON historico_scan(numeroPatrimonio)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_historico_scan_timestamp ON historico_scan(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_historico_scan_tipoAcesso ON historico_scan(tipoAcesso)")
            }
        }
        
        /**
         * Migração da versão 10 para 11
         * Adiciona tabela de fotos de referência por descrição
         * v2.9: Fotos de referência para exibição offline
         */
        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Criar tabela foto_referencia
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS foto_referencia (
                        id INTEGER PRIMARY KEY NOT NULL,
                        descricaoNormalizada TEXT NOT NULL,
                        imagemBlob BLOB,
                        hashImagem TEXT,
                        tamanhoBytes INTEGER NOT NULL DEFAULT 0,
                        dataAtualizacao INTEGER NOT NULL,
                        ativo INTEGER NOT NULL DEFAULT 1
                    )
                """)
                
                // Criar índices
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_foto_referencia_descricaoNormalizada ON foto_referencia(descricaoNormalizada)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_foto_referencia_dataAtualizacao ON foto_referencia(dataAtualizacao)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_foto_referencia_ativo ON foto_referencia(ativo)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_foto_referencia_ativo_dataAtualizacao ON foto_referencia(ativo, dataAtualizacao)")
            }
        }
        
        /**
         * Migração da versão 6 para 7
         * Corrige estrutura da tabela patrimonio (numeroSerie)
         */
        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Recriar tabela patrimonio com estrutura correta
                database.execSQL("DROP TABLE IF EXISTS patrimonio")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS patrimonio (
                        id INTEGER PRIMARY KEY NOT NULL,
                        numero TEXT NOT NULL,
                        numeroPatrimonio TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        marca TEXT,
                        modelo TEXT,
                        numeroSerie TEXT,
                        estado TEXT,
                        valor REAL,
                        setorId INTEGER,
                        setorNome TEXT,
                        idSala INTEGER,
                        nomeSala TEXT,
                        salaId INTEGER,
                        salaNome TEXT,
                        idResponsavel INTEGER,
                        nomeResponsavel TEXT,
                        responsavelId INTEGER,
                        responsavelNome TEXT,
                        status TEXT,
                        coletado INTEGER NOT NULL DEFAULT 0,
                        dataColeta INTEGER,
                        coletadoPor TEXT,
                        observacoesColeta TEXT,
                        observacoes TEXT,
                        dataUltimaAtualizacao INTEGER NOT NULL
                    )
                """)
                
                // Recriar índices
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_patrimonio_numero ON patrimonio(numero)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_patrimonio_numeroPatrimonio ON patrimonio(numeroPatrimonio)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_patrimonio_descricao ON patrimonio(descricao)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_patrimonio_idSala ON patrimonio(idSala)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_patrimonio_coletado ON patrimonio(coletado)")
            }
        }
        
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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)  // v2.9: Adicionar migração 10->11 (fotos referência)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
