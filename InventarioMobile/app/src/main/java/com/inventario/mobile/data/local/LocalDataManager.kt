package com.inventario.mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * LocalDataManager - Gerencia dados locais do usuário
 * Usa SharedPreferences para persistência
 */
class LocalDataManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "inventario_local_data",
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        @Volatile
        private var INSTANCE: LocalDataManager? = null
        
        private const val KEY_USER_JSON = "user_json"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        
        fun getInstance(context: Context): LocalDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalDataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }
    
    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }
    
    fun clearToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply()
    }
    
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, 0)
    }
    
    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }
    
    fun isUserLoggedIn(): Boolean {
        return getToken() != null && getUserId() > 0
    }
    
    suspend fun getCurrentUser(): com.inventario.mobile.data.model.Usuario? {
        return try {
            val userJson = sharedPreferences.getString(KEY_USER_JSON, null)
            if (userJson != null) {
                gson.fromJson(userJson, com.inventario.mobile.data.model.Usuario::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("LocalDataManager", "Erro ao deserializar usuário", e)
            null
        }
    }
    
    fun saveCurrentUser(usuario: com.inventario.mobile.data.model.Usuario) {
        try {
            val userJson = gson.toJson(usuario)
            sharedPreferences.edit()
                .putString(KEY_USER_JSON, userJson)
                .putString(KEY_TOKEN, usuario.accessToken)
                .putInt(KEY_USER_ID, usuario.id.toInt())
                .putString(KEY_USER_NAME, usuario.nome)
                .apply()
            
            android.util.Log.d("LocalDataManager", "✓ Usuário salvo: ${usuario.nome} (ID: ${usuario.id})")
        } catch (e: Exception) {
            android.util.Log.e("LocalDataManager", "Erro ao salvar usuário", e)
        }
    }
    
    suspend fun saveCurrentUserSuspend(usuario: com.inventario.mobile.data.model.Usuario) {
        saveCurrentUser(usuario)
    }
    
    fun clearSessionData() {
        sharedPreferences.edit()
            .remove(KEY_USER_JSON)
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .apply()
        
        android.util.Log.d("LocalDataManager", "✓ Dados da sessão limpos")
    }
}
