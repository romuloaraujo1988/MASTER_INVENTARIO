-- ============================================================================
-- Script: Criar Índices para Histórico de Coletas
-- Descrição: Cria índices otimizados para melhorar performance das consultas
--            de histórico de coletas de patrimônio
-- Data: 2025-01-16
-- Autor: Sistema de Inventário
-- ============================================================================

-- Índice para buscar coletas por patrimônio (query mais comum)
-- Usado em: buscarColetasPorPatrimonio, buscarColetasComFiltros
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio_data 
ON TABELA_COLETA(ID_PATRIMONIO, DATA_COLETA DESC);

-- Índice para buscar coletas por inventário
-- Usado em: filtros por inventário
CREATE INDEX IF NOT EXISTS idx_coleta_inventario 
ON TABELA_COLETA(ID_INVENTARIO);

-- Índice para buscar coletas por data
-- Usado em: filtros por período
CREATE INDEX IF NOT EXISTS idx_coleta_data 
ON TABELA_COLETA(DATA_COLETA DESC);

-- Índice composto para filtros combinados (patrimônio + inventário)
-- Usado em: buscarColetasComFiltros com filtro de inventário
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio_inventario 
ON TABELA_COLETA(ID_PATRIMONIO, ID_INVENTARIO, DATA_COLETA DESC);

-- Índice para buscar coletas por participante/coletor
-- Usado em: filtros por coletor
CREATE INDEX IF NOT EXISTS idx_coleta_participante 
ON TABELA_COLETA(ID_PARTICIPANTE_INVENTARIO);

-- Índice composto para buscar coleta anterior (patrimônio + data)
-- Usado em: buscarColetaAnterior
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio_data_anterior 
ON TABELA_COLETA(ID_PATRIMONIO, DATA_COLETA);

-- ============================================================================
-- Análise de Performance
-- ============================================================================

-- Verificar uso dos índices (executar após criar)
-- EXPLAIN ANALYZE SELECT * FROM TABELA_COLETA WHERE ID_PATRIMONIO = 1 ORDER BY DATA_COLETA DESC;

-- Estatísticas das tabelas (atualizar após criar índices)
ANALYZE TABELA_COLETA;
ANALYZE TABELA_PATRIMONIO;
ANALYZE TABELA_INVENTARIO;
ANALYZE TABELA_PARTICIPANTE_INVENTARIO;

-- ============================================================================
-- Notas de Implementação
-- ============================================================================

-- 1. Índice idx_coleta_patrimonio_data é o mais importante
--    - Cobre a query mais comum (buscar histórico de um patrimônio)
--    - Ordenação DESC já está no índice para evitar sort
--
-- 2. Índice idx_coleta_patrimonio_inventario é composto
--    - Útil quando filtrar por patrimônio E inventário
--    - PostgreSQL pode usar apenas parte do índice se necessário
--
-- 3. IF NOT EXISTS evita erros se script for executado múltiplas vezes
--
-- 4. ANALYZE atualiza estatísticas para o query planner
--    - Deve ser executado após criar índices
--    - Também após grandes inserções/atualizações
--
-- 5. Performance esperada:
--    - Busca de histórico: < 50ms para 1000 coletas
--    - Filtros combinados: < 100ms
--    - Comparação de coletas: < 10ms

-- ============================================================================
-- Manutenção
-- ============================================================================

-- Reindexar se necessário (após muitas atualizações)
-- REINDEX TABLE TABELA_COLETA;

-- Verificar tamanho dos índices
-- SELECT 
--     schemaname,
--     tablename,
--     indexname,
--     pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
-- FROM pg_stat_user_indexes
-- WHERE tablename = 'tabela_coleta'
-- ORDER BY pg_relation_size(indexrelid) DESC;

-- ============================================================================
-- Fim do Script
-- ============================================================================
