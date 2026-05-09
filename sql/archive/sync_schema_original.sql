-- Sincronizar banco atual com o schema do dump original de produção
DO $$
BEGIN
    -- tabela_inventario: adicionar colunas do schema original
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_inventario' AND column_name='status_inventario') THEN
        ALTER TABLE tabela_inventario ADD COLUMN status_inventario VARCHAR(50) DEFAULT 'PLANEJADO';
        -- copiar dados do campo antigo se existir
        UPDATE tabela_inventario SET status_inventario = status WHERE status_inventario IS NULL;
        RAISE NOTICE '✓ STATUS_INVENTARIO adicionada em tabela_inventario';
    ELSE
        RAISE NOTICE '- STATUS_INVENTARIO ja existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_inventario' AND column_name='responsavel_inventario') THEN
        ALTER TABLE tabela_inventario ADD COLUMN responsavel_inventario VARCHAR(255);
        RAISE NOTICE '✓ RESPONSAVEL_INVENTARIO adicionada';
    ELSE RAISE NOTICE '- RESPONSAVEL_INVENTARIO ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_inventario' AND column_name='total_patrimonios') THEN
        ALTER TABLE tabela_inventario ADD COLUMN total_patrimonios INTEGER DEFAULT 0;
        RAISE NOTICE '✓ TOTAL_PATRIMONIOS adicionada';
    ELSE RAISE NOTICE '- TOTAL_PATRIMONIOS ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_inventario' AND column_name='patrimonios_coletados') THEN
        ALTER TABLE tabela_inventario ADD COLUMN patrimonios_coletados INTEGER DEFAULT 0;
        RAISE NOTICE '✓ PATRIMONIOS_COLETADOS adicionada';
    ELSE RAISE NOTICE '- PATRIMONIOS_COLETADOS ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_inventario' AND column_name='percentual_conclusao') THEN
        ALTER TABLE tabela_inventario ADD COLUMN percentual_conclusao NUMERIC(5,2) DEFAULT 0.00;
        RAISE NOTICE '✓ PERCENTUAL_CONCLUSAO adicionada';
    ELSE RAISE NOTICE '- PERCENTUAL_CONCLUSAO ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_inventario' AND column_name='data_criacao') THEN
        ALTER TABLE tabela_inventario ADD COLUMN data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE '✓ DATA_CRIACAO adicionada';
    ELSE RAISE NOTICE '- DATA_CRIACAO ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_inventario' AND column_name='data_ultima_atualizacao') THEN
        ALTER TABLE tabela_inventario ADD COLUMN data_ultima_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE '✓ DATA_ULTIMA_ATUALIZACAO adicionada';
    ELSE RAISE NOTICE '- DATA_ULTIMA_ATUALIZACAO ja existe'; END IF;

    -- tabela_coleta: adicionar colunas do schema original
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_coleta' AND column_name='metodo_coleta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN metodo_coleta VARCHAR(20);
        RAISE NOTICE '✓ METODO_COLETA adicionada';
    ELSE RAISE NOTICE '- METODO_COLETA ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_coleta' AND column_name='tempo_coleta_segundos') THEN
        ALTER TABLE tabela_coleta ADD COLUMN tempo_coleta_segundos INTEGER;
        RAISE NOTICE '✓ TEMPO_COLETA_SEGUNDOS adicionada';
    ELSE RAISE NOTICE '- TEMPO_COLETA_SEGUNDOS ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_coleta' AND column_name='hora_coleta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN hora_coleta INTEGER;
        ALTER TABLE tabela_coleta ADD COLUMN dia_semana INTEGER;
        ALTER TABLE tabela_coleta ADD COLUMN periodo_coleta VARCHAR(10);
        ALTER TABLE tabela_coleta ADD COLUMN tipo_scan VARCHAR(20);
        ALTER TABLE tabela_coleta ADD COLUMN tentativas_scan INTEGER DEFAULT 1;
        ALTER TABLE tabela_coleta ADD COLUMN erros_scan INTEGER DEFAULT 0;
        ALTER TABLE tabela_coleta ADD COLUMN qualidade_etiqueta VARCHAR(20);
        RAISE NOTICE '✓ Colunas de metricas de scan adicionadas em tabela_coleta';
    ELSE RAISE NOTICE '- Colunas de metricas ja existem'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_coleta' AND column_name='status_coleta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN status_coleta VARCHAR(50) DEFAULT 'COLETADO';
        RAISE NOTICE '✓ STATUS_COLETA adicionada';
    ELSE RAISE NOTICE '- STATUS_COLETA ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_coleta' AND column_name='localizacao_encontrada') THEN
        ALTER TABLE tabela_coleta ADD COLUMN localizacao_encontrada VARCHAR(255);
        RAISE NOTICE '✓ LOCALIZACAO_ENCONTRADA adicionada';
    ELSE RAISE NOTICE '- LOCALIZACAO_ENCONTRADA ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_coleta' AND column_name='estado_encontrado') THEN
        ALTER TABLE tabela_coleta ADD COLUMN estado_encontrado VARCHAR(50);
        RAISE NOTICE '✓ ESTADO_ENCONTRADO adicionada';
    ELSE RAISE NOTICE '- ESTADO_ENCONTRADO ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_coleta' AND column_name='divergencia') THEN
        ALTER TABLE tabela_coleta ADD COLUMN divergencia BOOLEAN DEFAULT FALSE;
        ALTER TABLE tabela_coleta ADD COLUMN motivo_divergencia TEXT;
        RAISE NOTICE '✓ DIVERGENCIA adicionada';
    ELSE RAISE NOTICE '- DIVERGENCIA ja existe'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_coleta' AND column_name='foto_patrimonio') THEN
        ALTER TABLE tabela_coleta ADD COLUMN foto_patrimonio TEXT;
        RAISE NOTICE '✓ FOTO_PATRIMONIO adicionada';
    ELSE RAISE NOTICE '- FOTO_PATRIMONIO ja existe'; END IF;

    -- tabela_setor: colunas extras do schema original
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_setor' AND column_name='email') THEN
        ALTER TABLE tabela_setor ADD COLUMN email VARCHAR(100);
        ALTER TABLE tabela_setor ADD COLUMN telefone VARCHAR(20);
        ALTER TABLE tabela_setor ADD COLUMN observacoes TEXT;
        ALTER TABLE tabela_setor ADD COLUMN responsavel VARCHAR(100);
        RAISE NOTICE '✓ Colunas extras adicionadas em tabela_setor';
    ELSE RAISE NOTICE '- Colunas extras de setor ja existem'; END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_setor' AND column_name='id_campus') THEN
        ALTER TABLE tabela_setor ADD COLUMN id_campus INTEGER;
        RAISE NOTICE '✓ ID_CAMPUS adicionada em tabela_setor';
    ELSE RAISE NOTICE '- ID_CAMPUS ja existe em tabela_setor'; END IF;

    -- tabela_responsavel: CPF
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_responsavel' AND column_name='cpf') THEN
        ALTER TABLE tabela_responsavel ADD COLUMN cpf VARCHAR(14);
        ALTER TABLE tabela_responsavel ADD COLUMN data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE '✓ CPF adicionado em tabela_responsavel';
    ELSE RAISE NOTICE '- CPF ja existe em tabela_responsavel'; END IF;

    -- tabela_patrimonio: colunas do schema original
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_patrimonio' AND column_name='descricao_resumida') THEN
        ALTER TABLE tabela_patrimonio ADD COLUMN descricao_resumida VARCHAR(500);
        ALTER TABLE tabela_patrimonio ADD COLUMN situacao VARCHAR(20);
        RAISE NOTICE '✓ DESCRICAO_RESUMIDA/SITUACAO adicionadas em tabela_patrimonio';
    ELSE RAISE NOTICE '- DESCRICAO_RESUMIDA ja existe'; END IF;

END $$;

-- Criar índice de status_inventario (novo campo)
CREATE INDEX IF NOT EXISTS idx_inventario_status_inventario ON tabela_inventario(status_inventario);

SELECT 'Sincronia com schema original concluída!' AS resultado;
SELECT table_name, COUNT(*) as colunas FROM information_schema.columns
WHERE table_name IN ('tabela_setor','tabela_responsavel','tabela_sala','tabela_patrimonio',
                     'tabela_inventario','tabela_coleta','tabela_usuario','tabela_coletor')
GROUP BY table_name ORDER BY table_name;
