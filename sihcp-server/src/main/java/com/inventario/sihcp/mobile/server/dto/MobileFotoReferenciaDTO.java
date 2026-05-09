package com.inventario.sihcp.mobile.server.dto;

import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventario.sihcp.model.FotoReferencia;

/**
 * DTO para foto de referência na API Mobile.
 * 
 * Usado para sincronização de fotos de referência entre servidor e app Android.
 * A imagem é transmitida em Base64 para compatibilidade com JSON.
 * 
 * Suporta delta sync através do campo dataAtualizacao.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileFotoReferenciaDTO {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("descricaoNormalizada")
    private String descricaoNormalizada;
    
    @JsonProperty("imagemBase64")
    private String imagemBase64;
    
    @JsonProperty("hashImagem")
    private String hashImagem;
    
    @JsonProperty("tamanhoBytes")
    private Integer tamanhoBytes;
    
    @JsonProperty("dataAtualizacao")
    private Long dataAtualizacao;
    
    @JsonProperty("ativo")
    private Boolean ativo;
    
    // Construtores
    
    public MobileFotoReferenciaDTO() {}
    
    /**
     * Construtor a partir de entidade FotoReferencia
     * 
     * @param foto Entidade FotoReferencia
     */
    public MobileFotoReferenciaDTO(FotoReferencia foto) {
        if (foto != null) {
            this.id = foto.getId();
            this.descricaoNormalizada = foto.getDescricaoNormalizada();
            this.hashImagem = foto.getHashImagem();
            this.tamanhoBytes = foto.getTamanhoBytes();
            this.ativo = foto.isAtivo();
            
            // Converter imagem para Base64
            if (foto.getImagemBlob() != null && foto.getImagemBlob().length > 0) {
                this.imagemBase64 = Base64.getEncoder().encodeToString(foto.getImagemBlob());
            }
            
            // Converter timestamp para milissegundos
            if (foto.getDataAtualizacao() != null) {
                this.dataAtualizacao = foto.getDataAtualizacao().getTime();
            }
        }
    }
    
    /**
     * Construtor para resposta sem imagem (apenas metadados)
     * Útil para verificar se cliente precisa baixar a imagem
     * 
     * @param foto Entidade FotoReferencia
     * @param incluirImagem Se deve incluir a imagem Base64
     */
    public MobileFotoReferenciaDTO(FotoReferencia foto, boolean incluirImagem) {
        if (foto != null) {
            this.id = foto.getId();
            this.descricaoNormalizada = foto.getDescricaoNormalizada();
            this.hashImagem = foto.getHashImagem();
            this.tamanhoBytes = foto.getTamanhoBytes();
            this.ativo = foto.isAtivo();
            
            if (foto.getDataAtualizacao() != null) {
                this.dataAtualizacao = foto.getDataAtualizacao().getTime();
            }
            
            // Incluir imagem apenas se solicitado
            if (incluirImagem && foto.getImagemBlob() != null && foto.getImagemBlob().length > 0) {
                this.imagemBase64 = Base64.getEncoder().encodeToString(foto.getImagemBlob());
            }
        }
    }
    
    /**
     * Converte DTO para entidade FotoReferencia
     * 
     * @return Entidade FotoReferencia
     */
    public FotoReferencia toEntity() {
        FotoReferencia foto = new FotoReferencia();
        foto.setId(this.id != null ? this.id : 0);
        foto.setDescricaoNormalizada(this.descricaoNormalizada);
        foto.setHashImagem(this.hashImagem);
        foto.setTamanhoBytes(this.tamanhoBytes != null ? this.tamanhoBytes : 0);
        foto.setAtivo(this.ativo != null ? this.ativo : true);
        
        // Converter Base64 para bytes
        if (this.imagemBase64 != null && !this.imagemBase64.isEmpty()) {
            foto.setImagemBlob(Base64.getDecoder().decode(this.imagemBase64));
        }
        
        return foto;
    }
    
    // Getters e Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getDescricaoNormalizada() {
        return descricaoNormalizada;
    }
    
    public void setDescricaoNormalizada(String descricaoNormalizada) {
        this.descricaoNormalizada = descricaoNormalizada;
    }
    
    public String getImagemBase64() {
        return imagemBase64;
    }
    
    public void setImagemBase64(String imagemBase64) {
        this.imagemBase64 = imagemBase64;
    }
    
    public String getHashImagem() {
        return hashImagem;
    }
    
    public void setHashImagem(String hashImagem) {
        this.hashImagem = hashImagem;
    }
    
    public Integer getTamanhoBytes() {
        return tamanhoBytes;
    }
    
    public void setTamanhoBytes(Integer tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }
    
    public Long getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(Long dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    /**
     * Verifica se o DTO possui imagem
     * 
     * @return true se possui imagem Base64
     */
    public boolean possuiImagem() {
        return imagemBase64 != null && !imagemBase64.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("MobileFotoReferenciaDTO{id=%d, descricao='%s', hash='%s', tamanho=%d, ativo=%s}",
            id,
            descricaoNormalizada != null ? 
                (descricaoNormalizada.length() > 30 ? descricaoNormalizada.substring(0, 27) + "..." : descricaoNormalizada) : 
                "null",
            hashImagem != null ? hashImagem.substring(0, Math.min(8, hashImagem.length())) + "..." : "null",
            tamanhoBytes,
            ativo);
    }
}
