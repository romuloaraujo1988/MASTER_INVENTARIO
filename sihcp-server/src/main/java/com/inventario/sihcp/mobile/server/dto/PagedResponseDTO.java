package com.inventario.sihcp.mobile.server.dto;

import java.util.List;

/**
 * DTO de resposta paginada dedicado ao endpoint
 * {@code GET /api/mobile/descricoes/sugestoes}.
 *
 * <p>Diferencia-se de {@link PagedResponse} por ser um {@code record} Java 21
 * com um contrato reduzido e explícito ao app mobile, incluindo o indicador
 * {@link #semInventarioAtivo()} usado quando não há inventário ativo no
 * momento da requisição (Req 5.8).</p>
 *
 * <p>Campos de metadados (Req 5.5, 5.6):</p>
 * <ul>
 *   <li>{@code content}        — itens da página atual (nunca {@code null})</li>
 *   <li>{@code page}           — número da página solicitada (0-based)</li>
 *   <li>{@code size}           — tamanho efetivo aplicado (após sanitização)</li>
 *   <li>{@code totalElements}  — total de elementos em todas as páginas</li>
 *   <li>{@code totalPages}     — {@code ceil(totalElements / size)}</li>
 *   <li>{@code hasNext}        — {@code (page + 1) * size < totalElements}</li>
 *   <li>{@code semInventarioAtivo} — {@code true} quando não existe inventário
 *       ativo; nesse caso {@code content} é vazio e os demais metadados são
 *       zerados (Req 5.8)</li>
 * </ul>
 *
 * <p>Requirements: 5.5, 5.6, 5.8.</p>
 *
 * @param <T> tipo dos elementos em {@code content}
 * @param content             itens da página atual
 * @param page                número da página (0-based)
 * @param size                tamanho efetivo da página
 * @param totalElements       total de elementos
 * @param totalPages          total de páginas
 * @param hasNext             indica se há próxima página
 * @param semInventarioAtivo  indica ausência de inventário ativo (Req 5.8)
 */
public record PagedResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean semInventarioAtivo
) {

    /**
     * Fábrica para o caso {@code semInventarioAtivo=true} (Req 5.8).
     *
     * <p>Retorna uma resposta com {@code content} vazio e todos os metadados
     * de paginação zerados. O flag {@link #semInventarioAtivo()} permite ao
     * app diferenciar "nenhum resultado encontrado" de "não há inventário
     * ativo para consultar".</p>
     *
     * @param <T> tipo dos elementos
     * @return resposta vazia com {@code semInventarioAtivo=true}
     */
    public static <T> PagedResponseDTO<T> empty() {
        return new PagedResponseDTO<>(List.of(), 0, 0, 0L, 0, false, true);
    }

    /**
     * Variante explícita da fábrica vazia.
     *
     * <p>Utilidade principal: permitir a construção de respostas vazias
     * tanto no cenário "sem inventário ativo" ({@code semInventarioAtivo=true},
     * Req 5.8) quanto em cenários em que simplesmente não há resultados
     * com inventário ativo presente ({@code semInventarioAtivo=false}).</p>
     *
     * @param semInventarioAtivo valor do flag de ausência de inventário ativo
     * @param <T> tipo dos elementos
     * @return resposta vazia com o flag informado
     */
    public static <T> PagedResponseDTO<T> empty(boolean semInventarioAtivo) {
        return new PagedResponseDTO<>(List.of(), 0, 0, 0L, 0, false, semInventarioAtivo);
    }
}
