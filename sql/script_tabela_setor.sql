-- Script para criação da TABELA_SETOR
-- Sistema de Inventário IFMT

CREATE TABLE IF NOT EXISTS TABELA_SETOR (
    ID SERIAL PRIMARY KEY,
    NOME VARCHAR(255) NOT NULL UNIQUE,
    DESCRICAO TEXT,
    RESPONSAVEL_SETOR VARCHAR(255),
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_setor_nome ON TABELA_SETOR(NOME);
CREATE INDEX IF NOT EXISTS idx_setor_ativo ON TABELA_SETOR(ATIVO);

-- Comentários
COMMENT ON TABLE TABELA_SETOR IS 'Tabela de setores da instituição';
COMMENT ON COLUMN TABELA_SETOR.NOME IS 'Nome do setor';
COMMENT ON COLUMN TABELA_SETOR.DESCRICAO IS 'Descrição detalhada do setor';
COMMENT ON COLUMN TABELA_SETOR.RESPONSAVEL_SETOR IS 'Responsável pelo setor';
COMMENT ON COLUMN TABELA_SETOR.ATIVO IS 'Indica se o setor está ativo';

-- Inserção de dados básicos
INSERT INTO TABELA_SETOR (NOME, DESCRICAO) VALUES 
('ADMINISTRAÇÃO', 'Setor administrativo da instituição'),
('TECNOLOGIA DA INFORMAÇÃO', 'Setor de TI e suporte técnico'),
('ENSINO', 'Setor de coordenação de ensino'),
('BIBLIOTECA', 'Biblioteca da instituição'),
('LABORATÓRIOS', 'Laboratórios de ensino e pesquisa'),
('ALMOXARIFADO', 'Setor de almoxarifado e estoque'),
('MANUTENÇÃO', 'Setor de manutenção predial'),
('RECURSOS HUMANOS', 'Setor de gestão de pessoas')
ON CONFLICT (NOME) DO NOTHING;

-- Tabela SETOR criada com sucesso!