-- Script para corrigir id_participante nas coletas pendentes
-- Data: 26/11/2025

-- 1. Verificar quantas coletas precisam ser corrigidas
SELECT 'Coletas pendentes com id_participante=0:' as info;
SELECT COUNT(*) as total FROM sync_control 
WHERE table_name = 'local_coleta' 
AND synced = 0 
AND data_json LIKE '%"id_participante":0%';

-- 2. Corrigir id_participante de 0 para 2 (participante do usuário 1)
UPDATE sync_control 
SET data_json = REPLACE(data_json, '"id_participante":0', '"id_participante":2')
WHERE table_name = 'local_coleta' 
AND synced = 0 
AND data_json LIKE '%"id_participante":0%';

-- 3. Também corrigir na tabela local_coleta
UPDATE local_coleta 
SET id_participante = 2 
WHERE id_participante = 0 OR id_participante IS NULL;

-- 4. Verificar resultado
SELECT 'Após correção - coletas com id_participante=0:' as info;
SELECT COUNT(*) as total FROM sync_control 
WHERE table_name = 'local_coleta' 
AND synced = 0 
AND data_json LIKE '%"id_participante":0%';

-- 5. Mostrar amostra dos dados corrigidos
SELECT 'Amostra dos dados corrigidos:' as info;
SELECT id, substr(data_json, 1, 200) as data_preview 
FROM sync_control 
WHERE table_name = 'local_coleta' 
AND synced = 0 
LIMIT 2;
