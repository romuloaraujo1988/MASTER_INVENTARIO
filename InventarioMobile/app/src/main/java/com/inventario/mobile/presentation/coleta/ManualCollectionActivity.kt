package com.inventario.mobile.presentation.coleta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityManualCollectionBinding
import com.inventario.mobile.data.model.EstadoPatrimonio
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.NavigationHelper
import com.inventario.mobile.utils.SoundUtils
import com.inventario.mobile.utils.VoiceSearchManager
import com.inventario.mobile.presentation.dialog.EstadoPatrimonioDialog
import com.inventario.mobile.presentation.historico.HistoricoScansActivity
import com.inventario.mobile.ui.base.BaseOfflineActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ManualCollectionActivity : BaseOfflineActivity() {

    private lateinit var binding: ActivityManualCollectionBinding
    private val viewModel: ManualCollectionViewModel by viewModels()
    private var voiceSearchManager: VoiceSearchManager? = null

    // PreferencesManager injetado via Hilt
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    private var salaId: Long = -1L
    private var salaNome: String = ""
    
    // Guarda a descrição do último patrimônio coletado para "Coletar Similar"
    private var lastCollectedDescricao: String? = null

    // ✅ Injetar RegistrarColetaUseCase para coleta similar
    @Inject
    lateinit var registrarColetaUseCase: com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
    
    // ✅ v2.11: Injetar PhotoHelper para captura de fotos
    @javax.inject.Inject
    lateinit var photoHelper: com.inventario.mobile.utils.PhotoHelper
    
    // ✅ v2.11: Helper para captura de foto
    private var photoCaptureHelper: com.inventario.mobile.presentation.components.PhotoCaptureHelper? = null
    
    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceInput()
        } else {
            Toast.makeText(this, "Permissão de áudio necessária para busca por voz", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_SALA_ID = "extra_sala_id"
        const val EXTRA_SALA_NOME = "extra_sala_nome"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualCollectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // preferencesManager já é injetado via Hilt (@Inject)
        
        // Receber dados da sala selecionada
        salaId = intent.getLongExtra(EXTRA_SALA_ID, -1L)
        salaNome = intent.getStringExtra(EXTRA_SALA_NOME) ?: ""
        
        // Validar se sala foi selecionada
        if (salaId < 0 || salaNome.isEmpty()) {
            Log.e("ManualCollectionActivity", "Erro: Nenhuma sala selecionada (salaId=$salaId, salaNome=$salaNome)")
            Toast.makeText(this, "Erro: Nenhuma sala selecionada", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        setupObservers()
        
        // ✅ v2.11: Configurar captura de foto opcional
        setupPhotoCapture()
        
        // Configurar informações da sala no ViewModel
        viewModel.setSalaInfo(salaId, salaNome)
    }
    
    /**
     * v2.11: Configura captura de foto opcional
     */
    private fun setupPhotoCapture() {
        try {
            // Criar helper de captura de foto
            photoCaptureHelper = com.inventario.mobile.presentation.components.PhotoCaptureHelper(
                activity = this,
                photoHelper = photoHelper,
                preferencesManager = preferencesManager
            )
            
            // Registrar launcher de câmera
            photoCaptureHelper?.registerCameraLauncher()
            
            // Configurar views (se existirem no layout)
            binding.cardFotoOpcional?.let { card ->
                photoCaptureHelper?.setupPhotoCapture(
                    cardFotoOpcional = card,
                    textFotoLabel = binding.textFotoLabel!!,
                    btnAddPhoto = binding.btnAddPhoto!!,
                    layoutPhotoPreview = binding.layoutPhotoPreview!!,
                    imgPhotoPreview = binding.imgPhotoPreview!!,
                    btnRemovePhoto = binding.btnRemovePhoto!!,
                    layoutMotivoFoto = binding.layoutMotivoFoto!!,
                    spinnerMotivoFoto = binding.spinnerMotivoFoto!!
                )
                Log.d("ManualCollectionActivity", "✓ PhotoCaptureHelper configurado")
            } ?: run {
                Log.w("ManualCollectionActivity", "Card de foto não encontrado no layout")
            }
            
        } catch (e: Exception) {
            Log.e("ManualCollectionActivity", "Erro ao configurar captura de foto", e)
        }
    }

    private fun setupUI() {
        supportActionBar?.apply {
            title = "Coleta Manual - $salaNome"
            setDisplayHomeAsUpEnabled(true)
        }
        
        // Inicializar VoiceSearchManager
        voiceSearchManager = VoiceSearchManager(this)
        
        // Configurar botão de microfone no TextInputLayout
        binding.etPatrimonioNumber.parent.parent.let { textInputLayout ->
            if (textInputLayout is com.google.android.material.textfield.TextInputLayout) {
                textInputLayout.setEndIconOnClickListener {
                    Log.d("ManualCollection", "Botão de microfone clicado")
                    requestAudioPermissionIfNeeded()
                }
            }
        }

        binding.btnSearch.setOnClickListener {
            searchPatrimonio()
        }

        binding.btnCollect.setOnClickListener {
            collectPatrimonio()
        }

        binding.btnClear.setOnClickListener {
            clearForm()
        }
        
        // Enter no campo de texto = buscar
        binding.etPatrimonioNumber.setOnEditorActionListener { _, _, _ ->
            searchPatrimonio()
            true
        }
        
        // v2.7: Botão para voltar ao Dashboard
        binding.btnBackToDashboard.setOnClickListener {
            goBackToDashboard()
        }

        // Botão Coletar Similar (sem placa)
        binding.btnColetarSimilar.setOnClickListener {
            lastCollectedDescricao?.let { descricao ->
                showColetarSimilarDialog(descricao)
            } ?: run {
                Toast.makeText(this, "Nenhum patrimônio de referência", Toast.LENGTH_SHORT).show()
            }
        }
        
        // v2.12: Mostrar indicador visual se estado fixo está ativo
        atualizarIndicadorEstadoFixo()
    }
    
    /**
     * v2.12: Atualiza o subtítulo da toolbar para indicar estado fixo ativo
     */
    private fun atualizarIndicadorEstadoFixo() {
        if (preferencesManager.isEstadoFixoEnabled()) {
            val estadoFixo = preferencesManager.getEstadoFixo()
            if (estadoFixo != null) {
                val descricao = try {
                    EstadoPatrimonio.valueOf(estadoFixo).descricao
                } catch (e: IllegalArgumentException) {
                    estadoFixo
                }
                supportActionBar?.subtitle = "⚡ Estado fixo: $descricao"
            }
        } else {
            supportActionBar?.subtitle = null
        }
    }
    
    /**
     * v2.7: Navega de volta para o Dashboard (MainActivity)
     */
    private fun goBackToDashboard() {
        Log.d("ManualCollectionActivity", "Voltando ao Dashboard...")
        
        // Usar FLAG_ACTIVITY_CLEAR_TOP para voltar à MainActivity existente
        val intent = android.content.Intent(this, com.inventario.mobile.presentation.main.MainActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: ManualCollectionUiState) {
        // Atualizar informações da sala
        binding.tvSalaInfo?.text = "Sala: ${state.salaNome}"
        binding.tvCollectionCount.text = "Total coletado na sala: ${state.totalColetas}"

        // Controlar loading
        binding.progressBar?.visibility = if (state.isLoading) 
            android.view.View.VISIBLE else android.view.View.GONE

        // Exibir informações do patrimônio encontrado
        if (state.patrimonio != null) {
            // Tornar o card pai visível
            binding.cardPatrimonioInfo?.visibility = android.view.View.VISIBLE
            binding.tvPatrimonioInfo?.apply {
                visibility = android.view.View.VISIBLE
                text = buildString {
                    append("Patrimônio: ${state.patrimonio.numeroPatrimonio}\n")
                    append("Descrição: ${state.patrimonio.descricao}\n")
                    if (state.jaColetado) {
                        append("Status: JÁ COLETADO\n")
                        // Exibir informações detalhadas da coleta
                        if (!state.patrimonio.coletadoPor.isNullOrBlank()) {
                            append("Coletado por: ${state.patrimonio.coletadoPor}\n")
                        }
                        if (!state.patrimonio.dataColetaFormatada.isNullOrBlank()) {
                            append("Data: ${state.patrimonio.dataColetaFormatada}\n")
                        }
                        if (!state.patrimonio.localizacaoEncontrada.isNullOrBlank()) {
                            append("Local encontrado: ${state.patrimonio.localizacaoEncontrada}")
                        }
                    } else {
                        append("Status: Disponível para coleta")
                    }
                }
            }
            // Só habilita o botão se o patrimônio não foi coletado, não está carregando e é válido
            binding.btnCollect.isEnabled = !state.jaColetado && 
                                          !state.isLoading && 
                                          state.patrimonio.id > 0 && 
                                          state.patrimonio.numeroPatrimonio.isNotBlank()
            
            // ✅ v2.11: Mostrar card de foto opcional (se não foi coletado)
            photoCaptureHelper?.showPhotoCard(state.patrimonio.numeroPatrimonio, state.jaColetado)
        } else {
            // Ocultar o card pai quando não há patrimônio
            binding.cardPatrimonioInfo?.visibility = android.view.View.GONE
            binding.tvPatrimonioInfo?.visibility = android.view.View.GONE
            binding.btnCollect.isEnabled = false
            
            // ✅ v2.11: Ocultar card de foto
            photoCaptureHelper?.hidePhotoCard()
        }

        // Exibir mensagens
        state.errorMessage?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }

        state.successMessage?.let { message ->
            // Tocar som suave de sucesso
            SoundUtils.playSuccessSound()
            
            // Guardar descrição do patrimônio coletado para "Coletar Similar"
            state.patrimonio?.let { p ->
                if (!p.descricao.isNullOrBlank()) {
                    lastCollectedDescricao = p.descricao
                    binding.btnColetarSimilar.visibility = android.view.View.VISIBLE
                    binding.btnColetarSimilar.text = "Coletar similar: ${p.descricao.take(40)}${if (p.descricao.length > 40) "..." else ""}"
                }
            }
            
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
            clearForm()
        }
    }

    private fun searchPatrimonio() {
        val numeroPatrimonio = binding.etPatrimonioNumber.text.toString().trim()
        viewModel.searchPatrimonio(numeroPatrimonio)
    }

    private fun collectPatrimonio() {
        // v2.12: Verificar se estado fixo está habilitado (evita abrir dialog)
        if (preferencesManager.isEstadoFixoEnabled()) {
            val estadoFixo = preferencesManager.getEstadoFixo()
            if (estadoFixo != null) {
                Log.d("ManualCollection", "✓ Estado fixo ativo: $estadoFixo — coletando diretamente")
                viewModel.coletarPatrimonio(estadoFixo)
                return
            } else {
                // Estado fixo habilitado mas sem valor configurado — avisar o usuário
                Toast.makeText(
                    this,
                    "Estado fixo ativado, mas nenhum estado foi configurado. Acesse Configurações.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }
        
        // Estado fixo desabilitado: mostrar dialog de seleção
        val dialog = EstadoPatrimonioDialog.newInstance { estadoSelecionado ->
            // Após seleção, realizar a coleta com o estado
            viewModel.coletarPatrimonio(estadoSelecionado.name)
        }
        dialog.show(supportFragmentManager, "EstadoPatrimonioDialog")
    }

    private fun clearForm() {
        binding.etPatrimonioNumber.text?.clear()
        binding.etPatrimonioNumber.requestFocus()
        // Ocultar informações do patrimônio
        binding.cardPatrimonioInfo?.visibility = android.view.View.GONE
        binding.btnCollect.isEnabled = false
        viewModel.clearPatrimonio()
        
        // ✅ v2.11: Limpar e ocultar card de foto
        photoCaptureHelper?.clearPhoto()
        photoCaptureHelper?.hidePhotoCard()
    }

    // ========== COLETAR SIMILAR (sem placa) ==========

    private fun showColetarSimilarDialog(descricao: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔄 Coletar Similar")
            .setMessage("Registrar coleta de item similar:\n\n\"$descricao\"\n\nLocal: $salaNome\n\nEste item será registrado SEM número de patrimônio.")
            .setPositiveButton("Coletar") { _, _ ->
                if (preferencesManager.isEstadoFixoEnabled()) {
                    val estadoFixo = preferencesManager.getEstadoFixo()
                    if (!estadoFixo.isNullOrEmpty()) {
                        registrarColetaSimilar(descricao, estadoFixo)
                        return@setPositiveButton
                    }
                }
                showEstadoDialogParaSimilar(descricao)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEstadoDialogParaSimilar(descricao: String) {
        val dialog = EstadoPatrimonioDialog.newInstance { estadoSelecionado ->
            registrarColetaSimilar(descricao, estadoSelecionado.name)
        }
        dialog.show(supportFragmentManager, "EstadoPatrimonioDialogSimilar")
    }

    private fun registrarColetaSimilar(descricao: String, estadoConservacao: String) {
        Log.d("ManualCollection", "Registrando coleta similar: $descricao | Estado: $estadoConservacao")

        binding.progressBar?.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            try {
                val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
                val usuarioId = preferencesManager.getUserId() ?: 0
                val usuarioNome = preferencesManager.getUserName() ?: "Usuário"

                registrarColetaUseCase.registrarColetaPorDescricao(
                    descricao = descricao,
                    salaId = salaId.toInt(),
                    salaNome = salaNome,
                    estadoConservacao = estadoConservacao,
                    inventarioId = inventarioId,
                    usuarioId = usuarioId.toLong(),
                    usuarioNome = usuarioNome
                ).fold(
                    onSuccess = {
                        runOnUiThread {
                            binding.progressBar?.visibility = android.view.View.GONE
                            SoundUtils.playSuccessSound()
                            Toast.makeText(this@ManualCollectionActivity, "✓ Coleta similar registrada!", Toast.LENGTH_SHORT).show()
                            // Recarregar contagem de coletas
                            viewModel.setSalaInfo(salaId, salaNome)
                        }
                    },
                    onFailure = { error ->
                        runOnUiThread {
                            binding.progressBar?.visibility = android.view.View.GONE
                            Toast.makeText(this@ManualCollectionActivity, "Erro: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progressBar?.visibility = android.view.View.GONE
                    Toast.makeText(this@ManualCollectionActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        NavigationHelper.goBack(this)
        return true
    }
    
    // ========== MENU DA TOOLBAR ==========
    
    /**
     * v2.12: Infla o menu com botão de histórico
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manual_collection, menu)
        return true
    }
    
    /**
     * v2.12: Trata cliques no menu da toolbar
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavigationHelper.goBack(this)
                true
            }
            R.id.action_historico -> {
                abrirHistoricoColetas()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * v2.12: Abre a tela de histórico de scans/coletas
     */
    private fun abrirHistoricoColetas() {
        Log.d("ManualCollection", "Abrindo histórico de coletas...")
        val intent = Intent(this, HistoricoScansActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * v2.12: Atualiza indicador de estado fixo quando a activity volta ao foco
     * (usuário pode ter alterado nas configurações)
     */
    override fun onResume() {
        super.onResume()
        atualizarIndicadorEstadoFixo()
    }
    
    // ========== BUSCA POR VOZ ==========
    
    private fun requestAudioPermissionIfNeeded() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida
                startVoiceInput()
            }
            
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Mostrar explicação
                Toast.makeText(
                    this,
                    "Permissão de áudio necessária para busca por voz",
                    Toast.LENGTH_LONG
                ).show()
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            
            else -> {
                // Solicitar permissão
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startVoiceInput() {
        Log.d("ManualCollection", "Iniciando busca por voz")
        
        // Verificar disponibilidade
        if (voiceSearchManager?.isAvailable() != true) {
            Toast.makeText(
                this,
                "Reconhecimento de voz não disponível neste dispositivo",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Obter referência ao TextInputLayout pai
        val textInputLayout = binding.etPatrimonioNumber.parent.parent as? com.google.android.material.textfield.TextInputLayout
        
        // Mostrar feedback visual no TextInputLayout (não no EditText)
        textInputLayout?.hint = "🎤 Escutando..."
        
        // Iniciar reconhecimento
        voiceSearchManager?.startListening(object : VoiceSearchManager.VoiceSearchListener {
            override fun onResults(text: String) {
                Log.d("ManualCollection", "Resultado final: $text")
                processVoiceInput(text)
            }
            
            override fun onError(error: String) {
                Log.e("ManualCollection", "Erro no reconhecimento: $error")
                textInputLayout?.hint = "Número do Patrimônio"
                Toast.makeText(
                    this@ManualCollectionActivity,
                    "Erro: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            override fun onReadyForSpeech() {
                Log.d("ManualCollection", "Pronto para falar")
                textInputLayout?.hint = "🎤 Pode falar o número..."
            }
            
            override fun onBeginningOfSpeech() {
                Log.d("ManualCollection", "Começou a falar")
                textInputLayout?.hint = "🎤 Escutando..."
            }
            
            override fun onEndOfSpeech() {
                Log.d("ManualCollection", "Terminou de falar")
                textInputLayout?.hint = "⏳ Processando..."
            }
            
            override fun onPartialResults(text: String) {
                Log.d("ManualCollection", "Resultado parcial: $text")
                // Mostrar resultado parcial no campo
                binding.etPatrimonioNumber.setText(extractPatrimonioNumber(text))
            }
        })
    }
    
    private fun processVoiceInput(text: String) {
        Log.d("ManualCollection", "Processando entrada de voz: $text")
        
        // Obter referência ao TextInputLayout pai
        val textInputLayout = binding.etPatrimonioNumber.parent.parent as? com.google.android.material.textfield.TextInputLayout
        
        // Extrair número do patrimônio do texto falado
        val numeroPatrimonio = extractPatrimonioNumber(text)
        
        if (numeroPatrimonio.isNotEmpty()) {
            binding.etPatrimonioNumber.setText(numeroPatrimonio)
            textInputLayout?.hint = "Número do Patrimônio"
            
            // Buscar automaticamente
            searchPatrimonio()
            
            Toast.makeText(
                this,
                "Buscando patrimônio $numeroPatrimonio...",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            textInputLayout?.hint = "Número do Patrimônio"
            Toast.makeText(
                this,
                "Número não identificado. Tente novamente.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Extrai o número do patrimônio do texto falado
     * Suporta vários formatos:
     * - "12345" (direto)
     * - "patrimônio 12345"
     * - "número 12345"
     * - "um dois três quatro cinco" (por extenso)
     */
    private fun extractPatrimonioNumber(text: String): String {
        val cleanText = text.lowercase().trim()
        
        // Tentar extrair número direto
        val directNumber = cleanText.replace(Regex("[^0-9]"), "")
        if (directNumber.isNotEmpty()) {
            return directNumber
        }
        
        // Tentar converter números por extenso
        val numberWords = mapOf(
            "zero" to "0", "um" to "1", "dois" to "2", "três" to "3", "quatro" to "4",
            "cinco" to "5", "seis" to "6", "sete" to "7", "oito" to "8", "nove" to "9",
            "dez" to "10", "onze" to "11", "doze" to "12", "treze" to "13", "quatorze" to "14",
            "quinze" to "15", "dezesseis" to "16", "dezessete" to "17", "dezoito" to "18", "dezenove" to "19",
            "vinte" to "20", "trinta" to "30", "quarenta" to "40", "cinquenta" to "50",
            "sessenta" to "60", "setenta" to "70", "oitenta" to "80", "noventa" to "90",
            "cem" to "100", "cento" to "100", "duzentos" to "200", "trezentos" to "300",
            "quatrocentos" to "400", "quinhentos" to "500", "seiscentos" to "600",
            "setecentos" to "700", "oitocentos" to "800", "novecentos" to "900",
            "mil" to "1000"
        )
        
        val words = cleanText.split(" ")
        val result = StringBuilder()
        
        for (word in words) {
            numberWords[word]?.let { digit ->
                result.append(digit)
            }
        }
        
        return result.toString()
    }
    
    override fun onDestroy() {
        voiceSearchManager?.destroy()
        super.onDestroy()
    }
    
    // ========== CALLBACKS DE CONECTIVIDADE ==========
    
    /**
     * Chamado quando a conexão é restaurada
     * Permite validações online
     */
    override fun onConnectivityRestored() {
        Log.d("ManualCollectionActivity", "✓ Conexão restaurada! Validações online disponíveis...")
        
        Snackbar.make(
            binding.root,
            "Conexão restaurada. Validações online ativas.",
            Snackbar.LENGTH_SHORT
        ).show()
    }
    
    /**
     * Chamado quando a conexão é perdida
     * Usa apenas validações locais
     */
    override fun onConnectivityLost() {
        Log.d("ManualCollectionActivity", "⚠️ Conexão perdida! Usando validações locais...")
        
        Snackbar.make(
            binding.root,
            "Sem conexão. Usando dados locais para busca.",
            Snackbar.LENGTH_LONG
        ).setAction("OK") {
            // Dismiss
        }.show()
    }
}
