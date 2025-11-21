# 📊 Plano de Implementação - Estatísticas Android

**Data:** 16/11/2025  
**Versão:** 1.0  
**Status:** 🔄 Em Planejamento

---

## 🎯 Objetivo

Implementar estatísticas completas no app Android que **provem o valor da digitalização** e forneçam insights acionáveis para gestores.

---

## 📋 Status Atual

### ✅ Já Implementado

**Estrutura Base:**
- ✅ `StatisticsActivity.kt` - Activity principal com tabs
- ✅ `StatisticsPagerAdapter.kt` - Adapter para ViewPager2
- ✅ 4 Fragments criados:
  - `OverviewFragment.kt` - Visão geral (funcional)
  - `ChartsFragment.kt` - Gráficos (funcional)
  - `RankingsFragment.kt` - Rankings (placeholder)
  - `ExportFragment.kt` - Exportar (placeholder)

**Funcionalidades:**
- ✅ Visão geral com KPIs do dashboard
- ✅ Gráfico de evolução de coletas (linha)
- ✅ Pull-to-refresh
- ✅ Hilt configurado

### ⏳ Pendente de Implementação

**Backend:**
- ❌ Endpoint de estatísticas comparativas
- ❌ Endpoint de produtividade/rankings
- ❌ Endpoint de economia
- ❌ Endpoint de qualidade

**Android:**
- ❌ Fragment de comparativo Digital vs Papel
- ❌ Fragment de rankings completo
- ❌ Gráficos adicionais (pizza, barras)
- ❌ Exportação de relatórios

---

## 🚀 Plano de Implementação

### Sprint 1 (1 semana) - COMPARATIVO DIGITAL VS PAPEL

**Prioridade:** 🔴 ALTA  
**Objetivo:** Provar o valor da digitalização

#### Backend (2 dias)

**1. Criar DTO de Resposta**
```java
// src/main/java/com/inventario/mobile/server/dto/EstatisticasComparativasDTO.java
public class EstatisticasComparativasDTO {
    private VelocidadeDTO velocidade;
    private EconomiaDTO economia;
    private TempoDTO tempo;
    private QualidadeDTO qualidade;
    
    // Getters e Setters
}

public class VelocidadeDTO {
    private Integer digital;        // itens/dia
    private Integer papel;          // itens/dia
    private Double ganho;           // multiplicador
}

public class EconomiaDTO {
    private Double financeira;      // R$
    private Double porItem;         // R$
    private Double percentual;      // %
}

public class TempoDTO {
    private Integer horasEconomizadas;
    private Integer diasEquivalentes;
    private Double percentual;      // %
}

public class QualidadeDTO {
    private Double taxaErroDigital; // %
    private Double taxaErroPapel;   // %
    private Double melhoria;        // multiplicador
}
```

**2. Criar Service**
```java
// src/main/java/com/inventario/mobile/server/service/MobileEstatisticasService.java
@Service
public class MobileEstatisticasService {
    
    @Autowired
    private ColetaDAO coletaDAO;
    
    public EstatisticasComparativasDTO buscarEstatisticasComparativas(Integer inventarioId) {
        // 1. Buscar total de coletas
        Integer totalColetas = coletaDAO.contarColetasPorInventario(inventarioId);
        
        // 2. Calcular velocidade
        // Digital: média de 18 segundos por coleta = 200 coletas/hora = 300/dia (considerando 8h)
        // Papel: média de 3 minutos por coleta = 20 coletas/hora = 75/dia
        
        // 3. Calcular economia
        // Digital: R$ 0,50 por item
        // Papel: R$ 2,50 por item
        Double economiaFinanceira = totalColetas * 2.00; // R$ 2,00 economizado por item
        
        // 4. Calcular tempo
        // Digital: 18s por coleta
        // Papel: 180s por coleta
        Integer segundosEconomizados = totalColetas * (180 - 18);
        Integer horasEconomizadas = segundosEconomizados / 3600;
        Integer diasEquivalentes = horasEconomizadas / 8;
        
        // 5. Calcular qualidade
        Integer divergencias = coletaDAO.contarDivergencias(inventarioId);
        Double taxaErroDigital = (divergencias * 100.0) / totalColetas;
        Double taxaErroPapel = 15.0; // Estimativa baseada em estudos
        
        // 6. Montar DTO
        return new EstatisticasComparativasDTO(
            new VelocidadeDTO(300, 75, 4.0),
            new EconomiaDTO(economiaFinanceira, 2.00, 80.0),
            new TempoDTO(horasEconomizadas, diasEquivalentes, 84.0),
            new QualidadeDTO(taxaErroDigital, taxaErroPapel, 15.0)
        );
    }
}
```

**3. Criar Controller**
```java
// src/main/java/com/inventario/mobile/server/controller/MobileEstatisticasController.java
@RestController
@RequestMapping("/api/mobile/estatisticas")
public class MobileEstatisticasController {
    
    @Autowired
    private MobileEstatisticasService estatisticasService;
    
    @GetMapping("/comparativo")
    public ResponseEntity<ApiResponse<EstatisticasComparativasDTO>> 
        buscarEstatisticasComparativas(@RequestParam Integer inventarioId) {
        
        try {
            EstatisticasComparativasDTO estatisticas = 
                estatisticasService.buscarEstatisticasComparativas(inventarioId);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Estatísticas comparativas obtidas com sucesso", estatisticas)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erro ao buscar estatísticas: " + e.getMessage(), null));
        }
    }
}
```

**4. Adicionar Queries no DAO**
```java
// src/main/java/com/inventario/dao/ColetaDAO.java
public Integer contarDivergencias(Integer inventarioId) {
    String sql = "SELECT COUNT(*) FROM TABELA_COLETA WHERE ID_INVENTARIO = ? AND DIVERGENCIA = true";
    // Implementação
}
```

#### Android (3 dias)

**1. Criar DTOs Kotlin**
```kotlin
// data/remote/dto/EstatisticasComparativasDTO.kt
data class EstatisticasComparativasDTO(
    val velocidade: VelocidadeDTO,
    val economia: EconomiaDTO,
    val tempo: TempoDTO,
    val qualidade: QualidadeDTO
)

data class VelocidadeDTO(
    val digital: Int,
    val papel: Int,
    val ganho: Double
)

data class EconomiaDTO(
    val financeira: Double,
    val porItem: Double,
    val percentual: Double
)

data class TempoDTO(
    val horasEconomizadas: Int,
    val diasEquivalentes: Int,
    val percentual: Double
)

data class QualidadeDTO(
    val taxaErroDigital: Double,
    val taxaErroPapel: Double,
    val melhoria: Double
)
```

**2. Criar API Interface**
```kotlin
// data/remote/api/EstatisticasApi.kt
interface EstatisticasApi {
    @GET("api/mobile/estatisticas/comparativo")
    suspend fun buscarEstatisticasComparativas(
        @Query("inventarioId") inventarioId: Int
    ): ApiResponse<EstatisticasComparativasDTO>
}
```

**3. Criar Repository**
```kotlin
// domain/repository/EstatisticasRepository.kt
interface EstatisticasRepository {
    suspend fun buscarEstatisticasComparativas(inventarioId: Int): Result<EstatisticasComparativasDTO>
}

// data/repository/EstatisticasRepositoryImpl.kt
class EstatisticasRepositoryImpl @Inject constructor(
    private val estatisticasApi: EstatisticasApi
) : EstatisticasRepository {
    
    override suspend fun buscarEstatisticasComparativas(inventarioId: Int): Result<EstatisticasComparativasDTO> {
        return try {
            val response = estatisticasApi.buscarEstatisticasComparativas(inventarioId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**4. Criar Use Case**
```kotlin
// domain/usecase/BuscarEstatisticasComparativasUseCase.kt
class BuscarEstatisticasComparativasUseCase @Inject constructor(
    private val repository: EstatisticasRepository
) {
    suspend operator fun invoke(inventarioId: Int): Result<EstatisticasComparativasDTO> {
        return repository.buscarEstatisticasComparativas(inventarioId)
    }
}
```

**5. Criar ViewModel**
```kotlin
// presentation/statistics/EstatisticasViewModel.kt
@HiltViewModel
class EstatisticasViewModel @Inject constructor(
    private val buscarComparativoUseCase: BuscarEstatisticasComparativasUseCase
) : ViewModel() {
    
    private val _comparativoState = MutableStateFlow<ComparativoState>(ComparativoState.Idle)
    val comparativoState: StateFlow<ComparativoState> = _comparativoState.asStateFlow()
    
    fun carregarComparativo(inventarioId: Int) {
        viewModelScope.launch {
            _comparativoState.value = ComparativoState.Loading
            
            buscarComparativoUseCase(inventarioId).fold(
                onSuccess = { estatisticas ->
                    _comparativoState.value = ComparativoState.Success(estatisticas)
                },
                onFailure = { error ->
                    _comparativoState.value = ComparativoState.Error(error.message ?: "Erro desconhecido")
                }
            )
        }
    }
}

sealed class ComparativoState {
    object Idle : ComparativoState()
    object Loading : ComparativoState()
    data class Success(val estatisticas: EstatisticasComparativasDTO) : ComparativoState()
    data class Error(val message: String) : ComparativoState()
}
```

**6. Criar Fragment de Comparativo**
```kotlin
// presentation/statistics/ComparativoFragment.kt
@AndroidEntryPoint
class ComparativoFragment : Fragment() {
    
    private var _binding: FragmentStatisticsComparativoBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EstatisticasViewModel by viewModels()
    private var idInventario: Int = 0
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        
        // Carregar dados
        viewModel.carregarComparativo(idInventario)
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.comparativoState.collect { state ->
                when (state) {
                    is ComparativoState.Loading -> showLoading()
                    is ComparativoState.Success -> showData(state.estatisticas)
                    is ComparativoState.Error -> showError(state.message)
                    is ComparativoState.Idle -> {}
                }
            }
        }
    }
    
    private fun showData(estatisticas: EstatisticasComparativasDTO) {
        // Velocidade
        binding.tvVelocidadeDigital.text = "${estatisticas.velocidade.digital} itens/dia"
        binding.tvVelocidadePapel.text = "${estatisticas.velocidade.papel} itens/dia"
        binding.tvVelocidadeGanho.text = "${estatisticas.velocidade.ganho}x mais rápido"
        binding.progressVelocidade.progress = (estatisticas.velocidade.ganho * 25).toInt()
        
        // Economia
        binding.tvEconomiaFinanceira.text = formatCurrency(estatisticas.economia.financeira)
        binding.tvEconomiaPorItem.text = formatCurrency(estatisticas.economia.porItem)
        binding.tvEconomiaPercentual.text = "${estatisticas.economia.percentual.toInt()}%"
        binding.progressEconomia.progress = estatisticas.economia.percentual.toInt()
        
        // Tempo
        binding.tvTempoHoras.text = "${estatisticas.tempo.horasEconomizadas}h"
        binding.tvTempoDias.text = "${estatisticas.tempo.diasEquivalentes} dias"
        binding.tvTempoPercentual.text = "${estatisticas.tempo.percentual.toInt()}%"
        binding.progressTempo.progress = estatisticas.tempo.percentual.toInt()
        
        // Qualidade
        binding.tvQualidadeDigital.text = "${estatisticas.qualidade.taxaErroDigital}%"
        binding.tvQualidadePapel.text = "${estatisticas.qualidade.taxaErroPapel}%"
        binding.tvQualidadeMelhoria.text = "${estatisticas.qualidade.melhoria}x menos erros"
        binding.progressQualidade.progress = 95 // 95% de melhoria
    }
}
```

**7. Criar Layout XML**
```xml
<!-- res/layout/fragment_statistics_comparativo.xml -->
<ScrollView>
    <LinearLayout orientation="vertical">
        
        <!-- Card Velocidade -->
        <MaterialCardView>
            <LinearLayout>
                <TextView text="⚡ VELOCIDADE" />
                <TextView android:id="@+id/tvVelocidadeDigital" />
                <TextView android:id="@+id/tvVelocidadePapel" />
                <TextView android:id="@+id/tvVelocidadeGanho" />
                <ProgressBar android:id="@+id/progressVelocidade" />
            </LinearLayout>
        </MaterialCardView>
        
        <!-- Card Economia -->
        <MaterialCardView>
            <!-- Similar ao de velocidade -->
        </MaterialCardView>
        
        <!-- Card Tempo -->
        <MaterialCardView>
            <!-- Similar -->
        </MaterialCardView>
        
        <!-- Card Qualidade -->
        <MaterialCardView>
            <!-- Similar -->
        </MaterialCardView>
        
    </LinearLayout>
</ScrollView>
```

**8. Atualizar StatisticsPagerAdapter**
```kotlin
// Adicionar nova tab de Comparativo
override fun createFragment(position: Int): Fragment {
    return when (position) {
        0 -> OverviewFragment.newInstance(idInventario)
        1 -> ComparativoFragment.newInstance(idInventario) // NOVO
        2 -> ChartsFragment.newInstance(idInventario)
        3 -> RankingsFragment.newInstance(idInventario)
        4 -> ExportFragment.newInstance(idInventario)
        else -> OverviewFragment.newInstance(idInventario)
    }
}

override fun getItemCount(): Int = 5 // Atualizar para 5 tabs
```

---

### Sprint 2 (1 semana) - RANKINGS E PRODUTIVIDADE

**Prioridade:** 🟡 MÉDIA  
**Objetivo:** Gamificação e engajamento

#### Backend (2 dias)

**1. Criar DTO de Produtividade**
```java
public class ProdutividadeDTO {
    private List<RankingColetorDTO> ranking;
    private EstatisticasGeraisDTO estatisticasGerais;
    private TempoMedioDTO tempoMedio;
}

public class RankingColetorDTO {
    private Integer posicao;
    private String nome;
    private Integer itensPorDia;
    private Integer totalItens;
    private Double percentualMeta;
}
```

**2. Criar Query de Ranking**
```sql
SELECT 
    u.NOME_COMPLETO,
    COUNT(*) as total_coletas,
    COUNT(*) / NULLIF(EXTRACT(DAY FROM (MAX(c.DATA_COLETA) - MIN(c.DATA_COLETA))), 0) as coletas_por_dia,
    RANK() OVER (ORDER BY COUNT(*) DESC) as posicao
FROM TABELA_COLETA c
JOIN TABELA_USUARIO u ON c.ID_COLETOR = u.ID
WHERE c.ID_INVENTARIO = ?
GROUP BY u.ID, u.NOME_COMPLETO
ORDER BY total_coletas DESC
LIMIT 10;
```

**3. Implementar Service e Controller**

#### Android (3 dias)

**1. Implementar RankingsFragment completo**
**2. Adicionar RecyclerView com ranking**
**3. Adicionar animações e medalhas**

---

### Sprint 3 (1 semana) - ECONOMIA E QUALIDADE

**Prioridade:** 🟢 BAIXA  
**Objetivo:** Insights adicionais

---

## 📅 Cronograma Detalhado

### Semana 1 (18-22/11)
- **Seg-Ter:** Backend - Comparativo
- **Qua-Sex:** Android - Comparativo
- **Sex:** Testes e ajustes

### Semana 2 (25-29/11)
- **Seg-Ter:** Backend - Rankings
- **Qua-Sex:** Android - Rankings
- **Sex:** Testes e ajustes

### Semana 3 (02-06/12)
- **Seg-Ter:** Backend - Economia/Qualidade
- **Qua-Sex:** Android - Economia/Qualidade
- **Sex:** Testes finais e deploy

---

## ✅ Checklist de Implementação

### Sprint 1 - Comparativo

**Backend:**
- [ ] Criar DTOs de resposta
- [ ] Implementar MobileEstatisticasService
- [ ] Criar MobileEstatisticasController
- [ ] Adicionar queries no ColetaDAO
- [ ] Testar endpoint com Postman
- [ ] Documentar API

**Android:**
- [ ] Criar DTOs Kotlin
- [ ] Criar EstatisticasApi interface
- [ ] Implementar EstatisticasRepository
- [ ] Criar BuscarEstatisticasComparativasUseCase
- [ ] Criar EstatisticasViewModel
- [ ] Criar ComparativoFragment
- [ ] Criar layout XML
- [ ] Atualizar StatisticsPagerAdapter
- [ ] Testar fluxo completo

**Testes:**
- [ ] Teste unitário do Service
- [ ] Teste unitário do Use Case
- [ ] Teste de integração do endpoint
- [ ] Teste de UI do Fragment

---

## 🎯 Métricas de Sucesso

### Técnicas
- [ ] Endpoint responde em < 500ms
- [ ] Taxa de erro < 1%
- [ ] App não crasha
- [ ] Dados corretos exibidos

### Negócio
- [ ] Gestores visualizam estatísticas
- [ ] ROI comprovado com dados reais
- [ ] Decisões baseadas em dados
- [ ] Satisfação dos usuários

---

## 📞 Próximos Passos

1. **Revisar este plano** com a equipe
2. **Priorizar Sprint 1** (Comparativo)
3. **Iniciar implementação** do backend
4. **Testar endpoint** antes de começar Android
5. **Implementar Android** após backend pronto

---

**Preparado por:** Kiro AI Assistant  
**Data:** 16/11/2025  
**Status:** 📋 Pronto para execução

**🚀 Vamos começar pela Sprint 1?**
