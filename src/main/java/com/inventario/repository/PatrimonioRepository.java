package com.inventario.repository;

import com.inventario.model.Patrimonio;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository para Patrimônio
 * Abstrai a camada de persistência
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public interface PatrimonioRepository {
    
    /**
     * Busca patrimônio por ID
     * @param id ID do patrimônio
     * @return Optional contendo o patrimônio se encontrado
     */
    Optional<Patrimonio> findById(Integer id);
    
    /**
     * Busca patrimônio por número
     * @param numero Número do patrimônio
     * @return Optional contendo o patrimônio se encontrado
     */
    Optional<Patrimonio> findByNumero(String numero);
    
    /**
     * Lista todos os patrimônios
     * @return Lista de patrimônios (nunca null)
     */
    List<Patrimonio> findAll();
    
    /**
     * Lista patrimônios por sala
     * @param idSala ID da sala
     * @return Lista de patrimônios (nunca null)
     */
    List<Patrimonio> findBySala(Integer idSala);
    
    /**
     * Lista patrimônios por descrição (busca parcial)
     * @param descricao Descrição para buscar
     * @return Lista de patrimônios (nunca null)
     */
    List<Patrimonio> findByDescricao(String descricao);
    
    /**
     * Salva um patrimônio (insert ou update)
     * @param patrimonio Patrimônio a ser salvo
     * @return Patrimônio salvo com ID atualizado
     */
    Patrimonio save(Patrimonio patrimonio);
    
    /**
     * Exclui um patrimônio
     * @param id ID do patrimônio a ser excluído
     */
    void delete(Integer id);
    
    /**
     * Conta total de patrimônios
     * @return Quantidade de patrimônios
     */
    long count();
    
    /**
     * Verifica se existe patrimônio com o número informado
     * @param numero Número do patrimônio
     * @return true se existe
     */
    boolean existsByNumero(String numero);
}
