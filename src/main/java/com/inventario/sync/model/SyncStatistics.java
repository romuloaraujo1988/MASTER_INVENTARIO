package com.inventario.sync.model;

import java.time.LocalDateTime;

public class SyncStatistics {
    private int responsaveisSynced;
    private int salasSynced;
    private int patrimoniosSynced;
    private int coletasSynced;
    private int conflictsResolved;
    private int retries;
    private long totalDurationMs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public SyncStatistics() {
        this.startTime = LocalDateTime.now();
    }
    
    public void finish() {
        this.endTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getResponsaveisSynced() { return responsaveisSynced; }
    public void setResponsaveisSynced(int responsaveisSynced) { this.responsaveisSynced = responsaveisSynced; }
    public int getSalasSynced() { return salasSynced; }
    public void setSalasSynced(int salasSynced) { this.salasSynced = salasSynced; }
    public int getPatrimoniosSynced() { return patrimoniosSynced; }
    public void setPatrimoniosSynced(int patrimoniosSynced) { this.patrimoniosSynced = patrimoniosSynced; }
    public int getColetasSynced() { return coletasSynced; }
    public void setColetasSynced(int coletasSynced) { this.coletasSynced = coletasSynced; }
    public int getConflictsResolved() { return conflictsResolved; }
    public void setConflictsResolved(int conflictsResolved) { this.conflictsResolved = conflictsResolved; }
    public int getRetries() { return retries; }
    public void setRetries(int retries) { this.retries = retries; }
    public long getTotalDurationMs() { return totalDurationMs; }
    public void setTotalDurationMs(long totalDurationMs) { this.totalDurationMs = totalDurationMs; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public void incrementRetries() {
        this.retries++;
    }
}
