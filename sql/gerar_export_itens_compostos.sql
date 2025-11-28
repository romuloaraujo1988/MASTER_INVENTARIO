-- ============================================================================
-- SCRIPT PARA GERAR EXPORT DE ITENS COMPOSTOS
-- Execute este script no PostgreSQL para gerar os INSERTs
-- ============================================================================

-- Usar COPY para exportar diretamente para arquivo
-- Execute no psql: \o dados_export.sql

-- Cabeçalho
SELECT '-- SCRIPT DE DADOS - ITENS COMPOSTOS';
SELECT '-- Gerado em: ' || NOW();
SELECT '-- Total de registros: ' || (SELECT COUNT(*) FROM tabela_item_composto);
SELECT '';
SELECT 'SET session_replication_role = ''replica'';';
SELECT '';
SELECT '-- TABELA: tabela_item_composto';
SELECT '';

-- Gerar INSERTs para tabela_item_composto
SELECT 'INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (' ||
       id || ', ' ||
       COALESCE(id_patrimonio_principal::text, 'NULL') || ', ' ||
       COALESCE('''' || REPLACE(tipo_componente, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE('''' || REPLACE(descricao_componente, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE(quantidade_esperada::text, '1') || ', ' ||
       COALESCE(obrigatorio::text, 'true') || ', ' ||
       COALESCE('''' || REPLACE(observacao, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE('''' || data_cadastro::text || '''', 'NOW()') || ');'
FROM tabela_item_composto
ORDER BY id;

SELECT '';
SELECT '-- TABELA: tabela_coleta_componente';
SELECT '';

-- Gerar INSERTs para tabela_coleta_componente
SELECT 'INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (' ||
       id || ', ' ||
       COALESCE(id_item_composto::text, 'NULL') || ', ' ||
       COALESCE(id_inventario::text, 'NULL') || ', ' ||
       COALESCE(id_coletor::text, 'NULL') || ', ' ||
       COALESCE(quantidade_encontrada::text, '0') || ', ' ||
       COALESCE('''' || status_componente || '''', 'NULL') || ', ' ||
       COALESCE('''' || REPLACE(observacao_coleta, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE('''' || data_coleta::text || '''', 'NOW()') || ');'
FROM tabela_coleta_componente
ORDER BY id;

SELECT '';
SELECT '-- ATUALIZAR SEQUENCES';
SELECT 'SELECT setval(''tabela_item_composto_id_seq'', (SELECT MAX(id) FROM tabela_item_composto));';
SELECT 'SELECT setval(''tabela_coleta_componente_id_seq'', (SELECT MAX(id) FROM tabela_coleta_componente));';
SELECT '';
SELECT 'SET session_replication_role = ''origin'';';
SELECT '-- FIM DO SCRIPT';
