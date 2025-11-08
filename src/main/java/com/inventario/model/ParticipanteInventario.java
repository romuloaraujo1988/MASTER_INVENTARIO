package com.inventario.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe modelo para representar um participante de inventário.
 * Corresponde à TABELA_PARTICIPANTE_INVENTARIO no banco de dados.
 * 
 * Esta classe garante que apenas usuários cadastrados no sistema
 * possam participar de inventários através da chave estrangeira
 * para TABELA_USUARIO.
 */
public class ParticipanteInventario {
    
    private Integer idParticipante;
    private Integer idInventario;
    private Integer idUsuario;
    private String nomeUsuario; // Para exibição (JOIN com TABELA_USUARIO)
    private String papel; // COORDENADOR, COLETOR, OBSERVADOR
    private LocalDateTime dataInclusao;
    private LocalDateTime dataRemocao;
    private Boolean ativo;
    private String observacoes;
    private LocalDateTime dataUltimaAtualizacao;
    
    // Campos para estatísticas de coleta (não persistidos no banco)
    private String emailUsuario;
    private Integer totalColetas;
    private Integer coletasConcluidas;
    private Integer coletasPendentes;
    
    // Constantes para papéis
    public static final String PAPEL_COORDENADOR = "COORDENADOR";
    public static final String PAPEL_COLETOR = "COLETOR";
    public static final String PAPEL_OBSERVADOR = "OBSERVADOR";
    
    // Construtores
    public ParticipanteInventario() {
        this.ativo = true;
        this.dataInclusao = LocalDateTime.now();
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    public ParticipanteInventario(Integer idInventario, Integer idUsuario, String papel) {
        this();
        this.idInventario = idInventario;
        this.idUsuario = idUsuario;
        this.papel = papel;
    }
    
    // Getters e Setters
    public Integer getIdParticipante() {
        return idParticipante;
    }
    
    public void setIdParticipante(Integer idParticipante) {
        this.idParticipante = idParticipante;
    }
    
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getNomeUsuario() {
        return nomeUsuario;
    }
    
    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }
    
    public String getPapel() {
        return papel;
    }
    
    public void setPapel(String papel) {
        this.papel = papel;
    }
    
    public LocalDateTime getDataInclusao() {
        return dataInclusao;
    }
    
    public void setDataInclusao(LocalDateTime dataInclusao) {
        this.dataInclusao = dataInclusao;
    }
    
    public LocalDateTime getDataRemocao() {
        return dataRemocao;
    }
    
    public void setDataRemocao(LocalDateTime dataRemocao) {
        this.dataRemocao = dataRemocao;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public LocalDateTime getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }
    
    public void setDataUltimaAtualizacao(LocalDateTime dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }
    
    public String getEmailUsuario() {
        return emailUsuario;
    }
    
    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }
    
    public Integer getTotalColetas() {
        return totalColetas;
    }
    
    public void setTotalColetas(Integer totalColetas) {
        this.totalColetas = totalColetas;
    }
    
    public Integer getColetasConcluidas() {
        return coletasConcluidas;
    }
    
    public void setColetasConcluidas(Integer coletasConcluidas) {
        this.coletasConcluidas = coletasConcluidas;
    }
    
    public Integer getColetasPendentes() {
        return coletasPendentes;
    }
    
    public void setColetasPendentes(Integer coletasPendentes) {
        this.coletasPendentes = coletasPendentes;
    }
    
    // Métodos de conveniência
    public boolean isAtivo() {
        return ativo != null && ativo;
    }
    
    public boolean isCoordenador() {
        return PAPEL_COORDENADOR.equals(papel);
    }
    
    public boolean isColetor() {
        return PAPEL_COLETOR.equals(papel);
    }
    
    public boolean isObservador() {
        return PAPEL_OBSERVADOR.equals(papel);
    }
    
    /**
     * Remove o participante do inventário (soft delete)
     */
    public void remover() {
        this.ativo = false;
        this.dataRemocao = LocalDateTime.now();
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    /**
     * Reativa o participante no inventário
     */
    public void reativar() {
        this.ativo = true;
        this.dataRemocao = null;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipanteInventario that = (ParticipanteInventario) o;
        return Objects.equals(idParticipante, that.idParticipante) &&
               Objects.equals(idInventario, that.idInventario) &&
               Objects.equals(idUsuario, that.idUsuario);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idParticipante, idInventario, idUsuario);
    }
    
    @Override
    public String toString() {
        return "ParticipanteInventario{" +
                "idParticipante=" + idParticipante +
                ", idInventario=" + idInventario +
                ", idUsuario=" + idUsuario +
                ", nomeUsuario='" + nomeUsuario + '\'' +
                ", papel='" + papel + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}