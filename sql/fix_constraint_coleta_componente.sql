-- ============================================================================
-- SCRIPT PARA ADICIONAR CONSTRAINT UNIQUE NA TABELA_COLETA_COMPONENTE
-- ============================================================================
-- Data: 28/11/2025
-- 
-- Este script adiciona a constraint de unicidade necessária para o 
-- ON CONFLICT funcionar corretamente
-- ============================================================================

-- Verificar se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_coleta_componente_item_inventario'
    ) THEN
        -- Adicionar constraint de unicidade
        ALTER TABLE tabela_coleta_componente 
        ADD CONSTRAINT uk_coleta_componente_item_inventario 
        UNIQUE (id_item_composto, id_inventario);
        
        RAISE NOTICE '✓ Constraint de unicidade adicionada com sucesso!';
    ELSE
        RAISE NOTICE '✓ Constraint já existe, nada a fazer.';
    END IF;
END $$;

-- Verificar a estrutura final
SELECT 
    conname as constraint_name,
    contype as constraint_type,
    pg_get_constraintdef(oid) as definition
FROM pg_constraint
WHERE conrelid = 'tabela_coleta_componente'::regclass
ORDER BY conname;
