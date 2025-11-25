package com.inventario.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para resposta de coleta mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileColetaResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("numeroPatrimonio")
    private String numeroPatrimonio;
    
    @JsonProperty("descricaoPatrimonio")
    private String descricaoPatrimonio;
    
    @JsonProperty("idInventario")
    private Integer idInventario;
    
    @JsonProperty("nomeInventario")
    private String nomeInventario;
    
    @JsonProperty("idSala")
    private Integer idSala;
    
    @JsonProperty("nomeSala")
    private String nomeSala;
    
    @JsonProperty("localizacaoEncontrada")
    private String localizacaoEncontrada;
    
    @JsonProperty("estadoEncontrado")
    private String estadoEncontrado;
    
    @JsonProperty("observacaoColeta")
    private String observacaoColeta;
    
    @JsonProperty("dataColeta")
    private String dataColeta;  // String formatada para compatibilidade com app Android
    
    @JsonProperty("statusColeta")
    private String statusColeta;
    
    @JsonProperty("nomeColetor")
    private String nomeColetor;
    
    @JsonProperty("usuarioId")
    private Integer usuarioId;  // ID do usuário coletor
    
    @JsonProperty("patrimonioId")
    private Integer patrimonioId;  // ID do patrimônio coletado
    
    @JsonProperty("semEtiqueta")
    private Boolean semEtiqueta;
    
    @JsonProperty("descricaoItemSemEtiqueta")
    private String descricaoItemSemEtiqueta;
    
    @JsonProperty("categoriaItemSemEtiqueta")
    private String categoriaItemSemEtiqueta;
    
    @JsonProperty("sincronizado")
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
    
    public String getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(String dataColeta) {
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
