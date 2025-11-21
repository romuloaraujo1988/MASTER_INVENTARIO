package com.inventario.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para patrimônio mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobilePatrimonioDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("codigo")
    private String codigo;
    
    @JsonProperty("descricao")
    private String descricao;
    
    @JsonProperty("marca")
    private String marca;
    
    @JsonProperty("modelo")
    private String modelo;
    
    @JsonProperty("numeroSerie")
    private String numeroSerie;
    
    @JsonProperty("estado")
    private String estado;
    
    @JsonProperty("valor")
    private Double valor;
    
    @JsonProperty("setorId")
    private Long setorId;
    
    @JsonProperty("setorNome")
    private String setorNome;
    
    @JsonProperty("salaId")
    private Long salaId;
    
    @JsonProperty("salaNome")
    private String salaNome;
    
    @JsonProperty("responsavelId")
    private Long responsavelId;
    
    @JsonProperty("responsavelNome")
    private String responsavelNome;
    
    @JsonProperty("qrCode")
    private String qrCode;
    
    @JsonProperty("coletado")
    private Boolean coletado = false;
    
    @JsonProperty("dataColeta")
    private String dataColeta;
    
    @JsonProperty("observacoes")
    private String observacoes;
    
    @JsonProperty("coletadoPor")
    private String coletadoPor;
    
    @JsonProperty("dataColetaFormatada")
    private String dataColetaFormatada;
    
    @JsonProperty("ed")
    private String ed;
    
    @JsonProperty("numeroNotaFiscal")
    private String numeroNotaFiscal;
    
    @JsonProperty("fornecedor")
    private String fornecedor;
    
    // Construtores
    public MobilePatrimonioDTO() {}
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public String getNumeroSerie() {
        return numeroSerie;
    }
    
    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Double getValor() {
        return valor;
    }
    
    public void setValor(Double valor) {
        this.valor = valor;
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
    
    public Long getSalaId() {
        return salaId;
    }
    
    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }
    
    public String getSalaNome() {
        return salaNome;
    }
    
    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }
    
    public Long getResponsavelId() {
        return responsavelId;
    }
    
    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
    }
    
    public String getResponsavelNome() {
        return responsavelNome;
    }
    
    public void setResponsavelNome(String responsavelNome) {
        this.responsavelNome = responsavelNome;
    }
    
    public String getQrCode() {
        return qrCode;
    }
    
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
    
    public Boolean getColetado() {
        return coletado;
    }
    
    public void setColetado(Boolean coletado) {
        this.coletado = coletado;
    }
    
    public String getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(String dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public String getColetadoPor() {
        return coletadoPor;
    }
    
    public void setColetadoPor(String coletadoPor) {
        this.coletadoPor = coletadoPor;
    }
    
    public String getDataColetaFormatada() {
        return dataColetaFormatada;
    }
    
    public void setDataColetaFormatada(String dataColetaFormatada) {
        this.dataColetaFormatada = dataColetaFormatada;
    }
    
    public String getEd() {
        return ed;
    }
    
    public void setEd(String ed) {
        this.ed = ed;
    }
    
    public String getNumeroNotaFiscal() {
        return numeroNotaFiscal;
    }
    
    public void setNumeroNotaFiscal(String numeroNotaFiscal) {
        this.numeroNotaFiscal = numeroNotaFiscal;
    }
    
    public String getFornecedor() {
        return fornecedor;
    }
    
    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }
}