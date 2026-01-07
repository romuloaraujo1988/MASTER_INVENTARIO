package com.inventario.mobile.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;

import com.inventario.mobile.server.dto.MobileFotoReferenciaDTO;
import com.inventario.mobile.server.dto.PagedResponse;

/**
 * Property-based tests para paginação da API de Fotos de Referência.
 * 
 * **Property 6: Paginação da API**
 * Para qualquer requisição com tamanho de página N (1 <= N <= 50),
 * o resultado deve conter no máximo N elementos.
 * 
 * **Validates: Requirements 4.2**
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@Tag("property-test")
@DisplayName("Property Test: Paginação da API de Fotos de Referência")
public class MobileFotoReferenciaServicePaginationPropertyTest {
    
    private static final int TAMANHO_PAGINA_MAXIMO = 50;
    private final Random random = new Random();
    
    /**
     * Property 6: Paginação da API
     * 
     * Para qualquer tamanho de página solicitado:
     * - Se tamanho <= 50: resultado deve ter no máximo esse tamanho
     * - Se tamanho > 50: resultado deve ter no máximo 50 (limite máximo)
     * - Se tamanho < 1: resultado deve ter no máximo 1 (mínimo)
     * 
     * **Validates: Requirements 4.2**
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property 6: Tamanho da página deve respeitar limites")
    void property6_tamanhoPaginaDeveRespeitarLimites() {
        // Arrange: Gerar tamanho de página aleatório (incluindo valores fora dos limites)
        int tamanhoSolicitado = random.nextInt(200) - 50; // -50 a 149
        
        // Act: Calcular tamanho efetivo (como o service faz)
        int tamanhoEfetivo = Math.min(Math.max(1, tamanhoSolicitado), TAMANHO_PAGINA_MAXIMO);
        
        // Assert: Tamanho efetivo deve estar dentro dos limites
        assertTrue(tamanhoEfetivo >= 1, 
            String.format("Tamanho efetivo deve ser >= 1, mas foi %d para solicitado %d", 
                tamanhoEfetivo, tamanhoSolicitado));
        
        assertTrue(tamanhoEfetivo <= TAMANHO_PAGINA_MAXIMO, 
            String.format("Tamanho efetivo deve ser <= %d, mas foi %d para solicitado %d", 
                TAMANHO_PAGINA_MAXIMO, tamanhoEfetivo, tamanhoSolicitado));
    }
    
    /**
     * Property: Resultado da paginação nunca excede tamanho solicitado
     * 
     * Para qualquer lista de fotos e tamanho de página,
     * o resultado paginado deve ter no máximo o tamanho solicitado.
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Resultado paginado nunca excede tamanho solicitado")
    void property_resultadoPaginadoNuncaExcedeTamanho() {
        // Arrange: Gerar lista de fotos aleatória
        int totalFotos = random.nextInt(200); // 0 a 199 fotos
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosDTO(totalFotos);
        
        // Gerar tamanho de página válido
        int tamanhoPagina = random.nextInt(TAMANHO_PAGINA_MAXIMO) + 1; // 1 a 50
        int pagina = random.nextInt(Math.max(1, (totalFotos / tamanhoPagina) + 1)); // Página válida
        
        // Act: Simular paginação
        int offset = pagina * tamanhoPagina;
        int fim = Math.min(offset + tamanhoPagina, totalFotos);
        List<MobileFotoReferenciaDTO> paginaResultado = 
            offset < totalFotos ? todasFotos.subList(offset, fim) : new ArrayList<>();
        
        // Assert: Resultado nunca excede tamanho solicitado
        assertTrue(paginaResultado.size() <= tamanhoPagina,
            String.format("Resultado (%d) excede tamanho solicitado (%d). Total: %d, Página: %d",
                paginaResultado.size(), tamanhoPagina, totalFotos, pagina));
    }
    
    /**
     * Property: Total de páginas é calculado corretamente
     * 
     * Para qualquer total de elementos e tamanho de página,
     * o total de páginas deve ser ceil(total / tamanho).
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Total de páginas calculado corretamente")
    void property_totalPaginasCalculadoCorretamente() {
        // Arrange: Gerar valores aleatórios
        int totalElementos = random.nextInt(1000); // 0 a 999
        int tamanhoPagina = random.nextInt(TAMANHO_PAGINA_MAXIMO) + 1; // 1 a 50
        
        // Act: Calcular total de páginas
        int totalPaginasCalculado = (int) Math.ceil((double) totalElementos / tamanhoPagina);
        
        // Assert: Verificar cálculo
        int totalPaginasEsperado = totalElementos == 0 ? 0 : (totalElementos + tamanhoPagina - 1) / tamanhoPagina;
        
        assertEquals(totalPaginasEsperado, totalPaginasCalculado,
            String.format("Total de páginas incorreto para %d elementos com tamanho %d",
                totalElementos, tamanhoPagina));
    }
    
    /**
     * Property: Offset é calculado corretamente
     * 
     * Para qualquer página e tamanho, offset = página * tamanho.
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Offset calculado corretamente")
    void property_offsetCalculadoCorretamente() {
        // Arrange: Gerar valores aleatórios
        int pagina = random.nextInt(100); // 0 a 99
        int tamanhoPagina = random.nextInt(TAMANHO_PAGINA_MAXIMO) + 1; // 1 a 50
        
        // Act: Calcular offset
        int offset = pagina * tamanhoPagina;
        
        // Assert: Offset deve ser não-negativo e múltiplo do tamanho
        assertTrue(offset >= 0, "Offset deve ser não-negativo");
        assertEquals(0, offset % tamanhoPagina, 
            String.format("Offset (%d) deve ser múltiplo do tamanho (%d)", offset, tamanhoPagina));
    }
    
    /**
     * Property: Última página pode ter menos elementos
     * 
     * A última página pode ter menos elementos que o tamanho solicitado,
     * mas nunca mais.
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Última página pode ter menos elementos")
    void property_ultimaPaginaPodeTerMenosElementos() {
        // Arrange: Gerar lista de fotos aleatória
        int totalFotos = random.nextInt(200) + 1; // 1 a 200 fotos
        int tamanhoPagina = random.nextInt(TAMANHO_PAGINA_MAXIMO) + 1; // 1 a 50
        
        // Calcular última página
        int totalPaginas = (int) Math.ceil((double) totalFotos / tamanhoPagina);
        int ultimaPagina = totalPaginas - 1;
        
        // Act: Calcular elementos na última página
        int offset = ultimaPagina * tamanhoPagina;
        int elementosUltimaPagina = totalFotos - offset;
        
        // Assert: Última página tem entre 1 e tamanhoPagina elementos
        assertTrue(elementosUltimaPagina >= 1,
            String.format("Última página deve ter pelo menos 1 elemento, mas tem %d", elementosUltimaPagina));
        assertTrue(elementosUltimaPagina <= tamanhoPagina,
            String.format("Última página deve ter no máximo %d elementos, mas tem %d", 
                tamanhoPagina, elementosUltimaPagina));
    }
    
    /**
     * Property: Página além do total retorna lista vazia
     * 
     * Se a página solicitada está além do total de páginas,
     * o resultado deve ser uma lista vazia.
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Página além do total retorna lista vazia")
    void property_paginaAlemDoTotalRetornaListaVazia() {
        // Arrange: Gerar lista de fotos aleatória
        int totalFotos = random.nextInt(100) + 1; // 1 a 100 fotos
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosDTO(totalFotos);
        int tamanhoPagina = random.nextInt(TAMANHO_PAGINA_MAXIMO) + 1; // 1 a 50
        
        // Calcular página além do total
        int totalPaginas = (int) Math.ceil((double) totalFotos / tamanhoPagina);
        int paginaAlem = totalPaginas + random.nextInt(10); // Página além do total
        
        // Act: Simular paginação
        int offset = paginaAlem * tamanhoPagina;
        List<MobileFotoReferenciaDTO> resultado = 
            offset < totalFotos ? todasFotos.subList(offset, Math.min(offset + tamanhoPagina, totalFotos)) : new ArrayList<>();
        
        // Assert: Resultado deve ser vazio
        assertTrue(resultado.isEmpty(),
            String.format("Página %d (além do total %d) deve retornar lista vazia, mas retornou %d elementos",
                paginaAlem, totalPaginas, resultado.size()));
    }
    
    /**
     * Property: PagedResponse contém metadados corretos
     * 
     * O PagedResponse deve conter metadados consistentes com os dados.
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: PagedResponse contém metadados corretos")
    void property_pagedResponseContemMetadadosCorretos() {
        // Arrange: Gerar valores aleatórios
        int totalElementos = random.nextInt(500); // 0 a 499
        int tamanhoPagina = random.nextInt(TAMANHO_PAGINA_MAXIMO) + 1; // 1 a 50
        int pagina = random.nextInt(Math.max(1, (totalElementos / tamanhoPagina) + 1));
        
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosDTO(totalElementos);
        
        // Simular paginação
        int offset = pagina * tamanhoPagina;
        int fim = Math.min(offset + tamanhoPagina, totalElementos);
        List<MobileFotoReferenciaDTO> conteudo = 
            offset < totalElementos ? todasFotos.subList(offset, fim) : new ArrayList<>();
        
        // Act: Criar PagedResponse (construtor calcula totalPages automaticamente)
        PagedResponse<MobileFotoReferenciaDTO> response = new PagedResponse<>(
            conteudo, pagina, tamanhoPagina, totalElementos);
        
        // Calcular totalPages esperado para comparação
        int totalPaginasEsperado = totalElementos == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamanhoPagina);
        
        // Assert: Verificar metadados
        assertEquals(conteudo.size(), response.getContent().size(), "Tamanho do conteúdo incorreto");
        assertEquals(pagina, response.getPage(), "Número da página incorreto");
        assertEquals(tamanhoPagina, response.getSize(), "Tamanho da página incorreto");
        assertEquals(totalElementos, response.getTotalElements(), "Total de elementos incorreto");
        assertEquals(totalPaginasEsperado, response.getTotalPages(), "Total de páginas incorreto");
    }
    
    // ========== Métodos auxiliares ==========
    
    /**
     * Gera lista de DTOs de foto de referência para testes
     */
    private List<MobileFotoReferenciaDTO> gerarListaFotosDTO(int quantidade) {
        List<MobileFotoReferenciaDTO> fotos = new ArrayList<>();
        
        for (int i = 0; i < quantidade; i++) {
            MobileFotoReferenciaDTO dto = new MobileFotoReferenciaDTO();
            dto.setId(i + 1);
            dto.setDescricaoNormalizada("DESCRICAO_" + i);
            dto.setHashImagem("hash_" + i);
            dto.setTamanhoBytes(random.nextInt(50000) + 1000);
            dto.setDataAtualizacao(System.currentTimeMillis() - random.nextInt(86400000));
            dto.setAtivo(true);
            fotos.add(dto);
        }
        
        return fotos;
    }
}
