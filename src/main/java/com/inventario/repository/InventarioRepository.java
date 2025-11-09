package com.inventario.repository;

import com.inventario.model.Inventario;
import com.inventario.dao.InventarioDAORefactored;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para Inventario
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Repository
public class InventarioRepository implements com.inventario.repository.Repository<Inventario, Integer> {
    
    private final InventarioDAORefactored dao;
    
    public InventarioRepository() {
        this.dao = new InventarioDAORefactored();
    }
    
    public InventarioRepository(InventarioDAORefactored dao) {
        this.dao = dao;
    }
    
    @Override
    public Inventario save(Inventario entity) {
        try {
            if (entity.getId() > 0) {
                boolean atualizado = dao.atualizar(entity);
                if (!atualizado) {
                    throw new RepositoryException("Falha ao atualizar inventário");
                }
            } else {
                Integer id = dao.inserir(entity);
                if (id == null) {
                    throw new RepositoryException("Falha ao inserir inventário");
                }
                entity.setId(id);
            }
            return entity;
        } catch (Exception e) {
            throw new RepositoryException("Erro ao salvar inventário", e);
        }
    }
    
    @Override
    public Optional<Inventario> findById(Integer id) {
        try {
            Inventario inventario = dao.buscarInventarioPorId(id);
            return Optional.ofNullable(inventario);
        } catch (Exception e) {
            throw new RepositoryException("Erro ao buscar inventário por ID", e);
        }
    }
    
    @Override
    public List<Inventario> findAll() {
        try {
            return dao.listarInventarios();
        } catch (Exception e) {
            throw new RepositoryException("Erro ao listar inventários", e);
        }
    }
    
    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }
    
    @Override
    public long count() {
        return findAll().size();
    }
    
    @Override
    public void deleteById(Integer id) {
        try {
            boolean excluido = dao.excluir(id);
            if (!excluido) {
                throw new RepositoryException("Falha ao excluir inventário");
            }
        } catch (Exception e) {
            throw new RepositoryException("Erro ao deletar inventário", e);
        }
    }
    
    @Override
    public void delete(Inventario entity) {
        deleteById(entity.getId());
    }
    
    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Operação não suportada");
    }
    
    // Métodos específicos de Inventario
    
    public Optional<Inventario> findByStatus(String status) {
        try {
            Inventario inventario = dao.buscarInventarioPorStatus(status);
            return Optional.ofNullable(inventario);
        } catch (Exception e) {
            throw new RepositoryException("Erro ao buscar inventário por status", e);
        }
    }
    
    public List<Inventario> findByNomeContaining(String nome) {
        try {
            return dao.buscarInventariosPorFiltro(nome);
        } catch (Exception e) {
            throw new RepositoryException("Erro ao buscar inventários por nome", e);
        }
    }
    
    public boolean finalizar(Integer id) {
        try {
            return dao.finalizar(id);
        } catch (Exception e) {
            throw new RepositoryException("Erro ao finalizar inventário", e);
        }
    }
}
