package com.inventario.mobile.presentation.coleta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityItemSemEtiquetaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * Activity para registro de itens sem etiqueta
 * Permite coletar patrimônios encontrados que não possuem etiqueta de identificação
 */
@AndroidEntryPoint
class ItemSemEtiquetaActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityItemSemEtiquetaBinding
    private val viewModel: ItemSemEtiquetaViewModel by viewModels()
    
    private var fotoBase64: String? = null
    private var fotoBitmap: Bitmap? = null
    
    // Launcher para captura de foto
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                handleFotoCapturada(it)
            }
        }
    }
    
    // Launcher para permissão de câmera
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            abrirCamera()
        } else {
            showError("Permissão de câmera negada")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemSemEtiquetaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupCategorias()
        setupEstados()
        setupListeners()
        setupObservers()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Item Sem Etiqueta"
        }
    }
    
    private fun setupCategorias() {
        val categorias = listOf(
            "Selecione uma categoria",
            "Móveis",
            "Equipamentos de Informática",
            "Eletrodomésticos",
            "Ferramentas",
            "Veículos",
            "Livros e Publicações",
            "Equipamentos de Laboratório",
            "Outros"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapter
    }
    
    private fun setupEstados() {
        val estados = listOf(
            "Selecione o estado",
            "NOVO",
            "BOM",
            "REGULAR",
            "RUIM",
            "PÉSSIMO",
            "IRRECUPERÁVEL"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEstado.adapter = adapter
    }
    
    private fun setupListeners() {
        binding.btnTirarFoto.setOnClickListener {
            solicitarPermissaoCamera()
        }
        
        binding.btnRemoverFoto.setOnClickListener {
            removerFoto()
        }
        
        binding.btnRegistrar.setOnClickListener {
            validarERegistrar()
        }
        
        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ItemSemEtiquetaState.Idle -> {
                        hideLoading()
                    }
                    is ItemSemEtiquetaState.Loading -> {
                        showLoading()
                    }
                    is ItemSemEtiquetaState.Success -> {
                        hideLoading()
                        showSuccess("Item sem etiqueta registrado com sucesso!")
                        finish()
                    }
                    is ItemSemEtiquetaState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun solicitarPermissaoCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun abrirCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }
    
    private fun handleFotoCapturada(bitmap: Bitmap) {
        fotoBitmap = bitmap
        
        // Converter para Base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        fotoBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        
        // Mostrar preview
        binding.imgPreview.setImageBitmap(bitmap)
        binding.imgPreview.visibility = View.VISIBLE
        binding.btnRemoverFoto.visibility = View.VISIBLE
        binding.tvFotoStatus.text = "✓ Foto capturada"
        binding.tvFotoStatus.setTextColor(getColor(R.color.success))
    }
    
    private fun removerFoto() {
        fotoBitmap = null
        fotoBase64 = null
        binding.imgPreview.visibility = View.GONE
        binding.btnRemoverFoto.visibility = View.GONE
        binding.tvFotoStatus.text = "Nenhuma foto"
        binding.tvFotoStatus.setTextColor(getColor(R.color.text_secondary))
    }
    
    private fun validarERegistrar() {
        // Validar campos obrigatórios
        val descricao = binding.edtDescricao.text.toString().trim()
        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val estado = binding.spinnerEstado.selectedItem.toString()
        val localizacao = binding.edtLocalizacao.text.toString().trim()
        val observacoes = binding.edtObservacoes.text.toString().trim()
        
        // Validações
        if (descricao.isEmpty()) {
            binding.edtDescricao.error = "Descrição é obrigatória"
            binding.edtDescricao.requestFocus()
            return
        }
        
        if (categoria == "Selecione uma categoria") {
            showError("Selecione uma categoria")
            return
        }
        
        if (estado == "Selecione o estado") {
            showError("Selecione o estado do item")
            return
        }
        
        if (localizacao.isEmpty()) {
            binding.edtLocalizacao.error = "Localização é obrigatória"
            binding.edtLocalizacao.requestFocus()
            return
        }
        
        if (fotoBase64 == null) {
            showError("Foto é obrigatória para itens sem etiqueta")
            return
        }
        
        // Registrar item
        viewModel.registrarItemSemEtiqueta(
            descricao = descricao,
            categoria = categoria,
            estado = estado,
            localizacao = localizacao,
            observacoes = observacoes,
            fotoBase64 = fotoBase64!!
        )
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegistrar.isEnabled = false
        binding.btnTirarFoto.isEnabled = false
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegistrar.isEnabled = true
        binding.btnTirarFoto.isEnabled = true
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.success))
            .show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
