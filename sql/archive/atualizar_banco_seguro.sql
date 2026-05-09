-- ============================================================================
-- SCRIPT DE ATUALIZAÇÃO SEGURA DO BANCO DE DADOS
-- Sistema de Inventário Patrimonial - SIHCP
-- 
-- Este script APENAS ADICIONA tabelas e colunas que não existem.
-- NÃO APAGA nem MODIFICA dados existentes.
-- 
-- Pode ser executado múltiplas vezes com segurança (idempotente).
-- 
-- Data: 28/11/2025
-- ============================================================================

-- Configurações iniciais
SET client_encoding = 'UTF8';

-- ============================================================================
-- FUNÇÃO AUXILIAR: Verifica se coluna existe
-- ============================================================================
CREATE OR REPLACE FUNCTION coluna_existe(p_tabela TEXT, p_coluna TEXT) 
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = p_tabela 
        AND column_name = p_coluna
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- FUNÇÃO AUXILIAR: Verifica se tabela existe
-- ============================================================================
CREATE OR REPLACE FUNCTION tabela_existe(p_tabela TEXT) 
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = p_tabela
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- FUNÇÃO AUXILIAR: Verifica se índice existe
-- ============================================================================
CREATE OR REPLACE FUNCTION indice_existe(p_indice TEXT) 
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 
        FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND indexname = p_indice
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- TIPOS ENUMERADOS (se não existirem)
-- ============================================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'status_dispositivo') THEN
        CREATE TYPE status_dispositivo AS ENUM ('PENDENTE', 'APROVADO', 'BLOQUEADO', 'REVOGADO');
        RAISE NOTICE '✓ Tipo status_dispositivo criado';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_setor (se não existir)
-- ============================================================================
DO $$
BEGIN
    IF NOT tabela_existe('tabela_setor') THEN
        CREATE TABLE tabela_setor (
            id SERIAL PRIMARY KEY,
            nome VARCHAR(200) NOT NULL,
            sigla VARCHAR(20),
            descricao TEXT,
            id_campus INTEGER,
            ativo BOOLEAN DEFAULT TRUE,
            data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        RAISE NOTICE '✓ Tabela tabela_setor criada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_campus (se não existir)
-- ============================================================================
DO $$
BEGIN
    IF NOT tabela_existe('tabela_campus') THEN
        CREATE TABLE tabela_campus (
            id SERIAL PRIMARY KEY,
            nome VARCHAR(200) NOT NULL,
            sigla VARCHAR(20),
            endereco TEXT,
            cidade VARCHAR(100),
            uf VARCHAR(2),
            ativo BOOLEAN DEFAULT TRUE
        );
        RAISE NOTICE '✓ Tabela tabela_campus criada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_sala - Colunas adicionais
-- ============================================================================
DO $$
BEGIN
    -- Coluna id (se não existir como coluna separada)
    IF NOT coluna_existe('tabela_sala', 'id') THEN
        ALTER TABLE tabela_sala ADD COLUMN id SERIAL;
        RAISE NOTICE '✓ Coluna tabela_sala.id adicionada';
    END IF;
    
    -- Coluna created_at
    IF NOT coluna_existe('tabela_sala', 'created_at') THEN
        ALTER TABLE tabela_sala ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE '✓ Coluna tabela_sala.created_at adicionada';
    END IF;
    
    -- Coluna numero
    IF NOT coluna_existe('tabela_sala', 'numero') THEN
        ALTER TABLE tabela_sala ADD COLUMN numero VARCHAR(50);
        RAISE NOTICE '✓ Coluna tabela_sala.numero adicionada';
    END IF;
    
    -- Coluna tipo
    IF NOT coluna_existe('tabela_sala', 'tipo') THEN
        ALTER TABLE tabela_sala ADD COLUMN tipo VARCHAR(100);
        RAISE NOTICE '✓ Coluna tabela_sala.tipo adicionada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_inventario - Colunas adicionais
-- ============================================================================
DO $$
BEGIN
    -- Coluna updated_at
    IF NOT coluna_existe('tabela_inventario', 'updated_at') THEN
        ALTER TABLE tabela_inventario ADD COLUMN updated_at TIMESTAMP;
        RAISE NOTICE '✓ Coluna tabela_inventario.updated_at adicionada';
    END IF;
    
    -- Coluna created_at
    IF NOT coluna_existe('tabela_inventario', 'created_at') THEN
        ALTER TABLE tabela_inventario ADD COLUMN created_at TIMESTAMP;
        RAISE NOTICE '✓ Coluna tabela_inventario.created_at adicionada';
    END IF;
    
    -- Coluna responsavel
    IF NOT coluna_existe('tabela_inventario', 'responsavel') THEN
        ALTER TABLE tabela_inventario ADD COLUMN responsavel VARCHAR(200);
        RAISE NOTICE '✓ Coluna tabela_inventario.responsavel adicionada';
    END IF;
    
    -- Coluna tipo
    IF NOT coluna_existe('tabela_inventario', 'tipo') THEN
        ALTER TABLE tabela_inventario ADD COLUMN tipo VARCHAR(50);
        RAISE NOTICE '✓ Coluna tabela_inventario.tipo adicionada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_coleta - Colunas adicionais para métricas
-- ============================================================================
DO $$
BEGIN
    -- Coluna tempo_coleta_segundos
    IF NOT coluna_existe('tabela_coleta', 'tempo_coleta_segundos') THEN
        ALTER TABLE tabela_coleta ADD COLUMN tempo_coleta_segundos INTEGER;
        RAISE NOTICE '✓ Coluna tabela_coleta.tempo_coleta_segundos adicionada';
    END IF;
    
    -- Coluna tempo_scan_segundos
    IF NOT coluna_existe('tabela_coleta', 'tempo_scan_segundos') THEN
        ALTER TABLE tabela_coleta ADD COLUMN tempo_scan_segundos INTEGER;
        RAISE NOTICE '✓ Coluna tabela_coleta.tempo_scan_segundos adicionada';
    END IF;
    
    -- Coluna tempo_preenchimento_segundos
    IF NOT coluna_existe('tabela_coleta', 'tempo_preenchimento_segundos') THEN
        ALTER TABLE tabela_coleta ADD COLUMN tempo_preenchimento_segundos INTEGER;
        RAISE NOTICE '✓ Coluna tabela_coleta.tempo_preenchimento_segundos adicionada';
    END IF;
    
    -- Coluna metodo_coleta
    IF NOT coluna_existe('tabela_coleta', 'metodo_coleta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN metodo_coleta VARCHAR(50);
        RAISE NOTICE '✓ Coluna tabela_coleta.metodo_coleta adicionada';
    END IF;
    
    -- Coluna hora_coleta
    IF NOT coluna_existe('tabela_coleta', 'hora_coleta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN hora_coleta INTEGER;
        RAISE NOTICE '✓ Coluna tabela_coleta.hora_coleta adicionada';
    END IF;
    
    -- Coluna dia_semana
    IF NOT coluna_existe('tabela_coleta', 'dia_semana') THEN
        ALTER TABLE tabela_coleta ADD COLUMN dia_semana INTEGER;
        RAISE NOTICE '✓ Coluna tabela_coleta.dia_semana adicionada';
    END IF;
    
    -- Coluna periodo_coleta
    IF NOT coluna_existe('tabela_coleta', 'periodo_coleta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN periodo_coleta VARCHAR(20);
        RAISE NOTICE '✓ Coluna tabela_coleta.periodo_coleta adicionada';
    END IF;
    
    -- Coluna tipo_scan
    IF NOT coluna_existe('tabela_coleta', 'tipo_scan') THEN
        ALTER TABLE tabela_coleta ADD COLUMN tipo_scan VARCHAR(50);
        RAISE NOTICE '✓ Coluna tabela_coleta.tipo_scan adicionada';
    END IF;
    
    -- Coluna tentativas_scan
    IF NOT coluna_existe('tabela_coleta', 'tentativas_scan') THEN
        ALTER TABLE tabela_coleta ADD COLUMN tentativas_scan INTEGER DEFAULT 1;
        RAISE NOTICE '✓ Coluna tabela_coleta.tentativas_scan adicionada';
    END IF;
    
    -- Coluna erros_scan
    IF NOT coluna_existe('tabela_coleta', 'erros_scan') THEN
        ALTER TABLE tabela_coleta ADD COLUMN erros_scan INTEGER DEFAULT 0;
        RAISE NOTICE '✓ Coluna tabela_coleta.erros_scan adicionada';
    END IF;
    
    -- Coluna qualidade_etiqueta
    IF NOT coluna_existe('tabela_coleta', 'qualidade_etiqueta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN qualidade_etiqueta VARCHAR(20);
        RAISE NOTICE '✓ Coluna tabela_coleta.qualidade_etiqueta adicionada';
    END IF;
    
    -- Coluna observacao (separada de observacao_coleta)
    IF NOT coluna_existe('tabela_coleta', 'observacao') THEN
        ALTER TABLE tabela_coleta ADD COLUMN observacao TEXT;
        RAISE NOTICE '✓ Coluna tabela_coleta.observacao adicionada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_usuario - Colunas adicionais para mobile
-- ============================================================================
DO $$
BEGIN
    -- Coluna mobile_habilitado
    IF NOT coluna_existe('tabela_usuario', 'mobile_habilitado') THEN
        ALTER TABLE tabela_usuario ADD COLUMN mobile_habilitado BOOLEAN DEFAULT TRUE;
        RAISE NOTICE '✓ Coluna tabela_usuario.mobile_habilitado adicionada';
    END IF;
    
    -- Coluna ultimo_login_mobile
    IF NOT coluna_existe('tabela_usuario', 'ultimo_login_mobile') THEN
        ALTER TABLE tabela_usuario ADD COLUMN ultimo_login_mobile TIMESTAMP;
        RAISE NOTICE '✓ Coluna tabela_usuario.ultimo_login_mobile adicionada';
    END IF;
    
    -- Coluna total_logins_mobile
    IF NOT coluna_existe('tabela_usuario', 'total_logins_mobile') THEN
        ALTER TABLE tabela_usuario ADD COLUMN total_logins_mobile INTEGER DEFAULT 0;
        RAISE NOTICE '✓ Coluna tabela_usuario.total_logins_mobile adicionada';
    END IF;
    
    -- Coluna bloqueado
    IF NOT coluna_existe('tabela_usuario', 'bloqueado') THEN
        ALTER TABLE tabela_usuario ADD COLUMN bloqueado BOOLEAN DEFAULT FALSE;
        RAISE NOTICE '✓ Coluna tabela_usuario.bloqueado adicionada';
    END IF;
    
    -- Coluna primeiro_acesso
    IF NOT coluna_existe('tabela_usuario', 'primeiro_acesso') THEN
        ALTER TABLE tabela_usuario ADD COLUMN primeiro_acesso BOOLEAN DEFAULT TRUE;
        RAISE NOTICE '✓ Coluna tabela_usuario.primeiro_acesso adicionada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_patrimonio - Colunas adicionais
-- ============================================================================
DO $$
BEGIN
    -- Coluna descricao_resumida
    IF NOT coluna_existe('tabela_patrimonio', 'descricao_resumida') THEN
        ALTER TABLE tabela_patrimonio ADD COLUMN descricao_resumida VARCHAR(200);
        RAISE NOTICE '✓ Coluna tabela_patrimonio.descricao_resumida adicionada';
    END IF;
    
    -- Coluna categoria
    IF NOT coluna_existe('tabela_patrimonio', 'categoria') THEN
        ALTER TABLE tabela_patrimonio ADD COLUMN categoria VARCHAR(100);
        RAISE NOTICE '✓ Coluna tabela_patrimonio.categoria adicionada';
    END IF;
    
    -- Coluna marca
    IF NOT coluna_existe('tabela_patrimonio', 'marca') THEN
        ALTER TABLE tabela_patrimonio ADD COLUMN marca VARCHAR(100);
        RAISE NOTICE '✓ Coluna tabela_patrimonio.marca adicionada';
    END IF;
    
    -- Coluna modelo
    IF NOT coluna_existe('tabela_patrimonio', 'modelo') THEN
        ALTER TABLE tabela_patrimonio ADD COLUMN modelo VARCHAR(100);
        RAISE NOTICE '✓ Coluna tabela_patrimonio.modelo adicionada';
    END IF;
    
    -- Coluna situacao
    IF NOT coluna_existe('tabela_patrimonio', 'situacao') THEN
        ALTER TABLE tabela_patrimonio ADD COLUMN situacao VARCHAR(50);
        RAISE NOTICE '✓ Coluna tabela_patrimonio.situacao adicionada';
    END IF;
    
    -- Coluna ed (elemento de despesa)
    IF NOT coluna_existe('tabela_patrimonio', 'ed') THEN
        ALTER TABLE tabela_patrimonio ADD COLUMN ed VARCHAR(50);
        RAISE NOTICE '✓ Coluna tabela_patrimonio.ed adicionada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: dispositivo_mobile (se não existir)
-- ============================================================================
DO $$
BEGIN
    IF NOT tabela_existe('dispositivo_mobile') THEN
        CREATE TABLE dispositivo_mobile (
            id SERIAL PRIMARY KEY,
            device_id VARCHAR(100) NOT NULL UNIQUE,
            id_usuario INTEGER NOT NULL,
            modelo VARCHAR(100),
            fabricante VARCHAR(100),
            versao_android VARCHAR(20),
            versao_app VARCHAR(20),
            endereco_ip VARCHAR(50),
            endereco_mac VARCHAR(50),
            status status_dispositivo NOT NULL DEFAULT 'APROVADO',
            data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            data_ultima_conexao TIMESTAMP,
            data_ultima_sincronizacao TIMESTAMP,
            ativo BOOLEAN NOT NULL DEFAULT TRUE,
            token_atual TEXT,
            data_expiracao_token TIMESTAMP,
            observacoes TEXT,
            CONSTRAINT fk_dispositivo_usuario FOREIGN KEY (id_usuario) 
                REFERENCES tabela_usuario(id) ON DELETE CASCADE
        );
        RAISE NOTICE '✓ Tabela dispositivo_mobile criada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_categoria_patrimonio (se não existir)
-- ============================================================================
DO $$
BEGIN
    IF NOT tabela_existe('tabela_categoria_patrimonio') THEN
        CREATE TABLE tabela_categoria_patrimonio (
            id SERIAL PRIMARY KEY,
            nome VARCHAR(200) NOT NULL,
            descricao TEXT,
            codigo VARCHAR(20),
            ativo BOOLEAN DEFAULT TRUE,
            data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        RAISE NOTICE '✓ Tabela tabela_categoria_patrimonio criada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_subcategoria_patrimonio (se não existir)
-- ============================================================================
DO $$
BEGIN
    IF NOT tabela_existe('tabela_subcategoria_patrimonio') THEN
        CREATE TABLE tabela_subcategoria_patrimonio (
            id SERIAL PRIMARY KEY,
            nome VARCHAR(200) NOT NULL,
            descricao TEXT,
            id_categoria INTEGER,
            ativo BOOLEAN DEFAULT TRUE,
            data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_subcategoria_categoria FOREIGN KEY (id_categoria) 
                REFERENCES tabela_categoria_patrimonio(id) ON DELETE SET NULL
        );
        RAISE NOTICE '✓ Tabela tabela_subcategoria_patrimonio criada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_item_composto (se não existir)
-- ============================================================================
DO $$
BEGIN
    IF NOT tabela_existe('tabela_item_composto') THEN
        CREATE TABLE tabela_item_composto (
            id SERIAL PRIMARY KEY,
            id_patrimonio_principal INTEGER NOT NULL,
            id_patrimonio_componente INTEGER NOT NULL,
            quantidade INTEGER DEFAULT 1,
            observacao TEXT,
            data_vinculacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_item_composto_principal FOREIGN KEY (id_patrimonio_principal) 
                REFERENCES tabela_patrimonio(id) ON DELETE CASCADE,
            CONSTRAINT fk_item_composto_componente FOREIGN KEY (id_patrimonio_componente) 
                REFERENCES tabela_patrimonio(id) ON DELETE CASCADE
        );
        RAISE NOTICE '✓ Tabela tabela_item_composto criada';
    END IF;
END $$;

-- ============================================================================
-- TABELA: tabela_coleta_componente (se não existir)
-- ============================================================================
DO $$
BEGIN
    IF NOT tabela_existe('tabela_coleta_componente') THEN
        CREATE TABLE tabela_coleta_componente (
            id SERIAL PRIMARY KEY,
            id_coleta INTEGER NOT NULL,
            id_patrimonio_componente INTEGER NOT NULL,
            encontrado BOOLEAN DEFAULT TRUE,
            estado_encontrado VARCHAR(50),
            observacao TEXT,
            CONSTRAINT fk_coleta_componente_coleta FOREIGN KEY (id_coleta) 
                REFERENCES tabela_coleta(id) ON DELETE CASCADE,
            CONSTRAINT fk_coleta_componente_patrimonio FOREIGN KEY (id_patrimonio_componente) 
                REFERENCES tabela_patrimonio(id) ON DELETE CASCADE
        );
        RAISE NOTICE '✓ Tabela tabela_coleta_componente criada';
    END IF;
END $$;

-- ============================================================================
-- ÍNDICES (se não existirem)
-- ============================================================================
DO $$
BEGIN
    -- Índice para busca por número de patrimônio
    IF NOT indice_existe('idx_patrimonio_numero') THEN
        CREATE INDEX idx_patrimonio_numero ON tabela_patrimonio(numero);
        RAISE NOTICE '✓ Índice idx_patrimonio_numero criado';
    END IF;
    
    -- Índice para busca por sala
    IF NOT indice_existe('idx_patrimonio_sala') THEN
        CREATE INDEX idx_patrimonio_sala ON tabela_patrimonio(id_sala);
        RAISE NOTICE '✓ Índice idx_patrimonio_sala criado';
    END IF;
    
    -- Índice para busca por responsável
    IF NOT indice_existe('idx_patrimonio_responsavel') THEN
        CREATE INDEX idx_patrimonio_responsavel ON tabela_patrimonio(id_responsavel);
        RAISE NOTICE '✓ Índice idx_patrimonio_responsavel criado';
    END IF;
    
    -- Índice para coletas por inventário
    IF NOT indice_existe('idx_coleta_inventario') THEN
        CREATE INDEX idx_coleta_inventario ON tabela_coleta(id_inventario);
        RAISE NOTICE '✓ Índice idx_coleta_inventario criado';
    END IF;
    
    -- Índice para coletas por patrimônio
    IF NOT indice_existe('idx_coleta_patrimonio') THEN
        CREATE INDEX idx_coleta_patrimonio ON tabela_coleta(id_patrimonio);
        RAISE NOTICE '✓ Índice idx_coleta_patrimonio criado';
    END IF;
    
    -- Índice para coletas por data
    IF NOT indice_existe('idx_coleta_data') THEN
        CREATE INDEX idx_coleta_data ON tabela_coleta(data_coleta);
        RAISE NOTICE '✓ Índice idx_coleta_data criado';
    END IF;
    
    -- Índice para dispositivos por usuário
    IF NOT indice_existe('idx_dispositivo_usuario') THEN
        CREATE INDEX idx_dispositivo_usuario ON dispositivo_mobile(id_usuario);
        RAISE NOTICE '✓ Índice idx_dispositivo_usuario criado';
    END IF;
    
    -- Índice para dispositivos por device_id
    IF NOT indice_existe('idx_dispositivo_device_id') THEN
        CREATE INDEX idx_dispositivo_device_id ON dispositivo_mobile(device_id);
        RAISE NOTICE '✓ Índice idx_dispositivo_device_id criado';
    END IF;
END $$;

-- ============================================================================
-- LIMPEZA: Remover funções auxiliares
-- ============================================================================
DROP FUNCTION IF EXISTS coluna_existe(TEXT, TEXT);
DROP FUNCTION IF EXISTS tabela_existe(TEXT);
DROP FUNCTION IF EXISTS indice_existe(TEXT);

-- ============================================================================
-- RESUMO FINAL
-- ============================================================================
DO $$
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '============================================================';
    RAISE NOTICE '✅ ATUALIZAÇÃO DO BANCO CONCLUÍDA COM SUCESSO';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Nenhum dado foi apagado ou modificado.';
    RAISE NOTICE 'Apenas tabelas e colunas inexistentes foram adicionadas.';
    RAISE NOTICE '============================================================';
END $$;
