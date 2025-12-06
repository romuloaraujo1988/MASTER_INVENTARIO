package com.inventario.analytics.service;

import com.inventario.analytics.model.GravidadeDivergencia;
import com.inventario.analytics.model.TipoDivergencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para DivergenciaClassificadorService.
 */
class DivergenciaClassificadorServiceTest {
    
    private DivergenciaClassificadorService service;
    
    @BeforeEach
    void setUp() {
        service = new DivergenciaClassificadorService();
    }
    
    @Nested
    @DisplayName("Classificação de Divergência de Localização")
    class ClassificacaoLocalizacao {
        
        // **Feature: analytics-divergencias-metricas, Property 2: Classificação de gravidade de localização é determinística**
        // **Validates: Requirements 1.2**
        
        @Test
        @DisplayName("Deve retornar ALTA quando patrimônio mudou de setor")
        void deveRetornarAltaQuandoMudouDeSetor() {
            GravidadeDivergencia resultado = service.classificarDivergenciaLocalizacao(
                    "Sala 101", "Sala 201",
                    1, 2  // Setores diferentes
            );
            
            assertEquals(GravidadeDivergencia.ALTA, resultado);
        }
        
        @Test
        @DisplayName("Deve retornar MÉDIA quando patrimônio mudou de sala no mesmo setor")
        void deveRetornarMediaQuandoMudouDeSalaMesmoSetor() {
            GravidadeDivergencia resultado = service.classificarDivergenciaLocalizacao(
                    "Sala 101", "Sala 102",
                    1, 1  // Mesmo setor
            );
            
            assertEquals(GravidadeDivergencia.MEDIA, resultado);
        }
        
        @Test
        @DisplayName("Deve retornar BAIXA quando apenas descrição difere")
        void deveRetornarBaixaQuandoApenasDescricaoDifere() {
            GravidadeDivergencia resultado = service.classificarDivergenciaLocalizacao(
                    "Sala 101", "Sala 101 - Bloco A",
                    1, 1  // Mesmo setor
            );
            
            assertEquals(GravidadeDivergencia.BAIXA, resultado);
        }
        
        @Test
        @DisplayName("Deve retornar BAIXA quando não há diferença")
        void deveRetornarBaixaQuandoNaoHaDiferenca() {
            GravidadeDivergencia resultado = service.classificarDivergenciaLocalizacao(
                    "Sala 101", "Sala 101",
                    1, 1
            );
            
            assertEquals(GravidadeDivergencia.BAIXA, resultado);
        }
        
        @Test
        @DisplayName("Deve tratar valores nulos corretamente")
        void deveTratarValoresNulos() {
            GravidadeDivergencia resultado = service.classificarDivergenciaLocalizacao(
                    null, "Sala 101",
                    null, 1
            );
            
            assertNotNull(resultado);
        }
    }
    
    @Nested
    @DisplayName("Classificação de Divergência de Estado")
    class ClassificacaoEstado {
        
        // **Feature: analytics-divergencias-metricas, Property 3: Classificação de gravidade de estado é determinística**
        // **Validates: Requirements 1.3**
        
        @Test
        @DisplayName("Deve retornar CRÍTICA quando estado piorou 2 ou mais níveis")
        void deveRetornarCriticaQuandoPiorou2OuMaisNiveis() {
            // BOM (4) -> RUIM (2) = diferença de 2
            GravidadeDivergencia resultado = service.classificarDivergenciaEstado("BOM", "RUIM");
            assertEquals(GravidadeDivergencia.CRITICA, resultado);
            
            // EXCELENTE (5) -> RUIM (2) = diferença de 3
            resultado = service.classificarDivergenciaEstado("EXCELENTE", "RUIM");
            assertEquals(GravidadeDivergencia.CRITICA, resultado);
            
            // BOM (4) -> INSERVIVEL (0) = diferença de 4
            resultado = service.classificarDivergenciaEstado("BOM", "INSERVIVEL");
            assertEquals(GravidadeDivergencia.CRITICA, resultado);
        }
        
        @Test
        @DisplayName("Deve retornar ALTA quando estado piorou 1 nível")
        void deveRetornarAltaQuandoPiorou1Nivel() {
            // BOM (4) -> REGULAR (3) = diferença de 1
            GravidadeDivergencia resultado = service.classificarDivergenciaEstado("BOM", "REGULAR");
            assertEquals(GravidadeDivergencia.ALTA, resultado);
            
            // EXCELENTE (5) -> BOM (4) = diferença de 1
            resultado = service.classificarDivergenciaEstado("EXCELENTE", "BOM");
            assertEquals(GravidadeDivergencia.ALTA, resultado);
        }
        
        @Test
        @DisplayName("Deve retornar BAIXA quando estado melhorou")
        void deveRetornarBaixaQuandoMelhorou() {
            // REGULAR (3) -> BOM (4) = melhorou
            GravidadeDivergencia resultado = service.classificarDivergenciaEstado("REGULAR", "BOM");
            assertEquals(GravidadeDivergencia.BAIXA, resultado);
            
            // RUIM (2) -> EXCELENTE (5) = melhorou muito
            resultado = service.classificarDivergenciaEstado("RUIM", "EXCELENTE");
            assertEquals(GravidadeDivergencia.BAIXA, resultado);
        }
        
        @Test
        @DisplayName("Deve retornar BAIXA quando estado permaneceu igual")
        void deveRetornarBaixaQuandoIgual() {
            GravidadeDivergencia resultado = service.classificarDivergenciaEstado("BOM", "BOM");
            assertEquals(GravidadeDivergencia.BAIXA, resultado);
        }
        
        @Test
        @DisplayName("Deve tratar valores nulos como REGULAR")
        void deveTratarValoresNulosComoRegular() {
            // null é tratado como REGULAR (3)
            GravidadeDivergencia resultado = service.classificarDivergenciaEstado(null, "BOM");
            assertNotNull(resultado);
            
            resultado = service.classificarDivergenciaEstado("BOM", null);
            assertNotNull(resultado);
        }
    }
    
    @Nested
    @DisplayName("Detecção de Tipo de Divergência")
    class DeteccaoTipo {
        
        @Test
        @DisplayName("Deve retornar OUTRO quando coleta ou patrimônio é nulo")
        void deveRetornarOutroQuandoNulo() {
            TipoDivergencia resultado = service.detectarTipo(null, null);
            assertEquals(TipoDivergencia.OUTRO, resultado);
        }
    }
}
