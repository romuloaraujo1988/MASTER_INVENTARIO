-- ========================================
-- Script para Diagnosticar e Corrigir Responsáveis
-- ========================================

-- 1. Verificar estrutura da tabela
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'tabela_responsavel'
ORDER BY ordinal_position;

-- 2. Verificar todos os responsáveis (com e sem filtro)
SELECT 
    'Total de responsáveis' as tipo,
    COUNT(*) as quantidade
FROM TABELA_RESPONSAVEL
UNION ALL
SELECT 
    'Responsáveis ATIVOS' as tipo,
    COUNT(*) as quantidade
FROM TABELA_RESPONSAVEL
WHERE ATIVO = TRUE
UNION ALL
SELECT 
    'Responsáveis INATIVOS' as tipo,
    COUNT(*) as quantidade
FROM TABELA_RESPONSAVEL
WHERE ATIVO = FALSE OR ATIVO IS NULL;

-- 3. Listar todos os responsáveis com detalhes
SELECT 
    ID,
    NOME,
    CPF,
    EMAIL,
    TELEFONE,
    CARGO,
    ID_SETOR,
    ATIVO,
    DATA_CADASTRO
FROM TABELA_RESPONSAVEL
ORDER BY NOME;

-- 4. Verificar se coluna ATIVO existe e tem valores corretos
SELECT 
    ATIVO,
    COUNT(*) as quantidade
FROM TABELA_RESPONSAVEL
GROUP BY ATIVO;

-- ========================================
-- CORREÇÕES (descomente se necessário)
-- ========================================

-- Adicionar coluna ATIVO se não existir
-- ALTER TABLE TABELA_RESPONSAVEL 
-- ADD COLUMN IF NOT EXISTS ATIVO BOOLEAN DEFAULT TRUE;

-- Atualizar registros NULL para TRUE
-- UPDATE TABELA_RESPONSAVEL 
-- SET ATIVO = TRUE 
-- WHERE ATIVO IS NULL;

-- Ativar todos os responsáveis
-- UPDATE TABELA_RESPONSAVEL 
-- SET ATIVO = TRUE;

-- Inserir responsáveis de teste (se tabela estiver vazia)
/*
INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, TELEFONE, CARGO, ATIVO, DATA_CADASTRO)
VALUES 
    ('João Silva', '123.456.789-00', 'joao.silva@ifmt.edu.br', '(65) 3333-4444', 'Administrador', TRUE, NOW()),
    ('Maria Santos', '987.654.321-00', 'maria.santos@ifmt.edu.br', '(65) 3333-5555', 'Coordenadora TI', TRUE, NOW()),
    ('Pedro Oliveira', '111.222.333-44', 'pedro.oliveira@ifmt.edu.br', '(65) 3333-6666', 'Gerente Financeiro', TRUE, NOW()),
    ('Ana Costa', '555.666.777-88', 'ana.costa@ifmt.edu.br', '(65) 3333-7777', 'Supervisora RH', TRUE, NOW()),
    ('Carlos Ferreira', '999.888.777-66', 'carlos.ferreira@ifmt.edu.br', '(65) 3333-8888', 'Responsável Almoxarifado', TRUE, NOW())
ON CONFLICT (CPF) DO NOTHING;
*/

-- Verificar resultado após correções
SELECT 
    ID,
    NOME,
    ATIVO
FROM TABELA_RESPONSAVEL
WHERE ATIVO = TRUE
ORDER BY NOME;
