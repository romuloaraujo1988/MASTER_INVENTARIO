-- =====================================================
-- Script Consolidado: Ativação do Serviço Mobile
-- Descrição: Cria todas as tabelas necessárias para o funcionamento do serviço mobile
-- Autor: Sistema de Inventário
-- Data: 22/11/2025
-- =====================================================

-- PARTE 1: TABELA ESSENCIAL - dispositivo_mobile
-- Esta é a tabela mínima necessária para o serviço funcionar
-- =====================================================

\echo '>>> Criando ENUM status_dispositivo...'

-- Criar ENUM para status do dispositivo (SE NÃO EXISTIR)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'status_dispositivo') THEN
        CREATE TYPE status_dispositivo AS ENUM ('PENDENTE', 'APROVADO', 'BLOQUEADO', 'REJEITADO');
        RAISE NOTICE 'ENUM status_dispositivo criado com sucesso';
    ELSE
        RAISE NOTICE 'ENUM status_dispositivo já existe';
    END IF;
END $$;

\echo '>>> Criando tabela dispositivo_mobile...'

-- Criar tabela DISPOSITIVO_MOBILE
CREATE TABLE IF NOT EXISTS dispositivo_mobile (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL UNIQUE,
    id_usuario INTEGER NOT NULL,
    modelo VARCHAR(100),
    fabricante VARCHAR(100),
    versao_android VARCHAR(20),
    versao_app VARCHAR(20),
    endereco_ip VARCHAR(45),
    endereco_mac VARCHAR(17),
    status status_dispositivo NOT NULL DEFAULT 'APROVADO',
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_ultima_conexao TIMESTAMP,
    data_ultima_sincronizacao TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    token_atual TEXT,
    data_expiracao_token TIMESTAMP,
    observacoes TEXT,
    
    -- Foreign Keys
    CONSTRAINT fk_dispositivo_usuario 
        FOREIGN KEY (id_usuario) 
        REFERENCES tabela_usuario(id) 
        ON DELETE CASCADE
);

\echo '>>> Criando índices para dispositivo_mobile...'

-- Criar índices para performance
CREATE INDEX IF NOT EXISTS idx_dispositivo_device_id ON dispositivo_mobile(device_id);
CREATE INDEX IF NOT EXISTS idx_dispositivo_usuario ON dispositivo_mobile(id_usuario);
CREATE INDEX IF NOT EXISTS idx_dispositivo_status ON dispositivo_mobile(status);
CREATE INDEX IF NOT EXISTS idx_dispositivo_ativo ON dispositivo_mobile(ativo);

-- Comentários
COMMENT ON TABLE dispositivo_mobile IS 'Gerenciamento de dispositivos móveis conectados ao sistema';
COMMENT ON COLUMN dispositivo_mobile.device_id IS 'ID único do dispositivo (Android ID)';
COMMENT ON COLUMN dispositivo_mobile.id_usuario IS 'Usuário proprietário do dispositivo';
COMMENT ON COLUMN dispositivo_mobile.status IS 'Status do dispositivo: PENDENTE, APROVADO, BLOQUEADO, REJEITADO';

\echo '>>> Tabela dispositivo_mobile criada com sucesso!'

-- =====================================================
-- PARTE 2 (OPCIONAL): TABELAS COMPLEMENTARES
-- Estas tabelas são recomendadas para funcionalidade completa
-- Você pode comentar esta seção se quiser apenas o mínimo
-- =====================================================

\echo ''
\echo '>>> Iniciando criação de tabelas complementares...'

-- Adicionar campos mobile na tabela coleta (se não existirem)
\echo '>>> Adicionando campos mobile na tabela coleta...'

DO $$ 
BEGIN
    -- Device ID
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='coleta' AND column_name='device_id') THEN
        ALTER TABLE coleta ADD COLUMN device_id VARCHAR(255);
        CREATE INDEX IF NOT EXISTS idx_coleta_device_id ON coleta(device_id);
        RAISE NOTICE 'Campo device_id adicionado à tabela coleta';
    END IF;
    
    -- Coordenadas GPS
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='coleta' AND column_name='latitude') THEN
        ALTER TABLE coleta ADD COLUMN latitude DECIMAL(10, 8);
        RAISE NOTICE 'Campo latitude adicionado à tabela coleta';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='coleta' AND column_name='longitude') THEN
        ALTER TABLE coleta ADD COLUMN longitude DECIMAL(11, 8);
        RAISE NOTICE 'Campo longitude adicionado à tabela coleta';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='coleta' AND column_name='precisao_gps') THEN
        ALTER TABLE coleta ADD COLUMN precisao_gps DECIMAL(8, 2);
        RAISE NOTICE 'Campo precisao_gps adicionado à tabela coleta';
    END IF;
    
    -- Origem (WEB ou MOBILE)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='coleta' AND column_name='origem') THEN
        ALTER TABLE coleta ADD COLUMN origem VARCHAR(20) DEFAULT 'WEB';
        CREATE INDEX IF NOT EXISTS idx_coleta_origem ON coleta(origem);
        RAISE NOTICE 'Campo origem adicionado à tabela coleta';
    END IF;
    
    -- Versão do app
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='coleta' AND column_name='app_version') THEN
        ALTER TABLE coleta ADD COLUMN app_version VARCHAR(50);
        RAISE NOTICE 'Campo app_version adicionado à tabela coleta';
    END IF;
    
    -- Controle de sincronização
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='coleta' AND column_name='sincronizado') THEN
        ALTER TABLE coleta ADD COLUMN sincronizado BOOLEAN DEFAULT TRUE;
        CREATE INDEX IF NOT EXISTS idx_coleta_sincronizado ON coleta(sincronizado);
        RAISE NOTICE 'Campo sincronizado adicionado à tabela coleta';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='coleta' AND column_name='data_sincronizacao') THEN
        ALTER TABLE coleta ADD COLUMN data_sincronizacao TIMESTAMP;
        RAISE NOTICE 'Campo data_sincronizacao adicionado à tabela coleta';
    END IF;
END $$;

-- Adicionar campos QR Code na tabela patrimonio (se não existirem)
\echo '>>> Adicionando campos QR Code na tabela patrimonio...'

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='patrimonio' AND column_name='qr_code') THEN
        ALTER TABLE patrimonio ADD COLUMN qr_code VARCHAR(255);
        CREATE INDEX IF NOT EXISTS idx_patrimonio_qr_code ON patrimonio(qr_code);
        RAISE NOTICE 'Campo qr_code adicionado à tabela patrimonio';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='patrimonio' AND column_name='qr_gerado') THEN
        ALTER TABLE patrimonio ADD COLUMN qr_gerado BOOLEAN DEFAULT FALSE;
        CREATE INDEX IF NOT EXISTS idx_patrimonio_qr_gerado ON patrimonio(qr_gerado);
        RAISE NOTICE 'Campo qr_gerado adicionado à tabela patrimonio';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='patrimonio' AND column_name='data_qr_geracao') THEN
        ALTER TABLE patrimonio ADD COLUMN data_qr_geracao TIMESTAMP;
        RAISE NOTICE 'Campo data_qr_geracao adicionado à tabela patrimonio';
    END IF;
END $$;

-- Adicionar campos mobile na tabela usuario (se não existirem)
\echo '>>> Adicionando campos mobile na tabela usuario...'

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='tabela_usuario' AND column_name='mobile_habilitado') THEN
        ALTER TABLE tabela_usuario ADD COLUMN mobile_habilitado BOOLEAN DEFAULT TRUE;
        RAISE NOTICE 'Campo mobile_habilitado adicionado à tabela tabela_usuario';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='tabela_usuario' AND column_name='ultimo_login_mobile') THEN
        ALTER TABLE tabela_usuario ADD COLUMN ultimo_login_mobile TIMESTAMP;
        RAISE NOTICE 'Campo ultimo_login_mobile adicionado à tabela tabela_usuario';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name='tabela_usuario' AND column_name='total_logins_mobile') THEN
        ALTER TABLE tabela_usuario ADD COLUMN total_logins_mobile INTEGER DEFAULT 0;
        RAISE NOTICE 'Campo total_logins_mobile adicionado à tabela tabela_usuario';
    END IF;
END $$;

-- =====================================================
-- VERIFICAÇÃO FINAL
-- =====================================================

\echo ''
\echo '=============================================='
\echo 'RESUMO DA ATIVAÇÃO DO SERVIÇO MOBILE'
\echo '=============================================='

-- Verificar estrutura da tabela principal
\echo ''
\echo 'Estrutura da tabela dispositivo_mobile:'
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'dispositivo_mobile'
ORDER BY ordinal_position;

-- Contar registros (deve estar vazio inicialmente)
\echo ''
\echo 'Total de dispositivos registrados:'
SELECT COUNT(*) as total_dispositivos FROM dispositivo_mobile;

\echo ''
\echo '=============================================='
\echo '✓ Serviço Mobile ATIVADO com sucesso!'
\echo '=============================================='
\echo ''
\echo 'Próximos passos:'
\echo '1. Reiniciar o servidor mobile'
\echo '2. Testar a interface de Dispositivos Mobile'
\echo '3. Registrar um dispositivo via app Android'
\echo ''
