-- ============================================================
-- RELATÓRIOS DE ITENS COMPOSTOS - VIEWS E QUERIES
-- ============================================================

-- 1. Adicionar coluna de localização (se não existir)
ALTER TABLE tabela_coleta_componente 
ADD COLUMN IF NOT EXISTS localizacao_encontrada VARCHAR(255);

-- ============================================================
-- VIEW 1: RESUMO GERAL DE ITENS COMPOSTOS
-- ============================================================
CREATE OR REPLACE VIEW vw_resumo_itens_compostos AS
SELECT 
    p.numero AS numero_patrimonio,
    p.descricao AS descricao_patrimonio,
    s.descricao AS sala,
    r.nome AS responsavel,
    COUNT(ic.id) AS total_componentes,
    SUM(ic.quantidade_esperada) AS qtd_total_esperada,
    COALESCE(SUM(cc.quantidade_encontrada), 0) AS qtd_total_encontrada,
    CASE 
        WHEN COUNT(cc.id) = 0 THEN 'PENDENTE'
        WHEN SUM(cc.quantidade_encontrada) >= SUM(ic.quantidade_esperada) THEN 'COMPLETO'
        WHEN SUM(cc.quantidade_encontrada) > 0 THEN 'PARCIAL'
        ELSE 'PENDENTE'
    END AS status_coleta
FROM tabela_patrimonio p
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_sala s ON p.id_sala = s.id
LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
GROUP BY p.id, p.numero, p.descricao, s.descricao, r.nome
ORDER BY p.numero;

-- ============================================================
-- VIEW 2: DETALHES DE COMPONENTES POR PATRIMÔNIO
-- ============================================================
CREATE OR REPLACE VIEW vw_detalhes_componentes AS
SELECT 
    p.numero AS numero_patrimonio,
    p.descricao AS descricao_patrimonio,
    ic.tipo_componente,
    ic.descricao_componente,
    ic.quantidade_esperada,
    COALESCE(cc.quantidade_encontrada, 0) AS quantidade_encontrada,
    cc.localizacao_encontrada,
    cc.status_componente,
    cc.observacao_coleta,
    cc.data_coleta,
    u.nome_completo AS coletor,
    inv.nome AS inventario
FROM tabela_patrimonio p
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
LEFT JOIN tabela_usuario u ON cc.id_coletor = u.id
LEFT JOIN tabela_inventario inv ON cc.id_inventario = inv.id
ORDER BY p.numero, ic.tipo_componente;

-- ============================================================
-- VIEW 3: ESTATÍSTICAS POR INVENTÁRIO
-- ============================================================
CREATE OR REPLACE VIEW vw_estatisticas_itens_compostos AS
SELECT 
    inv.nome AS inventario,
    COUNT(DISTINCT ic.id_patrimonio_principal) AS total_patrimonios,
    COUNT(ic.id) AS total_componentes,
    COUNT(cc.id) AS componentes_coletados,
    COUNT(ic.id) - COUNT(cc.id) AS componentes_pendentes,
    ROUND(
        (COUNT(cc.id)::DECIMAL / NULLIF(COUNT(ic.id), 0)) * 100, 2
    ) AS percentual_coleta
FROM tabela_inventario inv
CROSS JOIN tabela_item_composto ic
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = inv.id
WHERE inv.status = 'EM_ANDAMENTO'
GROUP BY inv.id, inv.nome;

-- ============================================================
-- VIEW 4: COMPONENTES FALTANTES (NÃO COLETADOS)
-- ============================================================
CREATE OR REPLACE VIEW vw_componentes_faltantes AS
SELECT 
    p.numero AS numero_patrimonio,
    p.descricao AS descricao_patrimonio,
    s.descricao AS sala,
    ic.tipo_componente,
    ic.descricao_componente,
    ic.quantidade_esperada
FROM tabela_patrimonio p
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_sala s ON p.id_sala = s.id
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
WHERE cc.id IS NULL
ORDER BY s.descricao, p.numero;

-- ============================================================
-- VIEW 5: COLETAS POR LOCALIZAÇÃO ENCONTRADA
-- ============================================================
CREATE OR REPLACE VIEW vw_coletas_por_localizacao AS
SELECT 
    COALESCE(cc.localizacao_encontrada, 'Não informado') AS localizacao,
    COUNT(*) AS total_componentes,
    COUNT(DISTINCT ic.id_patrimonio_principal) AS total_patrimonios,
    STRING_AGG(DISTINCT ic.tipo_componente, ', ') AS tipos_componentes
FROM tabela_coleta_componente cc
INNER JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
GROUP BY cc.localizacao_encontrada
ORDER BY total_componentes DESC;

-- ============================================================
-- VIEW 6: HISTÓRICO DE COLETAS POR DATA
-- ============================================================
CREATE OR REPLACE VIEW vw_historico_coletas_componentes AS
SELECT 
    DATE(cc.data_coleta) AS data,
    u.nome_completo AS coletor,
    COUNT(*) AS componentes_coletados,
    COUNT(DISTINCT ic.id_patrimonio_principal) AS patrimonios_coletados,
    STRING_AGG(DISTINCT cc.localizacao_encontrada, ', ') AS localizacoes
FROM tabela_coleta_componente cc
INNER JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
LEFT JOIN tabela_usuario u ON cc.id_coletor = u.id
GROUP BY DATE(cc.data_coleta), u.nome_completo
ORDER BY data DESC, coletor;

-- ============================================================
-- QUERIES DE EXEMPLO PARA RELATÓRIOS
-- ============================================================

-- Relatório 1: Resumo geral
-- SELECT * FROM vw_resumo_itens_compostos;

-- Relatório 2: Detalhes de um patrimônio específico
-- SELECT * FROM vw_detalhes_componentes WHERE numero_patrimonio = '303747';

-- Relatório 3: Estatísticas do inventário atual
-- SELECT * FROM vw_estatisticas_itens_compostos;

-- Relatório 4: Componentes que faltam coletar
-- SELECT * FROM vw_componentes_faltantes;

-- Relatório 5: Onde os componentes foram encontrados
-- SELECT * FROM vw_coletas_por_localizacao;

-- Relatório 6: Histórico de coletas por dia
-- SELECT * FROM vw_historico_coletas_componentes;

-- ============================================================
-- QUERY COMPLETA PARA RELATÓRIO EXCEL/PDF
-- ============================================================
-- Relatório completo para exportação
SELECT 
    p.numero AS "Nº Patrimônio",
    p.descricao AS "Descrição do Patrimônio",
    s.descricao AS "Sala Original",
    r.nome AS "Responsável",
    ic.tipo_componente AS "Tipo Componente",
    ic.descricao_componente AS "Descrição Componente",
    ic.quantidade_esperada AS "Qtd. Esperada",
    COALESCE(cc.quantidade_encontrada, 0) AS "Qtd. Encontrada",
    COALESCE(cc.localizacao_encontrada, '-') AS "Local Encontrado",
    COALESCE(cc.status_componente, 'PENDENTE') AS "Status",
    COALESCE(cc.observacao_coleta, '-') AS "Observação",
    COALESCE(TO_CHAR(cc.data_coleta, 'DD/MM/YYYY HH24:MI'), '-') AS "Data Coleta",
    COALESCE(u.nome_completo, '-') AS "Coletor"
FROM tabela_patrimonio p
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_sala s ON p.id_sala = s.id
LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto
LEFT JOIN tabela_usuario u ON cc.id_coletor = u.id
ORDER BY p.numero, ic.tipo_componente;
