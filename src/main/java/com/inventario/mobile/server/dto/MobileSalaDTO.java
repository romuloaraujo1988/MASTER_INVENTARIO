package com.inventario.mobile.server.dto;

/**
 * DTO para dados de sala mobile
 */
public class MobileSalaDTO {
    
    private Integer id;
    private String nome;
    private String descricao;
    private String andar;
    private String bloco;
    private Boolean ativa;
    
    // Construtores
    public MobileSalaDTO() {
    }
    
    // Getters e Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getAndar() {
        return andar;
    }
    
    public void setAndar(String andar) {
        this.andar = andar;
    }
    
    public String getBloco() {
        return bloco;
    }
    
    public void setBloco(String bloco) {
        this.bloco = bloco;
    }
    
    public Boolean getAtiva() {
        return ativa;
    }
    
    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }
}
