-- Script para testar a query que busca participante
-- Use os valores do log: idInventario=2, idUsuario=1

-- Teste 1: Query exata que o DAO usa (minúsculo)
SELECT 
    'TESTE 1: Query do DAO (minúsculo)' as teste,
    id_participante 
FROM tabela_participante_inventario 
WHERE id_inventario = 2 
AND id_usuario = 1 
AND ativo = TRUE;

-- Teste 2: Query com maiúsculas
SELECT 
    'TESTE 2: Query com maiúsculas' as teste,
    ID_PARTICIPANTE 
FROM TABELA_PARTICIPANTE_INVENTARIO 
WHERE ID_INVENTARIO = 2 
AND ID_USUARIO = 1 
AND ATIVO = TRUE;

-- Teste 3: Verificar se o registro existe (sem filtro de ativo)
SELECT 
    'TESTE 3: Registro existe?' as teste,
    id_participante,
    id_inventario,
    id_usuario,
    papel,
    ativo,
    data_inclusao
FROM tabela_participante_inventario 
WHERE id_inventario = 2 
AND id_usuario = 1;

-- Teste 4: Verificar todos os participantes do inventário 2
SELECT 
    'TESTE 4: Todos participantes do inventário 2' as teste,
    pi.id_participante,
    pi.id_usuario,
    u.nome_completo,
    u.perfil,
    pi.papel,
    pi.ativo
FROM tabela_participante_inventario pi
JOIN tabela_usuario u ON pi.id_usuario = u.id
WHERE pi.id_inventario = 2
ORDER BY pi.ativo DESC, u.nome_completo;

-- Teste 5: Verificar estrutura da tabela
SELECT 
    'TESTE 5: Estrutura da tabela' as teste,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'tabela_participante_inventario'
ORDER BY ordinal_position;

-- Teste 6: Verificar se o tipo boolean está correto
SELECT 
    'TESTE 6: Tipo do campo ATIVO' as teste,
    data_type
FROM information_schema.columns
WHERE table_name = 'tabela_participante_inventario'
AND column_name = 'ativo';

-- Teste 7: Testar com cast explícito
SELECT 
    'TESTE 7: Com cast explícito' as teste,
    id_participante 
FROM tabela_participante_inventario 
WHERE id_inventario = 2 
AND id_usuario = 1 
AND ativo = TRUE::boolean;

-- Teste 8: Verificar o usuário 1
SELECT 
    'TESTE 8: Dados do usuário 1' as teste,
    id,
    nome_completo,
    login,
    perfil,
    ativo
FROM tabela_usuario
WHERE id = 1;

-- Teste 9: Verificar o inventário 2
SELECT 
    'TESTE 9: Dados do inventário 2' as teste,
    id,
    nome,
    status_inventario,
    data_inicio
FROM tabela_inventario
WHERE id = 2;
