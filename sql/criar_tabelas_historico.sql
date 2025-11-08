-- =====================================================
-- SCRIPT PARA CRIAÇÃO DE TABELAS DE HISTÓRICO
-- Sistema de Inventário Patrimonial
-- =====================================================

-- Tabela para histórico de responsáveis
DROP TABLE IF EXISTS TABELA_HISTORICO_RESPONSAVEL CASCADE;
CREATE TABLE TABELA_HISTORICO_RESPONSAVEL (
    id SERIAL PRIMARY KEY,
    patrimonio_id INTEGER NOT NULL,
    responsavel_anterior_id INTEGER,
    responsavel_novo_id INTEGER,
    data_mudanca TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo_mudanca VARCHAR(500),
    usuario_alteracao_id INTEGER,
    observacoes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Chaves estrangeiras
    CONSTRAINT fk_historico_responsavel_patrimonio 
        FOREIGN KEY (patrimonio_id) REFERENCES TABELA_PATRIMONIO(id),
    CONSTRAINT fk_historico_responsavel_responsavel_anterior 
        FOREIGN KEY (responsavel_anterior_id) REFERENCES TABELA_RESPONSAVEL(id),
    CONSTRAINT fk_historico_responsavel_responsavel_novo 
        FOREIGN KEY (responsavel_novo_id) REFERENCES TABELA_RESPONSAVEL(id),
    CONSTRAINT fk_historico_responsavel_usuario 
        FOREIGN KEY (usuario_alteracao_id) REFERENCES TABELA_USUARIO(id)
);

-- Tabela para histórico de localizações
DROP TABLE IF EXISTS TABELA_HISTORICO_LOCALIZACAO CASCADE;
CREATE TABLE TABELA_HISTORICO_LOCALIZACAO (
    id SERIAL PRIMARY KEY,
    patrimonio_id INTEGER NOT NULL,
    sala_anterior_id INTEGER,
    sala_nova_id INTEGER,
    setor_anterior_id INTEGER,
    setor_novo_id INTEGER,
    data_mudanca TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo_mudanca VARCHAR(500),
    usuario_alteracao_id INTEGER,
    observacoes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Chaves estrangeiras
    CONSTRAINT fk_historico_localizacao_patrimonio 
        FOREIGN KEY (patrimonio_id) REFERENCES TABELA_PATRIMONIO(id),
    CONSTRAINT fk_historico_localizacao_sala_anterior 
        FOREIGN KEY (sala_anterior_id) REFERENCES TABELA_SALA(id_sala),
    CONSTRAINT fk_historico_localizacao_sala_nova 
        FOREIGN KEY (sala_nova_id) REFERENCES TABELA_SALA(id_sala),
    CONSTRAINT fk_historico_localizacao_setor_anterior 
        FOREIGN KEY (setor_anterior_id) REFERENCES TABELA_SETOR(id),
    CONSTRAINT fk_historico_localizacao_setor_novo 
        FOREIGN KEY (setor_novo_id) REFERENCES TABELA_SETOR(id),
    CONSTRAINT fk_historico_localizacao_usuario 
        FOREIGN KEY (usuario_alteracao_id) REFERENCES TABELA_USUARIO(id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_hist_resp_patrimonio ON TABELA_HISTORICO_RESPONSAVEL(patrimonio_id);
CREATE INDEX IF NOT EXISTS idx_hist_resp_data ON TABELA_HISTORICO_RESPONSAVEL(data_mudanca);
CREATE INDEX IF NOT EXISTS idx_hist_resp_responsavel_anterior ON TABELA_HISTORICO_RESPONSAVEL(responsavel_anterior_id);
CREATE INDEX IF NOT EXISTS idx_hist_resp_responsavel_novo ON TABELA_HISTORICO_RESPONSAVEL(responsavel_novo_id);

CREATE INDEX IF NOT EXISTS idx_hist_loc_patrimonio ON TABELA_HISTORICO_LOCALIZACAO(patrimonio_id);
CREATE INDEX IF NOT EXISTS idx_hist_loc_data ON TABELA_HISTORICO_LOCALIZACAO(data_mudanca);
CREATE INDEX IF NOT EXISTS idx_hist_loc_sala_anterior ON TABELA_HISTORICO_LOCALIZACAO(sala_anterior_id);
CREATE INDEX IF NOT EXISTS idx_hist_loc_sala_nova ON TABELA_HISTORICO_LOCALIZACAO(sala_nova_id);
CREATE INDEX IF NOT EXISTS idx_hist_loc_setor_anterior ON TABELA_HISTORICO_LOCALIZACAO(setor_anterior_id);
CREATE INDEX IF NOT EXISTS idx_hist_loc_setor_novo ON TABELA_HISTORICO_LOCALIZACAO(setor_novo_id);

-- Triggers para auditoria automática das tabelas de histórico
CREATE OR REPLACE FUNCTION update_historico_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_historico_responsavel_updated_at ON TABELA_HISTORICO_RESPONSAVEL;
CREATE TRIGGER update_historico_responsavel_updated_at 
    BEFORE UPDATE ON TABELA_HISTORICO_RESPONSAVEL 
    FOR EACH ROW EXECUTE FUNCTION update_historico_updated_at_column();

DROP TRIGGER IF EXISTS update_historico_localizacao_updated_at ON TABELA_HISTORICO_LOCALIZACAO;
CREATE TRIGGER update_historico_localizacao_updated_at 
    BEFORE UPDATE ON TABELA_HISTORICO_LOCALIZACAO 
    FOR EACH ROW EXECUTE FUNCTION update_historico_updated_at_column();

-- Comentários nas tabelas
COMMENT ON TABLE TABELA_HISTORICO_RESPONSAVEL IS 'Histórico de mudanças de responsáveis por patrimônios';
COMMENT ON TABLE TABELA_HISTORICO_LOCALIZACAO IS 'Histórico de mudanças de localização de patrimônios';

COMMENT ON COLUMN TABELA_HISTORICO_RESPONSAVEL.patrimonio_id IS 'ID do patrimônio que teve responsável alterado';
COMMENT ON COLUMN TABELA_HISTORICO_RESPONSAVEL.responsavel_anterior_id IS 'ID do responsável anterior';
COMMENT ON COLUMN TABELA_HISTORICO_RESPONSAVEL.responsavel_novo_id IS 'ID do novo responsável';
COMMENT ON COLUMN TABELA_HISTORICO_RESPONSAVEL.data_mudanca IS 'Data e hora da mudança';
COMMENT ON COLUMN TABELA_HISTORICO_RESPONSAVEL.motivo_mudanca IS 'Motivo da mudança de responsável';
COMMENT ON COLUMN TABELA_HISTORICO_RESPONSAVEL.usuario_alteracao_id IS 'Usuário que fez a alteração';

COMMENT ON COLUMN TABELA_HISTORICO_LOCALIZACAO.patrimonio_id IS 'ID do patrimônio que teve localização alterada';
COMMENT ON COLUMN TABELA_HISTORICO_LOCALIZACAO.sala_anterior_id IS 'ID da sala anterior';
COMMENT ON COLUMN TABELA_HISTORICO_LOCALIZACAO.sala_nova_id IS 'ID da nova sala';
COMMENT ON COLUMN TABELA_HISTORICO_LOCALIZACAO.setor_anterior_id IS 'ID do setor anterior';
COMMENT ON COLUMN TABELA_HISTORICO_LOCALIZACAO.setor_novo_id IS 'ID do novo setor';
COMMENT ON COLUMN TABELA_HISTORICO_LOCALIZACAO.data_mudanca IS 'Data e hora da mudança';
COMMENT ON COLUMN TABELA_HISTORICO_LOCALIZACAO.motivo_mudanca IS 'Motivo da mudança de localização';
COMMENT ON COLUMN TABELA_HISTORICO_LOCALIZACAO.usuario_alteracao_id IS 'Usuário que fez a alteração';

PRINT 'Tabelas de histórico criadas com sucesso!';