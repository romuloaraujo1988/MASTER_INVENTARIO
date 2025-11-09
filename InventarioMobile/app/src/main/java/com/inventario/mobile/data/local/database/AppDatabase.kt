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
        SincronizacaoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun patrimonioDao(): PatrimonioDao
    abstract fun salaDao(): SalaDao
    abstract fun responsavelDao(): ResponsavelDao
    abstract fun coletaDao(): ColetaDao
    abstract fun sincronizacaoDao(): SincronizacaoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
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
