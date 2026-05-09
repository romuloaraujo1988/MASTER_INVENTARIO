package com.inventario.sihcp.mobile.server.dto;

/**
 * Request para sincronização incremental
 * Envia timestamp da última sincronização para receber apenas dados novos/modificados
 */
public class IncrementalSyncRequest {
    
    private Long lastSyncTimestamp;
    private Integer inventarioId;
    private Integer limit;
    private Integer offset;
    
    public IncrementalSyncRequest() {
    }
    
    public IncrementalSyncRequest(Long lastSyncTimestamp, Integer inventarioId) {
        this.lastSyncTimestamp = lastSyncTimestamp;
        this.inventarioId = inventarioId;
    }
    
    // Getters e Setters
    
    public Long getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }
    
    public void setLastSyncTimestamp(Long lastSyncTimestamp) {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }
    
    public Integer getInventarioId() {
        return inventarioId;
    }
    
    public void setInventarioId(Integer inventarioId) {
        this.inventarioId = inventarioId;
    }
    
    public Integer getLimit() {
        return limit != null ? limit : 100; // Default 100
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public Integer getOffset() {
        return offset != null ? offset : 0; // Default 0
    }
    
    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
