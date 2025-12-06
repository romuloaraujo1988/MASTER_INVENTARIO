package com.inventario.analytics.model;

/**
 * Ranking de responsável por quantidade de divergências.
 * 
 * Usado para identificar responsáveis com maior número de patrimônios divergentes.
 */
public class RankingResponsavelDTO {
    
    private int idResponsavel;
    private String nomeResponsavel;
    private int totalPatrimonios;
    private int totalDivergencias;
    private double taxaDivergencia;           // Percentual
    private int posicaoRanking;
    
    public RankingResponsavelDTO() {
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final RankingResponsavelDTO dto = new RankingResponsavelDTO();
        
        public Builder idResponsavel(int id) {
            dto.idResponsavel = id;
            return this;
        }
        
        public Builder nomeResponsavel(String nome) {
            dto.nomeResponsavel = nome;
            return this;
        }
        
        public Builder totalPatrimonios(int total) {
            dto.totalPatrimonios = total;
            return this;
        }
        
        public Builder totalDivergencias(int total) {
            dto.totalDivergencias = total;
            return this;
        }
        
        public Builder taxaDivergencia(double taxa) {
            dto.taxaDivergencia = taxa;
            return this;
        }
        
        public Builder posicaoRanking(int posicao) {
            dto.posicaoRanking = posicao;
            return this;
        }
        
        public RankingResponsavelDTO build() {
            return dto;
        }
    }
    
    // Getters e Setters
    
    public int getIdResponsavel() {
        return idResponsavel;
    }
    
    public void setIdResponsavel(int idResponsavel) {
        this.idResponsavel = idResponsavel;
    }
    
    public String getNomeResponsavel() {
        return nomeResponsavel;
    }
    
    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }
    
    public int getTotalPatrimonios() {
        return totalPatrimonios;
    }
    
    public void setTotalPatrimonios(int totalPatrimonios) {
        this.totalPatrimonios = totalPatrimonios;
    }
    
    public int getTotalDivergencias() {
        return totalDivergencias;
    }
    
    public void setTotalDivergencias(int totalDivergencias) {
        this.totalDivergencias = totalDivergencias;
    }
    
    public double getTaxaDivergencia() {
        return taxaDivergencia;
    }
    
    public void setTaxaDivergencia(double taxaDivergencia) {
        this.taxaDivergencia = taxaDivergencia;
    }
    
    public int getPosicaoRanking() {
        return posicaoRanking;
    }
    
    public void setPosicaoRanking(int posicaoRanking) {
        this.posicaoRanking = posicaoRanking;
    }
    
    /**
     * Calcula a taxa de divergência baseada nos totais.
     */
    public void calcularTaxa() {
        if (totalPatrimonios > 0) {
            this.taxaDivergencia = (totalDivergencias * 100.0) / totalPatrimonios;
        } else {
            this.taxaDivergencia = 0.0;
        }
    }
    
    /**
     * Retorna a taxa formatada com 1 casa decimal.
     */
    public String getTaxaFormatada() {
        return String.format("%.1f%%", taxaDivergencia);
    }
    
    @Override
    public String toString() {
        return "RankingResponsavelDTO{" +
                "nomeResponsavel='" + nomeResponsavel + '\'' +
                ", totalDivergencias=" + totalDivergencias +
                '}';
    }
}
