-- Script para verificar e corrigir responsáveis

-- 1. Verificar quantos responsáveis existem
SELECT 'Total de responsáveis' as info, COUNT(*) as quantidade 
FROM TABELA_RESPONSAVEL;

-- 2. Verificar quantos estão ativos
SELECT 'Responsáveis ativos' as info, COUNT(*) as quantidade 
FROM TABELA_RESPONSAVEL 
WHERE ATIVO = TRUE;

-- 3. Verificar quantos estão inativos
SELECT 'Responsáveis inativos' as info, COUNT(*) as quantidade 
FROM TABELA_RESPONSAVEL 
WHERE ATIVO = FALSE OR ATIVO IS NULL;

-- 4. Listar todos os responsáveis com seu status
SELECT ID, NOME, CPF, EMAIL, CARGO, ATIVO, DATA_CADASTRO
FROM TABELA_RESPONSAVEL
ORDER BY NOME;

-- 5. Se não houver responsáveis ativos, ativar todos
-- DESCOMENTE A LINHA ABAIXO PARA EXECUTAR:
-- UPDATE TABELA_RESPONSAVEL SET ATIVO = TRUE WHERE ATIVO = FALSE OR ATIVO IS NULL;

-- 6. Se não houver responsáveis, inserir alguns exemplos
-- DESCOMENTE AS LINHAS ABAIXO PARA EXECUTAR:
/*
INSERT INTO TABELA_RESPONSAVEL (NOME, CPF, EMAIL, TELEFONE, CARGO, ATIVO) 
VALUES 
('João Silva', '123.456.789-00', 'joao.silva@ifmt.edu.br', '(65) 3333-4444', 'Administrador', TRUE),
('Maria Santos', '987.654.321-00', 'maria.santos@ifmt.edu.br', '(65) 3333-5555', 'Coordenadora TI', TRUE),
('Pedro Oliveira', '111.222.333-44', 'pedro.oliveira@ifmt.edu.br', '(65) 3333-6666', 'Gerente Financeiro', TRUE),
('Ana Costa', '555.666.777-88', 'ana.costa@ifmt.edu.br', '(65) 3333-7777', 'Supervisora RH', TRUE),
('Carlos Ferreira', '999.888.777-66', 'carlos.ferreira@ifmt.edu.br', '(65) 3333-8888', 'Responsável Almoxarifado', TRUE);
*/

-- 7. Verificar novamente após as correções
SELECT 'Responsáveis ativos (após correção)' as info, COUNT(*) as quantidade 
FROM TABELA_RESPONSAVEL 
WHERE ATIVO = TRUE;
