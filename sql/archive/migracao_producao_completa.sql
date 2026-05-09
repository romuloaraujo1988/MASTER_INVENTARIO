-- ============================================================
-- MIGRAÇÃO: Sincronizar banco de produção com os DAOs Java
-- Execute em: sispatrimonio
-- Idempotente: pode ser re-executado sem risco
-- ============================================================

DO $$
BEGIN

    -- =====================================================
    -- TABELA_RESPONSAVEL
    -- =====================================================
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='tabela_responsavel' AND column_name='cargo') THEN
        ALTER TABLE tabela_responsavel ADD COLUMN cargo VARCHAR(100);
        RAISE NOTICE '✓ CARGO adicionada em tabela_responsavel';
    ELSE
        RAISE NOTICE '- CARGO ja existe em tabela_responsavel';
    END IF;

    -- =====================================================
    -- TABELA_SALA
    -- =====================================================
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='tabela_sala' AND column_name='numero_sala') THEN
        ALTER TABLE tabela_sala ADD COLUMN numero_sala VARCHAR(20);
        RAISE NOTICE '✓ NUMERO_SALA adicionada em tabela_sala';
    ELSE
        RAISE NOTICE '- NUMERO_SALA ja existe em tabela_sala';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='tabela_sala' AND column_name='area_m2') THEN
        ALTER TABLE tabela_sala ADD COLUMN area_m2 DECIMAL(8,2);
        RAISE NOTICE '✓ AREA_M2 adicionada em tabela_sala';
    ELSE
        RAISE NOTICE '- AREA_M2 ja existe em tabela_sala';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='tabela_sala' AND column_name='tipo_sala') THEN
        ALTER TABLE tabela_sala ADD COLUMN tipo_sala VARCHAR(50);
        RAISE NOTICE '✓ TIPO_SALA adicionada em tabela_sala';
    ELSE
        RAISE NOTICE '- TIPO_SALA ja existe em tabela_sala';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='tabela_sala' AND column_name='observacoes') THEN
        ALTER TABLE tabela_sala ADD COLUMN observacoes TEXT;
        RAISE NOTICE '✓ OBSERVACOES adicionada em tabela_sala';
    ELSE
        RAISE NOTICE '- OBSERVACOES ja existe em tabela_sala';
    END IF;

    -- =====================================================
    -- TABELA_ITEM_COMPOSTO (pode não existir ainda)
    -- =====================================================
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name='tabela_item_composto') THEN
        CREATE TABLE tabela_item_composto (
            id SERIAL PRIMARY KEY,
            id_patrimonio_principal INTEGER NOT NULL,
            tipo_componente VARCHAR(100) NOT NULL,
            descricao_componente VARCHAR(500) NOT NULL,
            quantidade_esperada INTEGER NOT NULL DEFAULT 1,
            obrigatorio BOOLEAN DEFAULT TRUE,
            observacao TEXT,
            data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_item_composto_patrimonio FOREIGN KEY (id_patrimonio_principal) 
                REFERENCES tabela_patrimonio(id) ON DELETE CASCADE
        );
        CREATE INDEX idx_item_composto_patrimonio ON tabela_item_composto(id_patrimonio_principal);
        CREATE INDEX idx_item_composto_tipo ON tabela_item_composto(tipo_componente);
        RAISE NOTICE '✓ TABELA_ITEM_COMPOSTO criada';
    ELSE
        RAISE NOTICE '- TABELA_ITEM_COMPOSTO ja existe';
    END IF;

    -- =====================================================
    -- TABELA_COLETA_COMPONENTE (pode não existir ainda)
    -- =====================================================
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name='tabela_coleta_componente') THEN
        CREATE TABLE tabela_coleta_componente (
            id SERIAL PRIMARY KEY,
            id_item_composto INTEGER NOT NULL,
            id_inventario INTEGER NOT NULL,
            id_coletor INTEGER NOT NULL,
            quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
            status_componente VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
            observacao_coleta TEXT,
            localizacao_encontrada VARCHAR(255),
            data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_coleta_componente_item FOREIGN KEY (id_item_composto) 
                REFERENCES tabela_item_composto(id) ON DELETE CASCADE,
            CONSTRAINT fk_coleta_componente_inventario FOREIGN KEY (id_inventario) 
                REFERENCES tabela_inventario(id) ON DELETE CASCADE,
            CONSTRAINT uk_coleta_componente_item_inventario UNIQUE (id_item_composto, id_inventario)
        );
        CREATE INDEX idx_coleta_componente_item ON tabela_coleta_componente(id_item_composto);
        CREATE INDEX idx_coleta_componente_inventario ON tabela_coleta_componente(id_inventario);
        CREATE INDEX idx_coleta_componente_status ON tabela_coleta_componente(status_componente);
        RAISE NOTICE '✓ TABELA_COLETA_COMPONENTE criada';
    ELSE
        RAISE NOTICE '- TABELA_COLETA_COMPONENTE ja existe';
    END IF;

END $$;

-- Verificação final
SELECT 
    table_name,
    COUNT(*) as total_colunas
FROM information_schema.columns 
WHERE table_name IN (
    'tabela_setor','tabela_responsavel','tabela_sala',
    'tabela_patrimonio','tabela_usuario','tabela_coleta',
    'tabela_item_composto','tabela_coleta_componente'
)
GROUP BY table_name
ORDER BY table_name;
