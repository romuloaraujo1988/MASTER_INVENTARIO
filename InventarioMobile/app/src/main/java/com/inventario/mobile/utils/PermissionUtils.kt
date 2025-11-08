package com.inventario.mobile.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Utilitários para gerenciamento de permissões
 */
object PermissionUtils {
    
    // Códigos de requisição de permissões
    const val REQUEST_CAMERA_PERMISSION = 1001
    const val REQUEST_STORAGE_PERMISSION = 1002
    const val REQUEST_LOCATION_PERMISSION = 1003
    const val REQUEST_MULTIPLE_PERMISSIONS = 1004
    
    // Permissões necessárias para o app
    val CAMERA_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )
    
    val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    val ALL_PERMISSIONS = CAMERA_PERMISSIONS + STORAGE_PERMISSIONS + LOCATION_PERMISSIONS
    
    /**
     * Verifica se uma permissão específica foi concedida
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Verifica se todas as permissões de um array foram concedidas
     */
    fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { isPermissionGranted(context, it) }
    }
    
    /**
     * Verifica se a permissão de câmera foi concedida
     */
    fun isCameraPermissionGranted(context: Context): Boolean {
        return arePermissionsGranted(context, CAMERA_PERMISSIONS)
    }
    
    /**
     * Verifica se as permissões de armazenamento foram concedidas
     */
    fun isStoragePermissionGranted(context: Context): Boolean {
        return arePermissionsGranted(context, STORAGE_PERMISSIONS)
    }
    
    /**
     * Verifica se as permissões de localização foram concedidas
     */
    fun isLocationPermissionGranted(context: Context): Boolean {
        return arePermissionsGranted(context, LOCATION_PERMISSIONS)
    }
    
    /**
     * Verifica se todas as permissões necessárias foram concedidas
     */
    fun areAllPermissionsGranted(context: Context): Boolean {
        return arePermissionsGranted(context, ALL_PERMISSIONS)
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
     * Solicita permissão de câmera (Fragment)
     */
    fun requestCameraPermission(fragment: Fragment) {
        fragment.requestPermissions(
            CAMERA_PERMISSIONS,
            REQUEST_CAMERA_PERMISSION
        )
    }
    
    /**
     * Solicita permissões de armazenamento
     */
    fun requestStoragePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            STORAGE_PERMISSIONS,
            REQUEST_STORAGE_PERMISSION
        )
    }
    
    /**
     * Solicita permissões de armazenamento (Fragment)
     */
    fun requestStoragePermission(fragment: Fragment) {
        fragment.requestPermissions(
            STORAGE_PERMISSIONS,
            REQUEST_STORAGE_PERMISSION
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
     * Solicita permissões de localização (Fragment)
     */
    fun requestLocationPermission(fragment: Fragment) {
        fragment.requestPermissions(
            LOCATION_PERMISSIONS,
            REQUEST_LOCATION_PERMISSION
        )
    }
    
    /**
     * Solicita múltiplas permissões
     */
    fun requestMultiplePermissions(activity: Activity, permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            REQUEST_MULTIPLE_PERMISSIONS
        )
    }
    
    /**
     * Solicita múltiplas permissões (Fragment)
     */
    fun requestMultiplePermissions(fragment: Fragment, permissions: Array<String>) {
        fragment.requestPermissions(
            permissions,
            REQUEST_MULTIPLE_PERMISSIONS
        )
    }
    
    /**
     * Solicita todas as permissões necessárias
     */
    fun requestAllPermissions(activity: Activity) {
        requestMultiplePermissions(activity, ALL_PERMISSIONS)
    }
    
    /**
     * Solicita todas as permissões necessárias (Fragment)
     */
    fun requestAllPermissions(fragment: Fragment) {
        requestMultiplePermissions(fragment, ALL_PERMISSIONS)
    }
    
    /**
     * Verifica se deve mostrar explicação para a permissão
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Verifica se deve mostrar explicação para a permissão (Fragment)
     */
    fun shouldShowRequestPermissionRationale(fragment: Fragment, permission: String): Boolean {
        return fragment.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * Verifica se alguma permissão foi negada permanentemente
     */
    fun hasPermissionBeenDeniedPermanently(activity: Activity, permission: String): Boolean {
        return !isPermissionGranted(activity, permission) && 
                !shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Verifica se alguma permissão foi negada permanentemente (Fragment)
     */
    fun hasPermissionBeenDeniedPermanently(fragment: Fragment, permission: String): Boolean {
        return !isPermissionGranted(fragment.requireContext(), permission) && 
                !shouldShowRequestPermissionRationale(fragment, permission)
    }
    
    /**
     * Obtém lista de permissões negadas
     */
    fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { !isPermissionGranted(context, it) }
    }
    
    /**
     * Obtém lista de permissões concedidas
     */
    fun getGrantedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { isPermissionGranted(context, it) }
    }
    
    /**
     * Processa resultado de solicitação de permissões
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: (deniedPermissions: List<String>) -> Unit,
        onPermanentlyDenied: (permanentlyDeniedPermissions: List<String>) -> Unit = {}
    ) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION,
            REQUEST_STORAGE_PERMISSION,
            REQUEST_LOCATION_PERMISSION,
            REQUEST_MULTIPLE_PERMISSIONS -> {
                val deniedPermissions = mutableListOf<String>()
                val permanentlyDeniedPermissions = mutableListOf<String>()
                
                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i])
                    }
                }
                
                if (deniedPermissions.isEmpty()) {
                    onGranted()
                } else {
                    onDenied(deniedPermissions)
                    if (permanentlyDeniedPermissions.isNotEmpty()) {
                        onPermanentlyDenied(permanentlyDeniedPermissions)
                    }
                }
            }
        }
    }
    
    /**
     * Abre configurações do app para permissões
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * Obtém nome amigável da permissão
     */
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> "Câmera"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Armazenamento (Leitura)"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Armazenamento (Escrita)"
            Manifest.permission.READ_MEDIA_IMAGES -> "Imagens"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Localização Precisa"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Localização Aproximada"
            Manifest.permission.RECORD_AUDIO -> "Microfone"
            Manifest.permission.READ_PHONE_STATE -> "Estado do Telefone"
            Manifest.permission.CALL_PHONE -> "Fazer Chamadas"
            Manifest.permission.SEND_SMS -> "Enviar SMS"
            Manifest.permission.READ_CONTACTS -> "Contatos"
            Manifest.permission.WRITE_CONTACTS -> "Editar Contatos"
            Manifest.permission.READ_CALENDAR -> "Calendário"
            Manifest.permission.WRITE_CALENDAR -> "Editar Calendário"
            else -> permission.substringAfterLast(".")
        }
    }
    
    /**
     * Obtém descrição da permissão
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> 
                "Necessária para capturar fotos dos patrimônios durante a coleta"
            Manifest.permission.READ_EXTERNAL_STORAGE -> 
                "Necessária para acessar e salvar fotos no dispositivo"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> 
                "Necessária para salvar fotos e dados no dispositivo"
            Manifest.permission.READ_MEDIA_IMAGES -> 
                "Necessária para acessar imagens salvas no dispositivo"
            Manifest.permission.ACCESS_FINE_LOCATION -> 
                "Necessária para registrar a localização precisa durante a coleta"
            Manifest.permission.ACCESS_COARSE_LOCATION -> 
                "Necessária para registrar a localização aproximada durante a coleta"
            else -> "Permissão necessária para o funcionamento do aplicativo"
        }
    }
    
    /**
     * Verifica se a permissão é crítica para o app
     */
    fun isCriticalPermission(permission: String): Boolean {
        return permission in arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    }
    
    /**
     * Verifica se a permissão é opcional
     */
    fun isOptionalPermission(permission: String): Boolean {
        return permission in arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    /**
     * Obtém permissões críticas não concedidas
     */
    fun getCriticalPermissionsDenied(context: Context): List<String> {
        return ALL_PERMISSIONS.filter { 
            isCriticalPermission(it) && !isPermissionGranted(context, it) 
        }
    }
    
    /**
     * Obtém permissões opcionais não concedidas
     */
    fun getOptionalPermissionsDenied(context: Context): List<String> {
        return ALL_PERMISSIONS.filter { 
            isOptionalPermission(it) && !isPermissionGranted(context, it) 
        }
    }
    
    /**
     * Verifica se pode usar a funcionalidade de câmera
     */
    fun canUseCamera(context: Context): Boolean {
        return isCameraPermissionGranted(context) && 
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
    
    /**
     * Verifica se pode usar localização
     */
    fun canUseLocation(context: Context): Boolean {
        return isLocationPermissionGranted(context)
    }
    
    /**
     * Verifica se pode salvar arquivos
     */
    fun canSaveFiles(context: Context): Boolean {
        return isStoragePermissionGranted(context)
    }
    
    /**
     * Classe para resultado de verificação de permissões
     */
    data class PermissionStatus(
        val permission: String,
        val isGranted: Boolean,
        val shouldShowRationale: Boolean,
        val isPermanentlyDenied: Boolean
    )
    
    /**
     * Obtém status detalhado de uma permissão
     */
    fun getPermissionStatus(activity: Activity, permission: String): PermissionStatus {
        val isGranted = isPermissionGranted(activity, permission)
        val shouldShowRationale = shouldShowRequestPermissionRationale(activity, permission)
        val isPermanentlyDenied = hasPermissionBeenDeniedPermanently(activity, permission)
        
        return PermissionStatus(
            permission = permission,
            isGranted = isGranted,
            shouldShowRationale = shouldShowRationale,
            isPermanentlyDenied = isPermanentlyDenied
        )
    }
    
    /**
     * Obtém status detalhado de uma permissão (Fragment)
     */
    fun getPermissionStatus(fragment: Fragment, permission: String): PermissionStatus {
        val context = fragment.requireContext()
        val isGranted = isPermissionGranted(context, permission)
        val shouldShowRationale = shouldShowRequestPermissionRationale(fragment, permission)
        val isPermanentlyDenied = hasPermissionBeenDeniedPermanently(fragment, permission)
        
        return PermissionStatus(
            permission = permission,
            isGranted = isGranted,
            shouldShowRationale = shouldShowRationale,
            isPermanentlyDenied = isPermanentlyDenied
        )
    }
    
    /**
     * Obtém status de todas as permissões
     */
    fun getAllPermissionsStatus(activity: Activity): List<PermissionStatus> {
        return ALL_PERMISSIONS.map { getPermissionStatus(activity, it) }
    }
    
    /**
     * Obtém status de todas as permissões (Fragment)
     */
    fun getAllPermissionsStatus(fragment: Fragment): List<PermissionStatus> {
        return ALL_PERMISSIONS.map { getPermissionStatus(fragment, it) }
    }
    
    /**
     * Verifica se o app pode funcionar com as permissões atuais
     */
    fun canAppFunction(context: Context): Boolean {
        val criticalPermissionsDenied = getCriticalPermissionsDenied(context)
        return criticalPermissionsDenied.isEmpty()
    }
    
    /**
     * Obtém mensagem de erro para permissões negadas
     */
    fun getPermissionDeniedMessage(deniedPermissions: List<String>): String {
        return when {
            deniedPermissions.isEmpty() -> ""
            deniedPermissions.size == 1 -> {
                val permission = deniedPermissions.first()
                "A permissão ${getPermissionName(permission)} é necessária. ${getPermissionDescription(permission)}"
            }
            else -> {
                val permissionNames = deniedPermissions.map { getPermissionName(it) }
                "As seguintes permissões são necessárias: ${permissionNames.joinToString(", ")}"
            }
        }
    }
    
    /**
     * Obtém mensagem para permissões negadas permanentemente
     */
    fun getPermanentlyDeniedMessage(): String {
        return "Algumas permissões foram negadas permanentemente. " +
                "Para usar todas as funcionalidades do app, vá em Configurações > Permissões e conceda as permissões necessárias."
    }
}