package com.inventario.mobile.presentation.coletas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.remote.api.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ColetasUiState(
    val isLoading: Boolean = false,
    val patrimoniosColetados: List<Patrimonio> = emptyList(),
    val patrimoniosPendentes: List<Patrimonio> = emptyList(),
    val itensSemEtiqueta: List<Patrimonio> = emptyList(),  // v2.7: Itens sem etiqueta
    val totalColetados: Int = 0,
    val totalPendentes: Int = 0,
    val totalSemEtiqueta: Int = 0,  // v2.7: Total de itens sem etiqueta
    val errorMessage: String? = null,
    val filtroAtivo: FiltroColeta = FiltroColeta.TODOS  // v2.7: Filtro ativo
)

/**
 * Enum para filtros de coleta
 */
enum class FiltroColeta {
    TODOS,           // Todas as coletas
    COM_ETIQUETA,    // Apenas coletas com número de patrimônio
    SEM_ETIQUETA     // Apenas itens sem etiqueta
}

/**
 * ViewModel para tela de Coletas
 * 
 * CORREÇÃO 26/11/2025: Agora busca coletas diretamente do servidor
 * via endpoint /api/mobile/coletas/all para garantir dados atualizados
 */
class ColetasViewModel(
    private val repository: InventarioRepository,
    private val apiService: ApiService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColetasUiState())
    val uiState: StateFlow<ColetasUiState> = _uiState.asStateFlow()

    /**
     * Carrega coletas do SERVIDOR (prioridade) com fallback para dados locais
     */
    fun loadColetas(filtrarPorUsuario: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                android.util.Log.d("ColetasViewModel", "═══════════════════════════════════════")
                android.util.Log.d("ColetasViewModel", "CARREGANDO COLETAS DO SERVIDOR")
                android.util.Log.d("ColetasViewModel", "═══════════════════════════════════════")
                
                // PRIORIDADE 1: Buscar coletas do servidor via endpoint /all
                val coletasDoServidor = buscarColetasDoServidor()
                
                if (coletasDoServidor != null) {
                    android.util.Log.d("ColetasViewModel", "✓ ${coletasDoServidor.size} coletas carregadas do servidor")
                    
                    // v2.7: Separar coletas com etiqueta e sem etiqueta
                    val coletasComEtiqueta = coletasDoServidor.filter { it.semEtiqueta != true }
                    val coletasSemEtiqueta = coletasDoServidor.filter { it.semEtiqueta == true }
                    
                    android.util.Log.d("ColetasViewModel", "📊 Com etiqueta: ${coletasComEtiqueta.size}, Sem etiqueta: ${coletasSemEtiqueta.size}")
                    
                    // Converter coletas COM etiqueta para patrimônios
                    val patrimoniosColetados = coletasComEtiqueta.map { coleta ->
                        Patrimonio(
                            id = coleta.idPatrimonio?.toLong() ?: 0L,
                            numeroPatrimonio = coleta.numeroPatrimonio ?: "",
                            descricao = coleta.descricaoPatrimonio ?: "Sem descrição",
                            marca = null,
                            modelo = null,
                            numeroSerie = null,
                            estado = coleta.estadoEncontrado,
                            valor = null,
                            setorId = null,
                            setorNome = null,
                            salaId = coleta.idSala?.toLong(),
                            salaNome = coleta.nomeSala ?: coleta.localizacaoEncontrada,
                            responsavelId = null,
                            responsavelNome = null,
                            coletado = true,
                            dataColeta = coleta.dataColeta,
                            coletadoPor = coleta.nomeUsuario,
                            observacoesColeta = coleta.observacao,
                            observacoes = null
                        )
                    }
                    
                    // v2.7: Converter coletas SEM etiqueta para patrimônios (com descrição especial)
                    val itensSemEtiqueta = coletasSemEtiqueta.map { coleta ->
                        Patrimonio(
                            id = coleta.id ?: 0L,  // Usar ID da coleta
                            numeroPatrimonio = "[SEM ETIQUETA]",  // Marcador visual
                            descricao = coleta.descricaoItemSemEtiqueta ?: coleta.descricaoPatrimonio ?: "Item sem etiqueta",
                            marca = coleta.categoriaItemSemEtiqueta,  // Usar marca para categoria
                            modelo = null,
                            numeroSerie = null,
                            estado = coleta.estadoEncontrado,
                            valor = null,
                            setorId = null,
                            setorNome = null,
                            salaId = coleta.idSala?.toLong(),
                            salaNome = coleta.nomeSala ?: coleta.localizacaoEncontrada,
                            responsavelId = null,
                            responsavelNome = null,
                            coletado = true,
                            dataColeta = coleta.dataColeta,
                            coletadoPor = coleta.nomeUsuario,
                            observacoesColeta = coleta.observacao,
                            observacoes = "Categoria: ${coleta.categoriaItemSemEtiqueta ?: "Não informada"}"
                        )
                    }
                    
                    // Buscar total de patrimônios para calcular pendentes
                    val totalPatrimonios = buscarTotalPatrimonios()
                    val totalPendentes = totalPatrimonios - patrimoniosColetados.size
                    
                    android.util.Log.d("ColetasViewModel", "Total patrimônios: $totalPatrimonios")
                    android.util.Log.d("ColetasViewModel", "Coletados (com etiqueta): ${patrimoniosColetados.size}")
                    android.util.Log.d("ColetasViewModel", "Itens sem etiqueta: ${itensSemEtiqueta.size}")
                    android.util.Log.d("ColetasViewModel", "Pendentes: $totalPendentes")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        patrimoniosColetados = patrimoniosColetados,
                        patrimoniosPendentes = emptyList(),
                        itensSemEtiqueta = itensSemEtiqueta,  // v2.7
                        totalColetados = patrimoniosColetados.size,
                        totalPendentes = totalPendentes,
                        totalSemEtiqueta = itensSemEtiqueta.size  // v2.7
                    )
                    
                    android.util.Log.d("ColetasViewModel", "✓ Coletas carregadas com sucesso do servidor!")
                    return@launch
                }
                
                // FALLBACK: Se servidor falhar, usar dados locais
                android.util.Log.w("ColetasViewModel", "⚠️ Servidor indisponível, usando dados locais")
                loadColetasLocal(filtrarPorUsuario)
                
            } catch (e: Exception) {
                android.util.Log.e("ColetasViewModel", "✗ Erro ao carregar coletas do servidor", e)
                
                // Tentar fallback local
                try {
                    loadColetasLocal(filtrarPorUsuario)
                } catch (localError: Exception) {
                    android.util.Log.e("ColetasViewModel", "✗ Erro também no fallback local", localError)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar coletas: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Busca coletas do servidor via API
     */
    private suspend fun buscarColetasDoServidor(): List<ColetaResponse>? {
        return try {
            val api = apiService ?: return null
            
            android.util.Log.d("ColetasViewModel", "Chamando endpoint /api/mobile/coletas/all...")
            val response = api.buscarTodasColetasSemPaginacao()
            
            if (response.isSuccessful && response.body() != null) {
                val coletasAllResponse = response.body()!!
                if (coletasAllResponse.success && coletasAllResponse.data != null) {
                    val pagedData = coletasAllResponse.data
                    android.util.Log.d("ColetasViewModel", "✓ Resposta do servidor: ${pagedData.content.size} coletas de ${pagedData.totalElements} total")
                    pagedData.content.map { dto ->
                        ColetaResponse(
                            id = dto.id,
                            idPatrimonio = dto.patrimonioId,
                            numeroPatrimonio = dto.numeroPatrimonio,
                            descricaoPatrimonio = dto.descricaoPatrimonio,
                            idSala = dto.idSala,
                            nomeSala = dto.nomeSala,
                            localizacaoEncontrada = dto.localizacaoEncontrada,
                            estadoEncontrado = dto.estadoEncontrado,
                            observacao = dto.observacoes,
                            dataColeta = dto.dataColeta,
                            nomeUsuario = dto.nomeColetor,
                            semEtiqueta = dto.semEtiqueta ?: false,  // v2.7
                            descricaoItemSemEtiqueta = dto.descricaoItemSemEtiqueta,  // v2.7
                            categoriaItemSemEtiqueta = dto.categoriaItemSemEtiqueta   // v2.7
                        )
                    }
                } else {
                    android.util.Log.w("ColetasViewModel", "Resposta do servidor sem dados")
                    null
                }
            } else {
                android.util.Log.e("ColetasViewModel", "Erro HTTP: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ColetasViewModel", "Erro ao buscar coletas do servidor", e)
            null
        }
    }
    
    /**
     * Busca total de patrimônios do servidor
     */
    private suspend fun buscarTotalPatrimonios(): Int {
        return try {
            val api = apiService ?: return 0
            
            val response = api.getDashboardStats()
            if (response.isSuccessful && response.body() != null) {
                val stats = response.body()!!
                if (stats.success && stats.data != null) {
                    stats.data.totalPatrimonios
                } else 0
            } else 0
        } catch (e: Exception) {
            android.util.Log.e("ColetasViewModel", "Erro ao buscar total de patrimônios", e)
            0
        }
    }
    
    /**
     * Fallback: Carrega coletas do banco local (SQLite)
     */
    private suspend fun loadColetasLocal(filtrarPorUsuario: Boolean) {
        android.util.Log.d("ColetasViewModel", "Carregando coletas do banco LOCAL...")
        
        val patrimonios = repository.getAllPatrimoniosList()
        android.util.Log.d("ColetasViewModel", "Total de patrimônios locais: ${patrimonios.size}")
        
        val usuarioAtual = repository.getCurrentUser()
        
        val coletados = if (filtrarPorUsuario && usuarioAtual != null) {
            patrimonios.filter { it.coletado == true && it.coletadoPor == usuarioAtual.nome }
        } else {
            patrimonios.filter { it.coletado == true }
        }
        
        val pendentes = patrimonios.filter { it.coletado != true }
        
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            patrimoniosColetados = coletados,
            patrimoniosPendentes = pendentes,
            totalColetados = coletados.size,
            totalPendentes = pendentes.size,
            errorMessage = "⚠️ Dados locais (servidor indisponível)"
        )
    }
    
    fun toggleFiltroUsuario() {
        viewModelScope.launch {
            val usuarioAtual = repository.getCurrentUser()
            val filtrarPorUsuario = !(_uiState.value.patrimoniosColetados.firstOrNull()?.let { 
                usuarioAtual?.nome == it.coletadoPor 
            } ?: true)
            loadColetas(filtrarPorUsuario)
        }
    }

    fun getColetasPorSala(): Map<String, List<Patrimonio>> {
        val coletados = _uiState.value.patrimoniosColetados
        return coletados.groupBy { it.salaNome ?: "Sala não definida" }
    }

    fun getPendentesPorSala(): Map<String, List<Patrimonio>> {
        val pendentes = _uiState.value.patrimoniosPendentes
        return pendentes.groupBy { it.salaNome ?: "Sala não definida" }
    }
    
    /**
     * v2.7: Aplica filtro de tipo de coleta
     */
    fun aplicarFiltro(filtro: FiltroColeta) {
        android.util.Log.d("ColetasViewModel", "Aplicando filtro: $filtro")
        _uiState.value = _uiState.value.copy(filtroAtivo = filtro)
    }
    
    /**
     * v2.7: Retorna coletas filtradas baseado no filtro ativo
     */
    fun getColetasFiltradas(): List<Patrimonio> {
        val state = _uiState.value
        return when (state.filtroAtivo) {
            FiltroColeta.TODOS -> state.patrimoniosColetados + state.itensSemEtiqueta
            FiltroColeta.COM_ETIQUETA -> state.patrimoniosColetados
            FiltroColeta.SEM_ETIQUETA -> state.itensSemEtiqueta
        }
    }
    
    /**
     * v2.7: Retorna itens sem etiqueta
     */
    fun getItensSemEtiqueta(): List<Patrimonio> {
        return _uiState.value.itensSemEtiqueta
    }
}

/**
 * Data class para resposta de coleta do servidor
 * v2.7: Adicionados campos para itens sem etiqueta
 */
data class ColetaResponse(
    val id: Long?,
    val idPatrimonio: Int?,
    val numeroPatrimonio: String?,
    val descricaoPatrimonio: String?,
    val idSala: Int?,
    val nomeSala: String?,
    val localizacaoEncontrada: String?,
    val estadoEncontrado: String?,
    val observacao: String?,
    val dataColeta: String?,
    val nomeUsuario: String?,
    val semEtiqueta: Boolean? = false,  // v2.7: Flag de item sem etiqueta
    val descricaoItemSemEtiqueta: String? = null,  // v2.7: Descrição do item sem etiqueta
    val categoriaItemSemEtiqueta: String? = null   // v2.7: Categoria do item sem etiqueta
)

class ColetasViewModelFactory(
    private val repository: InventarioRepository,
    private val apiService: ApiService? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColetasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ColetasViewModel(repository, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
