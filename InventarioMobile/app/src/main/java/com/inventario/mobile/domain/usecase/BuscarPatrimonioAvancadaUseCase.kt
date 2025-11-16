package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.PatrimonioConsulta
import com.inventario.mobile.domain.repository.PatrimonioConsultaRepository
import javax.inject.Inject

/**
 * Use Case: Busca avançada de patrimônios com múltiplos critérios
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 */
class BuscarPatrimonioAvancadaUseCase @Inject constructor(
    private val repository: PatrimonioConsultaRepository
) {
    /**
     * Executa o caso de uso
     * 
     * @param termo Termo de busca geral
     * @param salaId ID da sala (opcional)
     * @param responsavelId ID do responsável (opcional)
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Result com lista de patrimônios ou erro
     */
    suspend operator fun invoke(
        termo: String,
        salaId: Int? = null,
        responsavelId: Int? = null,
        limit: Int = 10
    ): Result<List<PatrimonioConsulta>> {
        return try {
            // Validações de negócio
            if (termo.isBlank()) {
                return Result.failure(Exception("Termo de busca não pode estar vazio"))
            }
            
            if (termo.length < 2) {
                return Result.failure(Exception("Digite ao menos 2 caracteres para buscar"))
            }
            
            if (limit <= 0) {
                return Result.failure(Exception("Limite deve ser maior que zero"))
            }
            
            if (limit > 50) {
                return Result.failure(Exception("Limite não pode ser maior que 50"))
            }
            
            // Validar IDs se fornecidos
            if (salaId != null && salaId <= 0) {
                return Result.failure(Exception("ID da sala inválido"))
            }
            
            if (responsavelId != null && responsavelId <= 0) {
                return Result.failure(Exception("ID do responsável inválido"))
            }
            
            // Sanitizar entrada
            val termoLimpo = sanitizarTermo(termo)
            
            // Validar termo sanitizado
            if (termoLimpo.length < 2) {
                return Result.failure(Exception("Termo deve conter ao menos 2 caracteres válidos"))
            }
            
            // Buscar no repository
            val result = repository.buscarAvancada(
                termo = termoLimpo,
                salaId = salaId,
                responsavelId = responsavelId,
                limit = limit
            )
            
            // Aplicar regras de negócio adicionais
            result.map { patrimonios ->
                // Ordenar por relevância
                patrimonios.sortedWith(criarComparadorRelevancia(termoLimpo))
            }
            
        } catch (e: Exception) {
            Result.failure(Exception("Erro na busca avançada: ${e.message}", e))
        }
    }
    
    /**
     * Sanitiza o termo de busca
     */
    fun sanitizarTermo(termo: String): String {
        return termo.trim()
            .replace(Regex("\\s+"), " ")
            .uppercase()
    }
    
    /**
     * Cria comparador de relevância baseado no termo
     */
    private fun criarComparadorRelevancia(termo: String): Comparator<PatrimonioConsulta> {
        return Comparator { p1, p2 ->
            val score1 = calcularScoreRelevancia(p1, termo)
            val score2 = calcularScoreRelevancia(p2, termo)
            
            when {
                score1 > score2 -> -1
                score1 < score2 -> 1
                else -> p1.codigo.compareTo(p2.codigo)
            }
        }
    }
    
    /**
     * Calcula score de relevância de um patrimônio
     */
    private fun calcularScoreRelevancia(patrimonio: PatrimonioConsulta, termo: String): Int {
        var score = 0
        val termoLower = termo.lowercase()
        
        // Código exato: +100
        if (patrimonio.codigo.equals(termo, ignoreCase = true)) {
            score += 100
        }
        // Código começa com termo: +50
        else if (patrimonio.codigo.startsWith(termo, ignoreCase = true)) {
            score += 50
        }
        // Código contém termo: +20
        else if (patrimonio.codigo.contains(termo, ignoreCase = true)) {
            score += 20
        }
        
        // Descrição começa com termo: +40
        if (patrimonio.descricao.startsWith(termoLower, ignoreCase = true)) {
            score += 40
        }
        // Descrição contém termo como palavra completa: +30
        else if (patrimonio.descricao.lowercase().contains(" $termoLower ")) {
            score += 30
        }
        // Descrição contém termo: +15
        else if (patrimonio.descricao.contains(termo, ignoreCase = true)) {
            score += 15
        }
        
        // Marca contém termo: +10
        if (patrimonio.marca?.contains(termo, ignoreCase = true) == true) {
            score += 10
        }
        
        // Modelo contém termo: +10
        if (patrimonio.modelo?.contains(termo, ignoreCase = true) == true) {
            score += 10
        }
        
        // Bônus se já foi coletado: +5
        if (patrimonio.coletado) {
            score += 5
        }
        
        // Bônus se tem informações completas: +3
        if (patrimonio.temInformacoesCompletas()) {
            score += 3
        }
        
        return score
    }
    
    /**
     * Valida se os critérios de busca são válidos
     */
    fun isCriteriosValidos(
        termo: String,
        salaId: Int?,
        responsavelId: Int?
    ): Boolean {
        if (termo.isBlank() || termo.length < 2) return false
        if (salaId != null && salaId <= 0) return false
        if (responsavelId != null && responsavelId <= 0) return false
        return true
    }
    
    /**
     * Gera descrição dos critérios de busca
     */
    fun gerarDescricaoCriterios(
        termo: String,
        salaId: Int?,
        responsavelId: Int?,
        salaNome: String? = null,
        responsavelNome: String? = null
    ): String {
        return buildString {
            append("Buscando por: '$termo'")
            
            if (salaId != null) {
                append(" | Sala: ${salaNome ?: "ID $salaId"}")
            }
            
            if (responsavelId != null) {
                append(" | Responsável: ${responsavelNome ?: "ID $responsavelId"}")
            }
        }
    }
    
    /**
     * Sugere refinamentos de busca
     */
    fun sugerirRefinamentos(
        patrimonios: List<PatrimonioConsulta>
    ): List<String> {
        val sugestoes = mutableListOf<String>()
        
        if (patrimonios.isEmpty()) {
            sugestoes.add("Tente usar menos caracteres")
            sugestoes.add("Verifique a ortografia")
            sugestoes.add("Tente buscar por código ao invés de descrição")
        } else if (patrimonios.size > 30) {
            sugestoes.add("Muitos resultados. Tente ser mais específico")
            sugestoes.add("Use filtros de sala ou responsável")
        }
        
        // Sugerir filtros baseados nos resultados
        val salas = patrimonios.mapNotNull { it.salaNome }.distinct()
        if (salas.size > 1) {
            sugestoes.add("Filtrar por sala: ${salas.take(3).joinToString()}")
        }
        
        val responsaveis = patrimonios.mapNotNull { it.responsavelNome }.distinct()
        if (responsaveis.size > 1) {
            sugestoes.add("Filtrar por responsável: ${responsaveis.take(3).joinToString()}")
        }
        
        return sugestoes
    }
}
