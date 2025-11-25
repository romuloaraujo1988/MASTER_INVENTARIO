package com.inventario.mobile.domain.usecase

import com.inventario.mobile.generators.ColetaGenerators
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

/**
 * Property-Based Tests para AgruparColetasPorSalaUseCase
 * 
 * Testa propriedades de correção do sistema de agrupamento usando Kotest Property Testing.
 * Cada teste executa no mínimo 100 iterações com dados gerados aleatoriamente.
 */
class AgruparColetasPorSalaUseCasePropertyTest : StringSpec({
    
    val useCase = AgruparColetasPorSalaUseCase()
    
    /**
     * Feature: visualizacao-coletas, Property 15: Agrupamento por sala
     * Validates: Requirements 10.1, 10.3, 10.5
     * 
     * *For any* conjunto de coletas agrupadas, coletas da mesma sala devem estar juntas 
     * e ordenadas alfabeticamente por nome da sala
     */
    "Property 15: Agrupamento por sala - coletas da mesma sala devem estar juntas" {
        checkAll(100, ColetaGenerators.listaColetasComSalas()) { coletas ->
            val resultado = useCase(coletas)
            
            // Cada grupo deve conter apenas coletas da mesma sala
            resultado.forEach { (sala, coletasDoGrupo) ->
                coletasDoGrupo.all { coleta ->
                    val salaColeta = coleta.nomeSala?.takeIf { it.isNotBlank() }
                        ?: coleta.localizacaoAtual?.takeIf { it.isNotBlank() }
                        ?: AgruparColetasPorSalaUseCase.SALA_NAO_DEFINIDA
                    
                    salaColeta == sala
                } shouldBe true
            }
            
            // Total de coletas nos grupos deve ser igual ao total original
            resultado.values.flatten().size shouldBe coletas.size
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 15 (continuação): Ordenação alfabética
     * Validates: Requirements 10.5
     * 
     * *For any* conjunto de coletas agrupadas, os grupos devem estar ordenados 
     * alfabeticamente por nome da sala
     */
    "Property 15: Agrupamento por sala - grupos devem estar ordenados alfabeticamente" {
        checkAll(100, ColetaGenerators.listaColetasComSalas()) { coletas ->
            val resultado = useCase(coletas)
            
            val chaves = resultado.keys.toList()
            val chavesOrdenadas = chaves.sortedWith(compareBy { sala ->
                if (sala == AgruparColetasPorSalaUseCase.SALA_NAO_DEFINIDA) "zzz$sala" else sala.lowercase()
            })
            
            // Chaves devem estar ordenadas
            chaves shouldBe chavesOrdenadas
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 16: Contagem de itens por grupo
     * Validates: Requirements 10.4
     * 
     * *For any* grupo de sala exibido, a contagem de itens deve corresponder 
     * exatamente ao número de coletas naquele grupo
     */
    "Property 16: Contagem de itens por grupo - contagem deve corresponder ao número de coletas" {
        checkAll(100, ColetaGenerators.listaColetasComSalas()) { coletas ->
            val resultado = useCase(coletas)
            val contagem = useCase.contarPorGrupo(resultado)
            
            // Contagem de cada grupo deve corresponder ao tamanho da lista
            resultado.forEach { (sala, coletasDoGrupo) ->
                contagem[sala] shouldBe coletasDoGrupo.size
            }
            
            // Soma das contagens deve ser igual ao total
            contagem.values.sum() shouldBe coletas.size
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 15 (continuação): Sala não definida
     * Validates: Requirements 10.2
     * 
     * *For any* patrimônios sem sala definida, devem ser agrupados em 
     * categoria "Sala não definida"
     */
    "Property 15: Agrupamento por sala - coletas sem sala devem ir para 'Sala não definida'" {
        checkAll(100, ColetaGenerators.listaColetasComSalas()) { coletas ->
            val coletasSemSala = coletas.filter { 
                it.nomeSala.isNullOrBlank() && it.localizacaoAtual.isNullOrBlank() 
            }
            
            if (coletasSemSala.isNotEmpty()) {
                val resultado = useCase(coletas)
                
                // Deve existir o grupo "Sala não definida"
                resultado shouldContainKey AgruparColetasPorSalaUseCase.SALA_NAO_DEFINIDA
                
                // Quantidade no grupo deve corresponder às coletas sem sala
                resultado[AgruparColetasPorSalaUseCase.SALA_NAO_DEFINIDA]?.size shouldBe coletasSemSala.size
            }
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 15 (continuação): Preservação de dados
     * Validates: Requirements 10.1
     * 
     * *For any* conjunto de coletas, o agrupamento não deve perder nenhuma coleta
     */
    "Property 15: Agrupamento por sala - nenhuma coleta deve ser perdida" {
        checkAll(100, ColetaGenerators.listaColetas()) { coletas ->
            val resultado = useCase(coletas)
            
            // Todas as coletas originais devem estar em algum grupo
            val todasColetasAgrupadas = resultado.values.flatten()
            
            todasColetasAgrupadas.size shouldBe coletas.size
            todasColetasAgrupadas shouldContainAll coletas
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 16 (continuação): Flat list com headers
     * Validates: Requirements 10.3, 10.4
     * 
     * *For any* conjunto de coletas agrupadas, a conversão para lista flat 
     * deve preservar a estrutura de grupos com headers
     */
    "Property 16: Flat list com headers - deve ter um header para cada grupo" {
        checkAll(100, ColetaGenerators.listaColetasComSalas()) { coletas ->
            val agrupadas = useCase(coletas)
            val flatList = useCase.toFlatListWithHeaders(agrupadas)
            
            // Número de headers deve ser igual ao número de grupos
            val headers = flatList.filterIsInstance<ColetaListItem.Header>()
            headers.size shouldBe agrupadas.size
            
            // Cada header deve ter a quantidade correta
            headers.forEach { header ->
                header.quantidade shouldBe agrupadas[header.nomeSala]?.size
            }
            
            // Número de itens deve ser igual ao total de coletas
            val items = flatList.filterIsInstance<ColetaListItem.Item>()
            items.size shouldBe coletas.size
        }
    }
})
