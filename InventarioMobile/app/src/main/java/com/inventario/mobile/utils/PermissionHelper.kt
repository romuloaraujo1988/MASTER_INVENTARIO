package com.inventario.mobile.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    const val REQUEST_CAMERA_PERMISSION = 100
    const val REQUEST_LOCATION_PERMISSION = 101
    const val REQUEST_ALL_PERMISSIONS = 102
    
    // Permissões necessárias
    val CAMERA_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )
    
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    val ALL_PERMISSIONS = CAMERA_PERMISSIONS + LOCATION_PERMISSIONS
    
    /**
     * Verifica se a permissão de câmera foi concedida
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Verifica se as permissões de localização foram concedidas
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Verifica se todas as permissões foram concedidas
     */
    fun hasAllPermissions(context: Context): Boolean {
        return hasCameraPermission(context) && hasLocationPermission(context)
    }
    
    /**
     * Solicita permissão de câmera
     */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            CAMERA_PERMISSIONS,
            REQUEST_CAMERA_PERMISSION
        )
    }
    
    /**
     * Solicita permissões de localização
     */
    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_PERMISSIONS,
            REQUEST_LOCATION_PERMISSION
        )
    }
    
    /**
     * Solicita todas as permissões
     */
    fun requestAllPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            ALL_PERMISSIONS,
            REQUEST_ALL_PERMISSIONS
        )
    }
    
    /**
     * Verifica se o usuário negou permanentemente a permissão
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Processa o resultado da solicitação de permissões
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION,
            REQUEST_LOCATION_PERMISSION,
            REQUEST_ALL_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && 
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onGranted()
                } else {
                    onDenied()
                }
            }
        }
    }
}
