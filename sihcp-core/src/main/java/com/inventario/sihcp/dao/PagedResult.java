package com.inventario.sihcp.dao;

import java.util.List;

/**
 * Resultado paginado genérico retornado pela camada DAO para a camada de service.
 *
 * <p>Representa uma "página" de itens do tipo {@code T} acompanhada dos metadados
 * mínimos necessários para a camada de service calcular {@code totalPages} e
 * {@code hasNext} do DTO HTTP correspondente ({@code PagedResponseDTO}).</p>
 *
 * <p>Este record vive em {@code sihcp-core/.../dao/} por ser um objeto de
 * transporte estritamente interno, consumido somente pelos services — nunca
 * exposto diretamente em respostas HTTP. Isso evita acoplamento entre o shape
 * retornado pelo SQL paginado e o contrato público da API.</p>
 *
 * <p>Invariantes esperadas (não validadas no construtor para manter o record
 * como simples transporte):</p>
 * <ul>
 *   <li>{@code items != null} (use {@link List#of()} para páginas vazias);</li>
 *   <li>{@code totalElements >= 0};</li>
 *   <li>{@code page >= 0};</li>
 *   <li>{@code size >= 1}.</li>
 * </ul>
 *
 * <p>Requirements: 5.6.</p>
 *
 * @param <T>            tipo de cada item da página (por exemplo,
 *                       {@link SugestaoDescricaoRow})
 * @param items          itens da página corrente; nunca {@code null}
 * @param totalElements  total de elementos considerando todos os filtros (não
 *                       apenas a página corrente)
 * @param page           índice da página corrente, iniciando em {@code 0}
 * @param size           tamanho máximo da página (número máximo de itens em
 *                       {@code items})
 */
public record PagedResult<T>(
        List<T> items,
        long totalElements,
        int page,
        int size
) { }
