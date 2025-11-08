package com.inventario.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Classe que representa um inventário no sistema.
 * Corresponde à TABELA_INVENTARIO no banco de dados.
 */
public class Inventario {
    
    // Constantes para status do inventário
    public static final String STATUS_PLANEJADO = "PLANEJADO";
    public static final String STATUS_EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String STATUS_CONCLUIDO = "CONCLUIDO";
    public static final String STATUS_CANCELADO = "CANCELADO";
    
    private Integer id;
    private String nome;
    private Integer ano;
    private Date dataInicio;
    private Date dataFim;
    private String observacao;
    private String tipoInventario;
    private String statusInventario;
    private String responsavelInventario;
    private Integer totalPatrimonios;
    private Integer patrimoniosColetados;
    private BigDecimal percentualConclusao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    
    // Construtores
    public Inventario() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.statusInventario = STATUS_PLANEJADO;
        this.totalPatrimonios = 0;
        this.patrimoniosColetados = 0;
        this.percentualConclusao = BigDecimal.ZERO;
    }
    
    public Inventario(String nome, Integer ano, Date dataInicio, String responsavelInventario) {
        this();
        this.nome = nome;
        this.ano = ano;
        this.dataInicio = dataInicio;
        this.responsavelInventario = responsavelInventario;
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
    
    public Integer getAno() {
        return ano;
    }
    
    public void setAno(Integer ano) {
        this.ano = ano;
    }
    
    public Date getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public Date getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    public String getTipoInventario() {
        return tipoInventario;
    }
    
    public void setTipoInventario(String tipoInventario) {
        this.tipoInventario = tipoInventario;
    }
    
    public String getStatusInventario() {
        return statusInventario;
    }
    
    public void setStatusInventario(String statusInventario) {
        this.statusInventario = statusInventario;
    }
    
    public String getResponsavelInventario() {
        return responsavelInventario;
    }
    
    public void setResponsavelInventario(String responsavelInventario) {
        this.responsavelInventario = responsavelInventario;
    }
    
    public Integer getTotalPatrimonios() {
        return totalPatrimonios;
    }
    
    public void setTotalPatrimonios(Integer totalPatrimonios) {
        this.totalPatrimonios = totalPatrimonios;
        atualizarPercentualConclusao();
    }
    
    public Integer getPatrimoniosColetados() {
        return patrimoniosColetados;
    }
    
    public void setPatrimoniosColetados(Integer patrimoniosColetados) {
        this.patrimoniosColetados = patrimoniosColetados;
        atualizarPercentualConclusao();
    }
    
    public BigDecimal getPercentualConclusao() {
        return percentualConclusao;
    }
    
    public void setPercentualConclusao(BigDecimal percentualConclusao) {
        this.percentualConclusao = percentualConclusao;
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
    public boolean isEmAndamento() {
        return STATUS_EM_ANDAMENTO.equals(statusInventario);
    }
    
    public boolean isConcluido() {
        return STATUS_CONCLUIDO.equals(statusInventario);
    }
    
    public boolean isCancelado() {
        return STATUS_CANCELADO.equals(statusInventario);
    }
    
    public boolean isPlanejado() {
        return STATUS_PLANEJADO.equals(statusInventario);
    }
    
    private void atualizarPercentualConclusao() {
        if (totalPatrimonios != null && totalPatrimonios > 0 && patrimoniosColetados != null) {
            this.percentualConclusao = new BigDecimal(patrimoniosColetados)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(totalPatrimonios), 2, RoundingMode.HALF_UP);
        } else {
            this.percentualConclusao = BigDecimal.ZERO;
        }
    }
    
    public void incrementarPatrimoniosColetados() {
        if (patrimoniosColetados == null) {
            patrimoniosColetados = 0;
        }
        patrimoniosColetados++;
        atualizarPercentualConclusao();
    }
    
    public void decrementarPatrimoniosColetados() {
        if (patrimoniosColetados != null && patrimoniosColetados > 0) {
            patrimoniosColetados--;
            atualizarPercentualConclusao();
        }
    }
    
    @Override
    public String toString() {
        return "Inventario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", ano=" + ano +
                ", statusInventario='" + statusInventario + '\'' +
                ", percentualConclusao=" + percentualConclusao +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Inventario that = (Inventario) o;
        
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}