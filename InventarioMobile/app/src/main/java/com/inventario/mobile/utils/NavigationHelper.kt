package com.inventario.mobile.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.inventario.mobile.presentation.login.LoginActivity
import com.inventario.mobile.presentation.main.MainActivity

/**
 * Classe utilitária para navegação entre telas
 */
object NavigationHelper {
    
    /**
     * Navega para a tela inicial do aplicativo
     * Se o usuário estiver logado, vai para MainActivity
     * Se não estiver logado, vai para LoginActivity
     */
    fun navigateToHome(context: Context) {
        val preferencesManager = PreferencesManager(context)
        val isLoggedIn = preferencesManager.isLoggedIn()
        
        val intent = if (isLoggedIn) {
            Intent(context, MainActivity::class.java)
        } else {
            Intent(context, LoginActivity::class.java)
        }
        
        // Limpar stack de activities e criar nova task
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        context.startActivity(intent)
        
        // Se for uma Activity, finalizar a atual
        if (context is Activity) {
            context.finish()
        }
    }
    
    /**
     * Navega para MainActivity (tela principal)
     * Usado quando sabemos que o usuário está logado
     */
    fun navigateToMain(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        context.startActivity(intent)
        
        if (context is Activity) {
            context.finish()
        }
    }
    
    /**
     * Navega para LoginActivity
     * Usado para logout ou quando sabemos que o usuário não está logado
     */
    fun navigateToLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        context.startActivity(intent)
        
        if (context is Activity) {
            context.finish()
        }
    }
    
    /**
     * Volta para a Activity anterior
     */
    fun goBack(activity: Activity) {
        activity.onBackPressed()
    }
}
