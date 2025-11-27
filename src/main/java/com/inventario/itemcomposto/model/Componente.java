package com.inventario.itemcomposto.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa um componente físico individual de um item composto.
 * 
 * Um componente define um tipo de item físico que deve existir em um
 * item composto, incluindo a quantidade esperada.
 * Exemplo: "4 cadeiras" em um conjunto de mesa com cadeiras.
 * 
 * @author Sistema de Inventário Patrimonial
 * @version 1.0
 * @since 27/11/2025
 */
public class Componente {
    
    private Integer id;
    private Integer idItemComposto;
    private String tipo;
    private String descricao;
    private int quantidadeEsperada;
    private int ordem;
    
    // Cache de quantidades encontradas por inventário
    // Key: idInventario, Value: quantidadeEncontrada
    private Map<Integer, Integer> quantidadesEncontradas;
    
    /**
     * Construtor padrão
     */
    public Componente() {
        this.quantidadeEsperada = 1;
        this.ordem = 0;
        this.quantidadesEncontradas = new HashMap<>();
    }
    
    /**
     * Construtor com tipo e descrição
     * 
     * @param tipo Tipo do componente (ex: CADEIRA, MESA)
     * @param descricao Descrição detalhada
     */
    public Componente(String tipo, String descricao) {
        this();
        this.tipo = tipo;
        this.descricao = descricao;
    }
    
    /**
     * Construtor completo
     * 
     * @param tipo Tipo do componente
     * @param descricao Descrição detalhada
     * @param quantidadeEsperada Quantidade esperada
     */
    public Componente(String tipo, String descricao, int quantidadeEsperada) {
        this(tipo, descricao);
        this.quantidadeEsperada = quantidadeEsperada;
    }
    
    // ==================== MÉTODOS DE NEGÓCIO ====================
    
    /**
     * Retorna a quantidade encontrada deste componente em um inventário específico
     * 
     * @param idInventario ID do inventário
     * @return Quantidade encontrada (0 se não foi coletado)
     */
    public int getQuantidadeEncontrada(Integer idInventario) {
        if (idInventario == null || quantidadesEncontradas == null) {
            return 0;
        }
        return quantidadesEncontradas.getOrDefault(idInventario, 0);
    }
    
    /**
     * Define a quantidade encontrada em um inventário
     * 
     * @param idInventario ID do inventário
     * @param quantidade Quantidade encontrada
     */
    public void setQuantidadeEncontrada(Integer idInventario, int quantidade) {
        if (idInventario != null) {
            if (this.quantidadesEncontradas == null) {
                this.quantidadesEncontradas = new HashMap<>();
            }
            this.quantidadesEncontradas.put(idInventario, quantidade);
        }
    }
    
    /**
     * Retorna a quantidade faltante em um inventário
     * 
     * @param idInventario ID do inventário
     * @return Quantidade faltante (pode ser negativo se encontrou mais que o esperado)
     */
    public int getQuantidadeFaltante(Integer idInventario) {
        return quantidadeEsperada - getQuantidadeEncontrada(idInventario);
    }
    
    /**
     * Verifica se o componente está completo em um inventário
     * (quantidade encontrada >= quantidade esperada)
     * 
     * @param idInventario ID do inventário
     * @return true se completo
     */
    public boolean isCompleto(Integer idInventario) {
        return getQuantidadeEncontrada(idInventario) >= quantidadeEsperada;
    }
    
    /**
     * Verifica se o componente foi parcialmente encontrado em um inventário
     * (quantidade encontrada > 0 mas < quantidade esperada)
     * 
     * @param idInventario ID do inventário
     * @return true se parcial
     */
    public boolean isParcial(Integer idInventario) {
        int encontrada = getQuantidadeEncontrada(idInventario);
        return encontrada > 0 && encontrada < quantidadeEsperada;
    }
    
    /**
     * Verifica se o componente está totalmente faltante em um inventário
     * (quantidade encontrada = 0)
     * 
     * @param idInventario ID do inventário
     * @return true se faltante
     */
    public boolean isFaltante(Integer idInventario) {
        return getQuantidadeEncontrada(idInventario) == 0;
    }
    
    /**
     * Calcula o percentual encontrado em um inventário
     * 
     * @param idInventario ID do inventário
     * @return Percentual (0.0 a 100.0)
     */
    public double getPercentualEncontrado(Integer idInventario) {
        if (quantidadeEsperada == 0) {
            return 0.0;
        }
        int encontrada = getQuantidadeEncontrada(idInventario);
        return Math.min(100.0, (encontrada * 100.0) / quantidadeEsperada);
    }
    
    /**
     * Retorna o status do componente em um inventário
     * 
     * @param idInventario ID do inventário
     * @return "COMPLETO", "PARCIAL" ou "FALTANTE"
     */
    public String getStatus(Integer idInventario) {
        if (isCompleto(idInventario)) {
            return "COMPLETO";
        } else if (isParcial(idInventario)) {
            return "PARCIAL";
        } else {
            return "FALTANTE";
        }
    }
    
    /**
     * Valida se o componente está em um estado válido
     * 
     * @return true se válido
     * @throws IllegalStateException se inválido
     */
    public boolean validar() {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalStateException("Tipo do componente é obrigatório");
        }
        if (descricao == null || descricao.trim().length() < 3) {
            throw new IllegalStateException("Descrição do componente deve ter pelo menos 3 caracteres");
        }
        if (quantidadeEsperada <= 0) {
            throw new IllegalStateException("Quantidade esperada deve ser maior que zero");
        }
        return true;
    }
    
    // ==================== GETTERS E SETTERS ====================
    
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
    
    public int getOrdem() {
        return ordem;
    }
    
    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
    
    public Map<Integer, Integer> getQuantidadesEncontradas() {
        return quantidadesEncontradas;
    }
    
    public void setQuantidadesEncontradas(Map<Integer, Integer> quantidadesEncontradas) {
        this.quantidadesEncontradas = quantidadesEncontradas;
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    @Override
    public String toString() {
        return String.format("Componente[id=%d, tipo=%s, descricao=%s, qtdEsperada=%d]",
                id, tipo, descricao, quantidadeEsperada);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Componente other = (Componente) obj;
        return id != null && id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
