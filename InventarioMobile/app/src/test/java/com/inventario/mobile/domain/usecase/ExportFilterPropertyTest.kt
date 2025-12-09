package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.generators.PatrimonioGenerators
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

/**
 * Property-Based Tests para filtros de exportação
 * 
 * **Feature: exportacao-pdf-android, Property 2: Export filter correctness**
 * **Validates: Requirements 3.2, 3.3, 3.4**
 */
class ExportFilterPropertyTest : FunSpec({
    
    /**
     * **Feature: exportacao-pdf-android, Property 2: Export filter correctness**
     * 
     * TODOS filter returns all patrimonios
     */
    test("TODOS filter should return all patrimonios") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val result = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(
                patrimonios, 
                ExportFilter.TODOS
            )
            
            result.size shouldBe patrimonios.size
            result shouldContainAll patrimonios
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 2: Export filter correctness**
     * 
     * COLETADOS filter returns only collected items
     */
    test("COLETADOS filter should return only collected items") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val result = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(
                patrimonios, 
                ExportFilter.COLETADOS
            )
            
            // Todos os itens retornados devem estar coletados
            result.all { it.coletado } shouldBe true
            
            // Deve conter todos os itens coletados da lista original
            val expectedColetados = patrimonios.filter { it.coletado }
            result.size shouldBe expectedColetados.size
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 2: Export filter correctness**
     * 
     * NAO_COLETADOS filter returns only non-collected items
     */
    test("NAO_COLETADOS filter should return only non-collected items") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val result = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(
                patrimonios, 
                ExportFilter.NAO_COLETADOS
            )
            
            // Nenhum item retornado deve estar coletado
            result.none { it.coletado } shouldBe true
            
            // Deve conter todos os itens não coletados da lista original
            val expectedNaoColetados = patrimonios.filter { !it.coletado }
            result.size shouldBe expectedNaoColetados.size
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 2: Export filter correctness**
     * 
     * Filter results are mutually exclusive and exhaustive
     */
    test("filter results should be mutually exclusive and exhaustive") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val todos = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(
                patrimonios, 
                ExportFilter.TODOS
            )
            val coletados = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(
                patrimonios, 
                ExportFilter.COLETADOS
            )
            val naoColetados = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(
                patrimonios, 
                ExportFilter.NAO_COLETADOS
            )
            
            // TODOS = COLETADOS + NAO_COLETADOS
            (coletados.size + naoColetados.size) shouldBe todos.size
            
            // Não deve haver interseção entre COLETADOS e NAO_COLETADOS
            val intersection = coletados.map { it.id }.intersect(naoColetados.map { it.id }.toSet())
            intersection.isEmpty() shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 2: Export filter correctness**
     * 
     * Empty list should return empty for all filters
     */
    test("empty list should return empty for all filters") {
        val emptyList = emptyList<Patrimonio>()
        
        ExportFilter.values().forEach { filter ->
            val result = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(emptyList, filter)
            result.isEmpty() shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 2: Export filter correctness**
     * 
     * All collected list should return empty for NAO_COLETADOS
     */
    test("all collected list should return empty for NAO_COLETADOS filter") {
        checkAll(100, Arb.list(PatrimonioGenerators.patrimonioColetado(), 1..50)) { patrimonios ->
            val result = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(
                patrimonios, 
                ExportFilter.NAO_COLETADOS
            )
            
            result.isEmpty() shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 2: Export filter correctness**
     * 
     * All non-collected list should return empty for COLETADOS
     */
    test("all non-collected list should return empty for COLETADOS filter") {
        checkAll(100, Arb.list(PatrimonioGenerators.patrimonioNaoColetado(), 1..50)) { patrimonios ->
            val result = BuscarPatrimoniosPorSalaParaExportacaoUseCase.applyFilter(
                patrimonios, 
                ExportFilter.COLETADOS
            )
            
            result.isEmpty() shouldBe true
        }
    }
})
