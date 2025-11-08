-- Script para criar tabelas SQLite para modo offline
-- Sistema de Inventário de Patrimônio
-- Data: 2024

-- ==================== TABELAS DE CONTROLE DO SISTEMA OFFLINE ====================

-- Tabela de controle de sincronização
CREATE TABLE IF NOT EXISTS sync_control (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    table_name TEXT NOT NULL,
    last_sync_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_direction TEXT CHECK(sync_direction IN ('UP', 'DOWN', 'BOTH')) DEFAULT 'BOTH',
    sync_status TEXT CHECK(sync_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED')) DEFAULT 'PENDING',
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de metadados de sincronização
CREATE TABLE IF NOT EXISTS sync_metadata (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_type TEXT NOT NULL,
    entity_id INTEGER NOT NULL,
    operation TEXT CHECK(operation IN ('INSERT', 'UPDATE', 'DELETE')) NOT NULL,
    sync_status TEXT CHECK(sync_status IN ('PENDING', 'SYNCED', 'CONFLICT', 'FAILED')) DEFAULT 'PENDING',
    local_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    remote_timestamp DATETIME,
    conflict_data TEXT, -- JSON com dados do conflito
    retry_count INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de logs offline
CREATE TABLE IF NOT EXISTS offline_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    log_level TEXT CHECK(log_level IN ('DEBUG', 'INFO', 'WARN', 'ERROR')) DEFAULT 'INFO',
    message TEXT NOT NULL,
    details TEXT,
    component TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==================== TABELAS ESPELHO DAS ENTIDADES PRINCIPAIS ====================

-- Tabela local de patrimônio
CREATE TABLE IF NOT EXISTS local_patrimonio (
    id INTEGER PRIMARY KEY,
    numero_patrimonio TEXT,
    descricao TEXT,
    descricao_resumida TEXT,
    marca TEXT,
    modelo TEXT,
    numero_serie TEXT,
    situacao TEXT,
    valor_aquisicao DECIMAL(15,2),
    data_aquisicao DATE,
    observacoes TEXT,
    responsavel_id INTEGER,
    sala_id INTEGER,
    sem_etiqueta BOOLEAN DEFAULT FALSE,
    -- Campos de controle offline
    is_local_only BOOLEAN DEFAULT FALSE,
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'PENDING',
    remote_id INTEGER -- ID no banco remoto após sincronização
);

-- Tabela local de coleta
CREATE TABLE IF NOT EXISTS local_coleta (
    id INTEGER PRIMARY KEY,
    patrimonio_id INTEGER,
    inventario_id INTEGER,
    participante_inventario_id INTEGER,
    situacao_encontrada TEXT,
    observacoes TEXT,
    data_coleta DATETIME,
    localizacao_atual TEXT,
    responsavel_atual TEXT,
    -- Campos de controle offline
    is_local_only BOOLEAN DEFAULT FALSE,
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'PENDING',
    remote_id INTEGER
);

-- Tabela local de inventário
CREATE TABLE IF NOT EXISTS local_inventario (
    id INTEGER PRIMARY KEY,
    nome_inventario TEXT,
    descricao TEXT,
    data_inicio DATE,
    data_fim DATE,
    status_inventario TEXT,
    percentual_conclusao DECIMAL(5,2),
    observacoes TEXT,
    -- Campos de controle offline
    is_local_only BOOLEAN DEFAULT FALSE,
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'PENDING',
    remote_id INTEGER
);

-- Tabela local de participante inventário
CREATE TABLE IF NOT EXISTS local_participante_inventario (
    id INTEGER PRIMARY KEY,
    inventario_id INTEGER,
    nome_participante TEXT,
    email TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    -- Campos de controle offline
    is_local_only BOOLEAN DEFAULT FALSE,
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'PENDING',
    remote_id INTEGER
);

-- ==================== ÍNDICES PARA OTIMIZAÇÃO ====================

-- Índices para sync_control
CREATE INDEX IF NOT EXISTS idx_sync_control_table_name ON sync_control(table_name);
CREATE INDEX IF NOT EXISTS idx_sync_control_status ON sync_control(sync_status);
CREATE INDEX IF NOT EXISTS idx_sync_control_timestamp ON sync_control(last_sync_timestamp);

-- Índices para sync_metadata
CREATE INDEX IF NOT EXISTS idx_sync_metadata_entity ON sync_metadata(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_sync_metadata_status ON sync_metadata(sync_status);
CREATE INDEX IF NOT EXISTS idx_sync_metadata_timestamp ON sync_metadata(local_timestamp);

-- Índices para offline_logs
CREATE INDEX IF NOT EXISTS idx_offline_logs_level ON offline_logs(log_level);
CREATE INDEX IF NOT EXISTS idx_offline_logs_timestamp ON offline_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_offline_logs_component ON offline_logs(component);

-- Índices para local_patrimonio
CREATE INDEX IF NOT EXISTS idx_local_patrimonio_numero ON local_patrimonio(numero_patrimonio);
CREATE INDEX IF NOT EXISTS idx_local_patrimonio_sync_status ON local_patrimonio(sync_status);
CREATE INDEX IF NOT EXISTS idx_local_patrimonio_local_only ON local_patrimonio(is_local_only);
CREATE INDEX IF NOT EXISTS idx_local_patrimonio_responsavel ON local_patrimonio(responsavel_id);
CREATE INDEX IF NOT EXISTS idx_local_patrimonio_sala ON local_patrimonio(sala_id);

-- Índices para local_coleta
CREATE INDEX IF NOT EXISTS idx_local_coleta_patrimonio ON local_coleta(patrimonio_id);
CREATE INDEX IF NOT EXISTS idx_local_coleta_inventario ON local_coleta(inventario_id);
CREATE INDEX IF NOT EXISTS idx_local_coleta_sync_status ON local_coleta(sync_status);
CREATE INDEX IF NOT EXISTS idx_local_coleta_data ON local_coleta(data_coleta);

-- Índices para local_inventario
CREATE INDEX IF NOT EXISTS idx_local_inventario_status ON local_inventario(status_inventario);
CREATE INDEX IF NOT EXISTS idx_local_inventario_sync_status ON local_inventario(sync_status);
CREATE INDEX IF NOT EXISTS idx_local_inventario_data_inicio ON local_inventario(data_inicio);

-- Índices para local_participante_inventario
CREATE INDEX IF NOT EXISTS idx_local_participante_inventario_id ON local_participante_inventario(inventario_id);
CREATE INDEX IF NOT EXISTS idx_local_participante_sync_status ON local_participante_inventario(sync_status);
CREATE INDEX IF NOT EXISTS idx_local_participante_ativo ON local_participante_inventario(ativo);

-- ==================== TRIGGERS PARA ATUALIZAÇÃO AUTOMÁTICA ====================

-- Trigger para atualizar local_updated_at em local_patrimonio
CREATE TRIGGER IF NOT EXISTS update_local_patrimonio_timestamp 
    AFTER UPDATE ON local_patrimonio
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_patrimonio 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- Trigger para atualizar local_updated_at em local_coleta
CREATE TRIGGER IF NOT EXISTS update_local_coleta_timestamp 
    AFTER UPDATE ON local_coleta
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_coleta 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- Trigger para atualizar local_updated_at em local_inventario
CREATE TRIGGER IF NOT EXISTS update_local_inventario_timestamp 
    AFTER UPDATE ON local_inventario
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_inventario 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- Trigger para atualizar local_updated_at em local_participante_inventario
CREATE TRIGGER IF NOT EXISTS update_local_participante_timestamp 
    AFTER UPDATE ON local_participante_inventario
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_participante_inventario 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- ==================== VIEWS PARA FACILITAR CONSULTAS ====================

-- View para dados pendentes de sincronização
CREATE VIEW IF NOT EXISTS v_pending_sync AS
SELECT 
    'patrimonio' as entity_type,
    id as entity_id,
    sync_status,
    local_updated_at as last_modified
FROM local_patrimonio 
WHERE sync_status = 'PENDING'

UNION ALL

SELECT 
    'coleta' as entity_type,
    id as entity_id,
    sync_status,
    local_updated_at as last_modified
FROM local_coleta 
WHERE sync_status = 'PENDING'

UNION ALL

SELECT 
    'inventario' as entity_type,
    id as entity_id,
    sync_status,
    local_updated_at as last_modified
FROM local_inventario 
WHERE sync_status = 'PENDING'

UNION ALL

SELECT 
    'participante_inventario' as entity_type,
    id as entity_id,
    sync_status,
    local_updated_at as last_modified
FROM local_participante_inventario 
WHERE sync_status = 'PENDING'

ORDER BY last_modified DESC;

-- View para estatísticas de sincronização
CREATE VIEW IF NOT EXISTS v_sync_stats AS
SELECT 
    'patrimonio' as entity_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN sync_status = 'PENDING' THEN 1 ELSE 0 END) as pending_sync,
    SUM(CASE WHEN sync_status = 'SYNCED' THEN 1 ELSE 0 END) as synced,
    SUM(CASE WHEN sync_status = 'CONFLICT' THEN 1 ELSE 0 END) as conflicts,
    SUM(CASE WHEN is_local_only = 1 THEN 1 ELSE 0 END) as local_only
FROM local_patrimonio

UNION ALL

SELECT 
    'coleta' as entity_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN sync_status = 'PENDING' THEN 1 ELSE 0 END) as pending_sync,
    SUM(CASE WHEN sync_status = 'SYNCED' THEN 1 ELSE 0 END) as synced,
    SUM(CASE WHEN sync_status = 'CONFLICT' THEN 1 ELSE 0 END) as conflicts,
    SUM(CASE WHEN is_local_only = 1 THEN 1 ELSE 0 END) as local_only
FROM local_coleta

UNION ALL

SELECT 
    'inventario' as entity_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN sync_status = 'PENDING' THEN 1 ELSE 0 END) as pending_sync,
    SUM(CASE WHEN sync_status = 'SYNCED' THEN 1 ELSE 0 END) as synced,
    SUM(CASE WHEN sync_status = 'CONFLICT' THEN 1 ELSE 0 END) as conflicts,
    SUM(CASE WHEN is_local_only = 1 THEN 1 ELSE 0 END) as local_only
FROM local_inventario

UNION ALL

SELECT 
    'participante_inventario' as entity_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN sync_status = 'PENDING' THEN 1 ELSE 0 END) as pending_sync,
    SUM(CASE WHEN sync_status = 'SYNCED' THEN 1 ELSE 0 END) as synced,
    SUM(CASE WHEN sync_status = 'CONFLICT' THEN 1 ELSE 0 END) as conflicts,
    SUM(CASE WHEN is_local_only = 1 THEN 1 ELSE 0 END) as local_only
FROM local_participante_inventario;

-- ==================== DADOS INICIAIS ====================

-- Inserir configurações iniciais de sincronização
INSERT OR IGNORE INTO sync_control (table_name, sync_direction) VALUES 
('local_patrimonio', 'BOTH'),
('local_coleta', 'BOTH'),
('local_inventario', 'BOTH'),
('local_participante_inventario', 'BOTH');

-- Inserir log inicial
INSERT INTO offline_logs (log_level, message, component) 
VALUES ('INFO', 'Banco de dados SQLite offline inicializado com sucesso', 'SQLiteConnection');

-- ==================== COMENTÁRIOS E DOCUMENTAÇÃO ====================

/*
ESTRUTURA DO BANCO DE DADOS OFFLINE:

1. TABELAS DE CONTROLE:
   - sync_control: Controla o status de sincronização por tabela
   - sync_metadata: Metadados de sincronização por registro
   - offline_logs: Logs do sistema offline

2. TABELAS ESPELHO:
   - local_patrimonio: Cópia local da tabela patrimonio
   - local_coleta: Cópia local da tabela coleta
   - local_inventario: Cópia local da tabela inventario
   - local_participante_inventario: Cópia local da tabela participante_inventario

3. CAMPOS DE CONTROLE OFFLINE:
   - is_local_only: Indica se o registro existe apenas localmente
   - local_created_at: Data de criação local
   - local_updated_at: Data de última atualização local
   - sync_status: Status de sincronização (PENDING, SYNCED, CONFLICT, FAILED)
   - remote_id: ID do registro no banco remoto após sincronização

4. ESTRATÉGIAS DE SINCRONIZAÇÃO:
   - TIMESTAMP_WINS: O registro mais recente prevalece
   - LOCAL_WINS: Dados locais prevalecem
   - REMOTE_WINS: Dados remotos prevalecem
   - MANUAL_RESOLUTION: Resolução manual de conflitos

5. ÍNDICES:
   - Otimizados para consultas de sincronização
   - Suporte a buscas por status, timestamps e entidades

6. TRIGGERS:
   - Atualização automática de timestamps
   - Manutenção de integridade dos dados

7. VIEWS:
   - v_pending_sync: Registros pendentes de sincronização
   - v_sync_stats: Estatísticas de sincronização
*/