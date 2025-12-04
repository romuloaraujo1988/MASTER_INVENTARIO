package com.inventario.service;

import com.inventario.model.ItemCompostoResumo;
import com.inventario.model.StatusIntegridade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RelatorioItemCompostoService.
 * 
 * **Feature: relatorio-itens-compostos, Property 10: Ordenação mantém dados corretos**
 * **Feature: relatorio-itens-compostos, Property 11: Ordenação numérica para taxa de integridade**
 * **Validates: Requirements 6.1, 6.2, 6.3, 6.4**
 */
@DisplayName("RelatorioItemCompostoService - Testes de Ordenação")
class RelatorioItemCompostoServiceTest {
    
    private RelatorioItemCompostoService service;
    private List<ItemCompostoResumo> itensParaTeste;
    
    @BeforeEach
    void setUp() {
        service = new RelatorioItemCompostoService(null); // DAO não é usado nos testes de ordenação
        itensParaTeste = criarItensParaTeste();
    }
    
    private List<ItemCompostoResumo> criarItensParaTeste() {
        List<ItemCompostoResumo> itens = new ArrayList<>();
        
        // Item 1: Taxa 100%, Completo
        ItemCompostoResumo item1 = new ItemCompostoResumo();
        item1.setIdPatrimonio(1);
        item1.setNumeroPatrimonio("12345");
        item1.setDescricaoPatrimonio("Conjunto A");
        item1.setNomeSala("Sala 101");
        item1.setNomeResponsavel("Ana Silva");
        item1.setComponentesEsperados(5);
        item1.setComponentesEncontrados(5);
        item1.setComponentesFaltantes(0);
        item1.setTaxaIntegridade(100.0);
        item1.setStatus(StatusIntegridade.COMPLETO);
        itens.add(item1);
        
        // Item 2: Taxa 60%, Parcial
        ItemCompostoResumo item2 = new ItemCompostoResumo();
        item2.setIdPatrimonio(2);
        item2.setNumeroPatrimonio("67890");
        item2.setDescricaoPatrimonio("Conjunto B");
        item2.setNomeSala("Sala 202");
        item2.setNomeResponsavel("Carlos Santos");
        item2.setComponentesEsperados(5);
        item2.setComponentesEncontrados(3);
        item2.setComponentesFaltantes(2);
        item2.setTaxaIntegridade(60.0);
        item2.setStatus(StatusIntegridade.PARCIAL);
        itens.add(item2);
        
        // Item 3: Taxa 0%, Incompleto
        ItemCompostoResumo item3 = new ItemCompostoResumo();
        item3.setIdPatrimonio(3);
        item3.setNumeroPatrimonio("11111");
        item3.setDescricaoPatrimonio("Conjunto C");
        item3.setNomeSala("Sala 303");
        item3.setNomeResponsavel("Beatriz Lima");
        item3.setComponentesEsperados(5);
        item3.setComponentesEncontrados(0);
        item3.setComponentesFaltantes(5);
        item3.setTaxaIntegridade(0.0);
        item3.setStatus(StatusIntegridade.INCOMPLETO);
        itens.add(item3);
        
        // Item 4: Taxa 80%, Parcial
        ItemCompostoResumo item4 = new ItemCompostoResumo();
        item4.setIdPatrimonio(4);
        item4.setNumeroPatrimonio("22222");
        item4.setDescricaoPatrimonio("Conjunto D");
        item4.setNomeSala("Sala 101");
        item4.setNomeResponsavel("Daniel Costa");
        item4.setComponentesEsperados(10);
        item4.setComponentesEncontrados(8);
        item4.setComponentesFaltantes(2);
        item4.setTaxaIntegridade(80.0);
        item4.setStatus(StatusIntegridade.PARCIAL);
        itens.add(item4);
        
        return itens;
    }
    
    @Nested
    @DisplayName("Testes de Ordenação Crescente/Decrescente")
    class OrdenacaoCrescenteDecrescente {
        
        @Test
        @DisplayName("ordenar() crescente por número patrimônio")
        void ordenar_PorNumero_Crescente() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "numero", true);
            
            assertEquals("11111", resultado.get(0).getNumeroPatrimonio());
            assertEquals("12345", resultado.get(1).getNumeroPatrimonio());
            assertEquals("22222", resultado.get(2).getNumeroPatrimonio());
            assertEquals("67890", resultado.get(3).getNumeroPatrimonio());
        }
        
        @Test
        @DisplayName("ordenar() decrescente por número patrimônio")
        void ordenar_PorNumero_Decrescente() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "numero", false);
            
            assertEquals("67890", resultado.get(0).getNumeroPatrimonio());
            assertEquals("22222", resultado.get(1).getNumeroPatrimonio());
            assertEquals("12345", resultado.get(2).getNumeroPatrimonio());
            assertEquals("11111", resultado.get(3).getNumeroPatrimonio());
        }
        
        @Test
        @DisplayName("ordenar() crescente por descrição")
        void ordenar_PorDescricao_Crescente() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "descricao", true);
            
            assertEquals("Conjunto A", resultado.get(0).getDescricaoPatrimonio());
            assertEquals("Conjunto B", resultado.get(1).getDescricaoPatrimonio());
            assertEquals("Conjunto C", resultado.get(2).getDescricaoPatrimonio());
            assertEquals("Conjunto D", resultado.get(3).getDescricaoPatrimonio());
        }
        
        @Test
        @DisplayName("ordenar() decrescente por descrição")
        void ordenar_PorDescricao_Decrescente() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "descricao", false);
            
            assertEquals("Conjunto D", resultado.get(0).getDescricaoPatrimonio());
            assertEquals("Conjunto C", resultado.get(1).getDescricaoPatrimonio());
            assertEquals("Conjunto B", resultado.get(2).getDescricaoPatrimonio());
            assertEquals("Conjunto A", resultado.get(3).getDescricaoPatrimonio());
        }
        
        @Test
        @DisplayName("ordenar() crescente por responsável")
        void ordenar_PorResponsavel_Crescente() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "responsavel", true);
            
            assertEquals("Ana Silva", resultado.get(0).getNomeResponsavel());
            assertEquals("Beatriz Lima", resultado.get(1).getNomeResponsavel());
            assertEquals("Carlos Santos", resultado.get(2).getNomeResponsavel());
            assertEquals("Daniel Costa", resultado.get(3).getNomeResponsavel());
        }
    }
    
    @Nested
    @DisplayName("Testes de Ordenação Numérica vs Alfabética")
    class OrdenacaoNumericaVsAlfabetica {
        
        @Test
        @DisplayName("ordenar() por taxa deve ser NUMÉRICA (não alfabética)")
        void ordenar_PorTaxa_NumericaNaoAlfabetica() {
            // Se fosse alfabética: "0.0", "100.0", "60.0", "80.0"
            // Numérica correta: 0.0, 60.0, 80.0, 100.0
            
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "taxa", true);
            
            assertEquals(0.0, resultado.get(0).getTaxaIntegridade(), 0.01);
            assertEquals(60.0, resultado.get(1).getTaxaIntegridade(), 0.01);
            assertEquals(80.0, resultado.get(2).getTaxaIntegridade(), 0.01);
            assertEquals(100.0, resultado.get(3).getTaxaIntegridade(), 0.01);
        }
        
        @Test
        @DisplayName("ordenar() por taxa decrescente deve ser NUMÉRICA")
        void ordenar_PorTaxa_Decrescente_Numerica() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "taxa", false);
            
            assertEquals(100.0, resultado.get(0).getTaxaIntegridade(), 0.01);
            assertEquals(80.0, resultado.get(1).getTaxaIntegridade(), 0.01);
            assertEquals(60.0, resultado.get(2).getTaxaIntegridade(), 0.01);
            assertEquals(0.0, resultado.get(3).getTaxaIntegridade(), 0.01);
        }
        
        @Test
        @DisplayName("ordenar() por componentes esperados deve ser numérica")
        void ordenar_PorEsperados_Numerica() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "esperados", true);
            
            // Todos têm 5 esperados exceto item4 que tem 10
            assertEquals(10, resultado.get(3).getComponentesEsperados());
        }
        
        @Test
        @DisplayName("ordenar() por componentes faltantes deve ser numérica")
        void ordenar_PorFaltantes_Decrescente() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "faltantes", false);
            
            assertEquals(5, resultado.get(0).getComponentesFaltantes()); // Item 3
            assertEquals(2, resultado.get(1).getComponentesFaltantes()); // Item 2 ou 4
            assertEquals(2, resultado.get(2).getComponentesFaltantes()); // Item 2 ou 4
            assertEquals(0, resultado.get(3).getComponentesFaltantes()); // Item 1
        }
    }
    
    @Nested
    @DisplayName("Testes de Preservação de Dados")
    class PreservacaoDeDados {
        
        @Test
        @DisplayName("ordenar() deve manter todos os dados dos itens")
        void ordenar_MantemDadosCorretos() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "taxa", true);
            
            // Verificar que todos os 4 itens estão presentes
            assertEquals(4, resultado.size());
            
            // Verificar que os dados não foram alterados
            ItemCompostoResumo itemMenorTaxa = resultado.get(0);
            assertEquals(3, itemMenorTaxa.getIdPatrimonio());
            assertEquals("11111", itemMenorTaxa.getNumeroPatrimonio());
            assertEquals("Conjunto C", itemMenorTaxa.getDescricaoPatrimonio());
            assertEquals("Sala 303", itemMenorTaxa.getNomeSala());
            assertEquals("Beatriz Lima", itemMenorTaxa.getNomeResponsavel());
            assertEquals(5, itemMenorTaxa.getComponentesEsperados());
            assertEquals(0, itemMenorTaxa.getComponentesEncontrados());
            assertEquals(5, itemMenorTaxa.getComponentesFaltantes());
            assertEquals(StatusIntegridade.INCOMPLETO, itemMenorTaxa.getStatus());
        }
        
        @Test
        @DisplayName("ordenar() não deve modificar lista original")
        void ordenar_NaoModificaListaOriginal() {
            String primeiroNumeroOriginal = itensParaTeste.get(0).getNumeroPatrimonio();
            
            service.ordenar(itensParaTeste, "numero", false);
            
            // Lista original deve permanecer inalterada
            assertEquals(primeiroNumeroOriginal, itensParaTeste.get(0).getNumeroPatrimonio());
        }
        
        @Test
        @DisplayName("ordenar() deve retornar nova instância de lista")
        void ordenar_RetornaNovaInstancia() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "numero", true);
            
            assertNotSame(itensParaTeste, resultado);
        }
    }
    
    @Nested
    @DisplayName("Testes de Casos Especiais")
    class CasosEspeciais {
        
        @Test
        @DisplayName("ordenar() com lista vazia deve retornar lista vazia")
        void ordenar_ListaVazia_RetornaListaVazia() {
            List<ItemCompostoResumo> resultado = service.ordenar(Collections.emptyList(), "numero", true);
            
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
        
        @Test
        @DisplayName("ordenar() com lista nula deve retornar lista vazia")
        void ordenar_ListaNula_RetornaListaVazia() {
            List<ItemCompostoResumo> resultado = service.ordenar(null, "numero", true);
            
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
        
        @Test
        @DisplayName("ordenar() com coluna nula deve usar ordenação padrão (número)")
        void ordenar_ColunaNula_UsaOrdenacaoPadrao() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, null, true);
            
            assertEquals("11111", resultado.get(0).getNumeroPatrimonio());
        }
        
        @Test
        @DisplayName("ordenar() com coluna desconhecida deve usar ordenação padrão")
        void ordenar_ColunaDesconhecida_UsaOrdenacaoPadrao() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "coluna_invalida", true);
            
            assertEquals("11111", resultado.get(0).getNumeroPatrimonio());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"taxa", "taxaintegridade", "Taxa de Integridade"})
        @DisplayName("ordenar() deve aceitar diferentes variações do nome da coluna taxa")
        void ordenar_VariacoesNomeColunaTaxa(String nomeColuna) {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, nomeColuna, true);
            
            assertEquals(0.0, resultado.get(0).getTaxaIntegridade(), 0.01);
            assertEquals(100.0, resultado.get(3).getTaxaIntegridade(), 0.01);
        }
    }
    
    @Nested
    @DisplayName("Testes de Ordenação por Status")
    class OrdenacaoPorStatus {
        
        @Test
        @DisplayName("ordenar() por status deve ordenar por ordinal do enum")
        void ordenar_PorStatus_Crescente() {
            List<ItemCompostoResumo> resultado = service.ordenar(itensParaTeste, "status", true);
            
            // Ordem do enum: COMPLETO(0), INCOMPLETO(1), PARCIAL(2), TODOS(3)
            assertEquals(StatusIntegridade.COMPLETO, resultado.get(0).getStatus());
            assertEquals(StatusIntegridade.INCOMPLETO, resultado.get(1).getStatus());
        }
    }
}
