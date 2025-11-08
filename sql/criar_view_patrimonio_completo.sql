-- Script para criar VIEW que resolve problema de colunas faltantes
-- Esta VIEW adiciona as colunas SITUACAO e DESCRICAO_RESUMIDA como valores calculados

-- Criar VIEW para patrimônios com colunas calculadas
CREATE OR REPLACE VIEW VW_PATRIMONIO_COMPLETO AS
SELECT 
    p.*,
    'ATIVO' as SITUACAO,
    CASE 
        WHEN LENGTH(p.DESCRICAO) <= 50 THEN p.DESCRICAO
        ELSE SUBSTRING(p.DESCRICAO, 1, 47) || '...'
    END as DESCRICAO_RESUMIDA,
    r.NOME as nome_responsavel,
    s.DESCRICAO as nome_sala
FROM TABELA_PATRIMONIO p
LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA;

-- Verificar se a VIEW foi criada corretamente
SELECT 
    COUNT(*) as total_patrimonios,
    COUNT(SITUACAO) as com_situacao,
    COUNT(DESCRICAO_RESUMIDA) as com_descricao_resumida
FROM VW_PATRIMONIO_COMPLETO;

PRINT 'VIEW VW_PATRIMONIO_COMPLETO criada com sucesso!';