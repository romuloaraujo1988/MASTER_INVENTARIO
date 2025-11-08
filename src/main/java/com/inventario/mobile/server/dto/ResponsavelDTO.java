package com.inventario.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventario.model.Responsavel;

import java.io.Serializable;

/**
 * DTO para transferência de dados de Responsável para o app mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ResponsavelDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("nome")
    private String nome;
    
    @JsonProperty("cpf")
    private String cpf;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("telefone")
    private String telefone;
    
    @JsonProperty("cargo")
    private String cargo;
    
    @JsonProperty("idSetor")
    private Integer idSetor;
    
    @JsonProperty("nomeSetor")
    private String nomeSetor;
    
    @JsonProperty("ativo")
    private Boolean ativo;
    
    @JsonProperty("dataCadastro")
    private String dataCadastro;
    
    // Construtores
    
    public ResponsavelDTO() {
    }
    
    public ResponsavelDTO(Responsavel responsavel) {
        this.id = responsavel.getId();
        this.nome = responsavel.getNome();
        this.cpf = responsavel.getCpf();
        this.email = responsavel.getEmail();
        this.telefone = responsavel.getTelefone();
        this.cargo = responsavel.getCargo();
        this.idSetor = responsavel.getIdSetor();
        this.ativo = responsavel.getAtivo();
        
        // Formatar data de cadastro se existir
        if (responsavel.getDataCadastro() != null) {
            this.dataCadastro = responsavel.getDataCadastro().toString();
        }
        
        // Nome do setor (já vem do DAO)
        this.nomeSetor = responsavel.getNomeSetor();
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
