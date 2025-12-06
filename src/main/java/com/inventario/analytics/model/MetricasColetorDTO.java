package com.inventario.analytics.model;

/**
 * Métricas de tempo de coleta por coletor.
 * 
 * Contém estatísticas de tempo (média, mín, máx, desvio padrão)
 * para um coletor específico.
 */
public class MetricasColetorDTO {
    
    private int idColetor;
    private String nomeColetor;
    private int totalColetas;
    private double tempoMedio;                // Segundos
    private int tempoMinimo;                  // Segundos
    private int tempoMaximo;                  // Segundos
    private double desvioPadrao;              // Segundos
    private int coletasComTempo;              // Coletas com métrica disponível
    private double percentualComTempo;        // % de coletas com tempo
    private int posicaoRanking;               // Posição no ranking (1 = mais rápido)
    
    public MetricasColetorDTO() {
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final MetricasColetorDTO dto = new MetricasColetorDTO();
        
        public Builder idColetor(int id) {
            dto.idColetor = id;
            return this;
        }
        
        public Builder nomeColetor(String nome) {
            dto.nomeColetor = nome;
            return this;
        }
        
        public Builder totalColetas(int total) {
            dto.totalColetas = total;
            return this;
        }
        
        public Builder tempoMedio(double tempo) {
            dto.tempoMedio = tempo;
            return this;
        }
        
        public Builder tempoMinimo(int tempo) {
            dto.tempoMinimo = tempo;
            return this;
        }
        
        public Builder tempoMaximo(int tempo) {
            dto.tempoMaximo = tempo;
            return this;
        }
        
        public Builder desvioPadrao(double desvio) {
            dto.desvioPadrao = desvio;
            return this;
        }
        
        public Builder coletasComTempo(int coletas) {
            dto.coletasComTempo = coletas;
            return this;
        }
        
        public Builder percentualComTempo(double percentual) {
            dto.percentualComTempo = percentual;
            return this;
        }
        
        public Builder posicaoRanking(int posicao) {
            dto.posicaoRanking = posicao;
            return this;
        }
        
        public MetricasColetorDTO build() {
            return dto;
        }
    }
    
    // Getters e Setters
    
    public int getIdColetor() {
        return idColetor;
    }
    
    public void setIdColetor(int idColetor) {
        this.idColetor = idColetor;
    }
    
    public String getNomeColetor() {
        return nomeColetor;
    }
    
    public void setNomeColetor(String nomeColetor) {
        this.nomeColetor = nomeColetor;
    }
    
    public int getTotalColetas() {
        return totalColetas;
    }
    
    public void setTotalColetas(int totalColetas) {
        this.totalColetas = totalColetas;
    }
    
    public double getTempoMedio() {
        return tempoMedio;
    }
    
    public void setTempoMedio(double tempoMedio) {
        this.tempoMedio = tempoMedio;
    }
    
    public int getTempoMinimo() {
        return tempoMinimo;
    }
    
    public void setTempoMinimo(int tempoMinimo) {
        this.tempoMinimo = tempoMinimo;
    }
    
    public int getTempoMaximo() {
        return tempoMaximo;
    }
    
    public void setTempoMaximo(int tempoMaximo) {
        this.tempoMaximo = tempoMaximo;
    }
    
    public double getDesvioPadrao() {
        return desvioPadrao;
    }
    
    public void setDesvioPadrao(double desvioPadrao) {
        this.desvioPadrao = desvioPadrao;
    }
    
    public int getColetasComTempo() {
        return coletasComTempo;
    }
    
    public void setColetasComTempo(int coletasComTempo) {
        this.coletasComTempo = coletasComTempo;
    }
    
    public double getPercentualComTempo() {
        return percentualComTempo;
    }
    
    public void setPercentualComTempo(double percentualComTempo) {
        this.percentualComTempo = percentualComTempo;
    }
    
    public int getPosicaoRanking() {
        return posicaoRanking;
    }
    
    public void setPosicaoRanking(int posicaoRanking) {
        this.posicaoRanking = posicaoRanking;
    }
    
    /**
     * Verifica se o coletor tem cobertura de métricas adequada (>= 50%).
     */
    public boolean temCoberturaAdequada() {
        return percentualComTempo >= 50.0;
    }
    
    /**
     * Retorna o tempo médio formatado (ex: "1m 30s" ou "45s").
     */
    public String getTempoMedioFormatado() {
        if (tempoMedio < 60) {
            return String.format("%.0fs", tempoMedio);
        } else {
            int minutos = (int) (tempoMedio / 60);
            int segundos = (int) (tempoMedio % 60);
            return String.format("%dm %ds", minutos, segundos);
        }
    }
    
    @Override
    public String toString() {
        return "MetricasColetorDTO{" +
                "nomeColetor='" + nomeColetor + '\'' +
                ", totalColetas=" + totalColetas +
                ", tempoMedio=" + tempoMedio +
                '}';
    }
}
