package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.PatrimonioConsulta
import com.inventario.mobile.domain.repository.PatrimonioConsultaRepository
import javax.inject.Inject

/**
 * Use Case: Buscar patrimônios por descrição
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 */
class BuscarPatrimonioPorDescricaoUseCase @Inject constructor(
    private val repository: PatrimonioConsultaRepository
) {
    /**
     * Executa o caso de uso
     * 
     * @param descricao Descrição ou parte dela
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Result com lista de patrimônios ou erro
     */
    suspend operator fun invoke(
        descricao: String,
        limit: Int = 10
    ): Result<List<PatrimonioConsulta>> {
        return try {
            // Validações de negócio
            if (descricao.isBlank()) {
                return Result.failure(Exception("Descrição não pode estar vazia"))
            }
            
            if (descricao.length < 3) {
                return Result.failure(Exception("Digite ao menos 3 caracteres da descrição"))
            }
            
            if (limit <= 0) {
                return Result.failure(Exception("Limite deve ser maior que zero"))
            }
            
            if (limit > 50) {
                return Result.failure(Exception("Limite não pode ser maior que 50"))
            }
            
            // Sanitizar entrada
            val descricaoLimpa = sanitizarDescricao(descricao)
            
            // Validar se não contém apenas caracteres especiais
            if (descricaoLimpa.length < 3) {
                return Result.failure(Exception("Descrição deve conter ao menos 3 caracteres válidos"))
            }
            
            // Buscar no repository
            val result = repository.buscarPorDescricao(descricaoLimpa, limit)
            
            // Aplicar regras de negócio adicionais
            result.map { patrimonios ->
                // Ordenar por relevância
                patrimonios.sortedWith { p1, p2 ->
                    val desc1 = p1.descricao.lowercase()
                    val desc2 = p2.descricao.lowercase()
                    val termoLower = descricaoLimpa.lowercase()
                    
                    when {
                        // Descrições que começam com o termo primeiro
                        desc1.startsWith(termoLower) && !desc2.startsWith(termoLower) -> -1
                        !desc1.startsWith(termoLower) && desc2.startsWith(termoLower) -> 1
                        
                        // Descrições que contêm o termo como palavra completa
                        desc1.contains(" $termoLower ") && !desc2.contains(" $termoLower ") -> -1
                        !desc1.contains(" $termoLower ") && desc2.contains(" $termoLower ") -> 1
                        
                        // Ordenação alfabética
                        else -> desc1.compareTo(desc2)
                    }
                }
            }
            
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao buscar patrimônios por descrição: ${e.message}", e))
        }
    }
    
    /**
     * Valida se a descrição tem formato válido
     */
    fun isDescricaoValida(descricao: String): Boolean {
        val descricaoLimpa = sanitizarDescricao(descricao)
        return descricaoLimpa.length >= 3
    }
    
    /**
     * Sanitiza a descrição de entrada
     */
    fun sanitizarDescricao(descricao: String): String {
        return descricao.trim()
            .replace(Regex("\\s+"), " ") // Múltiplos espaços -> um espaço
            .replace(Regex("[^a-zA-Z0-9\\sÀ-ÿ]"), "") // Remove caracteres especiais, mantém acentos
    }
    
    /**
     * Gera sugestões de busca baseadas na entrada
     */
    fun gerarSugestoes(entrada: String): List<String> {
        val entradaLimpa = sanitizarDescricao(entrada).lowercase()
        
        if (entradaLimpa.length < 2) return emptyList()
        
        // Sugestões comuns baseadas em tipos de patrimônio
        val sugestoesComuns = listOf(
            "cadeira", "mesa", "computador", "monitor", "impressora",
            "armário", "estante", "telefone", "ar condicionado", "projetor",
            "notebook", "tablet", "scanner", "quadro", "ventilador"
        )
        
        return sugestoesComuns.filter { 
            it.contains(entradaLimpa) || entradaLimpa.contains(it)
        }.take(5)
    }
    
    /**
     * Extrai palavras-chave da descrição
     */
    fun extrairPalavrasChave(descricao: String): List<String> {
        return sanitizarDescricao(descricao)
            .lowercase()
            .split(" ")
            .filter { it.length >= 3 }
            .distinct()
            .take(5)
    }
}
