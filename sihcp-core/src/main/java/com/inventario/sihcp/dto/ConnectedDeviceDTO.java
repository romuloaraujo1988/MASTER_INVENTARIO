package com.inventario.sihcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para representar um dispositivo conectado ao servidor mobile
 * Usado para comunicação entre MobileMonitorFrame e MobileConnectionController
 */
public class ConnectedDeviceDTO {
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("deviceInfo")
    private String deviceInfo;
    
    @JsonProperty("androidVersion")
    private String androidVersion;
    
    @JsonProperty("appVersion")
    private String appVersion;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("hostname")
    private String hostname;
    
    @JsonProperty("connectedAt")
    private String connectedAt;
    
    @JsonProperty("lastHeartbeat")
    private String lastHeartbeat;
    
    @JsonProperty("connectionDuration")
    private String connectionDuration;
    
    @JsonProperty("requestCount")
    private int requestCount;
    
    @JsonProperty("isActive")
    private boolean isActive;
    
    // Construtores
    public ConnectedDeviceDTO() {
    }
    
    public ConnectedDeviceDTO(String deviceId, String username, String deviceInfo) {
        this.deviceId = deviceId;
        this.username = username;
        this.deviceInfo = deviceInfo;
    }
    
    // Getters e Setters
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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
    
    public String getAndroidVersion() {
        return androidVersion;
    }
    
    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
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
    
    public String getConnectedAt() {
        return connectedAt;
    }
    
    public void setConnectedAt(String connectedAt) {
        this.connectedAt = connectedAt;
    }
    
    public String getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(String lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public String getConnectionDuration() {
        return connectionDuration;
    }
    
    public void setConnectionDuration(String connectionDuration) {
        this.connectionDuration = connectionDuration;
    }
    
    public int getRequestCount() {
        return requestCount;
    }
    
    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public String toString() {
        return "ConnectedDeviceDTO{" +
                "deviceId='" + deviceId + '\'' +
                ", username='" + username + '\'' +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
