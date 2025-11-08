-- Script para adicionar colunas para itens sem patrimônio na TABELA_COLETA
-- Sistema de Inventário IFMT
-- Data: 2025

-- Verificar e adicionar coluna DESCRICAO_ITEM_SEM_ETIQUETA
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_coleta' 
        AND column_name = 'descricao_item_sem_etiqueta'
    ) THEN
        ALTER TABLE TABELA_COLETA 
        ADD COLUMN DESCRICAO_ITEM_SEM_ETIQUETA TEXT;
        
        RAISE NOTICE 'Campo DESCRICAO_ITEM_SEM_ETIQUETA adicionado com sucesso!';
    ELSE
        RAISE NOTICE 'Campo DESCRICAO_ITEM_SEM_ETIQUETA já existe na tabela.';
    END IF;
END $$;

-- Verificar e adicionar coluna CATEGORIA_ITEM_SEM_ETIQUETA
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_coleta' 
        AND column_name = 'categoria_item_sem_etiqueta'
    ) THEN
        ALTER TABLE TABELA_COLETA 
        ADD COLUMN CATEGORIA_ITEM_SEM_ETIQUETA VARCHAR(100);
        
        RAISE NOTICE 'Campo CATEGORIA_ITEM_SEM_ETIQUETA adicionado com sucesso!';
    ELSE
        RAISE NOTICE 'Campo CATEGORIA_ITEM_SEM_ETIQUETA já existe na tabela.';
    END IF;
END $$;

-- Adicionar comentários aos campos
COMMENT ON COLUMN TABELA_COLETA.DESCRICAO_ITEM_SEM_ETIQUETA IS 'Descrição detalhada do item encontrado sem etiqueta patrimonial';
COMMENT ON COLUMN TABELA_COLETA.CATEGORIA_ITEM_SEM_ETIQUETA IS 'Categoria do item sem etiqueta (MÓVEIS, EQUIPAMENTOS, ELETRÔNICOS, etc.)';

-- Criar índices para melhorar performance das consultas
CREATE INDEX IF NOT EXISTS idx_coleta_descricao_sem_etiqueta 
ON TABELA_COLETA(DESCRICAO_ITEM_SEM_ETIQUETA) 
WHERE SEM_ETIQUETA = TRUE;

CREATE INDEX IF NOT EXISTS idx_coleta_categoria_sem_etiqueta 
ON TABELA_COLETA(CATEGORIA_ITEM_SEM_ETIQUETA) 
WHERE SEM_ETIQUETA = TRUE;

-- Criar índice composto para consultas mais eficientes
CREATE INDEX IF NOT EXISTS idx_coleta_sem_etiqueta_completo 
ON TABELA_COLETA(ID_INVENTARIO, SEM_ETIQUETA, CATEGORIA_ITEM_SEM_ETIQUETA) 
WHERE SEM_ETIQUETA = TRUE;

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
AND column_name IN ('sem_etiqueta', 'descricao_item_sem_etiqueta', 'categoria_item_sem_etiqueta')
ORDER BY column_name;

-- Verificar índices criados
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'tabela_coleta' 
AND indexname LIKE '%sem_etiqueta%'
ORDER BY indexname;

PRINT 'Script executado com sucesso!';
PRINT 'Colunas para itens sem patrimônio adicionadas à TABELA_COLETA';
PRINT 'Índices criados para otimização de consultas';

-- Exemplo de uso das novas colunas:
/*
-- Inserir coleta de item sem etiqueta
INSERT INTO TABELA_COLETA (
    ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, 
    STATUS_COLETA, SEM_ETIQUETA, 
    DESCRICAO_ITEM_SEM_ETIQUETA, CATEGORIA_ITEM_SEM_ETIQUETA,
    LOCALIZACAO_ENCONTRADA, OBSERVACAO_COLETA
) VALUES (
    1, 0, 1, 
    'COLETADO', TRUE, 
    'Mesa de escritório em madeira, cor marrom, com 3 gavetas', 'MÓVEIS',
    'Sala 101', 'Item encontrado sem etiqueta patrimonial visível'
);

-- Consultar itens sem etiqueta por categoria
SELECT 
    DESCRICAO_ITEM_SEM_ETIQUETA,
    CATEGORIA_ITEM_SEM_ETIQUETA,
    LOCALIZACAO_ENCONTRADA,
    DATA_COLETA,
    OBSERVACAO_COLETA
FROM TABELA_COLETA 
WHERE SEM_ETIQUETA = TRUE 
  AND CATEGORIA_ITEM_SEM_ETIQUETA = 'MÓVEIS'
ORDER BY DATA_COLETA DESC;

-- Agrupar itens sem etiqueta por categoria
SELECT 
    CATEGORIA_ITEM_SEM_ETIQUETA,
    COUNT(*) as TOTAL_ITENS
FROM TABELA_COLETA 
WHERE SEM_ETIQUETA = TRUE 
GROUP BY CATEGORIA_ITEM_SEM_ETIQUETA
ORDER BY TOTAL_ITENS DESC;
*/