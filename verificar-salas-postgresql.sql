-- Script para verificar quantas salas existem no PostgreSQL
-- Execute: psql -h localhost -U inventario -d sispatrimonio -f verificar-salas-postgresql.sql

\echo '========================================='
\echo '  VERIFICAÇÃO DE SALAS NO POSTGRESQL'
\echo '========================================='
\echo ''

-- 1. Total de salas (ativas e inativas)
\echo '1. Total de salas (todas):'
SELECT COUNT(*) as total_salas FROM TABELA_SALA;

-- 2. Salas ativas
\echo ''
\echo '2. Salas ATIVAS:'
SELECT COUNT(*) as salas_ativas FROM TABELA_SALA WHERE ATIVO = TRUE;

-- 3. Salas inativas
\echo ''
\echo '3. Salas INATIVAS:'
SELECT COUNT(*) as salas_inativas FROM TABELA_SALA WHERE ATIVO = FALSE;

-- 4. Distribuição por status
\echo ''
\echo '4. Distribuição por status:'
SELECT 
    CASE WHEN ATIVO = TRUE THEN 'ATIVA' ELSE 'INATIVA' END as status,
    COUNT(*) as quantidade
FROM TABELA_SALA
GROUP BY ATIVO
ORDER BY ATIVO DESC;

-- 5. Primeiras 20 salas ativas (para verificar dados)
\echo ''
\echo '5. Primeiras 20 salas ATIVAS:'
SELECT 
    ID_SALA,
    NUMERO_SALA,
    DESCRICAO,
    BLOCO,
    ANDAR,
    ATIVO
FROM TABELA_SALA
WHERE ATIVO = TRUE
ORDER BY NUMERO_SALA
LIMIT 20;

-- 6. Verificar se há salas sem número
\echo ''
\echo '6. Salas sem número (podem causar problemas):'
SELECT COUNT(*) as salas_sem_numero 
FROM TABELA_SALA 
WHERE NUMERO_SALA IS NULL OR NUMERO_SALA = '';

-- 7. Verificar duplicatas de número
\echo ''
\echo '7. Números de sala duplicados:'
SELECT 
    NUMERO_SALA,
    COUNT(*) as quantidade
FROM TABELA_SALA
WHERE ATIVO = TRUE
GROUP BY NUMERO_SALA
HAVING COUNT(*) > 1;

\echo ''
\echo '========================================='
\echo '  FIM DA VERIFICAÇÃO'
\echo '========================================='
