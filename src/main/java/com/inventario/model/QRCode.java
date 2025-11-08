package com.inventario.model;

import java.sql.Timestamp;
// Formatação de datas centralizada em DateFormatUtils
import com.inventario.util.DateFormatUtils;
import java.util.Objects;

/**
 * Classe que representa um QR Code gerado para um patrimônio
 * Armazena apenas dados imutáveis do patrimônio
 */
public class QRCode {
    
    private int id;
    private int idPatrimonio;
    private String codigoQR;
    private String hashDados;
    private Timestamp dataGeracao;
    private String formato;
    private int tamanho;
    private boolean ativo;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Campos transientes para exibição
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    
    // Constantes
    public static final String FORMATO_PNG = "PNG";
    public static final String FORMATO_JPG = "JPG";
    public static final String FORMATO_SVG = "SVG";
    
    public static final int TAMANHO_PEQUENO = 100;
    public static final int TAMANHO_MEDIO = 200;
    public static final int TAMANHO_GRANDE = 300;
    public static final int TAMANHO_EXTRA_GRANDE = 500;
    
    // Formatação de datas centralizada em DateFormatUtils
    
    // Construtores
    public QRCode() {
        this.formato = FORMATO_PNG;
        this.tamanho = TAMANHO_MEDIO;
        this.ativo = true;
        this.dataGeracao = new Timestamp(System.currentTimeMillis());
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
    
    public QRCode(int idPatrimonio, String codigoQR) {
        this();
        this.idPatrimonio = idPatrimonio;
        this.codigoQR = codigoQR;
    }
    
    public QRCode(int idPatrimonio, String codigoQR, String hashDados) {
        this(idPatrimonio, codigoQR);
        this.hashDados = hashDados;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getIdPatrimonio() {
        return idPatrimonio;
    }
    
    public void setIdPatrimonio(int idPatrimonio) {
        this.idPatrimonio = idPatrimonio;
    }
    
    public String getCodigoQR() {
        return codigoQR;
    }
    
    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }
    
    public String getHashDados() {
        return hashDados;
    }
    
    public void setHashDados(String hashDados) {
        this.hashDados = hashDados;
    }
    
    public Timestamp getDataGeracao() {
        return dataGeracao;
    }
    
    public void setDataGeracao(Timestamp dataGeracao) {
        this.dataGeracao = dataGeracao;
    }
    
    public String getFormato() {
        return formato;
    }
    
    public void setFormato(String formato) {
        this.formato = formato;
    }
    
    public int getTamanho() {
        return tamanho;
    }
    
    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Campos transientes
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
    
    // Métodos utilitários
    public String getDataGeracaoFormatada() {
        return DateFormatUtils.formatWithDefault(dataGeracao, "Não informado");
    }
    
    public String getStatusAtivacao() {
        return ativo ? "Ativo" : "Inativo";
    }
    
    public String getTamanhoDescricao() {
        switch (tamanho) {
            case TAMANHO_PEQUENO:
                return "Pequeno (100px)";
            case TAMANHO_MEDIO:
                return "Médio (200px)";
            case TAMANHO_GRANDE:
                return "Grande (300px)";
            case TAMANHO_EXTRA_GRANDE:
                return "Extra Grande (500px)";
            default:
                return tamanho + "px";
        }
    }
    
    public String getIdentificacao() {
        StringBuilder sb = new StringBuilder();
        if (numeroPatrimonio != null && !numeroPatrimonio.trim().isEmpty()) {
            sb.append("Nº ").append(numeroPatrimonio);
        }
        if (descricaoPatrimonio != null && !descricaoPatrimonio.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(descricaoPatrimonio);
        }
        if (sb.length() == 0) {
            sb.append("ID ").append(idPatrimonio);
        }
        return sb.toString();
    }
    
    public boolean isHashValido(String hashParaComparar) {
        if (hashDados == null || hashParaComparar == null) {
            return false;
        }
        return hashDados.equals(hashParaComparar);
    }
    
    public boolean temDadosCompletos() {
        return idPatrimonio > 0 && 
               codigoQR != null && !codigoQR.trim().isEmpty() &&
               hashDados != null && !hashDados.trim().isEmpty();
    }
    
    public String getResumo() {
        return String.format("QR Code #%d - %s (%s)", 
            id, 
            getIdentificacao(),
            getStatusAtivacao());
    }
    
    // Métodos de validação
    public boolean isFormatoValido() {
        return formato != null && 
               (FORMATO_PNG.equals(formato) || 
                FORMATO_JPG.equals(formato) || 
                FORMATO_SVG.equals(formato));
    }
    
    public boolean isTamanhoValido() {
        return tamanho >= 50 && tamanho <= 1000;
    }
    
    @Override
    public String toString() {
        return getResumo();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        QRCode qrCode = (QRCode) obj;
        return id == qrCode.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}