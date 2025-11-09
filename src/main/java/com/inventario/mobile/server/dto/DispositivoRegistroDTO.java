package com.inventario.mobile.server.dto;

/**
 * DTO para registro de dispositivo mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DispositivoRegistroDTO {

    private String deviceId;
    private Integer idUsuario;
    private String modelo;
    private String fabricante;
    private String versaoAndroid;
    private String versaoApp;
    private String enderecoIp;
    private String enderecoMac;

    // Construtores
    public DispositivoRegistroDTO() {
    }

    public DispositivoRegistroDTO(String deviceId, Integer idUsuario, String modelo, String fabricante) {
        this.deviceId = deviceId;
        this.idUsuario = idUsuario;
        this.modelo = modelo;
        this.fabricante = fabricante;
    }

    // Getters e Setters
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

    @Override
    public String toString() {
        return "DispositivoRegistroDTO{" +
                "deviceId='" + deviceId + '\'' +
                ", idUsuario=" + idUsuario +
                ", modelo='" + modelo + '\'' +
                ", fabricante='" + fabricante + '\'' +
                '}';
    }
}
