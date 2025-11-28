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
     * v2.7: Usa método específico para itens sem etiqueta
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
                android.util.Log.d("ItemSemEtiquetaVM", "═══════════════════════════════════════════")
                android.util.Log.d("ItemSemEtiquetaVM", "🏷️ Registrando item SEM ETIQUETA")
                android.util.Log.d("ItemSemEtiquetaVM", "   Descrição: $descricao")
                android.util.Log.d("ItemSemEtiquetaVM", "   Categoria: $categoria")
                android.util.Log.d("ItemSemEtiquetaVM", "   Estado: $estado")
                android.util.Log.d("ItemSemEtiquetaVM", "   Localização: $localizacao")
                
                // v2.7: Usar método específico para itens sem etiqueta
                val result = registrarColetaUseCase.registrarItemSemEtiqueta(
                    descricao = descricao,
                    categoria = categoria,
                    salaId = null,  // TODO: Obter sala selecionada se disponível
                    localizacaoAtual = localizacao,
                    estadoEncontrado = estado,
                    observacoes = observacoes,
                    latitude = null,
                    longitude = null,
                    fotoBase64 = fotoBase64
                )
                
                if (result.isSuccess) {
                    android.util.Log.d("ItemSemEtiquetaVM", "✓ Item sem etiqueta registrado com sucesso!")
                    _state.value = ItemSemEtiquetaState.Success
                } else {
                    val error = result.exceptionOrNull()
                    android.util.Log.e("ItemSemEtiquetaVM", "❌ Erro: ${error?.message}")
                    _state.value = ItemSemEtiquetaState.Error(
                        error?.message ?: "Erro ao registrar item sem etiqueta"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemSemEtiquetaVM", "❌ Exceção: ${e.message}", e)
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
