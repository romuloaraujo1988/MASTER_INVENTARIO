-- Script para limpar todas as coletas locais
-- Data: 26/11/2025
-- Objetivo: Remover coletas com problemas e começar do zero

-- 1. Mostrar estatísticas antes da limpeza
SELECT '=== ANTES DA LIMPEZA ===' as info;
SELECT 'Total de coletas na local_coleta:' as info, COUNT(*) as total FROM local_coleta;
SELECT 'Total de operações na sync_control:' as info, COUNT(*) as total FROM sync_control WHERE table_name = 'local_coleta';
SELECT 'Coletas pendentes:' as info, COUNT(*) as total FROM sync_control WHERE table_name = 'local_coleta' AND synced = 0;

-- 2. Limpar tabela local_coleta
DELETE FROM local_coleta;
SELECT 'Coletas removidas da local_coleta' as resultado;

-- 3. Limpar operações de coleta na sync_control
DELETE FROM sync_control WHERE table_name = 'local_coleta';
SELECT 'Operações de coleta removidas da sync_control' as resultado;

-- 4. Resetar autoincrement (opcional)
DELETE FROM sqlite_sequence WHERE name = 'local_coleta';
SELECT 'Autoincrement resetado' as resultado;

-- 5. Mostrar estatísticas após a limpeza
SELECT '=== APÓS A LIMPEZA ===' as info;
SELECT 'Total de coletas na local_coleta:' as info, COUNT(*) as total FROM local_coleta;
SELECT 'Total de operações na sync_control:' as info, COUNT(*) as total FROM sync_control WHERE table_name = 'local_coleta';

-- 6. Verificar se participantes ainda estão lá
SELECT '=== VERIFICAÇÃO ===' as info;
SELECT 'Participantes disponíveis:' as info, COUNT(*) as total FROM local_participante_inventario;
SELECT 'Patrimônios disponíveis:' as info, COUNT(*) as total FROM local_patrimonio;
SELECT 'Inventários disponíveis:' as info, COUNT(*) as total FROM local_inventario;

SELECT '✅ Limpeza concluída! Pronto para fazer novas coletas.' as resultado;
