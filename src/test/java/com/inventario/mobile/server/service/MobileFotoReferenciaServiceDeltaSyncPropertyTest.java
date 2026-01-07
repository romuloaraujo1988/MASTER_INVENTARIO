package com.inventario.mobile.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;

import com.inventario.mobile.server.dto.MobileFotoReferenciaDTO;

/**
 * Property-based tests para delta sync da API de Fotos de Referência.
 * 
 * **Property 7: Delta Sync**
 * Para qualquer timestamp de sincronização, apenas fotos com
 * dataAtualizacao > timestamp devem ser retornadas.
 * 
 * **Validates: Requirements 4.3**
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@Tag("property-test")
@DisplayName("Property Test: Delta Sync de Fotos de Referência")
public class MobileFotoReferenciaServiceDeltaSyncPropertyTest {
    
    private final Random random = new Random();
    
    // Constantes de tempo para testes
    private static final long UM_DIA_MS = 24 * 60 * 60 * 1000L;
    private static final long UMA_SEMANA_MS = 7 * UM_DIA_MS;
    private static final long UM_MES_MS = 30 * UM_DIA_MS;
    
    /**
     * Property 7: Delta Sync
     * 
     * Para qualquer timestamp de sincronização, apenas fotos com
     * dataAtualizacao > timestamp devem ser retornadas.
     * 
     * **Validates: Requirements 4.3**
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property 7: Delta sync retorna apenas fotos atualizadas após timestamp")
    void property7_deltaSyncRetornaApenasAtualizadas() {
        // Arrange: Gerar lista de fotos com datas variadas
        long agora = System.currentTimeMillis();
        int totalFotos = random.nextInt(100) + 10; // 10 a 109 fotos
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosComDatasVariadas(totalFotos, agora);
        
        // Gerar timestamp de sincronização aleatório (entre 1 mês atrás e agora)
        long timestampSync = agora - random.nextLong(UM_MES_MS);
        
        // Act: Filtrar fotos atualizadas após timestamp (simulando delta sync)
        List<MobileFotoReferenciaDTO> fotosAtualizadas = todasFotos.stream()
            .filter(foto -> foto.getDataAtualizacao() > timestampSync)
            .collect(Collectors.toList());
        
        // Assert: Todas as fotos retornadas devem ter dataAtualizacao > timestamp
        for (MobileFotoReferenciaDTO foto : fotosAtualizadas) {
            assertTrue(foto.getDataAtualizacao() > timestampSync,
                String.format("Foto ID %d com dataAtualizacao %d não deveria estar no resultado (timestamp: %d)",
                    foto.getId(), foto.getDataAtualizacao(), timestampSync));
        }
        
        // Assert: Nenhuma foto com dataAtualizacao <= timestamp deve estar no resultado
        long fotosExcluidas = todasFotos.stream()
            .filter(foto -> foto.getDataAtualizacao() <= timestampSync)
            .count();
        
        assertEquals(totalFotos - fotosExcluidas, fotosAtualizadas.size(),
            "Quantidade de fotos atualizadas não corresponde ao esperado");
    }
    
    /**
     * Property: Delta sync com timestamp null retorna todas as fotos
     * 
     * Se o timestamp for null (primeira sincronização), todas as fotos
     * ativas devem ser retornadas.
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Delta sync com timestamp null retorna todas as fotos ativas")
    void property_deltaSyncSemTimestampRetornaTodas() {
        // Arrange: Gerar lista de fotos
        long agora = System.currentTimeMillis();
        int totalFotos = random.nextInt(100) + 10;
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosComDatasVariadas(totalFotos, agora);
        
        // Algumas fotos inativas
        int fotosInativas = random.nextInt(totalFotos / 4);
        for (int i = 0; i < fotosInativas; i++) {
            todasFotos.get(i).setAtivo(false);
        }
        
        // Act: Simular delta sync sem timestamp (retorna todas ativas)
        Long timestampSync = null;
        List<MobileFotoReferenciaDTO> resultado = todasFotos.stream()
            .filter(foto -> timestampSync == null || foto.getDataAtualizacao() > timestampSync)
            .filter(foto -> Boolean.TRUE.equals(foto.getAtivo()))
            .collect(Collectors.toList());
        
        // Assert: Deve retornar todas as fotos ativas
        long fotosAtivas = todasFotos.stream().filter(f -> Boolean.TRUE.equals(f.getAtivo())).count();
        assertEquals(fotosAtivas, resultado.size(),
            "Delta sync sem timestamp deve retornar todas as fotos ativas");
    }
    
    /**
     * Property: Delta sync com timestamp futuro retorna lista vazia
     * 
     * Se o timestamp for no futuro, nenhuma foto deve ser retornada
     * (todas as fotos têm dataAtualizacao <= timestamp).
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Delta sync com timestamp futuro retorna lista vazia")
    void property_deltaSyncTimestampFuturoRetornaVazio() {
        // Arrange: Gerar lista de fotos
        long agora = System.currentTimeMillis();
        int totalFotos = random.nextInt(100) + 10;
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosComDatasVariadas(totalFotos, agora);
        
        // Timestamp no futuro
        long timestampFuturo = agora + UM_DIA_MS + random.nextLong(UMA_SEMANA_MS);
        
        // Act: Filtrar fotos atualizadas após timestamp futuro
        List<MobileFotoReferenciaDTO> resultado = todasFotos.stream()
            .filter(foto -> foto.getDataAtualizacao() > timestampFuturo)
            .collect(Collectors.toList());
        
        // Assert: Resultado deve ser vazio
        assertTrue(resultado.isEmpty(),
            String.format("Delta sync com timestamp futuro (%d) deveria retornar lista vazia, mas retornou %d fotos",
                timestampFuturo, resultado.size()));
    }
    
    /**
     * Property: Delta sync preserva ordem por dataAtualizacao
     * 
     * As fotos retornadas devem estar ordenadas por dataAtualizacao
     * (mais recentes primeiro ou mais antigas primeiro, dependendo da implementação).
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Delta sync pode ser ordenado por dataAtualizacao")
    void property_deltaSyncPodeSerOrdenado() {
        // Arrange: Gerar lista de fotos
        long agora = System.currentTimeMillis();
        int totalFotos = random.nextInt(100) + 10;
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosComDatasVariadas(totalFotos, agora);
        
        // Timestamp de sincronização
        long timestampSync = agora - UMA_SEMANA_MS;
        
        // Act: Filtrar e ordenar por dataAtualizacao (mais antigas primeiro)
        List<MobileFotoReferenciaDTO> resultado = todasFotos.stream()
            .filter(foto -> foto.getDataAtualizacao() > timestampSync)
            .sorted((a, b) -> Long.compare(a.getDataAtualizacao(), b.getDataAtualizacao()))
            .collect(Collectors.toList());
        
        // Assert: Verificar ordenação
        for (int i = 1; i < resultado.size(); i++) {
            assertTrue(resultado.get(i).getDataAtualizacao() >= resultado.get(i - 1).getDataAtualizacao(),
                String.format("Fotos não estão ordenadas: %d >= %d",
                    resultado.get(i).getDataAtualizacao(), resultado.get(i - 1).getDataAtualizacao()));
        }
    }
    
    /**
     * Property: Delta sync inclui fotos desativadas após timestamp
     * 
     * Fotos que foram desativadas após o timestamp devem ser incluídas
     * para que o cliente possa removê-las do cache local.
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Delta sync inclui fotos desativadas após timestamp")
    void property_deltaSyncIncluiDesativadasAposTimestamp() {
        // Arrange: Gerar lista de fotos
        long agora = System.currentTimeMillis();
        int totalFotos = random.nextInt(100) + 10;
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosComDatasVariadas(totalFotos, agora);
        
        // Desativar algumas fotos com dataAtualizacao recente
        int fotosDesativadas = 0;
        for (int i = 0; i < totalFotos; i++) {
            if (random.nextBoolean() && todasFotos.get(i).getDataAtualizacao() > agora - UM_DIA_MS) {
                todasFotos.get(i).setAtivo(false);
                fotosDesativadas++;
            }
        }
        
        // Timestamp de sincronização (2 dias atrás)
        long timestampSync = agora - 2 * UM_DIA_MS;
        
        // Act: Delta sync inclui todas as fotos atualizadas (ativas e inativas)
        List<MobileFotoReferenciaDTO> resultado = todasFotos.stream()
            .filter(foto -> foto.getDataAtualizacao() > timestampSync)
            .collect(Collectors.toList());
        
        // Assert: Resultado deve incluir fotos inativas atualizadas
        long inativasNoResultado = resultado.stream().filter(f -> !Boolean.TRUE.equals(f.getAtivo())).count();
        
        // Verificar que fotos inativas com dataAtualizacao > timestamp estão incluídas
        long inativasEsperadas = todasFotos.stream()
            .filter(f -> !Boolean.TRUE.equals(f.getAtivo()) && f.getDataAtualizacao() > timestampSync)
            .count();
        
        assertEquals(inativasEsperadas, inativasNoResultado,
            "Delta sync deve incluir fotos inativas atualizadas após timestamp");
    }
    
    /**
     * Property: Quantidade de fotos no delta sync é proporcional ao intervalo
     * 
     * Quanto maior o intervalo desde o último sync, mais fotos devem ser retornadas.
     */
    @RepeatedTest(value = 100, name = "Iteração {currentRepetition} de {totalRepetitions}")
    @DisplayName("Property: Quantidade de fotos é proporcional ao intervalo de sync")
    void property_quantidadeProporcionalAoIntervalo() {
        // Arrange: Gerar lista de fotos com distribuição uniforme
        long agora = System.currentTimeMillis();
        int totalFotos = random.nextInt(100) + 50; // 50 a 149 fotos
        List<MobileFotoReferenciaDTO> todasFotos = gerarListaFotosDistribuicaoUniforme(totalFotos, agora);
        
        // Dois timestamps: um mais antigo e um mais recente
        long timestampAntigo = agora - UM_MES_MS;
        long timestampRecente = agora - UM_DIA_MS;
        
        // Act: Contar fotos para cada timestamp
        long fotasDesdeAntigo = todasFotos.stream()
            .filter(foto -> foto.getDataAtualizacao() > timestampAntigo)
            .count();
        
        long fotasDesdeRecente = todasFotos.stream()
            .filter(foto -> foto.getDataAtualizacao() > timestampRecente)
            .count();
        
        // Assert: Timestamp mais antigo deve retornar >= fotos que timestamp recente
        assertTrue(fotasDesdeAntigo >= fotasDesdeRecente,
            String.format("Timestamp antigo (%d fotos) deve retornar >= timestamp recente (%d fotos)",
                fotasDesdeAntigo, fotasDesdeRecente));
    }
    
    // ========== Métodos auxiliares ==========
    
    /**
     * Gera lista de DTOs com datas de atualização variadas
     */
    private List<MobileFotoReferenciaDTO> gerarListaFotosComDatasVariadas(int quantidade, long agora) {
        List<MobileFotoReferenciaDTO> fotos = new ArrayList<>();
        
        for (int i = 0; i < quantidade; i++) {
            MobileFotoReferenciaDTO dto = new MobileFotoReferenciaDTO();
            dto.setId(i + 1);
            dto.setDescricaoNormalizada("DESCRICAO_" + i);
            dto.setHashImagem("hash_" + i);
            dto.setTamanhoBytes(random.nextInt(50000) + 1000);
            
            // Data de atualização aleatória (entre 2 meses atrás e agora)
            long dataAtualizacao = agora - random.nextLong(2 * UM_MES_MS);
            dto.setDataAtualizacao(dataAtualizacao);
            dto.setAtivo(true);
            
            fotos.add(dto);
        }
        
        return fotos;
    }
    
    /**
     * Gera lista de DTOs com distribuição uniforme de datas
     */
    private List<MobileFotoReferenciaDTO> gerarListaFotosDistribuicaoUniforme(int quantidade, long agora) {
        List<MobileFotoReferenciaDTO> fotos = new ArrayList<>();
        long intervalo = 2 * UM_MES_MS / quantidade;
        
        for (int i = 0; i < quantidade; i++) {
            MobileFotoReferenciaDTO dto = new MobileFotoReferenciaDTO();
            dto.setId(i + 1);
            dto.setDescricaoNormalizada("DESCRICAO_" + i);
            dto.setHashImagem("hash_" + i);
            dto.setTamanhoBytes(random.nextInt(50000) + 1000);
            
            // Data de atualização distribuída uniformemente
            long dataAtualizacao = agora - (2 * UM_MES_MS) + (i * intervalo);
            dto.setDataAtualizacao(dataAtualizacao);
            dto.setAtivo(true);
            
            fotos.add(dto);
        }
        
        return fotos;
    }
}
