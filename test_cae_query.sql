-- Testar busca de coletas para sala CAE
SELECT 
    c.id,
    c.id_patrimonio,
    c.numero_patrimonio,
    c.data_coleta,
    c.localizacao_encontrada,
    c.estado_encontrado,
    c.sem_etiqueta,
    c.descricao_item_sem_etiqueta
FROM TABELA_COLETA c
WHERE c.localizacao_encontrada LIKE '%CAE%' 
   OR c.localizacao_atual LIKE '%CAE%'
ORDER BY c.data_coleta DESC
LIMIT 20;
