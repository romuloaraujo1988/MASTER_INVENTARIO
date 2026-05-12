package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.SugestaoDescricaoEntity
import com.inventario.mobile.data.remote.dto.SugestaoDescricaoDto
import com.inventario.mobile.data.util.TextNormalizer
import com.inventario.mobile.domain.model.SugestaoDescricao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper para conversões entre as representações de uma sugestão de descrição
 * de patrimônio não coletado.
 *
 * Orquestra a conversão entre:
 *  - DTO (contrato JSON de `GET /api/mobile/descricoes/sugestoes`)
 *  - Model de domínio (usado por Use Cases e ViewModel)
 *  - Entity Room (cache offline em `sugestao_descricao`)
 *
 * O [TextNormalizer] é recebido como parâmetro em [toEntity] — e não como
 * dependência de construtor — para que `ViewModel`/`Repository` o injete uma
 * única vez e reutilize entre várias conversões dentro de um mesmo upsert,
 * evitando múltiplas resoluções de dependência no caminho quente do autocomplete.
 *
 * Feature: coleta-descricao-livre-com-sugestao
 * Requirements: 7.1 (cache offline isolado por inventário)
 */
@Singleton
class SugestaoDescricaoMapper @Inject constructor() {

    /**
     * Converte o DTO recebido do servidor em model de domínio.
     *
     * Todos os campos do DTO são nulos por tolerância a payloads inesperados;
     * aqui aplicamos defaults seguros (0 para `idPatrimonio` e string vazia
     * para os demais) para blindar as camadas superiores contra NPE.
     *
     * @param dto objeto desserializado do corpo da resposta JSON.
     * @return instância de domínio equivalente.
     */
    fun toDomain(dto: SugestaoDescricaoDto): SugestaoDescricao {
        return SugestaoDescricao(
            idPatrimonio = dto.idPatrimonio ?: 0,
            numeroPatrimonio = dto.numeroPatrimonio ?: "",
            descricao = dto.descricao ?: ""
        )
    }

    /**
     * Converte um model de domínio em [SugestaoDescricaoEntity] pronto para
     * persistência via `SugestaoDescricaoDao.upsertAll`.
     *
     * Pré-computa [SugestaoDescricaoEntity.descricaoNormalizada] aplicando o
     * [normalizer] recebido (NFD + remoção de diacríticos + `lowercase` pt-BR),
     * permitindo filtros offline acento/caso-insensíveis sem custo de CPU no
     * caminho quente do autocomplete (Req 7.2).
     *
     * @param domain sugestão vinda do servidor ou reconstruída a partir do cache.
     * @param idInventario identificador do inventário ativo — compõe a PK
     *                     `(idInventario, idPatrimonio)` para isolamento por inventário.
     * @param normalizer normalizador de texto já injetado pela camada chamadora;
     *                   passado como parâmetro para reaproveitamento em batch.
     * @return entity com `coletadoLocal = false` e `dataAtualizacao = now`.
     */
    fun toEntity(
        domain: SugestaoDescricao,
        idInventario: Int,
        normalizer: TextNormalizer
    ): SugestaoDescricaoEntity {
        return SugestaoDescricaoEntity(
            idInventario = idInventario,
            idPatrimonio = domain.idPatrimonio,
            numeroPatrimonio = domain.numeroPatrimonio,
            descricao = domain.descricao,
            descricaoNormalizada = normalizer.normalize(domain.descricao),
            coletadoLocal = false,
            dataAtualizacao = System.currentTimeMillis()
        )
    }

    /**
     * Converte a entity Room (cache offline) de volta para o model de domínio.
     *
     * Usado no fallback offline quando a chamada ao servidor falha e o
     * repositório devolve registros lidos de `SugestaoDescricaoDao.buscarFiltrado`.
     * Os campos `descricaoNormalizada`, `coletadoLocal` e `dataAtualizacao` são
     * detalhes de persistência e não pertencem ao domínio, portanto não são
     * propagados.
     *
     * @param entity registro persistido no Room.
     * @return sugestão de domínio equivalente.
     */
    fun toDomain(entity: SugestaoDescricaoEntity): SugestaoDescricao {
        return SugestaoDescricao(
            idPatrimonio = entity.idPatrimonio,
            numeroPatrimonio = entity.numeroPatrimonio,
            descricao = entity.descricao
        )
    }
}
