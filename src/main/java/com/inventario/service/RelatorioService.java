package com.inventario.service;

import com.inventario.dao.RelatorioColetaDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Serviço para operações de relatórios
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class RelatorioService {
    
    private static final Logger logger = LoggerFactory.getLogger(RelatorioService.class);
    
    private final RelatorioColetaDAO relatorioDAO;
    
    public RelatorioService() {
        this.relatorioDAO = new RelatorioColetaDAO();
    }
    
    public RelatorioService(RelatorioColetaDAO relatorioDAO) {
        this.relatorioDAO = relatorioDAO;
    }
    
    /**
     * Gera relatório de itens encontrados
     */
    public List<Map<String, Object>> gerarRelatorioItensEncontrados(int idInventario) {
        try {
            return relatorioDAO.gerarRelatorioItensEncontrados(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de itens encontrados: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório de itens não encontrados
     */
    public List<Map<String, Object>> gerarRelatorioItensNaoEncontrados(int idInventario) {
        try {
            return relatorioDAO.gerarRelatorioItensNaoEncontrados(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de itens não encontrados: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório de itens sem etiqueta
     */
    public List<Map<String, Object>> gerarRelatorioItensSemEtiqueta(int idInventario) {
        try {
            return relatorioDAO.gerarRelatorioItensSemEtiqueta(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de itens sem etiqueta: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório detalhado por responsável
     */
    public List<Map<String, Object>> gerarRelatorioDetalhadoPorResponsavel(int idInventario, String nomeResponsavel) {
        try {
            return relatorioDAO.gerarRelatorioDetalhadoPorResponsavel(idInventario, nomeResponsavel);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório por responsável: {} - {}", idInventario, nomeResponsavel, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório de itens não coletados
     */
    public List<Map<String, Object>> gerarRelatorioItensNaoColetados(int idInventario) {
        try {
            return relatorioDAO.gerarRelatorioItensNaoColetados(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de itens não coletados: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório de divergências
     */
    public List<Map<String, Object>> gerarRelatorioDivergencias(int idInventario) {
        try {
            return relatorioDAO.gerarRelatorioDivergencias(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de divergências: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera estatísticas gerais
     */
    public List<Map<String, Object>> gerarEstatisticasGerais(int idInventario) {
        try {
            return relatorioDAO.gerarEstatisticasGerais(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao gerar estatísticas gerais: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório avançado por setor
     */
    public List<Map<String, Object>> gerarRelatorioAvancadoPorSetor(int idInventario, String nomeSetor, 
            Date dataInicio, Date dataFim) {
        try {
            return relatorioDAO.gerarRelatorioAvancadoPorSetor(idInventario, nomeSetor, dataInicio, dataFim);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório avançado por setor: {} - {}", idInventario, nomeSetor, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório avançado por responsável
     */
    public List<Map<String, Object>> gerarRelatorioAvancadoPorResponsavel(int idInventario, String nomeResponsavel,
            Date dataInicio, Date dataFim) {
        try {
            return relatorioDAO.gerarRelatorioAvancadoPorResponsavel(idInventario, nomeResponsavel, dataInicio, dataFim);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório avançado por responsável: {} - {}", idInventario, nomeResponsavel, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório avançado por período
     */
    public List<Map<String, Object>> gerarRelatorioAvancadoPorPeriodo(int idInventario, Date dataInicio, Date dataFim) {
        try {
            return relatorioDAO.gerarRelatorioAvancadoPorPeriodo(idInventario, dataInicio, dataFim);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório por período: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera estatísticas avançadas por setor
     */
    public List<Map<String, Object>> gerarEstatisticasAvancadasPorSetor(int idInventario, Date dataInicio, Date dataFim) {
        try {
            return relatorioDAO.gerarEstatisticasAvancadasPorSetor(idInventario, dataInicio, dataFim);
        } catch (Exception e) {
            logger.error("Erro ao gerar estatísticas avançadas por setor: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório consolidado
     */
    public List<Map<String, Object>> gerarRelatorioConsolidado(int idInventario, Date dataInicio, Date dataFim) {
        try {
            return relatorioDAO.gerarRelatorioConsolidado(idInventario, dataInicio, dataFim);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório consolidado: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gera relatório geral completo
     */
    public List<Map<String, Object>> gerarRelatorioGeralCompleto(int idInventario) {
        try {
            return relatorioDAO.gerarRelatorioGeralCompleto(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório geral completo: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
}
