-- ============================================================
-- CRIAÇÃO DAS TABELAS DE ITENS COMPOSTOS
-- Sistema de Inventário - Coleta de Componentes
-- ============================================================

-- ============================================================
-- TABELA 1: tabela_item_composto
-- Armazena os componentes de um patrimônio composto
-- Ex: Conjunto Escolar tem MESA + CADEIRA
-- ============================================================
CREATE TABLE IF NOT EXISTS tabela_item_composto (
    id SERIAL PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL,
    tipo_componente VARCHAR(100) NOT NULL,
    descricao_componente VARCHAR(500) NOT NULL,
    quantidade_esperada INTEGER NOT NULL DEFAULT 1,
    obrigatorio BOOLEAN DEFAULT TRUE,
    ativo BOOLEAN DEFAULT TRUE,
    numero_serie VARCHAR(100),
    observacao TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key para patrimônio
    CONSTRAINT fk_item_composto_patrimonio 
        FOREIGN KEY (id_patrimonio_principal) 
        REFERENCES tabela_patrimonio(id) 
        ON DELETE CASCADE,
    
    -- Validação: quantidade deve ser maior que zero
    CONSTRAINT chk_quantidade_esperada 
        CHECK (quantidade_esperada > 0)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_item_composto_patrimonio 
    ON tabela_item_composto(id_patrimonio_principal);

CREATE INDEX IF NOT EXISTS idx_item_composto_tipo 
    ON tabela_item_composto(tipo_componente);

-- ============================================================
-- TABELA 2: tabela_coleta_componente
-- Registra a coleta de cada componente durante o inventário
-- ============================================================
CREATE TABLE IF NOT EXISTS tabela_coleta_componente (
    id SERIAL PRIMARY KEY,
    id_item_composto INTEGER NOT NULL,
    id_inventario INTEGER NOT NULL,
    id_coletor INTEGER NOT NULL,
    quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
    localizacao_encontrada VARCHAR(255),
    status_componente VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    observacao_coleta TEXT,
    data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_coleta_comp_item 
        FOREIGN KEY (id_item_composto) 
        REFERENCES tabela_item_composto(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_coleta_comp_inventario 
        FOREIGN KEY (id_inventario) 
        REFERENCES tabela_inventario(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_coleta_comp_coletor 
        FOREIGN KEY (id_coletor) 
        REFERENCES tabela_usuario(id),
    
    -- Cada componente só pode ser coletado uma vez por inventário
    CONSTRAINT uk_coleta_componente_inventario 
        UNIQUE (id_item_composto, id_inventario),
    
    -- Validação de status
    CONSTRAINT chk_status_componente 
        CHECK (status_componente IN ('PENDENTE', 'COMPLETO', 'PARCIAL', 'FALTANTE'))
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_coleta_comp_inventario 
    ON tabela_coleta_componente(id_inventario);

CREATE INDEX IF NOT EXISTS idx_coleta_comp_item 
    ON tabela_coleta_componente(id_item_composto);

CREATE INDEX IF NOT EXISTS idx_coleta_comp_status 
    ON tabela_coleta_componente(status_componente);

CREATE INDEX IF NOT EXISTS idx_coleta_comp_localizacao 
    ON tabela_coleta_componente(localizacao_encontrada);

-- ============================================================
-- TRIGGER: Atualizar data_coleta automaticamente
-- ============================================================
CREATE OR REPLACE FUNCTION atualizar_data_coleta_componente()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_coleta = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_atualizar_data_coleta ON tabela_coleta_componente;

CREATE TRIGGER trg_atualizar_data_coleta
    BEFORE UPDATE ON tabela_coleta_componente
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_coleta_componente();

-- ============================================================
-- FIM
-- ============================================================
