package com.inventario.sihcp.print;

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
    private int largura = 50; // mm
    private int altura = 20; // mm
    private boolean incluirQRCode = true;
    private boolean incluirDescricao = true;
    private boolean incluirMarcaModelo = true;
    private int margemHorizontal = 2; // mm
    private int margemVertical = 2; // mm
    private int colunas = 2;
    private String cabecalho = "INSTITUTO FEDERAL";
    private int tamanhoQRCode = 15; // mm
    
    // Coordenadas (em mm, relativas ao topo-esquerdo da etiqueta)
    private int xCabecalho = 2, yCabecalho = 5;
    private int xQRCode = 2, yQRCode = 7;
    private int xNumero = 20, yNumero = 10;
    private int xDescricao = 20, yDescricao = 15;
    private int xMarcaModelo = 20, yMarcaModelo = 18;
    
    // Configurações de Tipografia
    private int tamanhoFonteCabecalho = 7;
    private int tamanhoFonteNumero = 10;
    private int tamanhoFonteDescricao = 7;
    private int tamanhoFonteMarcaModelo = 6;
    // Construtor com valores padrão
    public EtiquetaConfig() {
        this.cabecalho = "INSTITUTO FEDERAL";
        this.largura = 50;
        this.altura = 20;
        this.colunas = 2;
        this.incluirQRCode = true;
        this.incluirDescricao = true;
        this.incluirMarcaModelo = true;
        this.tamanhoQRCode = 15;
        
        // Coordenadas padrão (mm)
        this.xCabecalho = 2;
        this.yCabecalho = 5;
        this.xQRCode = 2;
        this.yQRCode = 7;
        this.xNumero = 20;
        this.yNumero = 10;
        this.xDescricao = 20;
        this.yDescricao = 15;
        this.xMarcaModelo = 20;
        this.yMarcaModelo = 18;
        
        load(); // Tentar carregar se existir
    }

    public void save() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("cabecalho", cabecalho);
        props.setProperty("largura", String.valueOf(largura));
        props.setProperty("altura", String.valueOf(altura));
        props.setProperty("colunas", String.valueOf(colunas));
        props.setProperty("incluirQRCode", String.valueOf(incluirQRCode));
        props.setProperty("incluirDescricao", String.valueOf(incluirDescricao));
        props.setProperty("incluirMarcaModelo", String.valueOf(incluirMarcaModelo));
        props.setProperty("tamanhoQRCode", String.valueOf(tamanhoQRCode));
        props.setProperty("xCabecalho", String.valueOf(xCabecalho));
        props.setProperty("yCabecalho", String.valueOf(yCabecalho));
        props.setProperty("xQRCode", String.valueOf(xQRCode));
        props.setProperty("yQRCode", String.valueOf(yQRCode));
        props.setProperty("xNumero", String.valueOf(xNumero));
        props.setProperty("yNumero", String.valueOf(yNumero));
        props.setProperty("xDescricao", String.valueOf(xDescricao));
        props.setProperty("yDescricao", String.valueOf(yDescricao));
        props.setProperty("xMarcaModelo", String.valueOf(xMarcaModelo));
        props.setProperty("yMarcaModelo", String.valueOf(yMarcaModelo));
        
        props.setProperty("tamanhoFonteCabecalho", String.valueOf(tamanhoFonteCabecalho));
        props.setProperty("tamanhoFonteNumero", String.valueOf(tamanhoFonteNumero));
        props.setProperty("tamanhoFonteDescricao", String.valueOf(tamanhoFonteDescricao));
        props.setProperty("tamanhoFonteMarcaModelo", String.valueOf(tamanhoFonteMarcaModelo));

        try {
            java.io.File configDir = new java.io.File("config");
            if (!configDir.exists()) configDir.mkdir();
            
            try (java.io.FileOutputStream out = new java.io.FileOutputStream("config/etiqueta.properties")) {
                props.store(out, "Configurações de Etiqueta Patrimonial");
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        java.io.File file = new java.io.File("config/etiqueta.properties");
        if (!file.exists()) return;

        java.util.Properties props = new java.util.Properties();
        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
            props.load(in);
            this.cabecalho = props.getProperty("cabecalho", cabecalho);
            this.largura = Integer.parseInt(props.getProperty("largura", String.valueOf(largura)));
            this.altura = Integer.parseInt(props.getProperty("altura", String.valueOf(altura)));
            this.colunas = Integer.parseInt(props.getProperty("colunas", String.valueOf(colunas)));
            this.incluirQRCode = Boolean.parseBoolean(props.getProperty("incluirQRCode", String.valueOf(incluirQRCode)));
            this.incluirDescricao = Boolean.parseBoolean(props.getProperty("incluirDescricao", String.valueOf(incluirDescricao)));
            this.incluirMarcaModelo = Boolean.parseBoolean(props.getProperty("incluirMarcaModelo", String.valueOf(incluirMarcaModelo)));
            this.tamanhoQRCode = Integer.parseInt(props.getProperty("tamanhoQRCode", String.valueOf(tamanhoQRCode)));
            
            this.xCabecalho = Integer.parseInt(props.getProperty("xCabecalho", String.valueOf(xCabecalho)));
            this.yCabecalho = Integer.parseInt(props.getProperty("yCabecalho", String.valueOf(yCabecalho)));
            this.xQRCode = Integer.parseInt(props.getProperty("xQRCode", String.valueOf(xQRCode)));
            this.yQRCode = Integer.parseInt(props.getProperty("yQRCode", String.valueOf(yQRCode)));
            this.xNumero = Integer.parseInt(props.getProperty("xNumero", String.valueOf(xNumero)));
            this.yNumero = Integer.parseInt(props.getProperty("yNumero", String.valueOf(yNumero)));
            this.xDescricao = Integer.parseInt(props.getProperty("xDescricao", String.valueOf(xDescricao)));
            this.yDescricao = Integer.parseInt(props.getProperty("yDescricao", String.valueOf(yDescricao)));
            this.xMarcaModelo = Integer.parseInt(props.getProperty("xMarcaModelo", String.valueOf(xMarcaModelo)));
            this.yMarcaModelo = Integer.parseInt(props.getProperty("yMarcaModelo", String.valueOf(yMarcaModelo)));
            
            this.tamanhoFonteCabecalho = Integer.parseInt(props.getProperty("tamanhoFonteCabecalho", String.valueOf(tamanhoFonteCabecalho)));
            this.tamanhoFonteNumero = Integer.parseInt(props.getProperty("tamanhoFonteNumero", String.valueOf(tamanhoFonteNumero)));
            this.tamanhoFonteDescricao = Integer.parseInt(props.getProperty("tamanhoFonteDescricao", String.valueOf(tamanhoFonteDescricao)));
            this.tamanhoFonteMarcaModelo = Integer.parseInt(props.getProperty("tamanhoFonteMarcaModelo", String.valueOf(tamanhoFonteMarcaModelo)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
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

    public boolean isIncluirMarcaModelo() {
        return incluirMarcaModelo;
    }

    public void setIncluirMarcaModelo(boolean incluirMarcaModelo) {
        this.incluirMarcaModelo = incluirMarcaModelo;
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

    public int getColunas() {
        return colunas;
    }

    public void setColunas(int colunas) {
        this.colunas = colunas;
    }

    public String getCabecalho() {
        return cabecalho;
    }

    public void setCabecalho(String cabecalho) {
        this.cabecalho = cabecalho;
    }

    public int getTamanhoQRCode() {
        return tamanhoQRCode;
    }

    public void setTamanhoQRCode(int tamanhoQRCode) {
        this.tamanhoQRCode = tamanhoQRCode;
    }

    public int getxCabecalho() { return xCabecalho; }
    public void setxCabecalho(int x) { this.xCabecalho = x; }
    public int getyCabecalho() { return yCabecalho; }
    public void setyCabecalho(int y) { this.yCabecalho = y; }

    public int getxQRCode() { return xQRCode; }
    public void setxQRCode(int x) { this.xQRCode = x; }
    public int getyQRCode() { return yQRCode; }
    public void setyQRCode(int y) { this.yQRCode = y; }

    public int getxNumero() { return xNumero; }
    public void setxNumero(int x) { this.xNumero = x; }
    public int getyNumero() { return yNumero; }
    public void setyNumero(int y) { this.yNumero = y; }

    public int getxDescricao() { return xDescricao; }
    public void setxDescricao(int x) { this.xDescricao = x; }
    public int getyDescricao() { return yDescricao; }
    public void setyDescricao(int y) { this.yDescricao = y; }

    public int getxMarcaModelo() { return xMarcaModelo; }
    public void setxMarcaModelo(int x) { this.xMarcaModelo = x; }
    public int getyMarcaModelo() { return yMarcaModelo; }
    public void setyMarcaModelo(int y) { this.yMarcaModelo = y; }
    
    public int getTamanhoFonteCabecalho() { return tamanhoFonteCabecalho; }
    public void setTamanhoFonteCabecalho(int t) { this.tamanhoFonteCabecalho = t; }
    
    public int getTamanhoFonteNumero() { return tamanhoFonteNumero; }
    public void setTamanhoFonteNumero(int t) { this.tamanhoFonteNumero = t; }
    
    public int getTamanhoFonteDescricao() { return tamanhoFonteDescricao; }
    public void setTamanhoFonteDescricao(int t) { this.tamanhoFonteDescricao = t; }
    
    public int getTamanhoFonteMarcaModelo() { return tamanhoFonteMarcaModelo; }
    public void setTamanhoFonteMarcaModelo(int t) { this.tamanhoFonteMarcaModelo = t; }
}
