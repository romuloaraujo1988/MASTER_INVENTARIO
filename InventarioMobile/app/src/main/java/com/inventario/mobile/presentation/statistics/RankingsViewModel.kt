package com.inventario.mobile.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.SetorPendente
import com.inventario.mobile.domain.model.TopColetor
import com.inventario.mobile.domain.repository.RankingsRepository
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel da aba "Rankings" da tela de Estatísticas.
 *
 * Carrega em paralelo o ranking de coletores e o ranking de setores
 * pendentes para o inventário ativo. Cada lista tem um estado próprio
 * para que erros de uma não bloqueiem a outra.
 *
 * A projeção de término é tratada em [DashboardViewModelClean] (Overview),
 * portanto este ViewModel não a inclui.
 */
@HiltViewModel
class RankingsViewModel @Inject constructor(
    private val repository: RankingsRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _coletoresState = MutableStateFlow<RankingState<TopColetor>>(RankingState.Loading)
    val coletoresState: StateFlow<RankingState<TopColetor>> = _coletoresState.asStateFlow()

    private val _setoresState = MutableStateFlow<RankingState<SetorPendente>>(RankingState.Loading)
    val setoresState: StateFlow<RankingState<SetorPendente>> = _setoresState.asStateFlow()

    /**
     * Recarrega ambos os rankings em paralelo.
     *
     * Cada chamada usa o `inventarioId` ativo lido do `PreferencesManager`;
     * passar `null` para o servidor é equivalente, mas explicitar deixa
     * o log do servidor mais claro (e evita ambiguidade caso o usuário
     * troque o inventário ativo durante uma sessão).
     */
    fun carregar() {
        val invId = preferencesManager.getInventarioAtivoId()
        carregarColetores(invId)
        carregarSetores(invId)
    }

    private fun carregarColetores(invId: Int?) {
        _coletoresState.value = RankingState.Loading
        viewModelScope.launch {
            repository.buscarTopColetores(invId, limit = 10).fold(
                onSuccess = { lista ->
                    _coletoresState.value = if (lista.isEmpty()) {
                        RankingState.Empty
                    } else {
                        RankingState.Success(lista)
                    }
                },
                onFailure = { erro ->
                    _coletoresState.value = RankingState.Error(
                        erro.message ?: "Erro ao carregar coletores"
                    )
                }
            )
        }
    }

    private fun carregarSetores(invId: Int?) {
        _setoresState.value = RankingState.Loading
        viewModelScope.launch {
            repository.buscarSetoresPendentes(invId, limit = 10).fold(
                onSuccess = { lista ->
                    _setoresState.value = if (lista.isEmpty()) {
                        RankingState.Empty
                    } else {
                        RankingState.Success(lista)
                    }
                },
                onFailure = { erro ->
                    _setoresState.value = RankingState.Error(
                        erro.message ?: "Erro ao carregar setores"
                    )
                }
            )
        }
    }
}

/**
 * Estado genérico de uma lista de ranking — usado por ambas as listas
 * para manter a UI consistente.
 */
sealed class RankingState<out T> {
    object Loading : RankingState<Nothing>()
    object Empty : RankingState<Nothing>()
    data class Success<T>(val itens: List<T>) : RankingState<T>()
    data class Error(val mensagem: String) : RankingState<Nothing>()
}
