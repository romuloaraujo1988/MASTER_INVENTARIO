package com.inventario.mobile.ui.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.inventario.mobile.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity base com verificação automática de sessão
 * Todas as activities protegidas devem herdar desta classe
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "BaseActivity"
    }
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    /**
     * Define se esta activity requer autenticação
     * Override para false em activities públicas (ex: LoginActivity)
     */
    protected open val requiresAuthentication: Boolean = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (requiresAuthentication) {
            Log.d(TAG, "Verificando sessão em ${this::class.simpleName}...")
            checkSession()
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        if (requiresAuthentication) {
            Log.d(TAG, "Verificando sessão (onResume) em ${this::class.simpleName}...")
            checkSession()
        }
    }
    
    /**
     * Verifica se sessão ainda é válida
     * Se inválida, faz logout automático
     */
    private fun checkSession() {
        if (!sessionManager.isSessionValid()) {
            Log.w(TAG, "⚠️ Sessão inválida detectada em ${this::class.simpleName}")
            sessionManager.logout(
                showMessage = true,
                message = "Sua sessão expirou. Por favor, faça login novamente."
            )
        }
    }
}
