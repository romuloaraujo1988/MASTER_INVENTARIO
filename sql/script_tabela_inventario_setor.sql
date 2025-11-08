-- Script para criação da TABELA_INVENTARIO_SETOR
-- Sistema de Inventário IFMT
-- Relaciona inventários com setores específicos para controle de escopo

-- Criar tabela de relacionamento inventário-setor
CREATE TABLE IF NOT EXISTS TABELA_INVENTARIO_SETOR (
    ID SERIAL PRIMARY KEY,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_SETOR INTEGER, -- NULL quando INCLUIR_TODOS_SETORES = TRUE
    INCLUIR_TODOS_SETORES BOOLEAN DEFAULT FALSE,
    DATA_INCLUSAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ATIVO BOOLEAN DEFAULT TRUE,
    OBSERVACOES TEXT,
    
    -- Chaves estrangeiras
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_SETOR) REFERENCES TABELA_SETOR(ID),
    
    -- Constraint para evitar duplicatas
    UNIQUE(ID_INVENTARIO, ID_SETOR),
    
    -- Constraint para garantir que quando INCLUIR_TODOS_SETORES = TRUE, ID_SETOR deve ser NULL
    CHECK (
        (INCLUIR_TODOS_SETORES = TRUE AND ID_SETOR IS NULL) OR
        (INCLUIR_TODOS_SETORES = FALSE AND ID_SETOR IS NOT NULL)
    )
);

-- Índices para melhorar performance
CREATE INDEX IF NOT EXISTS IDX_INVENTARIO_SETOR_INVENTARIO 
    ON TABELA_INVENTARIO_SETOR(ID_INVENTARIO);

CREATE INDEX IF NOT EXISTS IDX_INVENTARIO_SETOR_SETOR 
    ON TABELA_INVENTARIO_SETOR(ID_SETOR);

CREATE INDEX IF NOT EXISTS IDX_INVENTARIO_SETOR_ATIVO 
    ON TABELA_INVENTARIO_SETOR(ATIVO);

CREATE INDEX IF NOT EXISTS IDX_INVENTARIO_SETOR_TODOS 
    ON TABELA_INVENTARIO_SETOR(INCLUIR_TODOS_SETORES);

-- Índice composto para consultas frequentes
CREATE INDEX IF NOT EXISTS IDX_INVENTARIO_SETOR_INVENTARIO_ATIVO 
    ON TABELA_INVENTARIO_SETOR(ID_INVENTARIO, ATIVO);

-- Comentários na tabela e campos
COMMENT ON TABLE TABELA_INVENTARIO_SETOR IS 'Relaciona inventários com setores específicos para controle de escopo';
COMMENT ON COLUMN TABELA_INVENTARIO_SETOR.ID IS 'Identificador único do relacionamento';
COMMENT ON COLUMN TABELA_INVENTARIO_SETOR.ID_INVENTARIO IS 'Referência ao inventário';
COMMENT ON COLUMN TABELA_INVENTARIO_SETOR.ID_SETOR IS 'Referência ao setor (NULL quando incluir todos)';
COMMENT ON COLUMN TABELA_INVENTARIO_SETOR.INCLUIR_TODOS_SETORES IS 'Indica se o inventário inclui todos os setores';
COMMENT ON COLUMN TABELA_INVENTARIO_SETOR.DATA_INCLUSAO IS 'Data de inclusão do relacionamento';
COMMENT ON COLUMN TABELA_INVENTARIO_SETOR.ATIVO IS 'Indica se o relacionamento está ativo';
COMMENT ON COLUMN TABELA_INVENTARIO_SETOR.OBSERVACOES IS 'Observações sobre o relacionamento';

-- View para facilitar consultas de setores por inventário
CREATE OR REPLACE VIEW VW_INVENTARIO_SETORES AS
SELECT 
    i.ID as id_inventario,
    i.NOME as nome_inventario,
    i.STATUS_INVENTARIO,
    CASE 
        WHEN BOOL_OR(ise.INCLUIR_TODOS_SETORES) = TRUE THEN 'TODOS OS SETORES'
        ELSE STRING_AGG(s.NOME, ', ' ORDER BY s.NOME)
    END as setores_incluidos,
    BOOL_OR(ise.INCLUIR_TODOS_SETORES) as incluir_todos_setores,
    COUNT(CASE WHEN ise.ID_SETOR IS NOT NULL THEN 1 END) as quantidade_setores_especificos
FROM TABELA_INVENTARIO i
LEFT JOIN TABELA_INVENTARIO_SETOR ise ON i.ID = ise.ID_INVENTARIO AND ise.ATIVO = TRUE
LEFT JOIN TABELA_SETOR s ON ise.ID_SETOR = s.ID
GROUP BY i.ID, i.NOME, i.STATUS_INVENTARIO
ORDER BY i.ID;

COMMENT ON VIEW VW_INVENTARIO_SETORES IS 'View que mostra os setores selecionados para cada inventário';

-- View para listar salas do escopo do inventário
CREATE OR REPLACE VIEW VW_SALAS_ESCOPO_INVENTARIO AS
SELECT DISTINCT
    i.ID as id_inventario,
    i.NOME as nome_inventario,
    sl.ID_SALA,
    sl.DESCRICAO as descricao_sala,
    s.ID as id_setor,
    s.NOME as nome_setor
FROM TABELA_INVENTARIO i
INNER JOIN TABELA_INVENTARIO_SETOR ise ON i.ID = ise.ID_INVENTARIO AND ise.ATIVO = TRUE
LEFT JOIN TABELA_SETOR s ON (
    (ise.INCLUIR_TODOS_SETORES = FALSE AND ise.ID_SETOR = s.ID) OR
    (ise.INCLUIR_TODOS_SETORES = TRUE)
)
INNER JOIN TABELA_SALA sl ON (
    (ise.INCLUIR_TODOS_SETORES = FALSE AND sl.ID_SETOR = s.ID) OR
    (ise.INCLUIR_TODOS_SETORES = TRUE AND sl.ID_SETOR IS NOT NULL)
)
WHERE 
    CASE 
        WHEN ise.INCLUIR_TODOS_SETORES = TRUE THEN TRUE
        ELSE s.ID IS NOT NULL
    END
ORDER BY i.ID, s.NOME, sl.DESCRICAO;

COMMENT ON VIEW VW_SALAS_ESCOPO_INVENTARIO IS 'View que lista todas as salas que fazem parte do escopo de cada inventário';

-- Função para verificar se um setor está no escopo do inventário
CREATE OR REPLACE FUNCTION fn_setor_no_escopo_inventario(
    p_id_inventario INTEGER,
    p_id_setor INTEGER
) RETURNS BOOLEAN AS $$
DECLARE
    v_incluir_todos BOOLEAN;
    v_setor_especifico INTEGER;
BEGIN
    -- Verificar se inclui todos os setores
    SELECT INCLUIR_TODOS_SETORES INTO v_incluir_todos
    FROM TABELA_INVENTARIO_SETOR
    WHERE ID_INVENTARIO = p_id_inventario 
      AND INCLUIR_TODOS_SETORES = TRUE 
      AND ATIVO = TRUE
    LIMIT 1;
    
    IF v_incluir_todos = TRUE THEN
        RETURN TRUE;
    END IF;
    
    -- Verificar se o setor específico está selecionado
    SELECT ID_SETOR INTO v_setor_especifico
    FROM TABELA_INVENTARIO_SETOR
    WHERE ID_INVENTARIO = p_id_inventario 
      AND ID_SETOR = p_id_setor 
      AND ATIVO = TRUE
    LIMIT 1;
    
    RETURN v_setor_especifico IS NOT NULL;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_setor_no_escopo_inventario IS 'Função que verifica se um setor está no escopo de um inventário';

-- Função para obter setores do inventário
CREATE OR REPLACE FUNCTION fn_obter_setores_inventario(
    p_id_inventario INTEGER
) RETURNS TABLE(
    id_setor INTEGER,
    nome_setor VARCHAR(255),
    incluir_todos BOOLEAN
) AS $$
BEGIN
    -- Verificar se inclui todos os setores
    IF EXISTS (
        SELECT 1 FROM TABELA_INVENTARIO_SETOR 
        WHERE ID_INVENTARIO = p_id_inventario 
          AND INCLUIR_TODOS_SETORES = TRUE 
          AND ATIVO = TRUE
    ) THEN
        -- Retornar todos os setores
        RETURN QUERY
        SELECT s.ID, s.NOME, TRUE as incluir_todos
        FROM TABELA_SETOR s
        ORDER BY s.NOME;
    ELSE
        -- Retornar apenas setores específicos
        RETURN QUERY
        SELECT s.ID, s.NOME, FALSE as incluir_todos
        FROM TABELA_INVENTARIO_SETOR ise
        INNER JOIN TABELA_SETOR s ON ise.ID_SETOR = s.ID
        WHERE ise.ID_INVENTARIO = p_id_inventario 
          AND ise.ATIVO = TRUE
        ORDER BY s.NOME;
    END IF;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_obter_setores_inventario IS 'Função que retorna os setores de um inventário';

-- Trigger para validar dados antes da inserção
CREATE OR REPLACE FUNCTION fn_validar_inventario_setor()
RETURNS TRIGGER AS $$
BEGIN
    -- Validar se inventário existe
    IF NOT EXISTS (SELECT 1 FROM TABELA_INVENTARIO WHERE ID = NEW.ID_INVENTARIO) THEN
        RAISE EXCEPTION 'Inventário com ID % não existe', NEW.ID_INVENTARIO;
    END IF;
    
    -- Validar se setor existe (quando não for incluir todos)
    IF NEW.INCLUIR_TODOS_SETORES = FALSE AND NEW.ID_SETOR IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM TABELA_SETOR WHERE ID = NEW.ID_SETOR) THEN
            RAISE EXCEPTION 'Setor com ID % não existe', NEW.ID_SETOR;
        END IF;
    END IF;
    
    -- Validar se já existe configuração "incluir todos" para este inventário
    IF NEW.INCLUIR_TODOS_SETORES = TRUE THEN
        IF EXISTS (
            SELECT 1 FROM TABELA_INVENTARIO_SETOR 
            WHERE ID_INVENTARIO = NEW.ID_INVENTARIO 
              AND INCLUIR_TODOS_SETORES = TRUE 
              AND ATIVO = TRUE
              AND ID != COALESCE(NEW.ID, -1)
        ) THEN
            RAISE EXCEPTION 'Já existe configuração "incluir todos setores" para este inventário';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Criar trigger
DROP TRIGGER IF EXISTS tg_validar_inventario_setor ON TABELA_INVENTARIO_SETOR;
CREATE TRIGGER tg_validar_inventario_setor
    BEFORE INSERT OR UPDATE ON TABELA_INVENTARIO_SETOR
    FOR EACH ROW EXECUTE FUNCTION fn_validar_inventario_setor();

-- Inserir dados de exemplo para inventários existentes (incluir todos os setores por padrão)
INSERT INTO TABELA_INVENTARIO_SETOR (ID_INVENTARIO, ID_SETOR, INCLUIR_TODOS_SETORES, OBSERVACOES)
SELECT 
    i.ID,
    NULL,
    TRUE,
    'Configuração padrão - migração automática'
FROM TABELA_INVENTARIO i
WHERE NOT EXISTS (
    SELECT 1 FROM TABELA_INVENTARIO_SETOR ise 
    WHERE ise.ID_INVENTARIO = i.ID
)
ON CONFLICT (ID_INVENTARIO, ID_SETOR) DO NOTHING;