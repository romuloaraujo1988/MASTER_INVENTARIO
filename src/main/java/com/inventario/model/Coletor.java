package com.inventario.model;

import java.time.LocalDateTime;

/**
 * Classe que representa um coletor no sistema.
 * Corresponde à TABELA_COLETOR no banco de dados.
 */
public class Coletor {
    
    private Integer id;
    private String nomeColetor;
    private String cpf;
    private String email;
    private String telefone;
    private Boolean ativo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    
    // Constantes para status
    public static final Boolean ATIVO_SIM = true;
    public static final Boolean ATIVO_NAO = false;
    
    // Construtores
    public Coletor() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.ativo = ATIVO_SIM;
    }
    
    public Coletor(String nomeColetor, String email) {
        this();
        this.nomeColetor = nomeColetor;
        this.email = email;
    }
    
    public Coletor(String nomeColetor, String cpf, String email, String telefone) {
        this();
        this.nomeColetor = nomeColetor;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
    }
    
    // Getters e Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNomeColetor() {
        return nomeColetor;
    }
    
    public void setNomeColetor(String nomeColetor) {
        this.nomeColetor = nomeColetor;
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
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    // Método de compatibilidade para String
    public void setAtivo(String ativo) {
        if ("S".equalsIgnoreCase(ativo) || "true".equalsIgnoreCase(ativo)) {
            this.ativo = ATIVO_SIM;
        } else {
            this.ativo = ATIVO_NAO;
        }
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getAtivoString() {
        return ativo != null && ativo ? "S" : "N";
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
    
    // Métodos utilitários
    public boolean isAtivo() {
        return ativo != null && ativo;
    }
    
    public void ativar() {
        this.ativo = ATIVO_SIM;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public void desativar() {
        this.ativo = ATIVO_NAO;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public boolean temInformacoesCompletas() {
        return nomeColetor != null && !nomeColetor.trim().isEmpty() &&
               email != null && !email.trim().isEmpty();
    }
    
    public boolean temCpfValido() {
        return cpf != null && cpf.replaceAll("[^0-9]", "").length() == 11;
    }
    
    public boolean temEmailValido() {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    @Override
    public String toString() {
        return "Coletor{" +
                "id=" + id +
                ", nomeColetor='" + nomeColetor + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", ativo=" + ativo +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Coletor coletor = (Coletor) o;
        
        return id != null ? id.equals(coletor.id) : coletor.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}