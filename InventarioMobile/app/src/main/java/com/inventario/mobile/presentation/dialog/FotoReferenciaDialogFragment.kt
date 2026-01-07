package com.inventario.mobile.presentation.dialog

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inventario.mobile.R
import com.inventario.mobile.databinding.DialogFotoReferenciaBinding

/**
 * Dialog para exibir foto de referência em tamanho maior com zoom
 * 
 * Suporta:
 * - Pinch to zoom
 * - Double tap to zoom
 * - Pan/drag quando ampliado
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
class FotoReferenciaDialogFragment : DialogFragment() {
    
    private var _binding: DialogFotoReferenciaBinding? = null
    private val binding get() = _binding!!
    
    private var bitmap: Bitmap? = null
    private var descricao: String? = null
    
    // Zoom e pan
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private val matrix = Matrix()
    private var scaleFactor = 1f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    
    companion object {
        private const val TAG = "FotoReferenciaDialog"
        private const val ARG_DESCRICAO = "descricao"
        
        private const val MIN_SCALE = 1f
        private const val MAX_SCALE = 5f
        
        // Bitmap é passado via companion object para evitar serialização
        private var pendingBitmap: Bitmap? = null
        
        /**
         * Cria nova instância do dialog
         * 
         * @param bitmap Bitmap da foto a exibir
         * @param descricao Descrição do patrimônio (opcional)
         */
        fun newInstance(bitmap: Bitmap, descricao: String? = null): FotoReferenciaDialogFragment {
            pendingBitmap = bitmap
            return FotoReferenciaDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DESCRICAO, descricao)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_InventarioMobile_FullScreenDialog)
        
        // Recuperar bitmap do companion object
        bitmap = pendingBitmap
        pendingBitmap = null
        
        descricao = arguments?.getString(ARG_DESCRICAO)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFotoReferenciaBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupZoom()
        setupListeners()
    }
    
    private fun setupUI() {
        // Exibir descrição se disponível
        if (!descricao.isNullOrBlank()) {
            binding.textViewDescricao.text = descricao
            binding.textViewDescricao.visibility = View.VISIBLE
        } else {
            binding.textViewDescricao.visibility = View.GONE
        }
        
        // Exibir bitmap
        bitmap?.let {
            binding.imageViewFoto.setImageBitmap(it)
        } ?: run {
            // Sem bitmap, fechar dialog
            dismiss()
        }
    }
    
    private fun setupZoom() {
        scaleGestureDetector = ScaleGestureDetector(
            requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = scaleFactor.coerceIn(MIN_SCALE, MAX_SCALE)
                    
                    binding.imageViewFoto.scaleX = scaleFactor
                    binding.imageViewFoto.scaleY = scaleFactor
                    
                    return true
                }
            }
        )
    }
    
    private fun setupListeners() {
        // Botão fechar
        binding.buttonFechar.setOnClickListener {
            dismiss()
        }
        
        // Click no fundo para fechar
        binding.root.setOnClickListener {
            dismiss()
        }
        
        // Impedir que click na imagem feche o dialog
        binding.cardFoto.setOnClickListener { /* noop */ }
        
        // Touch para zoom e pan
        binding.imageViewFoto.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                    isDragging = true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging && scaleFactor > 1f) {
                        val dx = event.x - lastTouchX
                        val dy = event.y - lastTouchY
                        
                        binding.imageViewFoto.translationX += dx
                        binding.imageViewFoto.translationY += dy
                        
                        lastTouchX = event.x
                        lastTouchY = event.y
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                }
            }
            
            true
        }
        
        // Double tap para zoom toggle
        var lastClickTime = 0L
        binding.imageViewFoto.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 300) {
                // Double tap - toggle zoom
                if (scaleFactor > 1f) {
                    // Reset zoom
                    scaleFactor = 1f
                    binding.imageViewFoto.scaleX = 1f
                    binding.imageViewFoto.scaleY = 1f
                    binding.imageViewFoto.translationX = 0f
                    binding.imageViewFoto.translationY = 0f
                } else {
                    // Zoom in
                    scaleFactor = 2.5f
                    binding.imageViewFoto.scaleX = scaleFactor
                    binding.imageViewFoto.scaleY = scaleFactor
                }
            }
            lastClickTime = currentTime
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
