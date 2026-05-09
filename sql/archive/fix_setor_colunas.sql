-- Adicionar colunas faltantes na TABELA_SETOR
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_setor' AND column_name='descricao') THEN
        ALTER TABLE TABELA_SETOR ADD COLUMN DESCRICAO TEXT;
        RAISE NOTICE '✓ DESCRICAO adicionada';
    ELSE
        RAISE NOTICE '- DESCRICAO ja existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_setor' AND column_name='responsavel_setor') THEN
        ALTER TABLE TABELA_SETOR ADD COLUMN RESPONSAVEL_SETOR VARCHAR(255);
        RAISE NOTICE '✓ RESPONSAVEL_SETOR adicionada';
    ELSE
        RAISE NOTICE '- RESPONSAVEL_SETOR ja existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tabela_setor' AND column_name='ativo') THEN
        ALTER TABLE TABELA_SETOR ADD COLUMN ATIVO BOOLEAN DEFAULT TRUE;
        UPDATE TABELA_SETOR SET ATIVO = TRUE WHERE ATIVO IS NULL;
        RAISE NOTICE '✓ ATIVO adicionada';
    ELSE
        RAISE NOTICE '- ATIVO ja existe';
    END IF;
END $$;

SELECT 'Schema TABELA_SETOR OK!' as resultado;
SELECT column_name, data_type FROM information_schema.columns WHERE table_name='tabela_setor' ORDER BY ordinal_position;
