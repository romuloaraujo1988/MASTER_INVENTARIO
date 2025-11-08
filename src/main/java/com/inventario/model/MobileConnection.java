package com.inventario.model;

import java.time.LocalDateTime;

/**
 * Modelo para representar uma conexão mobile ativa
 * Armazena informações sobre dispositivos mobile conectados ao servidor
 * 
 * @author Sistema de Inventário
 * @version 1.2.0
 */
public class MobileConnection {
    
    private String sessionId;
    private String ipAddress;
    private String hostname;
    private String username;
    private String deviceInfo;
    private String appVersion;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime connectedAt;
    private boolean isActive;
    
    // Construtores
    public MobileConnection() {
        this.connectedAt = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
        this.isActive = true;
    }
    
    public MobileConnection(String sessionId, String ipAddress, String hostname, String username) {
        this();
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.hostname = hostname;
        this.username = username;
    }
    
    // Getters e Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
    
    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }
    
    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    /**
     * Atualiza o timestamp do último heartbeat
     */
    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }
    
    /**
     * Verifica se a conexão está expirada (sem heartbeat por mais de 5 minutos)
     */
    public boolean isExpired() {
        return lastHeartbeat.isBefore(LocalDateTime.now().minusMinutes(5));
    }
    
    /**
     * Retorna o tempo de conexão em formato legível
     */
    public String getConnectionDuration() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(connectedAt, now).toMinutes();
        
        if (minutes < 60) {
            return minutes + " min";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "min";
        }
    }
    
    @Override
    public String toString() {
        return "MobileConnection{" +
                "sessionId='" + sessionId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", hostname='" + hostname + '\'' +
                ", username='" + username + '\'' +
                ", lastHeartbeat=" + lastHeartbeat +
                ", isActive=" + isActive +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MobileConnection that = (MobileConnection) obj;
        return sessionId != null ? sessionId.equals(that.sessionId) : that.sessionId == null;
    }
    
    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }
}