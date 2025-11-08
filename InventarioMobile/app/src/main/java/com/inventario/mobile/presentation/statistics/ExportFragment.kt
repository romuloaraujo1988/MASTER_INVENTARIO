package com.inventario.mobile.presentation.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inventario.mobile.databinding.FragmentStatisticsExportBinding

/**
 * Fragment para exportar relatórios
 */
class ExportFragment : Fragment() {

    private var _binding: FragmentStatisticsExportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvPlaceholder.text = "📤 Exportar\n\nEm desenvolvimento:\n• Gerar PDF\n• Gerar Excel\n• Compartilhar via WhatsApp\n• Enviar por Email"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
