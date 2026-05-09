package com.inventario.sihcp.offline;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gerenciador de status de sincronização
 * Controla o estado de sincronização de diferentes entidades e notifica listeners
 */
public class SyncStatusManager {
    
    private static SyncStatusManager instance;
    
    // Status de sincronização por entidade
    private Map<String, EntitySyncStatus> statusPorEntidade;
    
    // Listeners para mudanças de status
    private CopyOnWriteArrayList<SyncStatusListener> listeners;
    
    // Última sincronização geral
    private LocalDateTime ultimaSincronizacaoGeral;
    
    // Total de itens pendentes
    private int totalItensPendentes;
    
    private SyncStatusManager() {
        this.statusPorEntidade = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.totalItensPendentes = 0;
    }
    
    public static synchronized SyncStatusManager getInstance() {
        if (instance == null) {
            instance = new SyncStatusManager();
        }
        return instance;
    }
    
    /**
     * Registra o status de sincronização de uma entidade
     */
    public void registrarStatus(String entidade, int totalItens, int itensSincronizados, 
                                LocalDateTime ultimaSincronizacao) {
        EntitySyncStatus status = new EntitySyncStatus(
            entidade, 
            totalItens, 
            itensSincronizados, 
            ultimaSincronizacao
        );
        
        statusPorEntidade.put(entidade, status);
        atualizarTotalPendentes();
        notificarListeners();
    }
    
    /**
     * Marca uma entidade como sincronizada
     */
    public void marcarComoSincronizado(String entidade) {
        EntitySyncStatus status = statusPorEntidade.get(entidade);
        if (status != null) {
            status.itensSincronizados = status.totalItens;
            status.ultimaSincronizacao = LocalDateTime.now();
            atualizarTotalPendentes();
            notificarListeners();
        }
    }
    
    /**
     * Adiciona itens pendentes para uma entidade
     */
    public void adicionarItensPendentes(String entidade, int quantidade) {
        EntitySyncStatus status = statusPorEntidade.get(entidade);
        if (status != null) {
            status.totalItens += quantidade;
        } else {
            status = new EntitySyncStatus(entidade, quantidade, 0, null);
            statusPorEntidade.put(entidade, status);
        }
        atualizarTotalPendentes();
        notificarListeners();
    }
    
    /**
     * Remove itens pendentes de uma entidade (após sincronização)
     */
    public void removerItensPendentes(String entidade, int quantidade) {
        EntitySyncStatus status = statusPorEntidade.get(entidade);
        if (status != null) {
            status.itensSincronizados += quantidade;
            if (status.itensSincronizados > status.totalItens) {
                status.itensSincronizados = status.totalItens;
            }
            status.ultimaSincronizacao = LocalDateTime.now();
            atualizarTotalPendentes();
            notificarListeners();
        }
    }
    
    /**
     * Atualiza a última sincronização geral
     */
    public void atualizarUltimaSincronizacaoGeral() {
        this.ultimaSincronizacaoGeral = LocalDateTime.now();
        notificarListeners();
    }
    
    /**
     * Verifica se há dados desatualizados (mais de 24 horas sem sincronizar)
     */
    public boolean temDadosDesatualizados() {
        if (ultimaSincronizacaoGeral == null) {
            return true;
        }
        
        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        return ultimaSincronizacaoGeral.isBefore(limite);
    }
    
    /**
     * Obtém o status de uma entidade específica
     */
    public EntitySyncStatus getStatus(String entidade) {
        return statusPorEntidade.get(entidade);
    }
    
    /**
     * Obtém todos os status
     */
    public Map<String, EntitySyncStatus> getTodosStatus() {
        return new HashMap<>(statusPorEntidade);
    }
    
    /**
     * Obtém o total de itens pendentes de sincronização
     */
    public int getTotalItensPendentes() {
        return totalItensPendentes;
    }
    
    /**
     * Obtém a última sincronização geral
     */
    public LocalDateTime getUltimaSincronizacaoGeral() {
        return ultimaSincronizacaoGeral;
    }
    
    /**
     * Limpa todos os status
     */
    public void limparStatus() {
        statusPorEntidade.clear();
        totalItensPendentes = 0;
        ultimaSincronizacaoGeral = null;
        notificarListeners();
    }
    
    /**
     * Adiciona um listener para mudanças de status
     */
    public void addListener(SyncStatusListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove um listener
     */
    public void removeListener(SyncStatusListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Atualiza o total de itens pendentes
     */
    private void atualizarTotalPendentes() {
        totalItensPendentes = 0;
        for (EntitySyncStatus status : statusPorEntidade.values()) {
            totalItensPendentes += status.getItensPendentes();
        }
    }
    
    /**
     * Notifica todos os listeners sobre mudanças
     */
    private void notificarListeners() {
        for (SyncStatusListener listener : listeners) {
            try {
                listener.onStatusChanged(totalItensPendentes, ultimaSincronizacaoGeral);
            } catch (Exception e) {
                System.err.println("Erro ao notificar listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Interface para listeners de mudanças de status
     */
    public interface SyncStatusListener {
        void onStatusChanged(int totalPendentes, LocalDateTime ultimaSincronizacao);
    }
    
    /**
     * Classe interna para representar o status de sincronização de uma entidade
     */
    public static class EntitySyncStatus {
        private String entidade;
        private int totalItens;
        private int itensSincronizados;
        private LocalDateTime ultimaSincronizacao;
        
        public EntitySyncStatus(String entidade, int totalItens, int itensSincronizados, 
                               LocalDateTime ultimaSincronizacao) {
            this.entidade = entidade;
            this.totalItens = totalItens;
            this.itensSincronizados = itensSincronizados;
            this.ultimaSincronizacao = ultimaSincronizacao;
        }
        
        public String getEntidade() {
            return entidade;
        }
        
        public int getTotalItens() {
            return totalItens;
        }
        
        public int getItensSincronizados() {
            return itensSincronizados;
        }
        
        public int getItensPendentes() {
            return Math.max(0, totalItens - itensSincronizados);
        }
        
        public LocalDateTime getUltimaSincronizacao() {
            return ultimaSincronizacao;
        }
        
        public double getPercentualSincronizado() {
            if (totalItens == 0) return 100.0;
            return (itensSincronizados * 100.0) / totalItens;
        }
        
        public boolean estaSincronizado() {
            return itensSincronizados >= totalItens;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %d/%d (%.1f%%)", 
                entidade, itensSincronizados, totalItens, getPercentualSincronizado());
        }
    }
}
