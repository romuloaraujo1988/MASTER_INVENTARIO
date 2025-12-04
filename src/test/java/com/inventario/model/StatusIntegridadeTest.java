package com.inventario.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para StatusIntegridade.
 * 
 * **Feature: relatorio-itens-compostos, Property 7: Cor da taxa de integridade segue regra de 80%**
 * **Validates: Requirements 3.2, 3.3**
 */
@DisplayName("StatusIntegridade - Testes de Enum e Métodos")
class StatusIntegridadeTest {
    
    // === Testes para fromContagem() ===
    
    @Test
    @DisplayName("fromContagem() deve retornar COMPLETO quando encontrados >= esperados")
    void fromContagem_TodosEncontrados_RetornaCompleto() {
        assertEquals(StatusIntegridade.COMPLETO, StatusIntegridade.fromContagem(5, 5));
        assertEquals(StatusIntegridade.COMPLETO, StatusIntegridade.fromContagem(6, 5));
    }
    
    @Test
    @DisplayName("fromContagem() deve retornar PARCIAL quando alguns encontrados")
    void fromContagem_AlgunsEncontrados_RetornaParcial() {
        assertEquals(StatusIntegridade.PARCIAL, StatusIntegridade.fromContagem(3, 5));
        assertEquals(StatusIntegridade.PARCIAL, StatusIntegridade.fromContagem(1, 5));
    }
    
    @Test
    @DisplayName("fromContagem() deve retornar INCOMPLETO quando nenhum encontrado")
    void fromContagem_NenhumEncontrado_RetornaIncompleto() {
        assertEquals(StatusIntegridade.INCOMPLETO, StatusIntegridade.fromContagem(0, 5));
    }
    
    @Test
    @DisplayName("fromContagem() deve retornar COMPLETO quando esperados é zero")
    void fromContagem_EsperadosZero_RetornaCompleto() {
        assertEquals(StatusIntegridade.COMPLETO, StatusIntegridade.fromContagem(0, 0));
    }
    
    // === Testes para fromTaxa() ===
    
    @ParameterizedTest
    @ValueSource(doubles = {100.0, 100.1, 150.0})
    @DisplayName("fromTaxa() deve retornar COMPLETO quando taxa >= 100%")
    void fromTaxa_TaxaCem_RetornaCompleto(double taxa) {
        assertEquals(StatusIntegridade.COMPLETO, StatusIntegridade.fromTaxa(taxa));
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {0.1, 1.0, 50.0, 99.0, 99.9})
    @DisplayName("fromTaxa() deve retornar PARCIAL quando 0 < taxa < 100")
    void fromTaxa_TaxaParcial_RetornaParcial(double taxa) {
        assertEquals(StatusIntegridade.PARCIAL, StatusIntegridade.fromTaxa(taxa));
    }
    
    @Test
    @DisplayName("fromTaxa() deve retornar INCOMPLETO quando taxa = 0")
    void fromTaxa_TaxaZero_RetornaIncompleto() {
        assertEquals(StatusIntegridade.INCOMPLETO, StatusIntegridade.fromTaxa(0.0));
    }
    
    // === Testes para fromDescricao() ===
    
    @ParameterizedTest
    @CsvSource({
        "Completo, COMPLETO",
        "COMPLETO, COMPLETO",
        "completo, COMPLETO",
        "Incompleto, INCOMPLETO",
        "INCOMPLETO, INCOMPLETO",
        "incompleto, INCOMPLETO",
        "Parcial, PARCIAL",
        "PARCIAL, PARCIAL",
        "parcial, PARCIAL",
        "Todos, TODOS",
        "TODOS, TODOS",
        "todos, TODOS"
    })
    @DisplayName("fromDescricao() deve converter descrição para enum corretamente")
    void fromDescricao_DescricaoValida_RetornaEnumCorreto(String descricao, StatusIntegridade esperado) {
        assertEquals(esperado, StatusIntegridade.fromDescricao(descricao));
    }
    
    @ParameterizedTest
    @NullSource
    @DisplayName("fromDescricao() deve retornar TODOS quando descrição é nula")
    void fromDescricao_Nulo_RetornaTodos(String descricao) {
        assertEquals(StatusIntegridade.TODOS, StatusIntegridade.fromDescricao(descricao));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", "invalido", "xyz", "complete", "incomplete"})
    @DisplayName("fromDescricao() deve retornar TODOS quando descrição é inválida")
    void fromDescricao_DescricaoInvalida_RetornaTodos(String descricao) {
        assertEquals(StatusIntegridade.TODOS, StatusIntegridade.fromDescricao(descricao));
    }
    
    // === Testes para getCor() ===
    
    @Test
    @DisplayName("COMPLETO deve ter cor verde")
    void getCor_Completo_Verde() {
        Color cor = StatusIntegridade.COMPLETO.getCor();
        
        // Verde: RGB(46, 204, 113)
        assertEquals(46, cor.getRed());
        assertEquals(204, cor.getGreen());
        assertEquals(113, cor.getBlue());
    }
    
    @Test
    @DisplayName("INCOMPLETO deve ter cor vermelha")
    void getCor_Incompleto_Vermelho() {
        Color cor = StatusIntegridade.INCOMPLETO.getCor();
        
        // Vermelho: RGB(231, 76, 60)
        assertEquals(231, cor.getRed());
        assertEquals(76, cor.getGreen());
        assertEquals(60, cor.getBlue());
    }
    
    @Test
    @DisplayName("PARCIAL deve ter cor amarela/laranja")
    void getCor_Parcial_Amarelo() {
        Color cor = StatusIntegridade.PARCIAL.getCor();
        
        // Amarelo: RGB(241, 196, 15)
        assertEquals(241, cor.getRed());
        assertEquals(196, cor.getGreen());
        assertEquals(15, cor.getBlue());
    }
    
    // === Testes para getCorFundo() ===
    
    @Test
    @DisplayName("COMPLETO deve ter cor de fundo verde claro")
    void getCorFundo_Completo_VerdeClaro() {
        Color cor = StatusIntegridade.COMPLETO.getCorFundo();
        
        // Verde claro: RGB(212, 239, 223)
        assertEquals(212, cor.getRed());
        assertEquals(239, cor.getGreen());
        assertEquals(223, cor.getBlue());
    }
    
    @Test
    @DisplayName("INCOMPLETO deve ter cor de fundo vermelho claro")
    void getCorFundo_Incompleto_VermelhoClaro() {
        Color cor = StatusIntegridade.INCOMPLETO.getCorFundo();
        
        // Vermelho claro: RGB(250, 219, 216)
        assertEquals(250, cor.getRed());
        assertEquals(219, cor.getGreen());
        assertEquals(216, cor.getBlue());
    }
    
    @Test
    @DisplayName("PARCIAL deve ter cor de fundo amarelo claro")
    void getCorFundo_Parcial_AmareloClaro() {
        Color cor = StatusIntegridade.PARCIAL.getCorFundo();
        
        // Amarelo claro: RGB(252, 243, 207)
        assertEquals(252, cor.getRed());
        assertEquals(243, cor.getGreen());
        assertEquals(207, cor.getBlue());
    }
    
    @Test
    @DisplayName("TODOS deve ter cor de fundo branco")
    void getCorFundo_Todos_Branco() {
        Color cor = StatusIntegridade.TODOS.getCorFundo();
        
        assertEquals(Color.WHITE, cor);
    }
    
    // === Testes para getDescricao() e toString() ===
    
    @Test
    @DisplayName("getDescricao() deve retornar descrição amigável")
    void getDescricao_RetornaDescricaoAmigavel() {
        assertEquals("Completo", StatusIntegridade.COMPLETO.getDescricao());
        assertEquals("Incompleto", StatusIntegridade.INCOMPLETO.getDescricao());
        assertEquals("Parcial", StatusIntegridade.PARCIAL.getDescricao());
        assertEquals("Todos", StatusIntegridade.TODOS.getDescricao());
    }
    
    @Test
    @DisplayName("toString() deve retornar descrição amigável")
    void toString_RetornaDescricao() {
        assertEquals("Completo", StatusIntegridade.COMPLETO.toString());
        assertEquals("Incompleto", StatusIntegridade.INCOMPLETO.toString());
        assertEquals("Parcial", StatusIntegridade.PARCIAL.toString());
        assertEquals("Todos", StatusIntegridade.TODOS.toString());
    }
}
