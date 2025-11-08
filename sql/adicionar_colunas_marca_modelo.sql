-- Script para adicionar as colunas MARCA e MODELO na TABELA_PATRIMONIO
-- Execute este script se as colunas não existirem no banco de dados

-- Verificar se as colunas existem e adicioná-las se necessário
DO $$
BEGIN
    -- Adicionar coluna MARCA se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'tabela_patrimonio' 
                   AND column_name = 'marca') THEN
        ALTER TABLE TABELA_PATRIMONIO ADD COLUMN MARCA VARCHAR(255);
        RAISE NOTICE 'Coluna MARCA adicionada à TABELA_PATRIMONIO';
    ELSE
        RAISE NOTICE 'Coluna MARCA já existe na TABELA_PATRIMONIO';
    END IF;
    
    -- Adicionar coluna MODELO se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'tabela_patrimonio' 
                   AND column_name = 'modelo') THEN
        ALTER TABLE TABELA_PATRIMONIO ADD COLUMN MODELO VARCHAR(255);
        RAISE NOTICE 'Coluna MODELO adicionada à TABELA_PATRIMONIO';
    ELSE
        RAISE NOTICE 'Coluna MODELO já existe na TABELA_PATRIMONIO';
    END IF;
END $$;

-- Comentários nas novas colunas
COMMENT ON COLUMN TABELA_PATRIMONIO.MARCA IS 'Marca do patrimônio';
COMMENT ON COLUMN TABELA_PATRIMONIO.MODELO IS 'Modelo do patrimônio';

-- Criar índices para melhorar performance nas buscas
CREATE INDEX IF NOT EXISTS idx_patrimonio_marca ON TABELA_PATRIMONIO(MARCA);
CREATE INDEX IF NOT EXISTS idx_patrimonio_modelo ON TABELA_PATRIMONIO(MODELO);

SELECT 'Colunas MARCA e MODELO verificadas/adicionadas com sucesso!' as resultado;