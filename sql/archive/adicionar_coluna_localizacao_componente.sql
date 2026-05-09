-- Script para adicionar coluna de localização na tabela de coleta de componentes
-- Data: 12/01/2026
-- Problema: A informação de "Local Encontrado" não estava sendo carregada na JTable
-- Solução: Adicionar a coluna localizacao_encontrada na tabela tabela_coleta_componente

-- Verificar se a coluna já existe antes de adicionar
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_coleta_componente' 
        AND column_name = 'localizacao_encontrada'
    ) THEN
        ALTER TABLE tabela_coleta_componente 
        ADD COLUMN localizacao_encontrada VARCHAR(255);
        
        RAISE NOTICE 'Coluna localizacao_encontrada adicionada com sucesso!';
    ELSE
        RAISE NOTICE 'Coluna localizacao_encontrada já existe.';
    END IF;
END $$;

-- Comentário na coluna
COMMENT ON COLUMN tabela_coleta_componente.localizacao_encontrada IS 'Local onde o componente foi encontrado durante a coleta';

-- Verificar estrutura final
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'tabela_coleta_componente'
ORDER BY ordinal_position;

-- Mostrar quantidade de registros existentes
SELECT COUNT(*) as total_registros FROM tabela_coleta_componente;
