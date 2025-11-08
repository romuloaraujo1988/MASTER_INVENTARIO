package com.inventario.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.ui.coleta.ColetaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// @HiltViewModel
class ColetaViewModel /* @Inject constructor(
    private val patrimonioRepository: PatrimonioRepository,
    private val coletaRepository: ColetaRepository
) */ : ViewModel() {

    private val _uiState = MutableStateFlow(ColetaUiState())
    val uiState: StateFlow<ColetaUiState> = _uiState.asStateFlow()

    private var patrimonioId: Long = -1L
    private var salaId: Long = -1L
    private var salaNome: String = ""

    companion object {
        private const val TAG = "ColetaViewModel"
    }

    fun setSala(id: Long, nome: String) {
        Log.d(TAG, "setSala: id=$id, nome=$nome")
        salaId = id
        salaNome = nome
        _uiState.value = _uiState.value.copy(
            salaId = id,
            salaNome = nome,
            localizacao = nome  // Preencher automaticamente a localização com o nome da sala
        )
    }

    fun setPatrimonio(id: Long, codigo: String) {
        Log.d(TAG, "setPatrimonio: id=$id, codigo=$codigo")
        patrimonioId = id
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // TODO: Buscar dados completos do patrimônio no repositório
                // val patrimonio = patrimonioRepository.getPatrimonioById(id)
                
                // Por enquanto, usar dados simulados
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    patrimonioCodigo = codigo,
                    patrimonioDescricao = "Patrimônio $codigo - Descrição completa",
                    localizacao = salaNome
                )
                
                Log.d(TAG, "Patrimônio carregado com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar patrimônio", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar patrimônio: ${e.message}"
                )
            }
        }
    }

    fun salvarColeta(localizacao: String, observacoes: String) {
        Log.d(TAG, "=== INICIANDO SALVAMENTO DE COLETA ===")
        Log.d(TAG, "Localização: $localizacao")
        Log.d(TAG, "Observações: $observacoes")
        Log.d(TAG, "Sala ID: $salaId")
        Log.d(TAG, "Sala Nome: $salaNome")
        Log.d(TAG, "Patrimônio ID: $patrimonioId")
        Log.d(TAG, "Estado atual: ${_uiState.value}")
        
        if (salaId == -1L) {
            val errorMsg = "Nenhuma sala selecionada. Sala ID: $salaId, Nome: $salaNome"
            Log.e(TAG, errorMsg)
            _uiState.value = _uiState.value.copy(
                errorMessage = "Nenhuma sala selecionada"
            )
            return
        }
        
        if (patrimonioId == -1L) {
            val errorMsg = "Nenhum patrimônio selecionado. Patrimônio ID: $patrimonioId"
            Log.e(TAG, errorMsg)
            _uiState.value = _uiState.value.copy(
                errorMessage = "Nenhum patrimônio selecionado. Use o scanner QR Code."
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                Log.d(TAG, "Validações OK. Iniciando salvamento...")
                
                // TODO: Salvar coleta no repositório
                // val coleta = Coleta(
                //     patrimonioId = patrimonioId,
                //     salaId = salaId,
                //     localizacao = localizacao,
                //     observacoes = observacoes,
                //     dataColeta = Date(),
                //     sincronizado = false
                // )
                // coletaRepository.insertColeta(coleta)
                
                // Simular salvamento
                kotlinx.coroutines.delay(1000)
                
                Log.d(TAG, "Coleta salva com sucesso!")
                Log.d(TAG, "=== FIM DO SALVAMENTO ===")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar coleta", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao salvar coleta: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetState() {
        patrimonioId = -1L
        _uiState.value = ColetaUiState(
            salaId = salaId,
            salaNome = salaNome
        )
    }
}