package com.inventario.sihcp.mobile.server.dto;

/**
 * DTO de sugestão de descrição de patrimônio não coletado.
 *
 * <p>Representa uma única linha retornada pelo endpoint
 * {@code GET /api/mobile/descricoes/sugestoes}, com os dados mínimos
 * necessários para o app associar a sugestão a um patrimônio específico
 * (indispensável para marcar o patrimônio como coletado no cache local
 * após a coleta — Req 8.4).</p>
 *
 * <p>Implementado como {@code record} Java 21. Jackson serializa/deserializa
 * records via os accessors sintéticos, resultando em um JSON com os nomes
 * de campo {@code idPatrimonio}, {@code numeroPatrimonio} e {@code descricao}.</p>
 *
 * <p>Requirements: 5.1, 8.4.</p>
 *
 * @param idPatrimonio      identificador do patrimônio associado à sugestão
 * @param numeroPatrimonio  número do patrimônio (ex.: {@code IFMT-01234})
 * @param descricao         descrição cadastrada do patrimônio
 */
public record SugestaoDescricaoDTO(
        Integer idPatrimonio,
        String numeroPatrimonio,
        String descricao
) { }
