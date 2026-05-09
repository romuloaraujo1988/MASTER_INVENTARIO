package com.inventario.sihcp.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.inventario.sihcp.model.DescricaoResumo;

/**
 * Property-based tests para o filtro de busca do FotoReferenciaFrame.
 * 
 * Feature: foto-referencia-descricao
 * Property 5: Filtro de Busca
 * Validates: Requirements 1.7
 * 
 * Testes baseados em propriedades que verificam:
 * - Resultados filtrados contêm o texto buscado (case-insensitive)
 * - Filtro vazio retorna todos os itens
 * - Filtro é case-insensitive
 * - Resultados são subconjunto da lista original
 */
@DisplayName("FotoReferenciaFrame - Property Tests para Filtro de Busca")
public class FotoReferenciaFrameFilterPropertyTest {
    
    private Random random;
    private FilterHelper filterHelper;
    
    private static final String[] DESCRICOES_EXEMPLO = {
        "CADEIRA GIRATORIA EXECUTIVA",
        "CADEIRA FIXA ESTOFADA",
        "MESA DE ESCRITORIO 1.20M",
        "MESA DE REUNIAO 2.40M",
        "MONITOR LED 24 POLEGADAS",
        "MONITOR LCD 19 POLEGADAS",
        "COMPUTADOR DESKTOP DELL",
        "COMPUTADOR DESKTOP HP",
        "NOTEBOOK DELL LATITUDE",
        "NOTEBOOK LENOVO THINKPAD",
        "IMPRESSORA LASER HP",
        "IMPRESSORA JATO DE TINTA EPSON",
        "AR CONDICIONADO SPLIT 12000 BTU",
        "AR CONDICIONADO SPLIT 18000 BTU",
        "PROJETOR MULTIMIDIA EPSON",
        "TELEFONE IP CISCO",
        "ARMARIO DE ACO 2 PORTAS",
        "ARMARIO DE MADEIRA 3 PORTAS",
        "ESTANTE DE ACO 5 PRATELEIRAS",
        "GAVETEIRO VOLANTE 3 GAVETAS"
    };
    
    @BeforeEach
    void setUp() {
        random = new Random();
        filterHelper = new FilterHelper();
    }
    
    // ========================================================================
    // Property 5: Filtro de Busca
    // For any texto de busca e lista de descricoes, o resultado filtrado deve
    // conter apenas descricoes que incluem o texto buscado (case-insensitive).
    // ========================================================================
    
    @Nested
    @DisplayName("Property 5.1: Resultados Contêm Texto Buscado")
    class ResultadosContemTextoBuscado {
        
        /**
         * Property: For any texto de busca e lista de descricoes, todos os
         * resultados filtrados devem conter o texto buscado.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Todos os resultados devem conter o texto buscado")
        void todosResultadosDevemConterTextoBuscado() {
            // Arrange: gerar lista de descricoes e texto de busca aleatorio
            List<DescricaoResumo> descricoes = gerarListaDescricoes(10 + random.nextInt(20));
            String textoBusca = gerarTextoBuscaAleatorio();
            
            // Act
            List<DescricaoResumo> resultados = filterHelper.filtrar(descricoes, textoBusca);
            
            // Assert: todos os resultados devem conter o texto buscado
            String textoBuscaLower = textoBusca.toLowerCase();
            for (DescricaoResumo resultado : resultados) {
                assertTrue(
                    resultado.getDescricaoNormalizada().toLowerCase().contains(textoBuscaLower),
                    String.format(
                        "Resultado '%s' deve conter texto buscado '%s'",
                        resultado.getDescricaoNormalizada(), textoBusca
                    )
                );
            }
        }
        
        /**
         * Property: For any texto de busca presente em alguma descricao,
         * o filtro deve retornar pelo menos um resultado.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Filtro deve encontrar descricoes que contem o texto")
        void filtroDeveEncontrarDescricoesQueContemTexto() {
            // Arrange: gerar lista e escolher texto que existe em alguma descricao
            List<DescricaoResumo> descricoes = gerarListaDescricoes(10);
            DescricaoResumo descricaoAleatoria = descricoes.get(random.nextInt(descricoes.size()));
            String textoExistente = extrairSubstringAleatoria(descricaoAleatoria.getDescricaoNormalizada());
            
            // Act
            List<DescricaoResumo> resultados = filterHelper.filtrar(descricoes, textoExistente);
            
            // Assert: deve encontrar pelo menos um resultado
            assertFalse(resultados.isEmpty(),
                String.format(
                    "Filtro com texto '%s' deve encontrar pelo menos um resultado na lista",
                    textoExistente
                )
            );
        }
    }
    
    @Nested
    @DisplayName("Property 5.2: Case-Insensitive")
    class CaseInsensitive {
        
        /**
         * Property: For any texto de busca, o filtro deve ser case-insensitive.
         * filtrar(lista, "ABC") == filtrar(lista, "abc") == filtrar(lista, "AbC")
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Filtro deve ser case-insensitive")
        void filtroDeveSerCaseInsensitive() {
            // Arrange
            List<DescricaoResumo> descricoes = gerarListaDescricoes(15);
            String textoOriginal = gerarTextoBuscaAleatorio();
            String textoUpper = textoOriginal.toUpperCase();
            String textoLower = textoOriginal.toLowerCase();
            String textoMixed = misturarCase(textoOriginal);
            
            // Act
            List<DescricaoResumo> resultadosOriginal = filterHelper.filtrar(descricoes, textoOriginal);
            List<DescricaoResumo> resultadosUpper = filterHelper.filtrar(descricoes, textoUpper);
            List<DescricaoResumo> resultadosLower = filterHelper.filtrar(descricoes, textoLower);
            List<DescricaoResumo> resultadosMixed = filterHelper.filtrar(descricoes, textoMixed);
            
            // Assert: todos devem retornar o mesmo numero de resultados
            assertEquals(resultadosOriginal.size(), resultadosUpper.size(),
                "Resultados com UPPER devem ser iguais aos originais");
            assertEquals(resultadosOriginal.size(), resultadosLower.size(),
                "Resultados com lower devem ser iguais aos originais");
            assertEquals(resultadosOriginal.size(), resultadosMixed.size(),
                "Resultados com MiXeD devem ser iguais aos originais");
        }
        
        private String misturarCase(String texto) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < texto.length(); i++) {
                char c = texto.charAt(i);
                if (random.nextBoolean()) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
            return sb.toString();
        }
    }

    @Nested
    @DisplayName("Property 5.3: Filtro Vazio Retorna Todos")
    class FiltroVazioRetornaTodos {
        
        /**
         * Property: For any lista de descricoes, filtrar com string vazia
         * deve retornar todos os itens.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Filtro vazio deve retornar todos os itens")
        void filtroVazioDeveRetornarTodos() {
            // Arrange
            List<DescricaoResumo> descricoes = gerarListaDescricoes(5 + random.nextInt(20));
            
            // Act
            List<DescricaoResumo> resultadosVazio = filterHelper.filtrar(descricoes, "");
            List<DescricaoResumo> resultadosEspacos = filterHelper.filtrar(descricoes, "   ");
            
            // Assert
            assertEquals(descricoes.size(), resultadosVazio.size(),
                "Filtro vazio deve retornar todos os itens");
            assertEquals(descricoes.size(), resultadosEspacos.size(),
                "Filtro com apenas espacos deve retornar todos os itens");
        }
        
        @Test
        @DisplayName("Filtro null deve retornar todos os itens")
        void filtroNullDeveRetornarTodos() {
            // Arrange
            List<DescricaoResumo> descricoes = gerarListaDescricoes(10);
            
            // Act
            List<DescricaoResumo> resultados = filterHelper.filtrar(descricoes, null);
            
            // Assert
            assertEquals(descricoes.size(), resultados.size(),
                "Filtro null deve retornar todos os itens");
        }
    }
    
    @Nested
    @DisplayName("Property 5.4: Resultados São Subconjunto")
    class ResultadosSaoSubconjunto {
        
        /**
         * Property: For any filtro aplicado, os resultados devem ser um
         * subconjunto da lista original.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Resultados devem ser subconjunto da lista original")
        void resultadosDevemSerSubconjunto() {
            // Arrange
            List<DescricaoResumo> descricoes = gerarListaDescricoes(15);
            String textoBusca = gerarTextoBuscaAleatorio();
            
            // Act
            List<DescricaoResumo> resultados = filterHelper.filtrar(descricoes, textoBusca);
            
            // Assert: tamanho dos resultados <= tamanho original
            assertTrue(resultados.size() <= descricoes.size(),
                "Resultados nao podem ser maiores que a lista original");
            
            // Assert: todos os resultados devem estar na lista original
            for (DescricaoResumo resultado : resultados) {
                assertTrue(
                    descricoes.stream().anyMatch(d -> 
                        d.getDescricaoNormalizada().equals(resultado.getDescricaoNormalizada())
                    ),
                    "Resultado deve estar na lista original: " + resultado.getDescricaoNormalizada()
                );
            }
        }
        
        /**
         * Property: For any filtro, nenhum item que NAO contem o texto
         * deve aparecer nos resultados.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Itens que nao contem o texto nao devem aparecer")
        void itensQueNaoContemTextoNaoDevemAparecer() {
            // Arrange
            List<DescricaoResumo> descricoes = gerarListaDescricoes(15);
            String textoBusca = gerarTextoBuscaAleatorio();
            
            // Act
            List<DescricaoResumo> resultados = filterHelper.filtrar(descricoes, textoBusca);
            
            // Calcular itens que NAO deveriam aparecer
            String textoBuscaLower = textoBusca.toLowerCase().trim();
            List<DescricaoResumo> naoDeveriamAparecer = descricoes.stream()
                .filter(d -> !d.getDescricaoNormalizada().toLowerCase().contains(textoBuscaLower))
                .collect(Collectors.toList());
            
            // Assert: nenhum dos itens que nao contem o texto deve estar nos resultados
            for (DescricaoResumo naoDeveria : naoDeveriamAparecer) {
                assertFalse(
                    resultados.stream().anyMatch(r -> 
                        r.getDescricaoNormalizada().equals(naoDeveria.getDescricaoNormalizada())
                    ),
                    "Item que nao contem o texto nao deve aparecer: " + naoDeveria.getDescricaoNormalizada()
                );
            }
        }
    }
    
    @Nested
    @DisplayName("Property 5.5: Completude do Filtro")
    class CompletudeFiltro {
        
        /**
         * Property: For any filtro, TODOS os itens que contem o texto
         * devem aparecer nos resultados.
         */
        @RepeatedTest(value = 100, name = "Iteracao {currentRepetition}/{totalRepetitions}")
        @DisplayName("Todos os itens que contem o texto devem aparecer")
        void todosItensQueContemTextoDevemAparecer() {
            // Arrange
            List<DescricaoResumo> descricoes = gerarListaDescricoes(15);
            String textoBusca = gerarTextoBuscaAleatorio();
            
            // Act
            List<DescricaoResumo> resultados = filterHelper.filtrar(descricoes, textoBusca);
            
            // Calcular itens que DEVERIAM aparecer
            String textoBuscaLower = textoBusca.toLowerCase().trim();
            if (textoBuscaLower.isEmpty()) {
                // Filtro vazio - todos devem aparecer
                assertEquals(descricoes.size(), resultados.size());
                return;
            }
            
            List<DescricaoResumo> deveriamAparecer = descricoes.stream()
                .filter(d -> d.getDescricaoNormalizada().toLowerCase().contains(textoBuscaLower))
                .collect(Collectors.toList());
            
            // Assert: todos os itens que contem o texto devem estar nos resultados
            assertEquals(deveriamAparecer.size(), resultados.size(),
                String.format(
                    "Quantidade de resultados incorreta. Esperado: %d, Obtido: %d, Filtro: '%s'",
                    deveriamAparecer.size(), resultados.size(), textoBusca
                )
            );
        }
    }
    
    // ========================================================================
    // Métodos auxiliares
    // ========================================================================
    
    private List<DescricaoResumo> gerarListaDescricoes(int quantidade) {
        List<DescricaoResumo> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            String descricao = DESCRICOES_EXEMPLO[random.nextInt(DESCRICOES_EXEMPLO.length)];
            // Adicionar variacao para evitar duplicatas
            if (random.nextBoolean()) {
                descricao = descricao + " " + (random.nextInt(100) + 1);
            }
            lista.add(new DescricaoResumo(
                descricao,
                1 + random.nextInt(50),
                random.nextBoolean(),
                random.nextBoolean() ? random.nextInt(1000) : 0
            ));
        }
        return lista;
    }
    
    private String gerarTextoBuscaAleatorio() {
        // Escolher uma descricao aleatoria e extrair parte dela
        String descricao = DESCRICOES_EXEMPLO[random.nextInt(DESCRICOES_EXEMPLO.length)];
        return extrairSubstringAleatoria(descricao);
    }
    
    private String extrairSubstringAleatoria(String texto) {
        if (texto == null || texto.length() < 3) {
            return texto;
        }
        
        // Dividir em palavras e escolher uma ou parte de uma
        String[] palavras = texto.split("\\s+");
        if (palavras.length == 0) {
            return texto;
        }
        
        // Escolher uma palavra aleatoria
        String palavra = palavras[random.nextInt(palavras.length)];
        
        // Se a palavra for muito curta, retornar ela inteira
        if (palavra.length() <= 3) {
            return palavra;
        }
        
        // Extrair substring de 2 a 6 caracteres da palavra
        int tamanho = 2 + random.nextInt(Math.min(5, palavra.length() - 2));
        int inicio = random.nextInt(palavra.length() - tamanho);
        return palavra.substring(inicio, inicio + tamanho);
    }
    
    /**
     * Classe auxiliar que implementa a mesma logica de filtragem do FotoReferenciaFrame.
     * Isso permite testar a logica de forma isolada sem depender da UI.
     */
    static class FilterHelper {
        
        /**
         * Filtra a lista de descricoes pelo texto de busca.
         * Implementa a mesma logica do metodo filtrarDescricoes() do FotoReferenciaFrame.
         * 
         * @param descricoes Lista de descricoes
         * @param filtro Texto de busca
         * @return Lista filtrada
         */
        public List<DescricaoResumo> filtrar(List<DescricaoResumo> descricoes, String filtro) {
            if (descricoes == null) {
                return new ArrayList<>();
            }
            
            if (filtro == null || filtro.trim().isEmpty()) {
                return new ArrayList<>(descricoes);
            }
            
            String filtroLower = filtro.trim().toLowerCase();
            
            return descricoes.stream()
                .filter(d -> d.getDescricaoNormalizada().toLowerCase().contains(filtroLower))
                .collect(Collectors.toList());
        }
    }
}
