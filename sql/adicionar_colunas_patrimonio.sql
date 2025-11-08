-- Script para adicionar colunas faltantes na tabela TABELA_PATRIMONIO
-- Execute este script no banco de dados PostgreSQL

-- Adicionar coluna SITUACAO
ALTER TABLE tabela_patrimonio 
ADD COLUMN situacao VARCHAR(20) DEFAULT 'ATIVO';

-- Adicionar coluna DESCRICAO_RESUMIDA
ALTER TABLE tabela_patrimonio 
ADD COLUMN descricao_resumida TEXT;

-- Atualizar registros existentes com situacao ATIVO
UPDATE tabela_patrimonio 
SET situacao = 'ATIVO' 
WHERE situacao IS NULL;

-- Comentários:
-- A coluna SITUACAO é necessária para controlar o status dos patrimônios (ATIVO/INATIVO)
-- A coluna DESCRICAO_RESUMIDA é usada para armazenar uma versão resumida da descrição
-- Ambas as colunas são referenciadas no código Java mas não existem na estrutura atual da tabela