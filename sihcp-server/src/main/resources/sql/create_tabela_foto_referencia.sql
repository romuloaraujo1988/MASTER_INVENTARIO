-- Script para criação da tabela de fotos de referência
-- Utilizada para exibir fotos dos bens no aplicativo mobile durante a coleta

CREATE TABLE IF NOT EXISTS tabela_foto_referencia (
    id SERIAL PRIMARY KEY,
    descricao_normalizada VARCHAR(500) NOT NULL,
    imagem_blob BYTEA,
    hash_imagem VARCHAR(100),
    tamanho_bytes INTEGER,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    usuario_cadastro VARCHAR(100)
);

-- Índices para otimização de busca
CREATE INDEX IF NOT EXISTS idx_foto_ref_descricao ON tabela_foto_referencia(descricao_normalizada);
CREATE INDEX IF NOT EXISTS idx_foto_ref_ativo ON tabela_foto_referencia(ativo);
CREATE INDEX IF NOT EXISTS idx_foto_ref_data_atu ON tabela_foto_referencia(data_atualizacao);

-- Comentários para documentação
COMMENT ON TABLE tabela_foto_referencia IS 'Armazena fotos de referência vinculadas a descrições normalizadas de patrimônios';
COMMENT ON COLUMN tabela_foto_referencia.descricao_normalizada IS 'Descrição padronizada do item para vínculo com o patrimônio';
COMMENT ON COLUMN tabela_foto_referencia.imagem_blob IS 'Conteúdo binário da imagem';
COMMENT ON COLUMN tabela_foto_referencia.hash_imagem IS 'Hash MD5/SHA da imagem para controle de integridade e duplicidade';
