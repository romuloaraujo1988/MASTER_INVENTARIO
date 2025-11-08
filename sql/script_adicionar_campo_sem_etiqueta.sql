-- Script para adicionar campo SEM_ETIQUETA na TABELA_COLETA
-- Sistema de Inventário IFMT
-- Data: 2024

-- Verificar se a coluna já existe antes de adicionar
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_coleta' 
        AND column_name = 'sem_etiqueta'
    ) THEN
        -- Adicionar campo para identificar itens sem etiqueta
        ALTER TABLE TABELA_COLETA 
        ADD COLUMN SEM_ETIQUETA BOOLEAN DEFAULT FALSE;
        
        RAISE NOTICE 'Campo SEM_ETIQUETA adicionado com sucesso!';
    ELSE
        RAISE NOTICE 'Campo SEM_ETIQUETA já existe na tabela.';
    END IF;
END $$;

-- Adicionar comentário ao campo
COMMENT ON COLUMN TABELA_COLETA.SEM_ETIQUETA IS 'Indica se o item foi encontrado sem etiqueta patrimonial';

-- Criar índice para melhorar performance das consultas
CREATE INDEX IF NOT EXISTS idx_coleta_sem_etiqueta ON TABELA_COLETA(SEM_ETIQUETA);

-- Criar índice composto para consultas mais eficientes
CREATE INDEX IF NOT EXISTS idx_coleta_inventario_sem_etiqueta 
ON TABELA_COLETA(ID_INVENTARIO, SEM_ETIQUETA) 
WHERE SEM_ETIQUETA = TRUE;

-- Atualizar registros existentes (opcional - apenas se necessário)
-- UPDATE TABELA_COLETA SET SEM_ETIQUETA = FALSE WHERE SEM_ETIQUETA IS NULL;

-- Verificar a estrutura atualizada
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default,
    col_description(pgc.oid, a.attnum) as column_comment
FROM information_schema.columns a
JOIN pg_class pgc ON pgc.relname = a.table_name
WHERE table_name = 'tabela_coleta' 
AND column_name = 'sem_etiqueta';

-- Verificar índices criados
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'tabela_coleta' 
AND indexname LIKE '%sem_etiqueta%';

PRINT 'Script executado com sucesso!';
PRINT 'Campo SEM_ETIQUETA adicionado à TABELA_COLETA';
PRINT 'Índices criados para otimização de consultas';

-- Exemplo de uso do novo campo:
/*
-- Inserir coleta com item sem etiqueta
INSERT INTO TABELA_COLETA (
    ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, 
    STATUS_COLETA, SEM_ETIQUETA, OBSERVACAO_COLETA
) VALUES (
    1, 123, 1, 
    'COLETADO', TRUE, 'Item encontrado sem etiqueta patrimonial visível'
);

-- Consultar itens sem etiqueta
SELECT 
    p.NUMERO,
    p.DESCRICAO,
    c.OBSERVACAO_COLETA
FROM TABELA_COLETA c
JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID
WHERE c.SEM_ETIQUETA = TRUE;
*/