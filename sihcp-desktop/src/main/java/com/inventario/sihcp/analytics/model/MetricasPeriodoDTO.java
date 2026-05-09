package com.inventario.sihcp.analytics.model;

/**
 * Métricas de coleta por período do dia ou dia da semana.
 * 
 * Usado para análise de produtividade por período.
 */
public class MetricasPeriodoDTO {
    
    /**
     * Períodos do dia para classificação de coletas.
     */
    public enum Periodo {
        MANHA("Manhã", 6, 12),
        TARDE("Tarde", 12, 18),
        NOITE("Noite", 18, 6);
        
        private final String descricao;
        private final int horaInicio;
        private final int horaFim;
        
        Periodo(String descricao, int horaInicio, int horaFim) {
            this.descricao = descricao;
            this.horaInicio = horaInicio;
            this.horaFim = horaFim;
        }
        
        public String getDescricao() {
            return descricao;
        }
        
        public int getHoraInicio() {
            return horaInicio;
        }
        
        public int getHoraFim() {
            return horaFim;
        }
        
        /**
         * Classifica uma hora no período correspondente.
         * 
         * @param hora Hora do dia (0-23)
         * @return Período correspondente
         */
        public static Periodo fromHora(int hora) {
            if (hora >= 6 && hora < 12) {
                return MANHA;
            } else if (hora >= 12 && hora < 18) {
                return TARDE;
            } else {
                return NOITE;
            }
        }
    }
    
    private String periodo;                   // "MANHA", "TARDE", "NOITE" ou dia da semana
    private int totalColetas;
    private double tempoMedio;                // Segundos
    private boolean baixaProdutividade;       // tempo > 1.5x média geral
    private int coletasComTempo;              // Coletas com métrica disponível
    
    public MetricasPeriodoDTO() {
    }
    
    public MetricasPeriodoDTO(String periodo, int totalColetas, double tempoMedio) {
        this.periodo = periodo;
        this.totalColetas = totalColetas;
        this.tempoMedio = tempoMedio;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final MetricasPeriodoDTO dto = new MetricasPeriodoDTO();
        
        public Builder periodo(String periodo) {
            dto.periodo = periodo;
            return this;
        }
        
        public Builder periodo(Periodo periodo) {
            dto.periodo = periodo.name();
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
        
        public Builder baixaProdutividade(boolean baixa) {
            dto.baixaProdutividade = baixa;
            return this;
        }
        
        public Builder coletasComTempo(int coletas) {
            dto.coletasComTempo = coletas;
            return this;
        }
        
        public MetricasPeriodoDTO build() {
            return dto;
        }
    }
    
    // Getters e Setters
    
    public String getPeriodo() {
        return periodo;
    }
    
    public void setPeriodo(String periodo) {
        this.periodo = periodo;
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
    
    public boolean isBaixaProdutividade() {
        return baixaProdutividade;
    }
    
    public void setBaixaProdutividade(boolean baixaProdutividade) {
        this.baixaProdutividade = baixaProdutividade;
    }
    
    public int getColetasComTempo() {
        return coletasComTempo;
    }
    
    public void setColetasComTempo(int coletasComTempo) {
        this.coletasComTempo = coletasComTempo;
    }
    
    /**
     * Verifica se este período tem baixa produtividade comparado à média geral.
     * 
     * @param mediaGeral Tempo médio geral de todas as coletas
     */
    public void avaliarProdutividade(double mediaGeral) {
        this.baixaProdutividade = tempoMedio > (mediaGeral * 1.5);
    }
    
    /**
     * Retorna o tempo médio formatado.
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
    
    /**
     * Retorna descrição amigável do período.
     */
    public String getPeriodoDescricao() {
        try {
            return Periodo.valueOf(periodo).getDescricao();
        } catch (IllegalArgumentException e) {
            return periodo; // Retorna o valor original se não for um Periodo válido
        }
    }
    
    @Override
    public String toString() {
        return "MetricasPeriodoDTO{" +
                "periodo='" + periodo + '\'' +
                ", totalColetas=" + totalColetas +
                ", tempoMedio=" + tempoMedio +
                ", baixaProdutividade=" + baixaProdutividade +
                '}';
    }
}
