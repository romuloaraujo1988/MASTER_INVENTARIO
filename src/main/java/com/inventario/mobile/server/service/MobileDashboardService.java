package com.inventario.mobile.server.service;

import com.inventario.dao.ColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.model.Inventario;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Serviço para estatísticas e dashboard mobile
 */
@Service
public class MobileDashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileDashboardService.class);
    
    private final ColetaDAO coletaDAO;
    private final PatrimonioDAO patrimonioDAO;
    private final InventarioDAO inventarioDAO;
    
    public MobileDashboardService() {
        this.coletaDAO = new ColetaDAO();
        this.patrimonioDAO = new PatrimonioDAO();
        this.inventarioDAO = new InventarioDAO();
    }
    
    /**
     * Busca estatísticas gerais do dashboard
     */
    public Map<String, Object> buscarEstatisticasGerais(Integer inventarioId) throws SQLException {
        logger.info("Buscando estatísticas gerais (inventário: {})", inventarioId);
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            throw new IllegalArgumentException("Inventário não encontrado");
        }
        
        // Estatísticas básicas
        int totalPatrimonios = patrimonioDAO.findAll().size();
        int totalColetados = coletaDAO.contarColetasPorInventario(inventario.getId());
        int totalPendentes = totalPatrimonios - totalColetados;
        double percentualConclusao = totalPatrimonios > 0 
                ? (totalColetados * 100.0) / totalPatrimonios 
                : 0.0;
        
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("inventarioId", inventario.getId());
        estatisticas.put("inventarioNome", inventario.getNome());
        estatisticas.put("totalPatrimonios", totalPatrimonios);
        estatisticas.put("totalColetados", totalColetados);
        estatisticas.put("totalPendentes", totalPendentes);
        estatisticas.put("percentualConclusao", Math.round(percentualConclusao * 100.0) / 100.0);
        
        logger.info("Estatísticas: {}% concluído ({}/{})", 
                Math.round(percentualConclusao), totalColetados, totalPatrimonios);
        
        return estatisticas;
    }
    
    /**
     * Busca evolução de coletas (últimos N dias)
     */
    public Map<String, Object> buscarEvolucaoColetas(Integer inventarioId, int dias) throws SQLException {
        logger.info("Buscando evolução de coletas (últimos {} dias)", dias);
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            throw new IllegalArgumentException("Inventário não encontrado");
        }
        
        // Buscar coletas agrupadas por dia (dados reais do banco)
        List<Map<String, Object>> evolucaoDados = coletaDAO.buscarEvolucaoColetasPorDia(inventario.getId(), dias);
        
        // Formatar dados para o formato esperado pelo frontend
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");
        Map<String, Integer> evolucaoPorDia = new LinkedHashMap<>();
        
        // Preencher todos os dias do período (mesmo sem coletas)
        Calendar cal = Calendar.getInstance();
        for (int i = dias - 1; i >= 0; i--) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String dataFormatada = sdf.format(cal.getTime());
            evolucaoPorDia.put(dataFormatada, 0);
        }
        
        // Preencher com dados reais
        for (Map<String, Object> item : evolucaoDados) {
            java.sql.Date data = (java.sql.Date) item.get("data");
            Integer quantidade = (Integer) item.get("quantidade");
            String dataFormatada = sdf.format(data);
            evolucaoPorDia.put(dataFormatada, quantidade);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("inventarioId", inventario.getId());
        resultado.put("inventarioNome", inventario.getNome());
        resultado.put("dias", dias);
        resultado.put("evolucao", evolucaoPorDia);
        resultado.put("totalColetas", evolucaoDados.stream()
                .mapToInt(m -> (Integer) m.get("quantidade"))
                .sum());
        
        logger.info("Evolução carregada: {} dias com dados", evolucaoDados.size());
        
        return resultado;
    }
    
    /**
     * Busca top itens mais coletados
     */
    public Map<String, Object> buscarTopItens(Integer inventarioId, int limit) throws SQLException {
        logger.info("Buscando top {} itens", limit);
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            throw new IllegalArgumentException("Inventário não encontrado");
        }
        
        // Buscar top itens do banco (dados reais)
        List<Map<String, Object>> topItensDados = coletaDAO.buscarTopItensColetados(inventario.getId(), limit);
        
        // Formatar dados para o formato esperado pelo frontend
        Map<String, Integer> topItens = new LinkedHashMap<>();
        for (Map<String, Object> item : topItensDados) {
            String descricao = (String) item.get("descricao");
            Integer quantidade = (Integer) item.get("quantidade");
            topItens.put(descricao, quantidade);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("inventarioId", inventario.getId());
        resultado.put("inventarioNome", inventario.getNome());
        resultado.put("limit", limit);
        resultado.put("topItens", topItens);
        resultado.put("total", topItens.size());
        
        logger.info("Top itens carregados: {} itens encontrados", topItens.size());
        
        return resultado;
    }
    
    /**
     * Busca estatísticas por status de coleta
     */
    public Map<String, Object> buscarEstatisticasPorStatus(Integer inventarioId) throws SQLException {
        logger.info("Buscando estatísticas por status");
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            throw new IllegalArgumentException("Inventário não encontrado");
        }
        
        // Buscar estatísticas por status
        List<Map<String, Object>> statusDados = coletaDAO.buscarEstatisticasPorStatus(inventario.getId());
        
        // Formatar dados
        Map<String, Integer> statusMap = new LinkedHashMap<>();
        for (Map<String, Object> item : statusDados) {
            String status = (String) item.get("status");
            Integer quantidade = (Integer) item.get("quantidade");
            statusMap.put(status, quantidade);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("inventarioId", inventario.getId());
        resultado.put("inventarioNome", inventario.getNome());
        resultado.put("statusDistribuicao", statusMap);
        resultado.put("total", statusMap.values().stream().mapToInt(Integer::intValue).sum());
        
        logger.info("Estatísticas por status carregadas: {} status diferentes", statusMap.size());
        
        return resultado;
    }
    
    /**
     * Busca distribuição de coletas por sala
     */
    public Map<String, Object> buscarDistribuicaoPorSala(Integer inventarioId, int limit) throws SQLException {
        logger.info("Buscando distribuição por sala (top {})", limit);
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            throw new IllegalArgumentException("Inventário não encontrado");
        }
        
        // Buscar distribuição por sala
        List<Map<String, Object>> salaDados = coletaDAO.buscarDistribuicaoPorSala(inventario.getId(), limit);
        
        // Formatar dados
        Map<String, Integer> salaMap = new LinkedHashMap<>();
        for (Map<String, Object> item : salaDados) {
            String sala = (String) item.get("sala");
            Integer quantidade = (Integer) item.get("quantidade");
            salaMap.put(sala, quantidade);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("inventarioId", inventario.getId());
        resultado.put("inventarioNome", inventario.getNome());
        resultado.put("limit", limit);
        resultado.put("distribuicaoPorSala", salaMap);
        resultado.put("total", salaMap.size());
        
        logger.info("Distribuição por sala carregada: {} salas", salaMap.size());
        
        return resultado;
    }
}
