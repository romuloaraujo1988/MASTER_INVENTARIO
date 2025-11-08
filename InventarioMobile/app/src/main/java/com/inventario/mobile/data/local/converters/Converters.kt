package com.inventario.mobile.data.local.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Converters para tipos de dados customizados no Room
 */
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}