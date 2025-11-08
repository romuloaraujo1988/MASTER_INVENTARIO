package com.inventario.service;

import com.inventario.dao.SalaInventarioDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações de sala-inventário
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class SalaInventarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(SalaInventarioService.class);
    
    private final SalaInventarioDAO salaInventarioDAO;
    
    public SalaInventarioService() {
        this.salaInventarioDAO = new SalaInventarioDAO();
    }
    
    public SalaInventarioService(SalaInventarioDAO salaInventarioDAO) {
        this.salaInventarioDAO = salaInventarioDAO;
    }
    
    /**
     * Lista salas-inventário de um inventário
     */
    public List<com.inventario.model.SalaInventario> listarPorInventario(int idInventario) {
        try {
            return salaInventarioDAO.listarPorInventario(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao listar salas do inventário: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Finaliza coleta de uma sala
     */
    public boolean finalizarColetaSala(int idSala, int idInventario, int idUsuario, String observacao) {
        try {
            return salaInventarioDAO.finalizarColeta(idSala, idInventario, idUsuario, observacao);
        } catch (Exception e) {
            logger.error("Erro ao finalizar coleta da sala {} no inventário {}", idSala, idInventario, e);
            return false;
        }
    }
    
    /**
     * Verifica se sala já foi coletada
     */
    public boolean verificarSalaColetada(int idSala, int idInventario) {
        try {
            return salaInventarioDAO.isColetaFinalizada(idSala, idInventario);
        } catch (Exception e) {
            logger.error("Erro ao verificar se sala foi coletada: {} - {}", idSala, idInventario, e);
            return false;
        }
    }
    
    /**
     * Lista salas-inventário por status
     */
    public List<com.inventario.model.SalaInventario> listarPorStatus(int idInventario, String status) {
        try {
            return salaInventarioDAO.listarPorStatus(idInventario, status);
        } catch (Exception e) {
            logger.error("Erro ao listar salas por status {} do inventário {}", status, idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Conta salas finalizadas de um inventário
     */
    public int contarSalasFinalizadas(int idInventario) {
        try {
            return salaInventarioDAO.contarSalasFinalizadas(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao contar salas finalizadas do inventário: {}", idInventario, e);
            return 0;
        }
    }
    
    /**
     * Verifica se coleta foi finalizada
     */
    public boolean isColetaFinalizada(int idSala, int idInventario) {
        try {
            return salaInventarioDAO.isColetaFinalizada(idSala, idInventario);
        } catch (Exception e) {
            logger.error("Erro ao verificar se coleta foi finalizada: {} - {}", idSala, idInventario, e);
            return false;
        }
    }
    
    /**
     * Inicia coleta de uma sala
     */
    public boolean iniciarColeta(int idSala, int idInventario, int idParticipante) {
        try {
            return salaInventarioDAO.iniciarColeta(idSala, idInventario, idParticipante);
        } catch (Exception e) {
            logger.error("Erro ao iniciar coleta da sala {} no inventário {}", idSala, idInventario, e);
            return false;
        }
    }
    
    /**
     * Atualiza estatísticas da sala
     */
    public boolean atualizarEstatisticas(int idSala, int idInventario, int totalItens, int itensSemEtiqueta) {
        try {
            return salaInventarioDAO.atualizarEstatisticas(idSala, idInventario, totalItens, itensSemEtiqueta);
        } catch (Exception e) {
            logger.error("Erro ao atualizar estatísticas da sala {} no inventário {}", idSala, idInventario, e);
            return false;
        }
    }
    
    /**
     * Finaliza coleta de uma sala com observação
     */
    public boolean finalizarColeta(int idSala, int idInventario, int idParticipante, String observacao) {
        try {
            return salaInventarioDAO.finalizarColeta(idSala, idInventario, idParticipante, observacao);
        } catch (Exception e) {
            logger.error("Erro ao finalizar coleta da sala {} no inventário {}", idSala, idInventario, e);
            return false;
        }
    }
    
    /**
     * Reabre coleta de uma sala
     */
    public boolean reabrirColeta(int idSala, int idInventario) {
        try {
            return salaInventarioDAO.reabrirColeta(idSala, idInventario);
        } catch (Exception e) {
            logger.error("Erro ao reabrir coleta da sala {} no inventário {}", idSala, idInventario, e);
            return false;
        }
    }
}
