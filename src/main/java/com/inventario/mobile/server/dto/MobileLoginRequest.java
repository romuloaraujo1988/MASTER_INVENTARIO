package com.inventario.mobile.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de login mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileLoginRequest {
    
    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;
    
    @NotBlank(message = "Password é obrigatório")
    @Size(min = 4, max = 100, message = "Password deve ter entre 4 e 100 caracteres")
    private String password;
    
    // Device ID e App Version são opcionais
    private String deviceId;
    
    private String appVersion;
    
    // Construtores
    public MobileLoginRequest() {}
    
    public MobileLoginRequest(String username, String password, String deviceId, String appVersion) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
        this.appVersion = appVersion;
    }
    
    // Getters e Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
    
    @Override
    public String toString() {
        return "MobileLoginRequest{" +
                "username='" + username + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }
}