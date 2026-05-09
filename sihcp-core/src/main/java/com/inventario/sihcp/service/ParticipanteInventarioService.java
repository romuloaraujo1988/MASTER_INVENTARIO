package com.inventario.sihcp.service;

import com.inventario.sihcp.dao.ParticipanteInventarioDAO;
import com.inventario.sihcp.model.ParticipanteInventario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações com ParticipanteInventario
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class ParticipanteInventarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(ParticipanteInventarioService.class);
    
    private final ParticipanteInventarioDAO participanteInventarioDAO;
    
    public ParticipanteInventarioService() {
        this.participanteInventarioDAO = new ParticipanteInventarioDAO();
    }
    
    public ParticipanteInventarioService(ParticipanteInventarioDAO participanteInventarioDAO) {
        this.participanteInventarioDAO = participanteInventarioDAO;
    }
    
    /**
     * Busca ID do participante por usuário e inventário
     */
    public Integer buscarIdParticipantePorUsuario(int idInventario, int idUsuario) {
        try {
            return participanteInventarioDAO.buscarIdParticipantePorUsuario(idInventario, idUsuario);
        } catch (Exception e) {
            logger.error("Erro ao buscar participante por usuário: {} - {}", idInventario, idUsuario, e);
            return null;
        }
    }
    
    /**
     * Lista participantes de um inventário
     */
    public List<ParticipanteInventario> listarParticipantesInventario(int idInventario) {
        try {
            return participanteInventarioDAO.listarParticipantesInventario(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao listar participantes do inventário: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Adiciona participante ao inventário
     */
    public boolean adicionarParticipante(ParticipanteInventario participante) throws BusinessException {
        try {
            return participanteInventarioDAO.adicionarParticipante(participante);
        } catch (Exception e) {
            logger.error("Erro ao adicionar participante", e);
            throw new BusinessException("Erro ao adicionar participante: " + e.getMessage());
        }
    }
    
    /**
     * Remove participante do inventário
     */
    public boolean removerParticipante(int idInventario, int idUsuario) throws BusinessException {
        try {
            return participanteInventarioDAO.removerParticipante(idInventario, idUsuario);
        } catch (Exception e) {
            logger.error("Erro ao remover participante: {} - {}", idInventario, idUsuario, e);
            throw new BusinessException("Erro ao remover participante: " + e.getMessage());
        }
    }
}
