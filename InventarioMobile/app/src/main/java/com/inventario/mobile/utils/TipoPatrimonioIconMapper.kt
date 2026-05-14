package com.inventario.mobile.utils

import com.inventario.mobile.R

/**
 * Mapeia a descrição de um patrimônio (ou categoria) para um ícone visual
 * representativo do tipo de item.
 *
 * Usado no histórico de scans para dar identificação rápida do tipo de
 * patrimônio (monitor, cadeira, impressora etc.) sem precisar ler a
 * descrição completa.
 *
 * A função de match é resiliente:
 *  - Normaliza texto (remove acentos, lowercase)
 *  - Faz match por palavras-chave (contains)
 *  - Retorna um ícone genérico de patrimônio quando nenhum match é achado
 *
 * Como adicionar novos tipos:
 *  1. Coloque o drawable em `res/drawable/ic_tipo_xxx.xml`
 *  2. Adicione a entrada em [MAPEAMENTOS] com as palavras-chave mais comuns
 *     (primeiro match ganha — itens mais específicos devem vir antes)
 */
object TipoPatrimonioIconMapper {

    /**
     * Lista ordenada de (palavras-chave → ícone).
     * A ordem importa: entradas mais específicas vêm primeiro para evitar
     * que "monitor" seja capturado antes de "ar condicionado", por exemplo.
     */
    private val MAPEAMENTOS: List<Pair<List<String>, Int>> = listOf(
        // Informática — notebook vem antes de "computador" para não ser
        // capturado pela regra genérica.
        listOf("notebook", "laptop", "ultrabook") to R.drawable.ic_tipo_notebook,
        listOf("monitor", "tela", "display") to R.drawable.ic_tipo_monitor,
        listOf("computador", "cpu", "desktop", "pc ") to R.drawable.ic_tipo_computador,
        listOf("impressora", "multifuncional", "scanner") to R.drawable.ic_tipo_impressora,
        listOf("projetor", "datashow", "data show") to R.drawable.ic_tipo_projetor,
        listOf("televisao", "televisor", "tv ", "smart tv") to R.drawable.ic_tipo_tv,

        // Mobiliário
        listOf("cadeira", "poltrona", "banco giratorio") to R.drawable.ic_tipo_cadeira,
        listOf("mesa", "escrivaninha", "bancada", "birô", "biro") to R.drawable.ic_tipo_mesa,
        listOf("armario", "arquivo", "estante", "gaveteiro") to R.drawable.ic_tipo_armario,

        // Climatização
        listOf("ar condicionado", "climatizador", "ar-condicionado", "split")
            to R.drawable.ic_tipo_ar_condicionado,

        // Acervo / biblioteca
        listOf("livro", "dicionario", "enciclopedia", "periodico", "revista")
            to R.drawable.ic_tipo_livro
    )

    /**
     * Retorna o resource id do ícone apropriado para a descrição informada.
     * Nunca retorna 0 — se nada casar, devolve [R.drawable.ic_tipo_generico].
     */
    fun iconParaDescricao(descricao: String?): Int {
        if (descricao.isNullOrBlank()) return R.drawable.ic_tipo_generico

        val normalizada = normalizar(descricao)

        for ((palavrasChave, iconRes) in MAPEAMENTOS) {
            if (palavrasChave.any { palavra -> normalizada.contains(palavra) }) {
                return iconRes
            }
        }

        return R.drawable.ic_tipo_generico
    }

    /**
     * Normaliza texto para comparação: lowercase + remoção de acentos.
     * Ex.: "Televisão 55\"" → "televisao 55\""
     */
    private fun normalizar(texto: String): String {
        val semAcentos = java.text.Normalizer
            .normalize(texto, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return semAcentos.lowercase(java.util.Locale("pt", "BR"))
    }
}
