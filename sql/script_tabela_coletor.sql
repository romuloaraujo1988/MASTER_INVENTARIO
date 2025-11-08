-- Script para criação da TABELA_COLETOR
-- Sistema de Inventário IFMT

CREATE TABLE IF NOT EXISTS TABELA_COLETOR (
    ID SERIAL PRIMARY KEY,
    NOME_COLETOR VARCHAR(255) NOT NULL,
    CPF VARCHAR(14) UNIQUE,
    EMAIL VARCHAR(255),
    TELEFONE VARCHAR(20),
    MATRICULA VARCHAR(20),
    CARGO VARCHAR(100),
    ID_SETOR INTEGER,
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    OBSERVACOES TEXT,
    FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID)
);

-- Índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_coletor_nome ON TABELA_COLETOR(NOME_COLETOR);
CREATE INDEX IF NOT EXISTS idx_coletor_cpf ON TABELA_COLETOR(CPF);
CREATE INDEX IF NOT EXISTS idx_coletor_matricula ON TABELA_COLETOR(MATRICULA);
CREATE INDEX IF NOT EXISTS idx_coletor_ativo ON TABELA_COLETOR(ATIVO);
CREATE INDEX IF NOT EXISTS idx_coletor_setor ON TABELA_COLETOR(ID_SETOR);

-- Comentários
COMMENT ON TABLE TABELA_COLETOR IS 'Tabela de coletores que realizam o inventário';
COMMENT ON COLUMN TABELA_COLETOR.ID IS 'Identificador único do coletor';
COMMENT ON COLUMN TABELA_COLETOR.NOME_COLETOR IS 'Nome completo do coletor';
COMMENT ON COLUMN TABELA_COLETOR.CPF IS 'CPF do coletor';
COMMENT ON COLUMN TABELA_COLETOR.EMAIL IS 'Email de contato do coletor';
COMMENT ON COLUMN TABELA_COLETOR.TELEFONE IS 'Telefone de contato';
COMMENT ON COLUMN TABELA_COLETOR.MATRICULA IS 'Matrícula funcional do coletor';
COMMENT ON COLUMN TABELA_COLETOR.CARGO IS 'Cargo/função do coletor';
COMMENT ON COLUMN TABELA_COLETOR.ID_SETOR IS 'Setor ao qual o coletor pertence';
COMMENT ON COLUMN TABELA_COLETOR.ATIVO IS 'Indica se o coletor está ativo para coletas';
COMMENT ON COLUMN TABELA_COLETOR.DATA_CADASTRO IS 'Data de cadastro do coletor';
COMMENT ON COLUMN TABELA_COLETOR.OBSERVACOES IS 'Observações sobre o coletor';

-- Inserção de dados básicos
INSERT INTO TABELA_COLETOR (NOME_COLETOR, EMAIL, MATRICULA, CARGO) VALUES 
('COLETOR PRINCIPAL', 'coletor1@ifmt.edu.br', 'COL001', 'Técnico em Patrimônio'),
('COLETOR AUXILIAR', 'coletor2@ifmt.edu.br', 'COL002', 'Assistente Administrativo'),
('COLETOR LABORATÓRIOS', 'coletor.lab@ifmt.edu.br', 'COL003', 'Técnico de Laboratório'),
('COLETOR TI', 'coletor.ti@ifmt.edu.br', 'COL004', 'Técnico em Informática'),
('COLETOR BIBLIOTECA', 'coletor.bib@ifmt.edu.br', 'COL005', 'Bibliotecário'),
('COLETOR GERAL 1', 'coletor.geral1@ifmt.edu.br', 'COL006', 'Servidor Administrativo'),
('COLETOR GERAL 2', 'coletor.geral2@ifmt.edu.br', 'COL007', 'Servidor Administrativo'),
('COLETOR TERCEIRIZADO', 'coletor.terc@ifmt.edu.br', 'COL008', 'Terceirizado')
ON CONFLICT (CPF) DO NOTHING;

PRINT 'Tabela COLETOR criada com sucesso!';