package com.inventario.mobile.presentation.components

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.inventario.mobile.utils.PhotoHelper
import com.inventario.mobile.utils.PhotoResult
import com.inventario.mobile.utils.PreferencesManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper para integrar captura de foto nas telas de coleta
 * 
 * Uso:
 * 1. Criar instância no onCreate da Activity
 * 2. Chamar setupPhotoCapture() com as views do layout
 * 3. Chamar showPhotoCard() quando patrimônio for encontrado
 * 4. Chamar getPhotoResult() ao registrar coleta
 * 5. Chamar clearPhoto() ao limpar formulário
 * 
 * @author Sistema de Inventário v2.11
 */
class PhotoCaptureHelper(
    private val activity: AppCompatActivity,
    private val photoHelper: PhotoHelper,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "PhotoCaptureHelper"
        
        val MOTIVOS_FOTO = listOf(
            "Divergência de localização",
            "Estado de conservação ruim",
            "Necessita atenção/revisão",
            "Outro motivo"
        )
        
        val MOTIVOS_ENUM = listOf(
            MotivoFoto.DIVERGENCIA,
            MotivoFoto.ESTADO_RUIM,
            MotivoFoto.ATENCAO,
            MotivoFoto.OUTRO
        )
    }
    
    // Views
    private var cardFotoOpcional: MaterialCardView? = null
    private var textFotoLabel: TextView? = null
    private var btnAddPhoto: MaterialButton? = null
    private var layoutPhotoPreview: FrameLayout? = null
    private var imgPhotoPreview: ImageView? = null
    private var btnRemovePhoto: ImageButton? = null
    private var layoutMotivoFoto: TextInputLayout? = null
    private var spinnerMotivoFoto: AutoCompleteTextView? = null
    
    // Estado
    private var currentPhotoUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var currentPhotoResult: PhotoResult? = null
    private var currentMotivoFoto: MotivoFoto = MotivoFoto.OUTRO
    private var currentPatrimonioNumero: String = ""
    
    // Launcher para câmera
    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null
    
    /**
     * Configura o launcher de câmera (deve ser chamado no onCreate)
     */
    fun registerCameraLauncher(): ActivityResultLauncher<Uri> {
        takePictureLauncher = activity.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && currentPhotoUri != null) {
                Log.d(TAG, "✓ Foto capturada com sucesso")
                processPhoto(currentPhotoUri!!)
            } else {
                Log.w(TAG, "Captura de foto cancelada ou falhou")
            }
        }
        return takePictureLauncher!!
    }
    
    /**
     * Configura as views de captura de foto
     */
    fun setupPhotoCapture(
        cardFotoOpcional: MaterialCardView,
        textFotoLabel: TextView,
        btnAddPhoto: MaterialButton,
        layoutPhotoPreview: FrameLayout,
        imgPhotoPreview: ImageView,
        btnRemovePhoto: ImageButton,
        layoutMotivoFoto: TextInputLayout,
        spinnerMotivoFoto: AutoCompleteTextView
    ) {
        this.cardFotoOpcional = cardFotoOpcional
        this.textFotoLabel = textFotoLabel
        this.btnAddPhoto = btnAddPhoto
        this.layoutPhotoPreview = layoutPhotoPreview
        this.imgPhotoPreview = imgPhotoPreview
        this.btnRemovePhoto = btnRemovePhoto
        this.layoutMotivoFoto = layoutMotivoFoto
        this.spinnerMotivoFoto = spinnerMotivoFoto
        
        // Configurar adapter do spinner de motivos
        val adapter = ArrayAdapter(
            activity,
            android.R.layout.simple_dropdown_item_1line,
            MOTIVOS_FOTO
        )
        spinnerMotivoFoto.setAdapter(adapter)
        spinnerMotivoFoto.setOnItemClickListener { _, _, position, _ ->
            currentMotivoFoto = MOTIVOS_ENUM.getOrElse(position) { MotivoFoto.OUTRO }
            Log.d(TAG, "Motivo selecionado: $currentMotivoFoto")
        }
        
        // Configurar botão de adicionar foto
        btnAddPhoto.setOnClickListener {
            launchCamera()
        }
        
        // Configurar botão de remover foto
        btnRemovePhoto.setOnClickListener {
            clearPhoto()
        }
        
        Log.d(TAG, "✓ PhotoCaptureHelper configurado")
    }
    
    /**
     * Mostra o card de foto quando patrimônio é encontrado
     */
    fun showPhotoCard(patrimonioNumero: String, jaColetado: Boolean = false) {
        currentPatrimonioNumero = patrimonioNumero
        
        // Verificar se fotos estão habilitadas nas configurações
        if (!preferencesManager.isPhotoOnCollectionEnabled()) {
            Log.d(TAG, "Fotos desabilitadas nas configurações")
            cardFotoOpcional?.visibility = View.GONE
            return
        }
        
        // Não mostrar opção de foto se já foi coletado
        if (jaColetado) {
            cardFotoOpcional?.visibility = View.GONE
            return
        }
        
        cardFotoOpcional?.visibility = View.VISIBLE
        Log.d(TAG, "Card de foto exibido para patrimônio: $patrimonioNumero")
    }
    
    /**
     * Esconde o card de foto
     */
    fun hidePhotoCard() {
        cardFotoOpcional?.visibility = View.GONE
    }
    
    /**
     * Inicia a câmera para capturar foto
     */
    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoPath = photoFile.absolutePath
            currentPhotoUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                photoFile
            )
            
            Log.d(TAG, "Iniciando câmera para: $currentPhotoPath")
            takePictureLauncher?.launch(currentPhotoUri)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar câmera", e)
        }
    }
    
    /**
     * Processa a foto capturada
     */
    private fun processPhoto(uri: Uri) {
        try {
            // Comprimir e salvar foto
            val result = photoHelper.compressAndSavePhoto(uri, currentPatrimonioNumero)
            
            if (result != null) {
                currentPhotoResult = result
                
                // Mostrar preview
                val bitmap = BitmapFactory.decodeFile(result.fullPath)
                imgPhotoPreview?.setImageBitmap(bitmap)
                
                // Atualizar UI
                btnAddPhoto?.visibility = View.GONE
                layoutPhotoPreview?.visibility = View.VISIBLE
                layoutMotivoFoto?.visibility = View.VISIBLE
                textFotoLabel?.text = "📷 Foto adicionada (${result.sizeKB}KB)"
                
                Log.d(TAG, "✓ Foto processada: ${result.fullPath} (${result.sizeKB}KB)")
            } else {
                Log.e(TAG, "Falha ao processar foto")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar foto", e)
        }
    }
    
    /**
     * Limpa a foto atual
     */
    fun clearPhoto() {
        // Deletar arquivo temporário se existir
        currentPhotoResult?.let { result ->
            photoHelper.deletePhoto(result.fullPath)
        }
        
        currentPhotoUri = null
        currentPhotoPath = null
        currentPhotoResult = null
        currentMotivoFoto = MotivoFoto.OUTRO
        
        // Atualizar UI
        imgPhotoPreview?.setImageBitmap(null)
        btnAddPhoto?.visibility = View.VISIBLE
        layoutPhotoPreview?.visibility = View.GONE
        layoutMotivoFoto?.visibility = View.GONE
        spinnerMotivoFoto?.setText("", false)
        textFotoLabel?.text = "📷 Foto (opcional)"
        
        Log.d(TAG, "Foto removida")
    }
    
    /**
     * Retorna o resultado da foto (para salvar na coleta)
     */
    fun getPhotoResult(): PhotoCaptureResult? {
        return currentPhotoResult?.let { result ->
            PhotoCaptureResult(
                fotoPath = result.fullPath,
                fotoThumbnailPath = result.thumbnailPath,
                motivoFoto = currentMotivoFoto.name,
                sizeKB = result.sizeKB
            )
        }
    }
    
    /**
     * Verifica se tem foto
     */
    fun hasPhoto(): Boolean = currentPhotoResult != null
    
    /**
     * Cria arquivo temporário para foto
     */
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = activity.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
    }
    
    /**
     * Motivos para adicionar foto
     */
    enum class MotivoFoto {
        DIVERGENCIA,    // Patrimônio em local diferente
        ESTADO_RUIM,    // Estado de conservação ruim
        ATENCAO,        // Necessita atenção/revisão
        OUTRO           // Outro motivo
    }
}

/**
 * Resultado da captura de foto
 */
data class PhotoCaptureResult(
    val fotoPath: String,
    val fotoThumbnailPath: String?,
    val motivoFoto: String,
    val sizeKB: Long
)
