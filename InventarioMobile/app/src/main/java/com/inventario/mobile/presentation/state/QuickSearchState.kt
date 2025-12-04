package com.inventario.mobile.presentation.state

import com.inventario.mobile.domain.model.PatrimonioComColeta

/**
 * Estados possíveis da tela de busca rápida
 * Regra: Sealed class para estados mutuamente exclusivos
 * 
 * @see Requirements 1.3, 4.1
 */
sealed class QuickSearchState {
    
    /**
     * Estado inicial - aguardando entrada do usuário
     */
    object Idle : QuickSearchState()
    
    /**
     * Estado de carregamento - busca em andamento
     */
    object Loading : QuickSearchState()
    
    /**
     * Estado de sucesso - resultados encontrados
     * @param resultados Lista de patrimônios encontrados
     * @param tempoMs Tempo de execução da busca em milissegundos
     */
    data class Success(
        val resultados: List<PatrimonioComColeta>,
        val tempoMs: Long
    ) : QuickSearchState()
    
    /**
     * Estado vazio - nenhum resultado encontrado
     * @param query Termo de busca que não retornou resultados
     */
    data class Empty(val query: String) : QuickSearchState()
    
    /**
     * Estado de erro - falha na busca
     * @param message Mensagem de erro
     */
    data class Error(val message: String) : QuickSearchState()
}

/**
 * Estatísticas da busca
 * 
 * @see Requirements 4.1, 4.2, 4.3
 */
data class SearchStats(
    val totalResultados: Int = 0,
    val coletados: Int = 0,
    val pendentes: Int = 0,
    val divergencias: Int = 0,
    val tempoMs: Long = 0
) {
    /**
     * Calcula porcentagem de coletados
     */
    fun getPercentualColetados(): Float {
        return if (totalResultados > 0) {
            (coletados.toFloat() / totalResultados) * 100
        } else {
            0f
        }
    }
    
    /**
     * Retorna resumo formatado
     */
    fun getResumoFormatado(): String {
        return buildString {
            append("$totalResultados resultado(s)")
            if (coletados > 0 || pendentes > 0) {
                append(" • $coletados coletado(s) • $pendentes pendente(s)")
            }
            if (divergencias > 0) {
                append(" • $divergencias divergência(s)")
            }
            append(" • ${tempoMs}ms")
        }
    }
}
