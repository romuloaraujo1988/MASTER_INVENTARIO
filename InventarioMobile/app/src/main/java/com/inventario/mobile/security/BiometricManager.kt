package com.inventario.mobile.security

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Gerenciador de autenticação biométrica
 * Suporta impressão digital, reconhecimento facial e outras biometrias
 */
class BiometricAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "BiometricAuthManager"
    }

    /**
     * Verifica se o dispositivo suporta biometria
     */
    fun isBiometricAvailable(): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Biometria disponível")
                BiometricAvailability.Available
            }
            
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d(TAG, "Sem hardware biométrico")
                BiometricAvailability.NoHardware
            }
            
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d(TAG, "Hardware biométrico indisponível")
                BiometricAvailability.HardwareUnavailable
            }
            
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d(TAG, "Nenhuma biometria cadastrada")
                BiometricAvailability.NoneEnrolled
            }
            
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.d(TAG, "Atualização de segurança necessária")
                BiometricAvailability.SecurityUpdateRequired
            }
            
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.d(TAG, "Biometria não suportada")
                BiometricAvailability.Unsupported
            }
            
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.d(TAG, "Status desconhecido")
                BiometricAvailability.Unknown
            }
            
            else -> {
                Log.d(TAG, "Erro desconhecido")
                BiometricAvailability.Unknown
            }
        }
    }

    /**
     * Autentica usando biometria
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Autenticação Biométrica",
        subtitle: String = "Use sua biometria para continuar",
        description: String = "Toque no sensor para autenticar",
        negativeButtonText: String = "Usar Senha",
        callback: BiometricCallback
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Erro de autenticação: $errorCode - $errString")
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            callback.onAuthenticationFailed("Usuário escolheu usar senha")
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            callback.onAuthenticationCanceled()
                        }
                        BiometricPrompt.ERROR_LOCKOUT -> {
                            callback.onAuthenticationLockout("Muitas tentativas. Tente novamente mais tarde.")
                        }
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            callback.onAuthenticationLockout("Biometria bloqueada permanentemente. Use senha.")
                        }
                        else -> {
                            callback.onAuthenticationError(errorCode, errString.toString())
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Autenticação bem-sucedida")
                    
                    val authenticationType = when (result.authenticationType) {
                        BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> "Biometria"
                        BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> "Credencial do Dispositivo"
                        else -> "Desconhecido"
                    }
                    
                    callback.onAuthenticationSucceeded(authenticationType)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Autenticação falhou")
                    callback.onAuthenticationFailed("Biometria não reconhecida")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Autentica com opção de cancelar (sem fallback para senha)
     */
    fun authenticateWithCancel(
        activity: FragmentActivity,
        title: String = "Autenticação Biométrica",
        subtitle: String = "Use sua biometria para continuar",
        description: String = "Toque no sensor para autenticar",
        callback: BiometricCallback
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            callback.onAuthenticationCanceled()
                        }
                        else -> {
                            callback.onAuthenticationError(errorCode, errString.toString())
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback.onAuthenticationSucceeded("Biometria")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.onAuthenticationFailed("Biometria não reconhecida")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Retorna o tipo de biometria disponível
     */
    fun getBiometricType(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+
            "Biometria (Impressão Digital / Facial)"
        } else {
            "Impressão Digital"
        }
    }
}

/**
 * Estados de disponibilidade da biometria
 */
sealed class BiometricAvailability {
    object Available : BiometricAvailability()
    object NoHardware : BiometricAvailability()
    object HardwareUnavailable : BiometricAvailability()
    object NoneEnrolled : BiometricAvailability()
    object SecurityUpdateRequired : BiometricAvailability()
    object Unsupported : BiometricAvailability()
    object Unknown : BiometricAvailability()
    
    fun isAvailable(): Boolean = this is Available
    
    fun getMessage(): String {
        return when (this) {
            is Available -> "Biometria disponível"
            is NoHardware -> "Este dispositivo não possui sensor biométrico"
            is HardwareUnavailable -> "Sensor biométrico temporariamente indisponível"
            is NoneEnrolled -> "Nenhuma biometria cadastrada. Configure nas configurações do dispositivo"
            is SecurityUpdateRequired -> "Atualização de segurança necessária"
            is Unsupported -> "Biometria não suportada neste dispositivo"
            is Unknown -> "Status de biometria desconhecido"
        }
    }
}

/**
 * Callback para resultados da autenticação
 */
interface BiometricCallback {
    fun onAuthenticationSucceeded(authenticationType: String)
    fun onAuthenticationFailed(message: String)
    fun onAuthenticationError(errorCode: Int, errorMessage: String)
    fun onAuthenticationCanceled()
    fun onAuthenticationLockout(message: String)
}
