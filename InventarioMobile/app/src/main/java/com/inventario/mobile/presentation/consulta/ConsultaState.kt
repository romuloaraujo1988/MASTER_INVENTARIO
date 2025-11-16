package com.inventario.mobile.presentation.consulta

import com.inventario.mobile.domain.model.PatrimonioConsulta

/**
 * Estados da UI de consulta de patrimônios
 * 
 * Sealed class para garantir type-safety
 */
sealed class ConsultaState {
    /**
     * Estado inicial - aguardando ação do usuário
     */
    object Idle : ConsultaState()
    
    /**
     * Estado de carregamento - buscando dados
     * 
     * @param message Mensagem de loading (opcional)
     */
    data class Loading(val message: String = "Buscando patrimônios...") : ConsultaState()
    
    /**
     * Estado de sucesso - dados carregados
     * 
     * @param patrimonios Lista de patrimônios encontrados
     * @param totalResultados Total de resultados (pode ser maior que a lista se houver paginação)
     * @param termoBusca Termo usado na busca
     */
    data class Success(
        val patrimonios: List<PatrimonioConsulta>,
        val totalResultados: Int = patrimonios.size,
        val termoBusca: String = ""
    ) : ConsultaState() {
        val isEmpty: Boolean get() = patrimonios.isEmpty()
        val hasResults: Boolean get() = patrimonios.isNotEmpty()
    }
    
    /**
     * Estado de erro - falha na busca
     * 
     * @param message Mensagem de erro
     * @param throwable Exceção original (opcional)
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : ConsultaState()
    
    /**
     * Estado de resultado vazio - busca bem-sucedida mas sem resultados
     * 
     * @param termoBusca Termo usado na busca
     * @param sugestoes Sugestões de busca alternativa
     */
    data class Empty(
        val termoBusca: String,
        val sugestoes: List<String> = emptyList()
    ) : ConsultaState()
}
