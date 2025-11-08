package com.inventario.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.inventario.mobile.data.local.entity.PlaceholderEntity

@Dao
interface PlaceholderDao {
    @Query("SELECT * FROM placeholder LIMIT 1")
    suspend fun getPlaceholder(): PlaceholderEntity?
}