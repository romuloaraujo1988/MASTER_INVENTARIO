package com.inventario.mobile.presentation.login

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.inventario.mobile.security.BiometricAuthManager
import com.inventario.mobile.security.BiometricAvailability
import com.inventario.mobile.security.BiometricCallback
import com.inventario.mobile.security.SecureStorage

/**
 * Helper para integrar biometria no login
 */
class BiometricLoginHelper(
    private val context: Context,
    private val activity: FragmentActivity
) {
    private val biometricManager = BiometricAuthManager(context)
    private val secureStorage = SecureStorage.getInstance(context)

    companion object {
        private const val TAG = "BiometricLoginHelper"
    }

    /**
     * Verifica se pode usar biometria para login
     */
    fun canUseBiometricLogin(): BiometricLoginStatus {
        // Verificar se biometria está disponível no dispositivo
        val availability = biometricManager.isBiometricAvailable()
        if (!availability.isAvailable()) {
            return BiometricLoginStatus.Unavailable(availability.getMessage())
        }

        // Verificar se usuário habilitou biometria
        if (!secureStorage.isBiometricEnabled()) {
            return BiometricLoginStatus.NotConfigured
        }

        // Verificar se tem credenciais salvas
        if (!secureStorage.canUseBiometric()) {
            return BiometricLoginStatus.NoCredentials
        }

        return BiometricLoginStatus.Available
    }

    /**
     * Realiza login com biometria
     */
    fun loginWithBiometric(callback: BiometricLoginCallback) {
        val status = canUseBiometricLogin()
        
        if (status !is BiometricLoginStatus.Available) {
            callback.onBiometricLoginError(status.getMessage())
            return
        }

        biometricManager.authenticate(
            activity = activity,
            title = "Login com Biometria",
            subtitle = "Use sua biometria para fazer login",
            description = "Autentique-se para acessar o sistema",
            negativeButtonText = "Usar Senha",
            callback = object : BiometricCallback {
                override fun onAuthenticationSucceeded(authenticationType: String) {
                    Log.d(TAG, "Autenticação biométrica bem-sucedida")
                    
                    // Recuperar credenciais
                    val username = secureStorage.getUsername()
                    val password = secureStorage.getEncryptedPassword()
                    
                    if (username != null && password != null) {
                        callback.onBiometricLoginSuccess(username, password)
                    } else {
                        callback.onBiometricLoginError("Credenciais não encontradas")
                    }
                }

                override fun onAuthenticationFailed(message: String) {
                    Log.w(TAG, "Autenticação falhou: $message")
                    callback.onBiometricLoginFailed(message)
                }

                override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                    Log.e(TAG, "Erro de autenticação: $errorCode - $errorMessage")
                    callback.onBiometricLoginError(errorMessage)
                }

                override fun onAuthenticationCanceled() {
                    Log.d(TAG, "Autenticação cancelada")
                    callback.onBiometricLoginCanceled()
                }

                override fun onAuthenticationLockout(message: String) {
                    Log.w(TAG, "Bloqueio de autenticação: $message")
                    callback.onBiometricLoginError(message)
                }
            }
        )
    }

    /**
     * Configura biometria após login bem-sucedido
     */
    fun setupBiometricAfterLogin(
        username: String,
        password: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val availability = biometricManager.isBiometricAvailable()
        
        if (!availability.isAvailable()) {
            callback(false, availability.getMessage())
            return
        }

        // Autenticar para confirmar que o usuário quer habilitar
        biometricManager.authenticateWithCancel(
            activity = activity,
            title = "Habilitar Biometria",
            subtitle = "Confirme sua biometria",
            description = "Autentique-se para habilitar login biométrico",
            callback = object : BiometricCallback {
                override fun onAuthenticationSucceeded(authenticationType: String) {
                    // Salvar credenciais de forma segura
                    secureStorage.saveUsername(username)
                    secureStorage.saveEncryptedPassword(password)
                    secureStorage.setBiometricEnabled(true)
                    
                    Log.d(TAG, "Biometria configurada com sucesso")
                    callback(true, "Biometria habilitada com sucesso!")
                }

                override fun onAuthenticationFailed(message: String) {
                    callback(false, "Falha na autenticação: $message")
                }

                override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                    callback(false, errorMessage)
                }

                override fun onAuthenticationCanceled() {
                    callback(false, "Configuração cancelada")
                }

                override fun onAuthenticationLockout(message: String) {
                    callback(false, message)
                }
            }
        )
    }

    /**
     * Desabilita biometria
     */
    fun disableBiometric() {
        secureStorage.setBiometricEnabled(false)
        secureStorage.clearEncryptedPassword()
        Log.d(TAG, "Biometria desabilitada")
    }

    /**
     * Retorna informações sobre biometria
     */
    fun getBiometricInfo(): String {
        return biometricManager.getBiometricType()
    }
}

/**
 * Status do login biométrico
 */
sealed class BiometricLoginStatus {
    object Available : BiometricLoginStatus()
    object NotConfigured : BiometricLoginStatus()
    object NoCredentials : BiometricLoginStatus()
    data class Unavailable(val reason: String) : BiometricLoginStatus()
    
    fun getMessage(): String {
        return when (this) {
            is Available -> "Biometria disponível"
            is NotConfigured -> "Biometria não configurada. Configure nas configurações."
            is NoCredentials -> "Credenciais não encontradas"
            is Unavailable -> reason
        }
    }
}

/**
 * Callback para login biométrico
 */
interface BiometricLoginCallback {
    fun onBiometricLoginSuccess(username: String, password: String)
    fun onBiometricLoginFailed(message: String)
    fun onBiometricLoginError(message: String)
    fun onBiometricLoginCanceled()
}
