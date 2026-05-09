package com.inventario.sihcp.analytics.service;

import com.inventario.sihcp.analytics.model.GravidadeDivergencia;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Property-Based Tests para DivergenciaClassificadorService.
 * 
 * Usa junit-quickcheck para gerar casos de teste aleatórios.
 */
@RunWith(JUnitQuickcheck.class)
public class DivergenciaClassificadorPropertyTest {
    
    private final DivergenciaClassificadorService service = new DivergenciaClassificadorService();
    
    // Estados de conservação válidos
    private static final String[] ESTADOS = {"EXCELENTE", "BOM", "REGULAR", "RUIM", "PESSIMO", "INSERVIVEL"};
    private static final int[] NIVEIS = {5, 4, 3, 2, 1, 0};
    
    /**
     * **Feature: analytics-divergencias-metricas, Property 2: Classificação de gravidade de localização é determinística**
     * **Validates: Requirements 1.2**
     * 
     * Para qualquer par de setores, a classificação deve ser:
     * - ALTA se setores diferentes
     * - MÉDIA ou BAIXA se mesmo setor
     */
    @Property(trials = 100)
    public void classificacaoLocalizacaoEDeterministica(
            @InRange(minInt = 1, maxInt = 100) int setorCadastrado,
            @InRange(minInt = 1, maxInt = 100) int setorEncontrado) {
        
        GravidadeDivergencia resultado = service.classificarDivergenciaLocalizacao(
                "Sala A", "Sala B",
                setorCadastrado, setorEncontrado
        );
        
        assertNotNull("Resultado não pode ser nulo", resultado);
        
        if (setorCadastrado != setorEncontrado) {
            assertEquals("Mudança de setor deve ser ALTA", GravidadeDivergencia.ALTA, resultado);
        } else {
            // Mesmo setor: pode ser MÉDIA ou BAIXA
            assertTrue("Mesmo setor deve ser MÉDIA ou BAIXA",
                    resultado == GravidadeDivergencia.MEDIA || resultado == GravidadeDivergencia.BAIXA);
        }
    }
    
    /**
     * **Feature: analytics-divergencias-metricas, Property 3: Classificação de gravidade de estado é determinística**
     * **Validates: Requirements 1.3**
     * 
     * Para qualquer par de estados, a classificação deve seguir as regras:
     * - CRÍTICA se piorou 2+ níveis
     * - ALTA se piorou 1 nível
     * - BAIXA se melhorou ou igual
     */
    @Property(trials = 100)
    public void classificacaoEstadoEDeterministica(
            @InRange(minInt = 0, maxInt = 5) int indiceCadastrado,
            @InRange(minInt = 0, maxInt = 5) int indiceEncontrado) {
        
        String estadoCadastrado = ESTADOS[indiceCadastrado];
        String estadoEncontrado = ESTADOS[indiceEncontrado];
        int nivelCadastrado = NIVEIS[indiceCadastrado];
        int nivelEncontrado = NIVEIS[indiceEncontrado];
        
        GravidadeDivergencia resultado = service.classificarDivergenciaEstado(
                estadoCadastrado, estadoEncontrado
        );
        
        assertNotNull("Resultado não pode ser nulo", resultado);
        
        int diferenca = nivelCadastrado - nivelEncontrado;
        
        if (diferenca >= 2) {
            assertEquals("Piora de 2+ níveis deve ser CRÍTICA", 
                    GravidadeDivergencia.CRITICA, resultado);
        } else if (diferenca == 1) {
            assertEquals("Piora de 1 nível deve ser ALTA", 
                    GravidadeDivergencia.ALTA, resultado);
        } else {
            assertEquals("Melhora ou igual deve ser BAIXA", 
                    GravidadeDivergencia.BAIXA, resultado);
        }
    }
    
    /**
     * Propriedade: Classificação é idempotente.
     * 
     * Chamar a classificação múltiplas vezes com os mesmos parâmetros
     * deve sempre retornar o mesmo resultado.
     */
    @Property(trials = 50)
    public void classificacaoEIdempotente(
            @InRange(minInt = 0, maxInt = 5) int indiceCadastrado,
            @InRange(minInt = 0, maxInt = 5) int indiceEncontrado) {
        
        String estadoCadastrado = ESTADOS[indiceCadastrado];
        String estadoEncontrado = ESTADOS[indiceEncontrado];
        
        GravidadeDivergencia resultado1 = service.classificarDivergenciaEstado(
                estadoCadastrado, estadoEncontrado
        );
        GravidadeDivergencia resultado2 = service.classificarDivergenciaEstado(
                estadoCadastrado, estadoEncontrado
        );
        GravidadeDivergencia resultado3 = service.classificarDivergenciaEstado(
                estadoCadastrado, estadoEncontrado
        );
        
        assertEquals("Classificação deve ser idempotente", resultado1, resultado2);
        assertEquals("Classificação deve ser idempotente", resultado2, resultado3);
    }
    
    /**
     * Propriedade: Gravidade nunca é nula.
     * 
     * Para qualquer entrada, a classificação deve retornar um valor válido.
     */
    @Property(trials = 100)
    public void gravidadeNuncaENula(
            @InRange(minInt = 1, maxInt = 1000) int setorCadastrado,
            @InRange(minInt = 1, maxInt = 1000) int setorEncontrado) {
        
        GravidadeDivergencia resultado = service.classificarDivergenciaLocalizacao(
                "Local A", "Local B",
                setorCadastrado, setorEncontrado
        );
        
        assertNotNull("Gravidade nunca pode ser nula", resultado);
    }
}
