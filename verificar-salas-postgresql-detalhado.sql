-- Script detalhado para verificar EXATAMENTE quantas salas existem no PostgreSQL
-- Execute: psql -h localhost -U inventario -d sispatrimonio -f verificar-salas-postgresql-detalhado.sql

\echo '========================================='
\echo '  VERIFICAÇÃO DETALHADA DE SALAS'
\echo '========================================='
\echo ''

-- 1. Total absoluto de registros na tabela
\echo '1. Total ABSOLUTO de registros na TABELA_SALA:'
SELECT COUNT(*) as total_registros FROM TABELA_SALA;

-- 2. Distribuição por status ATIVO
\echo ''
\echo '2. Distribuição por campo ATIVO:'
SELECT 
    ATIVO,
    COUNT(*) as quantidade
FROM TABELA_SALA
GROUP BY ATIVO
ORDER BY ATIVO DESC;

-- 3. Verificar se há valores NULL no campo ATIVO
\echo ''
\echo '3. Registros com ATIVO = NULL:'
SELECT COUNT(*) as ativo_null FROM TABELA_SALA WHERE ATIVO IS NULL;

-- 4. Primeiras 20 salas (para ver os dados reais)
\echo ''
\echo '4. Primeiras 20 salas (todas, independente de ATIVO):'
SELECT 
    ID_SALA,
    NUMERO_SALA,
    DESCRICAO,
    ATIVO,
    ID_SETOR
FROM TABELA_SALA
ORDER BY ID_SALA
LIMIT 20;

-- 5. Últimas 20 salas
\echo ''
\echo '5. Últimas 20 salas:'
SELECT 
    ID_SALA,
    NUMERO_SALA,
    DESCRICAO,
    ATIVO,
    ID_SETOR
FROM TABELA_SALA
ORDER BY ID_SALA DESC
LIMIT 20;

-- 6. Verificar se há problema com JOIN de SETOR
\echo ''
\echo '6. Salas SEM setor (ID_SETOR NULL ou inválido):'
SELECT COUNT(*) as sem_setor
FROM TABELA_SALA s
WHERE s.ID_SETOR IS NULL 
   OR NOT EXISTS (SELECT 1 FROM TABELA_SETOR st WHERE st.ID = s.ID_SETOR);

-- 7. Testar a query exata que listarTodasSalas() usa
\echo ''
\echo '7. Resultado da query listarTodasSalas() (com LEFT JOIN):'
SELECT COUNT(*) as total_com_join
FROM TABELA_SALA s
LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID;

-- 8. Comparar com query sem JOIN
\echo ''
\echo '8. Resultado SEM JOIN (apenas TABELA_SALA):'
SELECT COUNT(*) as total_sem_join
FROM TABELA_SALA;

-- 9. Verificar se há problema com ORDER BY
\echo ''
\echo '9. Salas com NUMERO_SALA NULL ou vazio:'
SELECT COUNT(*) as numero_sala_invalido
FROM TABELA_SALA
WHERE NUMERO_SALA IS NULL OR NUMERO_SALA = '';

-- 10. Verificar estrutura da tabela
\echo ''
\echo '10. Estrutura da tabela TABELA_SALA:'
\d TABELA_SALA

\echo ''
\echo '========================================='
\echo '  FIM DA VERIFICAÇÃO'
\echo '========================================='
