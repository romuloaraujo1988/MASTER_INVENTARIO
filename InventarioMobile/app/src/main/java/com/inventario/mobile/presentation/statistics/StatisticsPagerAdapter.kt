package com.inventario.mobile.presentation.statistics

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.inventario.mobile.presentation.charts.ChartsFragment

/**
 * Adapter para gerenciar as tabs de estatísticas
 */
class StatisticsPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val idInventario: Int = 0
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OverviewFragment.newInstance(idInventario)
            1 -> ChartsFragment.newInstance(idInventario)
            2 -> RankingsFragment.newInstance(idInventario)
            3 -> ExportFragment.newInstance(idInventario)
            else -> OverviewFragment.newInstance(idInventario)
        }
    }
}
