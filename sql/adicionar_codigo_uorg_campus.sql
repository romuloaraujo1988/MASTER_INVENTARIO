-- Script para adicionar coluna codigo_uorg na tabela TABELA_CAMPUS
-- Necessário para integração com SIADS (Sistema Integrado de Administração de Serviços)
-- Data: 2025-11-08

-- Adicionar coluna codigo_uorg
ALTER TABLE TABELA_CAMPUS 
ADD COLUMN IF NOT EXISTS codigo_uorg VARCHAR(20);

-- Adicionar comentário na coluna
COMMENT ON COLUMN TABELA_CAMPUS.codigo_uorg IS 'Código da Unidade Organizacional no SIADS';

-- Criar índice para melhorar performance de buscas
CREATE INDEX IF NOT EXISTS idx_campus_codigo_uorg ON TABELA_CAMPUS(codigo_uorg);

-- Exibir estrutura atualizada da tabela
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'tabela_campus'
ORDER BY ordinal_position;
