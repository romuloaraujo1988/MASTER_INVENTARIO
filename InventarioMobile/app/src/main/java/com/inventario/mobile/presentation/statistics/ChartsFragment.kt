package com.inventario.mobile.presentation.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inventario.mobile.databinding.FragmentStatisticsChartsBinding

/**
 * Fragment para exibir gráficos
 */
class ChartsFragment : Fragment() {

    private var _binding: FragmentStatisticsChartsBinding? = null
    private val binding get() = _binding!!

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
        
        binding.tvPlaceholder.text = "📊 Gráficos\n\nEm desenvolvimento:\n• Evolução de Coletas\n• Status (Pizza)\n• Por Setor (Barras)\n• Performance de Coletores"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
