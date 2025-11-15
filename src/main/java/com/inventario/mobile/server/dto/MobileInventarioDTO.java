package com.inventario.mobile.server.dto;

import java.util.Date;

/**
 * DTO para dados de inventário mobile
 */
public class MobileInventarioDTO {
    
    private Integer id;
    private String nome;
    private String descricao;
    private String status;
    private Date dataInicio;
    private Date dataFim;
    private Date dataCriacao;
    private Integer totalPatrimonios;
    private Integer totalColetados;
    private Double percentualConclusao;
    
    // Construtores
    public MobileInventarioDTO() {
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
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public Date getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }
    
    public Date getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public Integer getTotalPatrimonios() {
        return totalPatrimonios;
    }
    
    public void setTotalPatrimonios(Integer totalPatrimonios) {
        this.totalPatrimonios = totalPatrimonios;
    }
    
    public Integer getTotalColetados() {
        return totalColetados;
    }
    
    public void setTotalColetados(Integer totalColetados) {
        this.totalColetados = totalColetados;
    }
    
    public Double getPercentualConclusao() {
        return percentualConclusao;
    }
    
    public void setPercentualConclusao(Double percentualConclusao) {
        this.percentualConclusao = percentualConclusao;
    }
}
