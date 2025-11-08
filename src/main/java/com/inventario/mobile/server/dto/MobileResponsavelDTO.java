package com.inventario.mobile.server.dto;

/**
 * DTO para dados de responsável mobile
 */
public class MobileResponsavelDTO {
    
    private Integer id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String cargo;
    private Integer idSetor;
    private String nomeSetor;
    private Boolean ativo;
    private String dataCadastro;
    
    // Construtores
    public MobileResponsavelDTO() {
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
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefone() {
        return telefone;
    }
    
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    public String getCargo() {
        return cargo;
    }
    
    public void setCargo(String cargo) {
        this.cargo = cargo;
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
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    public String getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(String dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
