-- Correção do campo ANO na tabela_inventario
-- Problema: o campo ANO era derivado da DATA_INICIO ao invés da DATA_FIM,
-- fazendo o "Inventário Anual 2025" aparecer como (2024) no sistema.
--
-- Regra correta: o ANO identifica o ano em que o inventário TERMINA/CONCLUI.
-- Um inventário que vai de 23/11/2025 a 15/04/2026 com nome "Anual 2025"
-- deve ter ANO = 2025 (conforme o nome definido pelo usuário).
--
-- Execute este script uma única vez para corrigir o registro existente.

UPDATE tabela_inventario
SET ano = 2025
WHERE id = 2
  AND nome = 'Inventário Anual 2025'
  AND ano = 2024;

-- Verificar resultado
SELECT id, nome, ano, data_inicio, data_fim, status_inventario
FROM tabela_inventario
ORDER BY id;
