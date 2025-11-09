package com.inventario.mobile.server.service;

import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.ColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.model.Inventario;
import com.inventario.model.Coleta;
import com.inventario.mobile.server.dto.DashboardStatsDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Serviço para estatísticas do dashboard mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service("mobileDashboardService")
public class MobileDashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileDashboardService.class);
    
    private final PatrimonioDAO patrimonioDAO;
    private final ColetaDAO coletaDAO;
    private final InventarioDAO inventarioDAO;
    
    public MobileDashboardService() {
        this.patrimonioDAO = new PatrimonioDAO();
        this.coletaDAO = new ColetaDAO();
        this.inventarioDAO = new InventarioDAO();
    }
    
    /**
     * Busca estatísticas do dashboard para o inventário ativo
     */
    public DashboardStatsDTO getDashboardStats() throws SQLException {
        logger.info("Buscando estatísticas do dashboard");
        
        try {
            // Buscar inventário ativo
            Inventario inventarioAtivo = inventarioDAO.buscarInventarioPorStatus("EM_ANDAMENTO");
            
            if (inventarioAtivo == null) {
                logger.warn("Nenhum inventário ativo encontrado");
                return new DashboardStatsDTO(0, 0, 0, 0.0, 0);
            }
            
            return getDashboardStatsByInventario(inventarioAtivo.getId());
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar estatísticas do dashboard", e);
            throw e;
        }
    }
    
    /**
     * Busca estatísticas do dashboard para um inventário específico
     */
    public DashboardStatsDTO getDashboardStatsByInventario(Integer inventarioId) throws SQLException {
        logger.info("Buscando estatísticas do dashboard para inventário: {}", inventarioId);
        
        try {
            // Contar total de patrimônios
            int totalPatrimonios = patrimonioDAO.contarPatrimoniosAtivos();
            logger.debug("Total de patrimônios ativos: {}", totalPatrimonios);
            
            // Buscar coletas do inventário
            List<Coleta> coletas = coletaDAO.buscarPorInventario(inventarioId);
            logger.debug("Total de coletas encontradas: {}", coletas.size());
            
            // Contar patrimônios coletados (únicos)
            Set<Integer> patrimoniosColetados = new HashSet<>();
            int divergencias = 0;
            Set<Integer> coletoresAtivos = new HashSet<>();
            
            for (Coleta coleta : coletas) {
                patrimoniosColetados.add(coleta.getIdPatrimonio());
                
                if (coleta.isDivergencia()) {
                    divergencias++;
                }
                
                if (coleta.getIdColetor() > 0) {
                    coletoresAtivos.add(coleta.getIdColetor());
                }
            }
            
            int totalColetados = patrimoniosColetados.size();
            logger.debug("Patrimônios coletados (únicos): {}", totalColetados);
            logger.debug("Divergências: {}", divergencias);
            logger.debug("Coletores ativos: {}", coletoresAtivos.size());
            
            // Calcular valor total (simplificado - pode ser otimizado com query SQL)
            double valorTotal = patrimonioDAO.calcularValorTotal();
            logger.debug("Valor total: {}", valorTotal);
            
            DashboardStatsDTO stats = new DashboardStatsDTO(
                totalPatrimonios,
                totalColetados,
                divergencias,
                valorTotal,
                coletoresAtivos.size()
            );
            
            logger.info("Estatísticas calculadas com sucesso");
            return stats;
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar estatísticas do dashboard para inventário {}", inventarioId, e);
            throw e;
        }
    }
}
