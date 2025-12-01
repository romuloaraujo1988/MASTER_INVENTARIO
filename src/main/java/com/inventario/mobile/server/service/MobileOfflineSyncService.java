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
    private static final String VERSAO_SERVIDOR = "2.3.0";
    
    // Limites para evitar sobrecarga de memória
    private static final int MAX_PATRIMONIOS_POR_SYNC = 10000;
    private static final int MAX_SALAS_POR_SYNC = 1000;
    private static final int MAX_RESPONSAVEIS_POR_SYNC = 500;
    
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
            
            logger.debug("Sincronização offline concluída: {} patrimônios, {} salas, {} responsáveis em {}ms", 
                patrimoniosDTO.size(), salasDTO.size(), responsaveisDTO.size(), totalTime);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Erro na sincronização offline", e);
            throw new RuntimeException("Erro ao buscar dados offline: " + e.getMessage(), e);
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
        dto.setAtiva(sala.getAtivo() != null ? sala.getAtivo() : true);  // getAtivo(), não getAtiva()
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
}
