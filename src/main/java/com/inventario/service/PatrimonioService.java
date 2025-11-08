package com.inventario.service;

import com.inventario.dao.PatrimonioDAORefactored;
import com.inventario.model.Patrimonio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações com Patrimônio
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional
public class PatrimonioService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatrimonioService.class);
    
    @Autowired
    private PatrimonioDAORefactored patrimonioDAO;
    
    /**
     * Busca patrimônio por ID
     */
    public Patrimonio buscarPorId(Long id) {
        try {
            return patrimonioDAO.findById(id.intValue());
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônio por ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Salva ou atualiza um patrimônio
     */
    public void salvar(Patrimonio patrimonio) throws SQLException {
        if (patrimonio.getId() > 0) {
            patrimonioDAO.update(patrimonio);
        } else {
            patrimonioDAO.insert(patrimonio);
        }
    }
    
    /**
     * Lista todos os patrimônios
     */
    public List<Patrimonio> listarTodos() {
        try {
            return patrimonioDAO.findAll();
        } catch (SQLException e) {
            logger.error("Erro ao listar patrimônios", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca patrimônios atualizados após uma data específica
     * Para implementação futura com controle de timestamps
     */
    public List<Patrimonio> buscarAtualizadosApos(LocalDateTime lastSync, Long setorId, Long salaId) {
        try {
            // Por enquanto, retorna todos os patrimônios
            // TODO: Implementar filtro por data de atualização quando campo for adicionado
            List<Patrimonio> todos = patrimonioDAO.findAll();
            
            // Filtrar por sala se especificado
            if (salaId != null) {
                return todos.stream()
                    .filter(p -> p.getIdSala() == salaId.intValue())
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return todos;
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônios atualizados", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca patrimônios criados após uma data específica
     * Para implementação futura com controle de timestamps
     */
    public List<Patrimonio> buscarCriadosApos(LocalDateTime lastSync, Long setorId, Long salaId) {
        try {
            // Por enquanto, retorna lista vazia
            // TODO: Implementar filtro por data de criação quando campo for adicionado
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios criados", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca patrimônio por número
     */
    public Patrimonio buscarPorNumero(String numero) {
        try {
            return patrimonioDAO.buscarPorNumero(numero);
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônio por número: {}", numero, e);
            return null;
        }
    }
    
    /**
     * Busca patrimônios por sala
     */
    public List<Patrimonio> buscarPorSala(int idSala) {
        try {
            return patrimonioDAO.buscarPorSala(idSala);
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônios por sala: {}", idSala, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca patrimônios por descrição abrangente
     */
    public List<Patrimonio> buscarPorDescricaoAbrangente(String descricao) {
        try {
            return patrimonioDAO.buscarPorDescricaoAbrangente(descricao);
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônios por descrição: {}", descricao, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Exclui patrimônio
     */
    public void excluir(int id) {
        try {
            patrimonioDAO.delete(id);
        } catch (SQLException e) {
            logger.error("Erro ao excluir patrimônio: {}", id, e);
            throw new RuntimeException("Erro ao excluir patrimônio", e);
        }
    }
}