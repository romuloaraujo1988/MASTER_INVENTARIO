package com.inventario.mobile.domain.validator

import com.inventario.mobile.domain.model.Coleta

/**
 * Validador de dados críticos de coleta
 * Garante que nenhuma coleta seja salva com dados inválidos
 * 
 * CRÍTICO: Com 10.000+ patrimônios, dados inválidos são CATASTRÓFICOS
 */
object ColetaValidator {
    
    /**
     * Valida todos os campos críticos de uma coleta
     * @return Result.success se válido, Result.failure com mensagem de erro se inválido
     */
    fun validar(coleta: Coleta): Result<Unit> {
        // Verificar se é coleta por descrição (sem número de patrimônio)
        val isColetaPorDescricao = !coleta.descricaoPatrimonio.isNullOrBlank() && 
                                   coleta.numeroPatrimonio.isNullOrBlank()
        
        // 1. Validar número do patrimônio OU descrição (um dos dois é obrigatório)
        if (coleta.numeroPatrimonio.isNullOrBlank() && coleta.descricaoPatrimonio.isNullOrBlank()) {
            return Result.failure(ValidationException(
                campo = "numeroPatrimonio",
                mensagem = "❌ Número do patrimônio ou descrição é obrigatório",
                codigoErro = "NUMERO_PATRIMONIO_VAZIO"
            ))
        }
        
        // 2. Validar ID do patrimônio (não obrigatório para coleta por descrição)
        if (!isColetaPorDescricao && coleta.patrimonioId <= 0) {
            return Result.failure(ValidationException(
                campo = "patrimonioId",
                mensagem = "❌ ID do patrimônio inválido: ${coleta.patrimonioId}",
                codigoErro = "PATRIMONIO_ID_INVALIDO"
            ))
        }
        
        // 3. Validar usuário (QUEM coletou)
        if (coleta.usuarioId <= 0) {
            return Result.failure(ValidationException(
                campo = "usuarioId",
                mensagem = "❌ Usuário não identificado",
                codigoErro = "USUARIO_NAO_IDENTIFICADO"
            ))
        }
        
        // 4. Validar data de coleta (QUANDO foi coletado)
        if (coleta.dataColeta <= 0) {
            return Result.failure(ValidationException(
                campo = "dataColeta",
                mensagem = "❌ Data de coleta inválida",
                codigoErro = "DATA_COLETA_INVALIDA"
            ))
        }
        
        // Data não pode ser no futuro
        if (coleta.dataColeta > System.currentTimeMillis()) {
            return Result.failure(ValidationException(
                campo = "dataColeta",
                mensagem = "❌ Data de coleta não pode ser no futuro",
                codigoErro = "DATA_COLETA_FUTURA"
            ))
        }
        
        // Data não pode ser muito antiga (mais de 1 ano)
        val umAnoAtras = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        if (coleta.dataColeta < umAnoAtras) {
            return Result.failure(ValidationException(
                campo = "dataColeta",
                mensagem = "⚠️ Data de coleta muito antiga (mais de 1 ano)",
                codigoErro = "DATA_COLETA_ANTIGA"
            ))
        }
        
        // 5. Validar inventário (opcional - será preenchido pelo mapper)
        // O inventário é obtido do PreferencesManager no momento do salvamento
        
        // 6. Validar coordenadas GPS (se fornecidas)
        if (coleta.latitude != null) {
            if (coleta.latitude < -90 || coleta.latitude > 90) {
                return Result.failure(ValidationException(
                    campo = "latitude",
                    mensagem = "❌ Latitude inválida: ${coleta.latitude}",
                    codigoErro = "LATITUDE_INVALIDA"
                ))
            }
        }
        
        if (coleta.longitude != null) {
            if (coleta.longitude < -180 || coleta.longitude > 180) {
                return Result.failure(ValidationException(
                    campo = "longitude",
                    mensagem = "❌ Longitude inválida: ${coleta.longitude}",
                    codigoErro = "LONGITUDE_INVALIDA"
                ))
            }
        }
        
        // ✅ Todos os campos críticos são válidos
        return Result.success(Unit)
    }
    
    /**
     * Valida apenas campos essenciais (validação rápida)
     */
    fun validarEssencial(coleta: Coleta): Boolean {
        // Coleta por descrição: descricaoPatrimonio preenchida, numeroPatrimonio pode ser vazio
        val temIdentificacao = !coleta.numeroPatrimonio.isNullOrBlank() || 
                               !coleta.descricaoPatrimonio.isNullOrBlank()
        
        // Para coleta por descrição, patrimonioId pode ser 0
        val isColetaPorDescricao = !coleta.descricaoPatrimonio.isNullOrBlank() && 
                                   coleta.numeroPatrimonio.isNullOrBlank()
        val patrimonioIdValido = isColetaPorDescricao || coleta.patrimonioId > 0
        
        return temIdentificacao &&
               patrimonioIdValido &&
               coleta.usuarioId > 0 &&
               coleta.dataColeta > 0
    }
}

/**
 * Exception customizada para erros de validação
 */
class ValidationException(
    val campo: String,
    mensagem: String,
    val codigoErro: String
) : Exception("[$codigoErro] $campo: $mensagem")
