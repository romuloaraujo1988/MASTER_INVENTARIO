package com.inventario.sihcp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventario.sihcp.event.DashboardEvent;
import com.inventario.sihcp.event.DashboardEventBus;
import com.inventario.sihcp.event.DashboardEventType;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.repository.PatrimonioRepository;

/**
 * Serviço para operações com Patrimônio
 * REFATORADO: Usa Repository Pattern
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@Service
@Transactional
public class PatrimonioService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatrimonioService.class);
    
    private final PatrimonioRepository patrimonioRepository;
    
    @Autowired
    public PatrimonioService(PatrimonioRepository patrimonioRepository) {
        this.patrimonioRepository = patrimonioRepository;
    }
    
    /**
     * Busca patrimônio por ID
     * @param id ID do patrimônio
     * @return Optional contendo o patrimônio se encontrado
     */
    public Optional<Patrimonio> buscarPorId(Integer id) {
        logger.debug("Buscando patrimônio por ID: {}", id);
        return patrimonioRepository.findById(id);
    }
    
    /**
     * Busca patrimônio por ID (compatibilidade com código antigo)
     * @deprecated Use buscarPorId(Integer) que retorna Optional
     */
    @Deprecated
    public Patrimonio buscarPorId(Long id) {
        return buscarPorId(id.intValue()).orElse(null);
    }
    
    /**
     * Busca patrimônio por número
     * @param numero Número do patrimônio
     * @return Optional contendo o patrimônio se encontrado
     */
    public Optional<Patrimonio> buscarPorNumero(String numero) {
        logger.debug("Buscando patrimônio por número: {}", numero);
        return patrimonioRepository.findByNumero(numero);
    }
    
    /**
     * Salva ou atualiza um patrimônio
     * @param patrimonio Patrimônio a ser salvo
     * @return Patrimônio salvo
     */
    public Patrimonio salvar(Patrimonio patrimonio) {
        logger.info("Salvando patrimônio: {}", patrimonio.getNumero());
        
        // Validações básicas
        if (patrimonio.getNumero() == null || patrimonio.getNumero().isEmpty()) {
            throw new IllegalArgumentException("Número do patrimônio é obrigatório");
        }
        
        boolean isNovo = patrimonio.getId() == 0;
        Patrimonio salvo = patrimonioRepository.save(patrimonio);
        
        // Publicar evento para atualização da dashboard
        publicarEventoPatrimonioAtualizado(salvo, isNovo ? "CRIADO" : "ATUALIZADO");
        
        return salvo;
    }
    
    /**
     * Publica evento de patrimônio atualizado para a dashboard.
     * Este método não lança exceções para não afetar a transação principal.
     */
    private void publicarEventoPatrimonioAtualizado(Patrimonio patrimonio, String operacao) {
        try {
            DashboardEvent event = DashboardEvent.builder(DashboardEventType.PATRIMONIO_ATUALIZADO)
                .source("PatrimonioService")
                .addMetadata("patrimonioId", patrimonio.getId())
                .addMetadata("numero", patrimonio.getNumero())
                .addMetadata("operacao", operacao)
                .addAffectedEntityId(patrimonio.getId())
                .build();
            
            DashboardEventBus.getInstance().publish(event);
            logger.debug("Evento PATRIMONIO_ATUALIZADO publicado para patrimônio: {}", patrimonio.getNumero());
        } catch (Exception e) {
            logger.warn("Falha ao publicar evento de patrimônio atualizado: {}", e.getMessage());
        }
    }
    
    /**
     * Lista todos os patrimônios
     * @return Lista de patrimônios (nunca null)
     */
    public List<Patrimonio> listarTodos() {
        logger.debug("Listando todos os patrimônios");
        return patrimonioRepository.findAll();
    }
    
    /**
     * Lista patrimônios por sala
     * @param idSala ID da sala
     * @return Lista de patrimônios (nunca null)
     */
    public List<Patrimonio> listarPorSala(Integer idSala) {
        logger.debug("Listando patrimônios da sala: {}", idSala);
        return patrimonioRepository.findBySala(idSala);
    }
    
    /**
     * Busca patrimônios por descrição
     * @param descricao Descrição para buscar
     * @return Lista de patrimônios (nunca null)
     */
    public List<Patrimonio> buscarPorDescricao(String descricao) {
        logger.debug("Buscando patrimônios por descrição: {}", descricao);
        return patrimonioRepository.findByDescricao(descricao);
    }
    
    /**
     * Exclui um patrimônio
     * @param id ID do patrimônio
     */
    public void excluir(Integer id) {
        logger.info("Excluindo patrimônio: {}", id);
        
        // Verificar se existe
        Optional<Patrimonio> patrimonio = buscarPorId(id);
        if (patrimonio.isEmpty()) {
            throw new IllegalArgumentException("Patrimônio não encontrado: " + id);
        }
        
        patrimonioRepository.delete(id);
    }
    
    /**
     * Conta total de patrimônios
     * @return Quantidade de patrimônios
     */
    public long contarTotal() {
        return patrimonioRepository.count();
    }
    
    /**
     * Verifica se existe patrimônio com o número informado
     * @param numero Número do patrimônio
     * @return true se existe
     */
    public boolean existePorNumero(String numero) {
        return patrimonioRepository.existsByNumero(numero);
    }
    
    /**
     * Busca patrimônios atualizados após uma data específica
     * Para implementação futura com controle de timestamps
     */
    public List<Patrimonio> buscarAtualizadosApos(LocalDateTime lastSync, Long setorId, Long salaId) {
        // Por enquanto, retorna todos os patrimônios
        // TODO: Implementar filtro por data de atualização quando campo for adicionado
        List<Patrimonio> todos = listarTodos();
        
        // Filtrar por sala se especificado
        if (salaId != null) {
            return todos.stream()
                .filter(p -> p.getIdSala() == salaId.intValue())
                .collect(java.util.stream.Collectors.toList());
        }
        
        return todos;
    }
    
    /**
     * Busca patrimônios criados após uma data específica
     * Para implementação futura com controle de timestamps
     */
    public List<Patrimonio> buscarCriadosApos(LocalDateTime lastSync, Long setorId, Long salaId) {
        // Por enquanto, retorna lista vazia
        // TODO: Implementar filtro por data de criação quando campo for adicionado
        return new ArrayList<>();
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
            com.inventario.sihcp.service.ColetaService coletaService) {
        
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
            List<com.inventario.sihcp.model.Coleta> coletasInventario = 
                coletaService.buscarPorInventario(idInventario);
            
            // Criar set com IDs dos patrimônios já coletados para busca O(1)
            Set<Integer> idsColetados = new HashSet<>();
            for (com.inventario.sihcp.model.Coleta coleta : coletasInventario) {
                int idPatrimonio = coleta.getIdPatrimonio();
                if (idPatrimonio != 0) {
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
            com.inventario.sihcp.service.ColetaService coletaService) {
        
        // Buscar todos os patrimônios pela descrição
        List<Patrimonio> todosPatrimonios = buscarPorDescricao(termoBusca);
        
        // Filtrar apenas os pendentes
        return filtrarPatrimoniosPendentes(todosPatrimonios, idInventario, coletaService);
    }
    
    /**
     * Obtém estatísticas de coleta para uma lista de patrimônios
     * 
     * @param patrimonios Lista de patrimônios
     * @param idInventario ID do inventário
     * @param coletaService Serviço de coleta
     * @return Map com estatísticas (total, pendentes, coletados, percentual)
     */
    public Map<String, Object> obterEstatisticasColeta(
            List<Patrimonio> patrimonios,
            Integer idInventario,
            com.inventario.sihcp.service.ColetaService coletaService) {
        
        Map<String, Object> stats = new HashMap<>();
        
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