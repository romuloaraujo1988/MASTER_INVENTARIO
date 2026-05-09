package com.inventario.sihcp.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para informações do usuário mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileUserInfo {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("nome")
    private String nome;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("setorId")
    private Long setorId;
    
    @JsonProperty("setorNome")
    private String setorNome;
    
    @JsonProperty("perfil")
    private String perfil;
    
    @JsonProperty("ativo")
    private Boolean ativo;
    
    // Construtores
    public MobileUserInfo() {}
    
    public MobileUserInfo(Long id, String username, String nome, String email, 
                         Long setorId, String setorNome, String perfil, Boolean ativo) {
        this.id = id;
        this.username = username;
        this.nome = nome;
        this.email = email;
        this.setorId = setorId;
        this.setorNome = setorNome;
        this.perfil = perfil;
        this.ativo = ativo;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Long getSetorId() {
        return setorId;
    }
    
    public void setSetorId(Long setorId) {
        this.setorId = setorId;
    }
    
    public String getSetorNome() {
        return setorNome;
    }
    
    public void setSetorNome(String setorNome) {
        this.setorNome = setorNome;
    }
    
    public String getPerfil() {
        return perfil;
    }
    
    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}