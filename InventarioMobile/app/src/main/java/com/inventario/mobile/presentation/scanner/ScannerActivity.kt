package com.inventario.mobile.presentation.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.inventario.mobile.databinding.ActivityScannerBinding
import com.inventario.mobile.utils.QRCodeUtils
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.di.NetworkModule
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.PermissionHelper
import com.inventario.mobile.utils.CameraUtils
import com.inventario.mobile.utils.Android14CameraHelper
import com.inventario.mobile.utils.NavigationHelper
import com.inventario.mobile.utils.SoundUtils
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import com.inventario.mobile.presentation.dialog.EstadoPatrimonioDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScannerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityScannerBinding
    private lateinit var viewModel: ScannerViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var android14CameraHelper: Android14CameraHelper
    private var currentScanResult: ScanResult? = null
    private var isInitializing = false
    private var retryCount = 0
    private val maxRetries = 3
    private var hasScannedOnce = false // ✅ Flag para evitar reiniciar scanner após primeira leitura
    
    // ✅ Injetar Use Cases via Hilt
    @Inject
    lateinit var registrarColetaUseCase: com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
    
    // ✅ v2.8: Injetar BuscarPatrimonioUseCase para suporte OFFLINE
    @Inject
    lateinit var buscarPatrimonioUseCase: com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase
    
    // Launcher para solicitar permissão de câmera
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        handleCameraPermissionResult(isGranted)
    }
    
    // Launcher moderno para scanner (substitui IntentIntegrator deprecated)
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "CALLBACK DO SCANNER RECEBIDO")
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "Thread: ${Thread.currentThread().name}")
        android.util.Log.d("ScannerActivity", "Result: $result")
        android.util.Log.d("ScannerActivity", "Contents: ${result?.contents}")
        android.util.Log.d("ScannerActivity", "Format: ${result?.formatName}")
        
        // ✅ CRÍTICO: Reset flags IMEDIATAMENTE
        isInitializing = false
        hasScannedOnce = true // ✅ Marcar que já escaneou uma vez
        
        android.util.Log.d("ScannerActivity", "✓ Flags atualizadas: isInitializing=false, hasScannedOnce=true")
        
        if (result == null || result.contents == null) {
            // Scan cancelado pelo usuário
            android.util.Log.w("ScannerActivity", "❌ Scan cancelado ou resultado nulo")
            showError("Scan cancelado")
            finish()
        } else {
            // Código lido com sucesso
            android.util.Log.d("ScannerActivity", "✅ Código lido com sucesso: ${result.contents}")
            android.util.Log.d("ScannerActivity", "Formato: ${result.formatName}")
            
            // ✅ Tocar som suave de sucesso
            SoundUtils.playSuccessSound()
            
            // ✅ Processar código na thread principal
            runOnUiThread {
                android.util.Log.d("ScannerActivity", "Processando código na UI thread...")
                processQRCode(result.contents)
            }
        }
    }
    
    companion object {
        const val EXTRA_QR_RESULT = "qr_result"
        const val EXTRA_PATRIMONIO_ID = "patrimonio_id"
        const val EXTRA_PATRIMONIO_CODIGO = "patrimonio_codigo"
        const val EXTRA_ALLOW_COLLECTION = "allow_collection"
        const val EXTRA_SALA_ID = "extra_sala_id"
        const val EXTRA_SALA_NOME = "extra_sala_nome"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("ScannerActivity", "Iniciando onCreate da ScannerActivity...")
            
            // Inflar layout
            binding = ActivityScannerBinding.inflate(layoutInflater)
            setContentView(binding.root)
            android.util.Log.d("ScannerActivity", "Layout inflado com sucesso")
            
            // Inicializar PreferencesManager
            android.util.Log.d("ScannerActivity", "Inicializando PreferencesManager...")
            preferencesManager = PreferencesManager(this)
            android.util.Log.d("ScannerActivity", "PreferencesManager inicializado com sucesso")
            
            // Ler dados da sala do Intent e salvar no PreferencesManager
            android.util.Log.d("ScannerActivity", "=== VERIFICANDO SALA NO INTENT ===")
            android.util.Log.d("ScannerActivity", "EXTRA_SALA_ID = $EXTRA_SALA_ID")
            android.util.Log.d("ScannerActivity", "EXTRA_SALA_NOME = $EXTRA_SALA_NOME")
            
            val salaId = intent.getIntExtra(EXTRA_SALA_ID, 0)
            val salaNome = intent.getStringExtra(EXTRA_SALA_NOME)
            
            android.util.Log.d("ScannerActivity", "Sala ID recebida: $salaId")
            android.util.Log.d("ScannerActivity", "Sala Nome recebida: $salaNome")
            
            if (salaId > 0) {
                preferencesManager.setCurrentSalaId(salaId)
                if (!salaNome.isNullOrBlank()) {
                    preferencesManager.setCurrentSalaNome(salaNome)
                }
                android.util.Log.d("ScannerActivity", "✅ Sala salva no PreferencesManager: ID=$salaId, Nome=$salaNome")
            } else {
                android.util.Log.e("ScannerActivity", "❌ ERRO: Nenhuma sala foi passada no Intent!")
                android.util.Log.e("ScannerActivity", "Intent extras: ${intent.extras?.keySet()?.joinToString()}")
            }
            
            // Inicializar Android14CameraHelper
            android.util.Log.d("ScannerActivity", "Inicializando Android14CameraHelper...")
            android14CameraHelper = Android14CameraHelper(this)
            android.util.Log.d("ScannerActivity", "Android14CameraHelper inicializado com sucesso")
            
            // ✅ v2.8: Inicializar ViewModel com Use Cases injetados via Hilt
            android.util.Log.d("ScannerActivity", "Inicializando ViewModel com Hilt (MODO OFFLINE SUPORTADO)...")
            val apiService = NetworkModule.getApiService(this)
            val repository = InventarioRepository.getInstance(this, apiService)
            val factory = ScannerViewModelFactory(
                repository, 
                preferencesManager, 
                registrarColetaUseCase,
                buscarPatrimonioUseCase // ✅ NOVO: Use Case para busca offline
            )
            viewModel = ViewModelProvider(this, factory)[ScannerViewModel::class.java]
            android.util.Log.d("ScannerActivity", "✓ ViewModel inicializado com sucesso (com BuscarPatrimonioUseCase + RegistrarColetaUseCase via Hilt)")
            
            setupToolbar()
            setupObservers()
            setupButtonListeners()
            
            // Verificar e solicitar permissões antes de inicializar scanner
            checkAndRequestPermissions()
            
            android.util.Log.d("ScannerActivity", "onCreate concluído com sucesso")
            
        } catch (e: Exception) {
            android.util.Log.e("ScannerActivity", "Erro crítico durante onCreate", e)
            android.util.Log.e("ScannerActivity", "Tipo do erro: ${e.javaClass.simpleName}")
            android.util.Log.e("ScannerActivity", "Mensagem: ${e.message}")
            android.util.Log.e("ScannerActivity", "Stack trace: ${e.stackTrace.joinToString("\n")}")
            
            // Tentar mostrar uma mensagem de erro ao usuário
            try {
                Toast.makeText(this, "Erro ao inicializar scanner: ${e.message}", Toast.LENGTH_LONG).show()
            } catch (toastException: Exception) {
                android.util.Log.e("ScannerActivity", "Não foi possível mostrar Toast de erro", toastException)
            }
            
            // Finalizar a activity
            finish()
        }
    }
    
    private fun handleCameraPermissionResult(isGranted: Boolean) {
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "Resultado da permissão de câmera: $isGranted")
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        
        // Reset flag antes de processar resultado
        isInitializing = false
        
        if (isGranted) {
            android.util.Log.d("ScannerActivity", "✅ Permissão concedida, inicializando scanner...")
            Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show()
            
            // Aguardar um pouco para garantir que o sistema processou a permissão
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    initializeScanner()
                } catch (e: Exception) {
                    android.util.Log.e("ScannerActivity", "Erro ao inicializar scanner após permissão", e)
                    showError("Erro ao inicializar câmera: ${e.message}")
                    finish()
                }
            }, 300) // 300ms de delay
            
        } else {
            android.util.Log.w("ScannerActivity", "❌ Permissão de câmera negada pelo usuário")
            
            // Verificar se deve mostrar rationale novamente
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                // Usuário negou mas não marcou "Não perguntar novamente"
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permissão Necessária")
                    .setMessage("A câmera é essencial para escanear códigos QR. Sem ela, não é possível usar esta funcionalidade.\n\nDeseja tentar novamente?")
                    .setPositiveButton("Tentar Novamente") { _, _ ->
                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                // Usuário marcou "Não perguntar novamente" ou negou permanentemente
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permissão Negada")
                    .setMessage("A permissão de câmera foi negada permanentemente.\n\nPara usar o scanner, você precisa habilitar a permissão manualmente nas configurações do aplicativo.")
                    .setPositiveButton("Abrir Configurações") { _, _ ->
                        openAppSettings()
                    }
                    .setNegativeButton("Fechar") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "VERIFICANDO PERMISSÃO DE CÂMERA (Android 14+)")
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        
        // Verificar se já tem permissão
        if (android14CameraHelper.checkCameraPermissions()) {
            android.util.Log.d("ScannerActivity", "✅ Permissão já concedida, inicializando scanner")
            
            // Aguardar um pouco para garantir que a UI está pronta
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    initializeScanner()
                } catch (e: Exception) {
                    android.util.Log.e("ScannerActivity", "Erro ao inicializar scanner", e)
                    showError("Erro ao inicializar câmera: ${e.message}")
                    finish()
                }
            }, 200)
            
        } else {
            android.util.Log.d("ScannerActivity", "⚠️ Permissão não concedida, solicitando...")
            
            // Verificar se deve mostrar rationale
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                // Mostrar explicação antes de solicitar
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permissão de Câmera Necessária")
                    .setMessage("Este aplicativo precisa acessar a câmera para escanear códigos QR dos patrimônios.\n\nSem esta permissão, não será possível usar o scanner.")
                    .setPositiveButton("Permitir") { _, _ ->
                        android.util.Log.d("ScannerActivity", "Usuário aceitou o rationale, solicitando permissão")
                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        android.util.Log.w("ScannerActivity", "Usuário negou o rationale")
                        Toast.makeText(this, "Não é possível escanear sem permissão de câmera", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                // Primeira vez solicitando, pedir diretamente
                android.util.Log.d("ScannerActivity", "Primeira solicitação de permissão")
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }
    
    /**
     * Abre as configurações do aplicativo para o usuário habilitar permissões manualmente
     */
    private fun openAppSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            android.util.Log.e("ScannerActivity", "Erro ao abrir configurações", e)
            Toast.makeText(this, "Não foi possível abrir as configurações", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    

    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Scanner de Códigos"
        }
    }
    
    private fun showEstadoPatrimonioDialog(patrimonioId: Long, salaNome: String) {
        val dialog = EstadoPatrimonioDialog.newInstance { estadoSelecionado ->
            // Callback executado quando o usuário seleciona um estado
            viewModel.coletarPatrimonioComEstado(patrimonioId, salaNome, estadoSelecionado.name)
        }
        dialog.show(supportFragmentManager, "EstadoPatrimonioDialog")
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun setupButtonListeners() {
        binding.buttonColetar.setOnClickListener {
            android.util.Log.d("ScannerActivity", "=== BOTÃO COLETAR CLICADO ===")
            currentScanResult?.let { result ->
                val salaId = preferencesManager.getCurrentSalaId()
                val salaNome = preferencesManager.getCurrentSalaNome()
                
                android.util.Log.d("ScannerActivity", "Sala ID do PreferencesManager: $salaId")
                android.util.Log.d("ScannerActivity", "Sala Nome do PreferencesManager: $salaNome")
                
                if (salaId > 0 && !salaNome.isNullOrBlank()) {
                    android.util.Log.d("ScannerActivity", "✅ Sala válida, mostrando diálogo de estado")
                    // Mostrar diálogo de seleção de estado antes de coletar
                    showEstadoPatrimonioDialog(result.patrimonioId, salaNome)
                } else {
                    android.util.Log.e("ScannerActivity", "❌ ERRO: Sala não selecionada!")
                    android.util.Log.e("ScannerActivity", "salaId: $salaId, salaNome: $salaNome")
                    Toast.makeText(this, "Sala não selecionada", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                android.util.Log.e("ScannerActivity", "❌ ERRO: currentScanResult é null!")
            }
        }
        
        binding.buttonRetry.setOnClickListener {
            android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
            android.util.Log.d("ScannerActivity", "BOTÃO 'ESCANEAR OUTRO' CLICADO")
            android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
            
            // ✅ Resetar estado e reiniciar scanner
            resetScannerState()
            
            // ✅ Aguardar um pouco para garantir que UI foi atualizada
            Handler(Looper.getMainLooper()).postDelayed({
                initializeScanner()
            }, 200)
        }
        
        binding.buttonCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    
    /**
     * Reseta o estado do scanner para permitir nova leitura
     */
    private fun resetScannerState() {
        android.util.Log.d("ScannerActivity", "Resetando estado do scanner...")
        
        // Limpar resultado atual
        currentScanResult = null
        
        // ✅ CRÍTICO: Resetar flag para permitir novo scan
        hasScannedOnce = false
        
        // Limpar estado do ViewModel
        viewModel.clearScanResult()
        
        // Ocultar card de informações
        binding.cardPatrimonioInfo.visibility = View.GONE
        
        // Ocultar botões
        binding.buttonColetar.visibility = View.GONE
        binding.buttonRetry.visibility = View.GONE
        binding.buttonCancel.visibility = View.VISIBLE
        
        // Resetar contador de tentativas
        retryCount = 0
        
        android.util.Log.d("ScannerActivity", "✓ hasScannedOnce resetado para false")
        
        // Atualizar status
        binding.textStatus.text = "Preparando scanner..."
        
        android.util.Log.d("ScannerActivity", "Estado resetado com sucesso")
    }
    
    private fun updateUI(state: ScannerUiState) {
        binding.progressBar.visibility = if (state.isLoading) 
            View.VISIBLE else View.GONE
            
        binding.textStatus.text = state.statusMessage
        binding.textColetasCount.text = state.totalColetas.toString()
        
        // Tratar mensagem de sucesso (coleta realizada)
        state.successMessage?.let { message ->
            // Tocar som de sucesso
            SoundUtils.playSuccessSound()
            
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
            
            // Limpar formulário e preparar para próxima coleta
            clearFormAndPrepareForNext()
        }
        
        state.errorMessage?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
            
            // Mostrar botão de tentar novamente quando houver erro
            showRetryInterface()
        }
        
        state.scanResult?.let { result ->
            android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
            android.util.Log.d("ScannerActivity", "PATRIMÔNIO ENCONTRADO - MOSTRANDO DADOS")
            android.util.Log.d("ScannerActivity", "Número: ${result.patrimonioCodigo}")
            android.util.Log.d("ScannerActivity", "Já coletado: ${result.jaColetado}")
            android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
            
            currentScanResult = result
            displayPatrimonioInfo(result)
            
            // ✅ SEMPRE mostrar interface de coleta (mesmo se já coletado)
            // Permite que usuário veja os dados e decida se quer escanear outro
            showCollectionInterface(result)
        }
    }
    
    /**
     * Limpa o formulário e prepara para próxima coleta (similar à coleta manual)
     */
    private fun clearFormAndPrepareForNext() {
        android.util.Log.d("ScannerActivity", "Limpando formulário e preparando para próxima coleta...")
        
        // Limpar resultado atual
        currentScanResult = null
        
        // Limpar estado do ViewModel
        viewModel.clearScanResult()
        
        // Ocultar card de informações
        binding.cardPatrimonioInfo.visibility = View.GONE
        
        // Ocultar botão de coletar
        binding.buttonColetar.visibility = View.GONE
        
        // Mostrar botão de escanear outro
        binding.buttonRetry.visibility = View.VISIBLE
        binding.buttonRetry.text = "Escanear Outro"
        binding.buttonCancel.visibility = View.VISIBLE
        
        // Atualizar status
        binding.textStatus.text = "Coleta realizada! Pronto para escanear outro patrimônio."
        
        android.util.Log.d("ScannerActivity", "Formulário limpo, pronto para próxima coleta")
    }
    
    private fun showRetryInterface() {
        // Ocultar card de informações do patrimônio
        binding.cardPatrimonioInfo.visibility = View.GONE
        
        // Ocultar botão de coletar
        binding.buttonColetar.visibility = View.GONE
        
        // Mostrar botão de tentar novamente
        binding.buttonRetry.visibility = View.VISIBLE
        binding.buttonRetry.text = "Escanear Novamente"
        
        // Mostrar botão de cancelar
        binding.buttonCancel.visibility = View.VISIBLE
    }
    
    private fun displayPatrimonioInfo(result: ScanResult) {
        binding.cardPatrimonioInfo.visibility = View.VISIBLE
        binding.textPatrimonioNumero.text = "Número: ${result.patrimonioCodigo}"
        binding.textPatrimonioDescricao.text = "Descrição: ${result.patrimonio?.descricao ?: "N/A"}"
        
        // ✅ CORREÇÃO: Usar salaNome ao invés de salaId
        val salaInfo = result.patrimonio?.salaNome ?: "Sala não informada"
        binding.textPatrimonioSala.text = "Sala: $salaInfo"
        
        // Status do patrimônio
        val statusText = if (result.jaColetado) "Status: JÁ COLETADO" else "Status: Disponível para coleta"
        binding.textPatrimonioStatus.text = statusText
        binding.textPatrimonioStatus.setTextColor(
            if (result.jaColetado) 
                getColor(android.R.color.holo_orange_dark)
            else 
                getColor(android.R.color.holo_green_dark)
        )
        
        // ✅ Exibir informações detalhadas da coleta quando já foi coletado
        if (result.jaColetado) {
            binding.layoutInfoColeta.visibility = View.VISIBLE
            
            // Coletado por
            binding.textColetadoPor.text = if (!result.coletadoPor.isNullOrBlank()) {
                "Coletado por: ${result.coletadoPor}"
            } else {
                "Coletado por: Não informado"
            }
            
            // Data da coleta
            binding.textDataColeta.text = if (!result.dataColetaFormatada.isNullOrBlank()) {
                "Data: ${result.dataColetaFormatada}"
            } else {
                "Data: Não informada"
            }
            
            // Localização encontrada
            val localizacao = result.patrimonio?.localizacaoEncontrada
            binding.textLocalizacaoEncontrada.text = if (!localizacao.isNullOrBlank()) {
                "Local encontrado: $localizacao"
            } else {
                "Local encontrado: Não informado"
            }
        } else {
            binding.layoutInfoColeta.visibility = View.GONE
        }
    }
    
    private fun showCollectionInterface(result: ScanResult) {
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "MOSTRANDO INTERFACE DE COLETA")
        android.util.Log.d("ScannerActivity", "Patrimônio: ${result.patrimonioCodigo}")
        android.util.Log.d("ScannerActivity", "Já coletado: ${result.jaColetado}")
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        
        // ✅ SEMPRE mostrar card de informações
        binding.cardPatrimonioInfo.visibility = View.VISIBLE
        
        if (result.jaColetado) {
            // Se já foi coletado, NÃO mostrar botão de coletar
            // Mas SEMPRE mostrar botão "Escanear Outro" para continuar coletando
            binding.buttonColetar.visibility = View.GONE
            binding.buttonRetry.visibility = View.VISIBLE
            binding.buttonRetry.text = "Escanear Outro"
            binding.buttonCancel.visibility = View.VISIBLE
            
            android.util.Log.w("ScannerActivity", "⚠️ Patrimônio ${result.patrimonioCodigo} já foi coletado")
            android.util.Log.d("ScannerActivity", "✓ Mostrando botão 'Escanear Outro' para continuar")
        } else {
            // Se não foi coletado, mostrar botão de coletar E botão de escanear outro
            binding.buttonColetar.visibility = View.VISIBLE
            binding.buttonColetar.text = "Coletar"
            binding.buttonRetry.visibility = View.VISIBLE
            binding.buttonRetry.text = "Escanear Outro"
            binding.buttonCancel.visibility = View.VISIBLE
            
            android.util.Log.d("ScannerActivity", "✓ Patrimônio disponível para coleta")
            android.util.Log.d("ScannerActivity", "✓ Mostrando botões: Coletar + Escanear Outro")
        }
    }
    
    private fun initializeScanner() {
        if (isInitializing) {
            android.util.Log.w("ScannerActivity", "⚠️ Scanner já está sendo inicializado, ignorando...")
            return
        }
        
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "INICIALIZANDO SCANNER DE CÓDIGOS (Android 14+)")
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        
        isInitializing = true
        
        // Atualizar UI
        binding.textStatus.text = "Inicializando câmera..."
        binding.progressBar.visibility = View.VISIBLE
        
        // Usar o Android14CameraHelper para verificações
        if (!android14CameraHelper.checkCameraPermissions()) {
            android.util.Log.e("ScannerActivity", "❌ ERRO: Permissões de câmera não concedidas!")
            showError("Permissões de câmera não concedidas")
            isInitializing = false
            binding.progressBar.visibility = View.GONE
            finish()
            return
        }
        
        // Verificar se a câmera está disponível usando o helper
        if (!android14CameraHelper.validateCameraSupport()) {
            android.util.Log.e("ScannerActivity", "❌ ERRO: Câmera não está disponível!")
            
            // Executar diagnóstico detalhado
            val diagnosticInfo = performCameraDiagnostic()
            android.util.Log.e("ScannerActivity", "Diagnóstico da câmera:\n$diagnosticInfo")
            
            isInitializing = false
            binding.progressBar.visibility = View.GONE
            showCameraDiagnosticDialog(diagnosticInfo)
            return
        }
        
        try {
            android.util.Log.d("ScannerActivity", "Configurando ScanOptions...")
            
            // Configurar opções do scanner (API moderna)
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(
                    com.google.zxing.BarcodeFormat.QR_CODE.name,
                    com.google.zxing.BarcodeFormat.EAN_13.name,
                    com.google.zxing.BarcodeFormat.EAN_8.name,
                    com.google.zxing.BarcodeFormat.CODE_128.name,
                    com.google.zxing.BarcodeFormat.CODE_39.name,
                    com.google.zxing.BarcodeFormat.CODE_93.name,
                    com.google.zxing.BarcodeFormat.UPC_A.name,
                    com.google.zxing.BarcodeFormat.UPC_E.name,
                    com.google.zxing.BarcodeFormat.ITF.name
                )
                setPrompt("Posicione o QR Code ou código de barras dentro do quadro")
                setCameraId(0) // Câmera traseira
                setBeepEnabled(false) // Desabilitar beep padrão - usaremos som customizado
                setBarcodeImageEnabled(false)
                setOrientationLocked(true)
                setTimeout(30000) // 30 segundos timeout
            }
            
            android.util.Log.d("ScannerActivity", "✓ ScanOptions configurado")
            android.util.Log.d("ScannerActivity", "  - Formatos: QR_CODE, EAN, CODE_128, etc")
            android.util.Log.d("ScannerActivity", "  - Câmera: Traseira (ID: 0)")
            android.util.Log.d("ScannerActivity", "  - Beep: Desabilitado")
            android.util.Log.d("ScannerActivity", "  - Timeout: 30s")
            
            android.util.Log.d("ScannerActivity", "Iniciando scanner com ScanContract...")
            binding.progressBar.visibility = View.GONE
            binding.textStatus.text = "Abrindo câmera..."
            
            barcodeLauncher.launch(options)
            android.util.Log.d("ScannerActivity", "✅ Scanner iniciado com sucesso!")
            
        } catch (e: Exception) {
            android.util.Log.e("ScannerActivity", "❌ ERRO ao inicializar scanner", e)
            android.util.Log.e("ScannerActivity", "Tipo: ${e.javaClass.simpleName}, Mensagem: ${e.message}")
            
            isInitializing = false
            binding.progressBar.visibility = View.GONE
            
            // Tentar novamente se não excedeu o limite
            if (retryCount < maxRetries) {
                retryCount++
                android.util.Log.w("ScannerActivity", "⚠️ Tentativa $retryCount de $maxRetries...")
                
                // Aguardar um pouco antes de tentar novamente
                binding.textStatus.text = "Tentando novamente... ($retryCount/$maxRetries)"
                
                Handler(Looper.getMainLooper()).postDelayed({
                    initializeScanner()
                }, 2000) // Aguardar 2 segundos
                
            } else {
                android.util.Log.e("ScannerActivity", "❌ Máximo de tentativas excedido")
                showCameraErrorDialog(e)
            }
        }
    }
    
    private fun showCameraErrorDialog(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("camera", ignoreCase = true) == true -> 
                "Erro na câmera: ${exception.message}"
            exception.message?.contains("permission", ignoreCase = true) == true -> 
                "Problema de permissão: ${exception.message}"
            else -> "Erro inesperado: ${exception.message}"
        }
        
        // Executar diagnóstico detalhado
        val diagnosticInfo = performCameraDiagnostic()
        android.util.Log.e("ScannerActivity", "Diagnóstico após erro:\n$diagnosticInfo")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🚨 Erro na Câmera")
            .setMessage("$errorMessage\n\nClique em 'Ver Diagnóstico' para mais detalhes.")
            .setPositiveButton("Tentar Novamente") { _, _ ->
                retryCount = 0
                initializeScanner()
            }
            .setNeutralButton("Ver Diagnóstico") { _, _ ->
                showCameraDiagnosticDialog(diagnosticInfo)
            }
            .setNegativeButton("Fechar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }


    /**
     * Executa diagnóstico detalhado da câmera
     */
    private fun performCameraDiagnostic(): String {
        return buildString {
            appendLine("=== DIAGNÓSTICO DA CÂMERA ===")
            appendLine()
            
            // Informações básicas do dispositivo
            appendLine("📱 DISPOSITIVO:")
            appendLine("• Modelo: ${android.os.Build.MODEL}")
            appendLine("• Fabricante: ${android.os.Build.MANUFACTURER}")
            appendLine("• Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
            appendLine()
            
              // Verificações de permissão
            appendLine("🔐 PERMISSÕES:")
            val hasCameraPermission = PermissionHelper.hasCameraPermission(this@ScannerActivity)
            appendLine("• Permissão CAMERA: ${if (hasCameraPermission) "✓ CONCEDIDA" else "✗ NEGADA"}")
            appendLine()
            
            // Verificações de hardware
            appendLine("📷 HARDWARE:")
            val packageManager = packageManager
            val hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
            val hasCameraBack = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
            val hasCameraFront = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
            val hasAutofocus = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)
            val hasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
            
            appendLine("• Camera ANY: ${if (hasCamera) "✓ DISPONÍVEL" else "✗ INDISPONÍVEL"}")
            appendLine("• Camera BACK: ${if (hasCameraBack) "✓ DISPONÍVEL" else "✗ INDISPONÍVEL"}")
            appendLine("• Camera FRONT: ${if (hasCameraFront) "✓ DISPONÍVEL" else "✗ INDISPONÍVEL"}")
            appendLine("• Autofocus: ${if (hasAutofocus) "✓ DISPONÍVEL" else "✗ INDISPONÍVEL"}")
            appendLine("• Flash: ${if (hasFlash) "✓ DISPONÍVEL" else "✗ INDISPONÍVEL"}")
            appendLine()
            
            // Verificações do CameraManager
            appendLine("🎥 CAMERA MANAGER:")
            try {
                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
                val cameraIds = cameraManager.cameraIdList
                appendLine("• Câmeras encontradas: ${cameraIds.size}")
                
                cameraIds.forEachIndexed { _, cameraId ->
                    try {
                        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                        val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                        val facingStr = when (facing) {
                            android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK -> "TRASEIRA"
                            android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT -> "FRONTAL"
                            else -> "DESCONHECIDA"
                        }
                        appendLine("  - Câmera $cameraId: $facingStr")
                    } catch (e: Exception) {
                        appendLine("  - Câmera $cameraId: ERRO - ${e.message}")
                    }
                }
            } catch (e: Exception) {
                appendLine("• ERRO ao acessar CameraManager: ${e.message}")
            }
            appendLine()
            
            // Verificações de aplicativos
            appendLine("📱 APLICATIVOS:")
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val hasCameraApp = cameraIntent.resolveActivity(packageManager) != null
            appendLine("• App de câmera disponível: ${if (hasCameraApp) "✓ SIM" else "✗ NÃO"}")
            appendLine()
            
            // Verificações específicas do helper
            appendLine("🔧 VALIDAÇÕES INTERNAS:")
            appendLine("• Android14CameraHelper.checkCameraPermissions(): ${android14CameraHelper.checkCameraPermissions()}")
            appendLine("• Android14CameraHelper.validateCameraSupport(): ${android14CameraHelper.validateCameraSupport()}")
            appendLine("• CameraUtils.canUseCamera(): ${CameraUtils.canUseCamera(this@ScannerActivity)}")
            appendLine("• CameraUtils.hasCamera(): ${CameraUtils.hasCamera(this@ScannerActivity)}")
            appendLine("• CameraUtils.hasCameraPermission(): ${CameraUtils.hasCameraPermission(this@ScannerActivity)}")
            appendLine("• CameraUtils.isCameraIntentAvailable(): ${CameraUtils.isCameraIntentAvailable(this@ScannerActivity)}")
        }
    }
    
    /**
     * Mostra dialog com diagnóstico da câmera
     */
    private fun showCameraDiagnosticDialog(diagnosticInfo: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔍 Diagnóstico da Câmera")
            .setMessage(diagnosticInfo)
            .setPositiveButton("Tentar Novamente") { _, _ ->
                retryCount = 0
                checkAndRequestPermissions()
            }
            .setNegativeButton("Copiar Diagnóstico") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Diagnóstico da Câmera", diagnosticInfo)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Diagnóstico copiado para a área de transferência", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Fechar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun processQRCode(qrContent: String) {
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "Processando código escaneado: $qrContent")
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        
        binding.textStatus.text = "Processando código..."
        
        // Primeiro, tenta decodificar como código de patrimônio (QR Code estruturado)
        val patrimonioData = QRCodeUtils.decodePatrimonioQRCode(qrContent)
        
        if (patrimonioData != null) {
            // É um QR Code estruturado (formato: PATRIMONIO:ID:CODIGO)
            android.util.Log.d("ScannerActivity", "QR Code estruturado detectado")
            android.util.Log.d("ScannerActivity", "Patrimônio ID: ${patrimonioData.patrimonioId}")
            android.util.Log.d("ScannerActivity", "Código: ${patrimonioData.codigo}")
            
            viewModel.searchPatrimonio(patrimonioData.patrimonioId, patrimonioData.codigo)
        } else {
            // Tenta interpretar como código simples (número do patrimônio direto)
            android.util.Log.d("ScannerActivity", "Código simples detectado, buscando por número: $qrContent")
            
            viewModel.searchPatrimonioByCodigo(qrContent)
        }
    }
    
    private fun handleScanSuccess(result: ScanResult) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_QR_RESULT, result.qrContent)
            putExtra(EXTRA_PATRIMONIO_ID, result.patrimonioId)
            putExtra(EXTRA_PATRIMONIO_CODIGO, result.patrimonioCodigo)
        }
        
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        NavigationHelper.goBack(this)
        return true
    }
    
    override fun onResume() {
        super.onResume()
        
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "onResume() chamado")
        android.util.Log.d("ScannerActivity", "isInitializing: $isInitializing")
        android.util.Log.d("ScannerActivity", "hasScannedOnce: $hasScannedOnce")
        android.util.Log.d("ScannerActivity", "currentScanResult: ${currentScanResult != null}")
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        
        // ✅ CRÍTICO: Só reiniciar scanner se:
        // 1. Não está inicializando
        // 2. Tem permissão
        // 3. NÃO escaneou ainda (evita reiniciar após primeira leitura)
        // 4. Não tem resultado de scan
        if (!isInitializing && 
            !hasScannedOnce && 
            android14CameraHelper.checkCameraPermissions() && 
            currentScanResult == null) {
            
            android.util.Log.d("ScannerActivity", "✅ Condições atendidas, inicializando scanner...")
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    initializeScanner()
                } catch (e: Exception) {
                    android.util.Log.e("ScannerActivity", "Erro ao inicializar scanner no onResume", e)
                }
            }, 300)
        } else {
            android.util.Log.d("ScannerActivity", "⏭️ Pulando inicialização do scanner no onResume")
            if (hasScannedOnce) {
                android.util.Log.d("ScannerActivity", "  Motivo: Já escaneou uma vez")
            }
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}
