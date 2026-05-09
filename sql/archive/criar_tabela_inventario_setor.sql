-- ============================================================
-- Script: criar_tabela_inventario_setor.sql
-- Objetivo: Criar tabela de relacionamento entre Inventário e Setor
-- Executar no PostgreSQL antes de usar a funcionalidade de escopo de setores
-- ============================================================

CREATE TABLE IF NOT EXISTS TABELA_INVENTARIO_SETOR (
    ID                    SERIAL PRIMARY KEY,
    ID_INVENTARIO         INTEGER NOT NULL,
    ID_SETOR              INTEGER,                -- NULL quando INCLUIR_TODOS_SETORES = TRUE
    INCLUIR_TODOS_SETORES BOOLEAN NOT NULL DEFAULT FALSE,
    DATA_INCLUSAO         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ATIVO                 BOOLEAN NOT NULL DEFAULT TRUE,
    OBSERVACOES           VARCHAR(500),
    CONSTRAINT fk_inv_setor_inventario
        FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID),
    CONSTRAINT fk_inv_setor_setor
        FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_inv_setor_inventario ON TABELA_INVENTARIO_SETOR(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_inv_setor_setor      ON TABELA_INVENTARIO_SETOR(ID_SETOR);
CREATE INDEX IF NOT EXISTS idx_inv_setor_ativo       ON TABELA_INVENTARIO_SETOR(ID_INVENTARIO, ATIVO);
CREATE INDEX IF NOT EXISTS idx_inv_setor_todos       ON TABELA_INVENTARIO_SETOR(ID_INVENTARIO, INCLUIR_TODOS_SETORES, ATIVO);

-- Verificação
SELECT 'Tabela TABELA_INVENTARIO_SETOR criada com sucesso.' AS status
WHERE EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_name = 'tabela_inventario_setor'
);
