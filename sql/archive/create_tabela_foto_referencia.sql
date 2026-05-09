-- Script para criação da tabela de fotos de referência
-- Utilizada pelo aplicativo mobile para visualização de itens durante a coleta

CREATE TABLE IF NOT EXISTS tabela_foto_referencia (
    id SERIAL PRIMARY KEY,
    descricao_normalizada VARCHAR(255) NOT NULL,
    imagem_blob BYTEA NOT NULL,
    hash_imagem VARCHAR(64),
    tamanho_bytes INTEGER,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    usuario_cadastro VARCHAR(100)
);

-- Índices para otimização de busca
CREATE INDEX IF NOT EXISTS idx_foto_ref_descricao ON tabela_foto_referencia(descricao_normalizada);
CREATE INDEX IF NOT EXISTS idx_foto_ref_ativo ON tabela_foto_referencia(ativo);

-- Comentários da tabela
COMMENT ON TABLE tabela_foto_referencia IS 'Armazena fotos de referência para descrições de patrimônios';
COMMENT ON COLUMN tabela_foto_referencia.descricao_normalizada IS 'Descrição do patrimônio vinculada à foto';
COMMENT ON COLUMN tabela_foto_referencia.imagem_blob IS 'Conteúdo binário da imagem';
COMMENT ON COLUMN tabela_foto_referencia.hash_imagem IS 'Hash MD5/SHA da imagem para evitar duplicatas';
