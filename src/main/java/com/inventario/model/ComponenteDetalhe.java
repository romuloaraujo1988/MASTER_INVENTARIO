package com.inventario.model;

import java.util.Date;

/**
 * Classe que representa o detalhe de um componente de um item composto.
 * Contém informações sobre a coleta individual de cada componente.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ComponenteDetalhe {
    
    // Identificação
    private Integer id;
    private Integer idItemComposto;
    
    // Informações do componente
    private String tipo;
    private String descricao;
    
    // Quantidades
    private int quantidadeEsperada;
    private int quantidadeEncontrada;
    
    // Status e observações
    private StatusComponente status;
    private String observacao;
    private String localizacaoEncontrada;
    
    // Informações da coleta
    private Date dataColeta;
    private String nomeColetor;
    private Integer idColetor;
    
    // Flag de obrigatoriedade
    private boolean obrigatorio;
    
    public ComponenteDetalhe() {
    }
    
    // === Métodos de Negócio ===
    
    /**
     * Calcula a quantidade faltante do componente
     * 
     * @return quantidade faltante (esperada - encontrada), mínimo 0
     */
    public int getQuantidadeFaltante() {
        int faltante = quantidadeEsperada - quantidadeEncontrada;
        return Math.max(0, faltante);
    }
    
    /**
     * Verifica se o componente está completo
     */
    public boolean isCompleto() {
        return quantidadeEncontrada >= quantidadeEsperada;
    }
    
    /**
     * Verifica se o componente está faltando completamente
     */
    public boolean isFaltante() {
        return quantidadeEncontrada == 0 && quantidadeEsperada > 0;
    }
    
    /**
     * Verifica se o componente foi parcialmente encontrado
     */
    public boolean isParcial() {
        return quantidadeEncontrada > 0 && quantidadeEncontrada < quantidadeEsperada;
    }
    
    /**
     * Calcula e retorna o status baseado nas quantidades
     */
    public StatusComponente calcularStatus() {
        if (dataColeta == null && quantidadeEncontrada == 0) {
            return StatusComponente.NAO_COLETADO;
        }
        return StatusComponente.fromQuantidades(quantidadeEncontrada, quantidadeEsperada);
    }
    
    /**
     * Retorna a descrição formatada do componente
     * Exemplo: "CADEIRA - Cadeira escolar"
     */
    public String getDescricaoCompleta() {
        if (descricao != null && !descricao.isEmpty()) {
            return tipo + " - " + descricao;
        }
        return tipo;
    }
    
    // === Getters e Setters ===
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getIdItemComposto() {
        return idItemComposto;
    }
    
    public void setIdItemComposto(Integer idItemComposto) {
        this.idItemComposto = idItemComposto;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public int getQuantidadeEsperada() {
        return quantidadeEsperada;
    }
    
    public void setQuantidadeEsperada(int quantidadeEsperada) {
        this.quantidadeEsperada = quantidadeEsperada;
    }
    
    public int getQuantidadeEncontrada() {
        return quantidadeEncontrada;
    }
    
    public void setQuantidadeEncontrada(int quantidadeEncontrada) {
        this.quantidadeEncontrada = quantidadeEncontrada;
    }
    
    public StatusComponente getStatus() {
        return status;
    }
    
    public void setStatus(StatusComponente status) {
        this.status = status;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    public String getLocalizacaoEncontrada() {
        return localizacaoEncontrada;
    }
    
    public void setLocalizacaoEncontrada(String localizacaoEncontrada) {
        this.localizacaoEncontrada = localizacaoEncontrada;
    }
    
    public Date getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(Date dataColeta) {
        this.dataColeta = dataColeta;
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
    
    public boolean isObrigatorio() {
        return obrigatorio;
    }
    
    public void setObrigatorio(boolean obrigatorio) {
        this.obrigatorio = obrigatorio;
    }
    
    @Override
    public String toString() {
        return "ComponenteDetalhe{" +
                "tipo='" + tipo + '\'' +
                ", esperado=" + quantidadeEsperada +
                ", encontrado=" + quantidadeEncontrada +
                ", status=" + status +
                '}';
    }
}
