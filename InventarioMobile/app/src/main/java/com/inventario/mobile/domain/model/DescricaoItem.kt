package com.inventario.mobile.domain.model

/**
 * Modelo de domínio para item de descrição na lista
 * Contém informações para exibição mais rica
 */
data class DescricaoItem(
    val descricao: String,
    val quantidade: Int = 0,
    val categoria: String = "",
    val icone: String = "📦"
) {
    companion object {
        /**
         * Detecta a categoria e ícone baseado na descrição
         */
        fun fromDescricao(descricao: String, quantidade: Int = 0): DescricaoItem {
            val descricaoLower = descricao.lowercase()
            
            val (categoria, icone) = when {
                // Cadeiras
                descricaoLower.contains("cadeira") -> "Cadeiras" to "🪑"
                descricaoLower.contains("poltrona") -> "Cadeiras" to "🪑"
                descricaoLower.contains("banco") && !descricaoLower.contains("armario") -> "Cadeiras" to "🪑"
                
                // Mesas
                descricaoLower.contains("mesa") -> "Mesas" to "🪑"
                descricaoLower.contains("escrivaninha") -> "Mesas" to "🪑"
                descricaoLower.contains("bancada") -> "Mesas" to "🪑"
                
                // Computadores e Eletrônicos
                descricaoLower.contains("computador") -> "Computadores" to "💻"
                descricaoLower.contains("notebook") -> "Computadores" to "💻"
                descricaoLower.contains("monitor") -> "Computadores" to "🖥️"
                descricaoLower.contains("impressora") -> "Computadores" to "🖨️"
                descricaoLower.contains("projetor") -> "Computadores" to "📽️"
                descricaoLower.contains("datashow") -> "Computadores" to "📽️"
                descricaoLower.contains("teclado") -> "Computadores" to "⌨️"
                descricaoLower.contains("mouse") -> "Computadores" to "🖱️"
                
                // Armários e Estantes
                descricaoLower.contains("armario") || descricaoLower.contains("armário") -> "Armários" to "🗄️"
                descricaoLower.contains("estante") -> "Armários" to "🗄️"
                descricaoLower.contains("arquivo") -> "Armários" to "🗄️"
                descricaoLower.contains("gaveteiro") -> "Armários" to "🗄️"
                descricaoLower.contains("prateleira") -> "Armários" to "🗄️"
                
                // Ar Condicionado
                descricaoLower.contains("ar condicionado") || descricaoLower.contains("ar-condicionado") -> "Climatização" to "❄️"
                descricaoLower.contains("ventilador") -> "Climatização" to "🌀"
                
                // Telefonia
                descricaoLower.contains("telefone") -> "Telefonia" to "📞"
                descricaoLower.contains("aparelho telefonico") -> "Telefonia" to "📞"
                
                // Quadros
                descricaoLower.contains("quadro") -> "Quadros" to "📋"
                descricaoLower.contains("lousa") -> "Quadros" to "📋"
                
                // Veículos
                descricaoLower.contains("veiculo") || descricaoLower.contains("veículo") -> "Veículos" to "🚗"
                descricaoLower.contains("carro") -> "Veículos" to "🚗"
                descricaoLower.contains("moto") -> "Veículos" to "🏍️"
                
                // Outros
                else -> "Outros" to "📦"
            }
            
            return DescricaoItem(
                descricao = descricao,
                quantidade = quantidade,
                categoria = categoria,
                icone = icone
            )
        }
    }
}
