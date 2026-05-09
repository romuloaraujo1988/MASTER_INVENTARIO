package com.inventario.sihcp.analytics.model;

/**
 * KPIs (Key Performance Indicators) do Dashboard de Analytics.
 * 
 * Contém métricas principais do inventário e comparação com período anterior.
 */
public class AnalyticsKPIs {
    
    private double taxaDivergenciaGeral;      // Percentual
    private double tempoMedioColeta;          // Segundos
    private double coletasPorHora;            // Média
    private int coletoresAtivos;              // Contagem
    private int totalDivergencias;            // Contagem
    private int totalColetas;                 // Contagem
    
    // Comparação com período anterior (positivo = piorou)
    private Double variacaoTaxaDivergencia;
    private Double variacaoTempoMedio;
    
    // Flag para indicar ausência de dados
    private boolean semDados;
    
    public AnalyticsKPIs() {
    }
    
    /**
     * Cria instância vazia para quando não há dados.
     */
    public static AnalyticsKPIs empty() {
        AnalyticsKPIs kpis = new AnalyticsKPIs();
        kpis.semDados = true;
        return kpis;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final AnalyticsKPIs kpis = new AnalyticsKPIs();
        
        public Builder taxaDivergenciaGeral(double taxa) {
            kpis.taxaDivergenciaGeral = taxa;
            return this;
        }
        
        public Builder tempoMedioColeta(double tempo) {
            kpis.tempoMedioColeta = tempo;
            return this;
        }
        
        public Builder coletasPorHora(double coletas) {
            kpis.coletasPorHora = coletas;
            return this;
        }
        
        public Builder coletoresAtivos(int coletores) {
            kpis.coletoresAtivos = coletores;
            return this;
        }
        
        public Builder totalDivergencias(int total) {
            kpis.totalDivergencias = total;
            return this;
        }
        
        public Builder totalColetas(int total) {
            kpis.totalColetas = total;
            return this;
        }
        
        public Builder variacaoTaxaDivergencia(Double variacao) {
            kpis.variacaoTaxaDivergencia = variacao;
            return this;
        }
        
        public Builder variacaoTempoMedio(Double variacao) {
            kpis.variacaoTempoMedio = variacao;
            return this;
        }
        
        public AnalyticsKPIs build() {
            return kpis;
        }
    }
    
    // Getters e Setters
    
    public double getTaxaDivergenciaGeral() {
        return taxaDivergenciaGeral;
    }
    
    public void setTaxaDivergenciaGeral(double taxaDivergenciaGeral) {
        this.taxaDivergenciaGeral = taxaDivergenciaGeral;
    }
    
    public double getTempoMedioColeta() {
        return tempoMedioColeta;
    }
    
    public void setTempoMedioColeta(double tempoMedioColeta) {
        this.tempoMedioColeta = tempoMedioColeta;
    }
    
    public double getColetasPorHora() {
        return coletasPorHora;
    }
    
    public void setColetasPorHora(double coletasPorHora) {
        this.coletasPorHora = coletasPorHora;
    }
    
    public int getColetoresAtivos() {
        return coletoresAtivos;
    }
    
    public void setColetoresAtivos(int coletoresAtivos) {
        this.coletoresAtivos = coletoresAtivos;
    }
    
    public int getTotalDivergencias() {
        return totalDivergencias;
    }
    
    public void setTotalDivergencias(int totalDivergencias) {
        this.totalDivergencias = totalDivergencias;
    }
    
    public int getTotalColetas() {
        return totalColetas;
    }
    
    public void setTotalColetas(int totalColetas) {
        this.totalColetas = totalColetas;
    }
    
    public Double getVariacaoTaxaDivergencia() {
        return variacaoTaxaDivergencia;
    }
    
    public void setVariacaoTaxaDivergencia(Double variacaoTaxaDivergencia) {
        this.variacaoTaxaDivergencia = variacaoTaxaDivergencia;
    }
    
    public Double getVariacaoTempoMedio() {
        return variacaoTempoMedio;
    }
    
    public void setVariacaoTempoMedio(Double variacaoTempoMedio) {
        this.variacaoTempoMedio = variacaoTempoMedio;
    }
    
    public boolean isSemDados() {
        return semDados;
    }
    
    public void setSemDados(boolean semDados) {
        this.semDados = semDados;
    }
    
    /**
     * Verifica se a taxa de divergência melhorou em relação ao período anterior.
     */
    public boolean taxaDivergenciaMelhorou() {
        return variacaoTaxaDivergencia != null && variacaoTaxaDivergencia < 0;
    }
    
    /**
     * Verifica se o tempo médio de coleta melhorou em relação ao período anterior.
     */
    public boolean tempoMedioMelhorou() {
        return variacaoTempoMedio != null && variacaoTempoMedio < 0;
    }
    
    @Override
    public String toString() {
        return "AnalyticsKPIs{" +
                "taxaDivergenciaGeral=" + taxaDivergenciaGeral +
                ", tempoMedioColeta=" + tempoMedioColeta +
                ", coletasPorHora=" + coletasPorHora +
                ", coletoresAtivos=" + coletoresAtivos +
                ", totalDivergencias=" + totalDivergencias +
                ", totalColetas=" + totalColetas +
                '}';
    }
}
