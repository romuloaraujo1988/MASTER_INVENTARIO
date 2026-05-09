package com.inventario.sihcp.sync.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class BackupInfo {
    private Path filePath;
    private LocalDateTime createdAt;
    private long sizeBytes;
    private String checksum;
    
    public BackupInfo() {}
    
    public BackupInfo(Path filePath, LocalDateTime createdAt, long sizeBytes) {
        this.filePath = filePath;
        this.createdAt = createdAt;
        this.sizeBytes = sizeBytes;
    }
    
    // Getters and Setters
    public Path getFilePath() { return filePath; }
    public void setFilePath(Path filePath) { this.filePath = filePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
}
