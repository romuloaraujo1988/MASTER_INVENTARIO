-- SCRIPT PARA ATUALIZAR LOCALIZAÇÕES DAS COLETAS
-- Atualiza "Secretaria" para "SALA DO DESFAZIMENTO(PREDIO ANTIGO)"
-- Execute este script se houver coletas com localização incorreta

-- ============================================
-- 1. VERIFICAÇÃO DAS LOCALIZAÇÕES
-- ============================================

-- Verificar se há coletas com localização "Secretaria"
SELECT 'VERIFICAÇÃO: Coletas com Secretaria' as status, COUNT(*) as total
FROM tabela_coleta 
WHERE localizacao_encontrada ILIKE '%secretaria%';

-- Mostrar exemplos
SELECT 'EXEMPLOS DE LOCALIZAÇÕES' as status, localizacao_encontrada, COUNT(*) as total
FROM tabela_coleta 
WHERE localizacao_encontrada ILIKE '%secretaria%'
GROUP BY localizacao_encontrada
ORDER BY total DESC
LIMIT 5;

-- ============================================
-- 2. ATUALIZAÇÃO DAS LOCALIZAÇÕES
-- ============================================

-- ATENÇÃO: Esta operação atualizará todas as localizações que contêm "Secretaria"
-- Execute apenas se necessário

BEGIN;

-- Atualizar localizações "Secretaria" para "SALA DO DESFAZIMENTO(PREDIO ANTIGO)"
UPDATE tabela_coleta 
SET localizacao_encontrada = 'SALA  DO DESFAZIMENTO(PREDIO ANTIGO)'
WHERE localizacao_encontrada ILIKE '%secretaria%';

-- Verificar quantas linhas foram atualizadas
SELECT 'COLETAS ATUALIZADAS' as status, COUNT(*) as total_atualizadas
FROM tabela_coleta 
WHERE localizacao_encontrada = 'SALA  DO DESFAZIMENTO(PREDIO ANTIGO)'
AND id IN (SELECT id FROM tabela_coleta WHERE localizacao_encontrada ILIKE '%secretaria%' BEFORE UPDATE);

COMMIT;

-- ============================================
-- 3. VERIFICAÇÃO APÓS ATUALIZAÇÃO
-- ============================================

-- Verificar se ainda há coletas com "Secretaria"
SELECT 'DEPOIS: Coletas com Secretaria' as status, COUNT(*) as total
FROM tabela_coleta 
WHERE localizacao_encontrada ILIKE '%secretaria%';

-- Verificar coletas na Sala de Desfazimento
SELECT 'DEPOIS: Coletas na Sala de Desfazimento' as status, COUNT(*) as total
FROM tabela_coleta 
WHERE localizacao_encontrada = 'SALA  DO DESFAZIMENTO(PREDIO ANTIGO)';

-- ============================================
-- 4. RESUMO
-- ============================================

SELECT 
    'RESUMO DA ATUALIZAÇÃO DE LOCALIZAÇÕES' as status,
    (SELECT COUNT(*) FROM tabela_coleta WHERE localizacao_encontrada = 'SALA  DO DESFAZIMENTO(PREDIO ANTIGO)') as total_na_desfazimento,
    (SELECT COUNT(*) FROM tabela_coleta WHERE localizacao_encontrada ILIKE '%secretaria%') as total_com_secretaria,
    'Todas as localizações "Secretaria" foram atualizadas para "SALA DO DESFAZIMENTO(PREDIO ANTIGO)"' as observacao;

-- ============================================
-- 5. ROLLBACK (SE NECESSÁRIO)
-- ============================================

/*
-- Para reverter (se necessário):

BEGIN;

UPDATE tabela_coleta 
SET localizacao_encontrada = 'Secretaria'
WHERE localizacao_encontrada = 'SALA  DO DESFAZIMENTO(PREDIO ANTIGO)'
AND id IN (SELECT id FROM tabela_coleta WHERE localizacao_encontrada ILIKE '%secretaria%' BEFORE UPDATE);

COMMIT;
*/