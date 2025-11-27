-- ========================================
-- Script para criar tabelas de Itens Compostos
-- Sistema de Inventário IFMT
-- ========================================

-- 1. Criar tabela de itens compostos
CREATE TABLE IF NOT EXISTS tabela_item_composto (
    id SERIAL PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL,
    tipo_componente VARCHAR(100) NOT NULL,
    descricao_componente VARCHAR(255) NOT NULL,
    quantidade_esperada INTEGER NOT NULL DEFAULT 1,
    obrigatorio BOOLEAN DEFAULT TRUE,
    observacao TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_composto_patrimonio 
        FOREIGN KEY (id_patrimonio_principal) 
        REFERENCES tabela_patrimonio(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_quantidade_positiva 
        CHECK (quantidade_esperada > 0)
);

-- 2. Criar tabela de coleta de componentes
CREATE TABLE IF NOT EXISTS tabela_coleta_componente (
    id SERIAL PRIMARY KEY,
    id_item_composto INTEGER NOT NULL,
    id_inventario INTEGER NOT NULL,
    id_coletor INTEGER NOT NULL,
    quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
    status_componente VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    observacao_coleta TEXT,
    data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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
        REFERENCES tabela_usuario(id) 
        ON DELETE RESTRICT,
    CONSTRAINT chk_status_componente 
        CHECK (status_componente IN ('PENDENTE', 'COMPLETO', 'PARCIAL', 'FALTANTE')),
    CONSTRAINT chk_quantidade_nao_negativa 
        CHECK (quantidade_encontrada >= 0),
    CONSTRAINT uk_coleta_componente_inventario 
        UNIQUE (id_item_composto, id_inventario)
);

-- 3. Criar índices para performance
CREATE INDEX IF NOT EXISTS idx_item_composto_patrimonio ON tabela_item_composto(id_patrimonio_principal);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_item ON tabela_coleta_componente(id_item_composto);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_inventario ON tabela_coleta_componente(id_inventario);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_status ON tabela_coleta_componente(status_componente);

-- 4. Comentários nas tabelas
COMMENT ON TABLE tabela_item_composto IS 'Armazena os componentes de itens compostos (ex: computador = CPU + monitor + teclado + mouse)';
COMMENT ON TABLE tabela_coleta_componente IS 'Registra a coleta individual de cada componente durante o inventário';

-- Verificar criação
SELECT 'Tabelas criadas com sucesso!' as status;
