package com.inventario.service;

import com.inventario.dao.SalaDAORefactored;
import com.inventario.model.Sala;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações com Sala
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
@Transactional
public class SalaService {
    
    private static final Logger logger = LoggerFactory.getLogger(SalaService.class);
    
    @Autowired
    private SalaDAORefactored salaDAO;
    
    public SalaService() {
        this.salaDAO = new SalaDAORefactored();
    }
    
    public SalaService(SalaDAORefactored salaDAO) {
        this.salaDAO = salaDAO;
    }
    
    public List<Sala> listarTodas() {
        try {
            return salaDAO.findAll();
        } catch (SQLException e) {
            logger.error("Erro ao listar salas", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Lista todas as salas (alias para listarTodas)
     */
    public List<Sala> listarSalas() {
        return listarTodas();
    }
    
    public List<Sala> listarAtivas() {
        try {
            return salaDAO.buscarPorFiltro(null, null, null, null, null, null, true);
        } catch (SQLException e) {
            logger.error("Erro ao listar salas ativas", e);
            return new ArrayList<>();
        }
    }
    
    public Sala buscarPorId(int id) {
        try {
            return salaDAO.buscarSalaPorId(id);
        } catch (SQLException e) {
            logger.error("Erro ao buscar sala por ID: {}", id, e);
            return null;
        }
    }
    
    public List<Sala> buscarPorFiltro(String descricao, String numeroSala, String bloco, 
                                       Integer andar, Integer idSetor, String tipoSala, Boolean ativo) {
        try {
            return salaDAO.buscarPorFiltro(descricao, numeroSala, bloco, andar, idSetor, tipoSala, ativo);
        } catch (SQLException e) {
            logger.error("Erro ao buscar salas por filtro", e);
            return new ArrayList<>();
        }
    }
    
    public void salvar(Sala sala) throws BusinessException {
        try {
            validarSala(sala);
            
            if (salaDAO.salaExiste(sala.getNumeroSala(), sala.getIdSala())) {
                throw new BusinessException("Já existe uma sala com este número");
            }
            
            if (sala.getIdSala() == 0) {
                salaDAO.inserirSalaComSucesso(sala);
                logger.info("Sala criada: ID={}", sala.getIdSala());
            } else {
                salaDAO.atualizarSala(sala);
                logger.info("Sala atualizada: ID={}", sala.getIdSala());
            }
        } catch (SQLException e) {
            logger.error("Erro ao salvar sala", e);
            throw new BusinessException("Erro ao salvar sala: " + e.getMessage(), e);
        }
    }
    
    public void excluir(int id) throws BusinessException {
        try {
            int qtdPatrimonios = salaDAO.contarPatrimoniosDaSala(id);
            if (qtdPatrimonios > 0) {
                throw new BusinessException(
                    String.format("Não é possível excluir a sala pois possui %d patrimônio(s) vinculado(s)", qtdPatrimonios)
                );
            }
            
            salaDAO.delete(id);
            logger.info("Sala excluída: ID={}", id);
        } catch (SQLException e) {
            logger.error("Erro ao excluir sala: ID={}", id, e);
            throw new BusinessException("Erro ao excluir sala: " + e.getMessage(), e);
        }
    }
    
    public void alterarStatus(int id, boolean ativo) throws BusinessException {
        try {
            Sala sala = salaDAO.buscarSalaPorId(id);
            if (sala != null) {
                sala.setAtivo(ativo);
                salaDAO.atualizarSala(sala);
            }
        } catch (SQLException e) {
            logger.error("Erro ao alterar status da sala: ID={}", id, e);
            throw new BusinessException("Erro ao alterar status: " + e.getMessage(), e);
        }
    }
    
    private void validarSala(Sala sala) throws BusinessException {
        if (sala == null) {
            throw new BusinessException("Sala não pode ser nula");
        }
        
        if (sala.getDescricao() == null || sala.getDescricao().trim().isEmpty()) {
            throw new BusinessException("Descrição da sala é obrigatória");
        }
        
        if (sala.getNumeroSala() == null || sala.getNumeroSala().trim().isEmpty()) {
            throw new BusinessException("Número da sala é obrigatório");
        }
    }
}
