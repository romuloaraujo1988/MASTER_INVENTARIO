package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.PatrimonioConsulta
import com.inventario.mobile.domain.repository.PatrimonioConsultaRepository
import javax.inject.Inject

/**
 * Use Case: Buscar patrimônios por código parcial
 * 
 * Regra: Contém APENAS lógica de negócio
 * Sem dependências Android
 */
class BuscarPatrimonioPorCodigoUseCase @Inject constructor(
    private val repository: PatrimonioConsultaRepository
) {
    /**
     * Executa o caso de uso
     * 
     * @param codigo Código parcial do patrimônio
     * @param limit Quantidade máxima de resultados (padrão: 10)
     * @return Result com lista de patrimônios ou erro
     */
    suspend operator fun invoke(
        codigo: String,
        limit: Int = 10
    ): Result<List<PatrimonioConsulta>> {
        return try {
            // Validações de negócio
            if (codigo.isBlank()) {
                return Result.failure(Exception("Código não pode estar vazio"))
            }
            
            if (codigo.length < 2) {
                return Result.failure(Exception("Digite ao menos 2 caracteres do código"))
            }
            
            if (limit <= 0) {
                return Result.failure(Exception("Limite deve ser maior que zero"))
            }
            
            if (limit > 50) {
                return Result.failure(Exception("Limite não pode ser maior que 50"))
            }
            
            // Sanitizar entrada
            val codigoLimpo = codigo.trim().uppercase()
            
            // Validar se contém apenas caracteres válidos para código
            if (!codigoLimpo.matches(Regex("[A-Z0-9]+"))) {
                return Result.failure(Exception("Código deve conter apenas letras e números"))
            }
            
            // Buscar no repository
            val result = repository.buscarPorCodigoParcial(codigoLimpo, limit)
            
            // Aplicar regras de negócio adicionais
            result.map { patrimonios ->
                // Ordenar por relevância (códigos que começam com o termo primeiro)
                patrimonios.sortedWith { p1, p2 ->
                    when {
                        p1.codigo.startsWith(codigoLimpo) && !p2.codigo.startsWith(codigoLimpo) -> -1
                        !p1.codigo.startsWith(codigoLimpo) && p2.codigo.startsWith(codigoLimpo) -> 1
                        else -> p1.codigo.compareTo(p2.codigo)
                    }
                }
            }
            
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao buscar patrimônios por código: ${e.message}", e))
        }
    }
    
    /**
     * Valida se o código tem formato válido
     */
    fun isCodigoValido(codigo: String): Boolean {
        return codigo.isNotBlank() && 
               codigo.length >= 2 && 
               codigo.trim().matches(Regex("[A-Za-z0-9]+"))
    }
    
    /**
     * Sanitiza o código de entrada
     */
    fun sanitizarCodigo(codigo: String): String {
        return codigo.trim().uppercase().replace(Regex("[^A-Z0-9]"), "")
    }
}
