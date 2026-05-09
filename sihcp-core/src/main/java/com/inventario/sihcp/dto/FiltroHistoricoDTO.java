package com.inventario.sihcp.dto;

import java.util.Date;

/**
 * DTO para representar filtros aplicados na busca de histórico de coletas.
 * Todos os campos são opcionais.
 */
public class FiltroHistoricoDTO {
    
    private Integer inventarioId;
    private Integer coletorId;
    private Date dataInicio;
    private Date dataFim;
    private Integer offset;
    private Integer limit;
    
    // Construtores
    public FiltroHistoricoDTO() {
        this.offset = 0;
        this.limit = 20; // Padrão: 20 registros por página
    }
    
    public FiltroHistoricoDTO(Integer inventarioId, Integer coletorId, Date dataInicio, Date dataFim) {
        this();
        this.inventarioId = inventarioId;
        this.coletorId = coletorId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }
    
    // Getters e Setters
    public Integer getInventarioId() {
        return inventarioId;
    }
    
    public void setInventarioId(Integer inventarioId) {
        this.inventarioId = inventarioId;
    }
    
    public Integer getColetorId() {
        return coletorId;
    }
    
    public void setColetorId(Integer coletorId) {
        this.coletorId = coletorId;
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
    
    public Integer getOffset() {
        return offset;
    }
    
    public void setOffset(Integer offset) {
        this.offset = offset;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    /**
     * Verifica se há algum filtro aplicado (exceto paginação)
     */
    public boolean temFiltros() {
        return inventarioId != null || 
               coletorId != null || 
               dataInicio != null || 
               dataFim != null;
    }
    
    /**
     * Limpa todos os filtros (mantém paginação)
     */
    public void limparFiltros() {
        this.inventarioId = null;
        this.coletorId = null;
        this.dataInicio = null;
        this.dataFim = null;
    }
    
    @Override
    public String toString() {
        return "FiltroHistoricoDTO{" +
                "inventarioId=" + inventarioId +
                ", coletorId=" + coletorId +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
