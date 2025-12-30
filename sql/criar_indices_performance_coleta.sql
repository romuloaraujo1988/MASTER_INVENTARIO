-- ============================================================================
-- SCRIPT: Criar Índices para Otimizar Performance de Coleta
-- ============================================================================
-- Objetivo: Reduzir tempo de coleta de patrimônios em 80-90%
-- Impacto: Queries mais rápidas, especialmente em coletas em lote
-- Data: 26/11/2025
-- ============================================================================

-- ============================================================================
-- 1. ÍNDICE CRÍTICO: Busca de Participante (Impacto: -40%)
-- ============================================================================
-- Usado em: ParticipanteInventarioDAO.buscarIdParticipantePorUsuario()
-- Queries por coleta: 1 (antes: 2)
-- Melhoria esperada: 50-100ms por coleta

CREATE INDEX IF NOT EXISTS idx_participante_inventario_usuario 
ON tabela_participante_inventario(id_inventario, id_usuario, ativo);

COMMENT ON INDEX idx_participante_inventario_usuario IS 
'Índice para busca rápida de participante por inventário e usuário. Crítico para performance de coleta.';

-- ============================================================================
-- 2. ÍNDICE CRÍTICO: Verificação de Duplicação (Impacto: -15%)
-- ============================================================================
-- Usado em: ColetaDAO.coletaExiste()
-- Queries por coleta: 1
-- Melhoria esperada: 30-50ms por coleta

CREATE INDEX IF NOT EXISTS idx_coleta_inventario_patrimonio 
ON tabela_coleta(id_inventario, id_patrimonio);

COMMENT ON INDEX idx_coleta_inventario_patrimonio IS 
'Índice para verificação rápida de coletas duplicadas. Essencial para evitar duplicação.';

-- ============================================================================
-- 3. ÍNDICE IMPORTANTE: Busca de Coletas por Sala (Impacto: -10%)
-- ============================================================================
-- Usado em: ColetaDAO.buscarColetasPorSala()
-- Queries por coleta: 0 (usado em relatórios, não em coleta)
-- Melhoria esperada: 20-30ms em relatórios

CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio_sala 
ON tabela_coleta(id_patrimonio) 
WHERE id_patrimonio IS NOT NULL;

COMMENT ON INDEX idx_coleta_patrimonio_sala IS 
'Índice para busca rápida de coletas por sala. Melhora performance de relatórios.';

-- ============================================================================
-- 4. ÍNDICE IMPORTANTE: Busca de Coletas por Inventário (Impacto: -5%)
-- ============================================================================
-- Usado em: ColetaDAO.buscarPorInventario()
-- Queries por coleta: 0 (usado em relatórios)
-- Melhoria esperada: 10-20ms em relatórios

CREATE INDEX IF NOT EXISTS idx_coleta_inventario 
ON tabela_coleta(id_inventario, data_coleta DESC);

COMMENT ON INDEX idx_coleta_inventario IS 
'Índice para busca rápida de coletas por inventário, ordenadas por data. Melhora relatórios.';

-- ============================================================================
-- 5. ÍNDICE IMPORTANTE: Busca de Coletas por Coletor (Impacto: -5%)
-- ============================================================================
-- Usado em: ColetaDAO.buscarPorColetor()
-- Queries por coleta: 0 (usado em relatórios)
-- Melhoria esperada: 10-20ms em relatórios

CREATE INDEX IF NOT EXISTS idx_coleta_coletor 
ON tabela_coleta(id_coletor, data_coleta DESC);

COMMENT ON INDEX idx_coleta_coletor IS 
'Índice para busca rápida de coletas por coletor. Melhora relatórios de desempenho.';

-- ============================================================================
-- 6. ÍNDICE IMPORTANTE: Busca de Patrimônios por Número (Impacto: -5%)
-- ============================================================================
-- Usado em: PatrimonioDAO.buscarPorNumero()
-- Queries por coleta: 1
-- Melhoria esperada: 10-20ms por coleta

CREATE INDEX IF NOT EXISTS idx_patrimonio_numero 
ON tabela_patrimonio(numero);

COMMENT ON INDEX idx_patrimonio_numero IS 
'Índice para busca rápida de patrimônios por número. Essencial para coleta por QR code.';

-- ============================================================================
-- 7. ÍNDICE IMPORTANTE: Busca de Inventário por Status (Impacto: -5%)
-- ============================================================================
-- Usado em: InventarioDAO.buscarPorStatus()
-- Queries por coleta: 1 (com cache, 0 após primeira coleta)
-- Melhoria esperada: 10-20ms por coleta

CREATE INDEX IF NOT EXISTS idx_inventario_status 
ON tabela_inventario(status);

COMMENT ON INDEX idx_inventario_status IS 
'Índice para busca rápida de inventários por status. Melhora busca de inventário ativo.';

-- ============================================================================
-- VERIFICAÇÃO: Listar todos os índices criados
-- ============================================================================

-- PostgreSQL: Listar índices da tabela de coleta
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN ('tabela_coleta', 'tabela_participante_inventario', 'tabela_patrimonio', 'tabela_inventario')
ORDER BY tablename, indexname;

-- ============================================================================
-- ANÁLISE: Verificar tamanho dos índices
-- ============================================================================

SELECT 
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as tamanho_indice
FROM pg_indexes
JOIN pg_class ON pg_class.relname = indexname
WHERE tablename IN ('tabela_coleta', 'tabela_participante_inventario', 'tabela_patrimonio', 'tabela_inventario')
ORDER BY pg_relation_size(indexrelid) DESC;

-- ============================================================================
-- ESTATÍSTICAS: Impacto esperado
-- ============================================================================
-- 
-- Antes (sem índices):
--   - Coleta 1: ~500ms
--   - Coleta 5: ~1.5s
--   - Coleta 10: ~3s
--   - Coleta 20: ~8s
--
-- Depois (com índices + cache):
--   - Coleta 1: ~300ms (-40%)
--   - Coleta 5: ~400ms (-73%)
--   - Coleta 10: ~500ms (-83%)
--   - Coleta 20: ~600ms (-92%)
--
-- Ganho Total: 92% de melhoria em performance
--
-- ============================================================================

-- ============================================================================
-- MANUTENÇÃO: Reindexar periodicamente
-- ============================================================================
-- Execute mensalmente para manter índices otimizados:
--
-- REINDEX INDEX idx_participante_inventario_usuario;
-- REINDEX INDEX idx_coleta_inventario_patrimonio;
-- REINDEX INDEX idx_coleta_patrimonio_sala;
-- REINDEX INDEX idx_coleta_inventario;
-- REINDEX INDEX idx_coleta_coletor;
-- REINDEX INDEX idx_patrimonio_numero;
-- REINDEX INDEX idx_inventario_status;
--
-- ============================================================================
