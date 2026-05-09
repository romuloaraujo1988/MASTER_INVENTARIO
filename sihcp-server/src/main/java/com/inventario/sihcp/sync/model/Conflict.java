package com.inventario.sihcp.sync.model;

import java.time.LocalDateTime;
import java.util.List;

public class Conflict {
    private EntityType entityType;
    private int entityId;
    private Object sqliteVersion;
    private Object postgresVersion;
    private LocalDateTime sqliteModified;
    private LocalDateTime postgresModified;
    private List<String> conflictingFields;
    
    public Conflict() {}
    
    public Conflict(EntityType entityType, int entityId, Object sqliteVersion, Object postgresVersion) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.sqliteVersion = sqliteVersion;
        this.postgresVersion = postgresVersion;
    }
    
    // Getters and Setters
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    public int getEntityId() { return entityId; }
    public void setEntityId(int entityId) { this.entityId = entityId; }
    public Object getSqliteVersion() { return sqliteVersion; }
    public void setSqliteVersion(Object sqliteVersion) { this.sqliteVersion = sqliteVersion; }
    public Object getPostgresVersion() { return postgresVersion; }
    public void setPostgresVersion(Object postgresVersion) { this.postgresVersion = postgresVersion; }
    public LocalDateTime getSqliteModified() { return sqliteModified; }
    public void setSqliteModified(LocalDateTime sqliteModified) { this.sqliteModified = sqliteModified; }
    public LocalDateTime getPostgresModified() { return postgresModified; }
    public void setPostgresModified(LocalDateTime postgresModified) { this.postgresModified = postgresModified; }
    public List<String> getConflictingFields() { return conflictingFields; }
    public void setConflictingFields(List<String> conflictingFields) { this.conflictingFields = conflictingFields; }
}
