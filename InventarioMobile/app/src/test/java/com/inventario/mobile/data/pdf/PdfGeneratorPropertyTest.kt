package com.inventario.mobile.data.pdf

import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.PdfSummary
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.generators.PatrimonioGenerators
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

/**
 * Property-Based Tests para PdfGenerator e cálculos relacionados
 */
class PdfGeneratorPropertyTest : FunSpec({
    
    /**
     * **Feature: exportacao-pdf-android, Property 3: Summary calculation consistency**
     * 
     * Verifica que totalItems = coletados + naoColetados
     * **Validates: Requirements 6.2, 6.3, 6.4, 6.5**
     */
    test("summary totalItems should equal coletados plus naoColetados") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val summary = PdfSummary.fromPatrimonios(patrimonios)
            
            summary.totalItems shouldBe (summary.coletados + summary.naoColetados)
            summary.isConsistent() shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 3: Summary calculation consistency**
     * 
     * Verifica que percentual = (coletados / totalItems) * 100
     * **Validates: Requirements 6.2, 6.3, 6.4, 6.5**
     */
    test("summary percentual should be correctly calculated") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val summary = PdfSummary.fromPatrimonios(patrimonios)
            
            val expectedPercentual = if (summary.totalItems > 0) {
                (summary.coletados.toDouble() / summary.totalItems) * 100
            } else {
                0.0
            }
            
            // Comparar com tolerância para ponto flutuante
            val diff = kotlin.math.abs(summary.percentualColeta - expectedPercentual)
            (diff < 0.001) shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 3: Summary calculation consistency**
     * 
     * Verifica que percentual está entre 0 e 100
     * **Validates: Requirements 6.5**
     */
    test("summary percentual should be between 0 and 100") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val summary = PdfSummary.fromPatrimonios(patrimonios)
            
            (summary.percentualColeta >= 0.0) shouldBe true
            (summary.percentualColeta <= 100.0) shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 4: PDF item count consistency**
     * 
     * Verifica que contagem no resumo = número de itens na lista
     * **Validates: Requirements 10.3**
     */
    test("summary item count should match list size") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val summary = PdfSummary.fromPatrimonios(patrimonios)
            
            summary.totalItems shouldBe patrimonios.size
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 4: PDF item count consistency**
     * 
     * Verifica contagem de coletados
     * **Validates: Requirements 10.3**
     */
    test("summary coletados count should match filtered list size") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val summary = PdfSummary.fromPatrimonios(patrimonios)
            val expectedColetados = patrimonios.count { it.coletado }
            
            summary.coletados shouldBe expectedColetados
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 4: PDF item count consistency**
     * 
     * Verifica contagem de não coletados
     * **Validates: Requirements 10.3**
     */
    test("summary naoColetados count should match filtered list size") {
        checkAll(100, PatrimonioGenerators.listaPatrimoniosMista()) { patrimonios ->
            val summary = PdfSummary.fromPatrimonios(patrimonios)
            val expectedNaoColetados = patrimonios.count { !it.coletado }
            
            summary.naoColetados shouldBe expectedNaoColetados
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 3: Summary calculation consistency**
     * 
     * Verifica que lista vazia gera summary zerado
     * **Validates: Requirements 6.2, 6.3, 6.4, 6.5**
     */
    test("empty list should generate zero summary") {
        val emptyList = emptyList<Patrimonio>()
        val summary = PdfSummary.fromPatrimonios(emptyList)
        
        summary.totalItems shouldBe 0
        summary.coletados shouldBe 0
        summary.naoColetados shouldBe 0
        summary.percentualColeta shouldBe 0.0
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 3: Summary calculation consistency**
     * 
     * Verifica que lista 100% coletada tem percentual 100
     * **Validates: Requirements 6.5**
     */
    test("all collected list should have 100 percent") {
        checkAll(100, Arb.list(PatrimonioGenerators.patrimonioColetado(), 1..50)) { patrimonios ->
            val summary = PdfSummary.fromPatrimonios(patrimonios)
            
            summary.percentualColeta shouldBe 100.0
            summary.naoColetados shouldBe 0
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 3: Summary calculation consistency**
     * 
     * Verifica que lista 0% coletada tem percentual 0
     * **Validates: Requirements 6.5**
     */
    test("all non-collected list should have 0 percent") {
        checkAll(100, Arb.list(PatrimonioGenerators.patrimonioNaoColetado(), 1..50)) { patrimonios ->
            val summary = PdfSummary.fromPatrimonios(patrimonios)
            
            summary.percentualColeta shouldBe 0.0
            summary.coletados shouldBe 0
        }
    }
})
