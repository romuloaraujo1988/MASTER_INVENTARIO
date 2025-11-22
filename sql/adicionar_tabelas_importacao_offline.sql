-- Script para adicionar tabelas necessárias para importação offline
-- Sistema de Inventário de Patrimônio
-- Data: 21/11/2024

-- ==================== TABELAS PARA IMPORTAÇÃO OFFLINE ====================

-- Tabela local de salas
CREATE TABLE IF NOT EXISTS local_sala (
    id INTEGER PRIMARY KEY,
    nome TEXT,
    descricao TEXT,
    bloco TEXT,
    andar TEXT,
    capacidade INTEGER,
    tipo TEXT,
    ativa BOOLEAN DEFAULT TRUE,
    -- Campos de controle offline
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'SYNCED'
);

-- Tabela local de responsáveis
CREATE TABLE IF NOT EXISTS local_responsavel (
    id INTEGER PRIMARY KEY,
    nome TEXT NOT NULL,
    cpf TEXT,
    matricula TEXT,
    email TEXT,
    telefone TEXT,
    cargo TEXT,
    setor TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    -- Campos de controle offline
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'SYNCED'
);

-- Tabela local de usuários (para login offline)
CREATE TABLE IF NOT EXISTS local_usuario (
    id INTEGER PRIMARY KEY,
    login TEXT NOT NULL UNIQUE,
    senha_hash TEXT NOT NULL,
    nome TEXT NOT NULL,
    email TEXT,
    perfil TEXT DEFAULT 'COLETOR',
    ativo BOOLEAN DEFAULT TRUE,
    -- Campos de controle offline
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ultimo_acesso DATETIME
);

-- ==================== ÍNDICES ====================

-- Índices para local_sala
CREATE INDEX IF NOT EXISTS idx_local_sala_nome ON local_sala(nome);
CREATE INDEX IF NOT EXISTS idx_local_sala_ativa ON local_sala(ativa);
CREATE INDEX IF NOT EXISTS idx_local_sala_bloco ON local_sala(bloco);

-- Índices para local_responsavel
CREATE INDEX IF NOT EXISTS idx_local_responsavel_nome ON local_responsavel(nome);
CREATE INDEX IF NOT EXISTS idx_local_responsavel_ativo ON local_responsavel(ativo);
CREATE INDEX IF NOT EXISTS idx_local_responsavel_cpf ON local_responsavel(cpf);
CREATE INDEX IF NOT EXISTS idx_local_responsavel_matricula ON local_responsavel(matricula);

-- Índices para local_usuario
CREATE INDEX IF NOT EXISTS idx_local_usuario_login ON local_usuario(login);
CREATE INDEX IF NOT EXISTS idx_local_usuario_ativo ON local_usuario(ativo);

-- ==================== TRIGGERS ====================

-- Trigger para atualizar local_updated_at em local_sala
CREATE TRIGGER IF NOT EXISTS update_local_sala_timestamp 
    AFTER UPDATE ON local_sala
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_sala 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- Trigger para atualizar local_updated_at em local_responsavel
CREATE TRIGGER IF NOT EXISTS update_local_responsavel_timestamp 
    AFTER UPDATE ON local_responsavel
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_responsavel 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- Trigger para atualizar local_updated_at em local_usuario
CREATE TRIGGER IF NOT EXISTS update_local_usuario_timestamp 
    AFTER UPDATE ON local_usuario
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_usuario 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- ==================== LOG ====================

INSERT INTO offline_logs (log_level, message, component) 
VALUES ('INFO', 'Tabelas de importação offline criadas com sucesso', 'SQLiteConnection');
