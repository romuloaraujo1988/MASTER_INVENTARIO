package com.inventario.sihcp.mobile.server.service;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.model.Inventario;

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
        logger.debug("Buscando estatísticas gerais (inventário: {})", inventarioId);
        
        // Obter inventário
        Inventario inventario;
        if (inventarioId != null) {
            inventario = inventarioDAO.findById(inventarioId);
        } else {
            inventario = inventarioDAO.buscarInventarioAtivo();
        }
        
        if (inventario == null) {
            logger.error("Inventário não encontrado! inventarioId={}", inventarioId);
            throw new IllegalArgumentException("Inventário não encontrado");
        }
        
        logger.debug("Inventário encontrado: ID={}", inventario.getId());
        
        // Estatísticas básicas
        // ATUALIZADO: Usar contarPatrimoniosAInventariar() que inclui Ativo + Pendente (11.070)
        int totalPatrimonios = patrimonioDAO.contarPatrimoniosAInventariar();
        int totalColetados = coletaDAO.contarColetasPorInventario(inventario.getId());
        int totalPendentes = totalPatrimonios - totalColetados;
        double percentualConclusao = totalPatrimonios > 0 
                ? (totalColetados * 100.0) / totalPatrimonios 
                : 0.0;
        
        // Buscar divergências (patrimônios coletados em local diferente do cadastrado)
        int divergencias = coletaDAO.contarDivergenciasPorInventario(inventario.getId());
        
        // Buscar coletores ativos (usuários que fizeram coletas neste inventário)
        int coletoresAtivos = coletaDAO.contarColetoresAtivosPorInventario(inventario.getId());
        
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("inventarioId", inventario.getId());
        estatisticas.put("inventarioNome", inventario.getNome());
        estatisticas.put("totalPatrimonios", totalPatrimonios);
        // ✅ CORRIGIDO: Usar nomes consistentes com DTO
        estatisticas.put("patrimoniosColetados", totalColetados);
        estatisticas.put("patrimoniosPendentes", totalPendentes);
        estatisticas.put("percentualConclusao", Math.round(percentualConclusao * 100.0) / 100.0);
        estatisticas.put("divergencias", divergencias);
        estatisticas.put("coletoresAtivos", coletoresAtivos);
        
        logger.debug("Estatísticas: {}% concluído ({}/{})", 
                Math.round(percentualConclusao), totalColetados, totalPatrimonios);
        
        return estatisticas;
    }
    
    /**
     * Busca evolução de coletas usando as datas de início e fim do inventário
     * 
     * CORREÇÃO: Agora usa as datas reais do inventário ao invés de "últimos N dias"
     * - Se o inventário tem dataInicio e dataFim: usa esse período
     * - Se o inventário tem apenas dataInicio: usa de dataInicio até hoje
     * - Fallback: usa últimos N dias (comportamento anterior)
     * 
     * @param inventarioId ID do inventário (null = inventário ativo)
     * @param dias Quantidade de dias (usado apenas como fallback se inventário não tem datas)
     */
    public Map<String, Object> buscarEvolucaoColetas(Integer inventarioId, int dias) throws SQLException {
        logger.debug("Buscando evolução de coletas para inventário: {}", inventarioId);
        
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
        
        // Determinar período do gráfico baseado nas datas do inventário
        Date dataInicio = inventario.getDataInicio();
        Date dataFim = inventario.getDataFim();
        Date hoje = new Date();
        
        // Formatar dados para o formato esperado pelo frontend
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");
        java.text.SimpleDateFormat sdfLog = new java.text.SimpleDateFormat("dd/MM/yyyy");
        Map<String, Integer> evolucaoPorDia = new LinkedHashMap<>();
        
        List<Map<String, Object>> evolucaoDados;
        int diasPeriodo;
        
        // Usar datas do inventário se disponíveis
        if (dataInicio != null) {
            // Determinar data final: usar dataFim se existir e for no passado, senão usar hoje
            Date dataFimEfetiva;
            if (dataFim != null && dataFim.before(hoje)) {
                dataFimEfetiva = dataFim;
            } else {
                dataFimEfetiva = hoje;
            }
            
            logger.debug("Usando período do inventário: {} a {}", 
                    sdfLog.format(dataInicio), sdfLog.format(dataFimEfetiva));
            
            // Buscar coletas no período do inventário
            evolucaoDados = coletaDAO.buscarEvolucaoColetasPorPeriodo(
                    inventario.getId(), dataInicio, dataFimEfetiva);
            
            // Calcular quantidade de dias no período
            long diffMillis = dataFimEfetiva.getTime() - dataInicio.getTime();
            diasPeriodo = (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1;
            
            // Preencher todos os dias do período (mesmo sem coletas)
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataInicio);
            
            for (int i = 0; i < diasPeriodo; i++) {
                String dataFormatada = sdf.format(cal.getTime());
                evolucaoPorDia.put(dataFormatada, 0);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else {
            // Fallback: usar últimos N dias (comportamento anterior)
            logger.debug("Inventário sem data de início definida, usando últimos {} dias", dias);
            
            evolucaoDados = coletaDAO.buscarEvolucaoColetasPorDia(inventario.getId(), dias);
            diasPeriodo = dias;
            
            // Preencher todos os dias do período (mesmo sem coletas)
            for (int i = dias - 1; i >= 0; i--) {
                Calendar tempCal = Calendar.getInstance();
                tempCal.add(Calendar.DAY_OF_MONTH, -i);
                String dataFormatada = sdf.format(tempCal.getTime());
                evolucaoPorDia.put(dataFormatada, 0);
            }
        }
        
        // Preencher com dados reais
        for (Map<String, Object> item : evolucaoDados) {
            Object dataObj = item.get("data");
            Integer quantidade = ((Number) item.get("quantidade")).intValue();
            
            String dataFormatada;
            if (dataObj instanceof java.sql.Date) {
                dataFormatada = sdf.format((java.sql.Date) dataObj);
            } else if (dataObj instanceof Date) {
                dataFormatada = sdf.format((Date) dataObj);
            } else {
                continue; // Pular se não conseguir converter
            }
            
            evolucaoPorDia.put(dataFormatada, quantidade);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("inventarioId", inventario.getId());
        resultado.put("inventarioNome", inventario.getNome());
        resultado.put("dias", diasPeriodo);
        resultado.put("evolucao", evolucaoPorDia);
        resultado.put("totalColetas", evolucaoDados.stream()
                .mapToInt(m -> ((Number) m.get("quantidade")).intValue())
                .sum());
        
        // Adicionar informações do período para debug/exibição
        if (dataInicio != null) {
            resultado.put("dataInicio", sdfLog.format(dataInicio));
            resultado.put("dataFim", inventario.getDataFim() != null ? 
                    sdfLog.format(inventario.getDataFim()) : sdfLog.format(new Date()));
            resultado.put("usandoDatasInventario", true);
        } else {
            resultado.put("usandoDatasInventario", false);
        }
        
        logger.debug("Evolução carregada: {} dias, {} registros", diasPeriodo, evolucaoDados.size());
        
        return resultado;
    }
    
    /**
     * Busca top itens mais coletados
     */
    public Map<String, Object> buscarTopItens(Integer inventarioId, int limit) throws SQLException {
        logger.debug("Buscando top {} itens", limit);
        
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
        
        logger.debug("Top itens carregados: {} itens", topItens.size());
        
        return resultado;
    }
    
    /**
     * Busca estatísticas por status de coleta
     */
    public Map<String, Object> buscarEstatisticasPorStatus(Integer inventarioId) throws SQLException {
        logger.debug("Buscando estatísticas por status");
        
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
        
        logger.debug("Estatísticas por status carregadas: {} status", statusMap.size());
        
        return resultado;
    }
    
    /**
     * Busca distribuição de coletas por sala
     */
    public Map<String, Object> buscarDistribuicaoPorSala(Integer inventarioId, int limit) throws SQLException {
        logger.debug("Buscando distribuição por sala (top {})", limit);
        
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
        
        logger.debug("Distribuição por sala carregada: {} salas", salaMap.size());
        
        return resultado;
    }
}
