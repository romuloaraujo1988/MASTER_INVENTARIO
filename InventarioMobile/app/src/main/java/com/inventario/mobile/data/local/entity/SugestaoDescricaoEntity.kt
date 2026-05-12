package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * Entity Room para sugestões de descrições de patrimônios não coletados
 * do inventário ativo.
 *
 * Espelha o contrato do endpoint `GET /api/mobile/descricoes/sugestoes` e serve
 * como cache offline do Autocomplete de sugestões na tela de coleta de item
 * sem etiqueta.
 *
 * Feature: coleta-descricao-livre-com-sugestao
 * Requirements: 7.1 (cache offline), 7.2 (filtro acento/caso-insensível),
 *               8.1 (marcação local pós-coleta)
 *
 * Notas de modelagem:
 * - Chave composta `(idInventario, idPatrimonio)` isola o cache por inventário
 *   e evita duplicatas quando múltiplos patrimônios compartilham a mesma descrição.
 * - `descricaoNormalizada` é pré-computada via `TextNormalizer` (NFD + remoção
 *   de diacríticos + lowercase pt-BR) no momento do upsert, permitindo filtro
 *   offline idêntico ao do servidor sem custo em caminho quente do autocomplete.
 * - `coletadoLocal` é marcado imediatamente após a persistência local da coleta
 *   (Req 8.1), fazendo a sugestão sumir do autocomplete sem depender da sincronização.
 * - Room mapeia `Boolean` para INTEGER (0/1) em SQLite, compatível com as queries
 *   `coletadoLocal = 0` definidas no DAO.
 */
@Entity(
    tableName = "sugestao_descricao",
    primaryKeys = ["idInventario", "idPatrimonio"],
    indices = [
        Index(value = ["idInventario", "coletadoLocal"]),
        Index(value = ["idInventario", "descricaoNormalizada"]),
        Index(value = ["descricaoNormalizada"])
    ]
)
data class SugestaoDescricaoEntity(
    val idInventario: Int,
    val idPatrimonio: Int,
    val numeroPatrimonio: String,
    val descricao: String,
    /** Pré-computada via TextNormalizer no momento do upsert. Acento/caso-insensível. */
    val descricaoNormalizada: String,
    /** Marcado localmente após coleta (Req 8.1). Sincronização confirma (Req 8.2). */
    val coletadoLocal: Boolean = false,
    /** Timestamp da última atualização vinda do servidor. */
    val dataAtualizacao: Long = System.currentTimeMillis()
)
