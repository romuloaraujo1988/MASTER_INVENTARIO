package com.inventario.mobile.repository

import android.content.Context
import com.inventario.mobile.api.DispositivoApi
import com.inventario.mobile.model.DispositivoMobile
import com.inventario.mobile.model.DispositivoRegistroRequest
import com.inventario.mobile.utils.DeviceInfoHelper

/**
 * Repository para gerenciamento de dispositivos
 */
class DispositivoRepository(
    private val api: DispositivoApi,
    private val context: Context
) {
    
    /**
     * Registra o dispositivo atual
     */
    suspend fun registrarDispositivoAtual(idUsuario: Int): Result<DispositivoMobile> {
        return try {
            val deviceInfo = DeviceInfoHelper.getDeviceInfo(context)
            
            val request = DispositivoRegistroRequest(
                deviceId = deviceInfo.deviceId,
                idUsuario = idUsuario,
                modelo = deviceInfo.model,
                fabricante = deviceInfo.manufacturer,
                versaoAndroid = deviceInfo.androidVersion,
                versaoApp = deviceInfo.appVersion,
                enderecoIp = deviceInfo.ipAddress,
                enderecoMac = deviceInfo.macAddress
            )
            
            val response = api.registrarDispositivo(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val dispositivo = response.body()!!.data!!
                Result.success(dispositivo)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erro ao registrar dispositivo"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica status do dispositivo atual
     */
    suspend fun verificarStatusAtual(): Result<DispositivoMobile> {
        return try {
            val deviceInfo = DeviceInfoHelper.getDeviceInfo(context)
            val response = api.verificarStatus(deviceInfo.deviceId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val dispositivo = response.body()!!.data!!
                Result.success(dispositivo)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erro ao verificar status"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se dispositivo está autorizado
     */
    suspend fun verificarAutorizacao(): Result<Boolean> {
        return try {
            val deviceInfo = DeviceInfoHelper.getDeviceInfo(context)
            val response = api.verificarAutorizacao(deviceInfo.deviceId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val autorizado = response.body()!!.data ?: false
                Result.success(autorizado)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erro ao verificar autorização"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Registra sincronização
     */
    suspend fun registrarSincronizacao(idDispositivo: Int): Result<Unit> {
        return try {
            val response = api.registrarSincronizacao(idDispositivo)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erro ao registrar sincronização"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
