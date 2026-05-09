package com.inventario.sihcp.itemcomposto.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Representa um patrimônio composto por múltiplos componentes físicos.
 * 
 * Um item composto é um patrimônio que consiste em dois ou mais itens físicos
 * distintos registrados sob um único número de patrimônio.
 * Exemplo: Mesa com 4 cadeiras, Estação de trabalho completa, etc.
 * 
 * @author Sistema de Inventário Patrimonial
 * @version 1.0
 * @since 27/11/2025
 */
public class ItemComposto {
    
    private Integer id;
    private Integer idPatrimonio;
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    private boolean deteccaoAutomatica;
    private Date dataCriacao;
    private Integer idUsuarioCriacao;
    private String nomeUsuarioCriacao;
    private String observacoes;
    private List<Componente> componentes;
    
    // Campos adicionais para relatórios (joins)
    private Integer idSala;
    private String nomeSala;
    private String numeroSala;
    private Integer idSetor;
    private String nomeSetor;
    private Integer idResponsavel;
    private String nomeResponsavel;
    
    /**
     * Construtor padrão
     */
    public ItemComposto() {
        this.componentes = new ArrayList<>();
        this.dataCriacao = new Date();
        this.deteccaoAutomatica = false;
    }
    
    /**
     * Construtor com ID do patrimônio
     * 
     * @param idPatrimonio ID do patrimônio principal
     */
    public ItemComposto(Integer idPatrimonio) {
        this();
        this.idPatrimonio = idPatrimonio;
    }
    
    // ==================== MÉTODOS DE NEGÓCIO ====================
    
    /**
     * Retorna o total de componentes deste item composto
     * 
     * @return Quantidade de componentes
     */
    public int getTotalComponentes() {
        return componentes != null ? componentes.size() : 0;
    }
    
    /**
     * Retorna a quantidade total esperada de itens físicos
     * (soma das quantidades esperadas de todos os componentes)
     * 
     * @return Total de itens físicos esperados
     */
    public int getTotalItensEsperados() {
        if (componentes == null || componentes.isEmpty()) {
            return 0;
        }
        return componentes.stream()
                .mapToInt(Componente::getQuantidadeEsperada)
                .sum();
    }
    
    /**
     * Verifica se o item composto está completo em um inventário específico
     * (todos os componentes foram encontrados na quantidade esperada)
     * 
     * @param idInventario ID do inventário
     * @return true se todos os componentes estão completos
     */
    public boolean isCompleto(Integer idInventario) {
        if (componentes == null || componentes.isEmpty()) {
            return false;
        }
        return componentes.stream()
                .allMatch(c -> c.isCompleto(idInventario));
    }
    
    /**
     * Calcula a taxa de integridade do item composto em um inventário
     * (percentual de componentes encontrados em relação ao esperado)
     * 
     * @param idInventario ID do inventário
     * @return Taxa de integridade (0.0 a 100.0)
     */
    public double getTaxaIntegridade(Integer idInventario) {
        if (componentes == null || componentes.isEmpty()) {
            return 0.0;
        }
        
        int totalEsperado = getTotalItensEsperados();
        if (totalEsperado == 0) {
            return 0.0;
        }
        
        int totalEncontrado = componentes.stream()
                .mapToInt(c -> c.getQuantidadeEncontrada(idInventario))
                .sum();
        
        return (totalEncontrado * 100.0) / totalEsperado;
    }
    
    /**
     * Retorna a quantidade total de itens encontrados em um inventário
     * 
     * @param idInventario ID do inventário
     * @return Total de itens encontrados
     */
    public int getTotalItensEncontrados(Integer idInventario) {
        if (componentes == null || componentes.isEmpty()) {
            return 0;
        }
        return componentes.stream()
                .mapToInt(c -> c.getQuantidadeEncontrada(idInventario))
                .sum();
    }
    
    /**
     * Retorna a quantidade total de itens faltantes em um inventário
     * 
     * @param idInventario ID do inventário
     * @return Total de itens faltantes
     */
    public int getTotalItensFaltantes(Integer idInventario) {
        return getTotalItensEsperados() - getTotalItensEncontrados(idInventario);
    }
    
    /**
     * Adiciona um componente ao item composto
     * 
     * @param componente Componente a ser adicionado
     */
    public void adicionarComponente(Componente componente) {
        if (this.componentes == null) {
            this.componentes = new ArrayList<>();
        }
        componente.setIdItemComposto(this.id);
        this.componentes.add(componente);
    }
    
    /**
     * Remove um componente do item composto
     * 
     * @param componente Componente a ser removido
     * @return true se o componente foi removido
     */
    public boolean removerComponente(Componente componente) {
        if (this.componentes == null) {
            return false;
        }
        return this.componentes.remove(componente);
    }
    
    /**
     * Valida se o item composto está em um estado válido
     * 
     * @return true se válido
     * @throws IllegalStateException se inválido
     */
    public boolean validar() {
        if (idPatrimonio == null) {
            throw new IllegalStateException("ID do patrimônio é obrigatório");
        }
        if (componentes == null || componentes.isEmpty()) {
            throw new IllegalStateException("Item composto deve ter pelo menos um componente");
        }
        // Validar cada componente
        componentes.forEach(Componente::validar);
        return true;
    }
    
    // ==================== GETTERS E SETTERS ====================
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
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
    
    public boolean isDeteccaoAutomatica() {
        return deteccaoAutomatica;
    }
    
    public void setDeteccaoAutomatica(boolean deteccaoAutomatica) {
        this.deteccaoAutomatica = deteccaoAutomatica;
    }
    
    public Date getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public Integer getIdUsuarioCriacao() {
        return idUsuarioCriacao;
    }
    
    public void setIdUsuarioCriacao(Integer idUsuarioCriacao) {
        this.idUsuarioCriacao = idUsuarioCriacao;
    }
    
    public String getNomeUsuarioCriacao() {
        return nomeUsuarioCriacao;
    }
    
    public void setNomeUsuarioCriacao(String nomeUsuarioCriacao) {
        this.nomeUsuarioCriacao = nomeUsuarioCriacao;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public List<Componente> getComponentes() {
        return componentes;
    }
    
    public void setComponentes(List<Componente> componentes) {
        this.componentes = componentes;
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
    
    public String getNumeroSala() {
        return numeroSala;
    }
    
    public void setNumeroSala(String numeroSala) {
        this.numeroSala = numeroSala;
    }
    
    public Integer getIdSetor() {
        return idSetor;
    }
    
    public void setIdSetor(Integer idSetor) {
        this.idSetor = idSetor;
    }
    
    public String getNomeSetor() {
        return nomeSetor;
    }
    
    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
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
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    @Override
    public String toString() {
        return String.format("ItemComposto[id=%d, patrimonio=%s, componentes=%d, deteccaoAuto=%b]",
                id, numeroPatrimonio, getTotalComponentes(), deteccaoAutomatica);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ItemComposto other = (ItemComposto) obj;
        return id != null && id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
