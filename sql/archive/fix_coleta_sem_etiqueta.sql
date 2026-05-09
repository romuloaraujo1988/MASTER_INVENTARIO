-- Fix: campos obrigatorios para coleta sem plaqueta
DO $$
BEGIN
    -- DESCRICAO_ITEM_SEM_ETIQUETA (DAO usa esse nome; banco tinha 'descricao')
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='tabela_coleta' AND column_name='descricao_item_sem_etiqueta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN descricao_item_sem_etiqueta VARCHAR(500);
        UPDATE tabela_coleta SET descricao_item_sem_etiqueta = descricao WHERE descricao IS NOT NULL;
        RAISE NOTICE 'DESCRICAO_ITEM_SEM_ETIQUETA adicionada e dados migrados de DESCRICAO';
    ELSE
        RAISE NOTICE 'DESCRICAO_ITEM_SEM_ETIQUETA ja existe';
    END IF;

    -- CATEGORIA_ITEM_SEM_ETIQUETA
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='tabela_coleta' AND column_name='categoria_item_sem_etiqueta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN categoria_item_sem_etiqueta VARCHAR(100);
        RAISE NOTICE 'CATEGORIA_ITEM_SEM_ETIQUETA adicionada';
    ELSE
        RAISE NOTICE 'CATEGORIA_ITEM_SEM_ETIQUETA ja existe';
    END IF;

    -- OBSERVACAO_COLETA (DAO usa esse nome; banco tinha 'observacoes_coleta')
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='tabela_coleta' AND column_name='observacao_coleta') THEN
        ALTER TABLE tabela_coleta ADD COLUMN observacao_coleta TEXT;
        UPDATE tabela_coleta SET observacao_coleta = observacoes_coleta WHERE observacoes_coleta IS NOT NULL;
        RAISE NOTICE 'OBSERVACAO_COLETA adicionada e dados migrados';
    ELSE
        RAISE NOTICE 'OBSERVACAO_COLETA ja existe';
    END IF;
END $$;

-- Confirmacao
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tabela_coleta'
  AND column_name IN (
      'descricao_item_sem_etiqueta',
      'categoria_item_sem_etiqueta',
      'observacao_coleta',
      'sem_etiqueta',
      'status_coleta',
      'foto_patrimonio',
      'estado_encontrado',
      'divergencia'
  )
ORDER BY column_name;
