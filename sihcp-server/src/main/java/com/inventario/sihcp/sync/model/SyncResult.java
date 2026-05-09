package com.inventario.sihcp.sync.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de uma operação de sincronização
 */
public class SyncResult {
    private boolean success;
    private int totalProcessed;
    private int totalSuccess;
    private int totalFailed;
    private long durationMs;
    private List<SyncError> errors;
    private String checksum;
    private LocalDateTime timestamp;
    
    public SyncResult() {
        this.errors = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final SyncResult result = new SyncResult();
        
        public Builder success(boolean success) {
            result.success = success;
            return this;
        }
        
        public Builder totalProcessed(int totalProcessed) {
            result.totalProcessed = totalProcessed;
            return this;
        }
        
        public Builder totalSuccess(int totalSuccess) {
            result.totalSuccess = totalSuccess;
            return this;
        }
        
        public Builder totalFailed(int totalFailed) {
            result.totalFailed = totalFailed;
            return this;
        }
        
        public Builder durationMs(long durationMs) {
            result.durationMs = durationMs;
            return this;
        }
        
        public Builder addError(SyncError error) {
            result.errors.add(error);
            return this;
        }
        
        public Builder errors(List<SyncError> errors) {
            result.errors = new ArrayList<>(errors);
            return this;
        }
        
        public Builder checksum(String checksum) {
            result.checksum = checksum;
            return this;
        }
        
        public SyncResult build() {
            return result;
        }
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public int getTotalProcessed() {
        return totalProcessed;
    }
    
    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }
    
    public int getTotalSuccess() {
        return totalSuccess;
    }
    
    public void setTotalSuccess(int totalSuccess) {
        this.totalSuccess = totalSuccess;
    }
    
    public int getTotalFailed() {
        return totalFailed;
    }
    
    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
    
    public List<SyncError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<SyncError> errors) {
        this.errors = errors;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
