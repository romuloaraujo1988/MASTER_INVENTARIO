-- Script para adicionar o campo SITUACAO na tabela TABELA_PATRIMONIO
-- Este campo representa a situação do patrimônio: ATIVO, PENDENTE, BAIXADO, ESTORNADO

-- Adicionar o campo SITUACAO se não existir
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_patrimonio' 
        AND column_name = 'situacao'
    ) THEN
        ALTER TABLE TABELA_PATRIMONIO 
        ADD COLUMN SITUACAO VARCHAR(20) DEFAULT 'ATIVO' 
        CHECK (SITUACAO IN ('ATIVO', 'PENDENTE', 'BAIXADO', 'ESTORNADO'));
        
        -- Atualizar registros existentes para ATIVO
        UPDATE TABELA_PATRIMONIO SET SITUACAO = 'ATIVO' WHERE SITUACAO IS NULL;
        
        RAISE NOTICE 'Campo SITUACAO adicionado com sucesso à tabela TABELA_PATRIMONIO';
    ELSE
        RAISE NOTICE 'Campo SITUACAO já existe na tabela TABELA_PATRIMONIO';
    END IF;
END
$$;

-- Criar índice para melhorar performance nas consultas por situação
CREATE INDEX IF NOT EXISTS idx_patrimonio_situacao ON TABELA_PATRIMONIO(SITUACAO);

-- Comentário no campo
COMMENT ON COLUMN TABELA_PATRIMONIO.SITUACAO IS 'Situação do patrimônio: ATIVO, PENDENTE, BAIXADO, ESTORNADO';

PRINT 'Script de adição do campo SITUACAO executado com sucesso!';