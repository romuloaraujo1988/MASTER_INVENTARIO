-- Script completo para resolver problemas de colunas faltantes
-- Execute este script no banco de dados sispatrimonio

-- 1. Criar VIEW para patrimônios com colunas calculadas
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

-- 2. Criar VIEW para coletas com coluna calculada
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

-- 3. Verificar se as VIEWs foram criadas corretamente
SELECT 
    'VW_PATRIMONIO_COMPLETO' as view_name,
    COUNT(*) as total_registros,
    COUNT(SITUACAO) as com_situacao,
    COUNT(DESCRICAO_RESUMIDA) as com_descricao_resumida
FROM VW_PATRIMONIO_COMPLETO
UNION ALL
SELECT 
    'VW_COLETA_COMPLETA' as view_name,
    COUNT(*) as total_registros,
    COUNT(CASE WHEN SEM_ETIQUETA = true THEN 1 END) as itens_sem_etiqueta,
    COUNT(DESCRICAO_ITEM_SEM_ETIQUETA) as com_descricao_item_sem_etiqueta
FROM VW_COLETA_COMPLETA;

-- 4. Comentários sobre as VIEWs criadas
/*
VW_PATRIMONIO_COMPLETO:
- Adiciona coluna SITUACAO com valor padrão 'ATIVO'
- Adiciona coluna DESCRICAO_RESUMIDA calculada automaticamente
- Inclui joins com responsável e sala

VW_COLETA_COMPLETA:
- Adiciona coluna DESCRICAO_ITEM_SEM_ETIQUETA baseada na descrição quando SEM_ETIQUETA = true
- Adiciona coluna CATEGORIA_ITEM_SEM_ETIQUETA para categorização

Essas VIEWs resolvem os problemas de colunas faltantes sem modificar a estrutura das tabelas.
*/

PRINT 'VIEWs criadas com sucesso! O sistema agora deve funcionar sem erros de colunas faltantes.';