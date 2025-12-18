package com.inventario.model;

import java.sql.Timestamp;
import com.inventario.util.DateFormatUtils;

/**
 * Classe que representa uma Sala no sistema
 * Versão migrada do sistema legado
 */
public class Sala {
    
    private int idSala;
    private String descricao;
    private String numeroSala;
    private Integer andar;
    private String bloco;
    private Integer idSetor;
    private Integer capacidade;
    private Double areaM2;
    private String tipoSala;
    private Boolean ativo;
    private Timestamp dataCadastro;
    private String observacoes;
    

    
    // Campo transiente para exibição
    private String nomeSetor;
    
    // Campo transiente para contagem de patrimônios (usado em relatórios)
    private Integer quantidadePatrimonios;
    
    // Constantes
    public static final Boolean ATIVO_SIM = true;
    public static final Boolean ATIVO_NAO = false;
    
    // Constantes para tipos de sala
    public static final String TIPO_ADMINISTRATIVA = "ADMINISTRATIVA";
    public static final String TIPO_LABORATORIO = "LABORATORIO";
    public static final String TIPO_AULA = "AULA";
    public static final String TIPO_DEPOSITO = "DEPOSITO";
    public static final String TIPO_BIBLIOTECA = "BIBLIOTECA";
    
    // Removido SimpleDateFormat - usando DateFormatUtils centralizado
    
    // Construtor padrão
    public Sala() {
        this.ativo = ATIVO_SIM;
        this.dataCadastro = new Timestamp(System.currentTimeMillis());
    }
    
    // Construtor com parâmetros principais
    public Sala(String numeroSala, String descricao, Integer idSetor) {
        this();
        this.numeroSala = numeroSala;
        this.descricao = descricao;
        this.idSetor = idSetor;
    }
    
    // Getters e Setters
    public int getIdSala() {
        return idSala;
    }
    
    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }
    
    // Métodos de compatibilidade
    public int getId() {
        return idSala;
    }
    
    public void setId(int id) {
        this.idSala = id;
    }
    
    public String getNumeroSala() {
        return numeroSala;
    }
    
    public void setNumeroSala(String numeroSala) {
        this.numeroSala = numeroSala;
    }
    
    public String getNumero() {
        return numeroSala;
    }
    
    public void setNumero(String numero) {
        this.numeroSala = numero;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public Integer getAndar() {
        return andar;
    }
    
    public void setAndar(Integer andar) {
        this.andar = andar;
    }
    
    public String getBloco() {
        return bloco;
    }
    
    public void setBloco(String bloco) {
        this.bloco = bloco;
    }
    
    public Integer getIdSetor() {
        return idSetor;
    }
    
    public void setIdSetor(Integer idSetor) {
        this.idSetor = idSetor;
    }
    
    public Integer getCapacidade() {
        return capacidade;
    }
    
    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }
    
    public Double getAreaM2() {
        return areaM2;
    }
    
    public void setAreaM2(Double areaM2) {
        this.areaM2 = areaM2;
    }
    
    public String getTipoSala() {
        return tipoSala;
    }
    
    public void setTipoSala(String tipoSala) {
        this.tipoSala = tipoSala;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    // Método de compatibilidade com String
    public void setAtivo(String ativo) {
        if ("S".equals(ativo) || "true".equalsIgnoreCase(ativo)) {
            this.ativo = true;
        } else if ("N".equals(ativo) || "false".equalsIgnoreCase(ativo)) {
            this.ativo = false;
        }
    }
    
    public Timestamp getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public String getNomeSetor() {
        return nomeSetor;
    }
    
    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }
    
    public Integer getQuantidadePatrimonios() {
        return quantidadePatrimonios;
    }
    
    public void setQuantidadePatrimonios(Integer quantidadePatrimonios) {
        this.quantidadePatrimonios = quantidadePatrimonios;
    }
    
    // Métodos utilitários
    public boolean isAtiva() {
        return Boolean.TRUE.equals(ativo);
    }
    
    public String getStatusAtivacao() {
        return isAtiva() ? "Ativa" : "Inativa";
    }
    
    public String getAreaFormatada() {
        if (areaM2 == null) {
            return "N/A";
        }
        return String.format("%.2f m²", areaM2);
    }
    
    public String getCapacidadeFormatada() {
        if (capacidade == null) {
            return "N/A";
        }
        return capacidade + " pessoas";
    }
    
    public String getIdentificacaoCompleta() {
        StringBuilder sb = new StringBuilder();
        if (numeroSala != null && !numeroSala.trim().isEmpty()) {
            sb.append(numeroSala);
        }
        if (descricao != null && !descricao.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(descricao);
        }
        return sb.toString();
    }
    
    public String getLocalizacaoCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(getIdentificacaoCompleta());
        if (nomeSetor != null && !nomeSetor.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" (");
            sb.append(nomeSetor);
            sb.append(")");
        }
        return sb.toString();
    }
    
    public String getDataCadastroFormatada() {
        return DateFormatUtils.formatWithDefault(dataCadastro, "Não informado");
    }
    

    
    public boolean temInformacoesCompletas() {
        return numeroSala != null && !numeroSala.trim().isEmpty() &&
               descricao != null && !descricao.trim().isEmpty() &&
               idSetor != null && idSetor > 0;
    }
    
    @Override
    public String toString() {
        return getIdentificacaoCompleta();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Sala sala = (Sala) obj;
        return idSala == sala.idSala;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idSala);
    }
}
