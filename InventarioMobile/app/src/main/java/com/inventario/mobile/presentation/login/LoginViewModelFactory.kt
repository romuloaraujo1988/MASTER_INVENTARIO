package com.inventario.mobile.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.domain.repository.AuthRepository
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.ServerConfigManager

class LoginViewModelFactory(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val serverConfigManager: ServerConfigManager,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(context, preferencesManager, serverConfigManager, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}