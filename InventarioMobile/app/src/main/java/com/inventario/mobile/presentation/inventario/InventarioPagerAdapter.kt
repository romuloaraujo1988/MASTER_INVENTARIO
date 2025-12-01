package com.inventario.mobile.presentation.inventario

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter para ViewPager2 da tela de Inventário.
 * Gerencia duas abas: Por Responsável e Por Sala.
 */
class InventarioPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
    
    companion object {
        const val TAB_POR_RESPONSAVEL = 0
        const val TAB_POR_SALA = 1
        const val TAB_COUNT = 2
        
        val TAB_TITLES = arrayOf(
            "Por Responsável",
            "Por Sala"
        )
    }
    
    override fun getItemCount(): Int = TAB_COUNT
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            TAB_POR_RESPONSAVEL -> InventarioPorResponsavelFragment.newInstance()
            TAB_POR_SALA -> InventarioPorSalaFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }
    
    /**
     * Retorna o título da aba para a posição especificada.
     */
    fun getTabTitle(position: Int): String {
        return if (position < TAB_TITLES.size) TAB_TITLES[position] else ""
    }
}
