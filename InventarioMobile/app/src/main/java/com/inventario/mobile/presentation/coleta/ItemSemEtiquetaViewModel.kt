package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para registro de itens sem etiqueta
 */
@HiltViewModel
class ItemSemEtiquetaViewModel @Inject constructor(
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _state = MutableStateFlow<ItemSemEtiquetaState>(ItemSemEtiquetaState.Idle)
    val state: StateFlow<ItemSemEtiquetaState> = _state.asStateFlow()
    
    /**
     * Registra item sem etiqueta
     */
    fun registrarItemSemEtiqueta(
        descricao: String,
        categoria: String,
        estado: String,
        localizacao: String,
        observacoes: String,
        fotoBase64: String
    ) {
        viewModelScope.launch {
            _state.value = ItemSemEtiquetaState.Loading
            
            try {
                // Obter ID do usuário logado
                val usuarioId = preferencesManager.getUserId() ?: 0
                
                // Criar coleta para item sem etiqueta
                val coleta = Coleta(
                    id = 0,
                    patrimonioId = 0, // Sem patrimônio (item sem etiqueta)
                    usuarioId = usuarioId.toLong(),
                    dataColeta = System.currentTimeMillis(),
                    localizacaoAtual = localizacao,
                    status = estado,
                    observacoes = observacoes,
                    latitude = null,
                    longitude = null,
                    sincronizado = false,
                    // Campos específicos para item sem etiqueta
                    semEtiqueta = true,
                    descricaoItemSemEtiqueta = descricao,
                    categoriaItemSemEtiqueta = categoria,
                    fotoPatrimonio = fotoBase64
                )
                
                // Registrar coleta
                val result = registrarColetaUseCase(coleta)
                
                if (result.isSuccess) {
                    _state.value = ItemSemEtiquetaState.Success
                } else {
                    val error = result.exceptionOrNull()
                    _state.value = ItemSemEtiquetaState.Error(
                        error?.message ?: "Erro ao registrar item sem etiqueta"
                    )
                }
            } catch (e: Exception) {
                _state.value = ItemSemEtiquetaState.Error(
                    e.message ?: "Erro desconhecido"
                )
            }
        }
    }
    
    /**
     * Limpa o estado
     */
    fun clearState() {
        _state.value = ItemSemEtiquetaState.Idle
    }
}

/**
 * Estados da tela de item sem etiqueta
 */
sealed class ItemSemEtiquetaState {
    object Idle : ItemSemEtiquetaState()
    object Loading : ItemSemEtiquetaState()
    object Success : ItemSemEtiquetaState()
    data class Error(val message: String) : ItemSemEtiquetaState()
}
