-- Script para criação da tabela TABELA_CAMPUS
-- Sistema de Inventário IFMT
-- Tabela para armazenar informações dos campus da instituição

CREATE TABLE IF NOT EXISTS TABELA_CAMPUS (
    ID SERIAL PRIMARY KEY,
    NOME VARCHAR(255) NOT NULL,
    LOCAL VARCHAR(500) NOT NULL,
    CNPJ VARCHAR(18) NOT NULL UNIQUE,
    DIRETOR VARCHAR(255) NOT NULL,
    CURSOS TEXT,
    TELEFONE VARCHAR(20),
    EMAIL VARCHAR(255),
    OBSERVACOES TEXT,
    ATIVO BOOLEAN NOT NULL DEFAULT TRUE,
    DATA_CRIACAO TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comentários da tabela
COMMENT ON TABLE TABELA_CAMPUS IS 'Tabela para armazenar informações dos campus da instituição';
COMMENT ON COLUMN TABELA_CAMPUS.ID IS 'Identificador único do campus';
COMMENT ON COLUMN TABELA_CAMPUS.NOME IS 'Nome do campus';
COMMENT ON COLUMN TABELA_CAMPUS.LOCAL IS 'Localização/endereço do campus';
COMMENT ON COLUMN TABELA_CAMPUS.CNPJ IS 'CNPJ do campus (único)';
COMMENT ON COLUMN TABELA_CAMPUS.DIRETOR IS 'Nome do diretor do campus';
COMMENT ON COLUMN TABELA_CAMPUS.CURSOS IS 'Lista de cursos oferecidos no campus';
COMMENT ON COLUMN TABELA_CAMPUS.TELEFONE IS 'Telefone de contato do campus';
COMMENT ON COLUMN TABELA_CAMPUS.EMAIL IS 'Email de contato do campus';
COMMENT ON COLUMN TABELA_CAMPUS.OBSERVACOES IS 'Observações gerais sobre o campus';
COMMENT ON COLUMN TABELA_CAMPUS.ATIVO IS 'Indica se o campus está ativo no sistema';
COMMENT ON COLUMN TABELA_CAMPUS.DATA_CRIACAO IS 'Data e hora de criação do registro';

-- Índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_campus_nome ON TABELA_CAMPUS(NOME);
CREATE INDEX IF NOT EXISTS idx_campus_cnpj ON TABELA_CAMPUS(CNPJ);
CREATE INDEX IF NOT EXISTS idx_campus_ativo ON TABELA_CAMPUS(ATIVO);
CREATE INDEX IF NOT EXISTS idx_campus_diretor ON TABELA_CAMPUS(DIRETOR);

-- Inserir campus padrão (exemplo)
INSERT INTO TABELA_CAMPUS (NOME, LOCAL, CNPJ, DIRETOR, CURSOS, TELEFONE, EMAIL, OBSERVACOES) 
VALUES (
    'Campus Cuiabá - Cel. Octayde Jorge da Silva',
    'Rua Senador Metello, 3773 - Jardim Ipê, Cuiabá - MT, 78030-005',
    '10.784.782/0001-12',
    'Diretor do Campus',
    'Cursos Técnicos e Superiores em diversas áreas',
    '(65) 3318-5100',
    'campus.cuiaba@ifmt.edu.br',
    'Campus principal do IFMT em Cuiabá'
) ON CONFLICT (CNPJ) DO NOTHING;

PRINT 'Tabela TABELA_CAMPUS criada com sucesso!';