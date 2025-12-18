package com.inventario.mobile.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.inventario.mobile.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente de UI para captura opcional de foto na coleta
 * 
 * Uso:
 * 1. Adicionar no layout XML
 * 2. Configurar launcher de câmera
 * 3. Chamar setPhoto() quando foto for capturada
 * 
 * @author Sistema de Inventário v2.11
 */
class PhotoCaptureButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    companion object {
        private const val TAG = "PhotoCaptureButton"
    }
    
    // Views
    private lateinit var cardPhoto: MaterialCardView
    private lateinit var btnAddPhoto: MaterialButton
    private lateinit var imgPreview: ImageView
    private lateinit var btnRemovePhoto: ImageView
    private lateinit var tvPhotoLabel: TextView
    
    // Estado
    private var currentPhotoUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var onPhotoChangedListener: ((Uri?, String?) -> Unit)? = null
    
    // Motivo da foto
    var motivoFoto: MotivoFoto = MotivoFoto.OUTRO
        private set
    
    init {
        initView()
    }
    
    private fun initView() {
        // Inflar layout programaticamente (sem XML)
        val cardView = MaterialCardView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            cardElevation = 2f
            radius = 12f
            setContentPadding(16, 16, 16, 16)
        }
        
        val innerLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        
        // Label
        tvPhotoLabel = TextView(context).apply {
            text = "📷 Foto (opcional)"
            textSize = 14f
            setTextColor(context.getColor(android.R.color.darker_gray))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        }
        
        // Botão adicionar foto
        btnAddPhoto = MaterialButton(context).apply {
            text = "Adicionar Foto"
            setIconResource(android.R.drawable.ic_menu_camera)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // Container para preview
        val previewContainer = FrameLayout(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                300
            )
            visibility = GONE
        }
        
        // Preview da foto
        imgPreview = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        
        // Botão remover
        btnRemovePhoto = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            layoutParams = LayoutParams(48, 48).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.END
                setMargins(0, 8, 8, 0)
            }
            setPadding(8, 8, 8, 8)
        }
        
        previewContainer.addView(imgPreview)
        previewContainer.addView(btnRemovePhoto)
        
        innerLayout.addView(tvPhotoLabel)
        innerLayout.addView(btnAddPhoto)
        innerLayout.addView(previewContainer)
        
        cardView.addView(innerLayout)
        addView(cardView)
        
        // Guardar referência do container
        cardPhoto = cardView
        
        // Configurar listeners
        btnRemovePhoto.setOnClickListener {
            clearPhoto()
        }
    }
    
    /**
     * Configura o launcher de câmera
     */
    fun setupCameraLauncher(
        launcher: ActivityResultLauncher<Uri>,
        onPhotoTaken: (Uri, String) -> Unit
    ) {
        btnAddPhoto.setOnClickListener {
            try {
                val photoFile = createImageFile()
                currentPhotoPath = photoFile.absolutePath
                currentPhotoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                
                currentPhotoUri?.let { uri ->
                    launcher.launch(uri)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar arquivo de foto", e)
            }
        }
    }
    
    /**
     * Define a foto capturada
     */
    fun setPhoto(bitmap: Bitmap?, path: String?) {
        if (bitmap != null && path != null) {
            imgPreview.setImageBitmap(bitmap)
            currentPhotoPath = path
            
            // Mostrar preview, esconder botão
            btnAddPhoto.visibility = GONE
            (imgPreview.parent as? FrameLayout)?.visibility = VISIBLE
            
            tvPhotoLabel.text = "📷 Foto adicionada"
            
            onPhotoChangedListener?.invoke(currentPhotoUri, path)
            
            Log.d(TAG, "✓ Foto definida: $path")
        }
    }
    
    /**
     * Define a foto a partir de URI
     */
    fun setPhotoFromUri(uri: Uri?) {
        if (uri != null) {
            imgPreview.setImageURI(uri)
            currentPhotoUri = uri
            
            btnAddPhoto.visibility = GONE
            (imgPreview.parent as? FrameLayout)?.visibility = VISIBLE
            
            tvPhotoLabel.text = "📷 Foto adicionada"
            
            onPhotoChangedListener?.invoke(uri, currentPhotoPath)
        }
    }
    
    /**
     * Limpa a foto
     */
    fun clearPhoto() {
        imgPreview.setImageBitmap(null)
        currentPhotoUri = null
        currentPhotoPath = null
        
        btnAddPhoto.visibility = VISIBLE
        (imgPreview.parent as? FrameLayout)?.visibility = GONE
        
        tvPhotoLabel.text = "📷 Foto (opcional)"
        
        onPhotoChangedListener?.invoke(null, null)
        
        Log.d(TAG, "Foto removida")
    }
    
    /**
     * Define o motivo da foto
     */
    fun setMotivo(motivo: MotivoFoto) {
        this.motivoFoto = motivo
        tvPhotoLabel.text = when (motivo) {
            MotivoFoto.DIVERGENCIA -> "📷 Foto de Divergência"
            MotivoFoto.ESTADO_RUIM -> "📷 Foto - Estado Ruim"
            MotivoFoto.ATENCAO -> "📷 Foto - Necessita Atenção"
            MotivoFoto.OUTRO -> "📷 Foto (opcional)"
        }
    }
    
    /**
     * Listener para mudanças na foto
     */
    fun setOnPhotoChangedListener(listener: (Uri?, String?) -> Unit) {
        onPhotoChangedListener = listener
    }
    
    /**
     * Retorna o caminho da foto atual
     */
    fun getPhotoPath(): String? = currentPhotoPath
    
    /**
     * Retorna a URI da foto atual
     */
    fun getPhotoUri(): Uri? = currentPhotoUri
    
    /**
     * Verifica se tem foto
     */
    fun hasPhoto(): Boolean = currentPhotoPath != null
    
    /**
     * Cria arquivo temporário para foto
     */
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
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
