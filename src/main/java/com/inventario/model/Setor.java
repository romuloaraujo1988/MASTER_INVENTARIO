package com.inventario.model;

import java.sql.Timestamp;
// Formatação de datas centralizada em DateFormatUtils
import com.inventario.util.DateFormatUtils;

/**
 * Classe que representa um Setor no sistema
 * Versão migrada do sistema legado
 */
public class Setor {
    
    private int id;
    private String nome;
    private String descricao;
    private String responsavelSetor;
    private String telefone;
    private String email;
    private String observacoes;
    private Boolean ativo;
    private Timestamp dataCriacao;
    private Integer idCampus;
    
    // Constantes
    public static final Boolean ATIVO_SIM = true;
    public static final Boolean ATIVO_NAO = false;
    
    // Constantes para compatibilidade
    public static final String ATIVO_SIM_STR = "S";
    public static final String ATIVO_NAO_STR = "N";
    
    // Formatação de datas centralizada em DateFormatUtils
    
    // Construtor padrão
    public Setor() {
        this.ativo = ATIVO_SIM;
        this.dataCriacao = new Timestamp(System.currentTimeMillis());
    }
    
    // Construtor com parâmetros principais
    public Setor(String nome, String descricao) {
        this();
        this.nome = nome;
        this.descricao = descricao;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
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
    
    public String getResponsavelSetor() {
        return responsavelSetor;
    }
    
    public void setResponsavelSetor(String responsavelSetor) {
        this.responsavelSetor = responsavelSetor;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    // Método para compatibilidade com String
    public void setAtivo(String ativo) {
        if (ativo != null) {
            this.ativo = ATIVO_SIM_STR.equals(ativo) || "true".equalsIgnoreCase(ativo);
        } else {
            this.ativo = null;
        }
    }
    
    public String getAtivoString() {
        if (ativo == null) return null;
        return ativo ? ATIVO_SIM_STR : ATIVO_NAO_STR;
    }
    
    public Timestamp getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(Timestamp dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    // Método para compatibilidade
    public Timestamp getDataCadastro() {
        return dataCriacao;
    }
    
    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCriacao = dataCadastro;
    }
    
    // Métodos utilitários
    public boolean isAtivo() {
        return ativo != null && ativo;
    }
    
    public String getStatusAtivacao() {
        return isAtivo() ? "Ativo" : "Inativo";
    }
    
    public String getIdentificacaoCompleta() {
        StringBuilder sb = new StringBuilder();
        if (nome != null && !nome.trim().isEmpty()) {
            sb.append(nome);
        }
        if (descricao != null && !descricao.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(descricao);
        }
        return sb.toString();
    }
    
    public String getDataCriacaoFormatada() {
        return DateFormatUtils.formatWithDefault(dataCriacao, "Não informado");
    }

    public String getDataCriacaoFormatadaCompleta() {
        return DateFormatUtils.formatWithDefault(dataCriacao, "Não informado");
    }
    
    public boolean temInformacoesCompletas() {
        return nome != null && !nome.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return nome != null ? nome : "";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Setor setor = (Setor) obj;
        return id == setor.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Integer getIdCampus() {
        return idCampus;
    }

    public void setIdCampus(Integer idCampus) {
        this.idCampus = idCampus;
    }
}