package com.inventario.mobile.domain.model

import java.math.BigDecimal
import java.util.Date

/**
 * Domain Model para detalhes completos de um patrimônio
 * Inclui todas as informações para visualização detalhada
 */
data class PatrimonioDetalhe(
    // Dados básicos
    val id: Int,
    val codigo: String,
    val descricao: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val estado: String? = null,
    val valor: BigDecimal? = null,
    val observacoes: String? = null,
    
    // Dados da sala
    val salaId: Int? = null,
    val salaNome: String? = null,
    val salaBloco: String? = null,
    val salaAndar: String? = null,
    
    // Dados do responsável
    val responsavelId: Int? = null,
    val responsavelNome: String? = null,
    val responsavelMatricula: String? = null,
    val responsavelSetor: String? = null,
    val responsavelEmail: String? = null,
    val responsavelTelefone: String? = null,
    
    // Status de coleta
    val coletado: Boolean = false,
    val dataColeta: Date? = null,
    val coletadoPor: String? = null,
    val localizacaoEncontrada: String? = null,
    val estadoEncontrado: String? = null,
    val observacoesColeta: String? = null,
    
    // Histórico
    val totalColetas: Int = 0,
    val ultimaColeta: Date? = null,
    
    // Foto
    val fotoUrl: String? = null
) {
    /**
     * Retorna localização completa formatada
     */
    fun getLocalizacaoCompleta(): String {
        return buildString {
            salaNome?.let { append(it) }
            if (salaBloco != null || salaAndar != null) {
                append(" - ")
                salaBloco?.let { append("Bloco $it") }
                if (salaBloco != null && salaAndar != null) append(" - ")
                salaAndar?.let { append(it) }
            }
        }.ifEmpty { "Sem localização" }
    }
    
    /**
     * Retorna responsável completo formatado
     */
    fun getResponsavelCompleto(): String {
        return buildString {
            responsavelNome?.let { append(it) }
            responsavelMatricula?.let { append(" - Mat: $it") }
            responsavelSetor?.let { append(" ($it)") }
        }.ifEmpty { "Sem responsável" }
    }
    
    /**
     * Retorna status de coleta com ícone
     */
    fun getStatusColetaCompleto(): String {
        return if (coletado) {
            buildString {
                append("✅ Coletado")
                dataColeta?.let { 
                    append(" em ${formatDate(it)}")
                }
                coletadoPor?.let {
                    append(" por $it")
                }
            }
        } else {
            "⏳ Pendente de coleta"
        }
    }
    
    /**
     * Retorna valor formatado em moeda brasileira
     */
    fun getValorFormatado(): String {
        return valor?.let { 
            "R$ ${String.format("%,.2f", it)}"
        } ?: "Valor não informado"
    }
    
    /**
     * Verifica se tem foto
     */
    fun temFoto(): Boolean = !fotoUrl.isNullOrEmpty()
    
    /**
     * Verifica se foi coletado
     */
    fun foiColetado(): Boolean = coletado
    
    /**
     * Verifica se tem divergências
     */
    fun temDivergencias(): Boolean {
        return coletado && (
            localizacaoEncontrada != salaNome ||
            estadoEncontrado != estado
        )
    }
    
    /**
     * Retorna informações de divergência
     */
    fun getDivergencias(): List<String> {
        val divergencias = mutableListOf<String>()
        
        if (coletado) {
            if (localizacaoEncontrada != salaNome) {
                divergencias.add("Localização: Cadastrado em '$salaNome', encontrado em '$localizacaoEncontrada'")
            }
            
            if (estadoEncontrado != estado) {
                divergencias.add("Estado: Cadastrado como '$estado', encontrado como '$estadoEncontrado'")
            }
        }
        
        return divergencias
    }
    
    /**
     * Verifica se tem informações completas
     */
    fun temInformacoesCompletas(): Boolean {
        return salaNome != null && 
               responsavelNome != null && 
               valor != null && 
               estado != null
    }
    
    /**
     * Retorna resumo para compartilhamento
     */
    fun getResumoCompartilhamento(): String {
        return buildString {
            appendLine("📦 Patrimônio: $codigo")
            appendLine("📝 Descrição: $descricao")
            marca?.let { appendLine("🏷️ Marca: $it") }
            modelo?.let { appendLine("🔧 Modelo: $it") }
            appendLine("📍 Localização: ${getLocalizacaoCompleta()}")
            appendLine("👤 Responsável: ${getResponsavelCompleto()}")
            appendLine("💰 Valor: ${getValorFormatado()}")
            appendLine("📊 Status: ${getStatusColetaCompleto()}")
        }
    }
    
    private fun formatDate(date: Date): String {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
