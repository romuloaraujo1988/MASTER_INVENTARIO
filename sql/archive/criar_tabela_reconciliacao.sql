-- ============================================================
-- Tabela de Reconciliação de Patrimônios
-- Relaciona itens NÃO ENCONTRADOS com itens SEM ETIQUETA
-- ============================================================

-- Criar tabela de reconciliação
CREATE TABLE IF NOT EXISTS tabela_reconciliacao (
    id SERIAL PRIMARY KEY,
    
    -- Patrimônio não encontrado (esperado mas não localizado)
    id_patrimonio INTEGER NOT NULL REFERENCES tabela_patrimonio(id),
    
    -- Coleta do item sem etiqueta (encontrado mas sem identificação)
    id_coleta_sem_etiqueta INTEGER NOT NULL REFERENCES tabela_coleta(id),
    
    -- Inventário onde ocorreu a reconciliação
    id_inventario INTEGER NOT NULL REFERENCES tabela_inventario(id),
    
    -- Usuário que fez a reconciliação
    id_usuario INTEGER NOT NULL REFERENCES tabela_usuario(id),
    
    -- Data/hora da reconciliação
    data_reconciliacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Percentual de similaridade (0-100)
    similaridade DECIMAL(5,2),
    
    -- Status: PENDENTE, CONFIRMADO, REJEITADO
    status VARCHAR(20) DEFAULT 'PENDENTE',
    
    -- Observações do operador
    observacoes TEXT,
    
    -- Ação tomada após confirmação: ATUALIZAR_LOCALIZACAO, GERAR_ETIQUETA, NENHUMA
    acao_tomada VARCHAR(50),
    
    -- Constraints
    CONSTRAINT uk_reconciliacao UNIQUE (id_patrimonio, id_coleta_sem_etiqueta, id_inventario)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_reconciliacao_patrimonio ON tabela_reconciliacao(id_patrimonio);
CREATE INDEX IF NOT EXISTS idx_reconciliacao_coleta ON tabela_reconciliacao(id_coleta_sem_etiqueta);
CREATE INDEX IF NOT EXISTS idx_reconciliacao_inventario ON tabela_reconciliacao(id_inventario);
CREATE INDEX IF NOT EXISTS idx_reconciliacao_status ON tabela_reconciliacao(status);

-- Comentários
COMMENT ON TABLE tabela_reconciliacao IS 'Relaciona patrimônios não encontrados com itens sem etiqueta';
COMMENT ON COLUMN tabela_reconciliacao.id_patrimonio IS 'Patrimônio que não foi encontrado no local esperado';
COMMENT ON COLUMN tabela_reconciliacao.id_coleta_sem_etiqueta IS 'Coleta de item sem etiqueta que pode ser o patrimônio';
COMMENT ON COLUMN tabela_reconciliacao.similaridade IS 'Percentual de similaridade entre descrições (0-100)';
COMMENT ON COLUMN tabela_reconciliacao.status IS 'PENDENTE=aguardando análise, CONFIRMADO=match correto, REJEITADO=não é o mesmo item';
COMMENT ON COLUMN tabela_reconciliacao.acao_tomada IS 'Ação após confirmação: ATUALIZAR_LOCALIZACAO, GERAR_ETIQUETA, NENHUMA';
