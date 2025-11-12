# Design Document - Gráficos e Visualizações Android

## Overview

Este documento descreve a arquitetura e design para implementação de gráficos no app Android, seguindo Clean Architecture + MVVM e mantendo compatibilidade com a estrutura existente.

## Architecture

### Camadas da Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  - DashboardFragment (UI)                                    │
│  - DashboardViewModel (gerencia estado)                      │
│  - DashboardState (sealed class)                             │
│  - ChartHelper (utilitário para configurar gráficos)         │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                              │
│  - ObterEstatisticasUseCase                                  │
│  - ObterColetasPorDiaUseCase                                 │
│  - ObterDistribuicaoPorSetorUseCase                          │
│  - DashboardStats (model)                                    │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                                │
│  - DashboardRepository (interface em domain)                 │
│  - DashboardRepositoryImpl (implementação)                   │
│  - DashboardApi (Retrofit)                                   │
│  - DashboardDao (Room)                                       │
│  - DashboardEntity (cache local)                             │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Domain Models

```kotlin
// domain/model/DashboardStats.kt
data class DashboardStats(
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualConclusao: Float,
    val ultimaAtualizacao: Long
) {
    val percentualColetados: Float
        get() = if (totalPatrimonios > 0) (coletados.toFloat() / totalPatrimonios) * 100 else 0f
    
    val percentualPendentes: Float
        get() = if (totalPatrimonios > 0) (pendentes.toFloat() / totalPatrimonios) * 100 else 0f
}

// domain/model/ColetaPorDia.kt
data class ColetaPorDia(
    val data: String, // formato: "yyyy-MM-dd"
    val quantidade: Int,
    val dataFormatada: String // formato: "dd/MM"
)

// domain/model/DistribuicaoPorSetor.kt
data class DistribuicaoPorSetor(
    val idSetor: Int,
    val nomeSetor: String,
    val quantidade: Int,
    val percentual: Float
)

// domain/model/RankingColetor.kt
data class RankingColetor(
    val idUsuario: Int,
    val nomeUsuario: String,
    val quantidadeColetas: Int,
    val posicao: Int
)

// domain/model/StatusPorSala.kt
data class StatusPorSala(
    val idSala: Int,
    val nomeSala: String,
    val coletados: Int,
    val pendentes: Int,
    val total: Int
)
```

### 2. Use Cases

```kotlin
// domain/usecase/ObterEstatisticasUseCase.kt
class ObterEstatisticasUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(idInventario: Int?): Result<DashboardStats> {
        return try {
            repository.obterEstatisticas(idInventario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// domain/usecase/ObterColetasPorDiaUseCase.kt
class ObterColetasPorDiaUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(dias: Int = 7): Result<List<ColetaPorDia>> {
        return try {
            if (dias <= 0 || dias > 30) {
                return Result.failure(IllegalArgumentException("Dias deve estar entre 1 e 30"))
            }
            repository.obterColetasPorDia(dias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// domain/usecase/ObterDistribuicaoPorSetorUseCase.kt
class ObterDistribuicaoPorSetorUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): Result<List<DistribuicaoPorSetor>> {
        return try {
            repository.obterDistribuicaoPorSetor()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. Repository Interface (Domain)

```kotlin
// domain/repository/DashboardRepository.kt
interface DashboardRepository {
    suspend fun obterEstatisticas(idInventario: Int?): Result<DashboardStats>
    suspend fun obterColetasPorDia(dias: Int): Result<List<ColetaPorDia>>
    suspend fun obterDistribuicaoPorSetor(): Result<List<DistribuicaoPorSetor>>
    suspend fun obterRankingColetores(limite: Int): Result<List<RankingColetor>>
    suspend fun obterStatusPorSala(): Result<List<StatusPorSala>>
}
```

### 4. Repository Implementation (Data)

```kotlin
// data/repository/DashboardRepositoryImpl.kt
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val dashboardDao: DashboardDao,
    @ApplicationContext private val context: Context
) : DashboardRepository {
    
    override suspend fun obterEstatisticas(idInventario: Int?): Result<DashboardStats> {
        return try {
            // Tentar buscar da API
            if (NetworkUtils.isOnline(context)) {
                val response = dashboardApi.getEstatisticas(idInventario)
                if (response.isSuccessful && response.body()?.success == true) {
                    val stats = response.body()!!.data
                    
                    // Salvar no cache
                    dashboardDao.salvarEstatisticas(
                        DashboardEntity(
                            id = 1,
                            totalPatrimonios = stats.totalPatrimonios,
                            coletados = stats.coletados,
                            pendentes = stats.pendentes,
                            percentualConclusao = stats.percentualConclusao,
                            ultimaAtualizacao = System.currentTimeMillis()
                        )
                    )
                    
                    return Result.success(stats)
                }
            }
            
            // Fallback para cache local
            val cached = dashboardDao.obterEstatisticas()
            if (cached != null) {
                Result.success(
                    DashboardStats(
                        totalPatrimonios = cached.totalPatrimonios,
                        coletados = cached.coletados,
                        pendentes = cached.pendentes,
                        percentualConclusao = cached.percentualConclusao,
                        ultimaAtualizacao = cached.ultimaAtualizacao
                    )
                )
            } else {
                Result.failure(Exception("Nenhum dado disponível"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 5. UI State

```kotlin
// presentation/dashboard/DashboardState.kt
sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    
    data class Success(
        val stats: DashboardStats,
        val coletasPorDia: List<ColetaPorDia>,
        val distribuicaoPorSetor: List<DistribuicaoPorSetor>,
        val isOffline: Boolean = false
    ) : DashboardState()
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : DashboardState()
}
```

### 6. ViewModel

```kotlin
// presentation/dashboard/DashboardViewModel.kt
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val obterEstatisticasUseCase: ObterEstatisticasUseCase,
    private val obterColetasPorDiaUseCase: ObterColetasPorDiaUseCase,
    private val obterDistribuicaoPorSetorUseCase: ObterDistribuicaoPorSetorUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    
    fun carregarDashboard(idInventario: Int? = null) {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            
            try {
                // Carregar dados em paralelo
                val statsDeferred = async { obterEstatisticasUseCase(idInventario) }
                val coletasDeferred = async { obterColetasPorDiaUseCase(7) }
                val distribuicaoDeferred = async { obterDistribuicaoPorSetorUseCase() }
                
                val statsResult = statsDeferred.await()
                val coletasResult = coletasDeferred.await()
                val distribuicaoResult = distribuicaoDeferred.await()
                
                if (statsResult.isSuccess && coletasResult.isSuccess && distribuicaoResult.isSuccess) {
                    _state.value = DashboardState.Success(
                        stats = statsResult.getOrThrow(),
                        coletasPorDia = coletasResult.getOrThrow(),
                        distribuicaoPorSetor = distribuicaoResult.getOrThrow()
                    )
                } else {
                    _state.value = DashboardState.Error("Erro ao carregar dados")
                }
            } catch (e: Exception) {
                _state.value = DashboardState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    fun retry() {
        carregarDashboard()
    }
}
```

### 7. Fragment/Activity

```kotlin
// presentation/dashboard/DashboardFragment.kt
@AndroidEntryPoint
class DashboardFragment : Fragment() {
    
    private val viewModel: DashboardViewModel by viewModels()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupListeners()
        
        viewModel.carregarDashboard()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is DashboardState.Idle -> hideLoading()
                    is DashboardState.Loading -> showLoading()
                    is DashboardState.Success -> handleSuccess(state)
                    is DashboardState.Error -> handleError(state)
                }
            }
        }
    }
    
    private fun handleSuccess(state: DashboardState.Success) {
        hideLoading()
        
        // Atualizar cards de estatísticas
        binding.tvTotalPatrimonios.text = state.stats.totalPatrimonios.toString()
        binding.tvColetados.text = state.stats.coletados.toString()
        binding.tvPendentes.text = state.stats.pendentes.toString()
        binding.tvPercentual.text = "${state.stats.percentualConclusao.toInt()}%"
        
        // Configurar gráfico de pizza
        setupPieChart(state.stats)
        
        // Configurar gráfico de barras
        setupBarChart(state.coletasPorDia)
        
        // Configurar gráfico de distribuição
        setupDistributionChart(state.distribuicaoPorSetor)
        
        // Indicar se está offline
        if (state.isOffline) {
            binding.tvOfflineIndicator.visibility = View.VISIBLE
        }
    }
    
    private fun setupPieChart(stats: DashboardStats) {
        val pieChart = binding.pieChartStatus
        
        val entries = listOf(
            PieEntry(stats.coletados.toFloat(), "Coletados"),
            PieEntry(stats.pendentes.toFloat(), "Pendentes")
        )
        
        val dataSet = PieDataSet(entries, "Status").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.success),
                ContextCompat.getColor(requireContext(), R.color.error)
            )
            valueTextSize = 14f
            valueTextColor = Color.WHITE
        }
        
        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            setDrawEntryLabels(false)
            animateY(1000)
            invalidate()
        }
    }
}
```

## Data Flow

### Fluxo de Dados para Gráficos

```
1. User Action (carregar dashboard)
   ↓
2. ViewModel.carregarDashboard()
   ↓
3. Use Cases (paralelo)
   - ObterEstatisticasUseCase
   - ObterColetasPorDiaUseCase
   - ObterDistribuicaoPorSetorUseCase
   ↓
4. Repository
   - Tenta API (se online)
   - Fallback para cache local
   - Salva resultado no cache
   ↓
5. ViewModel atualiza State
   ↓
6. Fragment observa State
   ↓
7. UI atualiza gráficos
```

## Library Selection

### MPAndroidChart

**Escolhida**: MPAndroidChart v3.1.0

**Motivos**:
- Biblioteca madura e estável
- Suporte a diversos tipos de gráficos
- Boa performance
- Customizável
- Documentação completa

**Dependência**:
```gradle
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
```

## Chart Configurations

### Configurações Padrão

```kotlin
// presentation/dashboard/ChartHelper.kt
object ChartHelper {
    
    fun configurePieChart(chart: PieChart) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            setDrawHoleEnabled(true)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setHoleColor(Color.TRANSPARENT)
            animateY(1000, Easing.EaseInOutQuad)
        }
    }
    
    fun configureBarChart(chart: BarChart) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            animateY(1000, Easing.EaseInOutQuad)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
        }
    }
    
    fun configureLineChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setPinchZoom(true)
            animateX(1000, Easing.EaseInOutQuad)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
            }
            
            axisRight.isEnabled = false
        }
    }
}
```

## Performance Considerations

### Otimizações

1. **Cache de Dados**: Salvar estatísticas no Room para acesso offline
2. **Carregamento Paralelo**: Usar `async/await` para carregar múltiplos dados
3. **Lazy Loading**: Carregar gráficos sob demanda
4. **Debounce**: Evitar múltiplas requisições simultâneas
5. **Background Processing**: Calcular estatísticas em coroutine

## Testing Strategy

### Testes Unitários

```kotlin
// ObterEstatisticasUseCaseTest.kt
class ObterEstatisticasUseCaseTest {
    
    @Test
    fun `deve retornar estatísticas com sucesso`() = runTest {
        // Given
        val repository = mockk<DashboardRepository>()
        val useCase = ObterEstatisticasUseCase(repository)
        val expectedStats = DashboardStats(100, 60, 40, 60f, System.currentTimeMillis())
        
        coEvery { repository.obterEstatisticas(any()) } returns Result.success(expectedStats)
        
        // When
        val result = useCase(1)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedStats, result.getOrNull())
    }
}
```

## Error Handling

### Estratégias de Erro

1. **Sem Conexão**: Usar cache local e indicar modo offline
2. **API Falha**: Fallback para dados locais
3. **Sem Dados**: Exibir empty state com ilustração
4. **Timeout**: Retry automático com backoff exponencial
5. **Erro Desconhecido**: Mensagem genérica com botão de retry
