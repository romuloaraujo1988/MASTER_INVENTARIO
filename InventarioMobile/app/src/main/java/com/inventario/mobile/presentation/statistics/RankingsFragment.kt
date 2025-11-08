package com.inventario.mobile.presentation.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inventario.mobile.databinding.FragmentStatisticsRankingsBinding

/**
 * Fragment para exibir rankings
 */
class RankingsFragment : Fragment() {

    private var _binding: FragmentStatisticsRankingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsRankingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvPlaceholder.text = "🏆 Rankings\n\nEm desenvolvimento:\n• Top Coletores\n• Setores com Mais Pendentes\n• Histórico de Coletas\n• Performance por Período"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
