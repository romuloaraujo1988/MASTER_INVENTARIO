-- ============================================================
-- SCRIPT SEGURO - Atualização das Tabelas de Itens Compostos
-- NÃO QUEBRA O BANCO - Usa IF NOT EXISTS em tudo
-- Execute com: psql -h localhost -U postgres -d sispatrimonio -f sql/atualizar-tabelas-itens-compostos-seguro.sql
-- ============================================================

-- Iniciar transação para rollback em caso de erro
BEGIN;

-- ============================================================
-- 1. CRIAR TABELAS SE NÃO EXISTIREM
-- ============================================================

-- Tabela de itens compostos (componentes de um patrimônio)
CREATE TABLE IF NOT EXISTS tabela_item_composto (
    id SERIAL PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL,
    tipo_componente VARCHAR(100) NOT NULL,
    descricao_componente VARCHAR(500) NOT NULL,
    quantidade_esperada INTEGER NOT NULL DEFAULT 1,
    obrigatorio BOOLEAN DEFAULT TRUE,
    observacao TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_composto_patrimonio 
        FOREIGN KEY (id_patrimonio_principal) 
        REFERENCES tabela_patrimonio(id) 
        ON DELETE CASCADE
);

-- Tabela de coleta de componentes
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
);

-- ============================================================
-- 2. ADICIONAR COLUNAS NOVAS (SE NÃO EXISTIREM)
-- ============================================================

-- Adicionar coluna localizacao_encontrada na tabela de coleta
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_coleta_componente' 
        AND column_name = 'localizacao_encontrada'
    ) THEN
        ALTER TABLE tabela_coleta_componente 
        ADD COLUMN localizacao_encontrada VARCHAR(255);
        RAISE NOTICE 'Coluna localizacao_encontrada adicionada com sucesso';
    ELSE
        RAISE NOTICE 'Coluna localizacao_encontrada já existe';
    END IF;
END $$;

-- Adicionar coluna ativo na tabela de itens compostos
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_item_composto' 
        AND column_name = 'ativo'
    ) THEN
        ALTER TABLE tabela_item_composto 
        ADD COLUMN ativo BOOLEAN DEFAULT TRUE;
        RAISE NOTICE 'Coluna ativo adicionada com sucesso';
    ELSE
        RAISE NOTICE 'Coluna ativo já existe';
    END IF;
END $$;

-- Adicionar coluna numero_serie na tabela de itens compostos (opcional)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tabela_item_composto' 
        AND column_name = 'numero_serie'
    ) THEN
        ALTER TABLE tabela_item_composto 
        ADD COLUMN numero_serie VARCHAR(100);
        RAISE NOTICE 'Coluna numero_serie adicionada com sucesso';
    ELSE
        RAISE NOTICE 'Coluna numero_serie já existe';
    END IF;
END $$;

-- ============================================================
-- 3. CRIAR ÍNDICES (SE NÃO EXISTIREM)
-- ============================================================

-- Índice para busca por patrimônio
CREATE INDEX IF NOT EXISTS idx_item_composto_patrimonio 
ON tabela_item_composto(id_patrimonio_principal);

-- Índice para busca por tipo de componente
CREATE INDEX IF NOT EXISTS idx_item_composto_tipo 
ON tabela_item_composto(tipo_componente);

-- Índice para busca de coletas por inventário
CREATE INDEX IF NOT EXISTS idx_coleta_comp_inventario 
ON tabela_coleta_componente(id_inventario);

-- Índice para busca de coletas por item composto
CREATE INDEX IF NOT EXISTS idx_coleta_comp_item 
ON tabela_coleta_componente(id_item_composto);

-- Índice para busca de coletas por status
CREATE INDEX IF NOT EXISTS idx_coleta_comp_status 
ON tabela_coleta_componente(status_componente);

-- Índice para busca por localização encontrada
CREATE INDEX IF NOT EXISTS idx_coleta_comp_localizacao 
ON tabela_coleta_componente(localizacao_encontrada);

-- ============================================================
-- 4. CRIAR CONSTRAINT UNIQUE (SE NÃO EXISTIR)
-- ============================================================

-- Garantir que cada componente só pode ser coletado uma vez por inventário
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uk_coleta_componente_inventario'
        AND table_name = 'tabela_coleta_componente'
    ) THEN
        ALTER TABLE tabela_coleta_componente 
        ADD CONSTRAINT uk_coleta_componente_inventario 
        UNIQUE (id_item_composto, id_inventario);
        RAISE NOTICE 'Constraint uk_coleta_componente_inventario criada';
    ELSE
        RAISE NOTICE 'Constraint uk_coleta_componente_inventario já existe';
    END IF;
EXCEPTION
    WHEN duplicate_object THEN
        RAISE NOTICE 'Constraint uk_coleta_componente_inventario já existe (exception)';
END $$;

-- ============================================================
-- 5. CRIAR CHECK CONSTRAINTS (SE NÃO EXISTIREM)
-- ============================================================

-- Validar status do componente
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'chk_status_componente'
    ) THEN
        ALTER TABLE tabela_coleta_componente 
        ADD CONSTRAINT chk_status_componente 
        CHECK (status_componente IN ('PENDENTE', 'COMPLETO', 'PARCIAL', 'FALTANTE'));
        RAISE NOTICE 'Constraint chk_status_componente criada';
    ELSE
        RAISE NOTICE 'Constraint chk_status_componente já existe';
    END IF;
EXCEPTION
    WHEN duplicate_object THEN
        RAISE NOTICE 'Constraint chk_status_componente já existe (exception)';
END $$;

-- Validar quantidade esperada > 0
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'chk_quantidade_esperada'
    ) THEN
        ALTER TABLE tabela_item_composto 
        ADD CONSTRAINT chk_quantidade_esperada 
        CHECK (quantidade_esperada > 0);
        RAISE NOTICE 'Constraint chk_quantidade_esperada criada';
    ELSE
        RAISE NOTICE 'Constraint chk_quantidade_esperada já existe';
    END IF;
EXCEPTION
    WHEN duplicate_object THEN
        RAISE NOTICE 'Constraint chk_quantidade_esperada já existe (exception)';
END $$;

-- ============================================================
-- 6. CRIAR TRIGGER PARA ATUALIZAR DATA_COLETA
-- ============================================================

-- Função para atualizar data_coleta automaticamente
CREATE OR REPLACE FUNCTION atualizar_data_coleta_componente()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_coleta = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para UPDATE
DROP TRIGGER IF EXISTS trg_atualizar_data_coleta ON tabela_coleta_componente;
CREATE TRIGGER trg_atualizar_data_coleta
    BEFORE UPDATE ON tabela_coleta_componente
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_coleta_componente();

-- ============================================================
-- 7. VERIFICAÇÃO FINAL
-- ============================================================

-- Mostrar estrutura das tabelas
SELECT 'TABELA: tabela_item_composto' as info;
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'tabela_item_composto'
ORDER BY ordinal_position;

SELECT 'TABELA: tabela_coleta_componente' as info;
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'tabela_coleta_componente'
ORDER BY ordinal_position;

-- Contar registros existentes
SELECT 'ESTATÍSTICAS' as info;
SELECT 
    (SELECT COUNT(*) FROM tabela_item_composto) as total_componentes,
    (SELECT COUNT(*) FROM tabela_coleta_componente) as total_coletas,
    (SELECT COUNT(DISTINCT id_patrimonio_principal) FROM tabela_item_composto) as total_patrimonios;

-- Confirmar transação
COMMIT;

-- ============================================================
-- SCRIPT EXECUTADO COM SUCESSO!
-- ============================================================
SELECT '✅ Script executado com sucesso!' as resultado;
