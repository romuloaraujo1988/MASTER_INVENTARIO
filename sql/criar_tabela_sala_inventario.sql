-- ============================================================================
-- TABELA PARA CONTROLE DE STATUS DE COLETA POR SALA
-- Sistema de Inventário IFMT
-- ============================================================================
-- 
-- Esta tabela permite controlar o status de coleta de cada sala em um inventário
-- Quando uma sala tem sua coleta finalizada, ela não deve aparecer no app mobile
--
-- ============================================================================

-- Criar tabela de controle de sala por inventário
CREATE TABLE IF NOT EXISTS TABELA_SALA_INVENTARIO (
    ID SERIAL PRIMARY KEY,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_SALA INTEGER NOT NULL,
    STATUS_COLETA VARCHAR(50) DEFAULT 'PENDENTE',
    DATA_INICIO TIMESTAMP,
    DATA_FINALIZACAO TIMESTAMP,
    TOTAL_PATRIMONIOS INTEGER DEFAULT 0,
    PATRIMONIOS_COLETADOS INTEGER DEFAULT 0,
    PERCENTUAL_CONCLUSAO DECIMAL(5,2) DEFAULT 0.00,
    OBSERVACOES TEXT,
    FINALIZADO_POR INTEGER, -- ID do usuário que finalizou
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DATA_ULTIMA_ATUALIZACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_sala_inventario_inventario FOREIGN KEY (ID_INVENTARIO) 
        REFERENCES TABELA_INVENTARIO(ID) ON DELETE CASCADE,
    CONSTRAINT fk_sala_inventario_sala FOREIGN KEY (ID_SALA) 
        REFERENCES TABELA_SALA(ID_SALA) ON DELETE CASCADE,
    CONSTRAINT fk_sala_inventario_usuario FOREIGN KEY (FINALIZADO_POR) 
        REFERENCES TABELA_USUARIO(ID) ON DELETE SET NULL,
    
    -- Garantir que não haja duplicatas (uma sala por inventário)
    CONSTRAINT uk_sala_inventario UNIQUE (ID_INVENTARIO, ID_SALA),
    
    -- Validar status
    CONSTRAINT chk_status_coleta CHECK (STATUS_COLETA IN ('PENDENTE', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA'))
);

-- Índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_sala_inventario_inventario ON TABELA_SALA_INVENTARIO(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_sala_inventario_sala ON TABELA_SALA_INVENTARIO(ID_SALA);
CREATE INDEX IF NOT EXISTS idx_sala_inventario_status ON TABELA_SALA_INVENTARIO(STATUS_COLETA);
CREATE INDEX IF NOT EXISTS idx_sala_inventario_finalizado ON TABELA_SALA_INVENTARIO(ID_INVENTARIO, STATUS_COLETA) 
    WHERE STATUS_COLETA = 'FINALIZADA';

-- Comentários para documentação
COMMENT ON TABLE TABELA_SALA_INVENTARIO IS 'Controle de status de coleta por sala em cada inventário';
COMMENT ON COLUMN TABELA_SALA_INVENTARIO.STATUS_COLETA IS 'Status da coleta: PENDENTE, EM_ANDAMENTO, FINALIZADA, CANCELADA';
COMMENT ON COLUMN TABELA_SALA_INVENTARIO.DATA_FINALIZACAO IS 'Data e hora em que a coleta da sala foi finalizada';
COMMENT ON COLUMN TABELA_SALA_INVENTARIO.FINALIZADO_POR IS 'ID do usuário que finalizou a coleta da sala';

-- Trigger para atualizar data de última atualização
CREATE OR REPLACE FUNCTION update_sala_inventario_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_sala_inventario_timestamp
    BEFORE UPDATE ON TABELA_SALA_INVENTARIO
    FOR EACH ROW
    EXECUTE FUNCTION update_sala_inventario_timestamp();

-- Trigger para atualizar estatísticas automaticamente
CREATE OR REPLACE FUNCTION update_sala_inventario_stats()
RETURNS TRIGGER AS $$
BEGIN
    -- Atualizar contadores quando uma coleta é inserida ou atualizada
    UPDATE TABELA_SALA_INVENTARIO
    SET 
        PATRIMONIOS_COLETADOS = (
            SELECT COUNT(DISTINCT c.ID_PATRIMONIO)
            FROM TABELA_COLETA c
            JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID
            WHERE c.ID_INVENTARIO = NEW.ID_INVENTARIO
            AND p.ID_SALA = NEW.ID_SALA
        ),
        PERCENTUAL_CONCLUSAO = CASE 
            WHEN TOTAL_PATRIMONIOS > 0 THEN 
                (PATRIMONIOS_COLETADOS::DECIMAL / TOTAL_PATRIMONIOS::DECIMAL) * 100
            ELSE 0
        END
    WHERE ID_INVENTARIO = NEW.ID_INVENTARIO
    AND ID_SALA = (
        SELECT ID_SALA 
        FROM TABELA_PATRIMONIO 
        WHERE ID = NEW.ID_PATRIMONIO
    );
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_sala_inventario_stats
    AFTER INSERT OR UPDATE ON TABELA_COLETA
    FOR EACH ROW
    EXECUTE FUNCTION update_sala_inventario_stats();

-- View para facilitar consultas
CREATE OR REPLACE VIEW vw_salas_inventario_status AS
SELECT 
    si.ID,
    si.ID_INVENTARIO,
    i.NOME AS NOME_INVENTARIO,
    si.ID_SALA,
    s.NUMERO_SALA,
    s.DESCRICAO AS DESCRICAO_SALA,
    si.STATUS_COLETA,
    si.DATA_INICIO,
    si.DATA_FINALIZACAO,
    si.TOTAL_PATRIMONIOS,
    si.PATRIMONIOS_COLETADOS,
    si.PERCENTUAL_CONCLUSAO,
    si.OBSERVACOES,
    u.NOME_COMPLETO AS FINALIZADO_POR_NOME,
    si.DATA_CRIACAO,
    si.DATA_ULTIMA_ATUALIZACAO
FROM TABELA_SALA_INVENTARIO si
JOIN TABELA_INVENTARIO i ON si.ID_INVENTARIO = i.ID
JOIN TABELA_SALA s ON si.ID_SALA = s.ID_SALA
LEFT JOIN TABELA_USUARIO u ON si.FINALIZADO_POR = u.ID;

COMMENT ON VIEW vw_salas_inventario_status IS 'View com informações completas do status de coleta por sala';

-- Função para inicializar salas de um inventário
CREATE OR REPLACE FUNCTION inicializar_salas_inventario(p_id_inventario INTEGER)
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    -- Inserir todas as salas ativas para o inventário
    INSERT INTO TABELA_SALA_INVENTARIO (ID_INVENTARIO, ID_SALA, STATUS_COLETA, TOTAL_PATRIMONIOS)
    SELECT 
        p_id_inventario,
        s.ID_SALA,
        'PENDENTE',
        COUNT(p.ID)
    FROM TABELA_SALA s
    LEFT JOIN TABELA_PATRIMONIO p ON s.ID_SALA = p.ID_SALA AND p.ATIVO = TRUE
    WHERE s.ATIVO = TRUE
    GROUP BY s.ID_SALA
    ON CONFLICT (ID_INVENTARIO, ID_SALA) DO NOTHING;
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION inicializar_salas_inventario IS 'Inicializa todas as salas ativas para um inventário';

-- Função para finalizar coleta de uma sala
CREATE OR REPLACE FUNCTION finalizar_coleta_sala(
    p_id_inventario INTEGER,
    p_id_sala INTEGER,
    p_id_usuario INTEGER,
    p_observacoes TEXT DEFAULT NULL
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE TABELA_SALA_INVENTARIO
    SET 
        STATUS_COLETA = 'FINALIZADA',
        DATA_FINALIZACAO = CURRENT_TIMESTAMP,
        FINALIZADO_POR = p_id_usuario,
        OBSERVACOES = COALESCE(p_observacoes, OBSERVACOES)
    WHERE ID_INVENTARIO = p_id_inventario
    AND ID_SALA = p_id_sala;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION finalizar_coleta_sala IS 'Finaliza a coleta de uma sala específica em um inventário';

-- Função para reabrir coleta de uma sala
CREATE OR REPLACE FUNCTION reabrir_coleta_sala(
    p_id_inventario INTEGER,
    p_id_sala INTEGER
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE TABELA_SALA_INVENTARIO
    SET 
        STATUS_COLETA = 'EM_ANDAMENTO',
        DATA_FINALIZACAO = NULL,
        FINALIZADO_POR = NULL
    WHERE ID_INVENTARIO = p_id_inventario
    AND ID_SALA = p_id_sala;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION reabrir_coleta_sala IS 'Reabre a coleta de uma sala que foi finalizada';

-- Exemplos de uso:
-- 
-- 1. Inicializar salas para um inventário:
--    SELECT inicializar_salas_inventario(1);
--
-- 2. Finalizar coleta de uma sala:
--    SELECT finalizar_coleta_sala(1, 10, 5, 'Coleta concluída com sucesso');
--
-- 3. Reabrir coleta de uma sala:
--    SELECT reabrir_coleta_sala(1, 10);
--
-- 4. Listar salas pendentes de um inventário:
--    SELECT * FROM vw_salas_inventario_status 
--    WHERE ID_INVENTARIO = 1 AND STATUS_COLETA != 'FINALIZADA';
--
-- 5. Listar salas finalizadas:
--    SELECT * FROM vw_salas_inventario_status 
--    WHERE ID_INVENTARIO = 1 AND STATUS_COLETA = 'FINALIZADA';

-- Verificar criação
SELECT 'Tabela TABELA_SALA_INVENTARIO criada com sucesso!' AS status;
SELECT 'View vw_salas_inventario_status criada com sucesso!' AS status;
SELECT 'Funções auxiliares criadas com sucesso!' AS status;
