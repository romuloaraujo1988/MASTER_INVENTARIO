-- Script para diagnosticar problemas de timestamp na tabela TABELA_SALA
-- Execute este script no PostgreSQL para identificar registros problemáticos

-- 1. Verificar estrutura da tabela
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'tabela_sala'
ORDER BY ordinal_position;

-- 2. Verificar se há valores NULL ou inválidos em DATA_CADASTRO
SELECT 
    ID_SALA,
    NUMERO_SALA,
    DESCRICAO,
    DATA_CADASTRO,
    CASE 
        WHEN DATA_CADASTRO IS NULL THEN 'NULL'
        WHEN DATA_CADASTRO::text = '' THEN 'VAZIO'
        ELSE 'OK'
    END AS STATUS_DATA
FROM TABELA_SALA
WHERE ATIVO = TRUE
ORDER BY ID_SALA;

-- 3. Contar salas com problemas
SELECT 
    COUNT(*) as total_salas,
    COUNT(DATA_CADASTRO) as com_data,
    COUNT(*) - COUNT(DATA_CADASTRO) as sem_data
FROM TABELA_SALA
WHERE ATIVO = TRUE;

-- 4. Verificar salas abertas para coleta (simulando a query do sistema)
SELECT DISTINCT 
    s.ID_SALA, 
    s.NUMERO_SALA, 
    s.DESCRICAO, 
    s.ID_SETOR, 
    s.ATIVO,
    s.DATA_CADASTRO,
    CASE 
        WHEN s.DATA_CADASTRO IS NULL THEN 'NULL'
        ELSE 'OK'
    END AS STATUS_DATA
FROM TABELA_SALA s
LEFT JOIN TABELA_SALA_INVENTARIO si ON s.ID_SALA = si.ID_SALA 
WHERE s.ATIVO = TRUE
AND (si.COLETA_FINALIZADA = FALSE OR si.COLETA_FINALIZADA IS NULL)
ORDER BY s.NUMERO_SALA;

-- 5. CORREÇÃO: Atualizar salas com DATA_CADASTRO NULL
-- DESCOMENTE as linhas abaixo para aplicar a correção
-- UPDATE TABELA_SALA 
-- SET DATA_CADASTRO = CURRENT_TIMESTAMP 
-- WHERE DATA_CADASTRO IS NULL;
