package com.inventario.mobile.presentation.dashboard

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.inventario.mobile.R
import com.inventario.mobile.databinding.FragmentDashboardBinding
import com.inventario.mobile.utils.VoiceSearchManager
import com.inventario.mobile.utils.VoiceCommandParser
import com.inventario.mobile.utils.CommandAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment do Dashboard
 * Clean Architecture + MVVM + Hilt
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel Clean injetado via Hilt
    private val viewModel: DashboardViewModelClean by viewModels()
    
    private lateinit var voiceSearchManager: VoiceSearchManager
    private lateinit var voiceCommandParser: VoiceCommandParser
    private var micAnimator: ObjectAnimator? = null

    companion object {
        private const val TAG = "DashboardFragment"
    }
    
    // Launcher para solicitar permissão de áudio
    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceSearch()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissão de áudio necessária para busca por voz",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Iniciando criação da view")
        
        try {
            _binding = FragmentDashboardBinding.inflate(inflater, container, false)
            Log.d(TAG, "onCreateView: Binding inflado com sucesso")
            return binding.root
        } catch (e: Exception) {
            Log.e(TAG, "onCreateView: Erro ao inflar binding", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: Iniciando configuração da view")
        
        try {
            super.onViewCreated(view, savedInstanceState)
            Log.d(TAG, "onViewCreated: super.onViewCreated executado")
            
            // Inicializar gerenciadores de voz
            voiceSearchManager = VoiceSearchManager(requireContext())
            voiceCommandParser = VoiceCommandParser()
            
            setupUI()
            Log.d(TAG, "onViewCreated: setupUI executado com sucesso")
            
            setupVoiceSearch()
            Log.d(TAG, "onViewCreated: setupVoiceSearch executado com sucesso")
            
            observeViewModel()
            Log.d(TAG, "onViewCreated: observeViewModel executado com sucesso")
            
            // Carregar dados do dashboard
            viewModel.loadDashboardData()
            Log.d(TAG, "onViewCreated: loadDashboardData executado com sucesso")
            
            // Carregar dados do gráfico
            viewModel.loadColetasEvolucao()
            Log.d(TAG, "onViewCreated: loadColetasEvolucao executado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: Erro durante configuração da view", e)
        }
    }

    private fun setupUI() {
        // Configurar SwipeRefreshLayout
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.success,
            R.color.info
        )
        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Pull-to-refresh acionado")
            viewModel.refreshData()
        }
        
        // Estatísticas acessíveis via Navigation Drawer
        
        // Cards de navegação removidos - usar Navigation Drawer ou botões de ação
        
        binding.btnQuickScan.setOnClickListener {
            try {
                // Navegar para seleção de sala (com otimizações de duplicação)
                val intent = Intent(requireContext(), com.inventario.mobile.presentation.sala.SalaSelectionActivity::class.java)
                intent.putExtra("COLETA_TIPO", "QRCODE")
                startActivity(intent)
                Log.d(TAG, "Navegando para SalaSelectionActivity (QR Code)")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao navegar para SalaSelectionActivity", e)
                android.widget.Toast.makeText(requireContext(), "Erro ao abrir seleção de sala", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // Botão de coleta manual
        binding.btnManualCollection.setOnClickListener {
            try {
                // Navegar para seleção de sala (com otimizações de duplicação)
                val intent = Intent(requireContext(), com.inventario.mobile.presentation.sala.SalaSelectionActivity::class.java)
                intent.putExtra("COLETA_TIPO", "MANUAL")
                startActivity(intent)
                Log.d(TAG, "Navegando para SalaSelectionActivity (Manual)")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao navegar para SalaSelectionActivity", e)
                android.widget.Toast.makeText(requireContext(), "Erro ao abrir seleção de sala", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Botão de visualizar coletas
        binding.btnViewCollections.setOnClickListener {
            try {
                val intent = Intent(requireContext(), com.inventario.mobile.presentation.coleta.CollectionViewActivity::class.java)
                startActivity(intent)
                Log.d(TAG, "Navegando para CollectionViewActivity")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao navegar para CollectionViewActivity", e)
                android.widget.Toast.makeText(requireContext(), "Erro ao abrir visualização de coletas", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // Botão de coleta por descrição (sem patrimônio)
        binding.btnDescriptionCollection.setOnClickListener {
            try {
                // Navegar para seleção de sala (com otimizações de duplicação)
                val intent = Intent(requireContext(), com.inventario.mobile.presentation.sala.SalaSelectionActivity::class.java)
                intent.putExtra("COLETA_TIPO", "DESCRICAO")
                startActivity(intent)
                Log.d(TAG, "Navegando para SalaSelectionActivity (Descrição)")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao navegar para SalaSelectionActivity", e)
                android.widget.Toast.makeText(requireContext(), "Erro ao abrir seleção de sala", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // Usar repeatOnLifecycle para melhor gerenciamento do ciclo de vida
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: DashboardUiStateClean) {
        try {
            Log.d(TAG, "updateUI: Atualizando UI com estado: $state")
            
            // Dados do usuário e estatísticas acessíveis via Navigation Drawer e Estatísticas
            state.dashboardStats?.let { stats ->
                Log.d(TAG, "updateUI: Estatísticas recebidas - Coletados: ${stats.totalColetados}, Pendentes: ${stats.totalPendentes}, Divergências: ${stats.divergencias}, Coletores: ${stats.coletoresAtivos}")
                
                // Atualizar KPIs detalhados
                try {
                    animateNumber(binding.tvKpiColetados, stats.totalColetados)
                    animateNumber(binding.tvKpiPendentes, stats.totalPendentes)
                    animateNumber(binding.tvKpiDivergencias, stats.divergencias)
                    animateNumber(binding.tvKpiColetores, stats.coletoresAtivos)
                    
                    Log.d(TAG, "updateUI: KPIs atualizados com sucesso")
                } catch (e: Exception) {
                    Log.e(TAG, "updateUI: Erro ao atualizar KPIs", e)
                }
            } ?: run {
                Log.w(TAG, "updateUI: dashboardStats é null")
            }
            
            // Atualizar estado de loading
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = state.isLoading
            
            // Atualizar mensagem de erro
            if (state.error != null) {
                binding.tvError.text = state.error
                binding.tvError.visibility = View.VISIBLE
                Log.w(TAG, "Erro no dashboard: ${state.error}")
            } else {
                binding.tvError.visibility = View.GONE
            }
            
            Log.d(TAG, "UI atualizada com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar UI", e)
        }
    }
    
    private fun formatNumber(number: Int): String {
        return String.format("%,d", number).replace(",", ".")
    }
    
    private fun formatNumberShort(number: Int): String {
        return when {
            number >= 1000000 -> String.format("%.1fM", number / 1000000.0)
            number >= 1000 -> String.format("%.1fK", number / 1000.0)
            else -> number.toString()
        }
    }
    
    private fun formatCurrency(value: Double): String {
        return String.format("R$ %,.2f", value).replace(",", "X").replace(".", ",").replace("X", ".")
    }
    
    private fun animateNumber(textView: android.widget.TextView, targetValue: Int) {
        val animator = android.animation.ValueAnimator.ofInt(0, targetValue)
        animator.duration = 800
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            textView.text = formatNumber(animation.animatedValue as Int)
        }
        animator.start()
    }
    
    private fun animateProgressBar(progressBar: android.widget.ProgressBar, targetProgress: Int) {
        val animator = android.animation.ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress)
        animator.duration = 1000
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        animator.start()
    }
    
    private fun animateCardEntrance(view: View, delay: Long) {
        view.alpha = 0f
        view.translationY = 50f
        view.scaleX = 0.95f
        view.scaleY = 0.95f
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(delay)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    

    // ========== BUSCA POR VOZ ==========
    
    private fun setupVoiceSearch() {
        // Configurar FAB
        binding.fabVoiceSearch.setOnClickListener {
            Log.d(TAG, "FAB de voz clicado")
            requestAudioPermissionIfNeeded()
        }
        
        // Configurar botão cancelar
        binding.btnCancelVoice.setOnClickListener {
            Log.d(TAG, "Cancelar busca por voz")
            stopVoiceSearch()
        }
    }
    
    private fun requestAudioPermissionIfNeeded() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida
                startVoiceSearch()
            }
            
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Mostrar explicação
                Toast.makeText(
                    requireContext(),
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
    
    private fun startVoiceSearch() {
        Log.d(TAG, "Iniciando busca por voz")
        
        // Verificar disponibilidade
        if (!voiceSearchManager.isAvailable()) {
            Toast.makeText(
                requireContext(),
                "Reconhecimento de voz não disponível neste dispositivo",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Mostrar overlay
        binding.voiceListeningOverlay.visibility = View.VISIBLE
        binding.tvVoiceStatus.text = "Preparando..."
        binding.tvVoiceText.text = ""
        binding.tvVoiceSuggestions.visibility = View.VISIBLE
        
        // Iniciar animação do microfone
        startMicAnimation()
        
        // Iniciar reconhecimento
        voiceSearchManager.startListening(object : VoiceSearchManager.VoiceSearchListener {
            override fun onResults(text: String) {
                Log.d(TAG, "Resultado final: $text")
                binding.tvVoiceText.text = text
                binding.tvVoiceStatus.text = "Processando..."
                
                // Processar comando após pequeno delay
                Handler(Looper.getMainLooper()).postDelayed({
                    processVoiceCommand(text)
                }, 500)
            }
            
            override fun onError(error: String) {
                Log.e(TAG, "Erro no reconhecimento: $error")
                binding.tvVoiceStatus.text = error
                binding.tvVoiceStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.error)
                )
                
                // Fechar após 2 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    stopVoiceSearch()
                }, 2000)
            }
            
            override fun onReadyForSpeech() {
                Log.d(TAG, "Pronto para falar")
                binding.tvVoiceStatus.text = "Pode falar..."
                binding.tvVoiceStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
            }
            
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Começou a falar")
                binding.tvVoiceStatus.text = "Escutando..."
                binding.tvVoiceSuggestions.visibility = View.GONE
            }
            
            override fun onEndOfSpeech() {
                Log.d(TAG, "Terminou de falar")
                binding.tvVoiceStatus.text = "Processando..."
            }
            
            override fun onPartialResults(text: String) {
                Log.d(TAG, "Resultado parcial: $text")
                binding.tvVoiceText.text = text
            }
        })
    }
    
    private fun stopVoiceSearch() {
        Log.d(TAG, "Parando busca por voz")
        
        voiceSearchManager.stopListening()
        stopMicAnimation()
        
        binding.voiceListeningOverlay.visibility = View.GONE
        binding.tvVoiceStatus.setTextColor(
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )
    }
    
    private fun startMicAnimation() {
        micAnimator?.cancel()
        
        micAnimator = ObjectAnimator.ofFloat(
            binding.ivMicAnimation,
            "scaleX",
            1f, 1.2f, 1f
        ).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        // Animar também scaleY
        ObjectAnimator.ofFloat(
            binding.ivMicAnimation,
            "scaleY",
            1f, 1.2f, 1f
        ).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
    
    private fun stopMicAnimation() {
        micAnimator?.cancel()
        binding.ivMicAnimation.scaleX = 1f
        binding.ivMicAnimation.scaleY = 1f
    }
    
    private fun processVoiceCommand(text: String) {
        val command = voiceCommandParser.parse(text)
        
        Log.d(TAG, "Comando reconhecido: ${command.action}, parâmetro: ${command.parameter}")
        
        when (command.action) {
            CommandAction.BUSCAR_PATRIMONIO -> {
                command.parameter?.let { numero ->
                    Toast.makeText(
                        requireContext(),
                        "Buscando patrimônio $numero...",
                        Toast.LENGTH_SHORT
                    ).show()
                    buscarPatrimonio(numero)
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "Número do patrimônio não identificado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            CommandAction.MOSTRAR_SALA -> {
                command.parameter?.let { numero ->
                    Toast.makeText(
                        requireContext(),
                        "Abrindo sala $numero...",
                        Toast.LENGTH_SHORT
                    ).show()
                    mostrarSala(numero)
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "Número da sala não identificado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            CommandAction.LISTAR_DIVERGENCIAS -> {
                Toast.makeText(
                    requireContext(),
                    "Listando divergências...",
                    Toast.LENGTH_SHORT
                ).show()
                listarDivergencias()
            }
            
            CommandAction.LISTAR_PENDENTES -> {
                Toast.makeText(
                    requireContext(),
                    "Listando pendentes...",
                    Toast.LENGTH_SHORT
                ).show()
                listarPendentes()
            }
            
            CommandAction.LISTAR_COLETADOS -> {
                Toast.makeText(
                    requireContext(),
                    "Listando coletados...",
                    Toast.LENGTH_SHORT
                ).show()
                listarColetados()
            }
            
            CommandAction.SINCRONIZAR -> {
                Toast.makeText(
                    requireContext(),
                    "Sincronizando...",
                    Toast.LENGTH_SHORT
                ).show()
                sincronizar()
            }
            
            CommandAction.ABRIR_SCANNER -> {
                Toast.makeText(
                    requireContext(),
                    "Abrindo scanner...",
                    Toast.LENGTH_SHORT
                ).show()
                abrirScanner()
            }
            
            CommandAction.VOLTAR -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            
            CommandAction.UNKNOWN -> {
                Toast.makeText(
                    requireContext(),
                    "Comando não reconhecido: \"${command.originalText}\"",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        
        stopVoiceSearch()
    }
    
    // Métodos de ação dos comandos
    
    private fun buscarPatrimonio(numero: String) {
        Log.d(TAG, "Buscar patrimônio: $numero")
        try {
            val intent = Intent(requireContext(), com.inventario.mobile.presentation.search.QuickSearchActivity::class.java)
            intent.putExtra("SEARCH_QUERY", numero)
            intent.putExtra("AUTO_SEARCH", true)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônio", e)
            Toast.makeText(requireContext(), "Erro ao buscar patrimônio", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun mostrarSala(numero: String) {
        Log.d(TAG, "Mostrar sala: $numero")
        try {
            val intent = Intent(requireContext(), com.inventario.mobile.presentation.inventario.InventarioActivity::class.java)
            intent.putExtra("FILTER_SALA", numero)
            intent.putExtra("AUTO_FILTER", true)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao mostrar sala", e)
            Toast.makeText(requireContext(), "Erro ao abrir sala", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun listarDivergencias() {
        Log.d(TAG, "Listar divergências")
        try {
            val intent = Intent(requireContext(), com.inventario.mobile.presentation.inventario.InventarioActivity::class.java)
            intent.putExtra("FILTER_TYPE", "DIVERGENCIAS")
            intent.putExtra("AUTO_FILTER", true)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao listar divergências", e)
            Toast.makeText(requireContext(), "Erro ao listar divergências", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun listarPendentes() {
        Log.d(TAG, "Listar pendentes")
        try {
            val intent = Intent(requireContext(), com.inventario.mobile.presentation.sync.PendingCollectionsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao listar pendentes", e)
            Toast.makeText(requireContext(), "Erro ao listar pendentes", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun listarColetados() {
        Log.d(TAG, "Listar coletados")
        try {
            val intent = Intent(requireContext(), com.inventario.mobile.presentation.coleta.CollectionViewActivity::class.java)
            intent.putExtra("FILTER_STATUS", "COLETADO")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao listar coletados", e)
            Toast.makeText(requireContext(), "Erro ao listar coletados", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sincronizar() {
        Log.d(TAG, "Sincronizar")
        try {
            val intent = Intent(requireContext(), com.inventario.mobile.presentation.sync.SyncActivity::class.java)
            intent.putExtra("AUTO_SYNC", true)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao sincronizar", e)
            Toast.makeText(requireContext(), "Erro ao sincronizar", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun abrirScanner() {
        Log.d(TAG, "Abrir scanner")
        try {
            // Navegar para seleção de sala antes de abrir o scanner
            val intent = Intent(requireContext(), com.inventario.mobile.presentation.sala.SalaSelectionActivity::class.java)
            intent.putExtra("COLETA_TIPO", "QRCODE")
            intent.putExtra("FROM_VOICE", true)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir scanner", e)
            Toast.makeText(requireContext(), "Erro ao abrir scanner", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: Limpando binding")
        
        // Limpar recursos de voz
        voiceSearchManager.destroy()
        stopMicAnimation()
        
        super.onDestroyView()
        _binding = null
    }
}