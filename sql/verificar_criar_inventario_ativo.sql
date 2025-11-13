-- Script para verificar e criar inventário ativo
-- Data: 12/11/2025

-- 1. Verificar inventários existentes
SELECT 
    id,
    nome,
    status_inventario,
    data_inicio,
    data_fim,
    percentual_conclusao
FROM tabela_inventario
ORDER BY id DESC;

-- 2. Verificar se existe inventário ativo
SELECT COUNT(*) as total_ativos
FROM tabela_inventario
WHERE status_inventario = 'EM_ANDAMENTO';

-- 3. Se não houver inventário ativo, criar um
-- DESCOMENTE AS LINHAS ABAIXO PARA CRIAR UM INVENTÁRIO ATIVO

/*
INSERT INTO tabela_inventario (
    nome,
    data_inicio,
    data_fim,
    status_inventario,
    responsavel_inventario,
    percentual_conclusao,
    ano
) VALUES (
    'Inventário Teste 2025',
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '30 days',
    'EM_ANDAMENTO',
    'Administrador',
    0.00,
    EXTRACT(YEAR FROM CURRENT_DATE)
);
*/

-- 4. Ou atualizar um inventário existente para EM_ANDAMENTO
-- DESCOMENTE E AJUSTE O ID ABAIXO

/*
UPDATE tabela_inventario
SET status_inventario = 'EM_ANDAMENTO'
WHERE id = 1; -- AJUSTE O ID CONFORME NECESSÁRIO
*/

-- 5. Verificar novamente
SELECT 
    id,
    nome,
    status_inventario,
    data_inicio
FROM tabela_inventario
WHERE status_inventario = 'EM_ANDAMENTO';
