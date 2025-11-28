-- ========================================
-- SCRIPT PARA EXTRAIR TODOS OS DADOS DE ITENS COMPOSTOS
-- ========================================
-- Execute este script no banco de ORIGEM usando psql
-- Gera arquivo com todos os INSERTs

-- Configurar saída
\pset tuples_only on
\pset format unaligned

-- Gerar cabeçalho
\o /tmp/itens_compostos_dados.sql
SELECT '-- Dados extraídos em: ' || CURRENT_TIMESTAMP;
SELECT '-- Total de registros: ' || (SELECT COUNT(*) FROM tabela_item_composto);
SELECT '';
SELECT '-- Desabilitar constraints temporariamente';
SELECT 'SET session_replication_role = replica;';
SELECT '';
SELECT '-- Limpar dados existentes (opcional)';
SELECT '-- DELETE FROM tabela_coleta_componente;';
SELECT '-- DELETE FROM tabela_item_composto;';
SELECT '';
SELECT '-- ========================================';
SELECT '-- INSERIR ITENS COMPOSTOS';
SELECT '-- ========================================';
SELECT '';
SELECT 'INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES';

-- Gerar todos os INSERTs de item_composto
SELECT 
    CASE 
        WHEN ROW_NUMBER() OVER (ORDER BY id) = 1 THEN ''
        ELSE ','
    END ||
    '(' || id || ', ' || id_patrimonio_principal || ', ''' || 
    tipo_componente || ''', ''' || 
    REPLACE(descricao_componente, '''', '''''') || ''', ' || 
    quantidade_esperada || ', ' || obrigatorio || ', ' ||
    COALESCE('''' || REPLACE(observacao, '''', '''''') || '''', 'NULL') || ', ''' ||
    data_cadastro || ''')'
FROM tabela_item_composto 
ORDER BY id;

SELECT ';';
SELECT '';
SELECT '-- ========================================';
SELECT '-- INSERIR COLETAS DE COMPONENTES';
SELECT '-- ========================================';
SELECT '';
SELECT 'INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES';

-- Gerar todos os INSERTs de coleta_componente
SELECT 
    CASE 
        WHEN ROW_NUMBER() OVER (ORDER BY id) = 1 THEN ''
        ELSE ','
    END ||
    '(' || id || ', ' || id_item_composto || ', ' || id_inventario || ', ' || 
    id_coletor || ', ' || quantidade_encontrada || ', ''' || 
    status_componente || ''', ' ||
    COALESCE('''' || REPLACE(observacao_coleta, '''', '''''') || '''', 'NULL') || ', ''' ||
    data_coleta || ''')'
FROM tabela_coleta_componente 
ORDER BY id;

SELECT ';';
SELECT '';
SELECT '-- Atualizar sequências';
SELECT 'SELECT setval(''tabela_item_composto_id_seq'', (SELECT COALESCE(MAX(id), 1) FROM tabela_item_composto));';
SELECT 'SELECT setval(''tabela_coleta_componente_id_seq'', (SELECT COALESCE(MAX(id), 1) FROM tabela_coleta_componente));';
SELECT '';
SELECT '-- Reabilitar constraints';
SELECT 'SET session_replication_role = DEFAULT;';
SELECT '';
SELECT '-- Verificação';
SELECT 'SELECT ''Itens compostos:'' as tabela, COUNT(*) as total FROM tabela_item_composto;';
SELECT 'SELECT ''Coletas componentes:'' as tabela, COUNT(*) as total FROM tabela_coleta_componente;';

\o
\pset tuples_only off
\pset format aligned

SELECT 'Arquivo gerado: /tmp/itens_compostos_dados.sql' as resultado;
SELECT 'Copie para o servidor destino e execute com: psql -f itens_compostos_dados.sql' as instrucao;
