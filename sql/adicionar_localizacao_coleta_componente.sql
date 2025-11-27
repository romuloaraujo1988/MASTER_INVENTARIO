-- =====================================================
-- Script para adicionar coluna de localização na tabela de coleta de componentes
-- Data: 27/11/2025
-- =====================================================

-- Adicionar coluna de localização encontrada
ALTER TABLE tabela_coleta_componente 
ADD COLUMN IF NOT EXISTS localizacao_encontrada VARCHAR(255);

-- Comentário na coluna
COMMENT ON COLUMN tabela_coleta_componente.localizacao_encontrada IS 
    'Local onde o componente foi encontrado durante a coleta (pode ser diferente da sala do patrimônio principal)';

-- Verificar se a coluna foi criada
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'tabela_coleta_componente' 
AND column_name = 'localizacao_encontrada';
