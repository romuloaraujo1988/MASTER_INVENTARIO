package com.inventario.siads.model;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Classe base para registros do SIADS
 * Representa um bem patrimonial no formato exigido pelo sistema federal
 */
public class SiadsRegistro {
    
    // Identificação do Bem
    private String numeroPatrimonio;
    private String descricao;
    private String especificacao;
    
    // Classificação
    private String codigoMaterial;
    private String grupoMaterial;
    private String classeContabil;
    
    // Valores
    private BigDecimal valorAquisicao;
    private BigDecimal valorDepreciado;
    private BigDecimal valorResidual;
    
    // Datas
    private LocalDate dataAquisicao;
    private LocalDate dataIncorporacao;
    private LocalDate dataInventario;
    
    // Localização
    private String orgao;
    private String unidadeGestora;
    private String setor;
    private String sala;
    
    // Responsável
    private String cpfResponsavel;
    private String nomeResponsavel;
    private String matriculaResponsavel;
    
    // Situação
    private String situacaoBem; // ATIVO, BAIXADO, EM_MANUTENCAO, etc
    private String estadoConservacao; // BOM, REGULAR, RUIM, OCIOSO
    
    // Origem
    private String formaAquisicao; // COMPRA, DOACAO, CESSAO, etc
    private String numeroNotaFiscal;
    private String fornecedor;
    
    // Detalhes do bem
    private String modelo;
    private String fabricante;
    
    // Construtores
    public SiadsRegistro() {
    }
    
    // Getters e Setters
    public String getNumeroPatrimonio() {
        return numeroPatrimonio;
    }
    
    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getEspecificacao() {
        return especificacao;
    }
    
    public void setEspecificacao(String especificacao) {
        this.especificacao = especificacao;
    }
    
    public String getCodigoMaterial() {
        return codigoMaterial;
    }
    
    public void setCodigoMaterial(String codigoMaterial) {
        this.codigoMaterial = codigoMaterial;
    }
    
    public String getGrupoMaterial() {
        return grupoMaterial;
    }
    
    public void setGrupoMaterial(String grupoMaterial) {
        this.grupoMaterial = grupoMaterial;
    }
    
    public String getClasseContabil() {
        return classeContabil;
    }
    
    public void setClasseContabil(String classeContabil) {
        this.classeContabil = classeContabil;
    }
    
    public BigDecimal getValorAquisicao() {
        return valorAquisicao;
    }
    
    public void setValorAquisicao(BigDecimal valorAquisicao) {
        this.valorAquisicao = valorAquisicao;
    }
    
    public BigDecimal getValorDepreciado() {
        return valorDepreciado;
    }
    
    public void setValorDepreciado(BigDecimal valorDepreciado) {
        this.valorDepreciado = valorDepreciado;
    }
    
    public BigDecimal getValorResidual() {
        return valorResidual;
    }
    
    public void setValorResidual(BigDecimal valorResidual) {
        this.valorResidual = valorResidual;
    }
    
    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }
    
    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }
    
    public LocalDate getDataIncorporacao() {
        return dataIncorporacao;
    }
    
    public void setDataIncorporacao(LocalDate dataIncorporacao) {
        this.dataIncorporacao = dataIncorporacao;
    }
    
    public LocalDate getDataInventario() {
        return dataInventario;
    }
    
    public void setDataInventario(LocalDate dataInventario) {
        this.dataInventario = dataInventario;
    }
    
    public String getOrgao() {
        return orgao;
    }
    
    public void setOrgao(String orgao) {
        this.orgao = orgao;
    }
    
    public String getUnidadeGestora() {
        return unidadeGestora;
    }
    
    public void setUnidadeGestora(String unidadeGestora) {
        this.unidadeGestora = unidadeGestora;
    }
    
    public String getSetor() {
        return setor;
    }
    
    public void setSetor(String setor) {
        this.setor = setor;
    }
    
    public String getSala() {
        return sala;
    }
    
    public void setSala(String sala) {
        this.sala = sala;
    }
    
    public String getCpfResponsavel() {
        return cpfResponsavel;
    }
    
    public void setCpfResponsavel(String cpfResponsavel) {
        this.cpfResponsavel = cpfResponsavel;
    }
    
    public String getNomeResponsavel() {
        return nomeResponsavel;
    }
    
    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }
    
    public String getMatriculaResponsavel() {
        return matriculaResponsavel;
    }
    
    public void setMatriculaResponsavel(String matriculaResponsavel) {
        this.matriculaResponsavel = matriculaResponsavel;
    }
    
    public String getSituacaoBem() {
        return situacaoBem;
    }
    
    public void setSituacaoBem(String situacaoBem) {
        this.situacaoBem = situacaoBem;
    }
    
    public String getEstadoConservacao() {
        return estadoConservacao;
    }
    
    public void setEstadoConservacao(String estadoConservacao) {
        this.estadoConservacao = estadoConservacao;
    }
    
    public String getFormaAquisicao() {
        return formaAquisicao;
    }
    
    public void setFormaAquisicao(String formaAquisicao) {
        this.formaAquisicao = formaAquisicao;
    }
    
    public String getNumeroNotaFiscal() {
        return numeroNotaFiscal;
    }
    
    public void setNumeroNotaFiscal(String numeroNotaFiscal) {
        this.numeroNotaFiscal = numeroNotaFiscal;
    }
    
    public String getFornecedor() {
        return fornecedor;
    }
    
    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public String getFabricante() {
        return fabricante;
    }
    
    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }
}