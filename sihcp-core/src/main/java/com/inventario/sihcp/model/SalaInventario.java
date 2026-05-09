package com.inventario.sihcp.model;

import java.time.LocalDateTime;
// Formatação de datas centralizada em DateFormatUtils
import com.inventario.sihcp.util.DateFormatUtils;
import java.math.BigDecimal;

/**
 * Classe modelo para representar o status de coleta por sala e inventário
 * Corresponde à tabela TABELA_SALA_INVENTARIO no banco de dados
 */
public class SalaInventario {
    private Integer idSalaInventario;
    private Integer idSala;
    private Integer idInventario;
    private Integer idParticipante;
    private Boolean coletaFinalizada;
    private LocalDateTime dataInicioColeta;
    private LocalDateTime dataFinalizacaoColeta;
    private String observacoesFinalizacao;
    private Integer totalItensColetados;
    private Integer totalItensSemEtiqueta;
    private BigDecimal percentualConclusao;
    private String statusColeta;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataUltimaAtualizacao;
    
    // Objetos relacionados (para joins)
    private Sala sala;
    private Inventario inventario;
    private ParticipanteInventario participante;
    
    // Constantes para status
    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String STATUS_FINALIZADA = "FINALIZADA";
    public static final String STATUS_CANCELADA = "CANCELADA";
    
    // Construtor padrão
    public SalaInventario() {
        this.coletaFinalizada = false;
        this.totalItensColetados = 0;
        this.totalItensSemEtiqueta = 0;
        this.percentualConclusao = BigDecimal.ZERO;
        this.statusColeta = STATUS_PENDENTE;
        this.dataCadastro = LocalDateTime.now();
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    // Construtor com parâmetros essenciais
    public SalaInventario(Integer idSala, Integer idInventario) {
        this();
        this.idSala = idSala;
        this.idInventario = idInventario;
    }
    
    // Getters e Setters
    public Integer getIdSalaInventario() {
        return idSalaInventario;
    }
    
    public void setIdSalaInventario(Integer idSalaInventario) {
        this.idSalaInventario = idSalaInventario;
    }
    
    public Integer getIdSala() {
        return idSala;
    }
    
    public void setIdSala(Integer idSala) {
        this.idSala = idSala;
    }
    
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public Integer getIdParticipante() {
        return idParticipante;
    }
    
    public void setIdParticipante(Integer idParticipante) {
        this.idParticipante = idParticipante;
    }
    
    public Boolean getColetaFinalizada() {
        return coletaFinalizada;
    }
    
    public void setColetaFinalizada(Boolean coletaFinalizada) {
        this.coletaFinalizada = coletaFinalizada;
    }
    
    public LocalDateTime getDataInicioColeta() {
        return dataInicioColeta;
    }
    
    public void setDataInicioColeta(LocalDateTime dataInicioColeta) {
        this.dataInicioColeta = dataInicioColeta;
    }
    
    public LocalDateTime getDataFinalizacaoColeta() {
        return dataFinalizacaoColeta;
    }
    
    public void setDataFinalizacaoColeta(LocalDateTime dataFinalizacaoColeta) {
        this.dataFinalizacaoColeta = dataFinalizacaoColeta;
    }
    
    public String getObservacoesFinalizacao() {
        return observacoesFinalizacao;
    }
    
    public void setObservacoesFinalizacao(String observacoesFinalizacao) {
        this.observacoesFinalizacao = observacoesFinalizacao;
    }
    
    public Integer getTotalItensColetados() {
        return totalItensColetados;
    }
    
    public void setTotalItensColetados(Integer totalItensColetados) {
        this.totalItensColetados = totalItensColetados;
    }
    
    public Integer getTotalItensSemEtiqueta() {
        return totalItensSemEtiqueta;
    }
    
    public void setTotalItensSemEtiqueta(Integer totalItensSemEtiqueta) {
        this.totalItensSemEtiqueta = totalItensSemEtiqueta;
    }
    
    public BigDecimal getPercentualConclusao() {
        return percentualConclusao;
    }
    
    public void setPercentualConclusao(BigDecimal percentualConclusao) {
        this.percentualConclusao = percentualConclusao;
    }
    
    public String getStatusColeta() {
        return statusColeta;
    }
    
    public void setStatusColeta(String statusColeta) {
        this.statusColeta = statusColeta;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public LocalDateTime getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }
    
    public void setDataUltimaAtualizacao(LocalDateTime dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }
    
    public Sala getSala() {
        return sala;
    }
    
    public void setSala(Sala sala) {
        this.sala = sala;
    }
    
    public Inventario getInventario() {
        return inventario;
    }
    
    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }
    
    public ParticipanteInventario getParticipante() {
        return participante;
    }
    
    public void setParticipante(ParticipanteInventario participante) {
        this.participante = participante;
    }
    
    // Métodos utilitários
    public boolean isColetaFinalizada() {
        return coletaFinalizada != null && coletaFinalizada;
    }
    
    public boolean isColetaEmAndamento() {
        return STATUS_EM_ANDAMENTO.equals(statusColeta);
    }
    
    public boolean isColetaPendente() {
        return STATUS_PENDENTE.equals(statusColeta);
    }
    
    public boolean isColetaCancelada() {
        return STATUS_CANCELADA.equals(statusColeta);
    }
    
    public String getStatusColetaFormatado() {
        if (statusColeta == null) return "Não definido";
        
        switch (statusColeta) {
            case STATUS_PENDENTE:
                return "Pendente";
            case STATUS_EM_ANDAMENTO:
                return "Em Andamento";
            case STATUS_FINALIZADA:
                return "Finalizada";
            case STATUS_CANCELADA:
                return "Cancelada";
            default:
                return statusColeta;
        }
    }
    
    public String getDataFinalizacaoFormatada() {
        if (dataFinalizacaoColeta == null) {
            return "Não finalizada";
        }
        return DateFormatUtils.formatDateTime(dataFinalizacaoColeta);
    }
    
    public String getDataInicioFormatada() {
        if (dataInicioColeta == null) {
            return "Não iniciada";
        }
        return DateFormatUtils.formatDateTime(dataInicioColeta);
    }
    
    public String getPercentualConclusaoFormatado() {
        if (percentualConclusao == null) {
            return "0%";
        }
        return String.format("%.1f%%", percentualConclusao.doubleValue());
    }
    
    public void iniciarColeta() {
        this.statusColeta = STATUS_EM_ANDAMENTO;
        this.dataInicioColeta = LocalDateTime.now();
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    public void finalizarColeta(String observacoes) {
        this.coletaFinalizada = true;
        this.statusColeta = STATUS_FINALIZADA;
        this.dataFinalizacaoColeta = LocalDateTime.now();
        this.observacoesFinalizacao = observacoes;
        this.percentualConclusao = new BigDecimal("100.00");
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    public void reabrirColeta() {
        this.coletaFinalizada = false;
        this.statusColeta = STATUS_EM_ANDAMENTO;
        this.dataFinalizacaoColeta = null;
        this.observacoesFinalizacao = null;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    public void cancelarColeta(String motivo) {
        this.statusColeta = STATUS_CANCELADA;
        this.observacoesFinalizacao = motivo;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("SalaInventario{id=%d, sala=%d, inventario=%d, status=%s, finalizada=%s}",
                idSalaInventario, idSala, idInventario, statusColeta, coletaFinalizada);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SalaInventario that = (SalaInventario) obj;
        
        if (idSalaInventario != null) {
            return idSalaInventario.equals(that.idSalaInventario);
        }
        
        return idSala != null && idSala.equals(that.idSala) &&
               idInventario != null && idInventario.equals(that.idInventario);
    }
    
    @Override
    public int hashCode() {
        if (idSalaInventario != null) {
            return idSalaInventario.hashCode();
        }
        
        int result = idSala != null ? idSala.hashCode() : 0;
        result = 31 * result + (idInventario != null ? idInventario.hashCode() : 0);
        return result;
    }
}