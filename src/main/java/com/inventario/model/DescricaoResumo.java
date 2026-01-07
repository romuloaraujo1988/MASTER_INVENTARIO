package com.inventario.model;

/**
 * Classe que representa um resumo de descrição de patrimônio para
 * exibição na tela de cadastro de fotos de referência.
 * 
 * Agrupa descrições normalizadas com contagem de patrimônios e
 * indicação se já possui foto cadastrada.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
public class DescricaoResumo {
    
    private String descricaoNormalizada;
    private int quantidadePatrimonios;
    private boolean possuiFoto;
    private int idFotoReferencia;
    
    /**
     * Construtor padrão
     */
    public DescricaoResumo() {
    }
    
    /**
     * Construtor com parâmetros principais
     * 
     * @param descricaoNormalizada Descrição normalizada
     * @param quantidadePatrimonios Quantidade de patrimônios com essa descrição
     */
    public DescricaoResumo(String descricaoNormalizada, int quantidadePatrimonios) {
        this.descricaoNormalizada = descricaoNormalizada;
        this.quantidadePatrimonios = quantidadePatrimonios;
        this.possuiFoto = false;
    }
    
    /**
     * Construtor completo
     * 
     * @param descricaoNormalizada Descrição normalizada
     * @param quantidadePatrimonios Quantidade de patrimônios
     * @param possuiFoto Se já possui foto cadastrada
     * @param idFotoReferencia ID da foto de referência (se existir)
     */
    public DescricaoResumo(String descricaoNormalizada, int quantidadePatrimonios, 
                          boolean possuiFoto, int idFotoReferencia) {
        this.descricaoNormalizada = descricaoNormalizada;
        this.quantidadePatrimonios = quantidadePatrimonios;
        this.possuiFoto = possuiFoto;
        this.idFotoReferencia = idFotoReferencia;
    }
    
    // Getters e Setters
    
    public String getDescricaoNormalizada() {
        return descricaoNormalizada;
    }
    
    public void setDescricaoNormalizada(String descricaoNormalizada) {
        this.descricaoNormalizada = descricaoNormalizada;
    }
    
    public int getQuantidadePatrimonios() {
        return quantidadePatrimonios;
    }
    
    public void setQuantidadePatrimonios(int quantidadePatrimonios) {
        this.quantidadePatrimonios = quantidadePatrimonios;
    }
    
    public boolean isPossuiFoto() {
        return possuiFoto;
    }
    
    public void setPossuiFoto(boolean possuiFoto) {
        this.possuiFoto = possuiFoto;
    }
    
    public int getIdFotoReferencia() {
        return idFotoReferencia;
    }
    
    public void setIdFotoReferencia(int idFotoReferencia) {
        this.idFotoReferencia = idFotoReferencia;
    }
    
    /**
     * Retorna descrição truncada para exibição em tabelas
     * 
     * @param maxLength Tamanho máximo
     * @return Descrição truncada com "..." se necessário
     */
    public String getDescricaoTruncada(int maxLength) {
        if (descricaoNormalizada == null) return "";
        if (descricaoNormalizada.length() <= maxLength) return descricaoNormalizada;
        return descricaoNormalizada.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Retorna texto formatado para exibição
     * 
     * @return String formatada
     */
    public String getTextoExibicao() {
        String status = possuiFoto ? "✓" : "○";
        return String.format("%s %s (%d patrimônios)", 
            status, 
            getDescricaoTruncada(60), 
            quantidadePatrimonios);
    }
    
    @Override
    public String toString() {
        return getTextoExibicao();
    }
}
