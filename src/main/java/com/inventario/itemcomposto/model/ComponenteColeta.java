package com.inventario.itemcomposto.model;

import java.util.Date;

/**
 * Registra a quantidade de componentes encontrados durante a coleta de inventário.
 * 
 * @author Sistema de Inventário Patrimonial
 * @version 1.0
 * @since 27/11/2025
 */
public class ComponenteColeta {
    
    private Integer id;
    private Integer idComponente;
    private Integer idColeta;
    private Integer idInventario;
    private int quantidadeEncontrada;
    private String observacoes;
    private Date dataRegistro;
    private Integer idUsuarioRegistro;
    
    public ComponenteColeta() {
        this.quantidadeEncontrada = 0;
        this.dataRegistro = new Date();
    }
    
    public ComponenteColeta(Integer idComponente, Integer idColeta, Integer idInventario, int quantidadeEncontrada) {
        this();
        this.idComponente = idComponente;
        this.idColeta = idColeta;
        this.idInventario = idInventario;
        this.quantidadeEncontrada = quantidadeEncontrada;
    }
    
    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getIdComponente() { return idComponente; }
    public void setIdComponente(Integer idComponente) { this.idComponente = idComponente; }
    
    public Integer getIdColeta() { return idColeta; }
    public void setIdColeta(Integer idColeta) { this.idColeta = idColeta; }
    
    public Integer getIdInventario() { return idInventario; }
    public void setIdInventario(Integer idInventario) { this.idInventario = idInventario; }
    
    public int getQuantidadeEncontrada() { return quantidadeEncontrada; }
    public void setQuantidadeEncontrada(int quantidadeEncontrada) { this.quantidadeEncontrada = quantidadeEncontrada; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public Date getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(Date dataRegistro) { this.dataRegistro = dataRegistro; }
    
    public Integer getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(Integer idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
    
    @Override
    public String toString() {
        return String.format("ComponenteColeta[id=%d, componente=%d, qtdEncontrada=%d]",
                id, idComponente, quantidadeEncontrada);
    }
}
