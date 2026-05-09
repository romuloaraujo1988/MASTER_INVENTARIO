package com.inventario.sihcp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para EstatisticasIntegridade.
 * 
 * **Feature: relatorio-itens-compostos, Property 7: Cor da taxa de integridade segue regra de 80%**
 * **Validates: Requirements 4.3, 4.4**
 */
@DisplayName("EstatisticasIntegridade - Testes de Métodos de Negócio")
class EstatisticasIntegridadeTest {
    
    private EstatisticasIntegridade stats;
    
    @BeforeEach
    void setUp() {
        stats = new EstatisticasIntegridade();
    }
    
    // === Testes para isAlerta() - Regra dos 80% ===
    
    @ParameterizedTest
    @ValueSource(doubles = {0.0, 10.0, 50.0, 79.0, 79.9, 79.99})
    @DisplayName("isAlerta() deve retornar true quando taxa < 80%")
    void isAlerta_TaxaAbaixoDe80_RetornaTrue(double taxa) {
        stats.setTaxaIntegridadeGeral(taxa);
        
        assertTrue(stats.isAlerta(), 
            "Taxa " + taxa + "% deveria gerar alerta (< 80%)");
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {80.0, 80.1, 85.0, 90.0, 95.0, 100.0})
    @DisplayName("isAlerta() deve retornar false quando taxa >= 80%")
    void isAlerta_TaxaIgualOuAcimaDe80_RetornaFalse(double taxa) {
        stats.setTaxaIntegridadeGeral(taxa);
        
        assertFalse(stats.isAlerta(), 
            "Taxa " + taxa + "% não deveria gerar alerta (>= 80%)");
    }
    
    @Test
    @DisplayName("isAlerta() deve retornar false para exatamente 80%")
    void isAlerta_Exatamente80_RetornaFalse() {
        stats.setTaxaIntegridadeGeral(80.0);
        
        assertFalse(stats.isAlerta());
    }
    
    @Test
    @DisplayName("isAlerta() deve retornar true para 79.99%")
    void isAlerta_79Ponto99_RetornaTrue() {
        stats.setTaxaIntegridadeGeral(79.99);
        
        assertTrue(stats.isAlerta());
    }
    
    // === Testes para getCorTaxa() ===
    
    @Test
    @DisplayName("getCorTaxa() deve retornar vermelho quando em alerta")
    void getCorTaxa_EmAlerta_RetornaVermelho() {
        stats.setTaxaIntegridadeGeral(79.0);
        
        Color cor = stats.getCorTaxa();
        
        // Vermelho: RGB(231, 76, 60)
        assertEquals(231, cor.getRed());
        assertEquals(76, cor.getGreen());
        assertEquals(60, cor.getBlue());
    }
    
    @Test
    @DisplayName("getCorTaxa() deve retornar verde quando não em alerta")
    void getCorTaxa_SemAlerta_RetornaVerde() {
        stats.setTaxaIntegridadeGeral(80.0);
        
        Color cor = stats.getCorTaxa();
        
        // Verde: RGB(46, 204, 113)
        assertEquals(46, cor.getRed());
        assertEquals(204, cor.getGreen());
        assertEquals(113, cor.getBlue());
    }
    
    // === Testes para getCorFundoTaxa() ===
    
    @Test
    @DisplayName("getCorFundoTaxa() deve retornar vermelho claro quando em alerta")
    void getCorFundoTaxa_EmAlerta_RetornaVermelhoClaro() {
        stats.setTaxaIntegridadeGeral(50.0);
        
        Color cor = stats.getCorFundoTaxa();
        
        // Vermelho claro: RGB(250, 219, 216)
        assertEquals(250, cor.getRed());
        assertEquals(219, cor.getGreen());
        assertEquals(216, cor.getBlue());
    }
    
    @Test
    @DisplayName("getCorFundoTaxa() deve retornar verde claro quando não em alerta")
    void getCorFundoTaxa_SemAlerta_RetornaVerdeClaro() {
        stats.setTaxaIntegridadeGeral(85.0);
        
        Color cor = stats.getCorFundoTaxa();
        
        // Verde claro: RGB(212, 239, 223)
        assertEquals(212, cor.getRed());
        assertEquals(239, cor.getGreen());
        assertEquals(223, cor.getBlue());
    }
    
    // === Testes para calcularTaxa() ===
    
    @ParameterizedTest
    @CsvSource({
        "100, 100, 100.0",
        "100, 80, 80.0",
        "100, 50, 50.0",
        "100, 0, 0.0",
        "10, 8, 80.0",
        "10, 5, 50.0",
        "0, 0, 100.0"
    })
    @DisplayName("calcularTaxa() deve calcular corretamente baseado em completos/total")
    void calcularTaxa_DiversosCenarios(int total, int completos, double taxaEsperada) {
        stats.setTotalConjuntos(total);
        stats.setConjuntosCompletos(completos);
        
        stats.calcularTaxa();
        
        assertEquals(taxaEsperada, stats.getTaxaIntegridadeGeral(), 0.01);
    }
    
    @Test
    @DisplayName("calcularTaxa() deve retornar 100% quando total é zero")
    void calcularTaxa_TotalZero_Retorna100() {
        stats.setTotalConjuntos(0);
        stats.setConjuntosCompletos(0);
        
        stats.calcularTaxa();
        
        assertEquals(100.0, stats.getTaxaIntegridadeGeral(), 0.01);
    }
    
    // === Testes para getTaxaFormatada() ===
    
    @Test
    @DisplayName("getTaxaFormatada() deve formatar com uma casa decimal e símbolo %")
    void getTaxaFormatada_FormataCorretamente() {
        stats.setTaxaIntegridadeGeral(85.5);
        
        String formatada = stats.getTaxaFormatada();
        
        assertTrue(formatada.contains("85"));
        assertTrue(formatada.contains("%"));
    }
    
    @Test
    @DisplayName("getTaxaFormatada() deve formatar 100% corretamente")
    void getTaxaFormatada_Cem_FormataCorretamente() {
        stats.setTaxaIntegridadeGeral(100.0);
        
        String formatada = stats.getTaxaFormatada();
        
        assertTrue(formatada.contains("100"));
        assertTrue(formatada.contains("%"));
    }
    
    // === Testes para getPercentualCompletos() e getPercentualIncompletos() ===
    
    @Test
    @DisplayName("getPercentualCompletos() deve calcular corretamente")
    void getPercentualCompletos_CalculaCorretamente() {
        stats.setTotalConjuntos(100);
        stats.setConjuntosCompletos(75);
        
        assertEquals(75.0, stats.getPercentualCompletos(), 0.01);
    }
    
    @Test
    @DisplayName("getPercentualIncompletos() deve calcular corretamente")
    void getPercentualIncompletos_CalculaCorretamente() {
        stats.setTotalConjuntos(100);
        stats.setConjuntosIncompletos(25);
        
        assertEquals(25.0, stats.getPercentualIncompletos(), 0.01);
    }
    
    @Test
    @DisplayName("getPercentualCompletos() deve retornar 100% quando total é zero")
    void getPercentualCompletos_TotalZero_Retorna100() {
        stats.setTotalConjuntos(0);
        stats.setConjuntosCompletos(0);
        
        assertEquals(100.0, stats.getPercentualCompletos(), 0.01);
    }
    
    @Test
    @DisplayName("getPercentualIncompletos() deve retornar 0% quando total é zero")
    void getPercentualIncompletos_TotalZero_RetornaZero() {
        stats.setTotalConjuntos(0);
        stats.setConjuntosIncompletos(0);
        
        assertEquals(0.0, stats.getPercentualIncompletos(), 0.01);
    }
    
    // === Testes para construtor com parâmetros ===
    
    @Test
    @DisplayName("Construtor deve calcular conjuntosParciais automaticamente")
    void construtor_CalculaConjuntosParciais() {
        EstatisticasIntegridade estatisticas = new EstatisticasIntegridade(100, 60, 20, 60.0);
        
        assertEquals(100, estatisticas.getTotalConjuntos());
        assertEquals(60, estatisticas.getConjuntosCompletos());
        assertEquals(20, estatisticas.getConjuntosIncompletos());
        assertEquals(20, estatisticas.getConjuntosParciais()); // 100 - 60 - 20 = 20
        assertEquals(60.0, estatisticas.getTaxaIntegridadeGeral(), 0.01);
    }
    
    // === Testes para getLimiarAlerta() ===
    
    @Test
    @DisplayName("getLimiarAlerta() deve retornar 80.0")
    void getLimiarAlerta_Retorna80() {
        assertEquals(80.0, EstatisticasIntegridade.getLimiarAlerta(), 0.01);
    }
}
