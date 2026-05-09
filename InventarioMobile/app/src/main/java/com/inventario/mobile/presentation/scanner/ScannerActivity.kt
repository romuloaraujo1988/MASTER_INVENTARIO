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
import com.inventario.mobile.data.local.database.AppDatabase
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
import com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScannerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityScannerBinding
    private lateinit var viewModel: ScannerViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var android14CameraHelper: Android14CameraHelper
    private var currentScanResult: ScanResult? = null
    private var lastCollectedPatrimonio: ScanResult? = null // ✅ NOVO: Guarda último patrimônio coletado para "Coletar Similar"
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
    
    // ✅ v2.10: Injetar VibrationHelper para feedback tátil
    @Inject
    lateinit var vibrationHelper: com.inventario.mobile.utils.VibrationHelper
    
    // ✅ v2.10: Injetar PhotoHelper para captura de fotos
    @Inject
    lateinit var photoHelper: com.inventario.mobile.utils.PhotoHelper
    
    // ✅ v2.11: Injetar RegistrarAcessoPatrimonioUseCase para histórico de scans
    @Inject
    lateinit var registrarAcessoUseCase: com.inventario.mobile.domain.usecase.RegistrarAcessoPatrimonioUseCase
    

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
                buscarPatrimonioUseCase,
                vibrationHelper // ✅ v2.10: VibrationHelper para feedback tátil
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

        // v2.20.11: atualiza os chips "Sala" e "Estado" com os valores atuais das
        // preferências assim que a tela carrega. Sem isso o chipSala continuava
        // exibindo o texto default "Selecionar sala" mesmo depois de o usuário ter
        // escolhido uma sala na tela anterior — confundindo quem achava que a sala
        // não estava registrada.
        atualizarChipsFlutuantes()
    }

    /**
     * Sincroniza os chips flutuantes (sala atual e estado fixo) com o
     * PreferencesManager. Chamado no setupToolbar() e no onResume() para refletir
     * mudanças feitas em outras telas (ex.: SalaSelectionActivity ativando o modo
     * rápido).
     */
    private fun atualizarChipsFlutuantes() {
        val salaNome = preferencesManager.getCurrentSalaNome()
        binding.chipSala.text = when {
            !salaNome.isNullOrBlank() -> salaNome.take(22)
            else -> "Selecionar sala"
        }

        val estadoFixoHabilitado = preferencesManager.isEstadoFixoEnabled()
        val estadoFixo = preferencesManager.getEstadoFixo()
        if (estadoFixoHabilitado && !estadoFixo.isNullOrEmpty()) {
            val descricao = try {
                com.inventario.mobile.data.model.EstadoPatrimonio.valueOf(estadoFixo).descricao
            } catch (e: Exception) {
                estadoFixo
            }
            binding.chipEstado.text = descricao
        } else {
            binding.chipEstado.text = "BOM"
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
    
    private fun updateEstadoFixoVisually() {
        val habilitado = preferencesManager.isEstadoFixoEnabled()
        val estadoFixo = preferencesManager.getEstadoFixo()
        
        if (habilitado && !estadoFixo.isNullOrEmpty()) {
            binding.textEstadoFixoSelecionado.visibility = View.VISIBLE
            // ✅ CORREÇÃO: Mostrar descrição amigável ao invés do valor bruto
            val descricaoAmigavel = try {
                com.inventario.mobile.data.model.EstadoPatrimonio.valueOf(estadoFixo).descricao
            } catch (e: Exception) {
                estadoFixo // Fallback para o valor bruto se não conseguir converter
            }
            binding.textEstadoFixoSelecionado.text = descricaoAmigavel
            binding.switchFixarEstado.isChecked = true
        } else {
            binding.textEstadoFixoSelecionado.visibility = View.GONE
            if (habilitado && estadoFixo.isNullOrEmpty()) {
                binding.switchFixarEstado.isChecked = false
                preferencesManager.setEstadoFixoEnabled(false)
            }
        }
    }

    private fun setupButtonListeners() {
        // Inicializar switch de fixar estado
        val estadoHabilitado = preferencesManager.isEstadoFixoEnabled()
        val estadoFixo = preferencesManager.getEstadoFixo()
        binding.switchFixarEstado.isChecked = estadoHabilitado && !estadoFixo.isNullOrEmpty()
        updateEstadoFixoVisually()
        
        binding.switchFixarEstado.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && buttonView.isPressed) {
                val dialog = EstadoPatrimonioDialog.newInstance { estadoSelecionado ->
                    preferencesManager.setEstadoFixoEnabled(true)
                    preferencesManager.setEstadoFixo(estadoSelecionado.name)
                    updateEstadoFixoVisually()
                }
                dialog.show(supportFragmentManager, "EstadoPatrimonioDialogFixo")
            } else if (!isChecked && buttonView.isPressed) {
                preferencesManager.setEstadoFixoEnabled(false)
                preferencesManager.setEstadoFixo(null)
                updateEstadoFixoVisually()
            }
        }
        
        // Histórico de scans
        binding.cardContadorColetas.setOnClickListener {
            val intent = Intent(this, com.inventario.mobile.presentation.historico.HistoricoScansActivity::class.java)
            startActivity(intent)
        }

        // ✅ Feature scanner-coleta-sem-etiqueta (Tasks 2.1 + 2.2): FAB permanente "Coletar similar"
        // Abre o fluxo de coleta sem etiqueta via DescricaoSelectionActivity a qualquer momento,
        // sem depender de um scan prévio. Validações de sala/inventário estão em handleColetaSemEtiquetaClick().
        binding.fabColetarSemEtiqueta.setOnClickListener {
            handleColetaSemEtiquetaClick()
        }

        // ✅ NOVO: Botão Coletar Similar (sem etiqueta)
        binding.buttonColetarSimilar.setOnClickListener {
            android.util.Log.d("ScannerActivity", "=== BOTÃO COLETAR SIMILAR CLICADO ===")
            lastCollectedPatrimonio?.let { scanResult ->
                val salaId = preferencesManager.getCurrentSalaId()
                val salaNome = preferencesManager.getCurrentSalaNome()
                
                val descricao = scanResult.patrimonio?.descricao ?: "Item similar"
                
                android.util.Log.d("ScannerActivity", "Coletando similar ao patrimônio: ${scanResult.patrimonioCodigo}")
                android.util.Log.d("ScannerActivity", "Descrição: $descricao")
                android.util.Log.d("ScannerActivity", "Sala: $salaNome")
                
                if (salaId > 0 && !salaNome.isNullOrBlank()) {
                    // Mostrar diálogo de confirmação
                    showColetarSimilarDialog(descricao, salaId, salaNome)
                } else {
                    Toast.makeText(this, "Sala não selecionada", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                android.util.Log.e("ScannerActivity", "❌ ERRO: lastCollectedPatrimonio é null!")
                Toast.makeText(this, "Nenhum patrimônio de referência", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Chip de sala — abre seleção de sala
        binding.chipSala.setOnClickListener {
            // Mostrar sala atual ou pedir para selecionar
            val salaNome = preferencesManager.getCurrentSalaNome()
            if (!salaNome.isNullOrBlank()) {
                binding.chipSala.text = salaNome.take(22)
            }
            Toast.makeText(this, "Sala: ${salaNome ?: "Não selecionada"}", Toast.LENGTH_SHORT).show()
        }

        // Chip de estado — abre seleção de estado
        binding.chipEstado.setOnClickListener {
            val dialog = EstadoPatrimonioDialog.newInstance { estadoSelecionado ->
                preferencesManager.setEstadoFixoEnabled(true)
                preferencesManager.setEstadoFixo(estadoSelecionado.name)
                binding.chipEstado.text = estadoSelecionado.name
                updateEstadoFixoVisually()
            }
            dialog.show(supportFragmentManager, "EstadoChipDialog")
        }

        binding.buttonColetar.setOnClickListener {
            android.util.Log.d("ScannerActivity", "=== BOTÃO COLETAR CLICADO ===")
            currentScanResult?.let { result ->
                val salaId = preferencesManager.getCurrentSalaId()
                val salaNome = preferencesManager.getCurrentSalaNome()
                
                if (salaId > 0 && !salaNome.isNullOrBlank()) {
                    val estadoFixoHabilitado = preferencesManager.isEstadoFixoEnabled()
                    val estadoFixoVal = preferencesManager.getEstadoFixo()
                    
                    if (estadoFixoHabilitado && !estadoFixoVal.isNullOrEmpty()) {
                        viewModel.coletarPatrimonioComEstado(result.patrimonioId, salaNome, estadoFixoVal)
                    } else {
                        showEstadoPatrimonioDialog(result.patrimonioId, salaNome)
                    }
                } else {
                    Toast.makeText(this, "Sala não selecionada", Toast.LENGTH_SHORT).show()
                }
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
     * ✅ Feature scanner-coleta-sem-etiqueta (Task 2.2): Handler do FAB "Coletar similar".
     *
     * Fluxo:
     * 1. Lê o contexto atual do coletor a partir de `PreferencesManager`
     *    (sala atual e inventário ativo).
     * 2. Valida pré-condições:
     *    - Sala selecionada (salaId > 0 e salaNome não vazio) → requisitos 2.4 e 2.5
     *    - Inventário ativo existe (inventarioId != null e > 0) → requisito 7.2
     *    Se qualquer validação falhar, exibe um Toast e bloqueia a navegação.
     * 3. Caso válido, inicia `DescricaoSelectionActivity` repassando os extras
     *    `EXTRA_SALA_ID` (Long) e `EXTRA_SALA_NOME` (String) → requisitos 2.1, 2.2, 2.3.
     *
     * Esta View apenas lê do `PreferencesManager` e navega. Nenhuma regra de negócio
     * é implementada aqui (conforme steering `clean-architecture.md`). A persistência
     * da coleta é feita pela `DescricaoSelectionActivity` → ViewModel → Use Case.
     *
     * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 7.2
     */
    private fun handleColetaSemEtiquetaClick() {
        val salaId = preferencesManager.getCurrentSalaId()
        val salaNome = preferencesManager.getCurrentSalaNome()
        val inventarioId = preferencesManager.getInventarioAtivoId()

        android.util.Log.d(
            "ScannerActivity",
            "FAB 'Coletar similar' clicado — salaId=$salaId, salaNome=$salaNome, inventarioId=$inventarioId"
        )

        // Validação 1: sala válida (Requirements 2.4, 2.5)
        if (salaId <= 0 || salaNome.isNullOrBlank()) {
            android.util.Log.w(
                "ScannerActivity",
                "Coleta sem etiqueta bloqueada: sala não selecionada (salaId=$salaId, salaNome=$salaNome)"
            )
            Toast.makeText(
                this,
                "Selecione uma sala antes de coletar",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Validação 2: inventário ativo (Requirement 7.2)
        if (inventarioId == null || inventarioId <= 0) {
            android.util.Log.w(
                "ScannerActivity",
                "Coleta sem etiqueta bloqueada: sem inventário ativo (inventarioId=$inventarioId)"
            )
            Toast.makeText(
                this,
                "Não há inventário ativo. Não é possível registrar coleta.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Caso válido: navegar para DescricaoSelectionActivity (Requirements 2.1, 2.2, 2.3)
        val intent = Intent(this, DescricaoSelectionActivity::class.java).apply {
            putExtra(DescricaoSelectionActivity.EXTRA_SALA_ID, salaId.toLong())
            putExtra(DescricaoSelectionActivity.EXTRA_SALA_NOME, salaNome)
        }
        startActivity(intent)
    }

    /**
     * ✅ NOVO: Mostra diálogo de confirmação para coletar item similar
     */
    private fun showColetarSimilarDialog(descricao: String, salaId: Int, salaNome: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔄 Coletar Similar")
            .setMessage("Registrar coleta de item similar:\n\n\"$descricao\"\n\nLocal: $salaNome\n\nEste item será registrado SEM número de patrimônio.")
            .setPositiveButton("Coletar") { _, _ ->
                val estadoFixoHabilitado = preferencesManager.isEstadoFixoEnabled()
                val estadoFixoVal = preferencesManager.getEstadoFixo()
                if (estadoFixoHabilitado && !estadoFixoVal.isNullOrEmpty()) {
                    android.util.Log.d("ScannerActivity", "Utilizando estado fixo para similar: $estadoFixoVal")
                    registrarColetaSimilar(descricao, salaId, salaNome, estadoFixoVal)
                } else {
                    showEstadoDialogParaSimilar(descricao, salaId, salaNome)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * ✅ NOVO: Mostra diálogo de estado para coleta similar
     */
    private fun showEstadoDialogParaSimilar(descricao: String, salaId: Int, salaNome: String) {
        val dialog = EstadoPatrimonioDialog.newInstance { estadoSelecionado ->
            android.util.Log.d("ScannerActivity", "Estado selecionado para similar: ${estadoSelecionado.name}")
            registrarColetaSimilar(descricao, salaId, salaNome, estadoSelecionado.name)
        }
        dialog.show(supportFragmentManager, "EstadoPatrimonioDialogSimilar")
    }
    
    /**
     * ✅ NOVO: Registra coleta de item similar (sem número de patrimônio)
     */
    private fun registrarColetaSimilar(descricao: String, salaId: Int, salaNome: String, estadoConservacao: String) {
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        android.util.Log.d("ScannerActivity", "REGISTRANDO COLETA SIMILAR")
        android.util.Log.d("ScannerActivity", "Descrição: $descricao")
        android.util.Log.d("ScannerActivity", "Sala: $salaNome (ID: $salaId)")
        android.util.Log.d("ScannerActivity", "Estado: $estadoConservacao")
        android.util.Log.d("ScannerActivity", "═══════════════════════════════════════")
        
        binding.progressBar.visibility = View.VISIBLE
        binding.textStatus.text = "Registrando coleta similar..."
        
        lifecycleScope.launch {
            try {
                val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
                val usuarioId = preferencesManager.getUserId() ?: 0
                val usuarioNome = preferencesManager.getUserName() ?: "Usuário"
                
                // Usar o Use Case para registrar coleta por descrição
                registrarColetaUseCase.registrarColetaPorDescricao(
                    descricao = descricao,
                    salaId = salaId,
                    salaNome = salaNome,
                    estadoConservacao = estadoConservacao,
                    inventarioId = inventarioId,
                    usuarioId = usuarioId.toLong(),
                    usuarioNome = usuarioNome
                ).fold(
                    onSuccess = { coletaSalva ->
                        android.util.Log.d("ScannerActivity", "✅ Coleta similar registrada com sucesso!")
                        
                        runOnUiThread {
                            binding.progressBar.visibility = View.GONE
                            SoundUtils.playSuccessSound()
                            if (preferencesManager.isVibrationOnCollectionEnabled()) {
                                vibrationHelper.vibrateSuccess()
                            }
                            Toast.makeText(this@ScannerActivity, "✓ Coleta similar registrada!", Toast.LENGTH_SHORT).show()
                            
                            // Atualizar contador via ViewModel
                            viewModel.loadColetasCount()
                            
                            // Manter botão de coletar similar visível para continuar
                            binding.textStatus.text = "Coleta similar registrada! Pronto para mais."
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("ScannerActivity", "❌ Erro ao registrar coleta similar", error)
                        
                        runOnUiThread {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@ScannerActivity, "Erro: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
                
            } catch (e: Exception) {
                android.util.Log.e("ScannerActivity", "❌ Erro ao registrar coleta similar", e)
                
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ScannerActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * Reseta o estado do scanner para permitir nova leitura
     */
    private fun resetScannerState() {
        android.util.Log.d("ScannerActivity", "Resetando estado do scanner...")
        
        currentScanResult = null
        hasScannedOnce = false
        viewModel.clearScanResult()
        retryCount = 0
        
        // Fechar bottom sheet se estiver aberto
        if (binding.bottomSheet.visibility == View.VISIBLE) {
            hideBottomSheet()
        }

        // ✅ Feature scanner-coleta-sem-etiqueta (Task 3): Garantir que o FAB "Coletar similar"
        // esteja visível ao resetar o scanner, mesmo que o bottom sheet já estivesse oculto
        // (ex.: reset após erro). Validates: Requirements 1.2, 1.3, 8.3
        binding.fabColetarSemEtiqueta.show()

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
            if (preferencesManager.isVibrationOnCollectionEnabled()) {
                vibrationHelper.vibrateSuccess()
            }
            
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
            
            // ✅ v2.11: Registrar no histórico de scans
            lifecycleScope.launch {
                try {
                    registrarAcessoUseCase.registrarBasico(
                        numeroPatrimonio = result.patrimonioCodigo,
                        descricao = result.patrimonio?.descricao,
                        nomeSala = result.patrimonio?.salaNome,
                        salaId = result.patrimonio?.salaId?.toInt(),
                        tipoAcesso = com.inventario.mobile.domain.model.TipoAcesso.SCAN_QR,
                        jaColetado = result.jaColetado
                    )
                } catch (e: Exception) {
                    android.util.Log.w("ScannerActivity", "Erro ao registrar histórico de scan: ${e.message}")
                }
            }
            
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
        
        // ✅ NOVO: Salvar último patrimônio coletado para "Coletar Similar"
        if (currentScanResult != null) {
            lastCollectedPatrimonio = currentScanResult
        }
        
        currentScanResult = null
        hasScannedOnce = false
        viewModel.clearScanResult()
        retryCount = 0
        
        // Fechar bottom sheet com animação
        hideBottomSheet()
        
        binding.textStatus.text = "Pronto para escanear"
        android.util.Log.d("ScannerActivity", "Formulário limpo, pronto para próxima coleta")
        
        // ✅ Reiniciar scanner automaticamente para agilizar a coleta contínua
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                initializeScanner()
            }
        }, 300) // Aguardar a animação do bottom sheet concluir antes de reabrir a câmera
    }
    
    private fun showRetryInterface() {
        // v2.20.11: após erro (código não encontrado, patrimônio inexistente etc.)
        // o scanner ficava parado em "Tente escanear novamente" sem reabrir a
        // câmera. O usuário perdia o fluxo e tinha que sair da tela. Agora
        // limpamos o estado, escondemos bottom sheet e reabrimos o scanner
        // automaticamente após um pequeno delay — mesmo padrão usado em
        // `clearFormAndPrepareForNext` quando a coleta dá certo.
        android.util.Log.d("ScannerActivity", "showRetryInterface: recuperando fluxo após erro")

        // Fechar bottom sheet e preparar para novo scan
        hideBottomSheet()
        binding.textStatus.text = "Tente escanear novamente"

        currentScanResult = null
        hasScannedOnce = false
        viewModel.clearScanResult()
        retryCount = 0

        // Reabrir a câmera automaticamente — delay um pouco maior que o sucesso
        // para dar tempo do Toast de erro ser lido pelo usuário.
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                android.util.Log.d("ScannerActivity", "Reinicializando scanner após erro")
                initializeScanner()
            }
        }, 1200)
    }
    
    private fun displayPatrimonioInfo(result: ScanResult) {
        // ── Número + badge de status ──────────────────────────────
        binding.textPatrimonioNumero.text = "Nº ${result.patrimonioCodigo}"

        if (result.jaColetado) {
            binding.textPatrimonioStatus.text = "⚠ Já coletado"
            binding.textPatrimonioStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_orange_dark))
        } else {
            binding.textPatrimonioStatus.text = "✓ Disponível"
            binding.textPatrimonioStatus.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(this, com.inventario.mobile.R.color.success)
                )
        }

        // ── Descrição ─────────────────────────────────────────────
        binding.textPatrimonioDescricao.text = result.patrimonio?.descricao ?: "Sem descrição"

        // ── Painel de detalhes expandível ─────────────────────────
        // Resetar estado colapsado a cada novo scan
        binding.layoutDetalhesExpandidos.visibility = View.GONE
        binding.textVerDetalhes.text = "▼ Ver detalhes"

        val patrimonio = result.patrimonio
        val temDetalhes = !patrimonio?.marca.isNullOrBlank() ||
                !patrimonio?.modelo.isNullOrBlank() ||
                !patrimonio?.estado.isNullOrBlank() ||
                (patrimonio?.valor != null && patrimonio.valor > 0)

        if (temDetalhes) {
            binding.textVerDetalhes.visibility = View.VISIBLE

            // Preencher campos de detalhe
            binding.textDetalheMarca.text = patrimonio?.marca?.takeIf { it.isNotBlank() } ?: "—"
            binding.textDetalheModelo.text = patrimonio?.modelo?.takeIf { it.isNotBlank() } ?: "—"
            binding.textDetalheEstado.text = patrimonio?.estado?.takeIf { it.isNotBlank() } ?: "—"
            binding.textDetalheValor.text = patrimonio?.valor
                ?.takeIf { it > 0 }
                ?.let { "R$ %,.2f".format(it) }
                ?: "—"

            binding.textVerDetalhes.setOnClickListener {
                toggleDetalhes()
            }
        } else {
            binding.textVerDetalhes.visibility = View.GONE
        }

        // ── Sala + Responsável ────────────────────────────────────
        binding.textPatrimonioSala.text = result.patrimonio?.salaNome ?: "Sala não informada"
        binding.textPatrimonioResponsavel.text = result.patrimonio?.responsavelNome ?: "Não atribuído"

        // ── Informações de coleta anterior ────────────────────────
        if (result.jaColetado) {
            binding.layoutInfoColeta.visibility = View.VISIBLE
            binding.textColetadoPor.text = result.coletadoPor?.let { "Coletado por: $it" } ?: "Coletado por: Não informado"
            binding.textDataColeta.text = result.dataColetaFormatada?.let { "Data: $it" } ?: "Data: Não informada"
            val localizacao = result.patrimonio?.localizacaoEncontrada
            binding.textLocalizacaoEncontrada.text = localizacao?.let { "Local encontrado: $it" } ?: "Local encontrado: Não informado"
        } else {
            binding.layoutInfoColeta.visibility = View.GONE
        }
    }

    /**
     * Expande ou colapsa o painel de detalhes com animação suave.
     */
    private fun toggleDetalhes() {
        val painel = binding.layoutDetalhesExpandidos
        val link = binding.textVerDetalhes

        if (painel.visibility == View.GONE) {
            // Expandir
            painel.visibility = View.VISIBLE
            painel.alpha = 0f
            painel.animate().alpha(1f).setDuration(180).start()
            link.text = "▲ Ocultar detalhes"
        } else {
            // Colapsar
            painel.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction { painel.visibility = View.GONE }
                .start()
            link.text = "▼ Ver detalhes"
        }
    }

    private fun showCollectionInterface(result: ScanResult) {
        android.util.Log.d("ScannerActivity", "Mostrando bottom sheet — jaColetado=${result.jaColetado}")

        // ✅ Feature scanner-coleta-sem-etiqueta (Task 3): Esconder FAB "Coletar similar"
        // enquanto o bottom sheet está aberto, para não competir visualmente com os botões
        // do próprio bottom sheet (ex.: buttonColetarSimilar interno). O FAB volta a ser
        // exibido em hideBottomSheet() / resetScannerState(). Validates: Requirements 1.2, 1.3, 8.3
        binding.fabColetarSemEtiqueta.hide()

        // Mostrar overlay escuro
        binding.scrimOverlay.visibility = View.VISIBLE
        binding.scrimOverlay.alpha = 0f
        binding.scrimOverlay.animate().alpha(1f).setDuration(200).start()

        // Animar bottom sheet subindo
        binding.bottomSheet.visibility = View.VISIBLE
        binding.bottomSheet.translationY = binding.bottomSheet.height.toFloat().coerceAtLeast(600f)
        binding.bottomSheet.animate()
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        if (result.jaColetado) {
            binding.buttonColetar.visibility = View.GONE
            binding.buttonRetry.visibility = View.VISIBLE
            binding.buttonRetry.text = "Escanear outro"
            binding.buttonColetarSimilar.visibility = View.GONE
        } else {
            binding.buttonColetar.visibility = View.VISIBLE
            binding.buttonRetry.visibility = View.VISIBLE
            binding.buttonRetry.text = "Escanear outro"
            // Mostrar "Coletar similar" se houver referência
            if (lastCollectedPatrimonio?.patrimonio?.descricao.isNullOrBlank().not()) {
                binding.buttonColetarSimilar.visibility = View.VISIBLE
                binding.buttonColetarSimilar.text = "Coletar similar"
            } else {
                binding.buttonColetarSimilar.visibility = View.GONE
            }
        }
    }

    private fun hideBottomSheet() {
        binding.bottomSheet.animate()
            .translationY(binding.bottomSheet.height.toFloat().coerceAtLeast(600f))
            .setDuration(250)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                binding.bottomSheet.visibility = View.GONE
            }
            .start()

        binding.scrimOverlay.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction { binding.scrimOverlay.visibility = View.GONE }
            .start()

        // ✅ Feature scanner-coleta-sem-etiqueta (Task 3): Reexibir o FAB "Coletar similar"
        // quando o bottom sheet é fechado, mantendo o acesso permanente ao fluxo de coleta
        // sem etiqueta entre scans. Validates: Requirements 1.2, 1.3, 8.3
        binding.fabColetarSemEtiqueta.show()
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

    /**
     * ✅ Feature scanner-coleta-sem-etiqueta (Task 4): Atualiza o contador de coletas
     * exibido em `binding.textColetasCount` com o valor atual lido diretamente do
     * `PreferencesManager`.
     *
     * Este método é necessário porque o `ScannerViewModel` (via `updateUI(state)`)
     * só atualiza o contador quando o `StateFlow` emite um novo estado. Quando o
     * usuário retorna de outra Activity (ex.: `DescricaoSelectionActivity`) após
     * registrar uma coleta sem etiqueta, o `PreferencesManager` já foi incrementado,
     * mas o `StateFlow` do ViewModel pode não ter sido reemitido — então lemos
     * diretamente do `PreferencesManager` para garantir que o contador reflita
     * imediatamente o valor correto ao voltar.
     *
     * Validates: Requirements 6.2, 6.3, 7.3
     */
    private fun atualizarContadorColetas() {
        val count = preferencesManager.getCollectionCount()
        android.util.Log.d("ScannerActivity", "atualizarContadorColetas: count=$count")
        binding.textColetasCount.text = count.toString()
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

        // ✅ Feature scanner-coleta-sem-etiqueta (Task 4): Garantir consistência da UI
        // ao retornar de outras Activities (ex.: DescricaoSelectionActivity).
        // - atualizarContadorColetas(): reflete imediatamente o PreferencesManager,
        //   mesmo que o ScannerViewModel não tenha reemitido o StateFlow.
        // - fabColetarSemEtiqueta.show(): rede de segurança para garantir que o FAB
        //   fique visível ao voltar (caso tenha sido escondido antes pelo bottom sheet).
        // Validates: Requirements 6.2, 6.3, 7.3
        atualizarContadorColetas()
        binding.fabColetarSemEtiqueta.show()

        // v2.20.11: manter os chips de sala e estado sincronizados com as
        // preferências quando o usuário volta de outras telas (ex.: trocou a
        // sala ou ativou o modo rápido na SalaSelectionActivity).
        atualizarChipsFlutuantes()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}
