package com.inventario.print;

/**
 * Configuração para impressão de etiquetas patrimoniais
 */
public class EtiquetaConfig {
    
    public enum Formato {
        PADRAO,
        COMPACTO,
        DETALHADO
    }
    
    private Formato formato = Formato.PADRAO;
    private int largura = 60; // mm
    private int altura = 40; // mm
    private boolean incluirQRCode = true;
    private boolean incluirDescricao = true;
    private int margemHorizontal = 5; // mm
    private int margemVertical = 5; // mm
    
    public Formato getFormato() {
        return formato;
    }
    
    public void setFormato(Formato formato) {
        this.formato = formato;
    }
    
    public int getLargura() {
        return largura;
    }
    
    public void setLargura(int largura) {
        this.largura = largura;
    }
    
    public int getAltura() {
        return altura;
    }
    
    public void setAltura(int altura) {
        this.altura = altura;
    }
    
    public boolean isIncluirQRCode() {
        return incluirQRCode;
    }
    
    public void setIncluirQRCode(boolean incluirQRCode) {
        this.incluirQRCode = incluirQRCode;
    }
    
    public boolean isIncluirDescricao() {
        return incluirDescricao;
    }
    
    public void setIncluirDescricao(boolean incluirDescricao) {
        this.incluirDescricao = incluirDescricao;
    }
    
    public int getMargemHorizontal() {
        return margemHorizontal;
    }
    
    public void setMargemHorizontal(int margemHorizontal) {
        this.margemHorizontal = margemHorizontal;
    }
    
    public int getMargemVertical() {
        return margemVertical;
    }
    
    public void setMargemVertical(int margemVertical) {
        this.margemVertical = margemVertical;
    }
}
