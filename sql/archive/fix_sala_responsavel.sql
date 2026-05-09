DO $$
BEGIN
    -- TABELA_SALA: adicionar ATIVO se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_sala' AND column_name='ativo') THEN
        ALTER TABLE tabela_sala ADD COLUMN ativo BOOLEAN DEFAULT TRUE;
        UPDATE tabela_sala SET ativo = TRUE;
        RAISE NOTICE '✓ ATIVO adicionada em tabela_sala';
    ELSE
        RAISE NOTICE '- ATIVO ja existe em tabela_sala';
    END IF;

    -- TABELA_RESPONSAVEL: adicionar ATIVO se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_responsavel' AND column_name='ativo') THEN
        ALTER TABLE tabela_responsavel ADD COLUMN ativo BOOLEAN DEFAULT TRUE;
        UPDATE tabela_responsavel SET ativo = TRUE;
        RAISE NOTICE '✓ ATIVO adicionada em tabela_responsavel';
    ELSE
        RAISE NOTICE '- ATIVO ja existe em tabela_responsavel';
    END IF;

    -- TABELA_RESPONSAVEL: adicionar ID_SETOR se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_responsavel' AND column_name='id_setor') THEN
        ALTER TABLE tabela_responsavel ADD COLUMN id_setor INTEGER REFERENCES tabela_setor(id);
        RAISE NOTICE '✓ ID_SETOR adicionada em tabela_responsavel';
    ELSE
        RAISE NOTICE '- ID_SETOR ja existe em tabela_responsavel';
    END IF;
END $$;

SELECT 'OK' as resultado;
