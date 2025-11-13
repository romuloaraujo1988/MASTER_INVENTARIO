package com.inventario.mobile.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisição de registro de coleta mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileColetaRequest {
    
    @NotBlank(message = "Número do patrimônio é obrigatório")
    private String numeroPatrimonio;
    
    // ID do inventário é opcional - se não informado, usa o inventário ativo
    private Integer idInventario;
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Integer usuarioId;
    
    private Integer idSala;
    
    private String localizacaoEncontrada;
    
    @NotBlank(message = "Estado encontrado é obrigatório")
    private String estadoEncontrado;
    
    private String observacaoColeta;
    
    private String dataColeta;  // Aceita tanto ISO 8601 quanto timestamp
    
    private Double latitude;
    
    private Double longitude;
    
    private String fotoPatrimonio;
    
    private Boolean semEtiqueta;
    
    private String descricaoItemSemEtiqueta;
    
    private String categoriaItemSemEtiqueta;
    
    private String deviceId;
    
    private String appVersion;
    
    // Construtores
    public MobileColetaRequest() {
        this.dataColeta = String.valueOf(System.currentTimeMillis());
        this.semEtiqueta = false;
    }
    
    // Getters e Setters
    public String getNumeroPatrimonio() {
        return numeroPatrimonio;
    }
    
    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
    }
    
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public Integer getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public Integer getIdSala() {
        return idSala;
    }
    
    public void setIdSala(Integer idSala) {
        this.idSala = idSala;
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
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getFotoPatrimonio() {
        return fotoPatrimonio;
    }
    
    public void setFotoPatrimonio(String fotoPatrimonio) {
        this.fotoPatrimonio = fotoPatrimonio;
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
}
