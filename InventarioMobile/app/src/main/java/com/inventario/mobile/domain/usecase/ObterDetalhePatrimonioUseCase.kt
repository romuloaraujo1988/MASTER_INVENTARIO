package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.PatrimonioDetalhe
import com.inventario.mobile.domain.repository.PatrimonioConsultaRepository
import javax.inject.Inject

/**
 * Use Case: Obter detalhes completos de um patrimônio
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 */
class ObterDetalhePatrimonioUseCase @Inject constructor(
    private val repository: PatrimonioConsultaRepository
) {
    /**
     * Executa o caso de uso
     * 
     * @param patrimonioId ID do patrimônio
     * @return Result com detalhes completos ou erro
     */
    suspend operator fun invoke(
        patrimonioId: Int
    ): Result<PatrimonioDetalhe> {
        return try {
            // Validações de negócio
            if (patrimonioId <= 0) {
                return Result.failure(Exception("ID do patrimônio deve ser maior que zero"))
            }
            
            // Buscar detalhes no repository
            val result = repository.obterDetalhesCompletos(patrimonioId)
            
            // Aplicar regras de negócio adicionais
            result.map { detalhe ->
                // Validar integridade dos dados
                validarIntegridade(detalhe)
                
                // Retornar detalhe enriquecido
                detalhe
            }
            
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao obter detalhes do patrimônio: ${e.message}", e))
        }
    }
    
    /**
     * Valida integridade dos dados do patrimônio
     */
    private fun validarIntegridade(detalhe: PatrimonioDetalhe) {
        // Validar campos obrigatórios
        require(detalhe.codigo.isNotBlank()) { "Código do patrimônio é obrigatório" }
        require(detalhe.descricao.isNotBlank()) { "Descrição do patrimônio é obrigatória" }
        
        // Validar valor se presente
        detalhe.valor?.let { valor ->
            require(valor.signum() >= 0) { "Valor do patrimônio não pode ser negativo" }
        }
        
        // Validar estado se presente
        detalhe.estado?.let { estado ->
            val estadosValidos = listOf("BOM", "REGULAR", "RUIM", "INUTILIZADO")
            require(estado.uppercase() in estadosValidos) { 
                "Estado inválido: $estado. Estados válidos: ${estadosValidos.joinToString()}" 
            }
        }
    }
    
    /**
     * Verifica se o patrimônio tem informações completas
     */
    fun temInformacoesCompletas(detalhe: PatrimonioDetalhe): Boolean {
        return detalhe.temInformacoesCompletas()
    }
    
    /**
     * Verifica se o patrimônio tem divergências
     */
    fun temDivergencias(detalhe: PatrimonioDetalhe): Boolean {
        return detalhe.temDivergencias()
    }
    
    /**
     * Gera relatório de divergências
     */
    fun gerarRelatorioDivergencias(detalhe: PatrimonioDetalhe): String {
        val divergencias = detalhe.getDivergencias()
        
        return if (divergencias.isEmpty()) {
            "✅ Nenhuma divergência encontrada"
        } else {
            buildString {
                appendLine("⚠️ Divergências encontradas:")
                divergencias.forEachIndexed { index, div ->
                    appendLine("${index + 1}. $div")
                }
            }
        }
    }
    
    /**
     * Verifica se o patrimônio precisa de atenção
     */
    fun precisaAtencao(detalhe: PatrimonioDetalhe): Boolean {
        return detalhe.temDivergencias() || 
               !detalhe.temInformacoesCompletas() ||
               detalhe.estado?.uppercase() in listOf("RUIM", "INUTILIZADO")
    }
    
    /**
     * Gera resumo executivo do patrimônio
     */
    fun gerarResumoExecutivo(detalhe: PatrimonioDetalhe): String {
        return buildString {
            appendLine("📦 PATRIMÔNIO ${detalhe.codigo}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📝 ${detalhe.descricao}")
            appendLine()
            
            // Status
            appendLine("📊 STATUS")
            appendLine("   ${detalhe.getStatusColetaCompleto()}")
            if (detalhe.temDivergencias()) {
                appendLine("   ⚠️ Possui divergências")
            }
            appendLine()
            
            // Localização
            appendLine("📍 LOCALIZAÇÃO")
            appendLine("   ${detalhe.getLocalizacaoCompleta()}")
            appendLine()
            
            // Responsável
            appendLine("👤 RESPONSÁVEL")
            appendLine("   ${detalhe.getResponsavelCompleto()}")
            appendLine()
            
            // Valor
            appendLine("💰 VALOR")
            appendLine("   ${detalhe.getValorFormatado()}")
            appendLine()
            
            // Alertas
            if (precisaAtencao(detalhe)) {
                appendLine("⚠️ REQUER ATENÇÃO")
                if (detalhe.temDivergencias()) {
                    appendLine("   • Possui divergências")
                }
                if (!detalhe.temInformacoesCompletas()) {
                    appendLine("   • Informações incompletas")
                }
                if (detalhe.estado?.uppercase() in listOf("RUIM", "INUTILIZADO")) {
                    appendLine("   • Estado crítico: ${detalhe.estado}")
                }
            }
        }
    }
}
