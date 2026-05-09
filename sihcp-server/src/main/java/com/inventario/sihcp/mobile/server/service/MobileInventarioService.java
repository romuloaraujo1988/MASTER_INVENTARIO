package com.inventario.sihcp.mobile.server.service;

import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.mobile.server.dto.MobileInventarioDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para operações de inventário mobile
 */
@Service
public class MobileInventarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileInventarioService.class);
    
    private final InventarioDAO inventarioDAO;
    private final PatrimonioDAO patrimonioDAO;
    private final ColetaDAO coletaDAO;
    
    public MobileInventarioService() {
        this.inventarioDAO = new InventarioDAO();
        this.patrimonioDAO = new PatrimonioDAO();
        this.coletaDAO = new ColetaDAO();
    }
    
    /**
     * Busca o inventário ativo (em andamento)
     */
    public MobileInventarioDTO buscarInventarioAtivo() throws SQLException {
        logger.debug("Buscando inventário ativo");
        
        Inventario inventario = inventarioDAO.buscarInventarioAtivo();
        
        if (inventario == null) {
            logger.debug("Nenhum inventário ativo encontrado");
            return null;
        }
        
        return converterParaDTO(inventario);
    }
    
    /**
     * Busca inventário por ID
     */
    public MobileInventarioDTO buscarPorId(Integer id) throws SQLException {
        logger.debug("Buscando inventário por ID: {}", id);
        
        Inventario inventario = inventarioDAO.findById(id);
        
        if (inventario == null) {
            return null;
        }
        
        return converterParaDTO(inventario);
    }
    
    /**
     * Lista todos os inventários
     */
    public List<MobileInventarioDTO> listarInventarios() throws SQLException {
        logger.debug("Listando inventários");
        
        List<Inventario> inventarios = inventarioDAO.findAll();
        List<MobileInventarioDTO> dtos = new ArrayList<>();
        
        for (Inventario inventario : inventarios) {
            dtos.add(converterParaDTO(inventario));
        }
        
        logger.debug("Encontrados {} inventários", dtos.size());
        
        return dtos;
    }
    
    /**
     * Busca estatísticas do inventário
     */
    public Map<String, Object> buscarEstatisticas(Integer idInventario) throws SQLException {
        logger.debug("Buscando estatísticas do inventário: {}", idInventario);
        
        Inventario inventario = inventarioDAO.findById(idInventario);
        
        if (inventario == null) {
            throw new IllegalArgumentException("Inventário não encontrado");
        }
        
        // Contar patrimônios totais (OTIMIZADO: usa COUNT ao invés de carregar todos)
        int totalPatrimonios = patrimonioDAO.contarPatrimoniosAtivos();
        
        // Contar coletas realizadas
        int totalColetados = coletaDAO.contarColetasPorInventario(idInventario);
        
        // Calcular pendentes
        int totalPendentes = totalPatrimonios - totalColetados;
        
        // Calcular percentual
        double percentualConclusao = totalPatrimonios > 0 
                ? (totalColetados * 100.0) / totalPatrimonios 
                : 0.0;
        
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("idInventario", idInventario);
        estatisticas.put("nomeInventario", inventario.getNome());
        estatisticas.put("status", inventario.getStatusInventario());
        estatisticas.put("totalPatrimonios", totalPatrimonios);
        estatisticas.put("totalColetados", totalColetados);
        estatisticas.put("totalPendentes", totalPendentes);
        estatisticas.put("percentualConclusao", Math.round(percentualConclusao * 100.0) / 100.0);
        estatisticas.put("dataInicio", inventario.getDataInicio());
        estatisticas.put("dataFim", inventario.getDataFim());
        
        logger.debug("Estatísticas: {}% concluído ({}/{})", 
                Math.round(percentualConclusao), totalColetados, totalPatrimonios);
        
        return estatisticas;
    }
    
    /**
     * Converte Inventario para DTO
     */
    private MobileInventarioDTO converterParaDTO(Inventario inventario) throws SQLException {
        MobileInventarioDTO dto = new MobileInventarioDTO();
        
        dto.setId(inventario.getId());
        dto.setNome(inventario.getNome());
        dto.setDescricao(null); // Inventario não tem campo descrição
        dto.setStatus(inventario.getStatusInventario());
        dto.setDataInicio(inventario.getDataInicio());
        dto.setDataFim(inventario.getDataFim());
        // Converter LocalDateTime para Date se necessário
        if (inventario.getDataCriacao() != null) {
            dto.setDataCriacao(java.sql.Timestamp.valueOf(inventario.getDataCriacao()));
        }
        
        // Adicionar estatísticas básicas
        try {
            int totalColetados = coletaDAO.contarColetasPorInventario(inventario.getId());
            dto.setTotalColetados(totalColetados);
            
            int totalPatrimonios = patrimonioDAO.contarPatrimoniosAtivos();
            dto.setTotalPatrimonios(totalPatrimonios);
            
            double percentual = totalPatrimonios > 0 
                    ? (totalColetados * 100.0) / totalPatrimonios 
                    : 0.0;
            dto.setPercentualConclusao(Math.round(percentual * 100.0) / 100.0);
        } catch (Exception e) {
            logger.warn("Erro ao calcular estatísticas do inventário {}: {}", 
                    inventario.getId(), e.getMessage());
        }
        
        return dto;
    }
}
