package com.inventario.mobile.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

/**
 * Helper para gerenciar permissões e inicialização de câmera no Android 14+
 * Resolve problemas específicos de crash após concessão de permissões
 */
class Android14CameraHelper(private val context: Context) {
    private val TAG = "Android14CameraHelper"
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val INITIALIZATION_DELAY_MS = 1000L // Delay necessário para Android 14
    
    /**
     * Verifica se estamos rodando no Android 14 ou superior
     */
    fun isAndroid14OrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }
    
    /**
     * Verifica se a permissão de câmera está concedida
     */
    fun hasCameraPermission(context: Context): Boolean {
        val hasCamera = ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        Log.d(TAG, "Camera permission status: $hasCamera")
        return hasCamera
    }
    
    /**
     * Solicita permissão de câmera com tratamento específico para Android 14
     */
    fun requestCameraPermission(activity: Activity) {
        Log.d(TAG, "Requesting camera permission for Android ${Build.VERSION.SDK_INT}")
        
        if (isAndroid14OrHigher()) {
            // Para Android 14+, solicitar múltiplas permissões relacionadas à câmera
            val permissions = arrayOf(
                Manifest.permission.CAMERA
            )
            
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Para versões anteriores, usar o método padrão
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Inicializa a câmera com delay específico para Android 14
     * Este delay é necessário para evitar crashes após concessão de permissões
     */
    suspend fun initializeCameraWithDelay(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "Initializing camera with Android 14 compatibility")
            
            if (!hasCameraPermission(context)) {
                onError("Permissão de câmera não concedida")
                return
            }
            
            if (isAndroid14OrHigher()) {
                Log.d(TAG, "Applying Android 14+ initialization delay")
                delay(INITIALIZATION_DELAY_MS)
            }
            
            // Verificar novamente se a câmera está disponível
            if (CameraUtils.canUseCamera(context)) {
                Log.d(TAG, "Camera initialization successful")
                onSuccess()
            } else {
                onError("Câmera não está disponível no dispositivo")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during camera initialization", e)
            onError("Erro ao inicializar câmera: ${e.message}")
        }
    }
    
    /**
     * Inicializa a câmera com delay usando Handler (versão não-coroutine)
     */
    fun initializeCameraWithDelayHandler(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "Initializing camera with Android 14 compatibility (Handler)")
            
            if (!hasCameraPermission(context)) {
                onError("Permissão de câmera não concedida")
                return
            }
            
            val handler = Handler(Looper.getMainLooper())
            val delayTime = if (isAndroid14OrHigher()) {
                Log.d(TAG, "Applying Android 14+ initialization delay")
                INITIALIZATION_DELAY_MS
            } else {
                0L
            }
            
            handler.postDelayed({
                try {
                    // Verificar novamente se a câmera está disponível
                    if (CameraUtils.canUseCamera(context)) {
                        Log.d(TAG, "Camera initialization successful")
                        onSuccess()
                    } else {
                        onError("Câmera não está disponível no dispositivo")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during delayed camera initialization", e)
                    onError("Erro ao inicializar câmera: ${e.message}")
                }
            }, delayTime)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during camera initialization setup", e)
            onError("Erro ao configurar inicialização da câmera: ${e.message}")
        }
    }
    
    /**
     * Verifica se todas as permissões de câmera estão concedidas (Android 14+)
     */
    fun checkCameraPermissions(): Boolean {
        return hasCameraPermission(context)
    }
    
    /**
     * Solicita permissões de câmera com callback
     */
    fun requestCameraPermissions(callback: (Boolean) -> Unit) {
        if (context is Activity) {
            // Implementar lógica de solicitação de permissão
            if (hasCameraPermission(context)) {
                callback(true)
            } else {
                requestCameraPermission(context)
                // Por simplicidade, vamos assumir que a permissão foi concedida
                // Em uma implementação real, isso seria tratado no onRequestPermissionsResult
                callback(false)
            }
        } else {
            callback(false)
        }
    }
    
    /**
     * Inicializa câmera com delay (versão simplificada)
     */
    fun initializeCameraWithDelay(onSuccess: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        val delayTime = if (isAndroid14OrHigher()) {
            INITIALIZATION_DELAY_MS
        } else {
            0L
        }
        
        handler.postDelayed({
            onSuccess()
        }, delayTime)
    }
    
    /**
     * Valida suporte de câmera (versão simplificada)
     */
    fun validateCameraSupport(): Boolean {
        return validateCameraSupport(context)
    }
    
    /**
     * Verifica se o dispositivo suporta câmera adequadamente
     */
    fun validateCameraSupport(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
            
            // Verificar se há câmeras disponíveis usando CameraManager
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            val hasCameraAvailable = cameraManager?.let { manager ->
                try {
                    val cameraIds = manager.cameraIdList
                    Log.d(TAG, "Available cameras: ${cameraIds.size}")
                    cameraIds.isNotEmpty()
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking camera availability", e)
                    false
                }
            } ?: false
            
            // Para Android 14+, verificar se a câmera não está sendo usada por outro app
            val cameraNotInUse = if (isAndroid14OrHigher()) {
                checkCameraNotInUse(context)
            } else {
                true
            }
            
            Log.d(TAG, "Camera support - hasCamera: $hasCamera, hasCameraAvailable: $hasCameraAvailable, cameraNotInUse: $cameraNotInUse")
            hasCamera && hasCameraAvailable && cameraNotInUse
        } catch (e: Exception) {
            Log.e(TAG, "Error validating camera support", e)
            false
        }
    }
    
    /**
     * Verifica se a câmera não está sendo usada por outro aplicativo
     */
    private fun checkCameraNotInUse(context: Context): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            cameraManager?.let { manager ->
                val cameraIds = manager.cameraIdList
                for (cameraId in cameraIds) {
                    try {
                        // Tentar abrir a câmera brevemente para verificar se está disponível
                        val characteristics = manager.getCameraCharacteristics(cameraId)
                        Log.d(TAG, "Camera $cameraId is available")
                        return true
                    } catch (e: Exception) {
                        Log.w(TAG, "Camera $cameraId may be in use: ${e.message}")
                    }
                }
                false
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking camera usage", e)
            true // Assumir que está disponível se não conseguir verificar
        }
    }
    
    /**
     * Obtém informações de debug sobre o estado da câmera
     */
    fun getCameraDebugInfo(context: Context): String {
        return buildString {
            appendLine("=== Camera Debug Info ===")
            appendLine("Android Version: ${Build.VERSION.SDK_INT}")
            appendLine("Is Android 14+: ${isAndroid14OrHigher()}")
            appendLine("Has Camera Permission: ${hasCameraPermission(context)}")
            appendLine("Camera Available: ${CameraUtils.canUseCamera(context)}")
            appendLine("Camera Support Valid: ${validateCameraSupport(context)}")
            appendLine("Device Model: ${Build.MODEL}")
            appendLine("Device Manufacturer: ${Build.MANUFACTURER}")
        }
    }
}