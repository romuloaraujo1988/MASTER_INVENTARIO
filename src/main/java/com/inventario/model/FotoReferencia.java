package com.inventario.model;

import java.sql.Timestamp;
import java.util.Objects;

import com.inventario.util.DateFormatUtils;

/**
 * Classe que representa uma Foto de Referência vinculada a uma descrição de patrimônio.
 * 
 * A foto de referência é uma imagem thumbnail (200x200px, max 50KB) que serve para
 * identificação visual de tipos de patrimônios no app mobile durante a coleta.
 * 
 * Uma foto de referência é vinculada a uma descrição normalizada, podendo assim
 * servir para múltiplos patrimônios do mesmo tipo.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
public class FotoReferencia {
    
    // Constantes
    public static final int TAMANHO_MAXIMO_BYTES = 51200; // 50KB
    public static final int LARGURA_MAXIMA = 200;
    public static final int ALTURA_MAXIMA = 200;
    
    // Campos principais
    private int id;
    private String descricaoNormalizada;
    private byte[] imagemBlob;
    private String hashImagem;
    private int tamanhoBytes;
    private Timestamp dataCadastro;
    private Timestamp dataAtualizacao;
    private boolean ativo;
    private String usuarioCadastro;
    
    // Campo transiente para contagem de patrimônios beneficiados
    private int quantidadePatrimonios;
    
    /**
     * Construtor padrão
     */
    public FotoReferencia() {
        this.ativo = true;
        this.dataCadastro = new Timestamp(System.currentTimeMillis());
        this.dataAtualizacao = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Construtor com descrição normalizada
     * 
     * @param descricaoNormalizada Descrição normalizada do patrimônio
     */
    public FotoReferencia(String descricaoNormalizada) {
        this();
        this.descricaoNormalizada = descricaoNormalizada;
    }
    
    /**
     * Construtor completo
     * 
     * @param descricaoNormalizada Descrição normalizada do patrimônio
     * @param imagemBlob Bytes da imagem (thumbnail JPEG)
     * @param hashImagem Hash SHA-256 da imagem
     */
    public FotoReferencia(String descricaoNormalizada, byte[] imagemBlob, String hashImagem) {
        this(descricaoNormalizada);
        this.imagemBlob = imagemBlob;
        this.hashImagem = hashImagem;
        this.tamanhoBytes = imagemBlob != null ? imagemBlob.length : 0;
    }
    
    // Getters e Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getDescricaoNormalizada() {
        return descricaoNormalizada;
    }
    
    public void setDescricaoNormalizada(String descricaoNormalizada) {
        this.descricaoNormalizada = descricaoNormalizada;
    }
    
    public byte[] getImagemBlob() {
        return imagemBlob;
    }
    
    public void setImagemBlob(byte[] imagemBlob) {
        this.imagemBlob = imagemBlob;
        this.tamanhoBytes = imagemBlob != null ? imagemBlob.length : 0;
    }
    
    public String getHashImagem() {
        return hashImagem;
    }
    
    public void setHashImagem(String hashImagem) {
        this.hashImagem = hashImagem;
    }
    
    public int getTamanhoBytes() {
        return tamanhoBytes;
    }
    
    public void setTamanhoBytes(int tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }
    
    public Timestamp getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public Timestamp getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(Timestamp dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    public String getUsuarioCadastro() {
        return usuarioCadastro;
    }
    
    public void setUsuarioCadastro(String usuarioCadastro) {
        this.usuarioCadastro = usuarioCadastro;
    }
    
    public int getQuantidadePatrimonios() {
        return quantidadePatrimonios;
    }
    
    public void setQuantidadePatrimonios(int quantidadePatrimonios) {
        this.quantidadePatrimonios = quantidadePatrimonios;
    }
    
    // Métodos utilitários
    
    /**
     * Verifica se a foto possui imagem válida
     * 
     * @return true se possui imagem com tamanho válido
     */
    public boolean possuiImagem() {
        return imagemBlob != null && imagemBlob.length > 0;
    }
    
    /**
     * Verifica se o tamanho da imagem está dentro do limite
     * 
     * @return true se tamanho <= 50KB
     */
    public boolean tamanhoValido() {
        return tamanhoBytes <= TAMANHO_MAXIMO_BYTES;
    }
    
    /**
     * Retorna o tamanho formatado em KB
     * 
     * @return String formatada (ex: "45.2 KB")
     */
    public String getTamanhoFormatado() {
        if (tamanhoBytes < 1024) {
            return tamanhoBytes + " bytes";
        }
        double kb = tamanhoBytes / 1024.0;
        return String.format("%.1f KB", kb);
    }
    
    /**
     * Retorna a data de cadastro formatada
     * 
     * @return Data formatada ou string vazia
     */
    public String getDataCadastroFormatada() {
        return DateFormatUtils.formatWithDefault(dataCadastro, "");
    }
    
    /**
     * Retorna a data de atualização formatada
     * 
     * @return Data formatada ou string vazia
     */
    public String getDataAtualizacaoFormatada() {
        return DateFormatUtils.formatWithDefault(dataAtualizacao, "");
    }
    
    /**
     * Atualiza o timestamp de atualização para agora
     */
    public void atualizarTimestamp() {
        this.dataAtualizacao = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Marca a foto como excluída (soft delete)
     */
    public void excluir() {
        this.ativo = false;
        atualizarTimestamp();
    }
    
    /**
     * Verifica se todos os campos obrigatórios estão preenchidos
     * 
     * @return true se todos os metadados obrigatórios estão presentes
     */
    public boolean metadadosCompletos() {
        return descricaoNormalizada != null && !descricaoNormalizada.trim().isEmpty()
            && hashImagem != null && !hashImagem.trim().isEmpty()
            && dataCadastro != null
            && tamanhoBytes > 0;
    }
    
    /**
     * Retorna resumo da foto para exibição
     * 
     * @return String com resumo
     */
    public String getResumo() {
        return String.format("Foto #%d - %s (%s)", 
            id,
            descricaoNormalizada != null ? 
                (descricaoNormalizada.length() > 50 ? 
                    descricaoNormalizada.substring(0, 47) + "..." : 
                    descricaoNormalizada) : 
                "Sem descrição",
            getTamanhoFormatado());
    }
    
    @Override
    public String toString() {
        return getResumo();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FotoReferencia that = (FotoReferencia) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
