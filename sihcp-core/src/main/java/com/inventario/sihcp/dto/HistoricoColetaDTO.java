package com.inventario.sihcp.dto;

import java.util.Date;

/**
 * DTO para representar uma coleta no histórico de um patrimônio.
 * Contém todos os dados necessários para exibição e comparação.
 */
public class HistoricoColetaDTO {
    
    // Identificadores
    private Integer id;
    private Integer patrimonioId;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    
    // Dados do inventário
    private Integer inventarioId;
    private String nomeInventario;
    
    // Dados do coletor
    private Integer coletorId;
    private String nomeColetorCompleto;
    
    // Dados da coleta
    private Date dataColeta;
    private String localizacaoEncontrada;
    private String estadoEncontrado;
    private String observacoes;
    
    // Dados da sala/setor
    private Integer salaId;
    private String nomeSala;
    private Integer setorId;
    private String nomeSetor;
    
    // Campos de comparação (calculados)
    private Boolean temMudancaLocalizacao;
    private Boolean temMudancaEstado;
    private String localizacaoAnterior;
    private String estadoAnterior;
    
    // Construtores
    public HistoricoColetaDTO() {
    }
    
    // Getters e Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getPatrimonioId() {
        return patrimonioId;
    }
    
    public void setPatrimonioId(Integer patrimonioId) {
        this.patrimonioId = patrimonioId;
    }
    
    public String getNumeroPatrimonio() {
        return numeroPatrimonio;
    }
    
    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
    }
    
    public String getDescricaoPatrimonio() {
        return descricaoPatrimonio;
    }
    
    public void setDescricaoPatrimonio(String descricaoPatrimonio) {
        this.descricaoPatrimonio = descricaoPatrimonio;
    }
    
    public Integer getInventarioId() {
        return inventarioId;
    }
    
    public void setInventarioId(Integer inventarioId) {
        this.inventarioId = inventarioId;
    }
    
    public String getNomeInventario() {
        return nomeInventario;
    }
    
    public void setNomeInventario(String nomeInventario) {
        this.nomeInventario = nomeInventario;
    }
    
    public Integer getColetorId() {
        return coletorId;
    }
    
    public void setColetorId(Integer coletorId) {
        this.coletorId = coletorId;
    }
    
    public String getNomeColetorCompleto() {
        return nomeColetorCompleto;
    }
    
    public void setNomeColetorCompleto(String nomeColetorCompleto) {
        this.nomeColetorCompleto = nomeColetorCompleto;
    }
    
    public Date getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(Date dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public String getLocalizacaoEncontrada() {
        return localizacaoEncontrada;
    }
    
    public void setLocalizacaoEncontrada(String localizacaoEncontrada) {
        this.localizacaoEncontrada = localizacaoEncontrada;
    }
    
    public String getEstadoEncontrado() {
        return estadoEncontrado;
    }
    
    public void setEstadoEncontrado(String estadoEncontrado) {
        this.estadoEncontrado = estadoEncontrado;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public Integer getSalaId() {
        return salaId;
    }
    
    public void setSalaId(Integer salaId) {
        this.salaId = salaId;
    }
    
    public String getNomeSala() {
        return nomeSala;
    }
    
    public void setNomeSala(String nomeSala) {
        this.nomeSala = nomeSala;
    }
    
    public Integer getSetorId() {
        return setorId;
    }
    
    public void setSetorId(Integer setorId) {
        this.setorId = setorId;
    }
    
    public String getNomeSetor() {
        return nomeSetor;
    }
    
    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }
    
    public Boolean getTemMudancaLocalizacao() {
        return temMudancaLocalizacao;
    }
    
    public void setTemMudancaLocalizacao(Boolean temMudancaLocalizacao) {
        this.temMudancaLocalizacao = temMudancaLocalizacao;
    }
    
    public Boolean getTemMudancaEstado() {
        return temMudancaEstado;
    }
    
    public void setTemMudancaEstado(Boolean temMudancaEstado) {
        this.temMudancaEstado = temMudancaEstado;
    }
    
    public String getLocalizacaoAnterior() {
        return localizacaoAnterior;
    }
    
    public void setLocalizacaoAnterior(String localizacaoAnterior) {
        this.localizacaoAnterior = localizacaoAnterior;
    }
    
    public String getEstadoAnterior() {
        return estadoAnterior;
    }
    
    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }
    
    @Override
    public String toString() {
        return "HistoricoColetaDTO{" +
                "id=" + id +
                ", patrimonioId=" + patrimonioId +
                ", numeroPatrimonio='" + numeroPatrimonio + '\'' +
                ", inventarioId=" + inventarioId +
                ", nomeInventario='" + nomeInventario + '\'' +
                ", dataColeta=" + dataColeta +
                ", localizacaoEncontrada='" + localizacaoEncontrada + '\'' +
                ", estadoEncontrado='" + estadoEncontrado + '\'' +
                '}';
    }
}
