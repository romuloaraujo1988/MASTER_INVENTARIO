-- SCRIPT DE ATUALIZAÇÃO: SECRETARIA → SALA DE DESFAZIMENTO
-- Execute este script no PostgreSQL para corrigir os dados
-- Data: 08/03/2026

-- ============================================
-- 1. VERIFICAÇÃO ANTES DA ATUALIZAÇÃO
-- ============================================

-- Verificar quantos patrimônios estão na Secretaria
SELECT 'ANTES: Patrimônios na Secretaria' as status, COUNT(*) as total
FROM tabela_patrimonio 
WHERE id_sala = 6; -- ID da Secretaria

-- Verificar quantos patrimônios estão na Sala de Desfazimento
SELECT 'ANTES: Patrimônios na Sala de Desfazimento' as status, COUNT(*) as total
FROM tabela_patrimonio 
WHERE id_sala = 14; -- ID da Sala de Desfazimento

-- ============================================
-- 2. ATUALIZAÇÃO DOS PATRIMÔNIOS
-- ============================================

-- ATENÇÃO: Esta operação moverá todos os patrimônios da Secretaria para a Sala de Desfazimento
-- Execute com cuidado e faça backup antes se necessário

BEGIN;

-- Atualizar os patrimônios da Secretaria (ID 6) para Sala de Desfazimento (ID 14)
UPDATE tabela_patrimonio 
SET id_sala = 14  -- Sala de Desfazimento
WHERE id_sala = 6; -- Secretaria

-- Verificar quantas linhas foram atualizadas
SELECT 'PATRIMÔNIOS ATUALIZADOS' as status, COUNT(*) as total_atualizados
FROM tabela_patrimonio 
WHERE id_sala = 14 
AND id IN (SELECT id FROM tabela_patrimonio WHERE id_sala = 6 BEFORE UPDATE);

COMMIT;

-- ============================================
-- 3. VERIFICAÇÃO APÓS A ATUALIZAÇÃO
-- ============================================

-- Verificar situação após atualização
SELECT 'DEPOIS: Patrimônios na Secretaria' as status, COUNT(*) as total
FROM tabela_patrimonio 
WHERE id_sala = 6;

SELECT 'DEPOIS: Patrimônios na Sala de Desfazimento' as status, COUNT(*) as total
FROM tabela_patrimonio 
WHERE id_sala = 14;

-- ============================================
-- 4. RESUMO DA OPERAÇÃO
-- ============================================

SELECT 
    'RESUMO DA ATUALIZAÇÃO' as status,
    (SELECT COUNT(*) FROM tabela_patrimonio WHERE id_sala = 14) as total_na_desfazimento,
    (SELECT COUNT(*) FROM tabela_patrimonio WHERE id_sala = 6) as total_na_secretaria,
    'Todos os patrimônios foram movidos da Secretaria para a Sala de Desfazimento' as observacao;

-- ============================================
-- 5. SCRIPT DE ROLLBACK (EM CASO DE ERRO)
-- ============================================

/*
-- Para reverter a operação (se necessário), execute:

BEGIN;

UPDATE tabela_patrimonio 
SET id_sala = 6  -- Voltar para Secretaria
WHERE id_sala = 14 
AND id IN (SELECT id FROM tabela_patrimonio WHERE id_sala = 6 BEFORE UPDATE);

COMMIT;
*/