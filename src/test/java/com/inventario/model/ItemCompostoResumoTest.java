package com.inventario.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ItemCompostoResumo.
 * 
 * **Feature: relatorio-itens-compostos, Property 7: Cor da taxa de integridade segue regra de 80%**
 * **Validates: Requirements 4.3, 4.4**
 */
@DisplayName("ItemCompostoResumo - Testes de Métodos de Negócio")
class ItemCompostoResumoTest {
    
    private ItemCompostoResumo item;
    
    @BeforeEach
    void setUp() {
        item = new ItemCompostoResumo();
        item.setIdPatrimonio(1);
        item.setNumeroPatrimonio("12345");
        item.setDescricaoPatrimonio("Conjunto de Escritório");
    }
    
    // === Testes para isCompleto() ===
    
    @Test
    @DisplayName("isCompleto() deve retornar true quando todos componentes encontrados")
    void isCompleto_TodosComponentesEncontrados_RetornaTrue() {
        item.setComponentesEsperados(5);
        item.setComponentesEncontrados(5);
        item.setComponentesFaltantes(0);
        
        assertTrue(item.isCompleto());
    }
    
    @Test
    @DisplayName("isCompleto() deve retornar true quando encontrados > esperados")
    void isCompleto_MaisEncontradosQueEsperados_RetornaTrue() {
        item.setComponentesEsperados(5);
        item.setComponentesEncontrados(6);
        item.setComponentesFaltantes(0);
        
        assertTrue(item.isCompleto());
    }
    
    @Test
    @DisplayName("isCompleto() deve retornar false quando há componentes faltantes")
    void isCompleto_ComComponentesFaltantes_RetornaFalse() {
        item.setComponentesEsperados(5);
        item.setComponentesEncontrados(3);
        item.setComponentesFaltantes(2);
        
        assertFalse(item.isCompleto());
    }
    
    @Test
    @DisplayName("isCompleto() deve retornar false quando faltantes > 0 mesmo com encontrados >= esperados")
    void isCompleto_FaltantesMaiorQueZero_RetornaFalse() {
        item.setComponentesEsperados(5);
        item.setComponentesEncontrados(5);
        item.setComponentesFaltantes(1); // Inconsistência nos dados
        
        assertFalse(item.isCompleto());
    }
    
    @Test
    @DisplayName("isCompleto() deve retornar true quando não há componentes esperados")
    void isCompleto_SemComponentesEsperados_RetornaTrue() {
        item.setComponentesEsperados(0);
        item.setComponentesEncontrados(0);
        item.setComponentesFaltantes(0);
        
        assertTrue(item.isCompleto());
    }
    
    // === Testes para calcularTaxaIntegridade() ===
    
    @ParameterizedTest
    @CsvSource({
        "10, 10, 100.0, COMPLETO",
        "10, 8, 80.0, PARCIAL",
        "10, 5, 50.0, PARCIAL",
        "10, 1, 10.0, PARCIAL",
        "10, 0, 0.0, INCOMPLETO",
        "0, 0, 100.0, COMPLETO"
    })
    @DisplayName("calcularTaxaIntegridade() deve calcular taxa e status corretamente")
    void calcularTaxaIntegridade_DiversosCenarios(int esperados, int encontrados, 
                                                   double taxaEsperada, StatusIntegridade statusEsperado) {
        item.setComponentesEsperados(esperados);
        item.setComponentesEncontrados(encontrados);
        
        item.calcularTaxaIntegridade();
        
        assertEquals(taxaEsperada, item.getTaxaIntegridade(), 0.01);
        assertEquals(statusEsperado, item.getStatus());
    }
    
    @Test
    @DisplayName("calcularTaxaIntegridade() deve retornar 100% quando esperados é zero")
    void calcularTaxaIntegridade_EsperadosZero_Retorna100() {
        item.setComponentesEsperados(0);
        item.setComponentesEncontrados(0);
        
        item.calcularTaxaIntegridade();
        
        assertEquals(100.0, item.getTaxaIntegridade(), 0.01);
        assertEquals(StatusIntegridade.COMPLETO, item.getStatus());
    }
    
    // === Testes para getTaxaIntegridadeFormatada() ===
    
    @Test
    @DisplayName("getTaxaIntegridadeFormatada() deve formatar com uma casa decimal")
    void getTaxaIntegridadeFormatada_FormataCorretamente() {
        item.setTaxaIntegridade(85.5);
        
        assertEquals("85,5%", item.getTaxaIntegridadeFormatada().replace(".", ","));
    }
    
    @Test
    @DisplayName("getTaxaIntegridadeFormatada() deve formatar 100% corretamente")
    void getTaxaIntegridadeFormatada_Cem_FormataCorretamente() {
        item.setTaxaIntegridade(100.0);
        
        String formatada = item.getTaxaIntegridadeFormatada();
        assertTrue(formatada.contains("100"));
    }
    
    // === Testes para getComponentesFaltantesFormatado() ===
    
    @Test
    @DisplayName("getComponentesFaltantesFormatado() deve retornar '-' quando lista vazia")
    void getComponentesFaltantesFormatado_ListaVazia_RetornaTraco() {
        item.setTiposFaltantes(Collections.emptyList());
        
        assertEquals("-", item.getComponentesFaltantesFormatado());
    }
    
    @Test
    @DisplayName("getComponentesFaltantesFormatado() deve retornar '-' quando lista nula")
    void getComponentesFaltantesFormatado_ListaNula_RetornaTraco() {
        item.setTiposFaltantes(null);
        
        assertEquals("-", item.getComponentesFaltantesFormatado());
    }
    
    @Test
    @DisplayName("getComponentesFaltantesFormatado() deve concatenar tipos com vírgula")
    void getComponentesFaltantesFormatado_ComTipos_ConcatenaComVirgula() {
        item.setTiposFaltantes(Arrays.asList("CADEIRA", "MESA", "MONITOR"));
        
        assertEquals("CADEIRA, MESA, MONITOR", item.getComponentesFaltantesFormatado());
    }
    
    @Test
    @DisplayName("getComponentesFaltantesFormatado() deve retornar tipo único sem vírgula")
    void getComponentesFaltantesFormatado_TipoUnico_SemVirgula() {
        item.setTiposFaltantes(Arrays.asList("CADEIRA"));
        
        assertEquals("CADEIRA", item.getComponentesFaltantesFormatado());
    }
    
    // === Testes para addTipoFaltante() ===
    
    @Test
    @DisplayName("addTipoFaltante() deve adicionar tipo à lista")
    void addTipoFaltante_AdicionaTipo() {
        item.addTipoFaltante("CADEIRA");
        item.addTipoFaltante("MESA");
        
        assertEquals(2, item.getTiposFaltantes().size());
        assertTrue(item.getTiposFaltantes().contains("CADEIRA"));
        assertTrue(item.getTiposFaltantes().contains("MESA"));
    }
    
    @Test
    @DisplayName("addTipoFaltante() deve criar lista se nula")
    void addTipoFaltante_ListaNula_CriaLista() {
        item.setTiposFaltantes(null);
        
        item.addTipoFaltante("CADEIRA");
        
        assertNotNull(item.getTiposFaltantes());
        assertEquals(1, item.getTiposFaltantes().size());
    }
}
