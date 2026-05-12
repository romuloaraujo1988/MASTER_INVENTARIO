package com.inventario.mobile.domain.model

/**
 * Contagens de fotos disponíveis para o relatório fotográfico de itens sem etiqueta.
 *
 * Mapeado a partir da resposta do endpoint
 * `GET api/mobile/relatorios/fotos/{inventarioId}/info`.
 *
 * Esta é uma entidade pura (sem dependências de Android ou Room),
 * pertencente à camada de domínio conforme Clean Architecture.
 *
 * @property inventarioId ID do inventário ao qual as contagens se referem.
 * @property totalFotos Total de fotos registradas no inventário.
 * @property semEtiqueta Quantidade de fotos de itens sem etiqueta (Requirement 1.2, 1.3).
 * @property patrimonio Quantidade de fotos de patrimônios identificados.
 * @property divergencia Quantidade de fotos de itens com divergência.
 * @property temFotos Indica se há ao menos uma foto disponível para gerar o relatório.
 */
data class RelatorioFotoInfo(
    val inventarioId: Int,
    val totalFotos: Int,
    val semEtiqueta: Int,
    val patrimonio: Int,
    val divergencia: Int,
    val temFotos: Boolean
)
