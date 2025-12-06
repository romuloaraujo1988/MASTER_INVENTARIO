package com.inventario.mobile.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Helper para gerenciar o tema do aplicativo (Dark Mode)
 * 
 * Uso:
 * - No Application.onCreate(): ThemeHelper.applyTheme(context)
 * - Para mudar tema: ThemeHelper.setTheme(context, mode)
 */
object ThemeHelper {
    
    private const val TAG = "ThemeHelper"
    
    /**
     * Aplica o tema salvo nas preferências
     * Deve ser chamado no Application.onCreate() antes de setContentView()
     */
    fun applyTheme(context: Context) {
        val preferencesManager = PreferencesManager(context)
        val mode = preferencesManager.getThemeMode()
        applyThemeMode(mode)
        android.util.Log.d(TAG, "Tema aplicado na inicialização: ${preferencesManager.getThemeModeName(mode)}")
    }
    
    /**
     * Define e aplica um novo tema
     */
    fun setTheme(context: Context, mode: Int) {
        val preferencesManager = PreferencesManager(context)
        preferencesManager.setThemeMode(mode)
        applyThemeMode(mode)
        android.util.Log.d(TAG, "Tema alterado para: ${preferencesManager.getThemeModeName(mode)}")
    }
    
    /**
     * Alterna entre Light e Dark mode
     */
    fun toggleDarkMode(context: Context) {
        val preferencesManager = PreferencesManager(context)
        val currentMode = preferencesManager.getThemeMode()
        val newMode = if (currentMode == PreferencesManager.THEME_MODE_DARK) {
            PreferencesManager.THEME_MODE_LIGHT
        } else {
            PreferencesManager.THEME_MODE_DARK
        }
        setTheme(context, newMode)
    }
    
    /**
     * Verifica se o tema atual é escuro
     */
    fun isDarkMode(context: Context): Boolean {
        val preferencesManager = PreferencesManager(context)
        return when (preferencesManager.getThemeMode()) {
            PreferencesManager.THEME_MODE_DARK -> true
            PreferencesManager.THEME_MODE_LIGHT -> false
            else -> {
                // Seguir sistema - verificar configuração atual
                val nightModeFlags = context.resources.configuration.uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    /**
     * Aplica o modo de tema no AppCompatDelegate
     */
    private fun applyThemeMode(mode: Int) {
        val nightMode = when (mode) {
            PreferencesManager.THEME_MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            PreferencesManager.THEME_MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
    
    /**
     * Obtém o nome do tema atual
     */
    fun getCurrentThemeName(context: Context): String {
        val preferencesManager = PreferencesManager(context)
        return preferencesManager.getThemeModeName()
    }
    
    /**
     * Obtém o modo de tema atual
     */
    fun getCurrentThemeMode(context: Context): Int {
        val preferencesManager = PreferencesManager(context)
        return preferencesManager.getThemeMode()
    }
}
