package com.inventario.mobile.domain.model

/**
 * Modelo de domínio para Sala com informações de progresso de coleta.
 * Usado para exibir salas com estatísticas de coleta na aba "Por Sala".
 * 
 * Regra: SEM dependências Android, apenas Kotlin puro
 * 
 * @property id Identificador único da sala
 * @property nome Nome da sala
 * @property numero Número/código da sala (opcional)
 * @property totalPatrimonios Total de patrimônios alocados na sala
 * @property coletados Quantidade de patrimônios já coletados
 * @property pendentes Quantidade de patrimônios ainda não coletados
 * @property percentualColeta Percentual de coleta (0-100)
 */
data class SalaComProgresso(
    val id: Int,
    val nome: String,
    val numero: String? = null,
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualColeta: Float
) {
    /**
     * Indica se a sala está com 100% de coleta
     */
    val isCompleta: Boolean
        get() = percentualColeta >= 100f
    
    /**
     * Indica se a sala não possui patrimônios
     */
    val isVazia: Boolean
        get() = totalPatrimonios == 0
    
    /**
     * Indica se a sala possui patrimônios pendentes de coleta
     */
    val temPendentes: Boolean
        get() = pendentes > 0
    
    /**
     * Retorna texto formatado do progresso (ex: "15/20 coletados")
     */
    val progressoTexto: String
        get() = "$coletados/$totalPatrimonios coletados"
    
    /**
     * Retorna texto formatado do percentual (ex: "75%")
     */
    val percentualTexto: String
        get() = "${percentualColeta.toInt()}%"
    
    /**
     * Retorna o nome da sala com o número, se disponível
     */
    val nomeCompleto: String
        get() = if (numero.isNullOrBlank()) nome else "$nome ($numero)"
    
    companion object {
        /**
         * Cria uma instância de SalaComProgresso calculando pendentes e percentual
         */
        fun criar(
            id: Int,
            nome: String,
            numero: String? = null,
            totalPatrimonios: Int,
            coletados: Int
        ): SalaComProgresso {
            val pendentes = totalPatrimonios - coletados
            val percentual = if (totalPatrimonios > 0) {
                (coletados.toFloat() / totalPatrimonios) * 100f
            } else {
                0f
            }
            
            return SalaComProgresso(
                id = id,
                nome = nome,
                numero = numero,
                totalPatrimonios = totalPatrimonios,
                coletados = coletados,
                pendentes = pendentes,
                percentualColeta = percentual
            )
        }
    }
}
