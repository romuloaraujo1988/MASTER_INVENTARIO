package com.inventario.sihcp.analytics.model;

/**
 * Ranking de setor por taxa de divergência.
 * 
 * Usado para identificar setores com maior taxa de problemas.
 */
public class RankingSetorDTO {
    
    private int idSetor;
    private String nomeSetor;
    private int totalColetas;
    private int totalDivergencias;
    private double taxaDivergencia;           // Percentual
    private int posicaoRanking;
    
    public RankingSetorDTO() {
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final RankingSetorDTO dto = new RankingSetorDTO();
        
        public Builder idSetor(int id) {
            dto.idSetor = id;
            return this;
        }
        
        public Builder nomeSetor(String nome) {
            dto.nomeSetor = nome;
            return this;
        }
        
        public Builder totalColetas(int total) {
            dto.totalColetas = total;
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
        
        public RankingSetorDTO build() {
            return dto;
        }
    }
    
    // Getters e Setters
    
    public int getIdSetor() {
        return idSetor;
    }
    
    public void setIdSetor(int idSetor) {
        this.idSetor = idSetor;
    }
    
    public String getNomeSetor() {
        return nomeSetor;
    }
    
    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }
    
    public int getTotalColetas() {
        return totalColetas;
    }
    
    public void setTotalColetas(int totalColetas) {
        this.totalColetas = totalColetas;
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
        if (totalColetas > 0) {
            this.taxaDivergencia = (totalDivergencias * 100.0) / totalColetas;
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
    
    /**
     * Verifica se o setor tem taxa de divergência alta (> 10%).
     */
    public boolean isTaxaAlta() {
        return taxaDivergencia > 10.0;
    }
    
    /**
     * Verifica se o setor tem taxa de divergência crítica (> 20%).
     */
    public boolean isTaxaCritica() {
        return taxaDivergencia > 20.0;
    }
    
    @Override
    public String toString() {
        return "RankingSetorDTO{" +
                "nomeSetor='" + nomeSetor + '\'' +
                ", totalColetas=" + totalColetas +
                ", totalDivergencias=" + totalDivergencias +
                ", taxaDivergencia=" + taxaDivergencia +
                '}';
    }
}
