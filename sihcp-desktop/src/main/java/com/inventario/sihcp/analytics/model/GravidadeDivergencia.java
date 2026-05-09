package com.inventario.sihcp.analytics.model;

import java.awt.Color;

/**
 * Níveis de gravidade de divergências.
 * 
 * Usado para priorizar ações corretivas.
 */
public enum GravidadeDivergencia {
    
    /**
     * Gravidade crítica - requer ação imediata.
     * Ex: Estado de conservação piorou 2 ou mais níveis.
     */
    CRITICA("Crítica", 4, new Color(192, 57, 43)),
    
    /**
     * Gravidade alta - requer atenção prioritária.
     * Ex: Patrimônio mudou de setor, estado piorou 1 nível.
     */
    ALTA("Alta", 3, new Color(231, 76, 60)),
    
    /**
     * Gravidade média - requer atenção.
     * Ex: Patrimônio mudou de sala no mesmo setor.
     */
    MEDIA("Média", 2, new Color(243, 156, 18)),
    
    /**
     * Gravidade baixa - informativo.
     * Ex: Apenas descrição de localização difere, estado melhorou.
     */
    BAIXA("Baixa", 1, new Color(46, 204, 113));
    
    private final String descricao;
    private final int nivel;
    private final Color cor;
    
    GravidadeDivergencia(String descricao, int nivel, Color cor) {
        this.descricao = descricao;
        this.nivel = nivel;
        this.cor = cor;
    }
    
    /**
     * Retorna a descrição da gravidade.
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Retorna o nível numérico da gravidade (1-4, onde 4 é mais grave).
     */
    public int getNivel() {
        return nivel;
    }
    
    /**
     * Retorna a cor associada à gravidade para uso em UI.
     */
    public Color getCor() {
        return cor;
    }
    
    /**
     * Verifica se esta gravidade é considerada grave (ALTA ou CRITICA).
     */
    public boolean isGrave() {
        return this == ALTA || this == CRITICA;
    }
    
    /**
     * Compara duas gravidades e retorna a mais grave.
     */
    public static GravidadeDivergencia maisGrave(GravidadeDivergencia g1, GravidadeDivergencia g2) {
        if (g1 == null) return g2;
        if (g2 == null) return g1;
        return g1.nivel >= g2.nivel ? g1 : g2;
    }
    
    /**
     * Converte uma string para o enum correspondente.
     * 
     * @param valor String a ser convertida
     * @return GravidadeDivergencia correspondente ou BAIXA se não encontrado
     */
    public static GravidadeDivergencia fromString(String valor) {
        if (valor == null || valor.isEmpty()) {
            return BAIXA;
        }
        
        String valorUpper = valor.toUpperCase().trim();
        
        // Tenta match direto
        try {
            return GravidadeDivergencia.valueOf(valorUpper);
        } catch (IllegalArgumentException e) {
            // Tenta match por descrição
            for (GravidadeDivergencia gravidade : values()) {
                if (gravidade.descricao.equalsIgnoreCase(valor)) {
                    return gravidade;
                }
            }
            return BAIXA;
        }
    }
    
    /**
     * Retorna a gravidade baseada no nível numérico.
     * 
     * @param nivel Nível (1-4)
     * @return GravidadeDivergencia correspondente
     */
    public static GravidadeDivergencia fromNivel(int nivel) {
        for (GravidadeDivergencia gravidade : values()) {
            if (gravidade.nivel == nivel) {
                return gravidade;
            }
        }
        return BAIXA;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}
