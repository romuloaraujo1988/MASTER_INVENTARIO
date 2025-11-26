package com.inventario.dto;

import java.sql.Timestamp;

/**
 * DTO para resumo de coleta - usado para exibição otimizada no histórico
 * Contém apenas os campos necessários para a tabela de histórico
 */
public class ColetaResumo {
    private long id;
    private Timestamp dataColeta;
    private String numeroPatrimonio;
    private String descricao;
    private String estadoEncontrado;
    private boolean semEtiqueta;
    
    public ColetaResumo() {}
    
    public ColetaResumo(long id, Timestamp dataColeta, String numeroPatrimonio, 
                        String descricao, String estadoEncontrado) {
        this.id = id;
        this.dataColeta = dataColeta;
        this.numeroPatrimonio = numeroPatrimonio;
        this.descricao = descricao;
        this.estadoEncontrado = estadoEncontrado;
    }
    
    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public boolean isSemEtiqueta() { return semEtiqueta; }
    public void setSemEtiqueta(boolean semEtiqueta) { this.semEtiqueta = semEtiqueta; }
    
    public Timestamp getDataColeta() { return dataColeta; }
    public void setDataColeta(Timestamp dataColeta) { this.dataColeta = dataColeta; }
    
    public String getNumeroPatrimonio() { return numeroPatrimonio; }
    public void setNumeroPatrimonio(String numeroPatrimonio) { this.numeroPatrimonio = numeroPatrimonio; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public String getEstadoEncontrado() { return estadoEncontrado; }
    public void setEstadoEncontrado(String estadoEncontrado) { this.estadoEncontrado = estadoEncontrado; }
}
