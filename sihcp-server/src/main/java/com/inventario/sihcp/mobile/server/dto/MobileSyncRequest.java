package com.inventario.sihcp.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para requisição de sincronização mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileSyncRequest {
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("lastSyncTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastSyncTime;
    
    @JsonProperty("patrimoniosColetados")
    private List<MobilePatrimonioDTO> patrimoniosColetados;
    
    @JsonProperty("setorId")
    private Long setorId;
    
    @JsonProperty("salaId")
    private Long salaId;
    
    // Construtores
    public MobileSyncRequest() {}
    
    public MobileSyncRequest(String deviceId, Long userId, LocalDateTime lastSyncTime, 
                           List<MobilePatrimonioDTO> patrimoniosColetados) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.lastSyncTime = lastSyncTime;
        this.patrimoniosColetados = patrimoniosColetados;
    }
    
    // Getters e Setters
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }
    
    public void setLastSyncTime(LocalDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
    
    public List<MobilePatrimonioDTO> getPatrimoniosColetados() {
        return patrimoniosColetados;
    }
    
    public void setPatrimoniosColetados(List<MobilePatrimonioDTO> patrimoniosColetados) {
        this.patrimoniosColetados = patrimoniosColetados;
    }
    
    public Long getSetorId() {
        return setorId;
    }
    
    public void setSetorId(Long setorId) {
        this.setorId = setorId;
    }
    
    public Long getSalaId() {
        return salaId;
    }
    
    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }
}