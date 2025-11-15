package com.inventario.repository;

import com.inventario.model.Setor;
import com.inventario.dao.SetorDAO;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Setor
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Repository
public class SetorRepository implements com.inventario.repository.Repository<Setor, Integer> {
    
    private final SetorDAO dao;
    
    public SetorRepository() {
        this.dao = new SetorDAO();
    }
    
    public SetorRepository(SetorDAO dao) {
        this.dao = dao;
    }
    
    @Override
    public Setor save(Setor entity) {
        try {
            if (entity.getId() > 0) {
                dao.update(entity);
            } else {
                dao.insert(entity);
            }
            return entity;
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao salvar setor", e);
        }
    }
    
    @Override
    public Optional<Setor> findById(Integer id) {
        try {
            Setor setor = dao.findById(id);
            return Optional.ofNullable(setor);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao buscar setor por ID", e);
        }
    }
    
    @Override
    public List<Setor> findAll() {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao listar setores", e);
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
            throw new RepositoryException("Erro ao contar setores", e);
        }
    }
    
    @Override
    public void deleteById(Integer id) {
        try {
            dao.delete(id);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao deletar setor", e);
        }
    }
    
    @Override
    public void delete(Setor entity) {
        deleteById(entity.getId());
    }
    
    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Operação não suportada");
    }
    
    // Métodos específicos de Setor
    
    public Optional<Setor> findByNome(String nome) {
        try {
            Setor setor = dao.buscarPorNome(nome);
            return Optional.ofNullable(setor);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao buscar setor por nome", e);
        }
    }
    
    public boolean existsByNome(String nome, Integer excludeId) {
        try {
            return dao.setorExiste(nome, excludeId);
        } catch (SQLException e) {
            throw new RepositoryException("Erro ao verificar existência de setor", e);
        }
    }
}
