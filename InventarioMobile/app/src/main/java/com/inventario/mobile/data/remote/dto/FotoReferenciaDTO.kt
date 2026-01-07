package com.inventario.mobile.data.remote.dto

import android.util.Base64
import com.inventario.mobile.data.local.entity.FotoReferenciaEntity

/**
 * DTO para foto de referência recebida do servidor
 * Corresponde ao MobileFotoReferenciaDTO.java do backend
 * 
 * A imagem é transmitida em Base64 para compatibilidade com JSON.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
data class FotoReferenciaDTO(
    val id: Int,
    val descricaoNormalizada: String,
    val imagemBase64: String? = null,
    val hashImagem: String? = null,
    val tamanhoBytes: Int = 0,
    val dataAtualizacao: Long = 0,
    val ativo: Boolean = true
) {
    /**
     * Verifica se o DTO possui imagem
     */
    fun possuiImagem(): Boolean = !imagemBase64.isNullOrEmpty()
    
    /**
     * Converte a imagem Base64 para ByteArray
     * 
     * @return ByteArray da imagem ou null se não houver imagem
     */
    fun decodificarImagem(): ByteArray? {
        return if (possuiImagem()) {
            try {
                Base64.decode(imagemBase64, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Converte DTO para Entity Room
     * 
     * @return FotoReferenciaEntity para persistência local
     */
    fun toEntity(): FotoReferenciaEntity {
        return FotoReferenciaEntity(
            id = id,
            descricaoNormalizada = descricaoNormalizada,
            imagemBlob = decodificarImagem(),
            hashImagem = hashImagem,
            tamanhoBytes = tamanhoBytes,
            dataAtualizacao = dataAtualizacao,
            ativo = ativo
        )
    }
    
    companion object {
        /**
         * Cria DTO a partir de Entity Room
         * 
         * @param entity Entity Room
         * @return DTO para envio ao servidor
         */
        fun fromEntity(entity: FotoReferenciaEntity): FotoReferenciaDTO {
            val imagemBase64 = entity.imagemBlob?.let {
                Base64.encodeToString(it, Base64.DEFAULT)
            }
            
            return FotoReferenciaDTO(
                id = entity.id,
                descricaoNormalizada = entity.descricaoNormalizada,
                imagemBase64 = imagemBase64,
                hashImagem = entity.hashImagem,
                tamanhoBytes = entity.tamanhoBytes,
                dataAtualizacao = entity.dataAtualizacao,
                ativo = entity.ativo
            )
        }
    }
}
