package com.inventario.service;

import com.inventario.dao.DashboardColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.model.Inventario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para operações de dashboard e estatísticas
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    private final DashboardColetaDAO dashboardDAO;
    private final InventarioDAO inventarioDAO;
    
    public DashboardService() {
        this.dashboardDAO = new DashboardColetaDAO();
        this.inventarioDAO = new InventarioDAO();
    }
    
    public DashboardService(DashboardColetaDAO dashboardDAO, InventarioDAO inventarioDAO) {
        this.dashboardDAO = dashboardDAO;
        this.inventarioDAO = inventarioDAO;
    }
    
    /**
     * Obtém inventário ativo
     */
    public Inventario obterInventarioAtivo() {
        try {
            return inventarioDAO.buscarInventarioPorStatus("EM_ANDAMENTO");
        } catch (Exception e) {
            logger.error("Erro ao buscar inventário ativo", e);
            return null;
        }
    }
    
    /**
     * Obtém estatísticas gerais do inventário
     */
    public Map<String, Object> obterEstatisticasGerais(int idInventario) {
        try {
            Map<String, Integer> stats = dashboardDAO.buscarEstatisticasColeta(idInventario);
            // Converter Map<String, Integer> para Map<String, Object>
            return new HashMap<>(stats);
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas gerais do inventário: {}", idInventario, e);
            return new HashMap<>();
        }
    }
    
    /**
     * Obtém progresso por setor
     */
    public List<Map<String, Object>> obterProgressoPorSetor(int idInventario) {
        try {
            Map<String, Map<String, Integer>> progressoMap = dashboardDAO.buscarProgressoPorSetor(idInventario);
            List<Map<String, Object>> resultado = new ArrayList<>();
            
            for (Map.Entry<String, Map<String, Integer>> entry : progressoMap.entrySet()) {
                Map<String, Object> setor = new HashMap<>();
                setor.put("setor", entry.getKey());
                setor.putAll(entry.getValue());
                resultado.add(setor);
            }
            
            return resultado;
        } catch (Exception e) {
            logger.error("Erro ao obter progresso por setor do inventário: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtém itens coletados recentemente
     * Nota: Método não implementado no DAO atual
     */
    public List<Map<String, Object>> obterItensColetadosRecentes(int idInventario, int limite) {
        logger.warn("Método obterItensColetadosRecentes não implementado no DAO");
        return new ArrayList<>();
    }
    
    /**
     * Obtém divergências encontradas
     * Nota: Método não implementado no DAO atual
     */
    public List<Map<String, Object>> obterDivergencias(int idInventario) {
        logger.warn("Método obterDivergencias não implementado no DAO");
        return new ArrayList<>();
    }
    
    /**
     * Obtém total de patrimônios
     */
    public int obterTotalPatrimonios(int idInventario) {
        try {
            Map<String, Integer> stats = dashboardDAO.buscarEstatisticasColeta(idInventario);
            return stats.getOrDefault("total", 0);
        } catch (Exception e) {
            logger.error("Erro ao obter total de patrimônios do inventário: {}", idInventario, e);
            return 0;
        }
    }
    
    /**
     * Obtém total de patrimônios coletados
     */
    public int obterTotalColetados(int idInventario) {
        try {
            Map<String, Integer> stats = dashboardDAO.buscarEstatisticasColeta(idInventario);
            return stats.getOrDefault("coletados", 0);
        } catch (Exception e) {
            logger.error("Erro ao obter total coletados do inventário: {}", idInventario, e);
            return 0;
        }
    }
    
    /**
     * Obtém percentual de conclusão
     */
    public double obterPercentualConclusao(int idInventario) {
        try {
            int total = obterTotalPatrimonios(idInventario);
            if (total == 0) return 0.0;
            
            int coletados = obterTotalColetados(idInventario);
            return (coletados * 100.0) / total;
        } catch (Exception e) {
            logger.error("Erro ao calcular percentual de conclusão do inventário: {}", idInventario, e);
            return 0.0;
        }
    }
    
    /**
     * Obtém estatísticas por sala
     * Nota: Método não implementado no DAO atual
     */
    public List<Map<String, Object>> obterEstatisticasPorSala(int idInventario) {
        logger.warn("Método obterEstatisticasPorSala não implementado no DAO");
        return new ArrayList<>();
    }
    
    /**
     * Obtém estatísticas por responsável
     */
    public List<Map<String, Object>> obterEstatisticasPorResponsavel(int idInventario) {
        try {
            Map<String, Map<String, Integer>> statsMap = dashboardDAO.buscarEstatisticasPorResponsavel(idInventario);
            List<Map<String, Object>> resultado = new ArrayList<>();
            
            for (Map.Entry<String, Map<String, Integer>> entry : statsMap.entrySet()) {
                Map<String, Object> responsavel = new HashMap<>();
                responsavel.put("responsavel", entry.getKey());
                responsavel.putAll(entry.getValue());
                resultado.add(responsavel);
            }
            
            return resultado;
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas por responsável do inventário: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtém estatísticas por coletor
     */
    public Map<String, Integer> obterEstatisticasColetores(int idInventario) {
        try {
            return dashboardDAO.obterEstatisticasColetores(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas de coletores do inventário: {}", idInventario, e);
            return new HashMap<>();
        }
    }
    
    /**
     * Obtém desempenho detalhado dos coletores por período
     */
    public Map<String, Map<String, Integer>> obterDesempenhoColetoresPorPeriodo(int idInventario) {
        try {
            return dashboardDAO.obterDesempenhoColetoresPorPeriodo(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao obter desempenho de coletores do inventário: {}", idInventario, e);
            return new HashMap<>();
        }
    }
}
