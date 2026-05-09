package com.inventario.sihcp.model;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import com.inventario.sihcp.util.DateFormatUtils;

/**
 * Classe que representa um Patrimônio no sistema
 * Versão migrada do sistema legado
 */
public class Patrimonio {
    
    private int id;
    private String numero;
    private String status;
    private String descricao;
    private String rotulos;
    private int idResponsavel;
    private BigDecimal valorAquisicao;
    private BigDecimal valorDepreciado;
    private String numeroNotaFiscal;
    private String numeroSerie;
    private String modelo;
    private String marca;
    private java.sql.Date dataEntrada;
    private Timestamp dataCarga;
    private String fornecedor;
    private String ed; // Elemento de Despesa (SIADS)
    private int idSala;
    private String estadoConservacao;
    private String observacoes;
    private String situacao;
    private String descricaoResumida;
    private String categoria; // Campo para categoria treinada pela IA
    
    // Campos transientes para exibição
    private String nomeResponsavel;
    private String nomeSala;
    
    // Campos transientes para SIADS
    private String cpfResponsavel;
    private String matriculaResponsavel;
    private String nomeSetor;
    private String codigoUOrg; // Código da Unidade Organizacional no SIADS
    
    // Constantes para formatação
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    // Removido SimpleDateFormat - usando DateFormatUtils centralizado
    
    // Constantes para estados de conservação
    public static final String ESTADO_OTIMO = "Ótimo";
    public static final String ESTADO_BOM = "Bom";
    public static final String ESTADO_REGULAR = "Regular";
    public static final String ESTADO_RUIM = "Ruim";
    public static final String ESTADO_PESSIMO = "Péssimo";
    
    // Constantes para status
    public static final String STATUS_ATIVO = "ATIVO";
    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_BAIXADO = "BAIXADO";
    public static final String STATUS_ESTORNADO = "ESTORNADO";
    public static final String STATUS_INATIVO = "INATIVO"; // Mantido para compatibilidade
    
    // Constantes para categorias
    public static final String CATEGORIA_MOVEIS = "Móveis";
    public static final String CATEGORIA_EQUIPAMENTOS = "Equipamentos";
    public static final String CATEGORIA_INFORMATICA = "Informática";
    public static final String CATEGORIA_VEICULOS = "Veículos";
    public static final String CATEGORIA_OUTROS = "Outros";
    
    // Construtor padrão
    public Patrimonio() {
        this.status = STATUS_ATIVO;
        this.situacao = STATUS_ATIVO;
        this.dataCarga = new Timestamp(System.currentTimeMillis());
    }
    
    // Construtor com parâmetros principais
    public Patrimonio(String numero, String descricao) {
        this();
        this.numero = numero;
        this.descricao = descricao;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    // Método de compatibilidade
    public String getNumeroPatrimonio() {
        return numero;
    }
    
    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numero = numeroPatrimonio;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getRotulos() {
        return rotulos;
    }
    
    public void setRotulos(String rotulos) {
        this.rotulos = rotulos;
    }
    
    public String getNumeroSerie() {
        return numeroSerie;
    }
    
    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getEstadoConservacao() {
        return estadoConservacao;
    }
    
    public void setEstadoConservacao(String estadoConservacao) {
        this.estadoConservacao = estadoConservacao;
    }
    
    // Método alias para compatibilidade
    public void setEstado(String estado) {
        this.estadoConservacao = estado;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public String getSituacao() {
        return situacao;
    }
    
    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }
    
    public String getDescricaoResumida() {
        return descricaoResumida;
    }

    public void setDescricaoResumida(String descricaoResumida) {
        this.descricaoResumida = descricaoResumida;
    }
    
    /**
     * Obtém a categoria do patrimônio
     * Prioriza a categoria treinada pela IA, senão usa classificação baseada na descrição
     */
    public String getCategoria() {
        // Se há categoria treinada pela IA, usar ela
        if (categoria != null && !categoria.trim().isEmpty()) {
            return categoria;
        }
        
        // Senão, usar classificação baseada na descrição (fallback)
        if (descricao == null) return "OUTROS";
        
        String desc = descricao.toLowerCase();
        if (desc.contains("computador") || desc.contains("notebook") || desc.contains("monitor") || 
            desc.contains("impressora") || desc.contains("scanner")) {
            return "INFORMATICA";
        } else if (desc.contains("mesa") || desc.contains("cadeira") || desc.contains("armario") || 
                   desc.contains("estante")) {
            return "MOBILIARIO";
        } else if (desc.contains("geladeira") || desc.contains("microondas") || desc.contains("ar condicionado")) {
            return "ELETRODOMESTICO";
        } else if (desc.contains("carro") || desc.contains("caminhao") || desc.contains("moto")) {
            return "VEICULO";
        } else if (desc.contains("microscopio") || desc.contains("balanca") || desc.contains("centrifuga")) {
            return "EQUIPAMENTO_LABORATORIO";
        }
        return "OUTROS";
    }
    
    /**
     * Define a categoria do patrimônio (usado pela IA treinada)
     */
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    /**
     * Gera automaticamente a descrição resumida baseada na descrição completa
     */
    public void gerarDescricaoResumida() {
        if (this.descricao != null && !this.descricao.trim().isEmpty()) {
            try {
                // Importar o serviço dinamicamente para evitar dependência circular
                Class<?> servicoClass = Class.forName("com.inventario.sihcp.service.DescricaoResumoService");
                java.lang.reflect.Method metodo = servicoClass.getMethod("gerarResumo", String.class);
                this.descricaoResumida = (String) metodo.invoke(null, this.descricao);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                // Fallback: usar os primeiros 50 caracteres
                this.descricaoResumida = this.descricao.length() > 50 ? 
                    this.descricao.substring(0, 47) + "..." : this.descricao;
            }
        } else {
            this.descricaoResumida = "";
        }
    }
    
    /**
     * Retorna a descrição resumida, gerando automaticamente se necessário
     */
    public String getDescricaoResumidaOuGerada() {
        if (descricaoResumida == null || descricaoResumida.trim().isEmpty()) {
            gerarDescricaoResumida();
        }
        return descricaoResumida;
    }
    
    public BigDecimal getValorAquisicao() {
        return valorAquisicao;
    }
    
    public void setValorAquisicao(BigDecimal valorAquisicao) {
        this.valorAquisicao = valorAquisicao;
    }
    
    /**
     * Método de compatibilidade para AI classes
     * @return valor de aquisição
     */
    public BigDecimal getValor() {
        return valorAquisicao;
    }
    
    public BigDecimal getValorDepreciado() {
        return valorDepreciado;
    }
    
    public void setValorDepreciado(BigDecimal valorDepreciado) {
        this.valorDepreciado = valorDepreciado;
    }
    
    public String getNumeroNotaFiscal() {
        return numeroNotaFiscal;
    }
    
    public void setNumeroNotaFiscal(String numeroNotaFiscal) {
        this.numeroNotaFiscal = numeroNotaFiscal;
    }
    
    public java.sql.Date getDataEntrada() {
        return dataEntrada;
    }
    
    public void setDataEntrada(java.sql.Date dataEntrada) {
        this.dataEntrada = dataEntrada;
    }
    
    public Timestamp getDataCarga() {
        return dataCarga;
    }
    
    public void setDataCarga(Timestamp dataCarga) {
        this.dataCarga = dataCarga;
    }
    
    public String getFornecedor() {
        return fornecedor;
    }
    
    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }
    
    public String getEd() {
        return ed;
    }
    
    public void setEd(String ed) {
        this.ed = ed;
    }
    
    public int getIdResponsavel() {
        return idResponsavel;
    }
    
    public void setIdResponsavel(int idResponsavel) {
        this.idResponsavel = idResponsavel;
    }
    
    public int getIdSala() {
        return idSala;
    }
    
    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }
    
    // Métodos de compatibilidade com versão anterior
    public String getAtivo() {
        return status;
    }
    
    public void setAtivo(String ativo) {
        this.status = ativo;
    }
    
    public Timestamp getDataCadastro() {
        return dataCarga;
    }
    
    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCarga = dataCadastro;
    }
    
    public Date getDataAquisicao() {
        return dataEntrada;
    }
    
    public void setDataAquisicao(Date dataAquisicao) {
        if (dataAquisicao != null) {
            this.dataEntrada = new java.sql.Date(dataAquisicao.getTime());
        } else {
            this.dataEntrada = null;
        }
    }
    
    // Campos transientes
    public String getNomeResponsavel() {
        return nomeResponsavel;
    }
    
    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }
    
    public String getNomeSala() {
        return nomeSala;
    }
    
    public void setNomeSala(String nomeSala) {
        this.nomeSala = nomeSala;
    }
    
    // Métodos utilitários
    public boolean isAtivo() {
        return STATUS_ATIVO.equals(status);
    }
    
    public String getStatusAtivacao() {
        return status != null ? status : STATUS_ATIVO;
    }
    
    public String getIdentificacaoCompleta() {
        StringBuilder sb = new StringBuilder();
        if (numero != null && !numero.trim().isEmpty()) {
            sb.append("Nº ").append(numero);
        }
        if (descricao != null && !descricao.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(descricao);
        }
        return sb.toString();
    }
    
    public String getLocalizacaoCompleta() {
        StringBuilder sb = new StringBuilder();
        if (nomeSala != null && !nomeSala.trim().isEmpty()) {
            sb.append("Sala ").append(nomeSala);
        }
        return sb.toString();
    }
    
    public String getValorFormatado() {
        if (valorAquisicao == null) {
            return "R$ 0,00";
        }
        return CURRENCY_FORMAT.format(valorAquisicao);
    }
    
    public String getDataEntradaFormatada() {
        return DateFormatUtils.formatWithDefault(dataEntrada, "");
    }
    
    public String getDataCargaFormatada() {
        return DateFormatUtils.formatWithDefault(dataCarga, "");
    }
    
    // Métodos de compatibilidade
    public String getDataAquisicaoFormatada() {
        return getDataEntradaFormatada();
    }
    
    public String getDataCadastroFormatada() {
        return getDataCargaFormatada();
    }
    
    public String getValorDepreciadoFormatado() {
        if (valorDepreciado == null) {
            return "R$ 0,00";
        }
        return CURRENCY_FORMAT.format(valorDepreciado);
    }
    
    public boolean temInformacoesBasicasCompletas() {
        return numero != null && !numero.trim().isEmpty() &&
               descricao != null && !descricao.trim().isEmpty() &&
               idResponsavel > 0 && idSala > 0;
    }
    
    public String getResumo() {
        return String.format("%s - %s (%s)", 
            numero != null ? numero : "S/N",
            descricao != null ? descricao : "Sem descrição",
            getStatusAtivacao());
    }
    
    @Override
    public String toString() {
        return getIdentificacaoCompleta();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Patrimonio patrimonio = (Patrimonio) obj;
        return id == patrimonio.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Getters e Setters para campos SIADS
    public String getCpfResponsavel() {
        return cpfResponsavel;
    }
    
    public void setCpfResponsavel(String cpfResponsavel) {
        this.cpfResponsavel = cpfResponsavel;
    }
    
    public String getMatriculaResponsavel() {
        return matriculaResponsavel;
    }
    
    public void setMatriculaResponsavel(String matriculaResponsavel) {
        this.matriculaResponsavel = matriculaResponsavel;
    }
    
    public String getNomeSetor() {
        return nomeSetor;
    }
    
    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }
    
    public String getCodigoUOrg() {
        return codigoUOrg;
    }
    
    public void setCodigoUOrg(String codigoUOrg) {
        this.codigoUOrg = codigoUOrg;
    }
}
