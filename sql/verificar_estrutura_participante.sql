-- Script para verificar a estrutura real da TABELA_PARTICIPANTE_INVENTARIO
-- Execute este script para descobrir o nome correto da coluna ID

-- Verificar estrutura da tabela
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'tabela_participante_inventario'
ORDER BY ordinal_position;

-- Verificar constraints
SELECT 
    constraint_name,
    constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'tabela_participante_inventario';

-- Testar query com ID
SELECT 'Testando com ID (maiúsculo):' as teste;
SELECT ID FROM TABELA_PARTICIPANTE_INVENTARIO LIMIT 1;

-- Testar query com id_participante
SELECT 'Testando com id_participante (minúsculo):' as teste;
SELECT id_participante FROM TABELA_PARTICIPANTE_INVENTARIO LIMIT 1;

-- Testar query com ID_PARTICIPANTE
SELECT 'Testando com ID_PARTICIPANTE (maiúsculo):' as teste;
SELECT ID_PARTICIPANTE FROM TABELA_PARTICIPANTE_INVENTARIO LIMIT 1;
