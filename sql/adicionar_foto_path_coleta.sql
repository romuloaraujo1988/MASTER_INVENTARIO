-- ============================================================
-- Migration: Adicionar coluna foto_path na tabela_coleta
-- Data: 20/05/2026
-- Motivo: Separar o caminho relativo da foto no servidor
--         (foto_path) do campo legado foto_patrimonio (Base64).
--         O campo foto_path é preenchido pelo endpoint
--         POST /api/mobile/fotos/upload após o upload do arquivo.
-- ============================================================

-- 1. Adicionar coluna foto_path (caminho relativo no servidor)
ALTER TABLE tabela_coleta
    ADD COLUMN IF NOT EXISTS foto_path VARCHAR(500);

COMMENT ON COLUMN tabela_coleta.foto_path IS
'Caminho relativo da foto no servidor após upload via PhotoSyncWorker.
Formato: inventario_{id}/{YYYY-MM}/{tipo}/coleta_{coletaId}_{identificador}.jpg
Distinto de foto_patrimonio (campo legado Base64 — não usar para novas fotos).';

-- 2. Índice para acelerar a query do relatório fotográfico
CREATE INDEX IF NOT EXISTS idx_coleta_foto_path
    ON tabela_coleta (id_inventario, foto_path)
    WHERE foto_path IS NOT NULL AND foto_path <> '';

-- 3. Verificação
SELECT
    COUNT(*) AS total_coletas,
    COUNT(CASE WHEN foto_path IS NOT NULL AND foto_path <> '' THEN 1 END) AS com_foto_path,
    COUNT(CASE WHEN foto_patrimonio IS NOT NULL AND foto_patrimonio <> '' THEN 1 END) AS com_foto_patrimonio_legado,
    COUNT(CASE WHEN sem_etiqueta = true THEN 1 END) AS sem_etiqueta
FROM tabela_coleta;
