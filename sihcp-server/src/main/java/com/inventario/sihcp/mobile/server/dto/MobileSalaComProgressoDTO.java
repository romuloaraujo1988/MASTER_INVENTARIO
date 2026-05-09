package com.inventario.sihcp.mobile.server.dto;

/**
 * DTO para dados de sala com estatísticas de progresso de coleta.
 * Usado na tela de Inventário por Sala do app mobile.
 */
public class MobileSalaComProgressoDTO {
    
    private Integer id;
    private String nome;
    private String numeroSala;
    private String descricao;
    private String andar;
    private String bloco;
    private Boolean ativa;
    
    // Estatísticas de progresso
    private Integer totalPatrimonios;
    private Integer coletados;
    private Integer pendentes;
    private Float percentualColeta;
    
    // Construtores
    public MobileSalaComProgressoDTO() {
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
    
    public String getNumeroSala() {
        return numeroSala;
    }
    
    public void setNumeroSala(String numeroSala) {
        this.numeroSala = numeroSala;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getAndar() {
        return andar;
    }
    
    public void setAndar(String andar) {
        this.andar = andar;
    }
    
    public String getBloco() {
        return bloco;
    }
    
    public void setBloco(String bloco) {
        this.bloco = bloco;
    }
    
    public Boolean getAtiva() {
        return ativa;
    }
    
    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }
    
    public Integer getTotalPatrimonios() {
        return totalPatrimonios;
    }
    
    public void setTotalPatrimonios(Integer totalPatrimonios) {
        this.totalPatrimonios = totalPatrimonios;
    }
    
    public Integer getColetados() {
        return coletados;
    }
    
    public void setColetados(Integer coletados) {
        this.coletados = coletados;
    }
    
    public Integer getPendentes() {
        return pendentes;
    }
    
    public void setPendentes(Integer pendentes) {
        this.pendentes = pendentes;
    }
    
    public Float getPercentualColeta() {
        return percentualColeta;
    }
    
    public void setPercentualColeta(Float percentualColeta) {
        this.percentualColeta = percentualColeta;
    }
    
    /**
     * Calcula pendentes e percentual automaticamente
     */
    public void calcularEstatisticas() {
        if (totalPatrimonios == null) totalPatrimonios = 0;
        if (coletados == null) coletados = 0;
        
        this.pendentes = totalPatrimonios - coletados;
        this.percentualColeta = totalPatrimonios > 0 
            ? (coletados.floatValue() / totalPatrimonios) * 100f 
            : 0f;
    }
}
