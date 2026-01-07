package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo

/**
 * Entity Room para Foto de Referência
 * Armazena fotos de referência por descrição para exibição offline
 * 
 * Suporta delta sync através do campo dataAtualizacao.
 * Imagens são armazenadas como BLOB para acesso rápido.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@Entity(
    tableName = "foto_referencia",
    indices = [
        // Índice único para descrição normalizada (busca principal)
        Index(value = ["descricaoNormalizada"], unique = true),
        
        // Índice para delta sync (buscar atualizadas desde timestamp)
        Index(value = ["dataAtualizacao"]),
        
        // Índice para filtrar ativas
        Index(value = ["ativo"]),
        
        // Índice composto para delta sync de ativas
        Index(value = ["ativo", "dataAtualizacao"])
    ]
)
data class FotoReferenciaEntity(
    @PrimaryKey
    val id: Int,
    
    /**
     * Descrição normalizada do patrimônio
     * Usada como chave de busca (sem números de série, patrimônio, etc.)
     */
    @ColumnInfo(name = "descricaoNormalizada")
    val descricaoNormalizada: String,
    
    /**
     * Imagem em formato BLOB (thumbnail 200x200, max 50KB)
     * Armazenada localmente para acesso rápido offline
     */
    @ColumnInfo(name = "imagemBlob", typeAffinity = ColumnInfo.BLOB)
    val imagemBlob: ByteArray?,
    
    /**
     * Hash SHA-256 da imagem
     * Usado para verificar se a imagem mudou (delta sync)
     */
    @ColumnInfo(name = "hashImagem")
    val hashImagem: String?,
    
    /**
     * Tamanho da imagem em bytes
     * Usado para controle de armazenamento
     */
    @ColumnInfo(name = "tamanhoBytes")
    val tamanhoBytes: Int = 0,
    
    /**
     * Timestamp da última atualização (milissegundos)
     * Usado para delta sync
     */
    @ColumnInfo(name = "dataAtualizacao")
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    /**
     * Flag de ativo/inativo
     * Fotos inativas são mantidas para delta sync (cliente remove do cache)
     */
    @ColumnInfo(name = "ativo")
    val ativo: Boolean = true
) {
    /**
     * Verifica se a foto possui imagem
     */
    fun possuiImagem(): Boolean = imagemBlob != null && imagemBlob.isNotEmpty()
    
    /**
     * Verifica se a foto foi atualizada após o timestamp
     */
    fun foiAtualizadaApos(timestamp: Long): Boolean = dataAtualizacao > timestamp
    
    // Implementação de equals/hashCode para ByteArray
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as FotoReferenciaEntity
        
        if (id != other.id) return false
        if (descricaoNormalizada != other.descricaoNormalizada) return false
        if (imagemBlob != null) {
            if (other.imagemBlob == null) return false
            if (!imagemBlob.contentEquals(other.imagemBlob)) return false
        } else if (other.imagemBlob != null) return false
        if (hashImagem != other.hashImagem) return false
        if (tamanhoBytes != other.tamanhoBytes) return false
        if (dataAtualizacao != other.dataAtualizacao) return false
        if (ativo != other.ativo) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + descricaoNormalizada.hashCode()
        result = 31 * result + (imagemBlob?.contentHashCode() ?: 0)
        result = 31 * result + (hashImagem?.hashCode() ?: 0)
        result = 31 * result + tamanhoBytes
        result = 31 * result + dataAtualizacao.hashCode()
        result = 31 * result + ativo.hashCode()
        return result
    }
}
