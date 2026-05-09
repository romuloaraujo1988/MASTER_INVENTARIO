-- ============================================================================
-- SCRIPT PARA POPULAR ITENS COMPOSTOS - VERSÃO 2
-- Sistema de Inventário Patrimonial - IFMT
-- Data: 28/11/2025
-- ============================================================================
-- 
-- INSTRUÇÕES:
-- 1. Execute este script no PostgreSQL (psql ou pgAdmin)
-- 2. O script irá:
--    a) Fazer backup da tabela antiga (se houver dados)
--    b) Recriar as tabelas com a estrutura correta
--    c) Popular com dados baseados nos patrimônios existentes
--
-- IMPORTANTE: Execute com um usuário que tenha permissão de DDL
-- ============================================================================

-- Desabilitar verificação de foreign keys durante a importação
SET session_replication_role = 'replica';

-- ============================================================================
-- PASSO 1: BACKUP E RECRIAÇÃO DAS TABELAS
-- ============================================================================

-- Backup da tabela antiga (se existir e tiver dados)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_item_composto') THEN
        IF EXISTS (SELECT 1 FROM tabela_item_composto LIMIT 1) THEN
            CREATE TABLE IF NOT EXISTS tabela_item_composto_backup_v2 AS SELECT * FROM tabela_item_composto;
            RAISE NOTICE '✓ Backup criado: tabela_item_composto_backup_v2';
        END IF;
    END IF;
END $$;

-- Dropar tabelas antigas
DROP TABLE IF EXISTS tabela_coleta_componente CASCADE;
DROP TABLE IF EXISTS tabela_item_composto CASCADE;

-- Criar tabela_item_composto com estrutura correta
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

-- Índices para performance
CREATE INDEX idx_item_composto_patrimonio ON tabela_item_composto(id_patrimonio_principal);
CREATE INDEX idx_item_composto_tipo ON tabela_item_composto(tipo_componente);

-- Criar tabela_coleta_componente
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
    -- Constraint de unicidade para ON CONFLICT funcionar
    CONSTRAINT uk_coleta_componente_item_inventario UNIQUE (id_item_composto, id_inventario)
);

-- Índices
CREATE INDEX idx_coleta_componente_item ON tabela_coleta_componente(id_item_composto);
CREATE INDEX idx_coleta_componente_inventario ON tabela_coleta_componente(id_inventario);
CREATE INDEX idx_coleta_componente_status ON tabela_coleta_componente(status_componente);

RAISE NOTICE '✓ Tabelas criadas com estrutura correta';


-- ============================================================================
-- PASSO 2: POPULAR ITENS COMPOSTOS BASEADO NOS PATRIMÔNIOS EXISTENTES
-- ============================================================================

-- ============================================================================
-- 2.1 CONJUNTOS ESCOLARES (Mesa + Cadeira)
-- Patrimônios que contêm "CONJUNTO ESCOLAR", "CONJUNTO ALUNO" ou "CJA-06"
-- ============================================================================
INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao)
SELECT 
    p.id,
    'MESA',
    'Mesa com tampo MDP 18mm, 450x600mm, estrutura em tubo aço carbono',
    1,
    true,
    'Componente do conjunto escolar'
FROM tabela_patrimonio p
WHERE UPPER(p.descricao) LIKE '%CONJUNTO ESCOLAR%' 
   OR UPPER(p.descricao) LIKE '%CONJUNTO ALUNO%'
   OR UPPER(p.descricao) LIKE '%CJA-06%';

INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao)
SELECT 
    p.id,
    'CADEIRA',
    'Cadeira empilhável, assento e encosto em polipropileno, estrutura em tubo aço carbono',
    1,
    true,
    'Componente do conjunto escolar'
FROM tabela_patrimonio p
WHERE UPPER(p.descricao) LIKE '%CONJUNTO ESCOLAR%' 
   OR UPPER(p.descricao) LIKE '%CONJUNTO ALUNO%'
   OR UPPER(p.descricao) LIKE '%CJA-06%';

-- ============================================================================
-- 2.2 COMPUTADORES DESKTOP (CPU + Monitor + Teclado + Mouse)
-- Patrimônios que contêm "COMPUTADOR" e "DESKTOP" ou "COMPLETO"
-- ============================================================================
INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao)
SELECT 
    p.id,
    'CPU',
    'Gabinete/Unidade Central de Processamento',
    1,
    true,
    'Componente principal do computador'
FROM tabela_patrimonio p
WHERE (UPPER(p.descricao) LIKE '%COMPUTADOR%DESKTOP%' 
   OR UPPER(p.descricao) LIKE '%DESKTOP%MODELO%'
   OR UPPER(p.descricao) LIKE '%COMPUTADOR TIPO DESKTOP%');

INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao)
SELECT 
    p.id,
    'MONITOR',
    'Monitor de vídeo LCD/LED',
    1,
    true,
    'Monitor do computador'
FROM tabela_patrimonio p
WHERE (UPPER(p.descricao) LIKE '%COMPUTADOR%DESKTOP%' 
   OR UPPER(p.descricao) LIKE '%DESKTOP%MODELO%'
   OR UPPER(p.descricao) LIKE '%COMPUTADOR TIPO DESKTOP%');

INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao)
SELECT 
    p.id,
    'TECLADO',
    'Teclado padrão ABNT2',
    1,
    true,
    'Teclado do computador'
FROM tabela_patrimonio p
WHERE (UPPER(p.descricao) LIKE '%COMPUTADOR%DESKTOP%' 
   OR UPPER(p.descricao) LIKE '%DESKTOP%MODELO%'
   OR UPPER(p.descricao) LIKE '%COMPUTADOR TIPO DESKTOP%');

INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao)
SELECT 
    p.id,
    'MOUSE',
    'Mouse óptico USB',
    1,
    true,
    'Mouse do computador'
FROM tabela_patrimonio p
WHERE (UPPER(p.descricao) LIKE '%COMPUTADOR%DESKTOP%' 
   OR UPPER(p.descricao) LIKE '%DESKTOP%MODELO%'
   OR UPPER(p.descricao) LIKE '%COMPUTADOR TIPO DESKTOP%');

-- ============================================================================
-- 2.3 CARTEIRAS UNIVERSITÁRIAS (Assento + Prancheta)
-- Patrimônios que contêm "CARTEIRA UNIVERSITARIA"
-- ============================================================================
INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao)
SELECT 
    p.id,
    'ASSENTO',
    'Assento e encosto em polipropileno ou madeira',
    1,
    true,
    'Componente da carteira universitária'
FROM tabela_patrimonio p
WHERE UPPER(p.descricao) LIKE '%CARTEIRA UNIVERSITARIA%';

INSERT INTO tabela_item_composto (id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao)
SELECT 
    p.id,
    'PRANCHETA',
    'Prancheta/apoio para escrita',
    1,
    true,
    'Componente da carteira universitária'
FROM tabela_patrimonio p
WHERE UPPER(p.descricao) LIKE '%CARTEIRA UNIVERSITARIA%';

-- ============================================================================
-- PASSO 3: ATUALIZAR SEQUENCE
-- ============================================================================
SELECT setval('tabela_item_composto_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM tabela_item_composto), false);

-- ============================================================================
-- PASSO 4: REABILITAR VERIFICAÇÃO DE FOREIGN KEYS
-- ============================================================================
SET session_replication_role = 'origin';

-- ============================================================================
-- PASSO 5: VERIFICAÇÃO FINAL
-- ============================================================================
DO $$
DECLARE
    v_total INTEGER;
    v_conjuntos INTEGER;
    v_computadores INTEGER;
    v_carteiras INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_total FROM tabela_item_composto;
    
    SELECT COUNT(*) INTO v_conjuntos 
    FROM tabela_item_composto 
    WHERE tipo_componente IN ('MESA', 'CADEIRA') 
    AND observacao LIKE '%conjunto escolar%';
    
    SELECT COUNT(*) INTO v_computadores 
    FROM tabela_item_composto 
    WHERE tipo_componente IN ('CPU', 'MONITOR', 'TECLADO', 'MOUSE');
    
    SELECT COUNT(*) INTO v_carteiras 
    FROM tabela_item_composto 
    WHERE tipo_componente IN ('ASSENTO', 'PRANCHETA');
    
    RAISE NOTICE '';
    RAISE NOTICE '============================================================';
    RAISE NOTICE '✅ ITENS COMPOSTOS POPULADOS COM SUCESSO';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Total de registros: %', v_total;
    RAISE NOTICE '  - Conjuntos escolares (mesa+cadeira): %', v_conjuntos;
    RAISE NOTICE '  - Computadores (cpu+monitor+teclado+mouse): %', v_computadores;
    RAISE NOTICE '  - Carteiras universitárias (assento+prancheta): %', v_carteiras;
    RAISE NOTICE '============================================================';
END $$;

-- Mostrar resumo por tipo de componente
SELECT tipo_componente, COUNT(*) as quantidade
FROM tabela_item_composto
GROUP BY tipo_componente
ORDER BY quantidade DESC;
