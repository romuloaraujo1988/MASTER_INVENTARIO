-- Script para atualizar a estrutura da TABELA_COLETA
-- Adiciona suporte ao novo campo ID_PARTICIPANTE_INVENTARIO
-- Mantém compatibilidade com ID_USUARIO durante a transição

-- 1. Adicionar nova coluna ID_PARTICIPANTE_INVENTARIO
ALTER TABLE TABELA_COLETA 
ADD COLUMN ID_PARTICIPANTE_INVENTARIO INTEGER;

-- 2. Criar índice para melhor performance
CREATE INDEX IF NOT EXISTS idx_coleta_participante 
ON TABELA_COLETA(ID_PARTICIPANTE_INVENTARIO);

-- 3. Adicionar chave estrangeira (após migração dos dados)
-- Esta constraint será adicionada após a migração dos dados existentes
-- ALTER TABLE TABELA_COLETA 
-- ADD CONSTRAINT fk_coleta_participante_inventario 
-- FOREIGN KEY (ID_PARTICIPANTE_INVENTARIO) 
-- REFERENCES TABELA_PARTICIPANTE_INVENTARIO(ID);

-- 4. Comentários para documentação
COMMENT ON COLUMN TABELA_COLETA.ID_PARTICIPANTE_INVENTARIO IS 
'Referência ao participante do inventário que realizou a coleta. Substitui gradualmente o campo ID_USUARIO/ID_COLETOR.';

-- 5. Script de verificação da estrutura
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'TABELA_COLETA' 
AND column_name IN ('ID_USUARIO', 'ID_COLETOR', 'ID_PARTICIPANTE_INVENTARIO')
ORDER BY ordinal_position;

-- 6. Verificar constraints existentes
SELECT 
    constraint_name,
    constraint_type,
    table_name
FROM information_schema.table_constraints 
WHERE table_name = 'TABELA_COLETA'
AND constraint_type = 'FOREIGN KEY';