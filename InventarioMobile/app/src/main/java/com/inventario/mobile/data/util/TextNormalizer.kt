package com.inventario.mobile.data.util

import java.text.Normalizer
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Normalizador de texto para uso em cache e filtro de sugestões de descrições.
 *
 * Aplica três passos na ordem:
 *  1. `Normalizer.Form.NFD` — decompõe caracteres acentuados em letra base + marca combinante.
 *  2. Remove marcas combinantes diacríticas via `\p{InCombiningDiacriticalMarks}+`.
 *  3. `lowercase(Locale.forLanguageTag("pt-BR"))` — normaliza a caixa segundo regras do
 *     português brasileiro.
 *
 * O resultado é uma string estável, acento-insensível e caixa-insensível, adequada para:
 *  - Persistir em `SugestaoDescricaoEntity.descricaoNormalizada` (pré-computado no upsert).
 *  - Filtrar sugestões offline no Room com `LIKE '%...%'`.
 *  - Espelhar o comportamento do servidor (`unaccent(lower(...))`) no caminho offline.
 *
 * A normalização é idempotente: `normalize(normalize(x)) == normalize(x)` para qualquer `x`.
 *
 * Exemplos:
 * ```
 * normalize("Cadeira Giratória")  // "cadeira giratoria"
 * normalize("ÁRVORE")              // "arvore"
 * normalize("Ônibus ESCOLAR")      // "onibus escolar"
 * normalize("sem acento")          // "sem acento"
 * normalize("")                    // ""
 * ```
 *
 * Requirements: 3.4, 7.2
 */
@Singleton
class TextNormalizer @Inject constructor() {

    /**
     * Normaliza a string de entrada removendo diacríticos e aplicando `lowercase` em pt-BR.
     *
     * @param input texto a ser normalizado (pode ser vazio).
     * @return texto normalizado, acento-insensível e em caixa baixa.
     */
    fun normalize(input: String): String {
        val nfd = Normalizer.normalize(input, Normalizer.Form.NFD)
        val stripped = nfd.replace(DIACRITIC_REGEX, "")
        return stripped.lowercase(PT_BR_LOCALE)
    }

    private companion object {
        private val DIACRITIC_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
        private val PT_BR_LOCALE: Locale = Locale.forLanguageTag("pt-BR")
    }
}
