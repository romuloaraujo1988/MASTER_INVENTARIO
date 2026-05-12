package com.inventario.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.inventario.mobile.data.local.entity.SugestaoDescricaoEntity

/**
 * DAO para a tabela `sugestao_descricao` — cache offline das sugestões de
 * descrição de patrimônios não coletados do inventário ativo.
 *
 * Feature: coleta-descricao-livre-com-sugestao
 * Requirements: 7.2 (filtro acento/caso-insensível), 8.3 (atualização ≤300ms
 *               após marcar coletado), 8.4 (múltiplos patrimônios por descrição).
 *
 * Observações:
 * - O filtro acento/caso-insensível é feito sobre a coluna derivada
 *   `descricaoNormalizada`, pré-computada via `TextNormalizer` no momento do
 *   upsert. A camada de serviço/repositório deve normalizar o termo antes de
 *   chamar [buscarFiltrado].
 * - A ordenação `descricaoNormalizada ASC, numeroPatrimonio ASC` espelha a
 *   ordenação estável do servidor (Req 6.4) e é determinística entre páginas.
 * - [upsertAll] usa `OnConflictStrategy.REPLACE` porque a chave composta
 *   `(idInventario, idPatrimonio)` garante idempotência do cache.
 */
@Dao
interface SugestaoDescricaoDao {

    /**
     * Busca sugestões ainda não coletadas filtradas por termo normalizado.
     *
     * @param idInventario      ID do inventário ativo.
     * @param termoNormalizado  Termo de busca já normalizado (NFD + sem
     *                          diacríticos + minúsculas). String vazia
     *                          retorna todas as sugestões não coletadas.
     * @param limit             Quantidade máxima de itens a retornar.
     */
    @Query(
        """
        SELECT * FROM sugestao_descricao
        WHERE idInventario = :idInventario
          AND coletadoLocal = 0
          AND (:termoNormalizado = '' OR descricaoNormalizada LIKE '%' || :termoNormalizado || '%')
        ORDER BY descricaoNormalizada ASC, numeroPatrimonio ASC
        LIMIT :limit
        """
    )
    suspend fun buscarFiltrado(
        idInventario: Int,
        termoNormalizado: String,
        limit: Int
    ): List<SugestaoDescricaoEntity>

    /**
     * Insere ou substitui o conjunto de sugestões recebidas do servidor.
     * A chave composta `(idInventario, idPatrimonio)` garante idempotência
     * sem duplicatas quando a mesma descrição aparece em múltiplos patrimônios.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<SugestaoDescricaoEntity>)

    /**
     * Marca uma sugestão como coletada localmente, removendo-a do resultado
     * de [buscarFiltrado] sem depender da sincronização (Req 8.1).
     */
    @Query(
        """
        UPDATE sugestao_descricao
        SET coletadoLocal = 1
        WHERE idInventario = :idInventario AND idPatrimonio = :idPatrimonio
        """
    )
    suspend fun marcarColetado(idInventario: Int, idPatrimonio: Int)

    /**
     * Remove do cache sugestões de qualquer inventário diferente do ativo,
     * garantindo isolamento por inventário (Req 7.1).
     */
    @Query("DELETE FROM sugestao_descricao WHERE idInventario != :idInventarioAtivo")
    suspend fun limparOutrosInventarios(idInventarioAtivo: Int)

    /**
     * Retorna a quantidade de sugestões ainda não coletadas para o inventário
     * informado. Útil para decidir se o cache está populado antes de usá-lo
     * como fallback offline (Req 7.2).
     */
    @Query(
        """
        SELECT COUNT(*) FROM sugestao_descricao
        WHERE idInventario = :idInventario AND coletadoLocal = 0
        """
    )
    suspend fun contarPorInventario(idInventario: Int): Int
}
