-- =====================================================
-- OTIMIZAÇÃO DE ÍNDICES - TABELA_SALA
-- Sistema de Inventário IFMT
-- =====================================================
-- 
-- Este script adiciona índices para melhorar a performance
-- das consultas de salas no app mobile.
-- 
-- SEGURO: Usa IF NOT EXISTS - não quebra se já existir
-- IMPACTO: Melhora significativa na velocidade de queries
-- 
-- =====================================================

-- Índice para busca por nome (usado em filtros)
CREATE INDEX IF NOT EXISTS idx_sala_nome 
ON TABELA_SALA(NOME);

-- Índice para busca por código
CREATE INDEX IF NOT EXISTS idx_sala_codigo 
ON TABELA_SALA(CODIGO);

-- Índice para filtrar salas ativas
CREATE INDEX IF NOT EXISTS idx_sala_ativo 
ON TABELA_SALA(ATIVO);

-- Índice para buscar por setor
CREATE INDEX IF NOT EXISTS idx_sala_setor 
ON TABELA_SALA(ID_SETOR);

-- Índice composto para paginação otimizada
-- (usado em ORDER BY NOME com filtro ATIVO)
CREATE INDEX IF NOT EXISTS idx_sala_ativo_nome 
ON TABELA_SALA(ATIVO, NOME);

-- Índice composto para busca por setor e nome
CREATE INDEX IF NOT EXISTS idx_sala_setor_nome 
ON TABELA_SALA(ID_SETOR, NOME);

-- Verificar índices criados
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'tabela_sala'
ORDER BY indexname;

-- Estatísticas de uso dos índices (após algum tempo de uso)
-- Descomentar para verificar:
/*
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as "Vezes Usado",
    idx_tup_read as "Tuplas Lidas",
    idx_tup_fetch as "Tuplas Retornadas"
FROM pg_stat_user_indexes
WHERE tablename = 'tabela_sala'
ORDER BY idx_scan DESC;
*/

-- Análise da tabela para atualizar estatísticas
ANALYZE TABELA_SALA;

SELECT 'Índices de TABELA_SALA otimizados com sucesso!' as resultado;
