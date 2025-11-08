package com.inventario.mobile.network

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.inventario.mobile.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

/**
 * Interceptor que adiciona informações do dispositivo em todas as requisições
 * Permite que o servidor rastreie dispositivos conectados
 */
class DeviceInfoInterceptor(private val context: Context) : Interceptor {

    companion object {
        private const val HEADER_DEVICE_ID = "X-Device-ID"
        private const val HEADER_USERNAME = "X-Username"
        private const val HEADER_DEVICE_MODEL = "X-Device-Model"
        private const val HEADER_ANDROID_VERSION = "X-Android-Version"
        private const val HEADER_APP_VERSION = "X-App-Version"
        private const val HEADER_DEVICE_MANUFACTURER = "X-Device-Manufacturer"
        
        private const val PREFS_NAME = "device_info"
        private const val KEY_DEVICE_ID = "device_id"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Obter informações do dispositivo
        val deviceId = getDeviceId()
        val username = getUsername()
        val deviceModel = getDeviceModel()
        val androidVersion = getAndroidVersion()
        val appVersion = getAppVersion()
        val manufacturer = getManufacturer()
        
        // Adicionar headers apenas se tiver username (usuário logado)
        val requestBuilder = originalRequest.newBuilder()
        
        if (username.isNotEmpty()) {
            requestBuilder
                .addHeader(HEADER_DEVICE_ID, deviceId)
                .addHeader(HEADER_USERNAME, username)
                .addHeader(HEADER_DEVICE_MODEL, deviceModel)
                .addHeader(HEADER_ANDROID_VERSION, androidVersion)
                .addHeader(HEADER_APP_VERSION, appVersion)
                .addHeader(HEADER_DEVICE_MANUFACTURER, manufacturer)
        }
        
        return chain.proceed(requestBuilder.build())
    }

    /**
     * Obtém ou gera um ID único para o dispositivo
     * Persiste em SharedPreferences para manter o mesmo ID
     */
    private fun getDeviceId(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        
        if (deviceId == null) {
            // Gerar novo UUID
            deviceId = UUID.randomUUID().toString()
            
            // Salvar para uso futuro
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        
        return deviceId
    }

    /**
     * Obtém o username do usuário logado
     */
    private fun getUsername(): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("username", "") ?: ""
    }

    /**
     * Obtém o modelo do dispositivo
     * Ex: "Samsung Galaxy S21", "Xiaomi Redmi Note 10"
     */
    private fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.capitalize()
        } else {
            "${manufacturer.capitalize()} $model"
        }
    }

    /**
     * Obtém a versão do Android
     * Ex: "13", "12", "11"
     */
    private fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    /**
     * Obtém a versão do aplicativo
     * Ex: "1.2.0"
     */
    private fun getAppVersion(): String {
        return BuildConfig.VERSION_NAME
    }

    /**
     * Obtém o fabricante do dispositivo
     * Ex: "Samsung", "Xiaomi", "Motorola"
     */
    private fun getManufacturer(): String {
        return Build.MANUFACTURER.capitalize()
    }

    /**
     * Capitaliza a primeira letra
     */
    private fun String.capitalize(): String {
        return this.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
}
