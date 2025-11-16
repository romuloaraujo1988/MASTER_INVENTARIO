package com.inventario.mobile.presentation.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.inventario.mobile.R
import com.inventario.mobile.data.local.database.AppDatabase
import com.inventario.mobile.databinding.FragmentStatisticsChartsBinding
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment para exibir gráficos de estatísticas
 * Inclui gráfico de evolução de coletas ao longo do tempo
 */
@AndroidEntryPoint
class ChartsFragment : Fragment() {

    private var _binding: FragmentStatisticsChartsBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var database: AppDatabase
    
    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCharts()
        loadChartData()
    }
    
    private fun setupCharts() {
        // Configurar gráfico de linha (evolução)
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            
            // Configurar eixo X
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.DKGRAY
            }
            
            // Configurar eixo Y esquerdo
            axisLeft.apply {
                setDrawGridLines(true)
                textColor = Color.DKGRAY
                axisMinimum = 0f
            }
            
            // Desabilitar eixo Y direito
            axisRight.isEnabled = false
            
            // Configurar legenda
            legend.apply {
                textColor = Color.DKGRAY
                textSize = 12f
            }
        }
    }
    
    private fun loadChartData() {
        val inventarioId = preferencesManager.getInventarioAtivoId()
        
        if (inventarioId == null || inventarioId <= 0) {
            binding.tvPlaceholder.visibility = View.VISIBLE
            binding.tvPlaceholder.text = "⚠️ Nenhum inventário ativo\n\nSelecione um inventário para visualizar os gráficos"
            binding.lineChart.visibility = View.GONE
            return
        }
        
        binding.tvPlaceholder.visibility = View.GONE
        binding.lineChart.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Buscar dados de evolução
                val evolutionData = database.coletaDao().getEvolutionData(inventarioId)
                
                if (evolutionData.isEmpty()) {
                    binding.tvPlaceholder.visibility = View.VISIBLE
                    binding.tvPlaceholder.text = "📊 Sem dados de coletas\n\nRealize coletas para visualizar o gráfico de evolução"
                    binding.lineChart.visibility = View.GONE
                    return@launch
                }
                
                // Preparar dados para o gráfico
                val entries = evolutionData.mapIndexed { index, data ->
                    Entry(index.toFloat(), data.quantidade.toFloat())
                }
                
                val labels = evolutionData.map { it.data }
                
                // Criar dataset
                val dataSet = LineDataSet(entries, "Coletas por Dia").apply {
                    color = Color.parseColor("#2196F3") // Azul
                    setCircleColor(Color.parseColor("#2196F3"))
                    lineWidth = 2.5f
                    circleRadius = 4f
                    setDrawCircleHole(false)
                    valueTextSize = 10f
                    valueTextColor = Color.DKGRAY
                    setDrawFilled(true)
                    fillColor = Color.parseColor("#E3F2FD") // Azul claro
                    mode = LineDataSet.Mode.CUBIC_BEZIER // Linha suave
                }
                
                // Configurar dados no gráfico
                val lineData = LineData(dataSet)
                binding.lineChart.data = lineData
                
                // Configurar labels do eixo X
                binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                
                // Atualizar gráfico
                binding.lineChart.invalidate()
                
                // Animar gráfico
                binding.lineChart.animateX(1000)
                
            } catch (e: Exception) {
                binding.tvPlaceholder.visibility = View.VISIBLE
                binding.tvPlaceholder.text = "❌ Erro ao carregar gráficos\n\n${e.message}"
                binding.lineChart.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
