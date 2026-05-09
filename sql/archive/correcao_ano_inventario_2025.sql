-- ============================================================
-- CORREÇÃO: Ano do Inventário Offline 2024 → 2025
-- Data: 2025
-- Descrição: Corrige o inventário padrão de fallback que foi
--            criado com ANO=2024 no banco SQLite offline.
-- ============================================================

-- Corrigir TABELA_INVENTARIO (banco SQLite offline)
UPDATE TABELA_INVENTARIO 
SET NOME = 'Inventário Offline 2025', ANO = 2025 
WHERE ID = 1 
  AND ANO = 2024 
  AND NOME LIKE '%Offline 2024%';

-- Verificar resultado
SELECT ID, NOME, ANO, STATUS_INVENTARIO FROM TABELA_INVENTARIO;
