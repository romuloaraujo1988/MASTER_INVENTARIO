package com.inventario.sihcp.repository;

import com.inventario.sihcp.model.Coleta;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository para Coleta
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public interface ColetaRepository {
    
    /**
     * Busca coleta por ID
     */
    Optional<Coleta> findById(Integer id);
    
    /**
     * Lista todas as coletas
     */
    List<Coleta> findAll();
    
    /**
     * Lista coletas por inventário
     */
    List<Coleta> findByInventario(Integer idInventario);
    
    /**
     * Lista coletas por sala
     */
    List<Coleta> findBySala(Integer idSala);
    
    /**
     * Lista coletas por usuário
     */
    List<Coleta> findByUsuario(Integer idUsuario);
    
    /**
     * Busca coleta por número de patrimônio
     */
    Optional<Coleta> findByNumeroPatrimonio(String numeroPatrimonio);
    
    /**
     * Salva uma coleta
     */
    Coleta save(Coleta coleta);
    
    /**
     * Exclui uma coleta
     */
    void delete(Integer id);
    
    /**
     * Conta coletas por inventário
     */
    long countByInventario(Integer idInventario);
    
    /**
     * Verifica se patrimônio já foi coletado
     */
    boolean existsByNumeroPatrimonio(String numeroPatrimonio);
}
