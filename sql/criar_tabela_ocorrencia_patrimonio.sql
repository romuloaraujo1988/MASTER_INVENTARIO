-- ============================================================
-- Tabela de Ocorrências de Patrimônios Não Encontrados
-- Registra histórico de investigações e ações tomadas
-- ============================================================

CREATE TABLE IF NOT EXISTS tabela_ocorrencia_patrimonio (
    id SERIAL PRIMARY KEY,
    
    -- Patrimônio relacionado
    id_patrimonio INTEGER NOT NULL REFERENCES tabela_patrimonio(id),
    
    -- Inventário onde foi detectado
    id_inventario INTEGER NOT NULL REFERENCES tabela_inventario(id),
    
    -- Usuário que registrou a ocorrência
    id_usuario INTEGER NOT NULL REFERENCES tabela_usuario(id),
    
    -- Tipo de ocorrência
    tipo_ocorrencia VARCHAR(50) NOT NULL,
    -- Valores: NOTIFICACAO_RESPONSAVEL, INVESTIGACAO, TRANSFERENCIA, 
    --          LOCALIZACAO_ATUALIZADA, EXTRAVIO_CONFIRMADO, BAIXA_SOLICITADA
    
    -- Status da ocorrência
    status VARCHAR(30) DEFAULT 'ABERTA',
    -- Valores: ABERTA, EM_ANDAMENTO, AGUARDANDO_RESPOSTA, RESOLVIDA, CANCELADA
    
    -- Datas
    data_abertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_prazo TIMESTAMP,
    data_fechamento TIMESTAMP,
    
    -- Descrição e observações
    descricao TEXT NOT NULL,
    resposta_responsavel TEXT,
    
    -- Ação tomada para resolução
    acao_resolucao VARCHAR(100),
    
    -- Nova localização (se encontrado)
    nova_localizacao VARCHAR(200),
    
    -- Notificação
    responsavel_notificado BOOLEAN DEFAULT FALSE,
    data_notificacao TIMESTAMP,
    email_responsavel VARCHAR(200)
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_ocorrencia_patrimonio ON tabela_ocorrencia_patrimonio(id_patrimonio);
CREATE INDEX IF NOT EXISTS idx_ocorrencia_inventario ON tabela_ocorrencia_patrimonio(id_inventario);
CREATE INDEX IF NOT EXISTS idx_ocorrencia_status ON tabela_ocorrencia_patrimonio(status);
CREATE INDEX IF NOT EXISTS idx_ocorrencia_tipo ON tabela_ocorrencia_patrimonio(tipo_ocorrencia);

-- Comentários
COMMENT ON TABLE tabela_ocorrencia_patrimonio IS 'Histórico de ocorrências para patrimônios não encontrados';
COMMENT ON COLUMN tabela_ocorrencia_patrimonio.tipo_ocorrencia IS 'NOTIFICACAO_RESPONSAVEL, INVESTIGACAO, TRANSFERENCIA, LOCALIZACAO_ATUALIZADA, EXTRAVIO_CONFIRMADO, BAIXA_SOLICITADA';
COMMENT ON COLUMN tabela_ocorrencia_patrimonio.status IS 'ABERTA, EM_ANDAMENTO, AGUARDANDO_RESPOSTA, RESOLVIDA, CANCELADA';
