package com.inventario.repository;

import com.inventario.model.Patrimonio;
import com.inventario.dao.PatrimonioDAORefactored;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Patrimonio
 * Implementa o padrão Repository sobre o DAO existente
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Repository
public class PatrimonioRepository implements com.inventario.repository.Repository<Patrimonio, Integer> {
    
    private final PatrimonioDAORefactored dao;
    
    public PatrimonioRepository() {
        this.dao = new PatrimonioDAORefactored();
    }
    
    public PatrimonioRepository(PatrimonioDAORefactored dao) {
        this.dao = dao;
    }
    
    @Override
    public Patrimonio save(Patrimonio entity) {
        try {
            if (entity.getId() > 0) {
                dao.update(entity);
            } else {
                dao.insert(entity);
            }
            return entity;
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao salvar patrimônio", e);
        }
    }
    
    @Override
    public Optional<Patrimonio> findById(Integer id) {
        try {
            Patrimonio patrimonio = dao.findById(id);
            return Optional.ofNullable(patrimonio);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao buscar patrimônio por ID", e);
        }
    }
    
    @Override
    public List<Patrimonio> findAll() {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao listar patrimônios", e);
        }
    }
    
    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }
    
    @Override
    public long count() {
        try {
            return dao.count();
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao contar patrimônios", e);
        }
    }
    
    @Override
    public void deleteById(Integer id) {
        try {
            dao.delete(id);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao deletar patrimônio", e);
        }
    }
    
    @Override
    public void delete(Patrimonio entity) {
        deleteById(entity.getId());
    }
    
    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Operação não suportada");
    }
    
    // Métodos específicos de Patrimonio
    
    public Optional<Patrimonio> findByNumero(String numero) {
        try {
            Patrimonio patrimonio = dao.buscarPorNumero(numero);
            return Optional.ofNullable(patrimonio);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao buscar patrimônio por número", e);
        }
    }
    
    public List<Patrimonio> findByDescricaoContaining(String descricao) {
        try {
            return dao.buscarPorDescricao(descricao);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao buscar patrimônios por descrição", e);
        }
    }
    
    public List<Patrimonio> findBySalaId(Integer idSala) {
        try {
            return dao.buscarPorSala(idSala);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao buscar patrimônios por sala", e);
        }
    }
    
    public List<Patrimonio> findByResponsavelId(Integer idResponsavel) {
        try {
            // Busca por ID usando paginação com valores grandes para pegar todos
            return dao.buscarPorResponsavelComPaginacao(idResponsavel, 0, Integer.MAX_VALUE);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao buscar patrimônios por responsável", e);
        }
    }
    
    public List<Patrimonio> findByResponsavelNome(String nomeResponsavel) {
        try {
            return dao.buscarPorResponsavel(nomeResponsavel);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao buscar patrimônios por nome do responsável", e);
        }
    }
}
