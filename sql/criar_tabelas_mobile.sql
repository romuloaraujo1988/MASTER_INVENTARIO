-- Script SQL para criação das tabelas necessárias para o aplicativo mobile
-- Sistema de Inventário - Módulo Mobile
-- Data: $(date)

-- =====================================================
-- TABELAS PARA SUPORTE AO APLICATIVO MOBILE
-- =====================================================

-- Tabela para controle de dispositivos móveis
CREATE TABLE IF NOT EXISTS mobile_device (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE NOT NULL,
    device_name VARCHAR(255),
    device_model VARCHAR(255),
    os_version VARCHAR(100),
    app_version VARCHAR(50),
    usuario_id INTEGER REFERENCES usuario(id),
    primeiro_acesso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acesso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mobile_device
CREATE INDEX idx_mobile_device_device_id ON mobile_device(device_id);
CREATE INDEX idx_mobile_device_usuario_id ON mobile_device(usuario_id);
CREATE INDEX idx_mobile_device_ativo ON mobile_device(ativo);

-- Tabela para controle de tokens JWT mobile
CREATE TABLE IF NOT EXISTS mobile_token (
    id SERIAL PRIMARY KEY,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    refresh_token_hash VARCHAR(255) UNIQUE,
    usuario_id INTEGER REFERENCES usuario(id),
    device_id VARCHAR(255) REFERENCES mobile_device(device_id),
    tipo_token VARCHAR(20) DEFAULT 'ACCESS', -- ACCESS, REFRESH
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP NOT NULL,
    revogado BOOLEAN DEFAULT FALSE,
    data_revogacao TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);

-- Índices para mobile_token
CREATE INDEX idx_mobile_token_hash ON mobile_token(token_hash);
CREATE INDEX idx_mobile_token_refresh ON mobile_token(refresh_token_hash);
CREATE INDEX idx_mobile_token_usuario ON mobile_token(usuario_id);
CREATE INDEX idx_mobile_token_device ON mobile_token(device_id);
CREATE INDEX idx_mobile_token_expiracao ON mobile_token(data_expiracao);
CREATE INDEX idx_mobile_token_revogado ON mobile_token(revogado);

-- Tabela para log de sincronização mobile
CREATE TABLE IF NOT EXISTS mobile_sync_log (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuario(id),
    device_id VARCHAR(255) REFERENCES mobile_device(device_id),
    tipo_sync VARCHAR(50) NOT NULL, -- PATRIMONIO, SETOR, SALA, COLETA
    operacao VARCHAR(20) NOT NULL, -- DOWNLOAD, UPLOAD
    registros_processados INTEGER DEFAULT 0,
    registros_sucesso INTEGER DEFAULT 0,
    registros_erro INTEGER DEFAULT 0,
    data_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_fim TIMESTAMP,
    status VARCHAR(20) DEFAULT 'INICIADO', -- INICIADO, CONCLUIDO, ERRO
    mensagem_erro TEXT,
    tamanho_dados BIGINT, -- em bytes
    versao_app VARCHAR(50)
);

-- Índices para mobile_sync_log
CREATE INDEX idx_mobile_sync_usuario ON mobile_sync_log(usuario_id);
CREATE INDEX idx_mobile_sync_device ON mobile_sync_log(device_id);
CREATE INDEX idx_mobile_sync_tipo ON mobile_sync_log(tipo_sync);
CREATE INDEX idx_mobile_sync_data ON mobile_sync_log(data_inicio);
CREATE INDEX idx_mobile_sync_status ON mobile_sync_log(status);

-- Tabela para controle de versão de dados (para sincronização incremental)
CREATE TABLE IF NOT EXISTS mobile_data_version (
    id SERIAL PRIMARY KEY,
    tabela VARCHAR(50) NOT NULL,
    registro_id INTEGER NOT NULL,
    versao INTEGER DEFAULT 1,
    data_modificacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    operacao VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    usuario_modificacao INTEGER REFERENCES usuario(id)
);

-- Índices para mobile_data_version
CREATE INDEX idx_mobile_data_version_tabela ON mobile_data_version(tabela);
CREATE INDEX idx_mobile_data_version_registro ON mobile_data_version(registro_id);
CREATE INDEX idx_mobile_data_version_data ON mobile_data_version(data_modificacao);
CREATE UNIQUE INDEX idx_mobile_data_version_unique ON mobile_data_version(tabela, registro_id);

-- Tabela para armazenar fotos das coletas mobile
CREATE TABLE IF NOT EXISTS mobile_coleta_foto (
    id SERIAL PRIMARY KEY,
    coleta_id INTEGER REFERENCES coleta(id),
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_arquivo TEXT NOT NULL,
    tamanho_arquivo BIGINT,
    tipo_mime VARCHAR(100),
    largura INTEGER,
    altura INTEGER,
    data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    device_id VARCHAR(255) REFERENCES mobile_device(device_id),
    hash_arquivo VARCHAR(255) -- para evitar duplicatas
);

-- Índices para mobile_coleta_foto
CREATE INDEX idx_mobile_coleta_foto_coleta ON mobile_coleta_foto(coleta_id);
CREATE INDEX idx_mobile_coleta_foto_device ON mobile_coleta_foto(device_id);
CREATE INDEX idx_mobile_coleta_foto_hash ON mobile_coleta_foto(hash_arquivo);

-- Tabela para configurações mobile por usuário
CREATE TABLE IF NOT EXISTS mobile_user_config (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuario(id),
    device_id VARCHAR(255) REFERENCES mobile_device(device_id),
    chave_config VARCHAR(100) NOT NULL,
    valor_config TEXT,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_modificacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mobile_user_config
CREATE INDEX idx_mobile_user_config_usuario ON mobile_user_config(usuario_id);
CREATE INDEX idx_mobile_user_config_device ON mobile_user_config(device_id);
CREATE INDEX idx_mobile_user_config_chave ON mobile_user_config(chave_config);
CREATE UNIQUE INDEX idx_mobile_user_config_unique ON mobile_user_config(usuario_id, device_id, chave_config);

-- Tabela para cache de QR Codes
CREATE TABLE IF NOT EXISTS mobile_qr_cache (
    id SERIAL PRIMARY KEY,
    patrimonio_id INTEGER REFERENCES patrimonio(id),
    qr_code VARCHAR(255) UNIQUE NOT NULL,
    qr_image_base64 TEXT, -- imagem do QR code em base64
    data_geracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE
);

-- Índices para mobile_qr_cache
CREATE INDEX idx_mobile_qr_cache_patrimonio ON mobile_qr_cache(patrimonio_id);
CREATE INDEX idx_mobile_qr_cache_qr_code ON mobile_qr_cache(qr_code);
CREATE INDEX idx_mobile_qr_cache_ativo ON mobile_qr_cache(ativo);

-- Tabela para estatísticas de uso mobile
CREATE TABLE IF NOT EXISTS mobile_usage_stats (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuario(id),
    device_id VARCHAR(255) REFERENCES mobile_device(device_id),
    data_uso DATE NOT NULL,
    total_logins INTEGER DEFAULT 0,
    total_scans INTEGER DEFAULT 0,
    total_coletas INTEGER DEFAULT 0,
    total_sincronizacoes INTEGER DEFAULT 0,
    tempo_uso_minutos INTEGER DEFAULT 0,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mobile_usage_stats
CREATE INDEX idx_mobile_usage_usuario ON mobile_usage_stats(usuario_id);
CREATE INDEX idx_mobile_usage_device ON mobile_usage_stats(device_id);
CREATE INDEX idx_mobile_usage_data ON mobile_usage_stats(data_uso);
CREATE UNIQUE INDEX idx_mobile_usage_unique ON mobile_usage_stats(usuario_id, device_id, data_uso);

-- =====================================================
-- ALTERAÇÕES EM TABELAS EXISTENTES
-- =====================================================

-- Adicionar campos para suporte mobile na tabela coleta
ALTER TABLE coleta ADD COLUMN IF NOT EXISTS device_id VARCHAR(255);
ALTER TABLE coleta ADD COLUMN IF NOT EXISTS latitude DECIMAL(10, 8);
ALTER TABLE coleta ADD COLUMN IF NOT EXISTS longitude DECIMAL(11, 8);
ALTER TABLE coleta ADD COLUMN IF NOT EXISTS precisao_gps DECIMAL(8, 2); -- em metros
ALTER TABLE coleta ADD COLUMN IF NOT EXISTS origem VARCHAR(20) DEFAULT 'WEB'; -- WEB, MOBILE
ALTER TABLE coleta ADD COLUMN IF NOT EXISTS app_version VARCHAR(50);
ALTER TABLE coleta ADD COLUMN IF NOT EXISTS sincronizado BOOLEAN DEFAULT TRUE;
ALTER TABLE coleta ADD COLUMN IF NOT EXISTS data_sincronizacao TIMESTAMP;

-- Adicionar índices para os novos campos
CREATE INDEX IF NOT EXISTS idx_coleta_device_id ON coleta(device_id);
CREATE INDEX IF NOT EXISTS idx_coleta_origem ON coleta(origem);
CREATE INDEX IF NOT EXISTS idx_coleta_sincronizado ON coleta(sincronizado);

-- Adicionar campo para QR Code na tabela patrimonio se não existir
ALTER TABLE patrimonio ADD COLUMN IF NOT EXISTS qr_code VARCHAR(255);
ALTER TABLE patrimonio ADD COLUMN IF NOT EXISTS qr_gerado BOOLEAN DEFAULT FALSE;
ALTER TABLE patrimonio ADD COLUMN IF NOT EXISTS data_qr_geracao TIMESTAMP;

-- Adicionar índice para QR Code
CREATE INDEX IF NOT EXISTS idx_patrimonio_qr_code ON patrimonio(qr_code);
CREATE INDEX IF NOT EXISTS idx_patrimonio_qr_gerado ON patrimonio(qr_gerado);

-- Adicionar campos de auditoria mobile na tabela usuario
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS mobile_habilitado BOOLEAN DEFAULT TRUE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS ultimo_login_mobile TIMESTAMP;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS total_logins_mobile INTEGER DEFAULT 0;

-- =====================================================
-- TRIGGERS PARA CONTROLE DE VERSÃO
-- =====================================================

-- Função para atualizar versão de dados
CREATE OR REPLACE FUNCTION update_mobile_data_version()
RETURNS TRIGGER AS $$
BEGIN
    -- Para INSERT
    IF TG_OP = 'INSERT' THEN
        INSERT INTO mobile_data_version (tabela, registro_id, operacao, usuario_modificacao)
        VALUES (TG_TABLE_NAME, NEW.id, 'INSERT', COALESCE(NEW.usuario_id, 1));
        RETURN NEW;
    END IF;
    
    -- Para UPDATE
    IF TG_OP = 'UPDATE' THEN
        UPDATE mobile_data_version 
        SET versao = versao + 1, 
            data_modificacao = CURRENT_TIMESTAMP,
            operacao = 'UPDATE',
            usuario_modificacao = COALESCE(NEW.usuario_id, OLD.usuario_id, 1)
        WHERE tabela = TG_TABLE_NAME AND registro_id = NEW.id;
        
        IF NOT FOUND THEN
            INSERT INTO mobile_data_version (tabela, registro_id, operacao, usuario_modificacao)
            VALUES (TG_TABLE_NAME, NEW.id, 'UPDATE', COALESCE(NEW.usuario_id, OLD.usuario_id, 1));
        END IF;
        RETURN NEW;
    END IF;
    
    -- Para DELETE
    IF TG_OP = 'DELETE' THEN
        UPDATE mobile_data_version 
        SET versao = versao + 1, 
            data_modificacao = CURRENT_TIMESTAMP,
            operacao = 'DELETE',
            usuario_modificacao = COALESCE(OLD.usuario_id, 1)
        WHERE tabela = TG_TABLE_NAME AND registro_id = OLD.id;
        
        IF NOT FOUND THEN
            INSERT INTO mobile_data_version (tabela, registro_id, operacao, usuario_modificacao)
            VALUES (TG_TABLE_NAME, OLD.id, 'DELETE', COALESCE(OLD.usuario_id, 1));
        END IF;
        RETURN OLD;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Triggers para controle de versão nas tabelas principais
DROP TRIGGER IF EXISTS trigger_patrimonio_version ON patrimonio;
CREATE TRIGGER trigger_patrimonio_version
    AFTER INSERT OR UPDATE OR DELETE ON patrimonio
    FOR EACH ROW EXECUTE FUNCTION update_mobile_data_version();

DROP TRIGGER IF EXISTS trigger_setor_version ON setor;
CREATE TRIGGER trigger_setor_version
    AFTER INSERT OR UPDATE OR DELETE ON setor
    FOR EACH ROW EXECUTE FUNCTION update_mobile_data_version();

DROP TRIGGER IF EXISTS trigger_sala_version ON sala;
CREATE TRIGGER trigger_sala_version
    AFTER INSERT OR UPDATE OR DELETE ON sala
    FOR EACH ROW EXECUTE FUNCTION update_mobile_data_version();

DROP TRIGGER IF EXISTS trigger_coleta_version ON coleta;
CREATE TRIGGER trigger_coleta_version
    AFTER INSERT OR UPDATE OR DELETE ON coleta
    FOR EACH ROW EXECUTE FUNCTION update_mobile_data_version();

-- =====================================================
-- FUNÇÕES AUXILIARES PARA MOBILE
-- =====================================================

-- Função para gerar QR Code para patrimônio
CREATE OR REPLACE FUNCTION gerar_qr_code_patrimonio(p_patrimonio_id INTEGER)
RETURNS VARCHAR(255) AS $$
DECLARE
    v_codigo VARCHAR(255);
    v_qr_code VARCHAR(255);
BEGIN
    -- Buscar código do patrimônio
    SELECT codigo INTO v_codigo FROM patrimonio WHERE id = p_patrimonio_id;
    
    IF v_codigo IS NULL THEN
        RAISE EXCEPTION 'Patrimônio não encontrado: %', p_patrimonio_id;
    END IF;
    
    -- Gerar QR Code (formato: INV_CODIGO_TIMESTAMP)
    v_qr_code := 'INV_' || v_codigo || '_' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT;
    
    -- Atualizar patrimônio com QR Code
    UPDATE patrimonio 
    SET qr_code = v_qr_code, 
        qr_gerado = TRUE, 
        data_qr_geracao = CURRENT_TIMESTAMP
    WHERE id = p_patrimonio_id;
    
    -- Inserir no cache de QR Codes
    INSERT INTO mobile_qr_cache (patrimonio_id, qr_code, data_geracao)
    VALUES (p_patrimonio_id, v_qr_code, CURRENT_TIMESTAMP)
    ON CONFLICT (qr_code) DO NOTHING;
    
    RETURN v_qr_code;
END;
$$ LANGUAGE plpgsql;

-- Função para limpar tokens expirados
CREATE OR REPLACE FUNCTION limpar_tokens_expirados()
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    -- Marcar tokens expirados como revogados
    UPDATE mobile_token 
    SET revogado = TRUE, 
        data_revogacao = CURRENT_TIMESTAMP
    WHERE data_expiracao < CURRENT_TIMESTAMP 
      AND revogado = FALSE;
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    
    -- Deletar tokens muito antigos (mais de 30 dias)
    DELETE FROM mobile_token 
    WHERE data_expiracao < CURRENT_TIMESTAMP - INTERVAL '30 days';
    
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- Função para estatísticas de uso mobile
CREATE OR REPLACE FUNCTION atualizar_stats_mobile(
    p_usuario_id INTEGER,
    p_device_id VARCHAR(255),
    p_tipo_operacao VARCHAR(20)
)
RETURNS VOID AS $$
BEGIN
    INSERT INTO mobile_usage_stats (
        usuario_id, device_id, data_uso, 
        total_logins, total_scans, total_coletas, total_sincronizacoes
    )
    VALUES (
        p_usuario_id, p_device_id, CURRENT_DATE,
        CASE WHEN p_tipo_operacao = 'LOGIN' THEN 1 ELSE 0 END,
        CASE WHEN p_tipo_operacao = 'SCAN' THEN 1 ELSE 0 END,
        CASE WHEN p_tipo_operacao = 'COLETA' THEN 1 ELSE 0 END,
        CASE WHEN p_tipo_operacao = 'SYNC' THEN 1 ELSE 0 END
    )
    ON CONFLICT (usuario_id, device_id, data_uso) 
    DO UPDATE SET
        total_logins = mobile_usage_stats.total_logins + 
            CASE WHEN p_tipo_operacao = 'LOGIN' THEN 1 ELSE 0 END,
        total_scans = mobile_usage_stats.total_scans + 
            CASE WHEN p_tipo_operacao = 'SCAN' THEN 1 ELSE 0 END,
        total_coletas = mobile_usage_stats.total_coletas + 
            CASE WHEN p_tipo_operacao = 'COLETA' THEN 1 ELSE 0 END,
        total_sincronizacoes = mobile_usage_stats.total_sincronizacoes + 
            CASE WHEN p_tipo_operacao = 'SYNC' THEN 1 ELSE 0 END,
        data_atualizacao = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VIEWS PARA RELATÓRIOS MOBILE
-- =====================================================

-- View para relatório de uso mobile
CREATE OR REPLACE VIEW vw_mobile_usage_report AS
SELECT 
    u.nome as usuario_nome,
    u.username,
    md.device_name,
    md.device_model,
    md.app_version,
    mus.data_uso,
    mus.total_logins,
    mus.total_scans,
    mus.total_coletas,
    mus.total_sincronizacoes,
    mus.tempo_uso_minutos,
    s.nome as setor_nome
FROM mobile_usage_stats mus
JOIN usuario u ON mus.usuario_id = u.id
JOIN mobile_device md ON mus.device_id = md.device_id
LEFT JOIN setor s ON u.setor_id = s.id
ORDER BY mus.data_uso DESC, u.nome;

-- View para status de sincronização
CREATE OR REPLACE VIEW vw_mobile_sync_status AS
SELECT 
    u.nome as usuario_nome,
    md.device_name,
    msl.tipo_sync,
    msl.operacao,
    msl.registros_processados,
    msl.registros_sucesso,
    msl.registros_erro,
    msl.status,
    msl.data_inicio,
    msl.data_fim,
    EXTRACT(EPOCH FROM (msl.data_fim - msl.data_inicio)) as duracao_segundos
FROM mobile_sync_log msl
JOIN usuario u ON msl.usuario_id = u.id
JOIN mobile_device md ON msl.device_id = md.device_id
ORDER BY msl.data_inicio DESC;

-- View para patrimônios com QR Code
CREATE OR REPLACE VIEW vw_patrimonio_qr AS
SELECT 
    p.id,
    p.codigo,
    p.descricao,
    p.qr_code,
    p.qr_gerado,
    p.data_qr_geracao,
    s.nome as setor_nome,
    sl.nome as sala_nome,
    CASE 
        WHEN EXISTS (SELECT 1 FROM coleta c WHERE c.patrimonio_id = p.id) 
        THEN TRUE 
        ELSE FALSE 
    END as ja_coletado
FROM patrimonio p
LEFT JOIN setor s ON p.setor_id = s.id
LEFT JOIN sala sl ON p.sala_id = sl.id
WHERE p.ativo = TRUE
ORDER BY p.codigo;

-- =====================================================
-- DADOS INICIAIS
-- =====================================================

-- Inserir configurações padrão para mobile
INSERT INTO mobile_user_config (usuario_id, device_id, chave_config, valor_config)
SELECT 
    u.id,
    'DEFAULT',
    'sync_interval_minutes',
    '30'
FROM usuario u
WHERE u.ativo = TRUE
ON CONFLICT (usuario_id, device_id, chave_config) DO NOTHING;

INSERT INTO mobile_user_config (usuario_id, device_id, chave_config, valor_config)
SELECT 
    u.id,
    'DEFAULT',
    'auto_sync_enabled',
    'true'
FROM usuario u
WHERE u.ativo = TRUE
ON CONFLICT (usuario_id, device_id, chave_config) DO NOTHING;

-- =====================================================
-- COMENTÁRIOS DAS TABELAS
-- =====================================================

COMMENT ON TABLE mobile_device IS 'Controle de dispositivos móveis registrados';
COMMENT ON TABLE mobile_token IS 'Controle de tokens JWT para autenticação mobile';
COMMENT ON TABLE mobile_sync_log IS 'Log de sincronizações realizadas pelo app mobile';
COMMENT ON TABLE mobile_data_version IS 'Controle de versão para sincronização incremental';
COMMENT ON TABLE mobile_coleta_foto IS 'Fotos capturadas durante as coletas mobile';
COMMENT ON TABLE mobile_user_config IS 'Configurações personalizadas por usuário/dispositivo';
COMMENT ON TABLE mobile_qr_cache IS 'Cache de QR Codes gerados para patrimônios';
COMMENT ON TABLE mobile_usage_stats IS 'Estatísticas de uso do aplicativo mobile';

COMMENT ON FUNCTION gerar_qr_code_patrimonio(INTEGER) IS 'Gera QR Code único para um patrimônio';
COMMENT ON FUNCTION limpar_tokens_expirados() IS 'Remove tokens JWT expirados';
COMMENT ON FUNCTION atualizar_stats_mobile(INTEGER, VARCHAR, VARCHAR) IS 'Atualiza estatísticas de uso mobile';

-- =====================================================
-- SCRIPT CONCLUÍDO
-- =====================================================

SELECT 'Tabelas mobile criadas com sucesso!' as resultado;