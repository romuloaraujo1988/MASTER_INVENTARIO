package com.inventario.mobile.presentation.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inventario.mobile.databinding.FragmentStatisticsExportBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment para exportar relatórios
 */
@AndroidEntryPoint
class ExportFragment : Fragment() {

    private var _binding: FragmentStatisticsExportBinding? = null
    private val binding get() = _binding!!
    
    private var idInventario: Int = 0
    
    companion object {
        private const val ARG_INVENTARIO_ID = "inventario_id"
        
        fun newInstance(idInventario: Int) = ExportFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INVENTARIO_ID, idInventario)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idInventario = it.getInt(ARG_INVENTARIO_ID, 0)
        }
    }

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
