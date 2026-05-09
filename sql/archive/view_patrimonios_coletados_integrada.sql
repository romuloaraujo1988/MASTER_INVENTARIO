-- ============================================================================
-- VIEW: Patrimônios Coletados (Integração Coleta Normal + Itens Compostos)
-- Data: 13/01/2026
-- Descrição: View que unifica a verificação de coleta considerando tanto
--            a tabela_coleta (coleta normal) quanto a tabela_coleta_componente
--            (coleta de itens compostos)
-- ============================================================================

-- Drop view se existir
DROP VIEW IF EXISTS vw_patrimonios_coletados_integrada;

-- Criar view integrada
CREATE OR REPLACE VIEW vw_patrimonios_coletados_integrada AS
SELECT 
    p.id AS id_patrimonio,
    p.numero AS numero_patrimonio,
    p.descricao AS descricao_patrimonio,
    p.status AS status_patrimonio,
    i.id AS id_inventario,
    i.nome AS nome_inventario,
    CASE 
        WHEN c.id IS NOT NULL THEN 'COLETA_NORMAL'
        WHEN cc_exists.id_patrimonio IS NOT NULL THEN 'COLETA_COMPONENTE'
        ELSE 'NAO_COLETADO'
    END AS tipo_coleta,
    CASE 
        WHEN c.id IS NOT NULL THEN TRUE
        WHEN cc_exists.id_patrimonio IS NOT NULL THEN TRUE
        ELSE FALSE
    END AS coletado,
    COALESCE(c.data_coleta, cc_exists.data_coleta) AS data_coleta,
    COALESCE(c.status_coleta, 'COLETADO') AS status_coleta
FROM tabela_patrimonio p
CROSS JOIN tabela_inventario i
LEFT JOIN tabela_coleta c ON p.id = c.id_patrimonio AND c.id_inventario = i.id
LEFT JOIN LATERAL (
    SELECT DISTINCT 
        ic.id_patrimonio_principal AS id_patrimonio,
        MIN(cc.data_coleta) AS data_coleta
    FROM tabela_item_composto ic
    INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
    WHERE ic.id_patrimonio_principal = p.id 
      AND cc.id_inventario = i.id
    GROUP BY ic.id_patrimonio_principal
) cc_exists ON TRUE
WHERE UPPER(p.status) IN ('ATIVO', 'PENDENTE');

-- Comentário na view
COMMENT ON VIEW vw_patrimonios_coletados_integrada IS 
'View integrada que mostra o status de coleta de patrimônios considerando tanto coletas normais quanto coletas de itens compostos';

-- ============================================================================
-- Exemplos de uso:
-- ============================================================================

-- 1. Listar todos os patrimônios coletados em um inventário (qualquer tipo)
-- SELECT * FROM vw_patrimonios_coletados_integrada 
-- WHERE id_inventario = 1 AND coletado = TRUE;

-- 2. Listar patrimônios NÃO coletados em um inventário
-- SELECT * FROM vw_patrimonios_coletados_integrada 
-- WHERE id_inventario = 1 AND coletado = FALSE;

-- 3. Contar por tipo de coleta
-- SELECT tipo_coleta, COUNT(*) 
-- FROM vw_patrimonios_coletados_integrada 
-- WHERE id_inventario = 1 
-- GROUP BY tipo_coleta;

-- 4. Estatísticas gerais
-- SELECT 
--     COUNT(*) AS total,
--     SUM(CASE WHEN coletado THEN 1 ELSE 0 END) AS coletados,
--     SUM(CASE WHEN NOT coletado THEN 1 ELSE 0 END) AS pendentes,
--     ROUND(SUM(CASE WHEN coletado THEN 1 ELSE 0 END)::DECIMAL / COUNT(*) * 100, 2) AS percentual
-- FROM vw_patrimonios_coletados_integrada 
-- WHERE id_inventario = 1;

-- ============================================================================
-- Índices recomendados para performance (se ainda não existirem)
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_coleta_componente_patrimonio_inventario 
ON tabela_coleta_componente(id_inventario);

CREATE INDEX IF NOT EXISTS idx_item_composto_patrimonio_principal 
ON tabela_item_composto(id_patrimonio_principal);

-- ============================================================================
-- Verificação após criação
-- ============================================================================
SELECT 'View vw_patrimonios_coletados_integrada criada com sucesso!' AS resultado;
