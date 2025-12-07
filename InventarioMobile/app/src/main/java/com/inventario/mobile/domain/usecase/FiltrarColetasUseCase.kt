package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.domain.model.StatusFiltro
import javax.inject.Inject

/**
 * Use Case: Filtrar coletas
 * 
 * Aplica múltiplos filtros simultaneamente usando operação AND.
 * Todos os filtros ativos devem ser satisfeitos para uma coleta aparecer no resultado.
 * 
 * @see Requirements 2.1, 2.2, 2.3, 3.1, 3.2, 4.1, 4.2, 4.3, 8.1, 8.2, 8.3, 8.4
 */
class FiltrarColetasUseCase @Inject constructor() {
    
    /**
     * Aplica filtros na lista de coletas
     * 
     * @param coletas Lista de coletas a filtrar
     * @param usuarioAtual Nome do usuário logado (para filtro por usuário)
     * @param filtroUsuario Se true, filtra apenas coletas do usuário atual
     * @param filtroSala Nome da sala para filtrar (null = todas as salas)
     * @param filtroStatus Status para filtrar (TODOS, COLETADOS, PENDENTES)
     * @param textoPesquisa Texto para pesquisar no número ou descrição do patrimônio
     * @return Lista de coletas que atendem TODOS os critérios de filtro
     */
    operator fun invoke(
        coletas: List<Coleta>,
        usuarioAtual: String?,
        filtroUsuario: Boolean,
        filtroSala: String?,
        filtroStatus: StatusFiltro,
        textoPesquisa: String = ""
    ): List<Coleta> {
        var resultado = coletas
        
        // Filtro por usuário (Requirements 2.1, 2.2, 2.3)
        if (filtroUsuario && !usuarioAtual.isNullOrBlank()) {
            resultado = resultado.filter { coleta ->
                coleta.nomeColetor?.equals(usuarioAtual, ignoreCase = true) == true
            }
        }
        
        // Filtro por sala (Requirements 3.1, 3.2)
        // Prioridade: localizacaoEncontrada (onde FOI ENCONTRADO) > nomeSala (localização ORIGINAL)
        if (!filtroSala.isNullOrBlank()) {
            resultado = resultado.filter { coleta ->
                val salaColeta = (coleta.localizacaoEncontrada ?: coleta.nomeSala)?.trim()
                salaColeta?.equals(filtroSala.trim(), ignoreCase = true) == true
            }
        }
        
        // Filtro por status (Requirements 4.1, 4.2, 4.3)
        resultado = when (filtroStatus) {
            StatusFiltro.COLETADOS -> resultado.filter { it.sincronizado }
            StatusFiltro.PENDENTES -> resultado.filter { !it.sincronizado }
            StatusFiltro.SEM_ETIQUETA -> resultado.filter { coleta ->
                // CORREÇÃO: Usar campo semEtiqueta=true OU (numeroPatrimonio vazio E descricaoItemSemEtiqueta preenchido)
                coleta.semEtiqueta || 
                (coleta.numeroPatrimonio.isNullOrBlank() && !coleta.descricaoItemSemEtiqueta.isNullOrBlank())
            }
            StatusFiltro.TODOS -> resultado
        }
        
        // Filtro por texto de pesquisa
        if (textoPesquisa.isNotBlank()) {
            resultado = resultado.filter { coleta ->
                coleta.numeroPatrimonio?.contains(textoPesquisa, ignoreCase = true) == true ||
                coleta.descricaoPatrimonio?.contains(textoPesquisa, ignoreCase = true) == true
            }
        }
        
        return resultado
    }
    
    /**
     * Calcula contadores de coletas
     * 
     * @param coletas Lista de coletas (já filtrada ou não)
     * @return Par com (totalColetados, totalPendentes)
     */
    fun calcularContadores(coletas: List<Coleta>): Pair<Int, Int> {
        val coletados = coletas.count { it.sincronizado }
        val pendentes = coletas.count { !it.sincronizado }
        return Pair(coletados, pendentes)
    }
    
    /**
     * Extrai lista de salas únicas das coletas
     * Prioridade: localizacaoEncontrada (onde FOI ENCONTRADO) > nomeSala (localização ORIGINAL)
     * 
     * @param coletas Lista de coletas
     * @return Lista de nomes de salas ordenada alfabeticamente
     */
    fun extrairSalasDisponiveis(coletas: List<Coleta>): List<String> {
        return coletas
            .mapNotNull { it.localizacaoEncontrada ?: it.nomeSala }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}
