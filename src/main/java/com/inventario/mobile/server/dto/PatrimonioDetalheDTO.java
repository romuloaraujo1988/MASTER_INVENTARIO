package com.inventario.mobile.server.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * DTO para detalhes completos de um patrimônio
 * Inclui todas as informações necessárias para visualização detalhada
 * 
 * @author Sistema de Inventário
 * @version 1.0
 */
public class PatrimonioDetalheDTO {
    
    // Dados básicos
    private Integer id;
    private String codigo;
    private String descricao;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String estado;
    private BigDecimal valor;
    private String observacoes;
    
    // Dados da sala
    private Integer salaId;
    private String salaNome;
    private String salaBloco;
    private String salaAndar;
    private String salaLocalizacaoCompleta;
    
    // Dados do responsável
    private Integer responsavelId;
    private String responsavelNome;
    private String responsavelMatricula;
    private String responsavelSetor;
    private String responsavelEmail;
    private String responsavelTelefone;
    
    // Status de coleta
    private boolean coletado;
    private Timestamp dataColeta;
    private String coletadoPor;
    private String localizacaoEncontrada;
    private String estadoEncontrado;
    private String observacoesColeta;
    
    // Histórico
    private Integer totalColetas;
    private Timestamp ultimaColeta;
    private List<Map<String, Object>> historicoColetas;
    
    // Foto
    private String fotoUrl;
    
    // Construtores
    public PatrimonioDetalheDTO() {
    }
    
    // Getters e Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public String getNumeroSerie() {
        return numeroSerie;
    }
    
    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
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
    
    public String getSalaNome() {
        return salaNome;
    }
    
    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }
    
    public String getSalaBloco() {
        return salaBloco;
    }
    
    public void setSalaBloco(String salaBloco) {
        this.salaBloco = salaBloco;
    }
    
    public String getSalaAndar() {
        return salaAndar;
    }
    
    public void setSalaAndar(String salaAndar) {
        this.salaAndar = salaAndar;
    }
    
    public String getSalaLocalizacaoCompleta() {
        return salaLocalizacaoCompleta;
    }
    
    public void setSalaLocalizacaoCompleta(String salaLocalizacaoCompleta) {
        this.salaLocalizacaoCompleta = salaLocalizacaoCompleta;
    }
    
    public Integer getResponsavelId() {
        return responsavelId;
    }
    
    public void setResponsavelId(Integer responsavelId) {
        this.responsavelId = responsavelId;
    }
    
    public String getResponsavelNome() {
        return responsavelNome;
    }
    
    public void setResponsavelNome(String responsavelNome) {
        this.responsavelNome = responsavelNome;
    }
    
    public String getResponsavelMatricula() {
        return responsavelMatricula;
    }
    
    public void setResponsavelMatricula(String responsavelMatricula) {
        this.responsavelMatricula = responsavelMatricula;
    }
    
    public String getResponsavelSetor() {
        return responsavelSetor;
    }
    
    public void setResponsavelSetor(String responsavelSetor) {
        this.responsavelSetor = responsavelSetor;
    }
    
    public String getResponsavelEmail() {
        return responsavelEmail;
    }
    
    public void setResponsavelEmail(String responsavelEmail) {
        this.responsavelEmail = responsavelEmail;
    }
    
    public String getResponsavelTelefone() {
        return responsavelTelefone;
    }
    
    public void setResponsavelTelefone(String responsavelTelefone) {
        this.responsavelTelefone = responsavelTelefone;
    }
    
    public boolean isColetado() {
        return coletado;
    }
    
    public void setColetado(boolean coletado) {
        this.coletado = coletado;
    }
    
    public Timestamp getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(Timestamp dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public String getColetadoPor() {
        return coletadoPor;
    }
    
    public void setColetadoPor(String coletadoPor) {
        this.coletadoPor = coletadoPor;
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
    
    public String getObservacoesColeta() {
        return observacoesColeta;
    }
    
    public void setObservacoesColeta(String observacoesColeta) {
        this.observacoesColeta = observacoesColeta;
    }
    
    public Integer getTotalColetas() {
        return totalColetas;
    }
    
    public void setTotalColetas(Integer totalColetas) {
        this.totalColetas = totalColetas;
    }
    
    public Timestamp getUltimaColeta() {
        return ultimaColeta;
    }
    
    public void setUltimaColeta(Timestamp ultimaColeta) {
        this.ultimaColeta = ultimaColeta;
    }
    
    public String getFotoUrl() {
        return fotoUrl;
    }
    
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
    
    public List<Map<String, Object>> getHistoricoColetas() {
        return historicoColetas;
    }
    
    public void setHistoricoColetas(List<Map<String, Object>> historicoColetas) {
        this.historicoColetas = historicoColetas;
    }
}
