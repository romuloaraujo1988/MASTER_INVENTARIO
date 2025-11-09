package com.inventario.service;

import com.inventario.dao.InventarioDAORefactored;
import com.inventario.dao.InventarioSetorDAO;
import com.inventario.dao.ParticipanteInventarioDAO;
import com.inventario.model.Inventario;
import com.inventario.model.ParticipanteInventario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações com Inventário
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional
public class InventarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);
    
    private final InventarioDAORefactored inventarioDAO;
    private final InventarioSetorDAO inventarioSetorDAO;
    private final ParticipanteInventarioDAO participanteInventarioDAO;
    
    public InventarioService() {
        this.inventarioDAO = new InventarioDAORefactored();
        this.inventarioSetorDAO = new InventarioSetorDAO();
        this.participanteInventarioDAO = new ParticipanteInventarioDAO();
    }
    
    public InventarioService(InventarioDAORefactored inventarioDAO, 
                            InventarioSetorDAO inventarioSetorDAO,
                            ParticipanteInventarioDAO participanteInventarioDAO) {
        this.inventarioDAO = inventarioDAO;
        this.inventarioSetorDAO = inventarioSetorDAO;
        this.participanteInventarioDAO = participanteInventarioDAO;
    }
    
    /**
     * Lista todos os inventários
     */
    public List<Inventario> listarTodos() {
        try {
            return inventarioDAO.findAll();
        } catch (Exception e) {
            logger.error("Erro ao listar inventários", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Lista todos os inventários (alias para listarTodos)
     */
    public List<Inventario> listarInventarios() {
        return listarTodos();
    }
    
    /**
     * Busca inventário por ID
     */
    public Inventario buscarPorId(int id) {
        try {
            return inventarioDAO.findById(id);
        } catch (Exception e) {
            logger.error("Erro ao buscar inventário por ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Busca inventário ativo
     */
    public Inventario buscarInventarioAtivo() {
        try {
            return inventarioDAO.buscarPorStatus("ATIVO");
        } catch (Exception e) {
            logger.error("Erro ao buscar inventário ativo", e);
            return null;
        }
    }
    
    /**
     * Salva um novo inventário
     */
    public boolean salvar(Inventario inventario) throws BusinessException {
        try {
            validarInventario(inventario);
            inventarioDAO.insert(inventario);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao salvar inventário", e);
            throw new BusinessException("Erro ao salvar inventário: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza um inventário existente
     */
    public boolean atualizar(Inventario inventario) throws BusinessException {
        try {
            validarInventario(inventario);
            inventarioDAO.update(inventario);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao atualizar inventário", e);
            throw new BusinessException("Erro ao atualizar inventário: " + e.getMessage());
        }
    }
    
    /**
     * Finaliza um inventário
     */
    public boolean finalizar(int idInventario) throws BusinessException {
        try {
            return inventarioDAO.finalizar(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao finalizar inventário: {}", idInventario, e);
            throw new BusinessException("Erro ao finalizar inventário: " + e.getMessage());
        }
    }
    
    /**
     * Salva configuração de setores do inventário
     */
    public boolean salvarConfiguracaoSetores(int idInventario, boolean incluirTodos, List<Integer> idsSetores) 
            throws BusinessException {
        try {
            return inventarioSetorDAO.salvarConfiguracaoSetores(idInventario, incluirTodos, idsSetores);
        } catch (Exception e) {
            logger.error("Erro ao salvar configuração de setores do inventário: {}", idInventario, e);
            throw new BusinessException("Erro ao salvar configuração de setores: " + e.getMessage());
        }
    }
    
    /**
     * Salva configuração de participantes do inventário
     */
    public boolean salvarConfiguracaoParticipantes(int idInventario, boolean incluirTodos, List<Integer> idsUsuarios) 
            throws BusinessException {
        try {
            // Limpar participantes existentes
            List<ParticipanteInventario> participantesExistentes = participanteInventarioDAO.listarParticipantesInventario(idInventario);
            for (ParticipanteInventario participante : participantesExistentes) {
                participanteInventarioDAO.removerParticipante(idInventario, participante.getIdUsuario());
            }
            
            // Adicionar novos participantes
            boolean sucesso = true;
            for (Integer idUsuario : idsUsuarios) {
                try {
                    ParticipanteInventario participante = new ParticipanteInventario(idInventario, idUsuario, "COLETOR");
                    boolean adicionado = participanteInventarioDAO.adicionarParticipante(participante);
                    if (!adicionado) {
                        sucesso = false;
                    }
                } catch (Exception e) {
                    logger.error("Erro ao adicionar participante {} ao inventário {}", idUsuario, idInventario, e);
                    sucesso = false;
                }
            }
            return sucesso;
        } catch (Exception e) {
            logger.error("Erro ao salvar configuração de participantes do inventário: {}", idInventario, e);
            throw new BusinessException("Erro ao salvar configuração de participantes: " + e.getMessage());
        }
    }
    
    /**
     * Busca participantes de um inventário
     */
    public List<ParticipanteInventario> buscarParticipantes(int idInventario) {
        try {
            return participanteInventarioDAO.listarParticipantesInventario(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao buscar participantes do inventário: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca inventário por status
     */
    public Inventario buscarPorStatus(String status) {
        try {
            return inventarioDAO.buscarPorStatus(status);
        } catch (Exception e) {
            logger.error("Erro ao buscar inventário por status: {}", status, e);
            return null;
        }
    }
    
    /**
     * Valida dados do inventário
     */
    private void validarInventario(Inventario inventario) throws BusinessException {
        if (inventario == null) {
            throw new BusinessException("Inventário não pode ser nulo");
        }
        
        if (inventario.getNome() == null || inventario.getNome().trim().isEmpty()) {
            throw new BusinessException("Nome do inventário é obrigatório");
        }
        
        if (inventario.getDataInicio() == null) {
            throw new BusinessException("Data de início é obrigatória");
        }
        
        if (inventario.getDataFim() == null) {
            throw new BusinessException("Data de fim é obrigatória");
        }
        
        if (inventario.getDataInicio().after(inventario.getDataFim())) {
            throw new BusinessException("Data de início não pode ser posterior à data de fim");
        }
    }
}
