package com.inventario.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO com informações do inventário ativo
 * Retornado no login para o app salvar localmente
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileInventarioInfo {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("nome")
    private String nome;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("ano")
    private Integer ano;
    
    @JsonProperty("totalPatrimonios")
    private Integer totalPatrimonios;
    
    @JsonProperty("patrimoniosColetados")
    private Integer patrimoniosColetados;
    
    @JsonProperty("percentualConclusao")
    private Double percentualConclusao;
    
    // Construtores
    public MobileInventarioInfo() {}
    
    public MobileInventarioInfo(Integer id, String nome, String status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }
    
    public MobileInventarioInfo(Integer id, String nome, String status, Integer ano,
                                Integer totalPatrimonios, Integer patrimoniosColetados, 
                                Double percentualConclusao) {
        this.id = id;
        this.nome = nome;
        this.status = status;
        this.ano = ano;
        this.totalPatrimonios = totalPatrimonios;
        this.patrimoniosColetados = patrimoniosColetados;
        this.percentualConclusao = percentualConclusao;
    }
    
    // Getters e Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getAno() {
        return ano;
    }
    
    public void setAno(Integer ano) {
        this.ano = ano;
    }
    
    public Integer getTotalPatrimonios() {
        return totalPatrimonios;
    }
    
    public void setTotalPatrimonios(Integer totalPatrimonios) {
        this.totalPatrimonios = totalPatrimonios;
    }
    
    public Integer getPatrimoniosColetados() {
        return patrimoniosColetados;
    }
    
    public void setPatrimoniosColetados(Integer patrimoniosColetados) {
        this.patrimoniosColetados = patrimoniosColetados;
    }
    
    public Double getPercentualConclusao() {
        return percentualConclusao;
    }
    
    public void setPercentualConclusao(Double percentualConclusao) {
        this.percentualConclusao = percentualConclusao;
    }
}
