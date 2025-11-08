-- SCRIPT URGENTE: Corrigir participante que não está sendo encontrado
-- Execute este script AGORA para resolver o problema

-- ============================================================================
-- PASSO 1: IDENTIFICAR O PROBLEMA
-- ============================================================================

-- Verificar inventário ativo
SELECT 
    '=== INVENTÁRIO ATIVO ===' as info,
    id,
    nome,
    status_inventario
FROM tabela_inventario 
WHERE status_inventario = 'EM_ANDAMENTO';

-- Verificar usuário que está tentando coletar
SELECT 
    '=== USUÁRIO LOGADO ===' as info,
    id,
    nome_completo,
    login,
    perfil,
    ativo
FROM tabela_usuario 
WHERE ativo = TRUE
ORDER BY id
LIMIT 5;

-- Verificar se o participante existe (SEM filtro de ativo)
SELECT 
    '=== PARTICIPANTE EXISTE? ===' as info,
    id_participante,
    id_inventario,
    id_usuario,
    papel,
    ativo,
    data_inclusao,
    data_remocao
FROM tabela_participante_inventario
WHERE id_inventario = (SELECT id FROM tabela_inventario WHERE status_inventario = 'EM_ANDAMENTO' LIMIT 1)
AND id_usuario IN (SELECT id FROM tabela_usuario WHERE ativo = TRUE LIMIT 5);

-- ============================================================================
-- PASSO 2: TESTAR A QUERY EXATA DO DAO
-- ============================================================================

-- Query exata que o DAO usa (ajuste os IDs conforme seu caso)
-- Substitua 2 e 1 pelos valores do seu log
SELECT 
    '=== TESTE QUERY DO DAO ===' as info,
    id_participante 
FROM tabela_participante_inventario 
WHERE id_inventario = 2  -- ← AJUSTE ESTE VALOR
AND id_usuario = 1        -- ← AJUSTE ESTE VALOR
AND ativo = TRUE;

-- ============================================================================
-- PASSO 3: VERIFICAR TIPO DO CAMPO ATIVO
-- ============================================================================

SELECT 
    '=== TIPO DO CAMPO ATIVO ===' as info,
    data_type,
    udt_name
FROM information_schema.columns
WHERE table_name = 'tabela_participante_inventario'
AND column_name = 'ativo';

-- ============================================================================
-- PASSO 4: TESTAR COM DIFERENTES VARIAÇÕES
-- ============================================================================

-- Teste 1: Com TRUE em maiúsculo
SELECT '=== TESTE 1: TRUE maiúsculo ===' as teste;
SELECT id_participante FROM tabela_participante_inventario 
WHERE id_inventario = 2 AND id_usuario = 1 AND ativo = TRUE;

-- Teste 2: Com true em minúsculo
SELECT '=== TESTE 2: true minúsculo ===' as teste;
SELECT id_participante FROM tabela_participante_inventario 
WHERE id_inventario = 2 AND id_usuario = 1 AND ativo = true;

-- Teste 3: Com 't' (formato PostgreSQL)
SELECT '=== TESTE 3: t (PostgreSQL) ===' as teste;
SELECT id_participante FROM tabela_participante_inventario 
WHERE id_inventario = 2 AND id_usuario = 1 AND ativo = 't';

-- Teste 4: Com 1 (numérico)
SELECT '=== TESTE 4: 1 (numérico) ===' as teste;
SELECT id_participante FROM tabela_participante_inventario 
WHERE id_inventario = 2 AND id_usuario = 1 AND ativo = 1;

-- Teste 5: Sem filtro de ativo
SELECT '=== TESTE 5: SEM filtro ativo ===' as teste;
SELECT id_participante, ativo FROM tabela_participante_inventario 
WHERE id_inventario = 2 AND id_usuario = 1;

-- ============================================================================
-- PASSO 5: CORREÇÃO AUTOMÁTICA
-- ============================================================================

-- Se o registro existe mas está inativo, reativar
UPDATE tabela_participante_inventario
SET ativo = TRUE,
    data_remocao = NULL,
    data_ultima_atualizacao = CURRENT_TIMESTAMP
WHERE id_inventario = (SELECT id FROM tabela_inventario WHERE status_inventario = 'EM_ANDAMENTO' LIMIT 1)
AND id_usuario IN (SELECT id FROM tabela_usuario WHERE ativo = TRUE)
AND ativo = FALSE;

SELECT '=== REGISTROS REATIVADOS ===' as info, ROW_COUNT() as total;

-- Se o registro não existe, criar
INSERT INTO tabela_participante_inventario 
    (id_inventario, id_usuario, papel, ativo, observacoes)
SELECT 
    i.id,
    u.id,
    CASE 
        WHEN u.perfil = 'ADMIN' THEN 'COORDENADOR'
        ELSE 'COLETOR'
    END,
    TRUE,
    'Adicionado automaticamente via script de correção'
FROM 
    tabela_inventario i,
    tabela_usuario u
WHERE 
    i.status_inventario = 'EM_ANDAMENTO'
    AND u.ativo = TRUE
    AND NOT EXISTS (
        SELECT 1 
        FROM tabela_participante_inventario pi
        WHERE pi.id_inventario = i.id
        AND pi.id_usuario = u.id
    );

SELECT '=== REGISTROS CRIADOS ===' as info, ROW_COUNT() as total;

-- ============================================================================
-- PASSO 6: VALIDAÇÃO FINAL
-- ============================================================================

-- Verificar todos os participantes ativos agora
SELECT 
    '=== PARTICIPANTES ATIVOS AGORA ===' as info,
    pi.id_participante,
    pi.id_inventario,
    i.nome as inventario,
    pi.id_usuario,
    u.nome_completo as usuario,
    u.perfil,
    pi.papel,
    pi.ativo
FROM tabela_participante_inventario pi
JOIN tabela_inventario i ON pi.id_inventario = i.id
JOIN tabela_usuario u ON pi.id_usuario = u.id
WHERE i.status_inventario = 'EM_ANDAMENTO'
AND pi.ativo = TRUE
ORDER BY u.nome_completo;

-- Testar query do DAO novamente
SELECT 
    '=== TESTE FINAL DA QUERY DO DAO ===' as info,
    id_participante 
FROM tabela_participante_inventario 
WHERE id_inventario = (SELECT id FROM tabela_inventario WHERE status_inventario = 'EM_ANDAMENTO' LIMIT 1)
AND id_usuario = (SELECT id FROM tabela_usuario WHERE ativo = TRUE LIMIT 1)
AND ativo = TRUE;

SELECT '========================================' as resultado;
SELECT 'SCRIPT EXECUTADO COM SUCESSO!' as resultado;
SELECT 'Agora recompile e teste o aplicativo' as resultado;
SELECT '========================================' as resultado;
