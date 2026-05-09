package com.inventario.sihcp.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para representar a comparação entre duas coletas consecutivas.
 * Identifica mudanças de localização, estado e observações.
 */
public class ComparacaoColetaDTO {
    
    private HistoricoColetaDTO coletaAtual;
    private HistoricoColetaDTO coletaAnterior;
    private List<Mudanca> mudancas;
    
    // Construtores
    public ComparacaoColetaDTO() {
        this.mudancas = new ArrayList<>();
    }
    
    public ComparacaoColetaDTO(HistoricoColetaDTO coletaAtual, HistoricoColetaDTO coletaAnterior) {
        this();
        this.coletaAtual = coletaAtual;
        this.coletaAnterior = coletaAnterior;
    }
    
    // Getters e Setters
    public HistoricoColetaDTO getColetaAtual() {
        return coletaAtual;
    }
    
    public void setColetaAtual(HistoricoColetaDTO coletaAtual) {
        this.coletaAtual = coletaAtual;
    }
    
    public HistoricoColetaDTO getColetaAnterior() {
        return coletaAnterior;
    }
    
    public void setColetaAnterior(HistoricoColetaDTO coletaAnterior) {
        this.coletaAnterior = coletaAnterior;
    }
    
    public List<Mudanca> getMudancas() {
        return mudancas;
    }
    
    public void setMudancas(List<Mudanca> mudancas) {
        this.mudancas = mudancas;
    }
    
    public void adicionarMudanca(Mudanca mudanca) {
        this.mudancas.add(mudanca);
    }
    
    /**
     * Verifica se há alguma mudança entre as coletas
     */
    public boolean temMudancas() {
        return mudancas != null && !mudancas.isEmpty();
    }
    
    /**
     * Classe interna para representar uma mudança específica
     */
    public static class Mudanca {
        private String campo;
        private String valorAnterior;
        private String valorAtual;
        private TipoMudanca tipo;
        
        public Mudanca() {
        }
        
        public Mudanca(String campo, String valorAnterior, String valorAtual, TipoMudanca tipo) {
            this.campo = campo;
            this.valorAnterior = valorAnterior;
            this.valorAtual = valorAtual;
            this.tipo = tipo;
        }
        
        // Getters e Setters
        public String getCampo() {
            return campo;
        }
        
        public void setCampo(String campo) {
            this.campo = campo;
        }
        
        public String getValorAnterior() {
            return valorAnterior;
        }
        
        public void setValorAnterior(String valorAnterior) {
            this.valorAnterior = valorAnterior;
        }
        
        public String getValorAtual() {
            return valorAtual;
        }
        
        public void setValorAtual(String valorAtual) {
            this.valorAtual = valorAtual;
        }
        
        public TipoMudanca getTipo() {
            return tipo;
        }
        
        public void setTipo(TipoMudanca tipo) {
            this.tipo = tipo;
        }
        
        @Override
        public String toString() {
            return "Mudanca{" +
                    "campo='" + campo + '\'' +
                    ", valorAnterior='" + valorAnterior + '\'' +
                    ", valorAtual='" + valorAtual + '\'' +
                    ", tipo=" + tipo +
                    '}';
        }
    }
    
    /**
     * Enum para tipos de mudança
     */
    public enum TipoMudanca {
        LOCALIZACAO("Localização"),
        ESTADO("Estado"),
        OBSERVACAO("Observação");
        
        private final String descricao;
        
        TipoMudanca(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    @Override
    public String toString() {
        return "ComparacaoColetaDTO{" +
                "coletaAtual=" + (coletaAtual != null ? coletaAtual.getId() : null) +
                ", coletaAnterior=" + (coletaAnterior != null ? coletaAnterior.getId() : null) +
                ", mudancas=" + mudancas.size() +
                '}';
    }
}
