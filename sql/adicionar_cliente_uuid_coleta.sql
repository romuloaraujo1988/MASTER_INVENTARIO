-- ============================================================================
--  Migração SQL — B3 (auditoria-sync — maio/2026)
--  Adiciona coluna CLIENTE_UUID em TABELA_COLETA para idempotência
--
--  PROPÓSITO
--  ---------
--  Elimina o risco de duplicata no servidor em cenário de timeout de rede:
--  o app envia uma coleta, o servidor a persiste, mas a resposta HTTP é
--  perdida no caminho. O app considera a chamada falha e reagenda o envio.
--  Sem UUID, o servidor recebe duas requisições sem como saber que são a
--  mesma coleta — resultado: duplicata persistida no banco.
--
--  Com CLIENTE_UUID, o servidor verifica antes do INSERT:
--      SELECT ID FROM TABELA_COLETA WHERE CLIENTE_UUID = ?
--  Se o UUID já existir, retorna a coleta existente (comportamento
--  idempotente) em vez de criar nova.
--
--  COMPATIBILIDADE
--  ---------------
--  - Coluna nullable: coletas existentes ficam com CLIENTE_UUID = NULL.
--    O servidor apenas faz a verificação se o app enviar o campo.
--    Clientes antigos continuam funcionando sem alteração.
--  - Índice UNIQUE parcial (WHERE CLIENTE_UUID IS NOT NULL) — permite
--    múltiplas coletas legadas com UUID null e mantém unicidade para
--    as novas.
--
--  EXECUÇÃO
--  --------
--  psql -h <host> -U <usuario> -d sispatrimonio \
--       -f sql/adicionar_cliente_uuid_coleta.sql
--
--  Deve ser executado ANTES de subir a nova versão do servidor (2.9.0+).
-- ============================================================================

-- Passo 1: adicionar coluna (idempotente)
ALTER TABLE TABELA_COLETA
    ADD COLUMN IF NOT EXISTS CLIENTE_UUID VARCHAR(36);

COMMENT ON COLUMN TABELA_COLETA.CLIENTE_UUID IS
    'UUID gerado pelo cliente (app Android) no momento da coleta. '
    'Chave de idempotência: o servidor rejeita reenvios do mesmo UUID '
    'retornando a coleta existente. NULL para coletas legadas que '
    'foram criadas antes da implementação (B3 — auditoria-sync maio/2026).';

-- Passo 2: índice único parcial (ignora NULLs)
-- Em PostgreSQL, o UNIQUE tradicional permite múltiplos NULLs. O
-- "WHERE CLIENTE_UUID IS NOT NULL" torna isso explícito e documenta
-- a intenção do esquema.
CREATE UNIQUE INDEX IF NOT EXISTS UQ_TABELA_COLETA_CLIENTE_UUID
    ON TABELA_COLETA (CLIENTE_UUID)
    WHERE CLIENTE_UUID IS NOT NULL;

-- Passo 3: verificar resultado
DO $$
DECLARE
    col_exists boolean;
    idx_exists boolean;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'tabela_coleta'
          AND column_name = 'cliente_uuid'
    ) INTO col_exists;

    SELECT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'uq_tabela_coleta_cliente_uuid'
    ) INTO idx_exists;

    IF col_exists AND idx_exists THEN
        RAISE NOTICE '✅ Migração B3 aplicada com sucesso.';
        RAISE NOTICE '   Coluna CLIENTE_UUID: OK';
        RAISE NOTICE '   Índice UQ_TABELA_COLETA_CLIENTE_UUID: OK';
    ELSE
        RAISE EXCEPTION '❌ Migração B3 falhou (coluna=%, índice=%)',
            col_exists, idx_exists;
    END IF;
END $$;
