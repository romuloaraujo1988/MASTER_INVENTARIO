-- Script para adicionar constraint UNIQUE na tabela TABELA_COLETA
-- Impede que um patrimônio seja coletado mais de uma vez no mesmo inventário

-- Verificar se existem duplicatas antes de adicionar a constraint
SELECT 
    ID_INVENTARIO, 
    ID_PATRIMONIO, 
    COUNT(*) as quantidade
FROM TABELA_COLETA 
WHERE ID_PATRIMONIO > 0  -- Excluir itens sem etiqueta (ID_PATRIMONIO = 0)
GROUP BY ID_INVENTARIO, ID_PATRIMONIO 
HAVING COUNT(*) > 1;

-- Se não houver duplicatas, adicionar a constraint
ALTER TABLE TABELA_COLETA 
ADD CONSTRAINT uk_coleta_inventario_patrimonio 
UNIQUE (ID_INVENTARIO, ID_PATRIMONIO);

-- Comentário sobre a constraint
COMMENT ON CONSTRAINT uk_coleta_inventario_patrimonio ON TABELA_COLETA 
IS 'Impede que um patrimônio seja coletado mais de uma vez no mesmo inventário';

SELECT 'Constraint UNIQUE adicionada com sucesso!' as resultado;