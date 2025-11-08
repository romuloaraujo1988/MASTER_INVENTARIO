-- Script para criação da tabela TABELA_QR_CODE
-- Sistema de Inventário IFMT - Módulo QR Code
-- Data: 2024

-- Criar tabela para armazenar QR Codes dos patrimônios
CREATE TABLE IF NOT EXISTS TABELA_QR_CODE (
    ID SERIAL PRIMARY KEY,
    ID_PATRIMONIO INTEGER NOT NULL,
    CODIGO_QR TEXT NOT NULL,
    HASH_DADOS VARCHAR(64), -- SHA-256 dos dados para validação de integridade
    DATA_GERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FORMATO VARCHAR(10) DEFAULT 'PNG',
    TAMANHO INTEGER DEFAULT 200,
    ATIVO BOOLEAN DEFAULT TRUE,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Chave estrangeira
    FOREIGN KEY (ID_PATRIMONIO) REFERENCES TABELA_PATRIMONIO(ID) ON DELETE CASCADE
);

-- Índices para otimização
-- Apenas um QR Code ativo por patrimônio
CREATE UNIQUE INDEX IF NOT EXISTS idx_qr_patrimonio_ativo 
ON TABELA_QR_CODE(ID_PATRIMONIO) 
WHERE ATIVO = TRUE;

-- Índice para busca por hash (validação)
CREATE INDEX IF NOT EXISTS idx_qr_hash 
ON TABELA_QR_CODE(HASH_DADOS);

-- Índice para busca por data de geração
CREATE INDEX IF NOT EXISTS idx_qr_data_geracao 
ON TABELA_QR_CODE(DATA_GERACAO);

-- Índice para patrimônios (busca geral)
CREATE INDEX IF NOT EXISTS idx_qr_patrimonio 
ON TABELA_QR_CODE(ID_PATRIMONIO);

-- Comentários para documentação
COMMENT ON TABLE TABELA_QR_CODE IS 'Armazena QR Codes gerados para patrimônios';
COMMENT ON COLUMN TABELA_QR_CODE.ID IS 'Chave primária';
COMMENT ON COLUMN TABELA_QR_CODE.ID_PATRIMONIO IS 'Referência ao patrimônio';
COMMENT ON COLUMN TABELA_QR_CODE.CODIGO_QR IS 'Dados codificados no QR Code (JSON ou string)';
COMMENT ON COLUMN TABELA_QR_CODE.HASH_DADOS IS 'Hash SHA-256 dos dados para validação de integridade';
COMMENT ON COLUMN TABELA_QR_CODE.DATA_GERACAO IS 'Data e hora de geração do QR Code';
COMMENT ON COLUMN TABELA_QR_CODE.FORMATO IS 'Formato da imagem (PNG, JPG, etc.)';
COMMENT ON COLUMN TABELA_QR_CODE.TAMANHO IS 'Tamanho da imagem em pixels';
COMMENT ON COLUMN TABELA_QR_CODE.ATIVO IS 'Indica se o QR Code está ativo (apenas um por patrimônio)';

-- Trigger para atualizar UPDATED_AT
CREATE OR REPLACE FUNCTION update_qr_code_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.UPDATED_AT = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_qr_code_timestamp
    BEFORE UPDATE ON TABELA_QR_CODE
    FOR EACH ROW
    EXECUTE FUNCTION update_qr_code_timestamp();

-- Verificar se a tabela foi criada
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'tabela_qr_code'
ORDER BY ordinal_position;

-- Verificar índices criados
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'tabela_qr_code';

PRINT 'Tabela TABELA_QR_CODE criada com sucesso!';
PRINT 'Índices criados para otimização de consultas';
PRINT 'Triggers configurados para auditoria';