package com.inventario.sihcp.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface base para o padrão Repository
 * Define operações CRUD genéricas
 * 
 * @param <T> Tipo da entidade
 * @param <ID> Tipo do identificador
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public interface Repository<T, ID> {
    
    /**
     * Salva uma entidade (insert ou update)
     * @param entity Entidade a ser salva
     * @return Entidade salva
     */
    T save(T entity);
    
    /**
     * Busca uma entidade por ID
     * @param id Identificador
     * @return Optional contendo a entidade se encontrada
     */
    Optional<T> findById(ID id);
    
    /**
     * Busca todas as entidades
     * @return Lista de entidades
     */
    List<T> findAll();
    
    /**
     * Verifica se uma entidade existe
     * @param id Identificador
     * @return true se existe
     */
    boolean existsById(ID id);
    
    /**
     * Conta o número de entidades
     * @return Número total de entidades
     */
    long count();
    
    /**
     * Deleta uma entidade por ID
     * @param id Identificador
     */
    void deleteById(ID id);
    
    /**
     * Deleta uma entidade
     * @param entity Entidade a ser deletada
     */
    void delete(T entity);
    
    /**
     * Deleta todas as entidades
     */
    void deleteAll();
}
