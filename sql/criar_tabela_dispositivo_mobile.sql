-- =====================================================
-- Script: Criar Tabela DISPOSITIVO_MOBILE
-- Descrição: Gerenciamento de dispositivos móveis conectados
-- Autor: Sistema de Inventário
-- Data: 09/11/2025
-- =====================================================

-- Criar ENUM para status do dispositivo
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'status_dispositivo') THEN
        CREATE TYPE status_dispositivo AS ENUM ('PENDENTE', 'APROVADO', 'BLOQUEADO', 'REJEITADO');
    END IF;
END $$;

-- Criar tabela DISPOSITIVO_MOBILE
CREATE TABLE IF NOT EXISTS dispositivo_mobile (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL UNIQUE,
    id_usuario INTEGER NOT NULL,
    modelo VARCHAR(100),
    fabricante VARCHAR(100),
    versao_android VARCHAR(20),
    versao_app VARCHAR(20),
    endereco_ip VARCHAR(45),
    endereco_mac VARCHAR(17),
    status status_dispositivo NOT NULL DEFAULT 'APROVADO',
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_ultima_conexao TIMESTAMP,
    data_ultima_sincronizacao TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    token_atual TEXT,
    data_expiracao_token TIMESTAMP,
    observacoes TEXT,
    
    -- Foreign Keys
    CONSTRAINT fk_dispositivo_usuario 
        FOREIGN KEY (id_usuario) 
        REFERENCES tabela_usuario(id) 
        ON DELETE CASCADE
);

-- Criar índices para performance
CREATE INDEX IF NOT EXISTS idx_dispositivo_device_id ON dispositivo_mobile(device_id);
CREATE INDEX IF NOT EXISTS idx_dispositivo_usuario ON dispositivo_mobile(id_usuario);
CREATE INDEX IF NOT EXISTS idx_dispositivo_status ON dispositivo_mobile(status);
CREATE INDEX IF NOT EXISTS idx_dispositivo_ativo ON dispositivo_mobile(ativo);

-- Comentários
COMMENT ON TABLE dispositivo_mobile IS 'Gerenciamento de dispositivos móveis conectados ao sistema';
COMMENT ON COLUMN dispositivo_mobile.device_id IS 'ID único do dispositivo (Android ID)';
COMMENT ON COLUMN dispositivo_mobile.id_usuario IS 'Usuário proprietário do dispositivo';
COMMENT ON COLUMN dispositivo_mobile.modelo IS 'Modelo do dispositivo (ex: Samsung Galaxy S21)';
COMMENT ON COLUMN dispositivo_mobile.fabricante IS 'Fabricante do dispositivo (ex: Samsung)';
COMMENT ON COLUMN dispositivo_mobile.versao_android IS 'Versão do Android (ex: 13)';
COMMENT ON COLUMN dispositivo_mobile.versao_app IS 'Versão do app instalado (ex: 1.2.0)';
COMMENT ON COLUMN dispositivo_mobile.status IS 'Status do dispositivo: PENDENTE, APROVADO, BLOQUEADO, REJEITADO';
COMMENT ON COLUMN dispositivo_mobile.data_ultima_conexao IS 'Data/hora da última conexão do dispositivo';
COMMENT ON COLUMN dispositivo_mobile.data_ultima_sincronizacao IS 'Data/hora da última sincronização de dados';
COMMENT ON COLUMN dispositivo_mobile.token_atual IS 'Token JWT atual do dispositivo';
COMMENT ON COLUMN dispositivo_mobile.data_expiracao_token IS 'Data de expiração do token';

-- Inserir dados de exemplo (opcional)
-- INSERT INTO dispositivo_mobile (device_id, id_usuario, modelo, fabricante, versao_android, versao_app, status)
-- VALUES ('abc123def456', 1, 'Galaxy S21', 'Samsung', '13', '1.0.0', 'APROVADO');

COMMIT;

-- Exibir estrutura da tabela
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'dispositivo_mobile'
ORDER BY ordinal_position;
