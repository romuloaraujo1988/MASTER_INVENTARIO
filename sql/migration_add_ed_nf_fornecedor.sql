-- ============================================
-- MIGRAÇÃO: Adicionar ED, Nota Fiscal e Fornecedor
-- Data: 16/11/2024
-- Descrição: Adiciona campos para integração com SIADS
-- ============================================

-- 1. ADICIONAR NOVAS COLUNAS
ALTER TABLE TABELA_PATRIMONIO 
ADD COLUMN IF NOT EXISTS ED VARCHAR(20),
ADD COLUMN IF NOT EXISTS NUMERO_NOTA_FISCAL VARCHAR(50),
ADD COLUMN IF NOT EXISTS FORNECEDOR VARCHAR(200);

-- 2. ADICIONAR COMENTÁRIOS
COMMENT ON COLUMN TABELA_PATRIMONIO.ED IS 'Elemento de Despesa (SIADS)';
COMMENT ON COLUMN TABELA_PATRIMONIO.NUMERO_NOTA_FISCAL IS 'Número da Nota Fiscal de aquisição';
COMMENT ON COLUMN TABELA_PATRIMONIO.FORNECEDOR IS 'Nome do fornecedor';

-- 3. CRIAR ÍNDICES PARA PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_patrimonio_ed 
ON TABELA_PATRIMONIO(ED);

CREATE INDEX IF NOT EXISTS idx_patrimonio_nota_fiscal 
ON TABELA_PATRIMONIO(NUMERO_NOTA_FISCAL);

CREATE INDEX IF NOT EXISTS idx_patrimonio_fornecedor 
ON TABELA_PATRIMONIO(FORNECEDOR);

-- 4. VERIFICAR ESTRUTURA
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'tabela_patrimonio'
AND column_name IN ('ed', 'numero_nota_fiscal', 'fornecedor')
ORDER BY ordinal_position;

-- 5. ESTATÍSTICAS
SELECT 
    COUNT(*) as total_patrimonios,
    COUNT(ED) as com_ed,
    COUNT(NUMERO_NOTA_FISCAL) as com_nota_fiscal,
    COUNT(FORNECEDOR) as com_fornecedor
FROM TABELA_PATRIMONIO;
