package com.inventario.mobile.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.inventario.mobile.data.local.dao.*
import com.inventario.mobile.data.local.entity.*
import com.inventario.mobile.data.local.converters.Converters

@Database(
    entities = [
        UsuarioEntity::class,
        PatrimonioEntity::class,
        ColetaEntity::class,
        SalaEntity::class,
        SetorEntity::class
    ],
    version = 3, // Incrementado para adicionar servidorId em ColetaEntity
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InventarioDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun patrimonioDao(): PatrimonioDao
    abstract fun coletaDao(): ColetaDao
    abstract fun salaDao(): SalaDao
    abstract fun setorDao(): SetorDao

    companion object {
        @Volatile
        private var INSTANCE: InventarioDatabase? = null

        fun getDatabase(context: Context): InventarioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventarioDatabase::class.java,
                    "inventario_database"
                )
                .fallbackToDestructiveMigration() // Para desenvolvimento - remove em produção
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
