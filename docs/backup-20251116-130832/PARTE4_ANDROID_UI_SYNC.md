# 📱 Sistema de Sincronização Offline - Parte 4

## Android - Interface de Sincronização

### SyncViewModel.kt

```kotlin
class SyncViewModel(
    private val repository: SyncRepository
) : ViewModel() {
    
    private val _syncState = MutableLiveData<SyncState>()
    val syncState: LiveData<SyncState> = _syncState
    
    private val _stats = MutableLiveData<SyncStats>()
    val stats: LiveData<SyncStats> = _stats
    
    init {
        carregarEstatisticas()
    }
    
    fun sincronizarCompleto() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading("Baixando dados...")
            
            val result = repository.sincronizacaoCompleta()
            
            result.fold(
                onSuccess = { syncResult ->
                    _syncState.value = SyncState.Success(
                        "Sincronização completa!\n" +
                        "${syncResult.patrimonios} patrimônios\n" +
                        "${syncResult.salas} salas\n" +
                        "${syncResult.responsaveis} responsáveis"
                    )
                    carregarEstatisticas()
                },
                onFailure = { error ->
                    _syncState.value = SyncState.Error(
                        "Erro: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun sincronizarIncremental() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading("Verificando atualizações...")
            
            val result = repository.sincronizacaoIncremental()
            
            result.fold(
                onSuccess = { syncResult ->
                    if (syncResult.patrimonios == 0) {
                        _syncState.value = SyncState.Success(
                            "Dados já estão atualizados!"
                        )
                    } else {
                        _syncState.value = SyncState.Success(
                            "Atualizados ${syncResult.patrimonios} patrimônios"
                        )
                    }
                    carregarEstatisticas()
                },
                onFailure = { error ->
                    _syncState.value = SyncState.Error(
                        "Erro: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun sincronizarColetas() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading("Enviando coletas...")
            
            val result = repository.sincronizarColetasPendentes()
            
            result.fold(
                onSuccess = { count ->
                    _syncState.value = SyncState.Success(
                        if (count > 0) {
                            "Enviadas $count coleta(s)"
                        } else {
                            "Nenhuma coleta pendente"
                        }
                    )
                    carregarEstatisticas()
                },
                onFailure = { error ->
                    _syncState.value = SyncState.Error(
                        "Erro ao enviar coletas: ${error.message}"
                    )
                }
            )
        }
    }
    
    private fun carregarEstatisticas() {
        viewModelScope.launch {
            val stats = repository.obterEstatisticas()
            _stats.value = stats
        }
    }
}

sealed class SyncState {
    object Idle : SyncState()
    data class Loading(val message: String) : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}
```

### SyncFragment.kt

```kotlin
class SyncFragment : Fragment() {
    
    private lateinit var binding: FragmentSyncBinding
    private val viewModel: SyncViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSyncBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        // Observar estado da sincronização
        viewModel.syncState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SyncState.Idle -> {
                    hideLoading()
                }
                is SyncState.Loading -> {
                    showLoading(state.message)
                }
                is SyncState.Success -> {
                    hideLoading()
                    showSuccess(state.message)
                }
                is SyncState.Error -> {
                    hideLoading()
                    showError(state.message)
                }
            }
        }
        
        // Observar estatísticas
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            updateStats(stats)
        }
    }
    
    private fun setupListeners() {
        binding.btnSyncCompleto.setOnClickListener {
            confirmarSincronizacaoCompleta()
        }
        
        binding.btnSyncIncremental.setOnClickListener {
            viewModel.sincronizarIncremental()
        }
        
        binding.btnSyncColetas.setOnClickListener {
            viewModel.sincronizarColetas()
        }
    }
    
    private fun confirmarSincronizacaoCompleta() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sincronização Completa")
            .setMessage(
                "Isso irá baixar TODOS os dados novamente.\n\n" +
                "Pode demorar alguns minutos dependendo da conexão.\n\n" +
                "Deseja continuar?"
            )
            .setPositiveButton("Sim") { _, _ ->
                viewModel.sincronizarCompleto()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun updateStats(stats: SyncStats) {
        binding.apply {
            tvTotalPatrimonios.text = stats.totalPatrimonios.toString()
            tvTotalSalas.text = stats.totalSalas.toString()
            tvTotalResponsaveis.text = stats.totalResponsaveis.toString()
            tvColetasPendentes.text = stats.coletasPendentes.toString()
            
            // Formatar data da última sincronização
            if (stats.ultimaSincronizacao > 0) {
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                tvUltimaSincronizacao.text = formatter.format(Date(stats.ultimaSincronizacao))
            } else {
                tvUltimaSincronizacao.text = "Nunca sincronizado"
            }
            
            // Mostrar badge se houver coletas pendentes
            if (stats.coletasPendentes > 0) {
                badgeColetasPendentes.visibility = View.VISIBLE
                badgeColetasPendentes.text = stats.coletasPendentes.toString()
            } else {
                badgeColetasPendentes.visibility = View.GONE
            }
        }
    }
    
    private fun showLoading(message: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvLoadingMessage.visibility = View.VISIBLE
        binding.tvLoadingMessage.text = message
        binding.layoutButtons.isEnabled = false
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.tvLoadingMessage.visibility = View.GONE
        binding.layoutButtons.isEnabled = true
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.green_success))
            .show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red_error))
            .show()
    }
}
```

---

## Continua na Parte 5...
