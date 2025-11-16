package com.inventario.mobile.domain.model

import java.math.BigDecimal
import java.util.Date

/**
 * Domain Model para consulta de patrimônios
 * Modelo puro sem dependências Android
 */
data class PatrimonioConsulta(
    val id: Int,
    val codigo: String,
    val descricao: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val estado: String? = null,
    val valor: BigDecimal? = null,
    val observacoes: String? = null,
    
    // Sala
    val salaId: Int? = null,
    val salaNome: String? = null,
    
    // Responsável
    val responsavelId: Int? = null,
    val responsavelNome: String? = null,
    
    // Setor
    val setorId: Int? = null,
    val setorNome: String? = null,
    
    // Status de coleta
    val coletado: Boolean = false,
    val dataColeta: Date? = null
) {
    /**
     * Retorna descrição resumida para exibição em listas
     */
    fun getDescricaoResumo(): String {
        return if (descricao.length > 50) {
            "${descricao.take(47)}..."
        } else {
            descricao
        }
    }
    
    /**
     * Retorna localização completa
     */
    fun getLocalizacaoCompleta(): String {
        return salaNome ?: "Sem localização"
    }
    
    /**
     * Retorna responsável ou mensagem padrão
     */
    fun getResponsavelOuPadrao(): String {
        return responsavelNome ?: "Sem responsável"
    }
    
    /**
     * Retorna status de coleta formatado
     */
    fun getStatusColeta(): String {
        return if (coletado) "✅ Coletado" else "⏳ Pendente"
    }
    
    /**
     * Verifica se tem informações completas
     */
    fun temInformacoesCompletas(): Boolean {
        return salaNome != null && responsavelNome != null
    }
    
    /**
     * Retorna valor formatado
     */
    fun getValorFormatado(): String {
        return valor?.let { "R$ ${String.format("%.2f", it)}" } ?: "Sem valor"
    }
}
