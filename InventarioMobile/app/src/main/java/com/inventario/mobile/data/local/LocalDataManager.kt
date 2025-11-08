package com.inventario.mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inventario.mobile.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gerenciador de dados locais temporário usando SharedPreferences
 * TODO: Substituir por implementação Room quando problemas de compilação forem resolvidos
 */
class LocalDataManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "inventario_local_data"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_PATRIMONIOS = "patrimonios"
        private const val KEY_COLETAS = "coletas"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_NEEDS_SYNC = "needs_sync"
        
        @Volatile
        private var INSTANCE: LocalDataManager? = null
        
        fun getInstance(context: Context): LocalDataManager {
            return INSTANCE ?: synchronized(this) {
                val instance = LocalDataManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    // === Métodos de Usuário ===
    
    suspend fun getCurrentUser(): Usuario? = withContext(Dispatchers.IO) {
        val userJson = prefs.getString(KEY_CURRENT_USER, null)
        userJson?.let { gson.fromJson(it, Usuario::class.java) }
    }
    
    suspend fun saveCurrentUserSuspend(usuario: Usuario) = withContext(Dispatchers.IO) {
        val userJson = gson.toJson(usuario)
        prefs.edit().putString(KEY_CURRENT_USER, userJson).apply()
    }
    
    fun saveCurrentUser(usuario: Usuario) {
        val userJson = gson.toJson(usuario)
        prefs.edit().putString(KEY_CURRENT_USER, userJson).apply()
    }
    
    suspend fun clearSessionData() = withContext(Dispatchers.IO) {
        prefs.edit()
            .remove(KEY_CURRENT_USER)
            .apply()
    }
    
    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        getCurrentUser() != null
    }
    
    // === Métodos de Patrimônio ===
    
    suspend fun findPatrimonioByNumero(numero: String): Patrimonio? = withContext(Dispatchers.IO) {
        val patrimonios = getPatrimonios()
        patrimonios.find { it.numeroPatrimonio == numero }
    }
    
    suspend fun savePatrimonio(patrimonio: Patrimonio) = withContext(Dispatchers.IO) {
        val patrimonios = getPatrimonios().toMutableList()
        val existingIndex = patrimonios.indexOfFirst { it.id == patrimonio.id }
        
        if (existingIndex >= 0) {
            patrimonios[existingIndex] = patrimonio
        } else {
            patrimonios.add(patrimonio)
        }
        
        savePatrimonios(patrimonios)
    }
    
    suspend fun findPatrimonioByQrCode(qrCode: String): Patrimonio? = withContext(Dispatchers.IO) {
        val patrimonios = getPatrimonios()
        patrimonios.find { it.qrCode == qrCode }
    }
    
    suspend fun findPatrimonioById(id: Long): Patrimonio? = withContext(Dispatchers.IO) {
        val patrimonios = getPatrimonios()
        patrimonios.find { it.id == id }
    }
    
    suspend fun savePatrimonioCache(patrimonios: List<Patrimonio>) = withContext(Dispatchers.IO) {
        savePatrimonios(patrimonios)
    }
    
    private fun getPatrimonios(): List<Patrimonio> {
        val patrimoniosJson = prefs.getString(KEY_PATRIMONIOS, null) ?: return emptyList()
        val type = object : TypeToken<List<Patrimonio>>() {}.type
        return gson.fromJson(patrimoniosJson, type) ?: emptyList()
    }
    
    private fun savePatrimonios(patrimonios: List<Patrimonio>) {
        val patrimoniosJson = gson.toJson(patrimonios)
        prefs.edit().putString(KEY_PATRIMONIOS, patrimoniosJson).apply()
    }
    
    // === Métodos de Coleta ===
    
    suspend fun saveColeta(coleta: Coleta) = withContext(Dispatchers.IO) {
        val coletas = getColetas().toMutableList()
        val existingIndex = coletas.indexOfFirst { it.id == coleta.id }
        
        if (existingIndex >= 0) {
            coletas[existingIndex] = coleta
        } else {
            coletas.add(coleta)
        }
        
        saveColetas(coletas)
        markNeedsSync()
    }
    
    suspend fun getColetas(): List<Coleta> = withContext(Dispatchers.IO) {
        getColetasInternal()
    }
    
    suspend fun getColetasPendentes(): List<Coleta> = withContext(Dispatchers.IO) {
        getColetasInternal().filter { !it.sincronizado }
    }
    
    suspend fun isPatrimonioColetado(patrimonioId: Long): Boolean = withContext(Dispatchers.IO) {
        getColetasInternal().any { it.patrimonioId.toLong() == patrimonioId }
    }
    
    suspend fun removeColeta(patrimonioId: Long) = withContext(Dispatchers.IO) {
        val coletas = getColetasInternal().toMutableList()
        coletas.removeAll { it.patrimonioId.toLong() == patrimonioId }
        saveColetas(coletas)
        markNeedsSync()
    }
    
    private fun getColetasInternal(): List<Coleta> {
        val coletasJson = prefs.getString(KEY_COLETAS, null) ?: return emptyList()
        val type = object : TypeToken<List<Coleta>>() {}.type
        return gson.fromJson(coletasJson, type) ?: emptyList()
    }
    
    private fun saveColetas(coletas: List<Coleta>) {
        val coletasJson = gson.toJson(coletas)
        prefs.edit().putString(KEY_COLETAS, coletasJson).apply()
    }
    
    // === Métodos de Sincronização ===
    
    suspend fun saveLastSyncTime(timestamp: Long) = withContext(Dispatchers.IO) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }
    
    suspend fun getLastSyncTime(): String = withContext(Dispatchers.IO) {
        val timestamp = prefs.getLong(KEY_LAST_SYNC, 0)
        if (timestamp == 0L) {
            "Nunca sincronizado"
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
    
    suspend fun needsSync(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_NEEDS_SYNC, false) || getColetasPendentes().isNotEmpty()
    }
    
    private fun markNeedsSync() {
        prefs.edit().putBoolean(KEY_NEEDS_SYNC, true).apply()
    }
    
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
}