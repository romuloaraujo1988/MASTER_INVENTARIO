-- Script para corrigir constraint de id_coletor na tabela_coleta
-- O sistema agora usa id_participante_inventario ao invés de id_coletor
-- Autor: Sistema de Inventário
-- Data: 17/11/2025

-- Opção 1: Remover a constraint de foreign key (RECOMENDADO)
-- Isso permite que id_coletor seja qualquer valor, já que não é mais usado
ALTER TABLE tabela_coleta 
DROP CONSTRAINT IF EXISTS tabela_coleta_id_coletor_fkey;

-- Opção 2: Tornar id_coletor nullable (alternativa)
-- ALTER TABLE tabela_coleta 
-- ALTER COLUMN id_coletor DROP NOT NULL;

-- Verificar as constraints restantes
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_name = 'tabela_coleta'
ORDER BY tc.constraint_name;

-- Mensagem de sucesso
SELECT 'Constraint removida com sucesso! O sistema agora usa id_participante_inventario.' AS status;
