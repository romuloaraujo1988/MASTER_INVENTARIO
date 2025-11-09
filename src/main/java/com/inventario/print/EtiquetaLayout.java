package com.inventario.print;

/**
 * Define layouts de etiquetas para impressão
 * Suporta diferentes tamanhos e formatos
 */
public enum EtiquetaLayout {
    
    // Etiquetas pequenas (40x30mm) - QR Code + Número
    PEQUENA_40x30(40, 30, 20, true, false, false),
    
    // Etiquetas médias (50x30mm) - QR Code + Número + Descrição resumida
    MEDIA_50x30(50, 30, 25, true, true, false),
    
    // Etiquetas grandes (60x40mm) - QR Code + Número + Descrição + Localização
    GRANDE_60x40(60, 40, 30, true, true, true),
    
    // Etiquetas extra grandes (80x50mm) - Todas as informações
    EXTRA_GRANDE_80x50(80, 50, 35, true, true, true);
    
    private final int larguraMm;
    private final int alturaMm;
    private final int tamanhoQrMm;
    private final boolean incluirNumero;
    private final boolean incluirDescricao;
    private final boolean incluirLocalizacao;
    
    EtiquetaLayout(int larguraMm, int alturaMm, int tamanhoQrMm, 
                   boolean incluirNumero, boolean incluirDescricao, boolean incluirLocalizacao) {
        this.larguraMm = larguraMm;
        this.alturaMm = alturaMm;
        this.tamanhoQrMm = tamanhoQrMm;
        this.incluirNumero = incluirNumero;
        this.incluirDescricao = incluirDescricao;
        this.incluirLocalizacao = incluirLocalizacao;
    }
    
    public int getLarguraMm() {
        return larguraMm;
    }
    
    public int getAlturaMm() {
        return alturaMm;
    }
    
    public int getTamanhoQrMm() {
        return tamanhoQrMm;
    }
    
    public boolean isIncluirNumero() {
        return incluirNumero;
    }
    
    public boolean isIncluirDescricao() {
        return incluirDescricao;
    }
    
    public boolean isIncluirLocalizacao() {
        return incluirLocalizacao;
    }
    
    public int getLarguraPixels(int dpi) {
        return mmToPixels(larguraMm, dpi);
    }
    
    public int getAlturaPixels(int dpi) {
        return mmToPixels(alturaMm, dpi);
    }
    
    public int getTamanhoQrPixels(int dpi) {
        return mmToPixels(tamanhoQrMm, dpi);
    }
    
    private int mmToPixels(int mm, int dpi) {
        return (int) Math.round((mm / 25.4) * dpi);
    }
    
    @Override
    public String toString() {
        return String.format("%dx%dmm", larguraMm, alturaMm);
    }
}
