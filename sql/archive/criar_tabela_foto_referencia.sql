-- ============================================
-- Script: criar_tabela_foto_referencia.sql
-- Descrição: Cria tabela para armazenar fotos de referência
--            vinculadas a descrições de patrimônios
-- Data: 06/01/2026
-- ============================================

-- Verificar se a tabela já existe antes de criar
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_schema = 'public' 
                   AND table_name = 'tabela_foto_referencia') THEN
        
        -- Criar tabela principal
        CREATE TABLE tabela_foto_referencia (
            id SERIAL PRIMARY KEY,
            descricao_normalizada VARCHAR(500) NOT NULL,
            imagem_blob BYTEA NOT NULL,
            hash_imagem VARCHAR(64) NOT NULL,
            tamanho_bytes INTEGER NOT NULL,
            data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            ativo BOOLEAN DEFAULT TRUE,
            usuario_cadastro VARCHAR(100),
            
            -- Constraint para garantir tamanho máximo de 50KB
            CONSTRAINT chk_foto_ref_tamanho CHECK (tamanho_bytes <= 51200),
            
            -- Constraint para descrição única (apenas entre ativos)
            CONSTRAINT uq_foto_ref_descricao_ativa UNIQUE (descricao_normalizada) 
                WHERE (ativo = TRUE)
        );
        
        RAISE NOTICE 'Tabela tabela_foto_referencia criada com sucesso.';
    ELSE
        RAISE NOTICE 'Tabela tabela_foto_referencia já existe.';
    END IF;
END $$;

-- Criar índices para otimização de consultas
CREATE INDEX IF NOT EXISTS idx_foto_ref_descricao 
    ON tabela_foto_referencia(descricao_normalizada);

CREATE INDEX IF NOT EXISTS idx_foto_ref_atualizacao 
    ON tabela_foto_referencia(data_atualizacao);

CREATE INDEX IF NOT EXISTS idx_foto_ref_ativo 
    ON tabela_foto_referencia(ativo);

-- Índice composto para busca de fotos ativas por descrição
CREATE INDEX IF NOT EXISTS idx_foto_ref_descricao_ativo 
    ON tabela_foto_referencia(descricao_normalizada, ativo) 
    WHERE ativo = TRUE;

-- Comentários na tabela e colunas
COMMENT ON TABLE tabela_foto_referencia IS 
    'Armazena fotos de referência vinculadas a descrições de patrimônios para identificação visual no app mobile';

COMMENT ON COLUMN tabela_foto_referencia.id IS 
    'Identificador único da foto de referência';

COMMENT ON COLUMN tabela_foto_referencia.descricao_normalizada IS 
    'Descrição normalizada do patrimônio (sem números ANAC, séries, etc.)';

COMMENT ON COLUMN tabela_foto_referencia.imagem_blob IS 
    'Imagem em formato JPEG comprimido (thumbnail 200x200px, max 50KB)';

COMMENT ON COLUMN tabela_foto_referencia.hash_imagem IS 
    'Hash SHA-256 da imagem para verificação de integridade e cache';

COMMENT ON COLUMN tabela_foto_referencia.tamanho_bytes IS 
    'Tamanho da imagem em bytes (máximo 51200 = 50KB)';

COMMENT ON COLUMN tabela_foto_referencia.data_cadastro IS 
    'Data e hora do cadastro inicial';

COMMENT ON COLUMN tabela_foto_referencia.data_atualizacao IS 
    'Data e hora da última atualização (usado para delta sync)';

COMMENT ON COLUMN tabela_foto_referencia.ativo IS 
    'Flag para soft delete (FALSE = excluído)';

COMMENT ON COLUMN tabela_foto_referencia.usuario_cadastro IS 
    'Login do usuário que cadastrou/atualizou a foto';

-- Trigger para atualizar data_atualizacao automaticamente
CREATE OR REPLACE FUNCTION atualizar_data_atualizacao_foto_referencia()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_foto_referencia_atualizacao ON tabela_foto_referencia;

CREATE TRIGGER trg_foto_referencia_atualizacao
    BEFORE UPDATE ON tabela_foto_referencia
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_atualizacao_foto_referencia();

-- Verificar criação
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'tabela_foto_referencia'
ORDER BY ordinal_position;
