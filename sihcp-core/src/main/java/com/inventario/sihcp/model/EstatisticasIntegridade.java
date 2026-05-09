package com.inventario.sihcp.model;

import java.awt.Color;

/**
 * Classe que representa as estatísticas agregadas de integridade dos itens compostos.
 * Usada para exibir resumo no topo do relatório.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class EstatisticasIntegridade {
    
    // Contagens de conjuntos
    private int totalConjuntos;
    private int conjuntosCompletos;
    private int conjuntosIncompletos;
    private int conjuntosParciais;
    
    // Taxa geral
    private double taxaIntegridadeGeral;
    
    // Limiar de alerta (80%)
    private static final double LIMIAR_ALERTA = 80.0;
    
    public EstatisticasIntegridade() {
    }
    
    public EstatisticasIntegridade(int totalConjuntos, int conjuntosCompletos, 
                                    int conjuntosIncompletos, double taxaIntegridadeGeral) {
        this.totalConjuntos = totalConjuntos;
        this.conjuntosCompletos = conjuntosCompletos;
        this.conjuntosIncompletos = conjuntosIncompletos;
        this.conjuntosParciais = totalConjuntos - conjuntosCompletos - conjuntosIncompletos;
        this.taxaIntegridadeGeral = taxaIntegridadeGeral;
    }
    
    // === Métodos de Negócio ===
    
    /**
     * Verifica se a taxa de integridade está abaixo do limiar de alerta (80%)
     * 
     * @return true se taxa < 80%, indicando situação de alerta
     */
    public boolean isAlerta() {
        return taxaIntegridadeGeral < LIMIAR_ALERTA;
    }
    
    /**
     * Retorna a cor apropriada para a taxa de integridade
     * Verde se >= 80%, Vermelho se < 80%
     */
    public Color getCorTaxa() {
        return isAlerta() ? new Color(231, 76, 60) : new Color(46, 204, 113);
    }
    
    /**
     * Retorna a cor de fundo apropriada para a taxa de integridade
     */
    public Color getCorFundoTaxa() {
        return isAlerta() ? new Color(250, 219, 216) : new Color(212, 239, 223);
    }
    
    /**
     * Retorna a taxa de integridade formatada como string
     * Exemplo: "85.5%"
     */
    public String getTaxaFormatada() {
        return String.format("%.1f%%", taxaIntegridadeGeral);
    }
    
    /**
     * Calcula a taxa de integridade baseado nas contagens
     */
    public void calcularTaxa() {
        if (totalConjuntos == 0) {
            this.taxaIntegridadeGeral = 100.0;
        } else {
            this.taxaIntegridadeGeral = (conjuntosCompletos * 100.0) / totalConjuntos;
        }
    }
    
    /**
     * Retorna o percentual de conjuntos completos
     */
    public double getPercentualCompletos() {
        if (totalConjuntos == 0) return 100.0;
        return (conjuntosCompletos * 100.0) / totalConjuntos;
    }
    
    /**
     * Retorna o percentual de conjuntos incompletos
     */
    public double getPercentualIncompletos() {
        if (totalConjuntos == 0) return 0.0;
        return (conjuntosIncompletos * 100.0) / totalConjuntos;
    }
    
    // === Getters e Setters ===
    
    public int getTotalConjuntos() {
        return totalConjuntos;
    }
    
    public void setTotalConjuntos(int totalConjuntos) {
        this.totalConjuntos = totalConjuntos;
    }
    
    public int getConjuntosCompletos() {
        return conjuntosCompletos;
    }
    
    public void setConjuntosCompletos(int conjuntosCompletos) {
        this.conjuntosCompletos = conjuntosCompletos;
    }
    
    public int getConjuntosIncompletos() {
        return conjuntosIncompletos;
    }
    
    public void setConjuntosIncompletos(int conjuntosIncompletos) {
        this.conjuntosIncompletos = conjuntosIncompletos;
    }
    
    public int getConjuntosParciais() {
        return conjuntosParciais;
    }
    
    public void setConjuntosParciais(int conjuntosParciais) {
        this.conjuntosParciais = conjuntosParciais;
    }
    
    public double getTaxaIntegridadeGeral() {
        return taxaIntegridadeGeral;
    }
    
    public void setTaxaIntegridadeGeral(double taxaIntegridadeGeral) {
        this.taxaIntegridadeGeral = taxaIntegridadeGeral;
    }
    
    public static double getLimiarAlerta() {
        return LIMIAR_ALERTA;
    }
    
    @Override
    public String toString() {
        return "EstatisticasIntegridade{" +
                "total=" + totalConjuntos +
                ", completos=" + conjuntosCompletos +
                ", incompletos=" + conjuntosIncompletos +
                ", taxa=" + getTaxaFormatada() +
                ", alerta=" + isAlerta() +
                '}';
    }
}
