-- Script para criação da TABELA_SALA
-- Sistema de Inventário IFMT

CREATE TABLE IF NOT EXISTS TABELA_SALA (
    ID_SALA SERIAL PRIMARY KEY,
    DESCRICAO VARCHAR(255) NOT NULL,
    NUMERO_SALA VARCHAR(20),
    ANDAR INTEGER,
    BLOCO VARCHAR(10),
    ID_SETOR INTEGER,
    CAPACIDADE INTEGER,
    AREA_M2 DECIMAL(8,2),
    TIPO_SALA VARCHAR(50),
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    OBSERVACOES TEXT,
    FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID)
);

-- Índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_sala_numero ON TABELA_SALA(NUMERO_SALA);
CREATE INDEX IF NOT EXISTS idx_sala_setor ON TABELA_SALA(ID_SETOR);
CREATE INDEX IF NOT EXISTS idx_sala_ativo ON TABELA_SALA(ATIVO);
CREATE INDEX IF NOT EXISTS idx_sala_bloco ON TABELA_SALA(BLOCO);
CREATE INDEX IF NOT EXISTS idx_sala_andar ON TABELA_SALA(ANDAR);

-- Comentários
COMMENT ON TABLE TABELA_SALA IS 'Tabela de salas/locais onde ficam os patrimônios';
COMMENT ON COLUMN TABELA_SALA.ID_SALA IS 'Identificador único da sala';
COMMENT ON COLUMN TABELA_SALA.DESCRICAO IS 'Descrição da sala ou local';
COMMENT ON COLUMN TABELA_SALA.NUMERO_SALA IS 'Número identificador da sala';
COMMENT ON COLUMN TABELA_SALA.ANDAR IS 'Andar onde fica a sala';
COMMENT ON COLUMN TABELA_SALA.BLOCO IS 'Bloco do prédio';
COMMENT ON COLUMN TABELA_SALA.ID_SETOR IS 'Setor responsável pela sala';
COMMENT ON COLUMN TABELA_SALA.CAPACIDADE IS 'Capacidade de pessoas na sala';
COMMENT ON COLUMN TABELA_SALA.AREA_M2 IS 'Área da sala em metros quadrados';
COMMENT ON COLUMN TABELA_SALA.TIPO_SALA IS 'Tipo da sala (ADMINISTRATIVA, LABORATORIO, AULA, DEPOSITO)';
COMMENT ON COLUMN TABELA_SALA.ATIVO IS 'Indica se a sala está ativa';

-- Inserção de dados básicos
INSERT INTO TABELA_SALA (DESCRICAO, NUMERO_SALA, ANDAR, BLOCO, TIPO_SALA, CAPACIDADE) VALUES 
('Sala de Administração', 'ADM-01', 1, 'A', 'ADMINISTRATIVA', 10),
('Sala de TI', 'TI-01', 1, 'A', 'ADMINISTRATIVA', 8),
('Biblioteca Principal', 'BIB-01', 1, 'B', 'BIBLIOTECA', 100),
('Laboratório de Informática 1', 'LAB-01', 2, 'C', 'LABORATORIO', 30),
('Laboratório de Informática 2', 'LAB-02', 2, 'C', 'LABORATORIO', 30),
('Laboratório de Química', 'LAB-03', 2, 'C', 'LABORATORIO', 25),
('Laboratório de Física', 'LAB-04', 2, 'C', 'LABORATORIO', 25),
('Almoxarifado Central', 'ALM-01', 0, 'A', 'DEPOSITO', 5),
('Sala de Aula 101', 'SA-101', 1, 'D', 'AULA', 40),
('Sala de Aula 102', 'SA-102', 1, 'D', 'AULA', 40),
('Sala de Reuniões', 'REU-01', 1, 'A', 'ADMINISTRATIVA', 12),
('Auditório', 'AUD-01', 1, 'E', 'AUDITORIO', 200),
('Coordenação de Ensino', 'CENS-01', 1, 'A', 'ADMINISTRATIVA', 6),
('Secretaria', 'SEC-01', 1, 'A', 'ADMINISTRATIVA', 8),
('Diretoria', 'DIR-01', 1, 'A', 'ADMINISTRATIVA', 4)
ON CONFLICT DO NOTHING;

PRINT 'Tabela SALA criada com sucesso!';