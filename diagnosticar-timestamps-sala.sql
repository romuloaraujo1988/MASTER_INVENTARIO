-- Script para diagnosticar e corrigir problemas de timestamp na TABELA_SALA
-- Execute este script no PostgreSQL para identificar e corrigir dados problemáticos

-- 1. Verificar estrutura da tabela
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'tabela_sala'
ORDER BY ordinal_position;

-- 2. Verificar se há timestamps inválidos ou nulos
SELECT 
    id_sala,
    numero_sala,
    descricao,
    data_cadastro,
    CASE 
        WHEN data_cadastro IS NULL THEN 'NULL'
        WHEN data_cadastro::text = '' THEN 'EMPTY'
        ELSE 'OK'
    END as status_timestamp
FROM tabela_sala
WHERE data_cadastro IS NULL 
   OR data_cadastro::text = ''
   OR data_cadastro < '1900-01-01'::timestamp
   OR data_cadastro > '2100-01-01'::timestamp;

-- 3. Contar salas com problemas
SELECT 
    COUNT(*) as total_salas,
    COUNT(data_cadastro) as com_data,
    COUNT(*) - COUNT(data_cadastro) as sem_data
FROM tabela_sala;

-- 4. Corrigir timestamps nulos ou inválidos (DESCOMENTE PARA EXECUTAR)
-- UPDATE tabela_sala 
-- SET data_cadastro = CURRENT_TIMESTAMP 
-- WHERE data_cadastro IS NULL 
--    OR data_cadastro < '1900-01-01'::timestamp
--    OR data_cadastro > '2100-01-01'::timestamp;

-- 5. Verificar outras colunas de timestamp que possam existir
SELECT 
    column_name,
    data_type
FROM information_schema.columns 
WHERE table_name = 'tabela_sala'
  AND data_type IN ('timestamp', 'timestamp without time zone', 'timestamp with time zone', 'date', 'time');
