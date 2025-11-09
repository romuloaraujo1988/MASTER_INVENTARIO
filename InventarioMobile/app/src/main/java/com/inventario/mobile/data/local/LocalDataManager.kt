package com.inventario.mobile.data.local

import android.content.Context

/**
 * LocalDataManager stub - Mantido para compatibilidade temporária
 * TODO: Migrar para PreferencesManager + Room DAOs
 */
class LocalDataManager private constructor(context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: LocalDataManager? = null
        
        fun getInstance(context: Context): LocalDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalDataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Métodos stub - retornam valores padrão
    fun getToken(): String? = null
    fun saveToken(token: String) {}
    fun clearToken() {}
    fun getUserId(): Int = 0
    fun getUserName(): String? = null
    fun isUserLoggedIn(): Boolean = false
    
    suspend fun getCurrentUser(): com.inventario.mobile.data.model.Usuario? = null
    
    fun saveCurrentUser(usuario: com.inventario.mobile.data.model.Usuario) {}
    
    suspend fun saveCurrentUserSuspend(usuario: com.inventario.mobile.data.model.Usuario) {}
    
    fun clearSessionData() {}
}
