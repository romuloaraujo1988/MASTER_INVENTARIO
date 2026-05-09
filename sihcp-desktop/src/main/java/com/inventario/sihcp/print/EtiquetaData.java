package com.inventario.sihcp.print;

/**
 * Dados para impressão de etiqueta
 */
public class EtiquetaData {
    
    private String numeroPatrimonio;
    private String descricao;
    private String localizacao;
    private String setor;
    private String sala;
    private byte[] qrCodeImage;
    
    public EtiquetaData() {
    }
    
    public EtiquetaData(String numeroPatrimonio, String descricao, String localizacao) {
        this.numeroPatrimonio = numeroPatrimonio;
        this.descricao = descricao;
        this.localizacao = localizacao;
    }
    
    public String getNumeroPatrimonio() {
        return numeroPatrimonio;
    }
    
    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getLocalizacao() {
        return localizacao;
    }
    
    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }
    
    public String getSetor() {
        return setor;
    }
    
    public void setSetor(String setor) {
        this.setor = setor;
    }
    
    public String getSala() {
        return sala;
    }
    
    public void setSala(String sala) {
        this.sala = sala;
    }
    
    public byte[] getQrCodeImage() {
        return qrCodeImage;
    }
    
    public void setQrCodeImage(byte[] qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }
    
    public String getDescricaoResumida(int maxLength) {
        if (descricao == null) return "";
        if (descricao.length() <= maxLength) return descricao;
        return descricao.substring(0, maxLength - 3) + "...";
    }
}
