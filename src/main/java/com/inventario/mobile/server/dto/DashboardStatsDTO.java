package com.inventario.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para estatísticas do dashboard mobile
 * Contém KPIs principais do inventário
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DashboardStatsDTO {
    
    @JsonProperty("totalPatrimonios")
    private Integer totalPatrimonios;
    
    @JsonProperty("patrimoniosColetados")
    private Integer patrimoniosColetados;
    
    @JsonProperty("percentualConclusao")
    private Double percentualConclusao;
    
    @JsonProperty("patrimoniosPendentes")
    private Integer patrimoniosPendentes;
    
    @JsonProperty("divergencias")
    private Integer divergencias;
    
    @JsonProperty("valorTotal")
    private Double valorTotal;
    
    @JsonProperty("coletoresAtivos")
    private Integer coletoresAtivos;
    
    @JsonProperty("ultimaAtualizacao")
    private String ultimaAtualizacao;
    
    // Construtores
    public DashboardStatsDTO() {}
    
    public DashboardStatsDTO(Integer totalPatrimonios, Integer patrimoniosColetados, 
                            Integer divergencias, Double valorTotal, Integer coletoresAtivos) {
        this.totalPatrimonios = totalPatrimonios;
        this.patrimoniosColetados = patrimoniosColetados;
        this.patrimoniosPendentes = totalPatrimonios - patrimoniosColetados;
        this.percentualConclusao = totalPatrimonios > 0 
            ? (patrimoniosColetados * 100.0) / totalPatrimonios 
            : 0.0;
        this.divergencias = divergencias;
        this.valorTotal = valorTotal;
        this.coletoresAtivos = coletoresAtivos;
        this.ultimaAtualizacao = java.time.LocalDateTime.now().toString();
    }
    
    // Getters e Setters
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
    
    public Integer getPatrimoniosPendentes() {
        return patrimoniosPendentes;
    }
    
    public void setPatrimoniosPendentes(Integer patrimoniosPendentes) {
        this.patrimoniosPendentes = patrimoniosPendentes;
    }
    
    public Integer getDivergencias() {
        return divergencias;
    }
    
    public void setDivergencias(Integer divergencias) {
        this.divergencias = divergencias;
    }
    
    public Double getValorTotal() {
        return valorTotal;
    }
    
    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }
    
    public Integer getColetoresAtivos() {
        return coletoresAtivos;
    }
    
    public void setColetoresAtivos(Integer coletoresAtivos) {
        this.coletoresAtivos = coletoresAtivos;
    }
    
    public String getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }
    
    public void setUltimaAtualizacao(String ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }
}
