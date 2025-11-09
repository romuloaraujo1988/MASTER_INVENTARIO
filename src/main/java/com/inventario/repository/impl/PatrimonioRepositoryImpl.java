package com.inventario.repository.impl;

import com.inventario.dao.PatrimonioDAO;
import com.inventario.exception.RepositoryException;
import com.inventario.model.Patrimonio;
import com.inventario.repository.PatrimonioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do Repository de Patrimônio
 * Centraliza tratamento de exceções e conversões
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Repository
public class PatrimonioRepositoryImpl implements PatrimonioRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(PatrimonioRepositoryImpl.class);
    
    private final PatrimonioDAO patrimonioDAO;
    
    @Autowired
    public PatrimonioRepositoryImpl(PatrimonioDAO patrimonioDAO) {
        this.patrimonioDAO = patrimonioDAO;
    }
    
    @Override
    public Optional<Patrimonio> findById(Integer id) {
        try {
            logger.debug("Buscando patrimônio por ID: {}", id);
            Patrimonio patrimonio = patrimonioDAO.findById(id);
            return Optional.ofNullable(patrimonio);
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônio por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<Patrimonio> findByNumero(String numero) {
        try {
            logger.debug("Buscando patrimônio por número: {}", numero);
            Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numero);  // ✅ Método correto
            return Optional.ofNullable(patrimonio);
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônio por número: {}", numero, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Patrimonio> findAll() {
        try {
            logger.debug("Listando todos os patrimônios");
            return patrimonioDAO.listarTodosComJoins();  // ✅ Método correto
        } catch (SQLException e) {
            logger.error("Erro ao listar patrimônios", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Patrimonio> findBySala(Integer idSala) {
        try {
            logger.debug("Buscando patrimônios da sala: {}", idSala);
            return patrimonioDAO.buscarPorSala(idSala.intValue());  // ✅ Método correto
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônios da sala: {}", idSala, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Patrimonio> findByDescricao(String descricao) {
        try {
            logger.debug("Buscando patrimônios por descrição: {}", descricao);
            return patrimonioDAO.buscarPorDescricao(descricao);  // ✅ Método correto
        } catch (SQLException e) {
            logger.error("Erro ao buscar patrimônios por descrição: {}", descricao, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Patrimonio save(Patrimonio patrimonio) {
        try {
            // Se getId() retorna int primitivo, apenas verificar se é maior que 0
            if (patrimonio.getId() > 0) {
                logger.info("Atualizando patrimônio: {}", patrimonio.getId());
                patrimonioDAO.update(patrimonio);
            } else {
                logger.info("Inserindo novo patrimônio: {}", patrimonio.getNumero());
                patrimonioDAO.insert(patrimonio);
            }
            return patrimonio;
        } catch (SQLException e) {
            logger.error("Erro ao salvar patrimônio: {}", patrimonio.getNumero(), e);
            throw new RepositoryException("Erro ao salvar patrimônio", e);
        }
    }
    
    @Override
    public void delete(Integer id) {
        try {
            logger.info("Excluindo patrimônio: {}", id);
            patrimonioDAO.delete(id);
        } catch (SQLException e) {
            logger.error("Erro ao excluir patrimônio: {}", id, e);
            throw new RepositoryException("Erro ao excluir patrimônio", e);
        }
    }
    
    @Override
    public long count() {
        try {
            logger.debug("Contando patrimônios");
            return patrimonioDAO.count();
        } catch (SQLException e) {
            logger.error("Erro ao contar patrimônios", e);
            return 0;
        }
    }
    
    @Override
    public boolean existsByNumero(String numero) {
        try {
            logger.debug("Verificando existência do patrimônio: {}", numero);
            // Usar numeroPatrimonioExiste com ID 0 para verificar se existe
            return patrimonioDAO.numeroPatrimonioExiste(numero, 0);  // ✅ Método correto
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência do patrimônio: {}", numero, e);
            return false;
        }
    }
}
