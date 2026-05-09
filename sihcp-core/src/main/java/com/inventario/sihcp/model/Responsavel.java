package com.inventario.sihcp.model;

import java.sql.Timestamp;
import com.inventario.sihcp.util.DateFormatUtils;

/**
 * Classe que representa um Responsável no sistema
 * Versão migrada do sistema legado
 */
public class Responsavel {
    
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String cargo;
    private int idSetor;
    private Boolean ativo;
    private Timestamp dataCadastro;
    private String observacoes;
    
    // Campo transiente para exibição
    private String nomeSetor;
    
    // Constantes
    public static final Boolean ATIVO_SIM = true;
    public static final Boolean ATIVO_NAO = false;
    
    // Constantes para compatibilidade
    public static final String ATIVO_SIM_STR = "S";
    public static final String ATIVO_NAO_STR = "N";
    
    // Formatação de datas centralizada através de DateFormatUtils
    
    // Construtor padrão
    public Responsavel() {
        this.ativo = ATIVO_SIM;
        this.dataCadastro = new Timestamp(System.currentTimeMillis());
    }
    
    // Construtor com parâmetros principais
    public Responsavel(String nome, String cpf, String email) {
        this();
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
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
    
    public int getIdSetor() {
        return idSetor;
    }
    
    public void setIdSetor(int idSetor) {
        this.idSetor = idSetor;
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
    
    public Timestamp getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public String getNomeSetor() {
        return nomeSetor;
    }
    
    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }
    
    // Métodos utilitários
    public boolean isAtivo() {
        return ativo != null && ativo;
    }
    
    public String getStatusAtivacao() {
        return isAtivo() ? "Ativo" : "Inativo";
    }
    
    public String getCpfFormatado() {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        
        return cpf.substring(0, 3) + "." + 
               cpf.substring(3, 6) + "." + 
               cpf.substring(6, 9) + "-" + 
               cpf.substring(9, 11);
    }
    
    public String getTelefoneFormatado() {
        if (telefone == null) {
            return telefone;
        }
        
        String tel = telefone.replaceAll("[^0-9]", "");
        
        if (tel.length() == 11) {
            // Celular: (XX) XXXXX-XXXX
            return "(" + tel.substring(0, 2) + ") " + 
                   tel.substring(2, 7) + "-" + 
                   tel.substring(7, 11);
        } else if (tel.length() == 10) {
            // Fixo: (XX) XXXX-XXXX
            return "(" + tel.substring(0, 2) + ") " + 
                   tel.substring(2, 6) + "-" + 
                   tel.substring(6, 10);
        }
        
        return telefone;
    }
    
    public boolean isEmailValido() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    public boolean isCpfValido() {
        if (cpf == null) {
            return false;
        }
        
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        
        if (cpfLimpo.length() != 11) {
            return false;
        }
        
        // Verificar se todos os dígitos são iguais
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        // Validação dos dígitos verificadores
        try {
            int[] digitos = new int[11];
            for (int i = 0; i < 11; i++) {
                digitos[i] = Integer.parseInt(cpfLimpo.substring(i, i + 1));
            }
            
            // Primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += digitos[i] * (10 - i);
            }
            int resto = soma % 11;
            int dv1 = resto < 2 ? 0 : 11 - resto;
            
            if (digitos[9] != dv1) {
                return false;
            }
            
            // Segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += digitos[i] * (11 - i);
            }
            resto = soma % 11;
            int dv2 = resto < 2 ? 0 : 11 - resto;
            
            return digitos[10] == dv2;
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public String getDataCadastroFormatada() {
        return DateFormatUtils.formatWithDefault(dataCadastro, "Não informado");
    }
    
    public String getIdentificacaoCompleta() {
        StringBuilder sb = new StringBuilder();
        if (nome != null && !nome.trim().isEmpty()) {
            sb.append(nome);
        }
        if (cargo != null && !cargo.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(cargo);
        }
        if (nomeSetor != null && !nomeSetor.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" (");
            sb.append(nomeSetor);
            if (sb.length() > 0) sb.append(")");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return nome;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Responsavel responsavel = (Responsavel) obj;
        return id == responsavel.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}