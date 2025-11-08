package com.inventario.mobile.presentation.statistics

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter para gerenciar as tabs de estatísticas
 */
class StatisticsPagerAdapter(fragmentActivity: FragmentActivity) : 
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OverviewFragment()
            1 -> ChartsFragment()
            2 -> RankingsFragment()
            3 -> ExportFragment()
            else -> OverviewFragment()
        }
    }
}
