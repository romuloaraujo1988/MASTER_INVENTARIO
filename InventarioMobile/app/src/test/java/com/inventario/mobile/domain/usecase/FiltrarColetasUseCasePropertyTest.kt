package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.StatusFiltro
import com.inventario.mobile.generators.ColetaGenerators
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-Based Tests para FiltrarColetasUseCase
 * 
 * Testa propriedades de correção do sistema de filtros usando Kotest Property Testing.
 * Cada teste executa no mínimo 100 iterações com dados gerados aleatoriamente.
 */
class FiltrarColetasUseCasePropertyTest : StringSpec({
    
    val useCase = FiltrarColetasUseCase()
    
    /**
     * Feature: visualizacao-coletas, Property 2: Filtro de usuário exclusivo
     * Validates: Requirements 2.1, 2.2
     * 
     * *For any* usuário logado, quando o filtro por usuário está ativo, 
     * apenas coletas realizadas por esse usuário devem aparecer na lista
     */
    "Property 2: Filtro de usuário exclusivo - todas coletas filtradas devem ser do usuário atual" {
        checkAll(100, ColetaGenerators.listaColetas(), Arb.string(5..20)) { coletas, usuarioAtual ->
            val resultado = useCase(
                coletas = coletas,
                usuarioAtual = usuarioAtual,
                filtroUsuario = true,
                filtroSala = null,
                filtroStatus = StatusFiltro.TODOS
            )
            
            // Todas as coletas retornadas devem ser do usuário atual
            resultado.all { it.nomeColetor.equals(usuarioAtual, ignoreCase = true) } shouldBe true
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 3: Filtro de usuário desativado mostra todos
     * Validates: Requirements 2.3
     * 
     * *For any* conjunto de coletas, quando o filtro por usuário está desativado, 
     * coletas de todos os usuários devem aparecer na lista
     */
    "Property 3: Filtro de usuário desativado - deve retornar todas as coletas" {
        checkAll(100, ColetaGenerators.listaColetas(), Arb.string(5..20)) { coletas, usuarioAtual ->
            val resultado = useCase(
                coletas = coletas,
                usuarioAtual = usuarioAtual,
                filtroUsuario = false,
                filtroSala = null,
                filtroStatus = StatusFiltro.TODOS
            )
            
            // Deve retornar todas as coletas (sem filtro)
            resultado.size shouldBe coletas.size
            resultado shouldContainAll coletas
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 6: Filtro de sala específica
     * Validates: Requirements 3.1
     * 
     * *For any* sala selecionada, apenas coletas de patrimônios dessa sala 
     * devem aparecer na lista filtrada
     */
    "Property 6: Filtro de sala específica - todas coletas filtradas devem ser da sala selecionada" {
        checkAll(100, ColetaGenerators.listaColetasComSalas()) { coletas ->
            val salasDisponiveis = coletas.mapNotNull { it.nomeSala }.distinct()
            
            if (salasDisponiveis.isNotEmpty()) {
                val salaFiltro = salasDisponiveis.random()
                
                val resultado = useCase(
                    coletas = coletas,
                    usuarioAtual = null,
                    filtroUsuario = false,
                    filtroSala = salaFiltro,
                    filtroStatus = StatusFiltro.TODOS
                )
                
                // Todas as coletas retornadas devem ser da sala selecionada
                resultado.all { 
                    it.nomeSala.equals(salaFiltro, ignoreCase = true) ||
                    it.localizacaoAtual.equals(salaFiltro, ignoreCase = true)
                } shouldBe true
            }
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 8: Múltiplos filtros simultâneos (AND lógico)
     * Validates: Requirements 3.4, 4.5, 8.1, 8.2, 8.3, 8.4
     * 
     * *For any* combinação de filtros ativos (usuário, sala, status), 
     * apenas coletas que atendem TODOS os critérios devem aparecer
     */
    "Property 8: Múltiplos filtros simultâneos - deve aplicar AND lógico em todos os filtros" {
        checkAll(100, ColetaGenerators.listaColetasComSalas(), Arb.string(5..20)) { coletas, usuarioAtual ->
            val salasDisponiveis = coletas.mapNotNull { it.nomeSala }.distinct()
            
            if (salasDisponiveis.isNotEmpty()) {
                val salaFiltro = salasDisponiveis.random()
                
                val resultado = useCase(
                    coletas = coletas,
                    usuarioAtual = usuarioAtual,
                    filtroUsuario = true,
                    filtroSala = salaFiltro,
                    filtroStatus = StatusFiltro.COLETADOS
                )
                
                // Todas as coletas devem atender TODOS os critérios
                resultado.all { coleta ->
                    val matchUsuario = coleta.nomeColetor.equals(usuarioAtual, ignoreCase = true)
                    val matchSala = coleta.nomeSala.equals(salaFiltro, ignoreCase = true) ||
                                   coleta.localizacaoAtual.equals(salaFiltro, ignoreCase = true)
                    val matchStatus = coleta.sincronizado
                    
                    matchUsuario && matchSala && matchStatus
                } shouldBe true
            }
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 9: Filtro de status coletados
     * Validates: Requirements 4.1
     * 
     * *For any* conjunto de patrimônios, quando o filtro "coletados" está ativo, 
     * apenas patrimônios com sincronizado=true devem aparecer
     */
    "Property 9: Filtro de status coletados - deve retornar apenas coletas sincronizadas" {
        checkAll(100, ColetaGenerators.listaColetas()) { coletas ->
            val resultado = useCase(
                coletas = coletas,
                usuarioAtual = null,
                filtroUsuario = false,
                filtroSala = null,
                filtroStatus = StatusFiltro.COLETADOS
            )
            
            // Todas as coletas retornadas devem estar sincronizadas
            resultado.all { it.sincronizado } shouldBe true
            
            // Quantidade deve ser igual ao número de sincronizadas na lista original
            resultado.size shouldBe coletas.count { it.sincronizado }
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 10: Filtro de status pendentes
     * Validates: Requirements 4.2
     * 
     * *For any* conjunto de patrimônios, quando o filtro "pendentes" está ativo, 
     * apenas patrimônios com sincronizado=false devem aparecer
     */
    "Property 10: Filtro de status pendentes - deve retornar apenas coletas não sincronizadas" {
        checkAll(100, ColetaGenerators.listaColetas()) { coletas ->
            val resultado = useCase(
                coletas = coletas,
                usuarioAtual = null,
                filtroUsuario = false,
                filtroSala = null,
                filtroStatus = StatusFiltro.PENDENTES
            )
            
            // Todas as coletas retornadas devem estar pendentes (não sincronizadas)
            resultado.all { !it.sincronizado } shouldBe true
            
            // Quantidade deve ser igual ao número de pendentes na lista original
            resultado.size shouldBe coletas.count { !it.sincronizado }
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 5: Contadores refletem filtros
     * Validates: Requirements 2.5, 6.3
     * 
     * *For any* conjunto de filtros aplicados, os contadores de totais 
     * devem corresponder exatamente ao número de itens na lista filtrada
     */
    "Property 5: Contadores refletem filtros - contadores devem corresponder à lista filtrada" {
        checkAll(100, ColetaGenerators.listaColetas()) { coletas ->
            val resultado = useCase(
                coletas = coletas,
                usuarioAtual = null,
                filtroUsuario = false,
                filtroSala = null,
                filtroStatus = StatusFiltro.TODOS
            )
            
            val (coletados, pendentes) = useCase.calcularContadores(resultado)
            
            // Soma dos contadores deve ser igual ao total de coletas
            (coletados + pendentes) shouldBe resultado.size
            
            // Contadores devem corresponder aos valores reais
            coletados shouldBe resultado.count { it.sincronizado }
            pendentes shouldBe resultado.count { !it.sincronizado }
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 12: Contadores sempre corretos
     * Validates: Requirements 6.1, 6.2, 6.4
     * 
     * *For any* estado da lista, os contadores de coletados e pendentes 
     * devem corresponder exatamente ao número de itens em cada categoria
     */
    "Property 12: Contadores sempre corretos - soma deve ser igual ao total" {
        checkAll(100, ColetaGenerators.listaColetas()) { coletas ->
            val (coletados, pendentes) = useCase.calcularContadores(coletas)
            
            // Soma deve ser igual ao total
            (coletados + pendentes) shouldBe coletas.size
            
            // Valores não podem ser negativos
            coletados shouldBeGreaterThanOrEqual 0
            pendentes shouldBeGreaterThanOrEqual 0
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 17: Remoção de filtros expande resultados
     * Validates: Requirements 8.5
     * 
     * *For any* conjunto de filtros, remover um filtro deve resultar 
     * em lista igual ou maior que a anterior
     */
    "Property 17: Remoção de filtros expande resultados - menos filtros = mais resultados" {
        checkAll(100, ColetaGenerators.listaColetas(), Arb.string(5..20)) { coletas, usuarioAtual ->
            // Com filtro de usuário ativo
            val comFiltro = useCase(
                coletas = coletas,
                usuarioAtual = usuarioAtual,
                filtroUsuario = true,
                filtroSala = null,
                filtroStatus = StatusFiltro.TODOS
            )
            
            // Sem filtro de usuário
            val semFiltro = useCase(
                coletas = coletas,
                usuarioAtual = usuarioAtual,
                filtroUsuario = false,
                filtroSala = null,
                filtroStatus = StatusFiltro.TODOS
            )
            
            // Remover filtro deve expandir ou manter resultados
            comFiltro.size shouldBeLessThanOrEqual semFiltro.size
        }
    }
    
    /**
     * Feature: visualizacao-coletas, Property 7: Sem filtro de sala mostra todas
     * Validates: Requirements 3.2
     * 
     * *For any* conjunto de coletas, quando nenhuma sala está selecionada, 
     * coletas de todas as salas devem aparecer
     */
    "Property 7: Sem filtro de sala mostra todas - deve retornar coletas de todas as salas" {
        checkAll(100, ColetaGenerators.listaColetasComSalas()) { coletas ->
            val resultado = useCase(
                coletas = coletas,
                usuarioAtual = null,
                filtroUsuario = false,
                filtroSala = null,
                filtroStatus = StatusFiltro.TODOS
            )
            
            // Deve retornar todas as coletas
            resultado.size shouldBe coletas.size
            
            // Deve conter coletas de múltiplas salas (se existirem)
            val salasNoResultado = resultado.mapNotNull { it.nomeSala }.distinct()
            val salasOriginais = coletas.mapNotNull { it.nomeSala }.distinct()
            
            salasNoResultado shouldContainAll salasOriginais
        }
    }
})
