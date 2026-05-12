package com.inventario.sihcp.dao;

/**
 * Objeto de transporte (row) entre {@code PatrimonioDAO} e a camada de service
 * para sugestões de descrições de patrimônios não coletados.
 *
 * <p>Este record é intencionalmente mantido na camada DAO ({@code sihcp-core})
 * e NÃO é exposto diretamente via HTTP. A camada de service (por exemplo,
 * {@code MobileSugestaoDescricaoService}) mapeia instâncias de
 * {@code SugestaoDescricaoRow} para o DTO de transporte público
 * ({@code SugestaoDescricaoDTO}) antes de retornar ao controller.</p>
 *
 * <p>A separação entre row e DTO permite evoluir o contrato HTTP
 * independentemente do shape retornado pelo SQL (por exemplo, adicionar
 * campos auxiliares no row sem quebrar clientes do endpoint).</p>
 *
 * <p>Corresponde às colunas projetadas pela consulta SQL descrita no design
 * da feature {@code coleta-descricao-livre-com-sugestao}:
 * {@code SELECT p.ID, p.NUMERO, p.DESCRICAO FROM TABELA_PATRIMONIO p ...}.</p>
 *
 * <p>Requirements: 5.6.</p>
 *
 * @param id         identificador do patrimônio (coluna {@code ID} da
 *                   {@code TABELA_PATRIMONIO})
 * @param numero     número do patrimônio (coluna {@code NUMERO})
 * @param descricao  descrição cadastrada do patrimônio (coluna {@code DESCRICAO})
 */
public record SugestaoDescricaoRow(
        Integer id,
        String numero,
        String descricao
) { }
