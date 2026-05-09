package com.inventario.sihcp.analytics.model;

import java.time.LocalDateTime;

/**
 * Divergência com análise de tipo e gravidade.
 * 
 * Representa uma divergência detectada durante a coleta,
 * incluindo classificação de gravidade e informações contextuais.
 */
public class DivergenciaAnalytics {
    
    private int idColeta;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    private TipoDivergencia tipo;
    private GravidadeDivergencia gravidade;
    private String valorCadastrado;
    private String valorEncontrado;
    private String motivo;
    private LocalDateTime dataColeta;
    private String nomeSetor;
    private Integer idSetor;
    private String nomeResponsavel;
    private Integer idResponsavel;
    private String nomeColetor;
    private Integer idColetor;
    
    public DivergenciaAnalytics() {
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final DivergenciaAnalytics div = new DivergenciaAnalytics();
        
        public Builder idColeta(int id) {
            div.idColeta = id;
            return this;
        }
        
        public Builder numeroPatrimonio(String numero) {
            div.numeroPatrimonio = numero;
            return this;
        }
        
        public Builder descricaoPatrimonio(String descricao) {
            div.descricaoPatrimonio = descricao;
            return this;
        }
        
        public Builder tipo(TipoDivergencia tipo) {
            div.tipo = tipo;
            return this;
        }
        
        public Builder gravidade(GravidadeDivergencia gravidade) {
            div.gravidade = gravidade;
            return this;
        }
        
        public Builder valorCadastrado(String valor) {
            div.valorCadastrado = valor;
            return this;
        }
        
        public Builder valorEncontrado(String valor) {
            div.valorEncontrado = valor;
            return this;
        }
        
        public Builder motivo(String motivo) {
            div.motivo = motivo;
            return this;
        }
        
        public Builder dataColeta(LocalDateTime data) {
            div.dataColeta = data;
            return this;
        }
        
        public Builder nomeSetor(String nome) {
            div.nomeSetor = nome;
            return this;
        }
        
        public Builder idSetor(Integer id) {
            div.idSetor = id;
            return this;
        }
        
        public Builder nomeResponsavel(String nome) {
            div.nomeResponsavel = nome;
            return this;
        }
        
        public Builder idResponsavel(Integer id) {
            div.idResponsavel = id;
            return this;
        }
        
        public Builder nomeColetor(String nome) {
            div.nomeColetor = nome;
            return this;
        }
        
        public Builder idColetor(Integer id) {
            div.idColetor = id;
            return this;
        }
        
        public DivergenciaAnalytics build() {
            return div;
        }
    }
    
    // Getters e Setters
    
    public int getIdColeta() {
        return idColeta;
    }
    
    public void setIdColeta(int idColeta) {
        this.idColeta = idColeta;
    }
    
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
    
    public TipoDivergencia getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoDivergencia tipo) {
        this.tipo = tipo;
    }
    
    public GravidadeDivergencia getGravidade() {
        return gravidade;
    }
    
    public void setGravidade(GravidadeDivergencia gravidade) {
        this.gravidade = gravidade;
    }
    
    public String getValorCadastrado() {
        return valorCadastrado;
    }
    
    public void setValorCadastrado(String valorCadastrado) {
        this.valorCadastrado = valorCadastrado;
    }
    
    public String getValorEncontrado() {
        return valorEncontrado;
    }
    
    public void setValorEncontrado(String valorEncontrado) {
        this.valorEncontrado = valorEncontrado;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public LocalDateTime getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(LocalDateTime dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public String getNomeSetor() {
        return nomeSetor;
    }
    
    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }
    
    public Integer getIdSetor() {
        return idSetor;
    }
    
    public void setIdSetor(Integer idSetor) {
        this.idSetor = idSetor;
    }
    
    public String getNomeResponsavel() {
        return nomeResponsavel;
    }
    
    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }
    
    public Integer getIdResponsavel() {
        return idResponsavel;
    }
    
    public void setIdResponsavel(Integer idResponsavel) {
        this.idResponsavel = idResponsavel;
    }
    
    public String getNomeColetor() {
        return nomeColetor;
    }
    
    public void setNomeColetor(String nomeColetor) {
        this.nomeColetor = nomeColetor;
    }
    
    public Integer getIdColetor() {
        return idColetor;
    }
    
    public void setIdColetor(Integer idColetor) {
        this.idColetor = idColetor;
    }
    
    /**
     * Verifica se é uma divergência crítica ou alta.
     */
    public boolean isGrave() {
        return gravidade == GravidadeDivergencia.CRITICA || 
               gravidade == GravidadeDivergencia.ALTA;
    }
    
    @Override
    public String toString() {
        return "DivergenciaAnalytics{" +
                "numeroPatrimonio='" + numeroPatrimonio + '\'' +
                ", tipo=" + tipo +
                ", gravidade=" + gravidade +
                '}';
    }
}
