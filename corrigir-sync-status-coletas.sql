-- Script para corrigir sync_status das coletas já sincronizadas
-- Data: 26/11/2025

-- 1. Verificar estado atual
SELECT '=== ANTES DA CORREÇÃO ===' as info;
SELECT 'Coletas na local_coleta:' as info, COUNT(*) as total FROM local_coleta;
SELECT 'Coletas com PENDING:' as info, COUNT(*) as total FROM local_coleta WHERE sync_status = 'PENDING';
SELECT 'Coletas com SYNCED:' as info, COUNT(*) as total FROM local_coleta WHERE sync_status = 'SYNCED';

SELECT 'Operações na sync_control:' as info, COUNT(*) as total FROM sync_control WHERE table_name = 'local_coleta';
SELECT 'Operações sincronizadas (synced=1):' as info, COUNT(*) as total FROM sync_control WHERE table_name = 'local_coleta' AND synced = 1;
SELECT 'Operações pendentes (synced=0):' as info, COUNT(*) as total FROM sync_control WHERE table_name = 'local_coleta' AND synced = 0;

-- 2. Atualizar sync_status das coletas que já foram sincronizadas
UPDATE local_coleta 
SET sync_status = 'SYNCED' 
WHERE id IN (
    SELECT record_id 
    FROM sync_control 
    WHERE table_name = 'local_coleta' 
    AND synced = 1
);

SELECT '✅ sync_status atualizado para coletas sincronizadas' as resultado;

-- 3. Verificar resultado
SELECT '=== APÓS A CORREÇÃO ===' as info;
SELECT 'Coletas com PENDING:' as info, COUNT(*) as total FROM local_coleta WHERE sync_status = 'PENDING';
SELECT 'Coletas com SYNCED:' as info, COUNT(*) as total FROM local_coleta WHERE sync_status = 'SYNCED';

-- 4. Mostrar detalhes das coletas
SELECT 'Detalhes das coletas:' as info;
SELECT id, id_participante, sync_status FROM local_coleta;
