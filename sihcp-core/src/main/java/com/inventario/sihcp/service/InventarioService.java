package com.inventario.sihcp.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.InventarioSetorDAO;
import com.inventario.sihcp.dao.ParticipanteInventarioDAO;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.ParticipanteInventario;

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
    
    private final InventarioDAO inventarioDAO;
    private final InventarioSetorDAO inventarioSetorDAO;
    private final ParticipanteInventarioDAO participanteInventarioDAO;
    
    public InventarioService() {
        this.inventarioDAO = new InventarioDAO();
        this.inventarioSetorDAO = new InventarioSetorDAO();
        this.participanteInventarioDAO = new ParticipanteInventarioDAO();
    }
    
    public InventarioService(InventarioDAO inventarioDAO, 
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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
        } catch (BusinessException | SQLException e) {
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
        } catch (BusinessException | SQLException e) {
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
        } catch (SQLException e) {
            logger.error("Erro ao finalizar inventário: {}", idInventario, e);
            throw new BusinessException("Erro ao finalizar inventário: " + e.getMessage());
        }
    }
    
    /**
     * Finaliza um inventário (alias para finalizar)
     */
    public boolean finalizarInventario(int idInventario) throws BusinessException {
        return finalizar(idInventario);
    }
    
    /**
     * Cancela um inventário
     */
    public boolean cancelarInventario(int idInventario) throws BusinessException {
        try {
            Inventario inventario = inventarioDAO.findById(idInventario);
            if (inventario == null) {
                throw new BusinessException("Inventário não encontrado");
            }
            inventario.setStatusInventario(Inventario.STATUS_CANCELADO);
            inventarioDAO.update(inventario);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erro ao cancelar inventário: {}", idInventario, e);
            throw new BusinessException("Erro ao cancelar inventário: " + e.getMessage());
        }
    }
    
    /**
     * Exclui um inventário
     */
    public boolean excluir(int idInventario) throws BusinessException {
        try {
            Inventario inventario = inventarioDAO.findById(idInventario);
            if (inventario == null) {
                throw new BusinessException("Inventário não encontrado");
            }
            inventarioDAO.delete(idInventario);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Erro ao excluir inventário: {}", idInventario, e);
            throw new BusinessException("Erro ao excluir inventário: " + e.getMessage());
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
     * Salva configuração de participantes do inventário.
     * Usa DELETE físico + INSERT atômico para evitar race conditions do soft-delete.
     */
    public boolean salvarConfiguracaoParticipantes(int idInventario, boolean incluirTodos, List<Integer> idsUsuarios) 
            throws BusinessException {
        try {
            // DELETE físico dos participantes existentes (evita conflito com registros ativo=TRUE)
            participanteInventarioDAO.removerTodosParticipantes(idInventario);

            // Adicionar novos participantes
            boolean sucesso = true;
            for (Integer idUsuario : idsUsuarios) {
                try {
                    ParticipanteInventario participante = new ParticipanteInventario(idInventario, idUsuario, "COLETOR");
                    participanteInventarioDAO.inserirParticipante(participante);
                } catch (IllegalArgumentException e) {
                    logger.error("Usuário ID={} inválido para inventário ID={}: {}", idUsuario, idInventario, e.getMessage());
                    sucesso = false;
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
     * Verifica se o inventário inclui todos os setores
     */
    public boolean inventarioIncluiTodosSetores(int idInventario) {
        try {
            return inventarioSetorDAO.inventarioIncluiTodosSetores(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao verificar se inventário inclui todos os setores: {}", idInventario, e);
            return false;
        }
    }
    
    /**
     * Busca os setores de um inventário
     */
    public List<com.inventario.sihcp.model.Setor> buscarSetoresDoInventario(int idInventario) {
        try {
            return inventarioSetorDAO.buscarSetoresDoInventario(idInventario);
        } catch (Exception e) {
            logger.error("Erro ao buscar setores do inventário: {}", idInventario, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca inventário por status
     */
    public Inventario buscarPorStatus(String status) {
        try {
            return inventarioDAO.buscarPorStatus(status);
        } catch (SQLException ex) {
            logger.error("Erro ao buscar inventário por status: {}", status, ex);
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
