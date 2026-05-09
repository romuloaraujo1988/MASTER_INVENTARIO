-- =====================================================
-- IMPLEMENTAÇÃO COMPLETA DAS TABELAS DO SISTEMA
-- Sistema de Inventário de Patrimônio IFMT
-- =====================================================
-- Este arquivo contém todos os scripts necessários para criar
-- as tabelas do sistema em PostgreSQL e SQLite
-- =====================================================

-- =====================================================
-- 1. SCRIPT PARA POSTGRESQL (SISTEMA PRINCIPAL)
-- =====================================================

-- 1.1 TABELA_SETOR
CREATE TABLE IF NOT EXISTS TABELA_SETOR (
    ID SERIAL PRIMARY KEY,
    NOME VARCHAR(255) NOT NULL UNIQUE,
    DESCRICAO TEXT,
    RESPONSAVEL_SETOR VARCHAR(255),
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para TABELA_SETOR
CREATE INDEX IF NOT EXISTS idx_setor_nome ON TABELA_SETOR(NOME);
CREATE INDEX IF NOT EXISTS idx_setor_ativo ON TABELA_SETOR(ATIVO);

-- 1.2 TABELA_RESPONSAVEL
CREATE TABLE IF NOT EXISTS TABELA_RESPONSAVEL (
    ID SERIAL PRIMARY KEY,
    NOME VARCHAR(255) NOT NULL,
    CPF VARCHAR(14) UNIQUE,
    EMAIL VARCHAR(255),
    TELEFONE VARCHAR(20),
    CARGO VARCHAR(100),
    ID_SETOR INTEGER,
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID)
);

-- Índices para TABELA_RESPONSAVEL
CREATE INDEX IF NOT EXISTS idx_responsavel_nome ON TABELA_RESPONSAVEL(NOME);
CREATE INDEX IF NOT EXISTS idx_responsavel_cpf ON TABELA_RESPONSAVEL(CPF);
CREATE INDEX IF NOT EXISTS idx_responsavel_setor ON TABELA_RESPONSAVEL(ID_SETOR);
CREATE INDEX IF NOT EXISTS idx_responsavel_ativo ON TABELA_RESPONSAVEL(ATIVO);

-- 1.3 TABELA_SALA
CREATE TABLE IF NOT EXISTS TABELA_SALA (
    ID_SALA SERIAL PRIMARY KEY,
    DESCRICAO VARCHAR(255) NOT NULL,
    NUMERO_SALA VARCHAR(20),
    ANDAR INTEGER,
    BLOCO VARCHAR(10),
    ID_SETOR INTEGER,
    CAPACIDADE INTEGER,
    AREA_M2 DECIMAL(8,2),
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID)
);

-- Índices para TABELA_SALA
CREATE INDEX IF NOT EXISTS idx_sala_numero ON TABELA_SALA(NUMERO_SALA);
CREATE INDEX IF NOT EXISTS idx_sala_setor ON TABELA_SALA(ID_SETOR);
CREATE INDEX IF NOT EXISTS idx_sala_ativo ON TABELA_SALA(ATIVO);

-- 1.4 TABELA_INVENTARIO
CREATE TABLE IF NOT EXISTS TABELA_INVENTARIO (
    ID SERIAL PRIMARY KEY,
    NOME VARCHAR(255) NOT NULL,
    ANO INTEGER NOT NULL,
    DATA_INICIO DATE NOT NULL,
    DATA_FIM DATE,
    OBSERVACAO TEXT,
    STATUS_INVENTARIO VARCHAR(50) DEFAULT 'PLANEJADO',
    RESPONSAVEL_INVENTARIO VARCHAR(255),
    TOTAL_PATRIMONIOS INTEGER DEFAULT 0,
    PATRIMONIOS_COLETADOS INTEGER DEFAULT 0,
    PERCENTUAL_CONCLUSAO DECIMAL(5,2) DEFAULT 0.00,
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DATA_ULTIMA_ATUALIZACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para TABELA_INVENTARIO
CREATE INDEX IF NOT EXISTS idx_inventario_ano ON TABELA_INVENTARIO(ANO);
CREATE INDEX IF NOT EXISTS idx_inventario_status ON TABELA_INVENTARIO(STATUS_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_inventario_data_inicio ON TABELA_INVENTARIO(DATA_INICIO);
CREATE INDEX IF NOT EXISTS idx_inventario_data_fim ON TABELA_INVENTARIO(DATA_FIM);

-- Trigger para atualizar data de última atualização
CREATE OR REPLACE FUNCTION update_inventario_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_inventario_timestamp
    BEFORE UPDATE ON TABELA_INVENTARIO
    FOR EACH ROW
    EXECUTE FUNCTION update_inventario_timestamp();

-- 1.5 TABELA_COLETOR
CREATE TABLE IF NOT EXISTS TABELA_COLETOR (
    ID SERIAL PRIMARY KEY,
    NOME_COLETOR VARCHAR(255) NOT NULL,
    MATRICULA VARCHAR(20) UNIQUE,
    EMAIL VARCHAR(255),
    TELEFONE VARCHAR(20),
    ATIVO BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para TABELA_COLETOR
CREATE INDEX IF NOT EXISTS idx_coletor_nome ON TABELA_COLETOR(NOME_COLETOR);
CREATE INDEX IF NOT EXISTS idx_coletor_matricula ON TABELA_COLETOR(MATRICULA);
CREATE INDEX IF NOT EXISTS idx_coletor_ativo ON TABELA_COLETOR(ATIVO);

-- 1.6 TABELA_USUARIO
CREATE TABLE IF NOT EXISTS TABELA_USUARIO (
    ID SERIAL PRIMARY KEY,
    LOGIN VARCHAR(50) NOT NULL UNIQUE,
    SENHA_HASH VARCHAR(255) NOT NULL,
    NOME_COMPLETO VARCHAR(150) NOT NULL,
    EMAIL VARCHAR(100) NOT NULL UNIQUE,
    MATRICULA VARCHAR(20) UNIQUE,
    PERFIL VARCHAR(20) NOT NULL CHECK (PERFIL IN ('ADMIN', 'SUPERVISOR', 'COLETOR', 'CONSULTA')),
    ID_SETOR INTEGER,
    ATIVO CHAR(1) DEFAULT 'S' CHECK (ATIVO IN ('S', 'N')),
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DATA_ULTIMO_ACESSO TIMESTAMP,
    TENTATIVAS_LOGIN INTEGER DEFAULT 0,
    BLOQUEADO CHAR(1) DEFAULT 'N' CHECK (BLOQUEADO IN ('S', 'N')),
    DATA_BLOQUEIO TIMESTAMP,
    DATA_EXPIRACAO_SENHA TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '90 days'),
    PRIMEIRO_ACESSO CHAR(1) DEFAULT 'S' CHECK (PRIMEIRO_ACESSO IN ('S', 'N')),
    OBSERVACOES TEXT,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID)
);

-- Índices para TABELA_USUARIO
CREATE INDEX IF NOT EXISTS idx_usuario_login ON TABELA_USUARIO(LOGIN);
CREATE INDEX IF NOT EXISTS idx_usuario_email ON TABELA_USUARIO(EMAIL);
CREATE INDEX IF NOT EXISTS idx_usuario_perfil ON TABELA_USUARIO(PERFIL);
CREATE INDEX IF NOT EXISTS idx_usuario_ativo ON TABELA_USUARIO(ATIVO);

-- Função para atualizar timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para atualizar updated_at automaticamente
CREATE TRIGGER update_usuario_updated_at 
    BEFORE UPDATE ON TABELA_USUARIO 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 1.7 TABELA_PATRIMONIO
CREATE TABLE IF NOT EXISTS TABELA_PATRIMONIO (
    ID SERIAL PRIMARY KEY,
    NUMERO VARCHAR(50) UNIQUE NOT NULL,
    STATUS VARCHAR(50) DEFAULT 'ATIVO',
    DESCRICAO TEXT,
    ROTULOS VARCHAR(500),
    ID_RESPONSAVEL INTEGER,
    VALOR_AQUISICAO DECIMAL(15,2),
    VALOR_DEPRECIADO DECIMAL(15,2),
    NUMERO_NOTA_FISCAL VARCHAR(100),
    NUMERO_SERIE VARCHAR(100),
    DATA_ENTRADA DATE,
    DATA_CARGA TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FORNECEDOR VARCHAR(255),
    ID_SALA INTEGER,
    ESTADO_CONSERVACAO VARCHAR(50),
    MARCA VARCHAR(100),
    MODELO VARCHAR(100),
    CATEGORIA VARCHAR(100),
    ED VARCHAR(100),
    OBSERVACOES TEXT,
    FOREIGN KEY (ID_RESPONSAVEL) REFERENCES TABELA_RESPONSAVEL(ID),
    FOREIGN KEY (ID_SALA) REFERENCES TABELA_SALA(ID_SALA)
);

-- Índices para TABELA_PATRIMONIO
CREATE INDEX IF NOT EXISTS idx_patrimonio_numero ON TABELA_PATRIMONIO(NUMERO);
CREATE INDEX IF NOT EXISTS idx_patrimonio_responsavel ON TABELA_PATRIMONIO(ID_RESPONSAVEL);
CREATE INDEX IF NOT EXISTS idx_patrimonio_sala ON TABELA_PATRIMONIO(ID_SALA);
CREATE INDEX IF NOT EXISTS idx_patrimonio_status ON TABELA_PATRIMONIO(STATUS);
CREATE INDEX IF NOT EXISTS idx_patrimonio_marca ON TABELA_PATRIMONIO(MARCA);
CREATE INDEX IF NOT EXISTS idx_patrimonio_modelo ON TABELA_PATRIMONIO(MODELO);

-- 1.8 TABELA_COLETA
CREATE TABLE IF NOT EXISTS TABELA_COLETA (
    ID SERIAL PRIMARY KEY,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_PATRIMONIO INTEGER NOT NULL,
    ID_USUARIO INTEGER NOT NULL,
    DATA_COLETA TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    STATUS_COLETA VARCHAR(50) DEFAULT 'COLETADO',
    OBSERVACAO_COLETA TEXT,
    LOCALIZACAO_ATUAL VARCHAR(255),
    LOCALIZACAO_ENCONTRADA VARCHAR(255),
    ESTADO_ENCONTRADO VARCHAR(50),
    DIVERGENCIA BOOLEAN DEFAULT FALSE,
    MOTIVO_DIVERGENCIA TEXT,
    LATITUDE DECIMAL(10,8),
    LONGITUDE DECIMAL(11,8),
    FOTO_PATRIMONIO TEXT,
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID),
    FOREIGN KEY (ID_PATRIMONIO) REFERENCES TABELA_PATRIMONIO(ID),
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID)
);

-- Índices para TABELA_COLETA
CREATE INDEX IF NOT EXISTS idx_coleta_inventario ON TABELA_COLETA(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio ON TABELA_COLETA(ID_PATRIMONIO);
CREATE INDEX IF NOT EXISTS idx_coleta_usuario ON TABELA_COLETA(ID_USUARIO);
CREATE INDEX IF NOT EXISTS idx_coleta_data ON TABELA_COLETA(DATA_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_status ON TABELA_COLETA(STATUS_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_divergencia ON TABELA_COLETA(DIVERGENCIA);

-- 1.9 TABELA_ITEM_COMPOSTO
CREATE TABLE IF NOT EXISTS tabela_item_composto (
    id SERIAL PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL,
    tipo_componente VARCHAR(100) NOT NULL,
    descricao_componente VARCHAR(500) NOT NULL,
    quantidade_esperada INTEGER NOT NULL DEFAULT 1,
    obrigatorio BOOLEAN DEFAULT TRUE,
    observacao TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_composto_patrimonio FOREIGN KEY (id_patrimonio_principal) 
        REFERENCES tabela_patrimonio(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_item_composto_patrimonio ON tabela_item_composto(id_patrimonio_principal);
CREATE INDEX IF NOT EXISTS idx_item_composto_tipo ON tabela_item_composto(tipo_componente);

-- 1.10 TABELA_COLETA_COMPONENTE
CREATE TABLE IF NOT EXISTS tabela_coleta_componente (
    id SERIAL PRIMARY KEY,
    id_item_composto INTEGER NOT NULL,
    id_inventario INTEGER NOT NULL,
    id_coletor INTEGER NOT NULL,
    quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
    status_componente VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    observacao_coleta TEXT,
    localizacao_encontrada VARCHAR(255),
    data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_coleta_componente_item FOREIGN KEY (id_item_composto) 
        REFERENCES tabela_item_composto(id) ON DELETE CASCADE,
    CONSTRAINT fk_coleta_componente_inventario FOREIGN KEY (id_inventario) 
        REFERENCES tabela_inventario(id) ON DELETE CASCADE,
    CONSTRAINT uk_coleta_componente_item_inventario UNIQUE (id_item_composto, id_inventario)
);

CREATE INDEX IF NOT EXISTS idx_coleta_componente_item ON tabela_coleta_componente(id_item_composto);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_inventario ON tabela_coleta_componente(id_inventario);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_status ON tabela_coleta_componente(status_componente);

-- 1.11 VIEWS ÚTEIS PARA POSTGRESQL
-- View de patrimônios com informações completas
CREATE OR REPLACE VIEW vw_patrimonios_completo AS
SELECT 
    p.ID,
    p.NUMERO,
    p.STATUS,
    p.DESCRICAO,
    p.MARCA,
    p.MODELO,
    p.OBSERVACOES,
    p.VALOR_AQUISICAO,
    p.VALOR_DEPRECIADO,
    p.DATA_ENTRADA,
    p.FORNECEDOR,
    p.ESTADO_CONSERVACAO,
    r.NOME AS RESPONSAVEL_NOME,
    s.DESCRICAO AS SALA_DESCRICAO,
    s.NUMERO_SALA,
    st.NOME AS SETOR_NOME
FROM TABELA_PATRIMONIO p
LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA
LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID;

-- View de coletas com informações completas
CREATE OR REPLACE VIEW vw_coletas_completo AS
SELECT 
    c.ID,
    c.DATA_COLETA,
    c.STATUS_COLETA,
    c.OBSERVACAO_COLETA,
    c.LOCALIZACAO_ATUAL,
    c.LOCALIZACAO_ENCONTRADA,
    c.DIVERGENCIA,
    i.NOME AS INVENTARIO_NOME,
    i.ANO AS INVENTARIO_ANO,
    p.NUMERO AS PATRIMONIO_NUMERO,
    p.DESCRICAO AS PATRIMONIO_DESCRICAO,
    u.NOME_COMPLETO AS COLETOR_NOME
FROM TABELA_COLETA c
JOIN TABELA_INVENTARIO i ON c.ID_INVENTARIO = i.ID
JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID
JOIN TABELA_USUARIO u ON c.ID_USUARIO = u.ID;

-- =====================================================
-- 2. SCRIPT PARA SQLITE (MODO OFFLINE)
-- =====================================================

-- 2.1 TABELAS DE CONTROLE DO SISTEMA OFFLINE
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

CREATE TABLE IF NOT EXISTS sync_metadata (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_type TEXT NOT NULL,
    entity_id INTEGER NOT NULL,
    operation TEXT CHECK(operation IN ('INSERT', 'UPDATE', 'DELETE')) NOT NULL,
    sync_status TEXT CHECK(sync_status IN ('PENDING', 'SYNCED', 'CONFLICT', 'FAILED')) DEFAULT 'PENDING',
    local_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    remote_timestamp DATETIME,
    conflict_data TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS offline_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    log_level TEXT CHECK(log_level IN ('DEBUG', 'INFO', 'WARN', 'ERROR')) DEFAULT 'INFO',
    message TEXT NOT NULL,
    details TEXT,
    component TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2.2 TABELAS ESPELHO DAS ENTIDADES PRINCIPAIS
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
    is_local_only BOOLEAN DEFAULT FALSE,
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'PENDING',
    remote_id INTEGER
);

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
    is_local_only BOOLEAN DEFAULT FALSE,
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'PENDING',
    remote_id INTEGER
);

CREATE TABLE IF NOT EXISTS local_inventario (
    id INTEGER PRIMARY KEY,
    nome_inventario TEXT,
    descricao TEXT,
    data_inicio DATE,
    data_fim DATE,
    status_inventario TEXT,
    percentual_conclusao DECIMAL(5,2),
    observacoes TEXT,
    is_local_only BOOLEAN DEFAULT FALSE,
    local_created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    local_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'PENDING',
    remote_id INTEGER
);

-- 2.3 ÍNDICES PARA OTIMIZAÇÃO
CREATE INDEX IF NOT EXISTS idx_sync_control_table_name ON sync_control(table_name);
CREATE INDEX IF NOT EXISTS idx_sync_control_status ON sync_control(sync_status);
CREATE INDEX IF NOT EXISTS idx_sync_control_timestamp ON sync_control(last_sync_timestamp);

CREATE INDEX IF NOT EXISTS idx_sync_metadata_entity ON sync_metadata(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_sync_metadata_status ON sync_metadata(sync_status);
CREATE INDEX IF NOT EXISTS idx_sync_metadata_timestamp ON sync_metadata(local_timestamp);

CREATE INDEX IF NOT EXISTS idx_local_patrimonio_numero ON local_patrimonio(numero_patrimonio);
CREATE INDEX IF NOT EXISTS idx_local_patrimonio_sync_status ON local_patrimonio(sync_status);
CREATE INDEX IF NOT EXISTS idx_local_patrimonio_local_only ON local_patrimonio(is_local_only);

CREATE INDEX IF NOT EXISTS idx_local_coleta_patrimonio ON local_coleta(patrimonio_id);
CREATE INDEX IF NOT EXISTS idx_local_coleta_inventario ON local_coleta(inventario_id);
CREATE INDEX IF NOT EXISTS idx_local_coleta_sync_status ON local_coleta(sync_status);

-- 2.4 VIEWS PARA FACILITAR CONSULTAS
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

ORDER BY last_modified DESC;

-- 2.5 TRIGGERS PARA ATUALIZAÇÃO AUTOMÁTICA
CREATE TRIGGER IF NOT EXISTS update_local_patrimonio_timestamp 
    AFTER UPDATE ON local_patrimonio
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_patrimonio 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS update_local_coleta_timestamp 
    AFTER UPDATE ON local_coleta
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_coleta 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS update_local_inventario_timestamp 
    AFTER UPDATE ON local_inventario
    FOR EACH ROW
    WHEN NEW.local_updated_at = OLD.local_updated_at
BEGIN
    UPDATE local_inventario 
    SET local_updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- =====================================================
-- 3. DADOS INICIAIS
-- =====================================================

-- 3.1 Dados iniciais para PostgreSQL
INSERT INTO TABELA_SETOR (NOME, DESCRICAO) VALUES 
('ADMINISTRAÇÃO', 'Setor administrativo da instituição'),
('TECNOLOGIA DA INFORMAÇÃO', 'Setor de TI e suporte técnico'),
('ENSINO', 'Setor de coordenação de ensino'),
('BIBLIOTECA', 'Biblioteca da instituição'),
('LABORATÓRIOS', 'Laboratórios de ensino e pesquisa'),
('ALMOXARIFADO', 'Setor de almoxarifado e estoque'),
('MANUTENÇÃO', 'Setor de manutenção predial'),
('RECURSOS HUMANOS', 'Setor de gestão de pessoas')
ON CONFLICT (NOME) DO NOTHING;

INSERT INTO TABELA_RESPONSAVEL (NOME, CARGO, EMAIL) VALUES 
('ADMINISTRADOR GERAL', 'Administrador', 'admin@ifmt.edu.br'),
('COORDENADOR TI', 'Coordenador de TI', 'ti@ifmt.edu.br'),
('COORDENADOR ENSINO', 'Coordenador de Ensino', 'ensino@ifmt.edu.br'),
('BIBLIOTECÁRIO CHEFE', 'Bibliotecário', 'biblioteca@ifmt.edu.br'),
('RESPONSÁVEL LABORATÓRIOS', 'Técnico de Laboratório', 'laboratorios@ifmt.edu.br')
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_SALA (DESCRICAO, NUMERO_SALA, ANDAR, BLOCO) VALUES 
('Sala de Administração', 'ADM-01', 1, 'A'),
('Sala de TI', 'TI-01', 1, 'A'),
('Biblioteca Principal', 'BIB-01', 1, 'B'),
('Laboratório de Informática 1', 'LAB-01', 2, 'C'),
('Laboratório de Informática 2', 'LAB-02', 2, 'C'),
('Almoxarifado Central', 'ALM-01', 0, 'A')
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_COLETOR (NOME_COLETOR, EMAIL) VALUES 
('COLETOR PRINCIPAL', 'coletor1@ifmt.edu.br'),
('COLETOR AUXILIAR', 'coletor2@ifmt.edu.br'),
('COLETOR LABORATÓRIOS', 'coletor.lab@ifmt.edu.br')
ON CONFLICT DO NOTHING;

INSERT INTO TABELA_USUARIO (LOGIN, SENHA_HASH, NOME_COMPLETO, EMAIL, PERFIL, PRIMEIRO_ACESSO) 
VALUES (
    'admin', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1VdLIqCRFpb6AQBaOLLqI.xjVgLOjHi', 
    'Administrador do Sistema', 
    'admin@sistema.com', 
    'ADMIN', 
    'S'
)
ON CONFLICT (LOGIN) DO NOTHING;

INSERT INTO TABELA_INVENTARIO (NOME, ANO, DATA_INICIO, RESPONSAVEL_INVENTARIO, OBSERVACAO) VALUES 
('Inventário Anual 2024', 2024, '2024-01-15', 'ADMINISTRADOR GERAL', 'Inventário anual completo de todos os patrimônios'),
('Inventário Laboratórios 2024', 2024, '2024-06-01', 'RESPONSÁVEL LABORATÓRIOS', 'Inventário específico dos laboratórios de ensino')
ON CONFLICT DO NOTHING;

-- 3.2 Dados iniciais para SQLite
INSERT OR IGNORE INTO sync_control (table_name, sync_direction) VALUES 
('local_patrimonio', 'BOTH'),
('local_coleta', 'BOTH'),
('local_inventario', 'BOTH'),
('local_participante_inventario', 'BOTH');

INSERT INTO offline_logs (log_level, message, component) 
VALUES ('INFO', 'Banco de dados SQLite offline inicializado com sucesso', 'SQLiteConnection');

-- =====================================================
-- 4. COMANDOS DE EXECUÇÃO
-- =====================================================

-- Para PostgreSQL:
-- psql -h localhost -U postgres -d sispatrimonio -f IMPLEMENTAR_TABELAS_COMPLETO.sql

-- Para SQLite:
-- sqlite3 inventario_offline.db < IMPLEMENTAR_TABELAS_COMPLETO.sql

-- =====================================================
-- 5. DOCUMENTAÇÃO DAS TABELAS
-- =====================================================

/*
ESTRUTURA DO BANCO DE DADOS:

1. TABELA_SETOR: Setores da instituição
   - ID: Identificador único
   - NOME: Nome do setor
   - DESCRICAO: Descrição detalhada
   - RESPONSAVEL_SETOR: Pessoa responsável
   - ATIVO: Status ativo/inativo

2. TABELA_RESPONSAVEL: Responsáveis pelos patrimônios
   - ID: Identificador único
   - NOME: Nome completo
   - CPF: CPF único
   - EMAIL: Email de contato
   - TELEFONE: Telefone de contato
   - CARGO: Função do responsável
   - ID_SETOR: Setor de pertencimento

3. TABELA_SALA: Salas/locais dos patrimônios
   - ID_SALA: Identificador único
   - DESCRICAO: Descrição da sala
   - NUMERO_SALA: Número identificador
   - ANDAR: Andar da sala
   - BLOCO: Bloco do prédio
   - ID_SETOR: Setor de pertencimento
   - CAPACIDADE: Capacidade da sala
   - AREA_M2: Área em metros quadrados

4. TABELA_INVENTARIO: Inventários realizados
   - ID: Identificador único
   - NOME: Nome do inventário
   - ANO: Ano de referência
   - DATA_INICIO: Data de início
   - DATA_FIM: Data de conclusão
   - STATUS_INVENTARIO: Status (PLANEJADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO)
   - PERCENTUAL_CONCLUSAO: Percentual de conclusão

5. TABELA_COLETOR: Coletores do inventário
   - ID: Identificador único
   - NOME_COLETOR: Nome do coletor
   - MATRICULA: Matrícula única
   - EMAIL: Email de contato
   - TELEFONE: Telefone de contato

6. TABELA_USUARIO: Usuários do sistema
   - ID: Identificador único
   - LOGIN: Login único
   - SENHA_HASH: Hash da senha (bcrypt)
   - NOME_COMPLETO: Nome completo
   - PERFIL: ADMIN, SUPERVISOR, COLETOR, CONSULTA
   - ATIVO: Status ativo/inativo

7. TABELA_PATRIMONIO: Patrimônios da instituição
   - ID: Identificador único
   - NUMERO: Número único do patrimônio
   - DESCRICAO: Descrição detalhada
   - MARCA: Marca do item
   - MODELO: Modelo do item
   - VALOR_AQUISICAO: Valor de aquisição
   - VALOR_DEPRECIADO: Valor depreciado
   - ID_RESPONSAVEL: Responsável pelo item
   - ID_SALA: Localização atual

8. TABELA_COLETA: Coletas realizadas
   - ID: Identificador único
   - ID_INVENTARIO: Referência ao inventário
   - ID_PATRIMONIO: Referência ao patrimônio
   - ID_USUARIO: Usuário que realizou a coleta
   - STATUS_COLETA: Status da coleta
   - DIVERGENCIA: Indica se houve divergência
   - LOCALIZACAO_ATUAL: Localização cadastrada
   - LOCALIZACAO_ENCONTRADA: Localização encontrada

9. TABELAS OFFLINE (SQLite):
   - sync_control: Controle de sincronização
   - sync_metadata: Metadados de sincronização
   - offline_logs: Logs do sistema offline
   - local_*: Tabelas espelho para uso offline
*/