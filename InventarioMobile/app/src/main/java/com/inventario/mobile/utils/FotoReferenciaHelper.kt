package com.inventario.mobile.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.inventario.mobile.data.local.entity.FotoReferenciaEntity
import com.inventario.mobile.domain.repository.FotoReferenciaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper para busca e cache de fotos de referência
 * 
 * Fornece métodos utilitários para:
 * - Buscar foto por descrição de patrimônio
 * - Normalizar descrições para busca
 * - Cache em memória de Bitmaps para performance
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@Singleton
class FotoReferenciaHelper @Inject constructor(
    private val fotoReferenciaRepository: FotoReferenciaRepository
) {
    
    companion object {
        private const val TAG = "FotoReferenciaHelper"
        
        // Cache em memória para Bitmaps (1/8 da memória disponível)
        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        private val cacheSize = maxMemory / 8
    }
    
    // LruCache para Bitmaps decodificados
    private val bitmapCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // Tamanho em KB
            return bitmap.byteCount / 1024
        }
    }
    
    // ========================================
    // Busca de Fotos
    // ========================================
    
    /**
     * Busca foto de referência por descrição do patrimônio
     * Retorna Bitmap ou null se não encontrada
     * 
     * @param descricao Descrição do patrimônio (será normalizada)
     * @return Bitmap da foto ou null
     */
    suspend fun buscarFotoPorDescricao(descricao: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val descricaoNormalizada = normalizarDescricao(descricao)
            
            // Verificar cache em memória primeiro
            bitmapCache.get(descricaoNormalizada)?.let { cached ->
                Log.d(TAG, "Cache hit para: $descricaoNormalizada")
                return@withContext cached
            }
            
            // Buscar no repositório (cache local Room)
            val fotoEntity = fotoReferenciaRepository.buscarPorDescricao(descricaoNormalizada)
            
            if (fotoEntity != null && fotoEntity.possuiImagem()) {
                val bitmap = decodificarImagem(fotoEntity.imagemBlob!!)
                
                if (bitmap != null) {
                    // Adicionar ao cache em memória
                    bitmapCache.put(descricaoNormalizada, bitmap)
                    Log.d(TAG, "Foto encontrada para: $descricaoNormalizada")
                }
                
                return@withContext bitmap
            }
            
            Log.d(TAG, "Foto não encontrada para: $descricaoNormalizada")
            null
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar foto para: $descricao", e)
            null
        }
    }
    
    /**
     * Busca foto de referência por descrição (retorna Entity)
     * Útil quando precisa de metadados além da imagem
     * 
     * @param descricao Descrição do patrimônio
     * @return FotoReferenciaEntity ou null
     */
    suspend fun buscarEntidadePorDescricao(descricao: String): FotoReferenciaEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val descricaoNormalizada = normalizarDescricao(descricao)
                fotoReferenciaRepository.buscarPorDescricao(descricaoNormalizada)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar entidade para: $descricao", e)
                null
            }
        }
    }
    
    /**
     * Busca fotos para múltiplas descrições (otimizado para listas)
     * Retorna mapa de descrição normalizada -> Bitmap
     * 
     * @param descricoes Lista de descrições
     * @return Map de descrição normalizada para Bitmap
     */
    suspend fun buscarFotosParaDescricoes(descricoes: List<String>): Map<String, Bitmap> {
        return withContext(Dispatchers.IO) {
            val resultado = mutableMapOf<String, Bitmap>()
            
            try {
                val descricoesNormalizadas = descricoes.map { normalizarDescricao(it) }.distinct()
                
                // Verificar cache primeiro
                val naoEmCache = mutableListOf<String>()
                for (descricao in descricoesNormalizadas) {
                    bitmapCache.get(descricao)?.let { cached ->
                        resultado[descricao] = cached
                    } ?: naoEmCache.add(descricao)
                }
                
                // Buscar as que não estão em cache
                if (naoEmCache.isNotEmpty()) {
                    val entidades = fotoReferenciaRepository.buscarPorDescricoes(naoEmCache)
                    
                    for (entidade in entidades) {
                        if (entidade.possuiImagem()) {
                            decodificarImagem(entidade.imagemBlob!!)?.let { bitmap ->
                                resultado[entidade.descricaoNormalizada] = bitmap
                                bitmapCache.put(entidade.descricaoNormalizada, bitmap)
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Fotos encontradas: ${resultado.size}/${descricoes.size}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar fotos em lote", e)
            }
            
            resultado
        }
    }
    
    /**
     * Verifica se existe foto para a descrição
     * Mais rápido que buscar a foto completa
     * 
     * @param descricao Descrição do patrimônio
     * @return true se existe foto
     */
    suspend fun existeFotoPara(descricao: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val descricaoNormalizada = normalizarDescricao(descricao)
                
                // Verificar cache primeiro
                if (bitmapCache.get(descricaoNormalizada) != null) {
                    return@withContext true
                }
                
                // Verificar no repositório
                val entidade = fotoReferenciaRepository.buscarPorDescricao(descricaoNormalizada)
                entidade?.possuiImagem() == true
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar existência de foto", e)
                false
            }
        }
    }
    
    // ========================================
    // Normalização de Descrições
    // ========================================
    
    /**
     * Normaliza descrição para busca
     * Remove padrões conhecidos que variam entre patrimônios do mesmo tipo
     * 
     * Padrões removidos:
     * - "PATRIMÔNIO ANAC XXXXXXX" ou "- PATRIMÔNIO ANAC XXXXXXX"
     * - Números de série após "N/S:" ou "SÉRIE:"
     * - Espaços múltiplos
     * 
     * @param descricao Descrição original
     * @return Descrição normalizada
     */
    fun normalizarDescricao(descricao: String): String {
        var normalizada = descricao.uppercase().trim()
        
        // Remover padrão "PATRIMÔNIO ANAC XXXXXXX" ou "- PATRIMÔNIO ANAC XXXXXXX"
        normalizada = normalizada.replace(Regex("-?\\s*PATRIMÔNIO\\s+ANAC\\s+\\d+"), "")
        
        // Remover números de série após "N/S:" ou "SÉRIE:"
        normalizada = normalizada.replace(Regex("N/S:\\s*\\S+"), "")
        normalizada = normalizada.replace(Regex("SÉRIE:\\s*\\S+"), "")
        normalizada = normalizada.replace(Regex("S/N:\\s*\\S+"), "")
        normalizada = normalizada.replace(Regex("SERIAL:\\s*\\S+"), "")
        
        // Remover números de patrimônio no final (padrão comum)
        normalizada = normalizada.replace(Regex("\\s+\\d{5,}$"), "")
        
        // Normalizar espaços múltiplos
        normalizada = normalizada.replace(Regex("\\s+"), " ").trim()
        
        return normalizada
    }
    
    // ========================================
    // Gerenciamento de Cache
    // ========================================
    
    /**
     * Limpa o cache em memória de Bitmaps
     * Útil quando há pressão de memória
     */
    fun limparCache() {
        bitmapCache.evictAll()
        Log.d(TAG, "Cache de Bitmaps limpo")
    }
    
    /**
     * Remove uma entrada específica do cache
     * 
     * @param descricao Descrição a remover
     */
    fun removerDoCache(descricao: String) {
        val normalizada = normalizarDescricao(descricao)
        bitmapCache.remove(normalizada)
    }
    
    /**
     * Obtém estatísticas do cache em memória
     * 
     * @return Pair de (hits, misses)
     */
    fun obterEstatisticasCache(): Pair<Int, Int> {
        return Pair(bitmapCache.hitCount(), bitmapCache.missCount())
    }
    
    /**
     * Obtém tamanho atual do cache em KB
     */
    fun tamanhoCache(): Int {
        return bitmapCache.size()
    }
    
    // ========================================
    // Utilitários Privados
    // ========================================
    
    /**
     * Decodifica ByteArray para Bitmap
     * Usa opções otimizadas para thumbnails
     */
    private fun decodificarImagem(dados: ByteArray): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                // Não fazer scale automático
                inScaled = false
                // Usar configuração RGB_565 para economizar memória (thumbnails)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeByteArray(dados, 0, dados.size, options)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao decodificar imagem", e)
            null
        }
    }
}
