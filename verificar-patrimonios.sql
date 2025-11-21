-- Verificar total de patrimônios
SELECT COUNT(*) as total_geral FROM TABELA_PATRIMONIO;

-- Verificar por STATUS
SELECT COUNT(*) as total, STATUS FROM TABELA_PATRIMONIO GROUP BY STATUS;

-- Verificar primeiros 5 registros
SELECT ID, NUMERO, DESCRICAO, STATUS FROM TABELA_PATRIMONIO LIMIT 5;

-- Verificar coletas do inventário 2
SELECT COUNT(*) as total_coletas FROM TABELA_COLETA WHERE ID_INVENTARIO = 2;
