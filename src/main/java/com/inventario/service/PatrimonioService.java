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
    
    /**
     * Filtra patrimônios que ainda não foram coletados em um inventário específico
     * 
     * @param patrimonios Lista de patrimônios a filtrar
     * @param idInventario ID do inventário para verificar coletas
     * @param coletaService Serviço de coleta para buscar coletas existentes
     * @return Lista contendo apenas patrimônios pendentes (não coletados)
     */
    public List<Patrimonio> filtrarPatrimoniosPendentes(
            List<Patrimonio> patrimonios, 
            Integer idInventario,
            ColetaService coletaService) {
        
        if (patrimonios == null || patrimonios.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (idInventario == null) {
            logger.warn("ID do inventário é nulo, retornando todos os patrimônios");
            return patrimonios;
        }
        
        List<Patrimonio> pendentes = new ArrayList<>();
        
        try {
            // Buscar todas as coletas do inventário
            List<com.inventario.model.Coleta> coletasInventario = 
                coletaService.buscarPorInventario(idInventario);
            
            // Criar set com IDs dos patrimônios já coletados para busca O(1)
            java.util.Set<Integer> idsColetados = new java.util.HashSet<>();
            for (com.inventario.model.Coleta coleta : coletasInventario) {
                Integer idPatrimonio = coleta.getIdPatrimonio();
                if (idPatrimonio != null && idPatrimonio != 0) {
                    idsColetados.add(idPatrimonio);
                }
            }
            
            // Filtrar apenas os não coletados
            for (Patrimonio p : patrimonios) {
                if (!idsColetados.contains(p.getId())) {
                    pendentes.add(p);
                }
            }
            
            logger.info("Filtrados {} patrimônios pendentes de {} total para inventário {}", 
                pendentes.size(), patrimonios.size(), idInventario);
            
        } catch (Exception e) {
            logger.error("Erro ao filtrar patrimônios pendentes para inventário {}: {}", 
                idInventario, e.getMessage(), e);
            // Em caso de erro, retorna todos para não bloquear o usuário
            return patrimonios;
        }
        
        return pendentes;
    }
    
    /**
     * Busca patrimônios por descrição e filtra apenas os pendentes de coleta
     * 
     * @param termoBusca Termo para buscar na descrição
     * @param idInventario ID do inventário para verificar coletas
     * @param coletaService Serviço de coleta
     * @return Lista de patrimônios pendentes que correspondem à busca
     */
    public List<Patrimonio> buscarPendentesPorDescricao(
            String termoBusca,
            Integer idInventario,
            ColetaService coletaService) {
        
        try {
            // Buscar todos os patrimônios pela descrição
            List<Patrimonio> todosPatrimonios = buscarPorDescricaoAbrangente(termoBusca);
            
            // Filtrar apenas os pendentes
            return filtrarPatrimoniosPendentes(todosPatrimonios, idInventario, coletaService);
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios pendentes por descrição '{}': {}", 
                termoBusca, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtém estatísticas de coleta para uma lista de patrimônios
     * 
     * @param patrimonios Lista de patrimônios
     * @param idInventario ID do inventário
     * @param coletaService Serviço de coleta
     * @return Map com estatísticas (total, pendentes, coletados, percentual)
     */
    public java.util.Map<String, Object> obterEstatisticasColeta(
            List<Patrimonio> patrimonios,
            Integer idInventario,
            ColetaService coletaService) {
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        if (patrimonios == null || patrimonios.isEmpty()) {
            stats.put("total", 0);
            stats.put("pendentes", 0);
            stats.put("coletados", 0);
            stats.put("percentualColetado", 0.0);
            stats.put("percentualPendente", 0.0);
            return stats;
        }
        
        int total = patrimonios.size();
        List<Patrimonio> pendentes = filtrarPatrimoniosPendentes(patrimonios, idInventario, coletaService);
        int qtdPendentes = pendentes.size();
        int qtdColetados = total - qtdPendentes;
        
        double percentualColetado = total > 0 ? (qtdColetados * 100.0 / total) : 0.0;
        double percentualPendente = total > 0 ? (qtdPendentes * 100.0 / total) : 0.0;
        
        stats.put("total", total);
        stats.put("pendentes", qtdPendentes);
        stats.put("coletados", qtdColetados);
        stats.put("percentualColetado", percentualColetado);
        stats.put("percentualPendente", percentualPendente);
        
        return stats;
    }
}