package com.inventario.mobile.server.dto;

import java.time.LocalDateTime;

/**
 * DTO para resposta de coleta mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileColetaResponse {
    
    private Long id;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    private Integer idInventario;
    private String nomeInventario;
    private Integer idSala;
    private String nomeSala;
    private String localizacaoEncontrada;
    private String estadoEncontrado;
    private String observacaoColeta;
    private LocalDateTime dataColeta;
    private String statusColeta;
    private String nomeColetor;
    private Integer usuarioId;  // ID do usuário coletor
    private Integer patrimonioId;  // ID do patrimônio coletado
    private Boolean semEtiqueta;
    private String descricaoItemSemEtiqueta;
    private String categoriaItemSemEtiqueta;
    private Boolean sincronizado;
    
    // Construtores
    public MobileColetaResponse() {
        this.sincronizado = true;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNumeroPatrimonio() {
        return numeroPatrimonio;
    }
    
    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
    }
    
    public String getDescricaoPatrimonio() {
        return descricaoPatrimonio;
    }
    
    public void setDescricaoPatrimonio(String descricaoPatrimonio) {
        this.descricaoPatrimonio = descricaoPatrimonio;
    }
    
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public String getNomeInventario() {
        return nomeInventario;
    }
    
    public void setNomeInventario(String nomeInventario) {
        this.nomeInventario = nomeInventario;
    }
    
    public Integer getIdSala() {
        return idSala;
    }
    
    public void setIdSala(Integer idSala) {
        this.idSala = idSala;
    }
    
    public String getNomeSala() {
        return nomeSala;
    }
    
    public void setNomeSala(String nomeSala) {
        this.nomeSala = nomeSala;
    }
    
    public String getLocalizacaoEncontrada() {
        return localizacaoEncontrada;
    }
    
    public void setLocalizacaoEncontrada(String localizacaoEncontrada) {
        this.localizacaoEncontrada = localizacaoEncontrada;
    }
    
    public String getEstadoEncontrado() {
        return estadoEncontrado;
    }
    
    public void setEstadoEncontrado(String estadoEncontrado) {
        this.estadoEncontrado = estadoEncontrado;
    }
    
    public String getObservacaoColeta() {
        return observacaoColeta;
    }
    
    public void setObservacaoColeta(String observacaoColeta) {
        this.observacaoColeta = observacaoColeta;
    }
    
    public LocalDateTime getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(LocalDateTime dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public String getStatusColeta() {
        return statusColeta;
    }
    
    public void setStatusColeta(String statusColeta) {
        this.statusColeta = statusColeta;
    }
    
    public String getNomeColetor() {
        return nomeColetor;
    }
    
    public void setNomeColetor(String nomeColetor) {
        this.nomeColetor = nomeColetor;
    }
    
    public Integer getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public Integer getPatrimonioId() {
        return patrimonioId;
    }
    
    public void setPatrimonioId(Integer patrimonioId) {
        this.patrimonioId = patrimonioId;
    }
    
    public Boolean getSemEtiqueta() {
        return semEtiqueta;
    }
    
    public void setSemEtiqueta(Boolean semEtiqueta) {
        this.semEtiqueta = semEtiqueta;
    }
    
    public String getDescricaoItemSemEtiqueta() {
        return descricaoItemSemEtiqueta;
    }
    
    public void setDescricaoItemSemEtiqueta(String descricaoItemSemEtiqueta) {
        this.descricaoItemSemEtiqueta = descricaoItemSemEtiqueta;
    }
    
    public String getCategoriaItemSemEtiqueta() {
        return categoriaItemSemEtiqueta;
    }
    
    public void setCategoriaItemSemEtiqueta(String categoriaItemSemEtiqueta) {
        this.categoriaItemSemEtiqueta = categoriaItemSemEtiqueta;
    }
    
    public Boolean getSincronizado() {
        return sincronizado;
    }
    
    public void setSincronizado(Boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
}
