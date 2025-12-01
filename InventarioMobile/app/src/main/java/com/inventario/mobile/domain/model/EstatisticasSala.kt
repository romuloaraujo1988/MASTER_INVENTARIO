package com.inventario.mobile.domain.model

/**
 * Modelo de domínio para estatísticas detalhadas de uma sala.
 * Usado para exibir o card de estatísticas quando uma sala é selecionada.
 * 
 * Regra: SEM dependências Android, apenas Kotlin puro
 * 
 * @property salaId Identificador único da sala
 * @property salaNome Nome da sala
 * @property totalPatrimonios Total de patrimônios alocados na sala
 * @property coletados Quantidade de patrimônios já coletados
 * @property pendentes Quantidade de patrimônios ainda não coletados
 * @property percentualColeta Percentual de coleta (0-100)
 * @property coletadosHoje Quantidade de patrimônios coletados hoje
 * @property coletadosSemana Quantidade de patrimônios coletados na última semana
 */
data class EstatisticasSala(
    val salaId: Int,
    val salaNome: String,
    val totalPatrimonios: Int,
    val coletados: Int,
    val pendentes: Int,
    val percentualColeta: Float,
    val coletadosHoje: Int = 0,
    val coletadosSemana: Int = 0
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
     * Retorna a faixa de progresso para mapeamento de cores
     * 0 = 0-25% (vermelho)
     * 1 = 26-50% (laranja)
     * 2 = 51-75% (amarelo)
     * 3 = 76-100% (verde)
     */
    val faixaProgresso: Int
        get() = when {
            percentualColeta <= 25f -> 0
            percentualColeta <= 50f -> 1
            percentualColeta <= 75f -> 2
            else -> 3
        }
    
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
     * Retorna texto formatado das coletas de hoje
     */
    val coletadosHojeTexto: String
        get() = "Hoje: $coletadosHoje"
    
    /**
     * Retorna texto formatado das coletas da semana
     */
    val coletadosSemanaTexto: String
        get() = "Esta semana: $coletadosSemana"
    
    companion object {
        /**
         * Cria uma instância de EstatisticasSala calculando pendentes e percentual
         */
        fun criar(
            salaId: Int,
            salaNome: String,
            totalPatrimonios: Int,
            coletados: Int,
            coletadosHoje: Int = 0,
            coletadosSemana: Int = 0
        ): EstatisticasSala {
            val pendentes = totalPatrimonios - coletados
            val percentual = if (totalPatrimonios > 0) {
                (coletados.toFloat() / totalPatrimonios) * 100f
            } else {
                0f
            }
            
            return EstatisticasSala(
                salaId = salaId,
                salaNome = salaNome,
                totalPatrimonios = totalPatrimonios,
                coletados = coletados,
                pendentes = pendentes,
                percentualColeta = percentual,
                coletadosHoje = coletadosHoje,
                coletadosSemana = coletadosSemana
            )
        }
    }
}
