-- Script para criação da TABELA_PARTICIPANTE_INVENTARIO
-- Sistema de Inventário IFMT
-- Gerencia os participantes de cada inventário

CREATE TABLE IF NOT EXISTS TABELA_PARTICIPANTE_INVENTARIO (
    ID SERIAL PRIMARY KEY,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_USUARIO INTEGER NOT NULL,
    PAPEL VARCHAR(50) NOT NULL CHECK (PAPEL IN ('COORDENADOR', 'COLETOR', 'OBSERVADOR')),
    DATA_INCLUSAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DATA_REMOCAO TIMESTAMP NULL,
    ATIVO BOOLEAN DEFAULT TRUE,
    OBSERVACOES TEXT,
    CRIADO_POR INTEGER,
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DATA_ULTIMA_ATUALIZACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Chaves estrangeiras
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID) ON DELETE CASCADE,
    FOREIGN KEY (CRIADO_POR) REFERENCES TABELA_USUARIO(ID),
    
    -- Constraint única para evitar duplicação
    UNIQUE(ID_INVENTARIO, ID_USUARIO)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_participante_inventario ON TABELA_PARTICIPANTE_INVENTARIO(ID_INVENTARIO);
CREATE INDEX IF NOT EXISTS idx_participante_usuario ON TABELA_PARTICIPANTE_INVENTARIO(ID_USUARIO);
CREATE INDEX IF NOT EXISTS idx_participante_papel ON TABELA_PARTICIPANTE_INVENTARIO(PAPEL);
CREATE INDEX IF NOT EXISTS idx_participante_ativo ON TABELA_PARTICIPANTE_INVENTARIO(ATIVO);

-- Comentários
COMMENT ON TABLE TABELA_PARTICIPANTE_INVENTARIO IS 'Tabela de participantes dos inventários';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.ID IS 'Identificador único do participante';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.ID_INVENTARIO IS 'Referência ao inventário';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.ID_USUARIO IS 'Referência ao usuário participante';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.PAPEL IS 'Papel do participante no inventário (COORDENADOR, COLETOR, OBSERVADOR)';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.DATA_INCLUSAO IS 'Data de inclusão do participante';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.DATA_REMOCAO IS 'Data de remoção do participante (se aplicável)';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.ATIVO IS 'Indica se o participante está ativo no inventário';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.OBSERVACOES IS 'Observações sobre a participação';
COMMENT ON COLUMN TABELA_PARTICIPANTE_INVENTARIO.CRIADO_POR IS 'Usuário que incluiu o participante';

-- Trigger para atualizar data de última atualização
CREATE OR REPLACE FUNCTION update_participante_inventario_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP;
    
    -- Se está sendo desativado, registrar data de remoção
    IF OLD.ATIVO = TRUE AND NEW.ATIVO = FALSE THEN
        NEW.DATA_REMOCAO = CURRENT_TIMESTAMP;
    END IF;
    
    -- Se está sendo reativado, limpar data de remoção
    IF OLD.ATIVO = FALSE AND NEW.ATIVO = TRUE THEN
        NEW.DATA_REMOCAO = NULL;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_participante_inventario_timestamp
    BEFORE UPDATE ON TABELA_PARTICIPANTE_INVENTARIO
    FOR EACH ROW
    EXECUTE FUNCTION update_participante_inventario_timestamp();

-- Função para validar regras de negócio
CREATE OR REPLACE FUNCTION validar_participante_inventario()
RETURNS TRIGGER AS $$
DECLARE
    status_inventario VARCHAR(50);
    count_coordenadores INTEGER;
BEGIN
    -- Verificar se o inventário permite alteração de participantes
    SELECT STATUS_INVENTARIO INTO status_inventario 
    FROM TABELA_INVENTARIO 
    WHERE ID = NEW.ID_INVENTARIO;
    
    IF status_inventario NOT IN ('PLANEJADO', 'ABERTO', 'REABERTO') THEN
        RAISE EXCEPTION 'Não é possível alterar participantes de inventário com status: %', status_inventario;
    END IF;
    
    -- Verificar se não há mais de um coordenador ativo
    IF NEW.PAPEL = 'COORDENADOR' AND NEW.ATIVO = TRUE THEN
        SELECT COUNT(*) INTO count_coordenadores
        FROM TABELA_PARTICIPANTE_INVENTARIO
        WHERE ID_INVENTARIO = NEW.ID_INVENTARIO 
        AND PAPEL = 'COORDENADOR' 
        AND ATIVO = TRUE
        AND (TG_OP = 'INSERT' OR ID != NEW.ID);
        
        IF count_coordenadores > 0 THEN
            RAISE EXCEPTION 'Já existe um coordenador ativo para este inventário';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_validar_participante_inventario
    BEFORE INSERT OR UPDATE ON TABELA_PARTICIPANTE_INVENTARIO
    FOR EACH ROW
    EXECUTE FUNCTION validar_participante_inventario();

-- View para facilitar consultas
CREATE OR REPLACE VIEW VW_PARTICIPANTES_INVENTARIO AS
SELECT 
    pi.ID,
    pi.ID_INVENTARIO,
    i.NOME AS NOME_INVENTARIO,
    i.STATUS_INVENTARIO,
    pi.ID_USUARIO,
    u.NOME AS NOME_USUARIO,
    u.EMAIL AS EMAIL_USUARIO,
    pi.PAPEL,
    pi.DATA_INCLUSAO,
    pi.DATA_REMOCAO,
    pi.ATIVO,
    pi.OBSERVACOES
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID
ORDER BY i.NOME, pi.PAPEL, u.NOME;

COMMENT ON VIEW VW_PARTICIPANTES_INVENTARIO IS 'View com informações completas dos participantes dos inventários';

PRINT 'Tabela PARTICIPANTE_INVENTARIO criada com sucesso!';
PRINT 'View VW_PARTICIPANTES_INVENTARIO criada com sucesso!';
PRINT 'Triggers e validações configurados!';