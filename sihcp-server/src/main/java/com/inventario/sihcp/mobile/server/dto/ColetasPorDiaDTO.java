package com.inventario.sihcp.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para dados de coletas por dia
 * Usado no gráfico de evolução
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ColetasPorDiaDTO {
    
    @JsonProperty("data")
    private String data;
    
    @JsonProperty("quantidade")
    private Integer quantidade;
    
    @JsonProperty("coletoresAtivos")
    private Integer coletoresAtivos;
    
    @JsonProperty("dataFormatada")
    private String dataFormatada;
    
    // Construtores
    public ColetasPorDiaDTO() {}
    
    public ColetasPorDiaDTO(String data, Integer quantidade, Integer coletoresAtivos, String dataFormatada) {
        this.data = data;
        this.quantidade = quantidade;
        this.coletoresAtivos = coletoresAtivos;
        this.dataFormatada = dataFormatada;
    }
    
    // Getters e Setters
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public Integer getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
    
    public Integer getColetoresAtivos() {
        return coletoresAtivos;
    }
    
    public void setColetoresAtivos(Integer coletoresAtivos) {
        this.coletoresAtivos = coletoresAtivos;
    }
    
    public String getDataFormatada() {
        return dataFormatada;
    }
    
    public void setDataFormatada(String dataFormatada) {
        this.dataFormatada = dataFormatada;
    }
}
