package com.inventario.mobile.domain.usecase

import com.inventario.mobile.data.model.Coleta
import javax.inject.Inject

/**
 * Use Case: Agrupar coletas por sala
 * 
 * Agrupa coletas pelo nome da sala, tratando salas nulas como "Sala não definida"
 * e ordenando os grupos alfabeticamente.
 * 
 * @see Requirements 10.1, 10.2, 10.3, 10.5
 */
class AgruparColetasPorSalaUseCase @Inject constructor() {
    
    companion object {
        const val SALA_NAO_DEFINIDA = "Sala não definida"
    }
    
    /**
     * Agrupa coletas por localização onde foram ENCONTRADAS
     * 
     * @param coletas Lista de coletas a agrupar
     * @return Map ordenado alfabeticamente com localização encontrada como chave e lista de coletas como valor
     */
    operator fun invoke(coletas: List<Coleta>): Map<String, List<Coleta>> {
        return coletas
            .groupBy { coleta ->
                // Usa localizacaoEncontrada - onde o item foi ENCONTRADO durante a coleta
                coleta.localizacaoEncontrada?.takeIf { it.isNotBlank() }
                    ?: SALA_NAO_DEFINIDA
            }
            .toSortedMap(compareBy { sala ->
                // "Sala não definida" vai para o final
                if (sala == SALA_NAO_DEFINIDA) "zzz$sala" else sala.lowercase()
            })
    }
    
    /**
     * Conta itens por grupo
     * 
     * @param coletasAgrupadas Map de coletas agrupadas por sala
     * @return Map com nome da sala e quantidade de itens
     */
    fun contarPorGrupo(coletasAgrupadas: Map<String, List<Coleta>>): Map<String, Int> {
        return coletasAgrupadas.mapValues { (_, coletas) -> coletas.size }
    }
    
    /**
     * Converte coletas agrupadas para lista flat com headers
     * Útil para exibição em RecyclerView com headers
     * 
     * @param coletasAgrupadas Map de coletas agrupadas por sala
     * @return Lista de ColetaListItem (Header ou Item)
     */
    fun toFlatListWithHeaders(coletasAgrupadas: Map<String, List<Coleta>>): List<ColetaListItem> {
        val result = mutableListOf<ColetaListItem>()
        
        coletasAgrupadas.forEach { (sala, coletas) ->
            // Adiciona header do grupo
            result.add(ColetaListItem.Header(sala, coletas.size))
            
            // Adiciona itens do grupo
            coletas.forEach { coleta ->
                result.add(ColetaListItem.Item(coleta))
            }
        }
        
        return result
    }
}

/**
 * Sealed class para representar itens na lista de coletas
 * Permite misturar headers e itens de coleta no mesmo RecyclerView
 */
sealed class ColetaListItem {
    /**
     * Header de grupo (nome da sala + quantidade)
     */
    data class Header(
        val nomeSala: String,
        val quantidade: Int
    ) : ColetaListItem()
    
    /**
     * Item de coleta
     */
    data class Item(
        val coleta: Coleta
    ) : ColetaListItem()
}
