package com.inventario.sihcp.dto;

import java.util.Date;

/**
 * DTO para representar estatísticas do histórico de coletas de um patrimônio.
 * Fornece informações agregadas úteis para análise.
 */
public class EstatisticasHistoricoDTO {
    
    private Integer totalColetas;
    private Integer totalInventarios;
    private Date primeiraColeta;
    private Date ultimaColeta;
    private Integer totalMudancasLocalizacao;
    private Integer totalMudancasEstado;
    private Long diasEntrePrimeiraEUltima;
    private Double mediaColetasPorInventario;
    
    // Construtores
    public EstatisticasHistoricoDTO() {
    }
    
    // Getters e Setters
    public Integer getTotalColetas() {
        return totalColetas;
    }
    
    public void setTotalColetas(Integer totalColetas) {
        this.totalColetas = totalColetas;
    }
    
    public Integer getTotalInventarios() {
        return totalInventarios;
    }
    
    public void setTotalInventarios(Integer totalInventarios) {
        this.totalInventarios = totalInventarios;
    }
    
    public Date getPrimeiraColeta() {
        return primeiraColeta;
    }
    
    public void setPrimeiraColeta(Date primeiraColeta) {
        this.primeiraColeta = primeiraColeta;
    }
    
    public Date getUltimaColeta() {
        return ultimaColeta;
    }
    
    public void setUltimaColeta(Date ultimaColeta) {
        this.ultimaColeta = ultimaColeta;
    }
    
    public Integer getTotalMudancasLocalizacao() {
        return totalMudancasLocalizacao;
    }
    
    public void setTotalMudancasLocalizacao(Integer totalMudancasLocalizacao) {
        this.totalMudancasLocalizacao = totalMudancasLocalizacao;
    }
    
    public Integer getTotalMudancasEstado() {
        return totalMudancasEstado;
    }
    
    public void setTotalMudancasEstado(Integer totalMudancasEstado) {
        this.totalMudancasEstado = totalMudancasEstado;
    }
    
    public Long getDiasEntrePrimeiraEUltima() {
        return diasEntrePrimeiraEUltima;
    }
    
    public void setDiasEntrePrimeiraEUltima(Long diasEntrePrimeiraEUltima) {
        this.diasEntrePrimeiraEUltima = diasEntrePrimeiraEUltima;
    }
    
    public Double getMediaColetasPorInventario() {
        return mediaColetasPorInventario;
    }
    
    public void setMediaColetasPorInventario(Double mediaColetasPorInventario) {
        this.mediaColetasPorInventario = mediaColetasPorInventario;
    }
    
    /**
     * Calcula dias entre primeira e última coleta se ambas existirem
     */
    public void calcularDiasEntrePrimeiraEUltima() {
        if (primeiraColeta != null && ultimaColeta != null) {
            long diff = ultimaColeta.getTime() - primeiraColeta.getTime();
            this.diasEntrePrimeiraEUltima = diff / (1000 * 60 * 60 * 24);
        }
    }
    
    /**
     * Calcula média de coletas por inventário
     */
    public void calcularMediaColetasPorInventario() {
        if (totalInventarios != null && totalInventarios > 0 && totalColetas != null) {
            this.mediaColetasPorInventario = (double) totalColetas / totalInventarios;
        }
    }
    
    @Override
    public String toString() {
        return "EstatisticasHistoricoDTO{" +
                "totalColetas=" + totalColetas +
                ", totalInventarios=" + totalInventarios +
                ", primeiraColeta=" + primeiraColeta +
                ", ultimaColeta=" + ultimaColeta +
                ", totalMudancasLocalizacao=" + totalMudancasLocalizacao +
                ", totalMudancasEstado=" + totalMudancasEstado +
                ", diasEntrePrimeiraEUltima=" + diasEntrePrimeiraEUltima +
                ", mediaColetasPorInventario=" + mediaColetasPorInventario +
                '}';
    }
}
