package com.inventario.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa o resumo de integridade de um item composto.
 * Contém informações agregadas sobre os componentes de um patrimônio.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ItemCompostoResumo {
    
    // Identificação do patrimônio
    private Integer idPatrimonio;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    
    // Localização
    private Integer idSala;
    private String nomeSala;
    
    // Responsável
    private Integer idResponsavel;
    private String nomeResponsavel;
    
    // Contagens de componentes
    private int totalComponentes;
    private int componentesEsperados;
    private int componentesEncontrados;
    private int componentesFaltantes;
    
    // Status e taxa
    private double taxaIntegridade;
    private StatusIntegridade status;
    
    // Lista de tipos de componentes faltantes
    private List<String> tiposFaltantes;
    
    public ItemCompostoResumo() {
        this.tiposFaltantes = new ArrayList<>();
    }
    
    // === Métodos de Negócio ===
    
    /**
     * Verifica se o item composto está completo (todos os componentes encontrados)
     */
    public boolean isCompleto() {
        return componentesFaltantes == 0 && componentesEncontrados >= componentesEsperados;
    }
    
    /**
     * Retorna os tipos de componentes faltantes formatados como string
     * Exemplo: "CADEIRA, MESA, MONITOR"
     */
    public String getComponentesFaltantesFormatado() {
        if (tiposFaltantes == null || tiposFaltantes.isEmpty()) {
            return "-";
        }
        return String.join(", ", tiposFaltantes);
    }
    
    /**
     * Calcula e atualiza a taxa de integridade baseado nas contagens
     */
    public void calcularTaxaIntegridade() {
        if (componentesEsperados == 0) {
            this.taxaIntegridade = 100.0;
        } else {
            this.taxaIntegridade = (componentesEncontrados * 100.0) / componentesEsperados;
        }
        
        // Atualizar status baseado na taxa
        this.status = StatusIntegridade.fromTaxa(taxaIntegridade);
    }
    
    /**
     * Retorna a taxa de integridade formatada como string
     * Exemplo: "85.5%"
     */
    public String getTaxaIntegridadeFormatada() {
        return String.format("%.1f%%", taxaIntegridade);
    }
    
    // === Getters e Setters ===
    
    public Integer getIdPatrimonio() {
        return idPatrimonio;
    }
    
    public void setIdPatrimonio(Integer idPatrimonio) {
        this.idPatrimonio = idPatrimonio;
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
    
    public Integer getIdSala() {
        return idSala;
    }
    
    public void setIdSala(Integer idSala) {
        this.idSala = idSala;
    }
    
    public String getNomeSala() {
        return nomeSala;
    }
    
    public void setNomeSala(String nomeSala) {
        this.nomeSala = nomeSala;
    }
    
    public Integer getIdResponsavel() {
        return idResponsavel;
    }
    
    public void setIdResponsavel(Integer idResponsavel) {
        this.idResponsavel = idResponsavel;
    }
    
    public String getNomeResponsavel() {
        return nomeResponsavel;
    }
    
    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }
    
    public int getTotalComponentes() {
        return totalComponentes;
    }
    
    public void setTotalComponentes(int totalComponentes) {
        this.totalComponentes = totalComponentes;
    }
    
    public int getComponentesEsperados() {
        return componentesEsperados;
    }
    
    public void setComponentesEsperados(int componentesEsperados) {
        this.componentesEsperados = componentesEsperados;
    }
    
    public int getComponentesEncontrados() {
        return componentesEncontrados;
    }
    
    public void setComponentesEncontrados(int componentesEncontrados) {
        this.componentesEncontrados = componentesEncontrados;
    }
    
    public int getComponentesFaltantes() {
        return componentesFaltantes;
    }
    
    public void setComponentesFaltantes(int componentesFaltantes) {
        this.componentesFaltantes = componentesFaltantes;
    }
    
    public double getTaxaIntegridade() {
        return taxaIntegridade;
    }
    
    public void setTaxaIntegridade(double taxaIntegridade) {
        this.taxaIntegridade = taxaIntegridade;
    }
    
    public StatusIntegridade getStatus() {
        return status;
    }
    
    public void setStatus(StatusIntegridade status) {
        this.status = status;
    }
    
    public List<String> getTiposFaltantes() {
        return tiposFaltantes;
    }
    
    public void setTiposFaltantes(List<String> tiposFaltantes) {
        this.tiposFaltantes = tiposFaltantes;
    }
    
    public void addTipoFaltante(String tipo) {
        if (this.tiposFaltantes == null) {
            this.tiposFaltantes = new ArrayList<>();
        }
        this.tiposFaltantes.add(tipo);
    }
    
    @Override
    public String toString() {
        return "ItemCompostoResumo{" +
                "numeroPatrimonio='" + numeroPatrimonio + '\'' +
                ", descricao='" + descricaoPatrimonio + '\'' +
                ", sala='" + nomeSala + '\'' +
                ", taxa=" + getTaxaIntegridadeFormatada() +
                ", status=" + status +
                '}';
    }
}
