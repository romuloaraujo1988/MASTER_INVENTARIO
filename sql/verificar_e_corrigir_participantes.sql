-- Script para Verificar e Corrigir Participantes do Inventário
-- Sistema de Inventário IFMT
-- Data: 28/10/2025

-- ============================================================================
-- PARTE 1: VERIFICAÇÕES
-- ============================================================================

-- 1. Verificar inventário ativo
SELECT 
    '=== INVENTÁRIO ATIVO ===' as SECAO,
    ID,
    NOME,
    STATUS_INVENTARIO,
    DATA_INICIO,
    DATA_FIM_PREVISTA
FROM TABELA_INVENTARIO 
WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO';

-- 2. Verificar participantes do inventário ativo
SELECT 
    '=== PARTICIPANTES ATIVOS ===' as SECAO,
    pi.ID,
    pi.ID_INVENTARIO,
    i.NOME as INVENTARIO,
    pi.ID_USUARIO,
    u.NOME_COMPLETO as USUARIO,
    u.PERFIL as PERFIL_USUARIO,
    pi.PAPEL as PAPEL_INVENTARIO,
    pi.ATIVO,
    pi.DATA_INCLUSAO,
    pi.OBSERVACOES
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID
WHERE i.STATUS_INVENTARIO = 'EM_ANDAMENTO'
ORDER BY pi.PAPEL, u.NOME_COMPLETO;

-- 3. Verificar usuários que NÃO são participantes
SELECT 
    '=== USUÁRIOS NÃO PARTICIPANTES ===' as SECAO,
    u.ID,
    u.NOME_COMPLETO,
    u.PERFIL,
    u.ATIVO
FROM TABELA_USUARIO u
WHERE u.ATIVO = TRUE
AND NOT EXISTS (
    SELECT 1 FROM TABELA_PARTICIPANTE_INVENTARIO pi
    JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
    WHERE pi.ID_USUARIO = u.ID
    AND pi.ATIVO = TRUE
    AND i.STATUS_INVENTARIO = 'EM_ANDAMENTO'
)
ORDER BY u.PERFIL, u.NOME_COMPLETO;

-- 4. Verificar participantes inativos que podem ser reativados
SELECT 
    '=== PARTICIPANTES INATIVOS ===' as SECAO,
    pi.ID,
    u.NOME_COMPLETO,
    u.PERFIL,
    pi.PAPEL,
    pi.DATA_REMOCAO,
    pi.OBSERVACOES
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID
WHERE i.STATUS_INVENTARIO = 'EM_ANDAMENTO'
AND pi.ATIVO = FALSE
ORDER BY pi.DATA_REMOCAO DESC;

-- 5. Contar participantes por papel
SELECT 
    '=== CONTAGEM POR PAPEL ===' as SECAO,
    pi.PAPEL,
    COUNT(*) as TOTAL
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
WHERE i.STATUS_INVENTARIO = 'EM_ANDAMENTO'
AND pi.ATIVO = TRUE
GROUP BY pi.PAPEL
ORDER BY pi.PAPEL;

-- ============================================================================
-- PARTE 2: CORREÇÕES (COMENTADAS - DESCOMENTE PARA EXECUTAR)
-- ============================================================================

-- ATENÇÃO: Revise os comandos abaixo antes de executar!
-- Substitua os valores de exemplo pelos valores reais do seu ambiente.

-- ----------------------------------------------------------------------------
-- OPÇÃO 1: Adicionar usuário específico como COLETOR
-- ----------------------------------------------------------------------------

/*
-- Substitua os valores:
-- @ID_INVENTARIO: ID do inventário ativo (consulte na PARTE 1, item 1)
-- @ID_USUARIO: ID do usuário a ser adicionado (consulte na PARTE 1, item 3)

INSERT INTO TABELA_PARTICIPANTE_INVENTARIO 
    (ID_INVENTARIO, ID_USUARIO, PAPEL, ATIVO, OBSERVACOES)
VALUES 
    (1, 2, 'COLETOR', TRUE, 'Adicionado via script de correção')
ON CONFLICT (ID_INVENTARIO, ID_USUARIO) 
DO UPDATE SET 
    ATIVO = TRUE,
    DATA_REMOCAO = NULL,
    PAPEL = 'COLETOR',
    DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP,
    OBSERVACOES = 'Reativado via script de correção';
*/

-- ----------------------------------------------------------------------------
-- OPÇÃO 2: Adicionar todos os OPERADORES como COLETORES
-- ----------------------------------------------------------------------------

/*
INSERT INTO TABELA_PARTICIPANTE_INVENTARIO 
    (ID_INVENTARIO, ID_USUARIO, PAPEL, ATIVO, OBSERVACOES)
SELECT 
    (SELECT ID FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' LIMIT 1) as ID_INVENTARIO,
    u.ID as ID_USUARIO,
    'COLETOR' as PAPEL,
    TRUE as ATIVO,
    'Adicionado automaticamente - Perfil OPERADOR' as OBSERVACOES
FROM TABELA_USUARIO u
WHERE u.PERFIL = 'OPERADOR'
AND u.ATIVO = TRUE
AND NOT EXISTS (
    SELECT 1 FROM TABELA_PARTICIPANTE_INVENTARIO pi
    WHERE pi.ID_USUARIO = u.ID
    AND pi.ID_INVENTARIO = (SELECT ID FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' LIMIT 1)
);
*/

-- ----------------------------------------------------------------------------
-- OPÇÃO 3: Reativar participante específico
-- ----------------------------------------------------------------------------

/*
-- Substitua os valores:
-- @ID_INVENTARIO: ID do inventário ativo
-- @ID_USUARIO: ID do usuário a ser reativado

UPDATE TABELA_PARTICIPANTE_INVENTARIO
SET 
    ATIVO = TRUE,
    DATA_REMOCAO = NULL,
    DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP,
    OBSERVACOES = COALESCE(OBSERVACOES || ' | ', '') || 'Reativado em ' || CURRENT_TIMESTAMP
WHERE ID_INVENTARIO = 1
AND ID_USUARIO = 2
AND ATIVO = FALSE;
*/

-- ----------------------------------------------------------------------------
-- OPÇÃO 4: Alterar papel de um participante
-- ----------------------------------------------------------------------------

/*
-- Substitua os valores:
-- @ID_INVENTARIO: ID do inventário ativo
-- @ID_USUARIO: ID do usuário
-- @NOVO_PAPEL: 'COORDENADOR', 'COLETOR' ou 'OBSERVADOR'

UPDATE TABELA_PARTICIPANTE_INVENTARIO
SET 
    PAPEL = 'COLETOR',
    DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP,
    OBSERVACOES = COALESCE(OBSERVACOES || ' | ', '') || 'Papel alterado em ' || CURRENT_TIMESTAMP
WHERE ID_INVENTARIO = 1
AND ID_USUARIO = 2
AND ATIVO = TRUE;
*/

-- ----------------------------------------------------------------------------
-- OPÇÃO 5: Remover participante (soft delete)
-- ----------------------------------------------------------------------------

/*
-- Substitua os valores:
-- @ID_INVENTARIO: ID do inventário ativo
-- @ID_USUARIO: ID do usuário a ser removido

UPDATE TABELA_PARTICIPANTE_INVENTARIO
SET 
    ATIVO = FALSE,
    DATA_REMOCAO = CURRENT_TIMESTAMP,
    DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP,
    OBSERVACOES = COALESCE(OBSERVACOES || ' | ', '') || 'Removido em ' || CURRENT_TIMESTAMP
WHERE ID_INVENTARIO = 1
AND ID_USUARIO = 2
AND ATIVO = TRUE;
*/

-- ============================================================================
-- PARTE 3: VALIDAÇÕES PÓS-CORREÇÃO
-- ============================================================================

-- Executar após fazer as correções para validar

-- 1. Verificar se o usuário foi adicionado/reativado
/*
SELECT 
    '=== VALIDAÇÃO: USUÁRIO ADICIONADO ===' as SECAO,
    pi.*,
    u.NOME_COMPLETO,
    u.PERFIL
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID
WHERE pi.ID_INVENTARIO = 1  -- Substitua pelo ID do inventário
AND pi.ID_USUARIO = 2        -- Substitua pelo ID do usuário
AND pi.ATIVO = TRUE;
*/

-- 2. Verificar total de participantes ativos
/*
SELECT 
    '=== VALIDAÇÃO: TOTAL DE PARTICIPANTES ===' as SECAO,
    COUNT(*) as TOTAL_PARTICIPANTES,
    SUM(CASE WHEN PAPEL = 'COORDENADOR' THEN 1 ELSE 0 END) as COORDENADORES,
    SUM(CASE WHEN PAPEL = 'COLETOR' THEN 1 ELSE 0 END) as COLETORES,
    SUM(CASE WHEN PAPEL = 'OBSERVADOR' THEN 1 ELSE 0 END) as OBSERVADORES
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
WHERE i.STATUS_INVENTARIO = 'EM_ANDAMENTO'
AND pi.ATIVO = TRUE;
*/

-- ============================================================================
-- PARTE 4: QUERIES ÚTEIS PARA DIAGNÓSTICO
-- ============================================================================

-- Verificar estrutura da tabela
SELECT 
    '=== ESTRUTURA DA TABELA ===' as SECAO,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'tabela_participante_inventario'
ORDER BY ordinal_position;

-- Verificar constraints
SELECT 
    '=== CONSTRAINTS ===' as SECAO,
    constraint_name,
    constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'tabela_participante_inventario';

-- Verificar índices
SELECT 
    '=== ÍNDICES ===' as SECAO,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'tabela_participante_inventario';

-- ============================================================================
-- NOTAS IMPORTANTES
-- ============================================================================

/*
1. SEMPRE faça backup antes de executar comandos de UPDATE/INSERT/DELETE
2. Revise os valores antes de descomentar e executar os comandos
3. Execute as verificações (PARTE 1) primeiro para entender o estado atual
4. Execute as correções (PARTE 2) uma de cada vez
5. Valide os resultados (PARTE 3) após cada correção
6. Mantenha registro das alterações realizadas

PAPÉIS DISPONÍVEIS:
- COORDENADOR: Gerencia o inventário (apenas 1 por inventário)
- COLETOR: Realiza coletas de patrimônios
- OBSERVADOR: Visualiza informações (sem permissão de coleta)

REGRAS DE NEGÓCIO:
- Apenas 1 COORDENADOR ativo por inventário
- Múltiplos COLETORES permitidos
- Múltiplos OBSERVADORES permitidos
- Constraint UNIQUE em (ID_INVENTARIO, ID_USUARIO)
- Soft delete (ATIVO = FALSE) em vez de DELETE físico
*/

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================

SELECT '=== SCRIPT EXECUTADO COM SUCESSO ===' as RESULTADO;
SELECT 'Revise os resultados acima e execute as correções necessárias.' as INSTRUCAO;
