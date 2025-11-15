-- =====================================================
-- SCRIPT PARA POPULAR NUMERO_SALA
-- Sistema de Inventário IFMT
-- =====================================================
-- 
-- Este script extrai o número/código da sala da DESCRICAO
-- e popula o campo NUMERO_SALA
-- 
-- =====================================================

-- Atualizar NUMERO_SALA extraindo da DESCRICAO
-- Remove o sufixo (IFMT - XXX) e mantém apenas o nome/número

UPDATE TABELA_SALA
SET NUMERO_SALA = TRIM(
    REGEXP_REPLACE(
        DESCRICAO, 
        '\s*\(IFMT\s*-\s*[^)]+\)\s*$', 
        '', 
        'g'
    )
)
WHERE NUMERO_SALA IS NULL OR NUMERO_SALA = '';

-- Verificar resultado
SELECT 
    ID_SALA,
    NUMERO_SALA,
    DESCRICAO,
    ATIVO
FROM TABELA_SALA
ORDER BY NUMERO_SALA
LIMIT 20;

-- Estatísticas
SELECT 
    COUNT(*) as total_salas,
    COUNT(NUMERO_SALA) as com_numero,
    COUNT(*) - COUNT(NUMERO_SALA) as sem_numero
FROM TABELA_SALA;

SELECT 'Campo NUMERO_SALA atualizado com sucesso!' as resultado;
