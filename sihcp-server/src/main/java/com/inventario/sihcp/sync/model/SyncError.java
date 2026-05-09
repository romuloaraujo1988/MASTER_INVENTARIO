package com.inventario.sihcp.sync.model;

/**
 * Representa um erro ocorrido durante sincronização
 */
public class SyncError {
    private int entityId;
    private String entityType;
    private String message;
    private String stackTrace;
    
    public SyncError() {
    }
    
    public SyncError(int entityId, String message) {
        this.entityId = entityId;
        this.message = message;
    }
    
    public SyncError(int entityId, String entityType, String message) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.message = message;
    }
    
    // Getters and Setters
    public int getEntityId() {
        return entityId;
    }
    
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}
