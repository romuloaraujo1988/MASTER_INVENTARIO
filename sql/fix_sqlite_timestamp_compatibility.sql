-- ==================================================================================
-- Script de Correção: Compatibilidade de Timestamps SQLite
-- Sistema de Inventário de Patrimônio
-- Data: 21/11/2025
-- 
-- PROBLEMA: SQLite armazena timestamps como TEXT, mas o código Java espera formato
--           específico compatível com Timestamp.valueOf()
-- 
-- SOLUÇÃO: Ajustar estrutura e dados para formato compatível
-- ==================================================================================

-- ==================== BACKUP DAS TABELAS EXISTENTES ====================

-- Criar backup da tabela SALA_INVENTARIO se existir
CREATE TABLE IF NOT EXISTS SALA_INVENTARIO_BACKUP AS 
SELECT * FROM SALA_INVENTARIO WHERE 1=0;

INSERT INTO SALA_INVENTARIO_BACKUP SELECT * FROM SALA_INVENTARIO;

-- ==================== RECRIAR TABELA SALA_INVENTARIO ====================

-- Dropar tabela antiga
DROP TABLE IF EXISTS SALA_INVENTARIO;

-- Recriar com estrutura corrigida
CREATE TABLE SALA_INVENTARIO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    ID_SALA INTEGER NOT NULL,
    ID_INVENTARIO INTEGER NOT NULL,
    STATUS TEXT DEFAULT 'ABERTA' CHECK(STATUS IN ('ABERTA', 'FINALIZADA', 'CANCELADA')),
    
    -- Timestamps no formato compatível com Java: 'YYYY-MM-DD HH:MM:SS.SSS'
    DATA_INICIO TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')),
    DATA_FIM TEXT,
    
    ID_PARTICIPANTE_INICIOU INTEGER,
    ID_PARTICIPANTE_FINALIZOU INTEGER,
    
    TOTAL_ITENS INTEGER DEFAULT 0,
    ITENS_SEM_ETIQUETA INTEGER DEFAULT 0,
    OBSERVACOES TEXT,
    
    -- Campos adicionais para compatibilidade
    COLETA_FINALIZADA BOOLEAN DEFAULT FALSE,
    DATA_INICIO_COLETA TEXT,
    DATA_FINALIZACAO_COLETA TEXT,
    OBSERVACOES_FINALIZACAO TEXT,
    TOTAL_ITENS_COLETADOS INTEGER DEFAULT 0,
    TOTAL_ITENS_SEM_ETIQUETA INTEGER DEFAULT 0,
    PERCENTUAL_CONCLUSAO DECIMAL(5,2) DEFAULT 0.00,
    STATUS_COLETA TEXT DEFAULT 'ABERTA',
    
    -- Constraint de unicidade
    UNIQUE(ID_SALA, ID_INVENTARIO)
);

-- ==================== RESTAURAR DADOS DO BACKUP ====================

-- Restaurar dados convertendo timestamps para formato correto
INSERT INTO SALA_INVENTARIO (
    ID, ID_SALA, ID_INVENTARIO, STATUS,
    DATA_INICIO, DATA_FIM,
    ID_PARTICIPANTE_INICIOU, ID_PARTICIPANTE_FINALIZOU,
    TOTAL_ITENS, ITENS_SEM_ETIQUETA, OBSERVACOES,
    COLETA_FINALIZADA, DATA_INICIO_COLETA, DATA_FINALIZACAO_COLETA,
    OBSERVACOES_FINALIZACAO, TOTAL_ITENS_COLETADOS, TOTAL_ITENS_SEM_ETIQUETA,
    PERCENTUAL_CONCLUSAO, STATUS_COLETA
)
SELECT 
    ID, ID_SALA, ID_INVENTARIO, 
    COALESCE(STATUS, 'ABERTA'),
    
    -- Converter DATA_INICIO para formato correto
    CASE 
        WHEN DATA_INICIO IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_INICIO)
        ELSE 
            strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')
    END,
    
    -- Converter DATA_FIM para formato correto
    CASE 
        WHEN DATA_FIM IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_FIM)
        ELSE 
            NULL
    END,
    
    ID_PARTICIPANTE_INICIOU, ID_PARTICIPANTE_FINALIZOU,
    COALESCE(TOTAL_ITENS, 0), 
    COALESCE(ITENS_SEM_ETIQUETA, 0), 
    OBSERVACOES,
    
    -- Campos adicionais
    COALESCE(COLETA_FINALIZADA, FALSE),
    
    CASE 
        WHEN DATA_INICIO_COLETA IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_INICIO_COLETA)
        ELSE 
            NULL
    END,
    
    CASE 
        WHEN DATA_FINALIZACAO_COLETA IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_FINALIZACAO_COLETA)
        ELSE 
            NULL
    END,
    
    OBSERVACOES_FINALIZACAO,
    COALESCE(TOTAL_ITENS_COLETADOS, 0),
    COALESCE(TOTAL_ITENS_SEM_ETIQUETA, 0),
    COALESCE(PERCENTUAL_CONCLUSAO, 0.00),
    COALESCE(STATUS_COLETA, 'ABERTA')
FROM SALA_INVENTARIO_BACKUP;

-- ==================== RECRIAR ÍNDICES ====================

CREATE INDEX IF NOT EXISTS idx_sala_inventario_sala ON SALA_INVENTARIO(ID_SALA);
CREATE INDEX IF NOT EXISTS idx_sala_inventario_inventario ON SALA_INVENTARIO(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_sala_inventario_status ON SALA_INVENTARIO(STATUS);
CREATE INDEX IF NOT EXISTS idx_sala_inventario_coleta_finalizada ON SALA_INVENTARIO(COLETA_FINALIZADA);

-- ==================== CORRIGIR OUTRAS TABELAS COM TIMESTAMPS ====================

-- Corrigir tabela COLETA
CREATE TABLE IF NOT EXISTS COLETA_BACKUP AS SELECT * FROM COLETA WHERE 1=0;
INSERT INTO COLETA_BACKUP SELECT * FROM COLETA;

DROP TABLE IF EXISTS COLETA;

CREATE TABLE COLETA (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_PATRIMONIO INTEGER,
    ID_COLETOR INTEGER,
    ID_PARTICIPANTE_INVENTARIO INTEGER,
    
    -- Timestamp no formato compatível: 'YYYY-MM-DD HH:MM:SS.SSS'
    DATA_COLETA TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S.%f', 'now', 'localtime')),
    
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

-- Restaurar dados com timestamps corrigidos
INSERT INTO COLETA SELECT 
    ID, ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, ID_PARTICIPANTE_INVENTARIO,
    CASE 
        WHEN DATA_COLETA IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S.%f', DATA_COLETA)
        ELSE 
            strftime('%Y-%m-%d %H:%M:%S.%f', 'now', 'localtime')
    END,
    STATUS_COLETA, OBSERVACAO_COLETA, LOCALIZACAO_ATUAL, LOCALIZACAO_ENCONTRADA,
    ESTADO_ENCONTRADO, DIVERGENCIA, MOTIVO_DIVERGENCIA, SEM_ETIQUETA,
    DESCRICAO_ITEM_SEM_ETIQUETA, CATEGORIA_ITEM_SEM_ETIQUETA,
    NUMERO_PATRIMONIO, DESCRICAO_PATRIMONIO
FROM COLETA_BACKUP;

-- Recriar índices da COLETA
CREATE INDEX IF NOT EXISTS idx_coleta_inventario ON COLETA(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio ON COLETA(ID_PATRIMONIO);
CREATE INDEX IF NOT EXISTS idx_coleta_data ON COLETA(DATA_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_sem_etiqueta ON COLETA(SEM_ETIQUETA);
CREATE INDEX IF NOT EXISTS idx_coleta_localizacao ON COLETA(LOCALIZACAO_ENCONTRADA);

-- Corrigir tabela SALA
CREATE TABLE IF NOT EXISTS SALA_BACKUP AS SELECT * FROM SALA WHERE 1=0;
INSERT INTO SALA_BACKUP SELECT * FROM SALA;

DROP TABLE IF EXISTS SALA;

CREATE TABLE SALA (
    ID_SALA INTEGER PRIMARY KEY AUTOINCREMENT,
    NUMERO_SALA TEXT NOT NULL,
    NOME_SALA TEXT,
    ANDAR TEXT,
    BLOCO TEXT,
    CAPACIDADE INTEGER,
    TIPO_SALA TEXT,
    ATIVA BOOLEAN DEFAULT TRUE,
    DATA_CADASTRO TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))
);

-- Restaurar dados
INSERT INTO SALA SELECT 
    ID_SALA, NUMERO_SALA, NOME_SALA, ANDAR, BLOCO, CAPACIDADE, TIPO_SALA, ATIVA,
    CASE 
        WHEN DATA_CADASTRO IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_CADASTRO)
        ELSE 
            strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')
    END
FROM SALA_BACKUP;

-- Recriar índices da SALA
CREATE INDEX IF NOT EXISTS idx_sala_numero ON SALA(NUMERO_SALA);
CREATE INDEX IF NOT EXISTS idx_sala_ativa ON SALA(ATIVA);

-- Corrigir tabela TABELA_INVENTARIO
CREATE TABLE IF NOT EXISTS TABELA_INVENTARIO_BACKUP AS SELECT * FROM TABELA_INVENTARIO WHERE 1=0;
INSERT INTO TABELA_INVENTARIO_BACKUP SELECT * FROM TABELA_INVENTARIO;

DROP TABLE IF EXISTS TABELA_INVENTARIO;

CREATE TABLE TABELA_INVENTARIO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    NOME TEXT NOT NULL,
    ANO INTEGER,
    DATA_INICIO TEXT NOT NULL,
    DATA_FIM TEXT,
    OBSERVACAO TEXT,
    STATUS_INVENTARIO TEXT DEFAULT 'PLANEJADO',
    RESPONSAVEL_INVENTARIO TEXT,
    TOTAL_PATRIMONIOS INTEGER DEFAULT 0,
    PATRIMONIOS_COLETADOS INTEGER DEFAULT 0,
    PERCENTUAL_CONCLUSAO DECIMAL(5,2) DEFAULT 0.00,
    DATA_CRIACAO TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')),
    DATA_ULTIMA_ATUALIZACAO TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))
);

-- Restaurar dados
INSERT INTO TABELA_INVENTARIO SELECT 
    ID, NOME, ANO,
    CASE 
        WHEN DATA_INICIO IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_INICIO)
        ELSE 
            strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')
    END,
    CASE 
        WHEN DATA_FIM IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_FIM)
        ELSE 
            NULL
    END,
    OBSERVACAO, STATUS_INVENTARIO, RESPONSAVEL_INVENTARIO,
    TOTAL_PATRIMONIOS, PATRIMONIOS_COLETADOS, PERCENTUAL_CONCLUSAO,
    CASE 
        WHEN DATA_CRIACAO IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_CRIACAO)
        ELSE 
            strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')
    END,
    CASE 
        WHEN DATA_ULTIMA_ATUALIZACAO IS NOT NULL THEN 
            strftime('%Y-%m-%d %H:%M:%S', DATA_ULTIMA_ATUALIZACAO)
        ELSE 
            strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')
    END
FROM TABELA_INVENTARIO_BACKUP;

-- Recriar índices
CREATE INDEX IF NOT EXISTS idx_inventario_status ON TABELA_INVENTARIO(STATUS_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_inventario_data_inicio ON TABELA_INVENTARIO(DATA_INICIO);

-- ==================== TRIGGERS PARA MANTER FORMATO CORRETO ====================

-- Trigger para garantir formato correto ao inserir em SALA_INVENTARIO
CREATE TRIGGER IF NOT EXISTS trg_sala_inventario_insert_format
AFTER INSERT ON SALA_INVENTARIO
FOR EACH ROW
WHEN NEW.DATA_INICIO IS NOT NULL AND NEW.DATA_INICIO NOT LIKE '____-__-__ __:__:__'
BEGIN
    UPDATE SALA_INVENTARIO 
    SET DATA_INICIO = strftime('%Y-%m-%d %H:%M:%S', NEW.DATA_INICIO)
    WHERE ID = NEW.ID;
END;

-- Trigger para garantir formato correto ao atualizar SALA_INVENTARIO
CREATE TRIGGER IF NOT EXISTS trg_sala_inventario_update_format
AFTER UPDATE ON SALA_INVENTARIO
FOR EACH ROW
WHEN NEW.DATA_FIM IS NOT NULL AND NEW.DATA_FIM NOT LIKE '____-__-__ __:__:__'
BEGIN
    UPDATE SALA_INVENTARIO 
    SET DATA_FIM = strftime('%Y-%m-%d %H:%M:%S', NEW.DATA_FIM)
    WHERE ID = NEW.ID;
END;

-- Trigger para garantir formato correto ao inserir em COLETA
CREATE TRIGGER IF NOT EXISTS trg_coleta_insert_format
AFTER INSERT ON COLETA
FOR EACH ROW
WHEN NEW.DATA_COLETA IS NOT NULL AND NEW.DATA_COLETA NOT LIKE '____-__-__ __:__:__.__'
BEGIN
    UPDATE COLETA 
    SET DATA_COLETA = strftime('%Y-%m-%d %H:%M:%S.%f', NEW.DATA_COLETA)
    WHERE ID = NEW.ID;
END;

-- ==================== FUNÇÕES AUXILIARES ====================

-- View para verificar formato de timestamps
CREATE VIEW IF NOT EXISTS v_timestamp_validation AS
SELECT 
    'SALA_INVENTARIO' as tabela,
    ID as registro_id,
    DATA_INICIO as timestamp_value,
    CASE 
        WHEN DATA_INICIO LIKE '____-__-__ __:__:__' THEN 'OK'
        ELSE 'FORMATO_INVALIDO'
    END as status_formato
FROM SALA_INVENTARIO
WHERE DATA_INICIO IS NOT NULL

UNION ALL

SELECT 
    'COLETA' as tabela,
    ID as registro_id,
    DATA_COLETA as timestamp_value,
    CASE 
        WHEN DATA_COLETA LIKE '____-__-__ __:__:__.__' THEN 'OK'
        ELSE 'FORMATO_INVALIDO'
    END as status_formato
FROM COLETA
WHERE DATA_COLETA IS NOT NULL;

-- ==================== LIMPEZA ====================

-- Remover tabelas de backup após confirmação
-- DROP TABLE IF EXISTS SALA_INVENTARIO_BACKUP;
-- DROP TABLE IF EXISTS COLETA_BACKUP;
-- DROP TABLE IF EXISTS SALA_BACKUP;
-- DROP TABLE IF EXISTS TABELA_INVENTARIO_BACKUP;

-- ==================== LOG DE EXECUÇÃO ====================

INSERT INTO offline_logs (log_level, message, component, details) 
VALUES (
    'INFO', 
    'Script de correção de timestamps executado com sucesso',
    'SQLiteTimestampFix',
    'Tabelas corrigidas: SALA_INVENTARIO, COLETA, SALA, TABELA_INVENTARIO. Formato: YYYY-MM-DD HH:MM:SS'
);

-- ==================== VERIFICAÇÃO FINAL ====================

-- Consultar registros com formato inválido
SELECT * FROM v_timestamp_validation WHERE status_formato = 'FORMATO_INVALIDO';

-- Se não houver resultados, todos os timestamps estão no formato correto!

-- ==================== DOCUMENTAÇÃO ====================

/*
FORMATO DE TIMESTAMPS COMPATÍVEL COM JAVA:

SQLite armazena timestamps como TEXT. Para compatibilidade com Java Timestamp.valueOf():

FORMATO CORRETO:
- Data/Hora: 'YYYY-MM-DD HH:MM:SS' (ex: '2024-11-21 14:30:00')
- Data/Hora com milissegundos: 'YYYY-MM-DD HH:MM:SS.SSS' (ex: '2024-11-21 14:30:00.123')

FORMATO INCORRETO:
- ISO 8601: '2024-11-21T14:30:00Z'
- Unix timestamp: 1700582400
- Formato brasileiro: '21/11/2024 14:30:00'

FUNÇÕES SQLite ÚTEIS:
- strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime') - Timestamp atual
- strftime('%Y-%m-%d %H:%M:%S.%f', 'now', 'localtime') - Com milissegundos
- datetime('now', 'localtime') - Datetime atual

CONVERSÃO EM JAVA:
- Timestamp.valueOf("2024-11-21 14:30:00") ✓ Funciona
- Timestamp.valueOf("2024-11-21T14:30:00Z") ✗ Erro de parsing

TRIGGERS:
- Garantem que novos registros sempre usem formato correto
- Convertem automaticamente timestamps em formato incorreto

VALIDAÇÃO:
- View v_timestamp_validation mostra registros com formato inválido
- Execute: SELECT * FROM v_timestamp_validation WHERE status_formato = 'FORMATO_INVALIDO';
*/
