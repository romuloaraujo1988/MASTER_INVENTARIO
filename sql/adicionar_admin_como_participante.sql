-- Script para adicionar usuário ADMIN como participante do inventário ativo
-- Execute este script se o usuário ADMIN não conseguir fazer coletas

-- 1. Verificar inventário ativo
SELECT 
    'INVENTÁRIO ATIVO:' as info,
    id,
    nome,
    status_inventario,
    data_inicio
FROM tabela_inventario 
WHERE status_inventario = 'EM_ANDAMENTO';

-- 2. Verificar usuário ADMIN
SELECT 
    'USUÁRIO ADMIN:' as info,
    id,
    nome_completo,
    login,
    perfil,
    ativo
FROM tabela_usuario 
WHERE perfil = 'ADMIN' AND ativo = TRUE;

-- 3. Verificar se ADMIN já é participante
SELECT 
    'PARTICIPAÇÃO ATUAL DO ADMIN:' as info,
    pi.id_participante,
    pi.id_inventario,
    pi.id_usuario,
    pi.papel,
    pi.ativo,
    u.nome_completo,
    i.nome as inventario
FROM tabela_participante_inventario pi
JOIN tabela_usuario u ON pi.id_usuario = u.id
JOIN tabela_inventario i ON pi.id_inventario = i.id
WHERE u.perfil = 'ADMIN'
AND i.status_inventario = 'EM_ANDAMENTO';

-- 4. Adicionar ADMIN como COORDENADOR se não existir
-- ATENÇÃO: Ajuste os IDs conforme seu ambiente!

INSERT INTO tabela_participante_inventario 
    (id_inventario, id_usuario, papel, ativo, observacoes)
SELECT 
    i.id as id_inventario,
    u.id as id_usuario,
    'COORDENADOR' as papel,
    TRUE as ativo,
    'Admin adicionado automaticamente como coordenador' as observacoes
FROM 
    tabela_inventario i,
    tabela_usuario u
WHERE 
    i.status_inventario = 'EM_ANDAMENTO'
    AND u.perfil = 'ADMIN'
    AND u.ativo = TRUE
    AND NOT EXISTS (
        SELECT 1 
        FROM tabela_participante_inventario pi
        WHERE pi.id_inventario = i.id
        AND pi.id_usuario = u.id
    )
LIMIT 1;

-- 5. Verificar se foi adicionado
SELECT 
    'VERIFICAÇÃO FINAL:' as info,
    pi.id_participante,
    pi.id_inventario,
    pi.id_usuario,
    pi.papel,
    pi.ativo,
    u.nome_completo,
    u.perfil,
    i.nome as inventario
FROM tabela_participante_inventario pi
JOIN tabela_usuario u ON pi.id_usuario = u.id
JOIN tabela_inventario i ON pi.id_inventario = i.id
WHERE u.perfil = 'ADMIN'
AND i.status_inventario = 'EM_ANDAMENTO'
AND pi.ativo = TRUE;

-- 6. Teste da query que o DAO usa
SELECT 
    'TESTE DA QUERY DO DAO:' as info,
    id_participante
FROM tabela_participante_inventario
WHERE id_inventario = (SELECT id FROM tabela_inventario WHERE status_inventario = 'EM_ANDAMENTO' LIMIT 1)
AND id_usuario = (SELECT id FROM tabela_usuario WHERE perfil = 'ADMIN' AND ativo = TRUE LIMIT 1)
AND ativo = TRUE;

SELECT 'Script executado com sucesso!' as resultado;
