package com.inventario.mobile.presentation.coleta

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityItemSemEtiquetaBinding
import com.inventario.mobile.domain.model.OrigemSugestoes
import com.inventario.mobile.presentation.coleta.adapter.SugestaoDescricaoAdapter
import com.inventario.mobile.presentation.coleta.state.SugestaoDescricaoState
import com.inventario.mobile.utils.FotoTipo
import com.inventario.mobile.utils.PhotoHelper
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Activity para registro de itens sem etiqueta.
 *
 * Captura a foto usando FileProvider + TakePicture (resolução completa),
 * comprime via [PhotoHelper] com tipo [FotoTipo.SEM_ETIQUETA] e passa o
 * caminho do arquivo ao ViewModel.
 * O upload para o servidor é feito pelo [PhotoSyncWorker] em background.
 *
 * ## Nomenclatura do arquivo gerado
 * ```
 * files/fotos/inventario_{id}/sem_etiqueta/{coletaId}_SE_{yyyyMMdd_HHmmss}.jpg
 * ```
 *
 * v2.22: usa nova API do PhotoHelper com inventarioId + FotoTipo.
 */
@AndroidEntryPoint
class ItemSemEtiquetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemSemEtiquetaBinding
    private val viewModel: ItemSemEtiquetaViewModel by viewModels()

    @Inject lateinit var photoHelper: PhotoHelper
    @Inject lateinit var preferencesManager: PreferencesManager

    /** URI do arquivo temporário criado antes de abrir a câmera. */
    private var fotoUri: Uri? = null

    /** Caminho absoluto do arquivo comprimido após captura. */
    private var fotoPath: String? = null
    private var fotoThumbnailPath: String? = null

    // Feature: coleta-descricao-livre-com-sugestao
    // Adapter do RecyclerView de sugestões — criado em onCreate.
    private lateinit var sugestaoAdapter: SugestaoDescricaoAdapter

    // Launcher para captura de foto em arquivo (resolução completa)
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && fotoUri != null) {
            handleFotoCapturada(fotoUri!!)
        }
    }

    // Launcher para permissão de câmera
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) abrirCamera()
        else showError("Permissão de câmera negada")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemSemEtiquetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategorias()
        setupEstados()
        // Feature: coleta-descricao-livre-com-sugestao
        setupSugestaoRecyclerView()
        setupListeners()
        setupObservers()

        // Feature: coleta-descricao-livre-com-sugestao (Req 1.1)
        // Focar o campo de descrição livre na abertura da tela e abrir
        // teclado programaticamente para acelerar a digitação.
        binding.edtDescricao.requestFocus()
        binding.edtDescricao.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding.edtDescricao, InputMethodManager.SHOW_IMPLICIT)
        }
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
        binding.btnTirarFoto.setOnClickListener { solicitarPermissaoCamera() }
        binding.btnRemoverFoto.setOnClickListener { removerFoto() }
        binding.btnRegistrar.setOnClickListener { validarERegistrar() }
        binding.btnCancelar.setOnClickListener { finish() }

        // Feature: coleta-descricao-livre-com-sugestao
        // Req 2.1, 2.3, 2.4 — alterna visibilidade do autocomplete e
        // dispara carregamento/ocultação no ViewModel.
        binding.toggleSugestao.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setToggleSugestao(isChecked)
        }

        // Feature: coleta-descricao-livre-com-sugestao
        // Req 3.7 — propaga o termo de busca ao ViewModel, que aplica o
        // debounce de 300ms internamente. Não altera o comportamento de
        // validação: validarERegistrar() continua lendo edtDescricao.text.
        binding.edtDescricao.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onTermoBuscaChange(s?.toString().orEmpty())
            }
        })
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ItemSemEtiquetaState.Idle    -> hideLoading()
                    is ItemSemEtiquetaState.Loading -> showLoading()
                    is ItemSemEtiquetaState.Success -> {
                        hideLoading()
                        showSuccess("Item sem etiqueta registrado com sucesso!")
                        finish()
                    }
                    is ItemSemEtiquetaState.Error   -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }

        // Feature: coleta-descricao-livre-com-sugestao
        // Req 3.2, 3.3, 3.6, 7.4 — renderiza o estado do autocomplete.
        lifecycleScope.launch {
            viewModel.sugestaoState.collect { state -> renderSugestaoState(state) }
        }

        // Feature: coleta-descricao-livre-com-sugestao
        // Req 3.5 — preenche o campo livre quando uma sugestão é
        // selecionada, mantendo o foco e o cursor ao final.
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is ItemSemEtiquetaViewModel.Event.PreencherCampoLivre ->
                        aplicarDescricaoSelecionada(event.texto)
                }
            }
        }
    }

    // ─── Sugestão de descrição (feature: coleta-descricao-livre-com-sugestao) ──

    /**
     * Cria o adapter e configura o RecyclerView de sugestões. O
     * [LinearLayoutManager] é vertical por padrão e o adapter delega o
     * clique ao ViewModel para preencher o campo livre (Req 3.3, 3.5).
     */
    private fun setupSugestaoRecyclerView() {
        sugestaoAdapter = SugestaoDescricaoAdapter { sugestao ->
            viewModel.onSugestaoSelecionada(sugestao)
        }
        binding.rvSugestoes.adapter = sugestaoAdapter
        binding.rvSugestoes.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Renderiza o [SugestaoDescricaoState] corrente controlando a
     * visibilidade do RecyclerView, o conteúdo do adapter e snackbars
     * contextuais. Nunca desabilita o [edtDescricao] — o campo livre
     * permanece sempre editável (Req 7.4, 7.7).
     */
    private fun renderSugestaoState(state: SugestaoDescricaoState) {
        when (state) {
            is SugestaoDescricaoState.Oculto -> {
                binding.rvSugestoes.visibility = View.GONE
                sugestaoAdapter.submitList(emptyList())
            }
            is SugestaoDescricaoState.Carregando -> {
                // Mantém a lista oculta enquanto carrega para evitar
                // "flash" de conteúdo obsoleto. O progress bar global
                // (binding.progressBar) é reservado ao fluxo de registro.
                binding.rvSugestoes.visibility = View.GONE
            }
            is SugestaoDescricaoState.Carregado -> {
                binding.rvSugestoes.visibility = View.VISIBLE
                sugestaoAdapter.submitList(state.sugestoes)
                if (state.origem == OrigemSugestoes.CACHE) {
                    // Req 3.2 — sinalizar que as sugestões vêm do cache.
                    Snackbar.make(
                        binding.root,
                        "Exibindo sugestões do cache local",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            is SugestaoDescricaoState.SemResultados -> {
                // Req 3.6 — esconder a lista e informar ausência de
                // resultados, mantendo o campo livre habilitado.
                binding.rvSugestoes.visibility = View.GONE
                sugestaoAdapter.submitList(emptyList())
                Snackbar.make(
                    binding.root,
                    "Nenhuma sugestão encontrada",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            is SugestaoDescricaoState.Erro -> {
                // Req 7.4 — manter campo editável e apenas informar o
                // usuário via snackbar.
                binding.rvSugestoes.visibility = View.GONE
                sugestaoAdapter.submitList(emptyList())
                Snackbar.make(binding.root, state.mensagem, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Aplica o texto selecionado no [edtDescricao] preservando o foco e
     * movendo o cursor para o fim, sem desabilitar o campo (Req 3.5,
     * 4.2, 7.4).
     */
    private fun aplicarDescricaoSelecionada(texto: String) {
        binding.edtDescricao.setText(texto)
        binding.edtDescricao.setSelection(binding.edtDescricao.text?.length ?: 0)
        binding.edtDescricao.requestFocus()
    }

    // ─── Câmera ──────────────────────────────────────────────────────────────

    private fun solicitarPermissaoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            abrirCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Cria um arquivo temporário via FileProvider e abre a câmera para
     * capturar a foto em resolução completa (não thumbnail).
     */
    private fun abrirCamera() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val tempFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)

            fotoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                tempFile
            )
            takePictureLauncher.launch(fotoUri)
        } catch (e: Exception) {
            android.util.Log.e("ItemSemEtiqueta", "Erro ao abrir câmera", e)
            showError("Erro ao abrir câmera: ${e.message}")
        }
    }

    /**
     * Comprime a foto capturada via [PhotoHelper] com tipo [FotoTipo.SEM_ETIQUETA]
     * e atualiza a UI com o preview.
     */
    private fun handleFotoCapturada(uri: Uri) {
        try {
            val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
            val result = photoHelper.compressAndSavePhoto(
                sourceUri     = uri,
                inventarioId  = inventarioId,
                tipo          = FotoTipo.SEM_ETIQUETA,
                identificador = PhotoHelper.ID_SEM_ETIQUETA,
                coletaId      = 0L   // ainda não inserida no banco
            )
            if (result != null) {
                fotoPath = result.fullPath
                fotoThumbnailPath = result.thumbnailPath

                val bitmap = BitmapFactory.decodeFile(result.fullPath)
                binding.imgPreview.setImageBitmap(bitmap)
                binding.imgPreview.visibility = View.VISIBLE
                binding.btnRemoverFoto.visibility = View.VISIBLE
                binding.tvFotoStatus.text = "✓ Foto capturada (${result.sizeKB} KB)"
                binding.tvFotoStatus.setTextColor(getColor(R.color.success))

                android.util.Log.d("ItemSemEtiqueta", "✓ Foto salva: ${result.fullPath} (${result.sizeKB} KB)")
            } else {
                showError("Falha ao processar foto. Tente novamente.")
            }
        } catch (e: Exception) {
            android.util.Log.e("ItemSemEtiqueta", "Erro ao processar foto", e)
            showError("Erro ao processar foto: ${e.message}")
        }
    }

    private fun removerFoto() {
        fotoPath?.let { photoHelper.deletePhoto(it) }
        fotoPath = null
        fotoThumbnailPath = null
        fotoUri = null
        binding.imgPreview.setImageBitmap(null)
        binding.imgPreview.visibility = View.GONE
        binding.btnRemoverFoto.visibility = View.GONE
        binding.tvFotoStatus.text = "Nenhuma foto"
        binding.tvFotoStatus.setTextColor(getColor(R.color.text_secondary))
    }

    // ─── Validação e registro ─────────────────────────────────────────────────

    private fun validarERegistrar() {
        val descricao  = binding.edtDescricao.text.toString().trim()
        val categoria  = binding.spinnerCategoria.selectedItem.toString()
        val estado     = binding.spinnerEstado.selectedItem.toString()
        val localizacao = binding.edtLocalizacao.text.toString().trim()
        val observacoes = binding.edtObservacoes.text.toString().trim()

        if (descricao.isEmpty()) {
            binding.edtDescricao.error = "Descrição é obrigatória"
            binding.edtDescricao.requestFocus()
            return
        }
        if (categoria == "Selecione uma categoria") {
            showError("Selecione uma categoria"); return
        }
        if (estado == "Selecione o estado") {
            showError("Selecione o estado do item"); return
        }
        if (localizacao.isEmpty()) {
            binding.edtLocalizacao.error = "Localização é obrigatória"
            binding.edtLocalizacao.requestFocus()
            return
        }
        if (fotoPath == null) {
            showError("Foto é obrigatória para itens sem etiqueta"); return
        }

        viewModel.registrarItemSemEtiqueta(
            descricao       = descricao,
            categoria       = categoria,
            estado          = estado,
            localizacao     = localizacao,
            observacoes     = observacoes,
            fotoPath        = fotoPath!!,
            fotoThumbnailPath = fotoThumbnailPath
        )
    }

    // ─── UI helpers ──────────────────────────────────────────────────────────

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegistrar.isEnabled = false
        binding.btnTirarFoto.isEnabled = false
        // Feature: coleta-descricao-livre-com-sugestao (Req 7.4, 7.7)
        // Campo de descrição livre permanece habilitado mesmo durante
        // o envio — nenhum estado (loading, cache, erro) deve desabilitá-lo.
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
