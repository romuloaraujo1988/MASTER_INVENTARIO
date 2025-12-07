package com.inventario.siads.model;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Classe base para registros do SIADS
 * Representa um bem patrimonial no formato exigido pelo sistema federal
 * 
 * Adequado às normas federais:
 * - IN SGD/ME nº 1/2019 (Gestão de Patrimônio)
 * - Decreto nº 9.373/2018 (Alienação de Bens)
 * - Manual SIADS v6.2.11
 * 
 * @version 2.0.0 - Adequação às normas federais
 */
public class SiadsRegistro {
    
    // ========================================
    // IDENTIFICAÇÃO DO BEM (Campos Obrigatórios)
    // ========================================
    private String numeroPatrimonio;      // Número único do patrimônio (obrigatório)
    private String descricao;             // Descrição do bem (obrigatório)
    private String especificacao;         // Especificação técnica detalhada
    
    // ========================================
    // CLASSIFICAÇÃO (Conforme CATMAT/CATSER)
    // ========================================
    private String codigoMaterial;        // Código do material (P + número)
    private String codigoCatmat;          // Código CATMAT oficial do governo federal
    private String grupoMaterial;         // Grupo/Marca do material
    private String classeContabil;        // Classe contábil (categoria)
    private String elementoDespesa;       // Elemento de Despesa (ED) - conforme SIAFI
    
    // ========================================
    // VALORES (Conforme normas contábeis federais)
    // ========================================
    private BigDecimal valorAquisicao;    // Valor de aquisição original (obrigatório)
    private BigDecimal valorDepreciado;   // Valor depreciado acumulado
    private BigDecimal valorResidual;     // Valor residual após depreciação
    private BigDecimal valorLiquido;      // Valor líquido contábil
    
    // ========================================
    // DATAS (Conforme ciclo de vida do bem)
    // ========================================
    private LocalDate dataAquisicao;      // Data de aquisição (obrigatório)
    private LocalDate dataIncorporacao;   // Data de incorporação ao patrimônio
    private LocalDate dataInventario;     // Data do último inventário
    private LocalDate dataUltimaMovimentacao; // Data da última movimentação
    
    // ========================================
    // LOCALIZAÇÃO (Conforme estrutura organizacional)
    // ========================================
    private String orgao;                 // Código do órgão (ex: 25000 - MEC)
    private String unidadeGestora;        // Código da Unidade Gestora (UG)
    private String codigoUorg;            // Código da Unidade Organizacional (UOrg)
    private String setor;                 // Nome do setor
    private String sala;                  // Nome/número da sala
    
    // ========================================
    // RESPONSÁVEL (Conforme termo de responsabilidade)
    // ========================================
    private String cpfResponsavel;        // CPF do responsável (11 dígitos)
    private String nomeResponsavel;       // Nome completo do responsável
    private String matriculaResponsavel;  // Matrícula SIAPE do responsável
    private String cargoResponsavel;      // Cargo do responsável
    
    // ========================================
    // SITUAÇÃO DO BEM (Conforme IN SGD/ME nº 1/2019)
    // ========================================
    private String situacaoBem;           // ATIVO, OCIOSO, BAIXADO, EM_MANUTENCAO
    private String estadoConservacao;     // OTIMO, BOM, REGULAR, RUIM, PESSIMO
    private String tipoAquisicao;         // 1=Compra, 2=Doação, 3=Cessão, etc
    
    // ========================================
    // ORIGEM/AQUISIÇÃO
    // ========================================
    private String formaAquisicao;        // COMPRA, DOACAO, CESSAO, PERMUTA, etc
    private String numeroNotaFiscal;      // Número da nota fiscal
    private String fornecedor;            // Nome do fornecedor
    private String numeroContrato;        // Número do contrato de aquisição
    private String numeroProcesso;        // Número do processo de aquisição
    private String numeroEmpenho;         // Número do empenho (SIAFI)
    
    // ========================================
    // DETALHES TÉCNICOS DO BEM
    // ========================================
    private String modelo;                // Modelo do bem
    private String fabricante;            // Fabricante/Marca
    private String numeroSerie;           // Número de série
    
    // ========================================
    // GARANTIA (Conforme controle de garantias)
    // ========================================
    private String garantidor;            // Nome do garantidor
    private String contratoGarantia;      // Número do contrato de garantia
    private LocalDate dataInicioGarantia; // Data início da garantia
    private LocalDate dataFimGarantia;    // Data fim da garantia
    
    // ========================================
    // BAIXA PATRIMONIAL (Conforme Decreto nº 9.373/2018)
    // ========================================
    private boolean baixado;              // Indica se o bem foi baixado
    private LocalDate dataBaixa;          // Data da baixa
    private String motivoBaixa;           // Código do motivo (1-10)
    private String descricaoMotivoBaixa;  // Descrição do motivo da baixa
    private String numeroProcessoBaixa;   // Número do processo de baixa
    private String tipoDestinacao;        // LEILAO, DOACAO, INUTILIZACAO, etc
    
    // ========================================
    // DEPRECIAÇÃO (Conforme NBC TSP 07)
    // ========================================
    private int vidaUtilAnos;             // Vida útil em anos
    private BigDecimal taxaDepreciacao;   // Taxa de depreciação anual (%)
    private String metodoDepreciacao;     // LINEAR, SOMA_DIGITOS, etc
    
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
    
    public String getCodigoUorg() {
        return codigoUorg;
    }
    
    public void setCodigoUorg(String codigoUorg) {
        this.codigoUorg = codigoUorg;
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
    
    // ========================================
    // NOVOS GETTERS E SETTERS - Normas Federais
    // ========================================
    
    // Código CATMAT
    public String getCodigoCatmat() {
        return codigoCatmat;
    }
    
    public void setCodigoCatmat(String codigoCatmat) {
        this.codigoCatmat = codigoCatmat;
    }
    
    // Elemento de Despesa
    public String getElementoDespesa() {
        return elementoDespesa;
    }
    
    public void setElementoDespesa(String elementoDespesa) {
        this.elementoDespesa = elementoDespesa;
    }
    
    // Valor Líquido
    public BigDecimal getValorLiquido() {
        return valorLiquido;
    }
    
    public void setValorLiquido(BigDecimal valorLiquido) {
        this.valorLiquido = valorLiquido;
    }
    
    // Data Última Movimentação
    public LocalDate getDataUltimaMovimentacao() {
        return dataUltimaMovimentacao;
    }
    
    public void setDataUltimaMovimentacao(LocalDate dataUltimaMovimentacao) {
        this.dataUltimaMovimentacao = dataUltimaMovimentacao;
    }
    
    // Cargo Responsável
    public String getCargoResponsavel() {
        return cargoResponsavel;
    }
    
    public void setCargoResponsavel(String cargoResponsavel) {
        this.cargoResponsavel = cargoResponsavel;
    }
    
    // Tipo Aquisição
    public String getTipoAquisicao() {
        return tipoAquisicao;
    }
    
    public void setTipoAquisicao(String tipoAquisicao) {
        this.tipoAquisicao = tipoAquisicao;
    }
    
    // Número Contrato
    public String getNumeroContrato() {
        return numeroContrato;
    }
    
    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }
    
    // Número Processo
    public String getNumeroProcesso() {
        return numeroProcesso;
    }
    
    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }
    
    // Número Empenho
    public String getNumeroEmpenho() {
        return numeroEmpenho;
    }
    
    public void setNumeroEmpenho(String numeroEmpenho) {
        this.numeroEmpenho = numeroEmpenho;
    }
    
    // Número Série
    public String getNumeroSerie() {
        return numeroSerie;
    }
    
    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }
    
    // Garantidor
    public String getGarantidor() {
        return garantidor;
    }
    
    public void setGarantidor(String garantidor) {
        this.garantidor = garantidor;
    }
    
    // Contrato Garantia
    public String getContratoGarantia() {
        return contratoGarantia;
    }
    
    public void setContratoGarantia(String contratoGarantia) {
        this.contratoGarantia = contratoGarantia;
    }
    
    // Data Início Garantia
    public LocalDate getDataInicioGarantia() {
        return dataInicioGarantia;
    }
    
    public void setDataInicioGarantia(LocalDate dataInicioGarantia) {
        this.dataInicioGarantia = dataInicioGarantia;
    }
    
    // Data Fim Garantia
    public LocalDate getDataFimGarantia() {
        return dataFimGarantia;
    }
    
    public void setDataFimGarantia(LocalDate dataFimGarantia) {
        this.dataFimGarantia = dataFimGarantia;
    }
    
    // Baixado
    public boolean isBaixado() {
        return baixado;
    }
    
    public void setBaixado(boolean baixado) {
        this.baixado = baixado;
    }
    
    // Data Baixa
    public LocalDate getDataBaixa() {
        return dataBaixa;
    }
    
    public void setDataBaixa(LocalDate dataBaixa) {
        this.dataBaixa = dataBaixa;
    }
    
    // Motivo Baixa
    public String getMotivoBaixa() {
        return motivoBaixa;
    }
    
    public void setMotivoBaixa(String motivoBaixa) {
        this.motivoBaixa = motivoBaixa;
    }
    
    // Descrição Motivo Baixa
    public String getDescricaoMotivoBaixa() {
        return descricaoMotivoBaixa;
    }
    
    public void setDescricaoMotivoBaixa(String descricaoMotivoBaixa) {
        this.descricaoMotivoBaixa = descricaoMotivoBaixa;
    }
    
    // Número Processo Baixa
    public String getNumeroProcessoBaixa() {
        return numeroProcessoBaixa;
    }
    
    public void setNumeroProcessoBaixa(String numeroProcessoBaixa) {
        this.numeroProcessoBaixa = numeroProcessoBaixa;
    }
    
    // Tipo Destinação
    public String getTipoDestinacao() {
        return tipoDestinacao;
    }
    
    public void setTipoDestinacao(String tipoDestinacao) {
        this.tipoDestinacao = tipoDestinacao;
    }
    
    // Vida Útil Anos
    public int getVidaUtilAnos() {
        return vidaUtilAnos;
    }
    
    public void setVidaUtilAnos(int vidaUtilAnos) {
        this.vidaUtilAnos = vidaUtilAnos;
    }
    
    // Taxa Depreciação
    public BigDecimal getTaxaDepreciacao() {
        return taxaDepreciacao;
    }
    
    public void setTaxaDepreciacao(BigDecimal taxaDepreciacao) {
        this.taxaDepreciacao = taxaDepreciacao;
    }
    
    // Método Depreciação
    public String getMetodoDepreciacao() {
        return metodoDepreciacao;
    }
    
    public void setMetodoDepreciacao(String metodoDepreciacao) {
        this.metodoDepreciacao = metodoDepreciacao;
    }
    
    // ========================================
    // CONSTANTES - Códigos SIADS Oficiais
    // ========================================
    
    /**
     * Códigos de Motivo de Baixa conforme Decreto nº 9.373/2018
     */
    public static final class MotivoBaixa {
        public static final String INSERVIVEL_OBSOLETO = "1";      // Inservível por obsolescência
        public static final String INSERVIVEL_OCIOSO = "2";        // Inservível por ociosidade
        public static final String INSERVIVEL_ANTIECON = "3";      // Inservível por antieconômico
        public static final String INSERVIVEL_IRRECUP = "4";       // Inservível por irrecuperável
        public static final String FURTO_ROUBO = "5";              // Furto ou roubo
        public static final String SINISTRO = "6";                 // Sinistro (incêndio, etc)
        public static final String DOACAO = "7";                   // Doação
        public static final String PERMUTA = "8";                  // Permuta
        public static final String VENDA = "9";                    // Venda/Leilão
        public static final String OUTROS = "10";                  // Outros motivos
    }
    
    /**
     * Códigos de Estado de Conservação conforme SIADS
     */
    public static final class EstadoConservacao {
        public static final String OTIMO = "1";
        public static final String BOM = "2";
        public static final String REGULAR = "3";
        public static final String RUIM = "4";
        public static final String PESSIMO = "5";
    }
    
    /**
     * Códigos de Situação do Bem conforme SIADS
     */
    public static final class SituacaoBem {
        public static final String EM_USO = "1";
        public static final String OCIOSO = "2";
        public static final String BAIXADO = "3";
    }
    
    /**
     * Códigos de Tipo de Aquisição conforme SIADS
     */
    public static final class TipoAquisicao {
        public static final String COMPRA = "1";
        public static final String DOACAO = "2";
        public static final String CESSAO = "3";
        public static final String PERMUTA = "4";
        public static final String FABRICACAO_PROPRIA = "5";
        public static final String OUTROS = "6";
    }
    
    /**
     * Códigos de Tipo de Destinação na Baixa
     */
    public static final class TipoDestinacao {
        public static final String LEILAO = "LEILAO";
        public static final String DOACAO = "DOACAO";
        public static final String INUTILIZACAO = "INUTILIZACAO";
        public static final String PERMUTA = "PERMUTA";
        public static final String DACAO_PAGAMENTO = "DACAO_PAGAMENTO";
    }
}
