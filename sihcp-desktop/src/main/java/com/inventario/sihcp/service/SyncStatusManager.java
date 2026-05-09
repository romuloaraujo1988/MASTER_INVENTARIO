package com.inventario.sihcp.service;

import com.inventario.sihcp.offline.OfflineDAO;

import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Gerenciador de status de sincronização
 * Fornece informações sobre o estado de sincronização dos dados
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class SyncStatusManager {
    
    private static final Logger LOGGER = Logger.getLogger(SyncStatusManager.class.getName());
    private static SyncStatusManager instance;
    
    private final OfflineDAO offlineDAO;
    
    /**
     * Status de sincronização de uma entidade
     */
    public enum SyncStatus {
        SYNCED,      // Sincronizado com servidor
        PENDING,     // Pendente de sincronização
        LOCAL_ONLY,  // Apenas local
        CONFLICT     // Conflito detectado
    }
    
    private SyncStatusManager() {
        this.offlineDAO = new OfflineDAO();
    }
    
    /**
     * Obtém a instância singleton
     */
    public static synchronized SyncStatusManager getInstance() {
        if (instance == null) {
            instance = new SyncStatusManager();
        }
        return instance;
    }
    
    /**
     * Obtém o status de sincronização de uma entidade
     * Verifica na tabela sync_control e nas tabelas locais
     * 
     * @param type Tipo da entidade (local_patrimonio, local_coleta, etc)
     * @param id ID da entidade
     * @return Status de sincronização
     */
    public SyncStatus getEntityStatus(String type, int id) {
        try {
            // Primeiro verifica se há operações pendentes na sync_control
            String sqlControl = "SELECT synced, conflict_resolved FROM sync_control " +
                              "WHERE table_name = ? AND record_id = ? " +
                              "ORDER BY timestamp DESC LIMIT 1";
            
            java.sql.Connection conn = offlineDAO.getConnection();
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sqlControl)) {
                stmt.setString(1, type);
                stmt.setInt(2, id);
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        boolean synced = rs.getBoolean("synced");
                        boolean conflictResolved = rs.getBoolean("conflict_resolved");
                        
                        if (!synced && !conflictResolved) {
                            return SyncStatus.PENDING;
                        } else if (!synced && conflictResolved) {
                            return SyncStatus.CONFLICT;
                        }
                    }
                }
            }
            
            // Verifica o sync_status na tabela local
            String sqlLocal = String.format("SELECT sync_status FROM %s WHERE id = ?", type);
            
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sqlLocal)) {
                stmt.setInt(1, id);
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String syncStatus = rs.getString("sync_status");
                        
                        if ("PENDING".equals(syncStatus)) {
                            return SyncStatus.PENDING;
                        } else if ("SYNCED".equals(syncStatus)) {
                            return SyncStatus.SYNCED;
                        } else if ("CONFLICT".equals(syncStatus)) {
                            return SyncStatus.CONFLICT;
                        } else {
                            return SyncStatus.LOCAL_ONLY;
                        }
                    }
                }
            }
            
            // Se não encontrou, assume que está sincronizado
            return SyncStatus.SYNCED;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao obter status da entidade: " + type + " #" + id, e);
            return SyncStatus.LOCAL_ONLY;
        }
    }
    
    /**
     * Obtém a quantidade de coletas pendentes de sincronização
     * Conta registros com sync_status = 'PENDING' na tabela local_coleta
     * 
     * @return Quantidade de coletas pendentes
     */
    public int getPendingCount() {
        try {
            String sql = "SELECT COUNT(*) as total FROM local_coleta WHERE sync_status = 'PENDING'";
            
            java.sql.Connection conn = offlineDAO.getConnection();
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
            
            return 0;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao contar pendências", e);
            return 0;
        }
    }
    
    /**
     * Obtém a quantidade de entidades pendentes por tipo
     * 
     * @param type Tipo da entidade (local_patrimonio, local_coleta, etc)
     * @return Quantidade de entidades pendentes
     */
    public int getPendingCountByType(String type) {
        try {
            String sql = String.format("SELECT COUNT(*) as total FROM %s WHERE sync_status = 'PENDING'", type);
            
            java.sql.Connection conn = offlineDAO.getConnection();
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
            
            return 0;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao contar pendências do tipo: " + type, e);
            return 0;
        }
    }
    
    /**
     * Obtém o timestamp da última sincronização
     */
    public LocalDateTime getLastSyncTime() {
        try {
            String lastSync = offlineDAO.obterMetadado("last_sync_timestamp");
            if (lastSync != null && !lastSync.isEmpty()) {
                return LocalDateTime.parse(lastSync);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao obter última sincronização", e);
        }
        return null;
    }
    
    /**
     * Verifica se os dados estão desatualizados (>7 dias)
     */
    public boolean isDataOutdated() {
        LocalDateTime lastSync = getLastSyncTime();
        if (lastSync == null) {
            return true;
        }
        
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return lastSync.isBefore(sevenDaysAgo);
    }
    
    /**
     * Marca uma entidade como sincronizada
     * Atualiza o sync_status na tabela local e registra na sync_control
     * 
     * @param type Tipo da entidade (local_patrimonio, local_coleta, etc)
     * @param id ID da entidade
     */
    public void markAsSynced(String type, int id) {
        try {
            // Atualiza o status na tabela local
            boolean updated = offlineDAO.atualizarStatusSync(type, id, "SYNCED");
            
            if (updated) {
                // Atualiza também na sync_control
                String sql = """
                    UPDATE sync_control 
                    SET synced = TRUE, conflict_resolved = TRUE 
                    WHERE table_name = ? AND record_id = ?
                """;
                
                java.sql.Connection conn = offlineDAO.getConnection();
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, type);
                    stmt.setInt(2, id);
                    stmt.executeUpdate();
                }
                
                LOGGER.info(String.format("✅ Entidade marcada como sincronizada: %s #%d", type, id));
            } else {
                LOGGER.warning(String.format("⚠️ Falha ao marcar como sincronizada: %s #%d", type, id));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao marcar como sincronizado: " + type + " #" + id, e);
        }
    }
    
    /**
     * Marca uma entidade como pendente de sincronização
     * Atualiza o sync_status na tabela local
     * 
     * @param type Tipo da entidade (local_patrimonio, local_coleta, etc)
     * @param id ID da entidade
     */
    public void markAsPending(String type, int id) {
        try {
            // Atualiza o status na tabela local
            boolean updated = offlineDAO.atualizarStatusSync(type, id, "PENDING");
            
            if (updated) {
                LOGGER.info(String.format("⏳ Entidade marcada como pendente: %s #%d", type, id));
            } else {
                LOGGER.warning(String.format("⚠️ Falha ao marcar como pendente: %s #%d", type, id));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao marcar como pendente: " + type + " #" + id, e);
        }
    }
    
    /**
     * Obtém estatísticas detalhadas de sincronização
     * 
     * @return Mapa com estatísticas por tipo de entidade
     */
    public java.util.Map<String, Integer> getDetailedStats() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        
        String[] types = {"local_patrimonio", "local_coleta", "local_inventario", "local_participante_inventario"};
        
        for (String type : types) {
            stats.put(type + "_pending", getPendingCountByType(type));
        }
        
        return stats;
    }
    
    /**
     * Obtém informações de status formatadas
     * Inclui estatísticas detalhadas por tipo de entidade
     * 
     * @return String formatada com informações de sincronização
     */
    public String getStatusInfo() {
        StringBuilder info = new StringBuilder();
        info.append("╔════════════════════════════════════════╗\n");
        info.append("║  STATUS DE SINCRONIZAÇÃO OFFLINE      ║\n");
        info.append("╚════════════════════════════════════════╝\n\n");
        
        // Última sincronização
        info.append("📅 Última sincronização: ");
        LocalDateTime lastSync = getLastSyncTime();
        if (lastSync != null) {
            info.append(lastSync.toString()).append("\n");
        } else {
            info.append("Nunca sincronizado\n");
        }
        
        // Status dos dados
        info.append("⏰ Dados desatualizados: ").append(isDataOutdated() ? "⚠️ Sim (>7 dias)" : "✅ Não").append("\n\n");
        
        // Estatísticas por tipo
        info.append("📊 PENDÊNCIAS POR TIPO:\n");
        info.append("─────────────────────────────────────\n");
        
        java.util.Map<String, Integer> stats = getDetailedStats();
        
        info.append(String.format("  • Coletas:      %3d pendentes\n", stats.getOrDefault("local_coleta_pending", 0)));
        info.append(String.format("  • Patrimônios:  %3d pendentes\n", stats.getOrDefault("local_patrimonio_pending", 0)));
        info.append(String.format("  • Inventários:  %3d pendentes\n", stats.getOrDefault("local_inventario_pending", 0)));
        info.append(String.format("  • Participantes:%3d pendentes\n", stats.getOrDefault("local_participante_inventario_pending", 0)));
        
        // Total
        int total = stats.values().stream().mapToInt(Integer::intValue).sum();
        info.append("─────────────────────────────────────\n");
        info.append(String.format("  TOTAL:         %3d pendentes\n", total));
        
        if (total > 0) {
            info.append("\n⚠️ Sincronize os dados quando possível!\n");
        } else {
            info.append("\n✅ Todos os dados estão sincronizados!\n");
        }
        
        return info.toString();
    }
}
