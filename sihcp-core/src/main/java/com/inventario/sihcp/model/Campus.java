package com.inventario.sihcp.model;

import java.sql.Timestamp;
import java.util.Objects;
import com.inventario.sihcp.util.DateFormatUtils;

/**
 * Classe que representa um Campus no sistema
 * Contém informações institucionais do campus
 */
public class Campus {
    
    private int id;
    private String nome;
    private String local;
    private String cnpj;
    private String codigoUorg; // Codigo da Unidade Organizacional no SIADS
    private String diretor;
    private String cursos;
    private String telefone;
    private String email;
    private String observacoes;
    private Boolean ativo;
    private Timestamp dataCriacao;
    
    // Constantes
    public static final Boolean ATIVO_SIM = true;
    public static final Boolean ATIVO_NAO = false;
    
    // Constantes para compatibilidade
    public static final String ATIVO_SIM_STR = "S";
    public static final String ATIVO_NAO_STR = "N";
    
    // Formatação de datas centralizada através de DateFormatUtils
    
    // Construtor padrão
    public Campus() {
        this.ativo = ATIVO_SIM;
        this.dataCriacao = new Timestamp(System.currentTimeMillis());
    }
    
    // Construtor com parâmetros principais
    public Campus(String nome, String local, String cnpj, String diretor) {
        this();
        this.nome = nome;
        this.local = local;
        this.cnpj = cnpj;
        this.diretor = diretor;
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
    
    public String getLocal() {
        return local;
    }
    
    public void setLocal(String local) {
        this.local = local;
    }
    
    public String getCnpj() {
        return cnpj;
    }
    
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
    
    public String getDiretor() {
        return diretor;
    }
    
    public void setDiretor(String diretor) {
        this.diretor = diretor;
    }
    
    public String getCursos() {
        return cursos;
    }
    
    public void setCursos(String cursos) {
        this.cursos = cursos;
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
    
    public String getCodigoUorg() {
        return codigoUorg;
    }
    
    public void setCodigoUorg(String codigoUorg) {
        this.codigoUorg = codigoUorg;
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
        if (local != null && !local.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(local);
        }
        return sb.toString();
    }
    
    public String getDataCadastroFormatada() {
        return DateFormatUtils.formatWithDefault(dataCriacao, "Não informado");
    }
    
    public String getDataCriacaoFormatada() {
        return DateFormatUtils.formatWithDefault(dataCriacao, "Não informado");
    }
    
    public boolean temInformacoesCompletas() {
        return nome != null && !nome.trim().isEmpty() &&
               local != null && !local.trim().isEmpty() &&
               cnpj != null && !cnpj.trim().isEmpty() &&
               diretor != null && !diretor.trim().isEmpty();
    }
    
    /**
     * Formata o CNPJ para exibição
     * @return CNPJ formatado (XX.XXX.XXX/XXXX-XX)
     */
    public String getCnpjFormatado() {
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }
        return cnpj.substring(0, 2) + "." +
               cnpj.substring(2, 5) + "." +
               cnpj.substring(5, 8) + "/" +
               cnpj.substring(8, 12) + "-" +
               cnpj.substring(12, 14);
    }
    
    /**
     * Valida se o CNPJ possui formato básico válido
     * @return true se o CNPJ tem 14 dígitos numéricos
     */
    public boolean isCnpjValido() {
        if (cnpj == null) return false;
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        return cnpjLimpo.length() == 14;
    }
    
    @Override
    public String toString() {
        return nome != null ? nome : "";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Campus campus = (Campus) obj;
        return id == campus.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}