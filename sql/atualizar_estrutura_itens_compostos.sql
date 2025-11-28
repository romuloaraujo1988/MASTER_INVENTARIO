-- ============================================================================
-- SCRIPT PARA ATUALIZAR ESTRUTURA DAS TABELAS DE ITENS COMPOSTOS
-- ============================================================================
-- Data: 28/11/2025
-- 
-- Este script atualiza as tabelas de itens compostos para a estrutura REAL
-- usada pelo sistema. Execute APÓS o script atualizar_banco_seguro.sql
-- ============================================================================

-- ============================================================================
-- VERIFICAR ESTRUTURA ATUAL
-- ============================================================================
DO $$
DECLARE
    v_estrutura_antiga BOOLEAN := FALSE;
BEGIN
    -- Verificar se a tabela tem a estrutura antiga (id_patrimonio_componente)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_item_composto' 
        AND column_name = 'id_patrimonio_componente'
    ) THEN
        v_estrutura_antiga := TRUE;
        RAISE NOTICE 'Estrutura antiga detectada - será necessário migrar';
    ELSE
        RAISE NOTICE 'Estrutura já está atualizada ou tabela não existe';
    END IF;
END $$;

-- ============================================================================
-- BACKUP DAS TABELAS ANTIGAS (se existirem dados)
-- ============================================================================
DO $$
BEGIN
    -- Backup da tabela_item_composto antiga
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_item_composto') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'tabela_item_composto' AND column_name = 'id_patrimonio_componente') THEN
            IF EXISTS (SELECT 1 FROM tabela_item_composto LIMIT 1) THEN
                CREATE TABLE IF NOT EXISTS tabela_item_composto_backup AS SELECT * FROM tabela_item_composto;
                RAISE NOTICE '✓ Backup criado: tabela_item_composto_backup';
            END IF;
        END IF;
    END IF;
    
    -- Backup da tabela_coleta_componente antiga
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_coleta_componente') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'tabela_coleta_componente' AND column_name = 'id_coleta') THEN
            IF EXISTS (SELECT 1 FROM tabela_coleta_componente LIMIT 1) THEN
                CREATE TABLE IF NOT EXISTS tabela_coleta_componente_backup AS SELECT * FROM tabela_coleta_componente;
                RAISE NOTICE '✓ Backup criado: tabela_coleta_componente_backup';
            END IF;
        END IF;
    END IF;
END $$;

-- ============================================================================
-- RECRIAR TABELA_ITEM_COMPOSTO COM ESTRUTURA CORRETA
-- ============================================================================
DO $$
BEGIN
    -- Verificar se precisa recriar
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_item_composto' 
        AND column_name = 'id_patrimonio_componente'
    ) THEN
        -- Dropar tabela antiga (já tem backup)
        DROP TABLE IF EXISTS tabela_coleta_componente CASCADE;
        DROP TABLE IF EXISTS tabela_item_composto CASCADE;
        RAISE NOTICE '✓ Tabelas antigas removidas';
    END IF;
    
    -- Criar tabela com estrutura correta
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_item_composto') THEN
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
        
        -- Índices
        CREATE INDEX idx_item_composto_patrimonio ON tabela_item_composto(id_patrimonio_principal);
        CREATE INDEX idx_item_composto_tipo ON tabela_item_composto(tipo_componente);
        
        RAISE NOTICE '✓ Tabela tabela_item_composto criada com estrutura correta';
    END IF;
END $$;

-- ============================================================================
-- RECRIAR TABELA_COLETA_COMPONENTE COM ESTRUTURA CORRETA
-- ============================================================================
DO $$
BEGIN
    -- Criar tabela com estrutura correta
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_coleta_componente') THEN
        CREATE TABLE tabela_coleta_componente (
            id SERIAL PRIMARY KEY,
            id_item_composto INTEGER NOT NULL,
            id_inventario INTEGER NOT NULL,
            id_coletor INTEGER NOT NULL,
            quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
            status_componente VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
            observacao_coleta TEXT,
            data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_coleta_componente_item FOREIGN KEY (id_item_composto) 
                REFERENCES tabela_item_composto(id) ON DELETE CASCADE,
            CONSTRAINT fk_coleta_componente_inventario FOREIGN KEY (id_inventario) 
                REFERENCES tabela_inventario(id) ON DELETE CASCADE,
            CONSTRAINT fk_coleta_componente_coletor FOREIGN KEY (id_coletor) 
                REFERENCES tabela_coletor(id) ON DELETE CASCADE
        );
        
        -- Índices
        CREATE INDEX idx_coleta_componente_item ON tabela_coleta_componente(id_item_composto);
        CREATE INDEX idx_coleta_componente_inventario ON tabela_coleta_componente(id_inventario);
        CREATE INDEX idx_coleta_componente_status ON tabela_coleta_componente(status_componente);
        
        RAISE NOTICE '✓ Tabela tabela_coleta_componente criada com estrutura correta';
    END IF;
END $$;

-- ============================================================================
-- VERIFICAÇÃO FINAL
-- ============================================================================
DO $$
DECLARE
    v_cols_item TEXT;
    v_cols_coleta TEXT;
BEGIN
    -- Verificar colunas da tabela_item_composto
    SELECT string_agg(column_name, ', ' ORDER BY ordinal_position)
    INTO v_cols_item
    FROM information_schema.columns
    WHERE table_name = 'tabela_item_composto';
    
    RAISE NOTICE 'Colunas tabela_item_composto: %', v_cols_item;
    
    -- Verificar colunas da tabela_coleta_componente
    SELECT string_agg(column_name, ', ' ORDER BY ordinal_position)
    INTO v_cols_coleta
    FROM information_schema.columns
    WHERE table_name = 'tabela_coleta_componente';
    
    RAISE NOTICE 'Colunas tabela_coleta_componente: %', v_cols_coleta;
    
    RAISE NOTICE '';
    RAISE NOTICE '============================================================';
    RAISE NOTICE '✅ ESTRUTURA DE ITENS COMPOSTOS ATUALIZADA';
    RAISE NOTICE '============================================================';
END $$;

-- ============================================================================
-- ESTRUTURA ESPERADA (REFERÊNCIA)
-- ============================================================================
/*
tabela_item_composto:
- id (SERIAL PRIMARY KEY)
- id_patrimonio_principal (INTEGER NOT NULL) - FK para tabela_patrimonio
- tipo_componente (VARCHAR(100) NOT NULL) - Ex: CADEIRA, MESA, MONITOR, CPU
- descricao_componente (VARCHAR(500) NOT NULL) - Descrição do componente
- quantidade_esperada (INTEGER DEFAULT 1) - Quantidade esperada
- obrigatorio (BOOLEAN DEFAULT TRUE) - Se é obrigatório na coleta
- observacao (TEXT) - Observações
- data_cadastro (TIMESTAMP) - Data de cadastro

tabela_coleta_componente:
- id (SERIAL PRIMARY KEY)
- id_item_composto (INTEGER NOT NULL) - FK para tabela_item_composto
- id_inventario (INTEGER NOT NULL) - FK para tabela_inventario
- id_coletor (INTEGER NOT NULL) - FK para tabela_coletor
- quantidade_encontrada (INTEGER DEFAULT 0) - Quantidade encontrada na coleta
- status_componente (VARCHAR(50) DEFAULT 'PENDENTE') - PENDENTE, COMPLETO, INCOMPLETO
- observacao_coleta (TEXT) - Observações da coleta
- data_coleta (TIMESTAMP) - Data/hora da coleta

ESTATÍSTICAS DO BANCO DE ORIGEM (28/11/2025):
- tabela_item_composto: 1.588 registros
  - CADEIRA: 707 registros
  - MESA: 707 registros
  - CPU: 87 registros
  - MONITOR: 87 registros
- tabela_coleta_componente: 12 registros (todos COMPLETO)
*/
