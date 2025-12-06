package com.inventario.analytics.model;

import java.time.LocalDate;

/**
 * Dados de divergência para análise temporal (gráfico de linha).
 * 
 * Representa a quantidade de divergências de um tipo específico em uma data.
 */
public class DivergenciaTemporal {
    
    private LocalDate data;
    private TipoDivergencia tipo;
    private long quantidade;
    
    public DivergenciaTemporal() {
    }
    
    public DivergenciaTemporal(LocalDate data, TipoDivergencia tipo, long quantidade) {
        this.data = data;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }
    
    // Getters e Setters
    
    public LocalDate getData() {
        return data;
    }
    
    public void setData(LocalDate data) {
        this.data = data;
    }
    
    public TipoDivergencia getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoDivergencia tipo) {
        this.tipo = tipo;
    }
    
    public long getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(long quantidade) {
        this.quantidade = quantidade;
    }
    
    @Override
    public String toString() {
        return "DivergenciaTemporal{" +
                "data=" + data +
                ", tipo=" + tipo +
                ", quantidade=" + quantidade +
                '}';
    }
}
