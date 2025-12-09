package com.inventario.mobile.presentation.export

import com.inventario.mobile.domain.model.Sala
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-Based Tests para filtro de busca de salas
 * 
 * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
 * **Validates: Requirements 2.3**
 */
class SalaSearchFilterPropertyTest : FunSpec({
    
    /**
     * Generator para Sala
     */
    val salaArb: Arb<Sala> = arbitrary {
        Sala(
            id = Arb.int(1..1000).bind().toLong(),
            nome = "Sala ${Arb.string(3..20).bind()}",
            codigo = "S${Arb.int(100..999).bind()}",
            setorId = Arb.int(1..10).bind().toLong()
        )
    }
    
    /**
     * Função de filtro de salas (simula o comportamento do adapter)
     */
    fun filterSalas(salas: List<Sala>, query: String): List<Sala> {
        if (query.isBlank()) return salas
        return salas.filter { sala ->
            sala.nome.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Filtered results should only contain salas whose names contain the query
     */
    test("filtered results should only contain salas whose names contain the query") {
        checkAll(100, Arb.list(salaArb, 1..50), Arb.string(1..5)) { salas, query ->
            val filtered = filterSalas(salas, query)
            
            // Todos os resultados devem conter a query no nome
            filtered.all { sala ->
                sala.nome.contains(query, ignoreCase = true)
            } shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Empty query should return all salas
     */
    test("empty query should return all salas") {
        checkAll(100, Arb.list(salaArb, 1..50)) { salas ->
            val filtered = filterSalas(salas, "")
            
            filtered.size shouldBe salas.size
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Blank query should return all salas
     */
    test("blank query should return all salas") {
        checkAll(100, Arb.list(salaArb, 1..50)) { salas ->
            val filtered = filterSalas(salas, "   ")
            
            filtered.size shouldBe salas.size
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Filter should be case-insensitive
     */
    test("filter should be case-insensitive") {
        val salas = listOf(
            Sala(id = 1, nome = "Sala 101", codigo = "S101", setorId = 1),
            Sala(id = 2, nome = "SALA 102", codigo = "S102", setorId = 1),
            Sala(id = 3, nome = "sala 103", codigo = "S103", setorId = 1),
            Sala(id = 4, nome = "Laboratório", codigo = "L001", setorId = 1)
        )
        
        val filteredLower = filterSalas(salas, "sala")
        val filteredUpper = filterSalas(salas, "SALA")
        val filteredMixed = filterSalas(salas, "SaLa")
        
        filteredLower.size shouldBe 3
        filteredUpper.size shouldBe 3
        filteredMixed.size shouldBe 3
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Filtered results should be subset of original list
     */
    test("filtered results should be subset of original list") {
        checkAll(100, Arb.list(salaArb, 1..50), Arb.string(1..5)) { salas, query ->
            val filtered = filterSalas(salas, query)
            
            // Todos os itens filtrados devem estar na lista original
            filtered.all { filteredSala ->
                salas.any { it.id == filteredSala.id }
            } shouldBe true
            
            // Tamanho do filtrado deve ser <= original
            (filtered.size <= salas.size) shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Non-matching query should return empty list
     */
    test("non-matching query should return empty list") {
        val salas = listOf(
            Sala(id = 1, nome = "Sala 101", codigo = "S101", setorId = 1),
            Sala(id = 2, nome = "Sala 102", codigo = "S102", setorId = 1),
            Sala(id = 3, nome = "Laboratório", codigo = "L001", setorId = 1)
        )
        
        val filtered = filterSalas(salas, "xyz123")
        
        filtered.isEmpty() shouldBe true
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Partial match should work
     */
    test("partial match should work") {
        val salas = listOf(
            Sala(id = 1, nome = "Sala 101", codigo = "S101", setorId = 1),
            Sala(id = 2, nome = "Sala 102", codigo = "S102", setorId = 1),
            Sala(id = 3, nome = "Laboratório de Informática", codigo = "L001", setorId = 1)
        )
        
        val filtered = filterSalas(salas, "10")
        
        filtered.size shouldBe 2
        filtered.all { it.nome.contains("10") } shouldBe true
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Empty list should return empty for any query
     */
    test("empty list should return empty for any query") {
        checkAll(100, Arb.string(0..10)) { query ->
            val filtered = filterSalas(emptyList(), query)
            
            filtered.isEmpty() shouldBe true
        }
    }
    
    /**
     * **Feature: exportacao-pdf-android, Property 1: Sala search filtering**
     * 
     * Filter should preserve order
     */
    test("filter should preserve order") {
        val salas = listOf(
            Sala(id = 1, nome = "Sala A", codigo = "SA", setorId = 1),
            Sala(id = 2, nome = "Sala B", codigo = "SB", setorId = 1),
            Sala(id = 3, nome = "Sala C", codigo = "SC", setorId = 1),
            Sala(id = 4, nome = "Laboratório", codigo = "L001", setorId = 1)
        )
        
        val filtered = filterSalas(salas, "Sala")
        
        // Verificar que a ordem é preservada
        filtered.size shouldBe 3
        filtered[0].nome shouldBe "Sala A"
        filtered[1].nome shouldBe "Sala B"
        filtered[2].nome shouldBe "Sala C"
    }
})
