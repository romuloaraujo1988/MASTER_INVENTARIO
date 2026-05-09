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
    -- Métricas de tempo para estatísticas de velocidade de coleta
    tempo_coleta_segundos INTEGER,       -- Tempo total desde abertura até salvar (segundos)
    tempo_scan_segundos INTEGER,         -- Tempo do scan/busca do patrimônio (segundos)
    tempo_preenchimento_segundos INTEGER,-- Tempo de preenchimento do formulário (segundos)
    metodo_coleta TEXT,                  -- MANUAL, SCANNER, BUSCA_DESCRICAO
    hora_coleta INTEGER,                 -- Hora do dia 0-23
    periodo_coleta TEXT,                 -- MANHA, TARDE, NOITE
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

-- Tabela TABELA_INVENTARIO (compatibilidade com InventarioDAO)
CREATE TABLE IF NOT EXISTS TABELA_INVENTARIO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    NOME TEXT NOT NULL,
    ANO INTEGER,
    DATA_INICIO DATE NOT NULL,
    DATA_FIM DATE,
    OBSERVACAO TEXT,
    STATUS_INVENTARIO TEXT DEFAULT 'PLANEJADO',
    RESPONSAVEL_INVENTARIO TEXT,
    TOTAL_PATRIMONIOS INTEGER DEFAULT 0,
    PATRIMONIOS_COLETADOS INTEGER DEFAULT 0,
    PERCENTUAL_CONCLUSAO DECIMAL(5,2) DEFAULT 0.00,
    DATA_CRIACAO DATETIME DEFAULT CURRENT_TIMESTAMP,
    DATA_ULTIMA_ATUALIZACAO DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabela SALA (compatibilidade com SalaDAO)
CREATE TABLE IF NOT EXISTS SALA (
    ID_SALA INTEGER PRIMARY KEY AUTOINCREMENT,
    NUMERO_SALA TEXT NOT NULL,
    NOME_SALA TEXT,
    ANDAR TEXT,
    BLOCO TEXT,
    CAPACIDADE INTEGER,
    TIPO_SALA TEXT,
    ATIVA BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabela PATRIMONIO (compatibilidade com PatrimonioDAO)
CREATE TABLE IF NOT EXISTS PATRIMONIO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    NUMERO TEXT NOT NULL UNIQUE,
    DESCRICAO TEXT,
    DESCRICAO_RESUMIDA TEXT,
    MARCA TEXT,
    MODELO TEXT,
    NUMERO_SERIE TEXT,
    ESTADO_CONSERVACAO TEXT,
    VALOR_AQUISICAO DECIMAL(15,2),
    DATA_AQUISICAO DATE,
    STATUS TEXT DEFAULT 'ATIVO',
    OBSERVACOES TEXT,
    ID_RESPONSAVEL INTEGER,
    ID_SALA INTEGER,
    NOME_SALA TEXT,
    DATA_CADASTRO DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabela RESPONSAVEL (compatibilidade com ResponsavelDAO)
CREATE TABLE IF NOT EXISTS RESPONSAVEL (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    NOME TEXT NOT NULL,
    CPF TEXT,
    MATRICULA TEXT,
    EMAIL TEXT,
    TELEFONE TEXT,
    CARGO TEXT,
    SETOR TEXT,
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabela COLETA (compatibilidade com ColetaDAO)
CREATE TABLE IF NOT EXISTS COLETA (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_PATRIMONIO INTEGER,
    ID_COLETOR INTEGER,
    ID_PARTICIPANTE_INVENTARIO INTEGER,
    DATA_COLETA DATETIME DEFAULT CURRENT_TIMESTAMP,
    STATUS_COLETA TEXT DEFAULT 'COLETADO',
    OBSERVACAO_COLETA TEXT,
    LOCALIZACAO_ATUAL TEXT,
    LOCALIZACAO_ENCONTRADA TEXT,
    ESTADO_ENCONTRADO TEXT,
    DIVERGENCIA BOOLEAN DEFAULT FALSE,
    MOTIVO_DIVERGENCIA TEXT,
    SEM_ETIQUETA BOOLEAN DEFAULT FALSE,
    DESCRICAO_ITEM_SEM_ETIQUETA TEXT,
    CATEGORIA_ITEM_SEM_ETIQUETA TEXT,
    NUMERO_PATRIMONIO TEXT,
    DESCRICAO_PATRIMONIO TEXT
);

-- Tabela SALA_INVENTARIO (compatibilidade com SalaInventarioDAO)
CREATE TABLE IF NOT EXISTS SALA_INVENTARIO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    ID_SALA INTEGER NOT NULL,
    ID_INVENTARIO INTEGER NOT NULL,
    STATUS TEXT DEFAULT 'ABERTA',
    DATA_INICIO TEXT,
    DATA_FIM TEXT,
    ID_PARTICIPANTE_INICIOU INTEGER,
    ID_PARTICIPANTE_FINALIZOU INTEGER,
    TOTAL_ITENS INTEGER DEFAULT 0,
    ITENS_SEM_ETIQUETA INTEGER DEFAULT 0,
    OBSERVACOES TEXT,
    UNIQUE(ID_SALA, ID_INVENTARIO)
);

-- Tabela PARTICIPANTE_INVENTARIO (compatibilidade com ParticipanteInventarioDAO)
CREATE TABLE IF NOT EXISTS PARTICIPANTE_INVENTARIO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_USUARIO INTEGER NOT NULL,
    NOME_PARTICIPANTE TEXT,
    EMAIL TEXT,
    PERFIL TEXT,
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_INCLUSAO DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(ID_INVENTARIO, ID_USUARIO)
);

-- Tabela USUARIO (compatibilidade com UsuarioDAO)
CREATE TABLE IF NOT EXISTS USUARIO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    LOGIN TEXT NOT NULL UNIQUE,
    SENHA TEXT NOT NULL,
    NOME_COMPLETO TEXT NOT NULL,
    EMAIL TEXT,
    PERFIL TEXT DEFAULT 'COLETOR',
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO DATETIME DEFAULT CURRENT_TIMESTAMP,
    ULTIMO_ACESSO DATETIME
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

-- Índices para TABELA_INVENTARIO
CREATE INDEX IF NOT EXISTS idx_inventario_status ON TABELA_INVENTARIO(STATUS_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_inventario_data_inicio ON TABELA_INVENTARIO(DATA_INICIO);

-- Índices para SALA
CREATE INDEX IF NOT EXISTS idx_sala_numero ON SALA(NUMERO_SALA);
CREATE INDEX IF NOT EXISTS idx_sala_ativa ON SALA(ATIVA);

-- Índices para PATRIMONIO
CREATE INDEX IF NOT EXISTS idx_patrimonio_numero ON PATRIMONIO(NUMERO);
CREATE INDEX IF NOT EXISTS idx_patrimonio_status ON PATRIMONIO(STATUS);
CREATE INDEX IF NOT EXISTS idx_patrimonio_sala ON PATRIMONIO(ID_SALA);
CREATE INDEX IF NOT EXISTS idx_patrimonio_responsavel ON PATRIMONIO(ID_RESPONSAVEL);

-- Índices para RESPONSAVEL
CREATE INDEX IF NOT EXISTS idx_responsavel_nome ON RESPONSAVEL(NOME);
CREATE INDEX IF NOT EXISTS idx_responsavel_ativo ON RESPONSAVEL(ATIVO);

-- Índices para COLETA
CREATE INDEX IF NOT EXISTS idx_coleta_inventario ON COLETA(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio ON COLETA(ID_PATRIMONIO);
CREATE INDEX IF NOT EXISTS idx_coleta_data ON COLETA(DATA_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_sem_etiqueta ON COLETA(SEM_ETIQUETA);
CREATE INDEX IF NOT EXISTS idx_coleta_localizacao ON COLETA(LOCALIZACAO_ENCONTRADA);

-- Índices para SALA_INVENTARIO
CREATE INDEX IF NOT EXISTS idx_sala_inventario_sala ON SALA_INVENTARIO(ID_SALA);
CREATE INDEX IF NOT EXISTS idx_sala_inventario_inventario ON SALA_INVENTARIO(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_sala_inventario_status ON SALA_INVENTARIO(STATUS);

-- Índices para PARTICIPANTE_INVENTARIO
CREATE INDEX IF NOT EXISTS idx_participante_inventario_inventario ON PARTICIPANTE_INVENTARIO(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_participante_inventario_usuario ON PARTICIPANTE_INVENTARIO(ID_USUARIO);
CREATE INDEX IF NOT EXISTS idx_participante_inventario_ativo ON PARTICIPANTE_INVENTARIO(ATIVO);

-- Índices para USUARIO
CREATE INDEX IF NOT EXISTS idx_usuario_login ON USUARIO(LOGIN);
CREATE INDEX IF NOT EXISTS idx_usuario_ativo ON USUARIO(ATIVO);

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

-- ==================== DADOS INICIAIS PARA TESTE ====================

-- Inserir usuário admin padrão (senha: admin123)
INSERT OR IGNORE INTO USUARIO (ID, LOGIN, SENHA, NOME_COMPLETO, EMAIL, PERFIL, ATIVO) 
VALUES (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
        'Administrador do Sistema', 'admin@ifmt.edu.br', 'ADMIN', TRUE);

-- Inserir inventário padrão para teste offline
INSERT OR IGNORE INTO TABELA_INVENTARIO (ID, NOME, ANO, DATA_INICIO, STATUS_INVENTARIO, RESPONSAVEL_INVENTARIO, PERCENTUAL_CONCLUSAO)
VALUES (1, 'Inventário Offline 2025', 2025, DATE('now'), 'EM_ANDAMENTO', 'Administrador', 0.00);

-- Inserir salas de exemplo
INSERT OR IGNORE INTO SALA (ID_SALA, NUMERO_SALA, NOME_SALA, ANDAR, BLOCO, ATIVA) VALUES
(1, '101', 'Sala de Aula 101', '1º Andar', 'Bloco A', TRUE),
(2, '102', 'Sala de Aula 102', '1º Andar', 'Bloco A', TRUE),
(3, '201', 'Laboratório de Informática', '2º Andar', 'Bloco B', TRUE);

-- Inserir responsáveis de exemplo
INSERT OR IGNORE INTO RESPONSAVEL (ID, NOME, CARGO, SETOR, ATIVO) VALUES
(1, 'João Silva', 'Professor', 'Departamento de TI', TRUE),
(2, 'Maria Santos', 'Coordenadora', 'Administração', TRUE);

-- Inserir patrimônios de exemplo
INSERT OR IGNORE INTO PATRIMONIO (ID, NUMERO, DESCRICAO, ESTADO_CONSERVACAO, STATUS, ID_SALA, NOME_SALA) VALUES
(1, '000001', 'Cadeira Giratória', 'BOM', 'ATIVO', 1, '101'),
(2, '000002', 'Mesa de Escritório', 'BOM', 'ATIVO', 1, '101'),
(3, '000003', 'Computador Desktop', 'BOM', 'ATIVO', 3, '201'),
(4, '000004', 'Projetor Multimídia', 'BOM', 'ATIVO', 2, '102'),
(5, '000005', 'Quadro Branco', 'BOM', 'ATIVO', 1, '101');

-- Vincular salas ao inventário
INSERT OR IGNORE INTO SALA_INVENTARIO (ID_SALA, ID_INVENTARIO, STATUS) VALUES
(1, 1, 'ABERTA'),
(2, 1, 'ABERTA'),
(3, 1, 'ABERTA');

-- Adicionar admin como participante do inventário
INSERT OR IGNORE INTO PARTICIPANTE_INVENTARIO (ID_INVENTARIO, ID_USUARIO, NOME_PARTICIPANTE, EMAIL, PERFIL, ATIVO)
VALUES (1, 1, 'Administrador do Sistema', 'admin@ifmt.edu.br', 'ADMIN', TRUE);

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