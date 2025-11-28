package com.inventario.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para resposta de login mobile
 * 
 * @author Sistema de Inventário
 * @version 1.1.0 - Adicionado inventário ativo
 */
public class MobileLoginResponse {
    
    @JsonProperty("accessToken")
    private String accessToken;
    
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    @JsonProperty("expiresIn")
    private Long expiresIn;
    
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";
    
    @JsonProperty("user")
    private MobileUserInfo user;
    
    /**
     * Informações do inventário ativo (em andamento)
     * Retornado automaticamente no login para o app salvar localmente
     */
    @JsonProperty("inventarioAtivo")
    private MobileInventarioInfo inventarioAtivo;
    
    // Construtores
    public MobileLoginResponse() {}
    
    public MobileLoginResponse(String accessToken, String refreshToken, Long expiresIn, MobileUserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    public MobileLoginResponse(String accessToken, String refreshToken, Long expiresIn, 
                               MobileUserInfo user, MobileInventarioInfo inventarioAtivo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
        this.inventarioAtivo = inventarioAtivo;
    }
    
    // Getters e Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public MobileUserInfo getUser() {
        return user;
    }
    
    public void setUser(MobileUserInfo user) {
        this.user = user;
    }
    
    public MobileInventarioInfo getInventarioAtivo() {
        return inventarioAtivo;
    }
    
    public void setInventarioAtivo(MobileInventarioInfo inventarioAtivo) {
        this.inventarioAtivo = inventarioAtivo;
    }
}