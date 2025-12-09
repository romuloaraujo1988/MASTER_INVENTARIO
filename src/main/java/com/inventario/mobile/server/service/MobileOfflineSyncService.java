package com.inventario.mobile.server.service;

import com.inventario.dao.InventarioDAO;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.ResponsavelDAO;
import com.inventario.dao.SalaDAO;
import com.inventario.mobile.server.dto.MobileOfflineDataDTO;
import com.inventario.mobile.server.dto.MobileOfflineDataDTO.*;
import com.inventario.model.Inventario;
import com.inventario.model.Patrimonio;
import com.inventario.model.Responsavel;
import com.inventario.model.Sala;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para sincronização offline completa
 * 
 * CRÍTICO: Endpoint otimizado para baixar TODOS os dados necessários
 * para o app funcionar offline com 10.000+ patrimônios
 * 
 * v2.3: Adicionado controle de memória e paginação
 */
@Service
public class MobileOfflineSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileOfflineSyncService.class);
    private static final String VERSAO_SERVIDOR = "2.4.0";  // Atualizado: limites aumentados
    
    // Limites para evitar sobrecarga de memória
    // AUMENTADO: Com servidor em processo separado (1GB dedicado), podemos carregar mais dados
    // v2.4: Removida limitação artificial - servidor standalone suporta carga maior
    private static final int MAX_PATRIMONIOS_POR_SYNC = 50000;  // Aumentado de 5000 para 50000
    private static final int MAX_SALAS_POR_SYNC = 2000;         // Aumentado de 500 para 2000
    private static final int MAX_RESPONSAVEIS_POR_SYNC = 1000;  // Aumentado de 300 para 1000
    
    @Autowired
    private PatrimonioDAO patrimonioDAO;
    
    @Autowired
    private SalaDAO salaDAO;
    
    @Autowired
    private ResponsavelDAO responsavelDAO;
    
    @Autowired
    private InventarioDAO inventarioDAO;
    
    /**
     * Busca TODOS os dados necessários para modo offline
     * 
     * OTIMIZAÇÕES:
     * - Query única por entidade (não múltiplas queries)
     * - DTOs simplificados (apenas campos essenciais)
     * - Transação read-only (mais rápido)
     * - Logs detalhados para debug
     * 
     * @param inventarioId ID do inventário ativo (opcional)
     * @return Todos os dados para offline
     */
    @Transactional(readOnly = true)
    public MobileOfflineDataDTO buscarDadosOffline(Integer inventarioId) {
        // Log de memória ANTES
        Runtime runtime = Runtime.getRuntime();
        long memBefore = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        logger.info("📊 Memória ANTES do sync offline: {}MB", memBefore);
        
        logger.debug("Iniciando sincronização offline");
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Buscar inventário ativo
            Inventario inventarioAtivo = null;
            if (inventarioId != null) {
                inventarioAtivo = inventarioDAO.findById(inventarioId);
            }
            if (inventarioAtivo == null) {
                inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
            }
            
            if (inventarioAtivo != null) {
                logger.debug("Inventário ativo: {} (ID: {})", 
                    inventarioAtivo.getNome(), inventarioAtivo.getId());
            } else {
                logger.warn("Nenhum inventário ativo encontrado");
            }
            
            // 2. Buscar patrimônios (com limite para evitar sobrecarga)
            logger.debug("Buscando patrimônios...");
            long patrimoniosStart = System.currentTimeMillis();
            
            List<Patrimonio> patrimonios = patrimonioDAO.listarTodosComJoins();
            
            // Aplicar limite para evitar sobrecarga de memória
            if (patrimonios.size() > MAX_PATRIMONIOS_POR_SYNC) {
                logger.warn("Limite de patrimônios atingido: {} > {}. Truncando lista.", 
                    patrimonios.size(), MAX_PATRIMONIOS_POR_SYNC);
                patrimonios = patrimonios.subList(0, MAX_PATRIMONIOS_POR_SYNC);
            }
            
            List<PatrimonioOfflineDTO> patrimoniosDTO = patrimonios.stream()
                .map(this::converterPatrimonioParaDTO)
                .collect(Collectors.toList());
            
            // Liberar memória da lista original
            patrimonios = null;
            
            long patrimoniosTime = System.currentTimeMillis() - patrimoniosStart;
            logger.debug("{} patrimônios carregados em {}ms", 
                patrimoniosDTO.size(), patrimoniosTime);
            
            // 3. Buscar salas (com limite)
            logger.debug("Buscando salas...");
            long salasStart = System.currentTimeMillis();
            
            List<Sala> salas = salaDAO.findAll();
            
            // Aplicar limite
            if (salas.size() > MAX_SALAS_POR_SYNC) {
                logger.warn("Limite de salas atingido: {} > {}. Truncando lista.", 
                    salas.size(), MAX_SALAS_POR_SYNC);
                salas = salas.subList(0, MAX_SALAS_POR_SYNC);
            }
            
            List<SalaOfflineDTO> salasDTO = salas.stream()
                .map(this::converterSalaParaDTO)
                .collect(Collectors.toList());
            
            // Liberar memória
            salas = null;
            
            long salasTime = System.currentTimeMillis() - salasStart;
            logger.debug("{} salas carregadas em {}ms", 
                salasDTO.size(), salasTime);
            
            // 4. Buscar responsáveis (com limite)
            logger.debug("Buscando responsáveis...");
            long responsaveisStart = System.currentTimeMillis();
            
            List<Responsavel> responsaveis = responsavelDAO.findAll();
            
            // Aplicar limite
            if (responsaveis.size() > MAX_RESPONSAVEIS_POR_SYNC) {
                logger.warn("Limite de responsáveis atingido: {} > {}. Truncando lista.", 
                    responsaveis.size(), MAX_RESPONSAVEIS_POR_SYNC);
                responsaveis = responsaveis.subList(0, MAX_RESPONSAVEIS_POR_SYNC);
            }
            
            List<ResponsavelOfflineDTO> responsaveisDTO = responsaveis.stream()
                .map(this::converterResponsavelParaDTO)
                .collect(Collectors.toList());
            
            // Liberar memória
            responsaveis = null;
            
            long responsaveisTime = System.currentTimeMillis() - responsaveisStart;
            logger.debug("{} responsáveis carregados em {}ms", 
                responsaveisDTO.size(), responsaveisTime);
            
            // 5. Criar metadados
            MetadataDTO metadata = new MetadataDTO();
            metadata.setTimestamp(System.currentTimeMillis());
            metadata.setTotalPatrimonios(patrimoniosDTO.size());
            metadata.setTotalSalas(salasDTO.size());
            metadata.setTotalResponsaveis(responsaveisDTO.size());
            metadata.setVersaoServidor(VERSAO_SERVIDOR);
            
            if (inventarioAtivo != null) {
                metadata.setInventarioAtivoId(inventarioAtivo.getId());
                metadata.setInventarioAtivoNome(inventarioAtivo.getNome());
            }
            
            // 6. Montar resposta
            MobileOfflineDataDTO response = new MobileOfflineDataDTO(
                patrimoniosDTO,
                salasDTO,
                responsaveisDTO,
                metadata
            );
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            // Log de memória DEPOIS
            long memAfter = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            logger.info("📊 Memória DEPOIS do sync offline: {}MB (delta: +{}MB)", memAfter, memAfter - memBefore);
            
            logger.info("✅ Sincronização offline concluída: {} patrimônios, {} salas, {} responsáveis em {}ms", 
                patrimoniosDTO.size(), salasDTO.size(), responsaveisDTO.size(), totalTime);
            
            return response;
            
        } catch (SQLException e) {
            logger.error("Erro de banco de dados na sincronização offline: {}", e.getMessage(), e);
            throw new RuntimeException("Erro de banco de dados ao buscar dados offline: " + e.getMessage(), e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.error("Erro de validação na sincronização offline: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar dados offline: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            logger.error("Erro na sincronização offline: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Converte Patrimonio para DTO simplificado
     */
    private PatrimonioOfflineDTO converterPatrimonioParaDTO(Patrimonio patrimonio) {
        PatrimonioOfflineDTO dto = new PatrimonioOfflineDTO();
        dto.setId((long) patrimonio.getId());  // Converter int para Long
        dto.setNumeroPatrimonio(patrimonio.getNumeroPatrimonio());
        dto.setDescricao(patrimonio.getDescricao());
        dto.setMarca(patrimonio.getMarca());
        dto.setModelo(patrimonio.getModelo());
        dto.setEstado(patrimonio.getEstadoConservacao());  // Método correto
        
        // Sala (usar IDs e nomes transientes)
        if (patrimonio.getIdSala() > 0) {
            dto.setSalaId(patrimonio.getIdSala());
            dto.setSalaNome(patrimonio.getNomeSala());
        }
        
        // Responsável (usar IDs e nomes transientes)
        if (patrimonio.getIdResponsavel() > 0) {
            dto.setResponsavelId(patrimonio.getIdResponsavel());
            dto.setResponsavelNome(patrimonio.getNomeResponsavel());
        }
        
        // Coletado (não existe no modelo, sempre false)
        dto.setColetado(false);
        
        return dto;
    }
    
    /**
     * Converte Sala para DTO simplificado
     */
    private SalaOfflineDTO converterSalaParaDTO(Sala sala) {
        SalaOfflineDTO dto = new SalaOfflineDTO();
        dto.setId(sala.getId());
        dto.setNome(sala.getDescricao());  // Sala usa getDescricao(), não getNome()
        Boolean ativo = sala.getAtivo();
        dto.setAtiva(Boolean.TRUE.equals(ativo));  // getAtivo(), não getAtiva() - default false se null
        return dto;
    }
    
    /**
     * Converte Responsavel para DTO simplificado
     */
    private ResponsavelOfflineDTO converterResponsavelParaDTO(Responsavel responsavel) {
        ResponsavelOfflineDTO dto = new ResponsavelOfflineDTO();
        dto.setId(responsavel.getId());
        dto.setNome(responsavel.getNome());
        dto.setCpf(responsavel.getCpf());
        return dto;
    }
    
    /**
     * Estima tamanho da resposta em KB (aproximado)
     */
    private int estimarTamanhoResposta(MobileOfflineDataDTO response) {
        // Estimativa: ~200 bytes por patrimônio, ~50 por sala, ~50 por responsável
        int patrimoniosSize = response.getPatrimonios().size() * 200;
        int salasSize = response.getSalas().size() * 50;
        int responsaveisSize = response.getResponsaveis().size() * 50;
        
        return (patrimoniosSize + salasSize + responsaveisSize) / 1024;
    }
    
    /**
     * Busca patrimônios PAGINADOS para modo offline
     * 
     * USO: Quando há mais de 5.000 patrimônios, o app deve fazer múltiplas
     * requisições para baixar todos os dados em partes.
     * 
     * @param page número da página (0-based)
     * @param size tamanho da página (máximo 2000)
     * @param inventarioId ID do inventário (opcional)
     * @return Map com content, totalElements, totalPages, page, size
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> buscarPatrimoniosPaginados(int page, int size, Integer inventarioId) {
        // Log de memória
        Runtime runtime = Runtime.getRuntime();
        long memBefore = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        logger.debug("📊 Memória ANTES: {}MB", memBefore);
        
        try {
            // Limitar tamanho máximo
            if (size > 2000) {
                size = 2000;
            }
            
            // Contar total de patrimônios
            int totalElements = patrimonioDAO.contarTotalPatrimonios();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            // Buscar página de patrimônios
            List<Patrimonio> patrimonios = patrimonioDAO.buscarPatrimoniosComPaginacao(page, size);
            
            // Converter para DTOs
            List<PatrimonioOfflineDTO> patrimoniosDTO = patrimonios.stream()
                .map(this::converterPatrimonioParaDTO)
                .collect(Collectors.toList());
            
            // Liberar memória
            patrimonios = null;
            
            // Log de memória
            long memAfter = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            logger.debug("📊 Memória DEPOIS: {}MB (delta: +{}MB)", memAfter, memAfter - memBefore);
            
            // Montar resposta
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("content", patrimoniosDTO);
            result.put("page", page);
            result.put("size", size);
            result.put("totalElements", totalElements);
            result.put("totalPages", totalPages);
            result.put("first", page == 0);
            result.put("last", page >= totalPages - 1);
            result.put("hasMore", page < totalPages - 1);
            
            return result;
            
        } catch (SQLException e) {
            logger.error("Erro de banco de dados ao buscar patrimônios paginados: {}", e.getMessage(), e);
            throw new RuntimeException("Erro de banco de dados ao buscar patrimônios: " + e.getMessage(), e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.error("Erro de validação ao buscar patrimônios paginados: {}", e.getMessage(), e);
            throw new RuntimeException("Erro de validação ao buscar patrimônios: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            logger.error("Erro ao buscar patrimônios paginados: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar patrimônios: " + e.getMessage(), e);
        }
    }
}
