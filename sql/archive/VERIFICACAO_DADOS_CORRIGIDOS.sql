-- SCRIPT DE VERIFICAÇÃO: DADOS CORRIGIDOS
-- Execute após as atualizações para verificar se tudo está correto

-- ============================================
-- 1. VERIFICAÇÃO DAS SALAS
-- ============================================

SELECT 'SITUAÇÃO DAS SALAS' as cabecalho;

SELECT 
    s.id,
    s.descricao as sala,
    s.ativo,
    COUNT(p.id) as total_patrimonios,
    CASE 
        WHEN s.descricao ILIKE '%secretaria%' THEN '⚠️ VERIFICAR'
        WHEN s.descricao ILIKE '%desfazimento%' THEN '✅ CORRETO'
        ELSE '✅ OK'
    END as status
FROM tabela_sala s
LEFT JOIN tabela_patrimonio p ON s.id = p.id_sala
WHERE s.descricao ILIKE '%secretaria%' OR s.descricao ILIKE '%desfazimento%'
GROUP BY s.id, s.descricao, s.ativo
ORDER BY s.descricao;

-- ============================================
-- 2. VERIFICAÇÃO DAS COLETAS
-- ============================================

SELECT 'ESTATÍSTICAS DA SALA DE DESFAZIMENTO' as cabecalho;

SELECT 
    'SALA DO DESFAZIMENTO(PREDIO ANTIGO)' as sala,
    COUNT(DISTINCT c.id) as total_coletas,
    COUNT(DISTINCT CASE WHEN c.divergencia = true THEN c.id END) as divergencias,
    COUNT(DISTINCT CASE WHEN c.estado_encontrado = 'IRRECUPERÁVEL' THEN c.id END) as irrecuperaveis,
    COUNT(DISTINCT CASE WHEN c.estado_encontrado = 'BOM' THEN c.id END) as bons,
    ROUND(COUNT(DISTINCT CASE WHEN c.divergencia = true THEN c.id END) * 100.0 / COUNT(DISTINCT c.id), 2) as taxa_divergencia,
    ROUND(COUNT(DISTINCT CASE WHEN c.estado_encontrado = 'IRRECUPERÁVEL' THEN c.id END) * 100.0 / COUNT(DISTINCT c.id), 2) as taxa_irrecuperaveis,
    ROUND(COUNT(DISTINCT CASE WHEN c.estado_encontrado = 'BOM' THEN c.id END) * 100.0 / COUNT(DISTINCT c.id), 2) as taxa_bons
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
WHERE p.id_sala = 14; -- ID da Sala de Desfazimento

-- ============================================
-- 3. VERIFICAÇÃO DE LOCALIZAÇÕES INCORRETAS
-- ============================================

SELECT 'VERIFICAÇÃO DE LOCALIZAÇÕES PROBLEMÁTICAS' as cabecalho;

SELECT 
    'Coletas com Secretaria' as tipo,
    COUNT(*) as total,
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ CORRETO'
        ELSE '⚠️ PRECISA CORRIGIR'
    END as status
FROM tabela_coleta 
WHERE localizacao_encontrada ILIKE '%secretaria%'

UNION ALL

SELECT 
    'Coletas na Sala de Desfazimento' as tipo,
    COUNT(*) as total,
    '✅ CORRETO' as status
FROM tabela_coleta 
WHERE localizacao_encontrada = 'SALA  DO DESFAZIMENTO(PREDIO ANTIGO)';

-- ============================================
-- 4. RESUMO FINAL
-- ============================================

SELECT 'RESUMO FINAL DA CORREÇÃO' as cabecalho;

SELECT 
    '1. Patrimônios na Secretaria' as item,
    (SELECT COUNT(*) FROM tabela_patrimonio WHERE id_sala = 6) as valor,
    CASE 
        WHEN (SELECT COUNT(*) FROM tabela_patrimonio WHERE id_sala = 6) = 0 THEN '✅ CORRETO'
        ELSE '❌ PRECISA CORRIGIR'
    END as status

UNION ALL

SELECT 
    '2. Patrimônios na Sala de Desfazimento' as item,
    (SELECT COUNT(*) FROM tabela_patrimonio WHERE id_sala = 14) as valor,
    CASE 
        WHEN (SELECT COUNT(*) FROM tabela_patrimonio WHERE id_sala = 14) > 0 THEN '✅ CORRETO'
        ELSE '⚠️ VERIFICAR'
    END as status

UNION ALL

SELECT 
    '3. Coletas com localização Secretaria' as item,
    (SELECT COUNT(*) FROM tabela_coleta WHERE localizacao_encontrada ILIKE '%secretaria%') as valor,
    CASE 
        WHEN (SELECT COUNT(*) FROM tabela_coleta WHERE localizacao_encontrada ILIKE '%secretaria%') = 0 THEN '✅ CORRETO'
        ELSE '❌ PRECISA CORRIGIR'
    END as status

UNION ALL

SELECT 
    '4. Coletas na Sala de Desfazimento' as item,
    (SELECT COUNT(*) FROM tabela_coleta WHERE localizacao_encontrada = 'SALA  DO DESFAZIMENTO(PREDIO ANTIGO)') as valor,
    '✅ CORRETO' as status;

-- ============================================
-- 5. RECOMENDAÇÕES
-- ============================================

SELECT 'RECOMENDAÇÕES:' as cabecalho;

SELECT '✅ Se todos os status estiverem "CORRETO", os dados foram corrigidos com sucesso.' as recomendacao
UNION ALL
SELECT '⚠️ Se houver algum status "PRECISA CORRIGIR", execute os scripts de atualização.' as recomendacao
UNION ALL
SELECT '📊 Use estes dados para atualizar o documento de análise.' as recomendacao
UNION ALL
SELECT '💾 Considere fazer backup do banco após as correções.' as recomendacao;