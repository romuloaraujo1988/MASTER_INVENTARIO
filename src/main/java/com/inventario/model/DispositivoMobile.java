package com.inventario.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa um Dispositivo Mobile conectado ao sistema
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DispositivoMobile {

    private Integer id;
    private String deviceId;           // Android ID único
    private Integer idUsuario;         // FK para Usuario
    private String nomeUsuario;        // Para JOIN
    private String modelo;             // Ex: "Samsung Galaxy S21"
    private String fabricante;         // Ex: "Samsung"
    private String versaoAndroid;      // Ex: "13"
    private String versaoApp;          // Ex: "1.2.0"
    private String enderecoIp;
    private String enderecoMac;
    private StatusDispositivo status;
    private LocalDateTime dataRegistro;
    private LocalDateTime dataUltimaConexao;
    private LocalDateTime dataUltimaSincronizacao;
    private Boolean ativo;
    private String tokenAtual;         // Token JWT atual
    private LocalDateTime dataExpiracaoToken;
    private String observacoes;

    // Construtores
    public DispositivoMobile() {
        this.status = StatusDispositivo.APROVADO; // Auto-aprovado
        this.ativo = true;
        this.dataRegistro = LocalDateTime.now();
    }

    public DispositivoMobile(String deviceId, Integer idUsuario, String modelo, String fabricante) {
        this();
        this.deviceId = deviceId;
        this.idUsuario = idUsuario;
        this.modelo = modelo;
        this.fabricante = fabricante;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getVersaoAndroid() {
        return versaoAndroid;
    }

    public void setVersaoAndroid(String versaoAndroid) {
        this.versaoAndroid = versaoAndroid;
    }

    public String getVersaoApp() {
        return versaoApp;
    }

    public void setVersaoApp(String versaoApp) {
        this.versaoApp = versaoApp;
    }

    public String getEnderecoIp() {
        return enderecoIp;
    }

    public void setEnderecoIp(String enderecoIp) {
        this.enderecoIp = enderecoIp;
    }

    public String getEnderecoMac() {
        return enderecoMac;
    }

    public void setEnderecoMac(String enderecoMac) {
        this.enderecoMac = enderecoMac;
    }

    public StatusDispositivo getStatus() {
        return status;
    }

    public void setStatus(StatusDispositivo status) {
        this.status = status;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public LocalDateTime getDataUltimaConexao() {
        return dataUltimaConexao;
    }

    public void setDataUltimaConexao(LocalDateTime dataUltimaConexao) {
        this.dataUltimaConexao = dataUltimaConexao;
    }

    public LocalDateTime getDataUltimaSincronizacao() {
        return dataUltimaSincronizacao;
    }

    public void setDataUltimaSincronizacao(LocalDateTime dataUltimaSincronizacao) {
        this.dataUltimaSincronizacao = dataUltimaSincronizacao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getTokenAtual() {
        return tokenAtual;
    }

    public void setTokenAtual(String tokenAtual) {
        this.tokenAtual = tokenAtual;
    }

    public LocalDateTime getDataExpiracaoToken() {
        return dataExpiracaoToken;
    }

    public void setDataExpiracaoToken(LocalDateTime dataExpiracaoToken) {
        this.dataExpiracaoToken = dataExpiracaoToken;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    // Métodos utilitários
    public void registrarConexao() {
        this.dataUltimaConexao = LocalDateTime.now();
    }

    public void registrarSincronizacao() {
        this.dataUltimaSincronizacao = LocalDateTime.now();
    }

    public void aprovar() {
        this.status = StatusDispositivo.APROVADO;
    }

    public void bloquear() {
        this.status = StatusDispositivo.BLOQUEADO;
        this.ativo = false;
    }

    public void desbloquear() {
        this.status = StatusDispositivo.APROVADO;
        this.ativo = true;
    }

    public void rejeitar() {
        this.status = StatusDispositivo.REJEITADO;
        this.ativo = false;
    }

    public boolean isAprovado() {
        return status == StatusDispositivo.APROVADO && Boolean.TRUE.equals(ativo);
    }

    public boolean isPendente() {
        return status == StatusDispositivo.PENDENTE;
    }

    public boolean isBloqueado() {
        return status == StatusDispositivo.BLOQUEADO;
    }

    public String getDescricaoCompleta() {
        return String.format("%s %s (Android %s)", fabricante, modelo, versaoAndroid);
    }

    @Override
    public String toString() {
        return "DispositivoMobile{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", modelo='" + modelo + '\'' +
                ", fabricante='" + fabricante + '\'' +
                ", status=" + status +
                ", ativo=" + ativo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositivoMobile that = (DispositivoMobile) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId);
    }
}
