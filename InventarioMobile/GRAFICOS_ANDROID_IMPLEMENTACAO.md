# 📊 Implementação de Gráficos no App Android

## Visão Geral

Sistema completo de gráficos estatísticos usando **MPAndroidChart v3.1.0** para visualização de dados do inventário no app mobile.

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                    ChartsFragment                        │
│                   (UI Layer)                             │
└──────────────────────┬──────────────────────────────────┘
                       │ observa
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  ChartsViewModel                         │
│                  (Presentation)                          │
└──────────────────────┬──────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────┐
│                ChartDataProvider                         │
│                (Data Provider)                           │
└──────────────────────┬──────────────────────────────────┘
                       │ acessa
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Room DAOs (Local Database)                  │
│          PatrimonioDao, ColetaDao                        │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 Componentes Criados

### 1. ChartHelper (Kotlin Object)
**Localização**: `com.inventario.mobile.presentation.charts.ChartHelper`

Utilitário para configuração e estilização de gráficos MPAndroidChart.

**Métodos Principais**:
- `setupPieChart()` - Configura gráfico de pizza
- `setupBarChart()` - Configura gráfico de barras
- `setupLineChart()` - Configura gráfico de linhas
- `createPieDataSet()` - Cria dataset de pizza
- `createBarDataSet()` - Cria dataset de barras
- `createLineDataSet()` - Cria dataset de linhas
- `createProgressBarData()` - Dados de progresso
- `createStatusPieData()` - Dados de status
- `createEvolutionLineData()` - Dados de evolução
- `createTopItemsBarData()` - Dados de top itens

**Cores Padrão**:
```kotlin
Azul: #3498db
Verde: #2ecc71
Amarelo: #f1c40f
Vermelho: #e74c3c
Roxo: #9b59b6
Cinza: #95a5a6
Turquesa: #1abc9a
Laranja: #e67e22
```

### 2. ChartDataProvider
**Localização**: `com.inventario.mobile.presentation.charts.ChartDataProvider`

Provedor de dados que busca informações do banco local Room.

**Métodos**:
- `getStatusData()` - Status dos patrimônios
- `getProgressData(idInventario)` - Progresso da coleta
- `getEvolutionData(idInventario)` - Evolução diária
- `getTopItemsData(idInventario)` - Top 10 itens
- `getPatrimoniosPorSetor()` - Patrimônios por setor
- `getGeneralStats(idInventario)` - Estatísticas gerais

**Data Classes**:
```kotlin
data class StatusData(ativos, inativos, manutencao, baixados)
data class ProgressData(total, coletados, pendentes, percentual)
data class GeneralStats(...)
```

### 3. ChartsViewModel
**Localização**: `com.inventario.mobile.presentation.charts.ChartsViewModel`

ViewModel que gerencia estado dos gráficos usando StateFlow.

**StateFlows**:
- `progressData: StateFlow<ProgressData?>`
- `statusData: StateFlow<StatusData?>`
- `evolutionData: StateFlow<Map<String, Int>>`
- `topItemsData: StateFlow<Map<String, Int>>`
- `isLoading: StateFlow<Boolean>`
- `error: StateFlow<String?>`

**Métodos**:
- `loadChartData(idInventario)` - Carrega todos os dados
- `refresh(idInventario)` - Recarrega dados

### 4. ChartsFragment
**Localização**: `com.inventario.mobile.presentation.charts.ChartsFragment`

Fragment que exibe os gráficos na UI.

**Gráficos Exibidos**:
1. **Estatísticas Resumidas** (Cards) - Total, Coletados, Pendentes, %
2. **Progresso da Coleta** (Barras) - Coletados vs Pendentes
3. **Status dos Patrimônios** (Pizza) - Distribuição por status
4. **Evolução das Coletas** (Linhas) - Acumulado por dia
5. **Top 10 Itens** (Barras) - Mais coletados

---

## 🚀 Como Usar

### Exemplo 1: Adicionar ao Navigation

```kotlin
// No navigation graph (nav_graph.xml)
<fragment
    android:id="@+id/chartsFragment"
    android:name="com.inventario.mobile.presentation.charts.ChartsFragment"
    android:label="Gráficos"
    tools:layout="@layout/fragment_charts">
    <argument
        android:name="inventario_id"
        app:argType="integer"
        android:defaultValue="0" />
</fragment>
```

### Exemplo 2: Navegar para Gráficos

```kotlin
// De qualquer Fragment
val action = DashboardFragmentDirections.actionDashboardToCharts(idInventario)
findNavController().navigate(action)

// Ou usando Bundle
val bundle = Bundle().apply {
    putInt("inventario_id", idInventario)
}
findNavController().navigate(R.id.chartsFragment, bundle)
```

### Exemplo 3: Adicionar Menu Item

```kotlin
// No DashboardFragment ou MainActivity
override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.menu_dashboard, menu)
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_charts -> {
            val idInventario = viewModel.inventarioAtivo.value?.id ?: 0
            val action = DashboardFragmentDirections.actionDashboardToCharts(idInventario)
            findNavController().navigate(action)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
```

```xml
<!-- res/menu/menu_dashboard.xml -->
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item
        android:id="@+id/action_charts"
        android:title="Gráficos"
        android:icon="@drawable/ic_chart"
        app:showAsAction="ifRoom" />
</menu>
```

### Exemplo 4: Adicionar Botão no Dashboard

```xml
<!-- No layout do DashboardFragment -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnVerGraficos"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="📊 Ver Gráficos"
    android:textSize="16sp"
    app:icon="@drawable/ic_chart"
    app:iconGravity="start" />
```

```kotlin
// No DashboardFragment
binding.btnVerGraficos.setOnClickListener {
    val idInventario = viewModel.inventarioAtivo.value?.id ?: 0
    val fragment = ChartsFragment.newInstance(idInventario)
    
    parentFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .commit()
}
```

### Exemplo 5: Gráfico Individual em Outra Tela

```kotlin
// Adicionar apenas um gráfico de progresso
class ColetaFragment : Fragment() {
    
    private fun setupProgressChart() {
        val chartHelper = ChartHelper
        chartHelper.setupBarChart(binding.chartProgresso, listOf("Coletados", "Pendentes"))
        
        viewLifecycleOwner.lifecycleScope.launch {
            val progressData = chartDataProvider.getProgressData(idInventario)
            val barData = chartHelper.createProgressBarData(
                progressData.coletados,
                progressData.pendentes
            )
            binding.chartProgresso.data = barData
            binding.chartProgresso.invalidate()
        }
    }
}
```

---

## 🎨 Personalização

### Alterar Cores dos Gráficos

```kotlin
// No ChartHelper.kt
private val CHART_COLORS = intArrayOf(
    Color.rgb(52, 152, 219),   // Azul - Altere aqui
    Color.rgb(46, 204, 113),   // Verde
    // ...
)
```

### Customizar Estilo de Gráfico

```kotlin
// Exemplo: Gráfico de pizza sem buraco
fun setupSolidPieChart(chart: PieChart) {
    chart.apply {
        setDrawHoleEnabled(false) // Sem buraco
        setUsePercentValues(false) // Valores absolutos
        // ...
    }
}
```

### Adicionar Animações Customizadas

```kotlin
// Animação diferente
chart.animateXY(1500, 1500, Easing.EaseInOutQuad)

// Sem animação
chart.animateY(0)
```

---

## 📊 Queries SQL Necessárias

### Adicionar no ColetaDao

```kotlin
@Dao
interface ColetaDao {
    
    @Query("SELECT COUNT(DISTINCT id_patrimonio) FROM coleta WHERE id_inventario = :idInventario")
    suspend fun countByInventario(idInventario: Int): Int
    
    @Query("""
        SELECT DATE(data_coleta) as data, COUNT(*) as quantidade
        FROM coleta
        WHERE id_inventario = :idInventario
        GROUP BY DATE(data_coleta)
        ORDER BY DATE(data_coleta)
        LIMIT 30
    """)
    suspend fun getEvolutionData(idInventario: Int): List<EvolutionData>
    
    @Query("""
        SELECT p.descricao, COUNT(c.id) as quantidade
        FROM coleta c
        INNER JOIN patrimonio p ON c.id_patrimonio = p.id
        WHERE c.id_inventario = :idInventario
        GROUP BY p.descricao
        ORDER BY quantidade DESC
        LIMIT 10
    """)
    suspend fun getTopItems(idInventario: Int): List<TopItemData>
}

data class EvolutionData(val data: String, val quantidade: Int)
data class TopItemData(val descricao: String, val quantidade: Int)
```

### Adicionar no PatrimonioDao

```kotlin
@Dao
interface PatrimonioDao {
    
    @Query("SELECT COUNT(*) FROM patrimonio")
    suspend fun countAll(): Int
    
    @Query("SELECT COUNT(*) FROM patrimonio WHERE status = :status")
    suspend fun countByStatus(status: String): Int
    
    @Query("""
        SELECT s.nome as setor, COUNT(p.id) as quantidade
        FROM patrimonio p
        LEFT JOIN sala sa ON p.id_sala = sa.id
        LEFT JOIN setor s ON sa.id_setor = s.id
        GROUP BY s.nome
        ORDER BY quantidade DESC
        LIMIT 10
    """)
    suspend fun getPatrimoniosPorSetor(): List<SetorData>
}

data class SetorData(val setor: String, val quantidade: Int)
```

---

## ✅ Checklist de Implementação

- [x] Dependência MPAndroidChart no build.gradle
- [x] ChartHelper criado
- [x] ChartDataProvider criado
- [x] ChartsViewModel criado
- [x] ChartsFragment criado
- [x] Layout XML criado
- [x] Cores definidas
- [ ] Adicionar queries no ColetaDao
- [ ] Adicionar queries no PatrimonioDao
- [ ] Adicionar ao navigation graph
- [ ] Adicionar menu item
- [ ] Testar com dados reais
- [ ] Adicionar testes unitários

---

## 🐛 Troubleshooting

### Gráfico não aparece
- Verificar se MPAndroidChart está no build.gradle
- Verificar se há dados no banco local
- Verificar logs de erro no Logcat

### Dados não aparecem
- Verificar se queries estão corretas
- Verificar se há dados no Room
- Usar dados de exemplo (já implementado como fallback)

### Performance lenta
- Limitar quantidade de dados (LIMIT nas queries)
- Usar paginação para gráficos grandes
- Desabilitar animações se necessário

### Crash ao abrir
- Verificar se Fragment está no navigation graph
- Verificar se Hilt está configurado (@AndroidEntryPoint)
- Verificar se ViewModel está injetado corretamente

---

## 📚 Referências

- [MPAndroidChart Documentation](https://github.com/PhilJay/MPAndroidChart)
- [MPAndroidChart Wiki](https://github.com/PhilJay/MPAndroidChart/wiki)
- [Chart Examples](https://github.com/PhilJay/MPAndroidChart/tree/master/MPChartExample)

---

**Versão**: 1.0.0  
**Data**: 11/11/2025  
**Plataforma**: Android (Kotlin)
