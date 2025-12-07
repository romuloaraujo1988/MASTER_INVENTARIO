-- ============================================================
-- SCRIPT COMPLETO: Tabelas de Reconciliação e Ocorrências
-- Execute este script no pgAdmin ou DBeaver
-- ============================================================

-- 1. TABELA DE RECONCILIAÇÃO
-- Relaciona itens NÃO ENCONTRADOS com itens SEM ETIQUETA
-- ============================================================

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
    
    -- Ação tomada após confirmação
    acao_tomada VARCHAR(50),
    
    -- Constraint de unicidade
    CONSTRAINT uk_reconciliacao UNIQUE (id_patrimonio, id_coleta_sem_etiqueta, id_inventario)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_reconciliacao_patrimonio ON tabela_reconciliacao(id_patrimonio);
CREATE INDEX IF NOT EXISTS idx_reconciliacao_coleta ON tabela_reconciliacao(id_coleta_sem_etiqueta);
CREATE INDEX IF NOT EXISTS idx_reconciliacao_inventario ON tabela_reconciliacao(id_inventario);
CREATE INDEX IF NOT EXISTS idx_reconciliacao_status ON tabela_reconciliacao(status);

-- ============================================================
-- 2. TABELA DE OCORRÊNCIAS
-- Histórico de investigações para patrimônios não encontrados
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

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_ocorrencia_patrimonio ON tabela_ocorrencia_patrimonio(id_patrimonio);
CREATE INDEX IF NOT EXISTS idx_ocorrencia_inventario ON tabela_ocorrencia_patrimonio(id_inventario);
CREATE INDEX IF NOT EXISTS idx_ocorrencia_status ON tabela_ocorrencia_patrimonio(status);
CREATE INDEX IF NOT EXISTS idx_ocorrencia_tipo ON tabela_ocorrencia_patrimonio(tipo_ocorrencia);

-- ============================================================
-- COMENTÁRIOS
-- ============================================================

COMMENT ON TABLE tabela_reconciliacao IS 'Relaciona patrimônios não encontrados com itens sem etiqueta';
COMMENT ON TABLE tabela_ocorrencia_patrimonio IS 'Histórico de ocorrências para patrimônios não encontrados';

-- ============================================================
-- VERIFICAÇÃO
-- ============================================================

SELECT 'Tabelas criadas com sucesso!' as resultado;

SELECT table_name 
FROM information_schema.tables 
WHERE table_name IN ('tabela_reconciliacao', 'tabela_ocorrencia_patrimonio')
ORDER BY table_name;
