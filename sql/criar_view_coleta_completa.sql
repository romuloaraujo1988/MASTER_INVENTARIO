-- Script para criar VIEW que resolve problema da coluna descricao_item_sem_etiqueta
-- Esta VIEW adiciona a coluna como valor calculado baseado na descrição existente

-- Criar VIEW para coletas com coluna calculada
CREATE OR REPLACE VIEW VW_COLETA_COMPLETA AS
SELECT 
    c.*,
    CASE 
        WHEN c.SEM_ETIQUETA = true THEN c.DESCRICAO
        ELSE NULL
    END as DESCRICAO_ITEM_SEM_ETIQUETA,
    CASE 
        WHEN c.SEM_ETIQUETA = true THEN 'ITEM_SEM_ETIQUETA'
        ELSE 'ITEM_COM_ETIQUETA'
    END as CATEGORIA_ITEM_SEM_ETIQUETA
FROM TABELA_COLETA c;

-- Verificar se a VIEW foi criada corretamente
SELECT 
    COUNT(*) as total_coletas,
    COUNT(CASE WHEN SEM_ETIQUETA = true THEN 1 END) as itens_sem_etiqueta,
    COUNT(DESCRICAO_ITEM_SEM_ETIQUETA) as com_descricao_item_sem_etiqueta
FROM VW_COLETA_COMPLETA;

PRINT 'VIEW VW_COLETA_COMPLETA criada com sucesso!';