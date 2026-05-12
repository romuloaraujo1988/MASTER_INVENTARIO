package com.inventario.mobile.domain.model

/**
 * Resultado consolidado de uma busca de sugestões de descrição para a
 * tela de coleta de item sem etiqueta.
 *
 * Agrega a lista de sugestões, a origem dos dados (servidor, cache ou
 * ausência) e os metadados de paginação necessários para o carregamento
 * incremental da lista no autocomplete.
 *
 * Entidade pura, pertencente à camada de domínio conforme Clean
 * Architecture, sem dependências de Android ou frameworks externos.
 *
 * @property sugestoes Lista de sugestões retornadas para exibição. Pode
 *                     estar vazia quando o servidor respondeu sem
 *                     correspondências ou quando o cache está vazio
 *                     em cenário offline (Requirements 3.6, 7.3).
 * @property origem Origem dos dados agregados nesta resposta. Permite que
 *                  a camada de apresentação exiba feedback apropriado
 *                  (por exemplo, aviso de uso de cache — Requirement 3.2).
 * @property totalElements Total de elementos disponíveis no universo
 *                         filtrado pelo termo de busca, conforme reportado
 *                         pela origem dos dados. Usado para indicadores de
 *                         progresso e para decidir sobre paginação
 *                         adicional (Requirements 5.5, 5.6).
 * @property hasNext `true` quando existem páginas adicionais a serem
 *                   carregadas além do conteúdo já presente em [sugestoes].
 *                   Em respostas vindas do cache o valor é sempre `false`,
 *                   pois o cache offline não é paginado (Requirement 7.2).
 */
data class ResultadoSugestoes(
    val sugestoes: List<SugestaoDescricao>,
    val origem: OrigemSugestoes,
    val totalElements: Long,
    val hasNext: Boolean
)
