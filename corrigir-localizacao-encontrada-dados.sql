-- ========================================
-- CORREÇÃO: Atualizar localizacao_encontrada com dados corretos
-- Data: 26/11/2025
-- Motivo: Dados antigos têm valores inconsistentes
-- ========================================

-- Mostrar dados ANTES da correção
SELECT '=== ANTES DA CORREÇÃO ===' as status;
SELECT 
    id,
    numero_patrimonio,
    localizacao_atual,
    localizacao_encontrada
FROM local_coleta
LIMIT 10;

-- Atualizar localizacao_encontrada para usar localizacao_atual
-- (que contém a identificação completa da sala)
UPDATE local_coleta 
SET localizacao_encontrada = localizacao_atual
WHERE localizacao_encontrada != localizacao_atual 
   OR localizacao_encontrada IS NULL;

-- Mostrar dados DEPOIS da correção
SELECT '=== DEPOIS DA CORREÇÃO ===' as status;
SELECT 
    id,
    numero_patrimonio,
    localizacao_atual,
    localizacao_encontrada
FROM local_coleta
LIMIT 10;

-- Estatísticas finais
SELECT '=== ESTATÍSTICAS ===' as status;
SELECT 
    COUNT(*) as total_registros,
    COUNT(CASE WHEN localizacao_encontrada = localizacao_atual THEN 1 END) as consistentes,
    COUNT(CASE WHEN localizacao_encontrada != localizacao_atual THEN 1 END) as inconsistentes
FROM local_coleta;
