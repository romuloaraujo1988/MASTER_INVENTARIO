package com.inventario.mobile.presentation.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.domain.repository.AuthRepository
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.ServerConfigManager

/**
 * Factory stub - Mantido para compatibilidade
 * TODO: Remover após migração completa para Hilt
 */
class LoginViewModelFactory(
    private val application: Application,
    private val preferencesManager: PreferencesManager,
    private val serverConfigManager: ServerConfigManager,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                application.applicationContext,
                preferencesManager,
                serverConfigManager,
                authRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
