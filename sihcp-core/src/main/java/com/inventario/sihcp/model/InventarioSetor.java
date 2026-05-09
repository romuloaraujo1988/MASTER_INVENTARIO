package com.inventario.sihcp.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa o relacionamento entre Inventário e Setor
 * Corresponde à TABELA_INVENTARIO_SETOR no banco de dados
 * 
 * Esta classe controla quais setores fazem parte do escopo de cada inventário,
 * permitindo inventários parciais ou completos.
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0
 */
public class InventarioSetor {
    
    private Integer id;
    private Integer idInventario;
    private Integer idSetor; // NULL quando incluirTodosSetores = true
    private Boolean incluirTodosSetores;
    private LocalDateTime dataInclusao;
    private Boolean ativo;
    private String observacoes;
    
    // Campos auxiliares para exibição (JOINs)
    private String nomeInventario;
    private String nomeSetor;
    
    /**
     * Construtor padrão
     */
    public InventarioSetor() {
        this.incluirTodosSetores = false;
        this.ativo = true;
        this.dataInclusao = LocalDateTime.now();
    }
    
    /**
     * Construtor para relacionamento específico inventário-setor
     */
    public InventarioSetor(Integer idInventario, Integer idSetor) {
        this();
        this.idInventario = idInventario;
        this.idSetor = idSetor;
        this.incluirTodosSetores = false;
    }
    
    /**
     * Construtor para configuração "incluir todos os setores"
     */
    public InventarioSetor(Integer idInventario, Boolean incluirTodosSetores) {
        this();
        this.idInventario = idInventario;
        this.incluirTodosSetores = incluirTodosSetores;
        if (incluirTodosSetores) {
            this.idSetor = null;
        }
    }
    
    /**
     * Construtor completo
     */
    public InventarioSetor(Integer id, Integer idInventario, Integer idSetor, 
                          Boolean incluirTodosSetores, LocalDateTime dataInclusao, 
                          Boolean ativo, String observacoes) {
        this.id = id;
        this.idInventario = idInventario;
        this.idSetor = idSetor;
        this.incluirTodosSetores = incluirTodosSetores;
        this.dataInclusao = dataInclusao;
        this.ativo = ativo;
        this.observacoes = observacoes;
    }
    
    // Getters e Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public Integer getIdSetor() {
        return idSetor;
    }
    
    public void setIdSetor(Integer idSetor) {
        this.idSetor = idSetor;
        // Se definir um setor específico, não pode incluir todos
        if (idSetor != null) {
            this.incluirTodosSetores = false;
        }
    }
    
    public Boolean getIncluirTodosSetores() {
        return incluirTodosSetores;
    }
    
    public void setIncluirTodosSetores(Boolean incluirTodosSetores) {
        this.incluirTodosSetores = incluirTodosSetores;
        // Se incluir todos os setores, ID_SETOR deve ser NULL
        if (incluirTodosSetores != null && incluirTodosSetores) {
            this.idSetor = null;
        }
    }
    
    public LocalDateTime getDataInclusao() {
        return dataInclusao;
    }
    
    public void setDataInclusao(LocalDateTime dataInclusao) {
        this.dataInclusao = dataInclusao;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public String getNomeInventario() {
        return nomeInventario;
    }
    
    public void setNomeInventario(String nomeInventario) {
        this.nomeInventario = nomeInventario;
    }
    
    public String getNomeSetor() {
        return nomeSetor;
    }
    
    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }
    
    // Métodos de conveniência
    
    /**
     * Verifica se este relacionamento representa "incluir todos os setores"
     */
    public boolean isIncluirTodos() {
        return incluirTodosSetores != null && incluirTodosSetores;
    }
    
    /**
     * Verifica se este relacionamento é para um setor específico
     */
    public boolean isSetorEspecifico() {
        return !isIncluirTodos() && idSetor != null;
    }
    
    /**
     * Ativa o relacionamento
     */
    public void ativar() {
        this.ativo = true;
    }
    
    /**
     * Desativa o relacionamento
     */
    public void desativar() {
        this.ativo = false;
    }
    
    /**
     * Valida se o objeto está em um estado consistente
     */
    public boolean isValido() {
        // ID do inventário é obrigatório
        if (idInventario == null) {
            return false;
        }
        
        // Se incluir todos os setores, ID_SETOR deve ser NULL
        if (isIncluirTodos() && idSetor != null) {
            return false;
        }
        
        // Se não incluir todos, ID_SETOR deve estar definido
        if (!isIncluirTodos() && idSetor == null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Retorna uma descrição textual do escopo
     */
    public String getDescricaoEscopo() {
        if (isIncluirTodos()) {
            return "Todos os setores";
        } else if (nomeSetor != null) {
            return nomeSetor;
        } else {
            return "Setor ID: " + idSetor;
        }
    }
    
    // Métodos Object
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        InventarioSetor that = (InventarioSetor) obj;
        return Objects.equals(idInventario, that.idInventario) &&
               Objects.equals(idSetor, that.idSetor) &&
               Objects.equals(incluirTodosSetores, that.incluirTodosSetores);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idInventario, idSetor, incluirTodosSetores);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InventarioSetor{");
        sb.append("id=").append(id);
        sb.append(", idInventario=").append(idInventario);
        sb.append(", idSetor=").append(idSetor);
        sb.append(", incluirTodosSetores=").append(incluirTodosSetores);
        sb.append(", ativo=").append(ativo);
        sb.append(", dataInclusao=").append(dataInclusao);
        if (nomeInventario != null) {
            sb.append(", nomeInventario='").append(nomeInventario).append("'");
        }
        if (nomeSetor != null) {
            sb.append(", nomeSetor='").append(nomeSetor).append("'");
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Cria uma cópia do objeto
     */
    public InventarioSetor clone() {
        InventarioSetor clone = new InventarioSetor();
        clone.setId(this.id);
        clone.setIdInventario(this.idInventario);
        clone.setIdSetor(this.idSetor);
        clone.setIncluirTodosSetores(this.incluirTodosSetores);
        clone.setDataInclusao(this.dataInclusao);
        clone.setAtivo(this.ativo);
        clone.setObservacoes(this.observacoes);
        clone.setNomeInventario(this.nomeInventario);
        clone.setNomeSetor(this.nomeSetor);
        return clone;
    }
}