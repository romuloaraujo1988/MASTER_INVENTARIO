-- ============================================================================
-- SCRIPT: Adicionar Índices de Performance para Sugestões de Descrição
-- ============================================================================
-- Feature: coleta-descricao-livre-com-sugestao
-- Objetivo: Suportar o endpoint GET /api/mobile/descricoes/sugestoes com p95
--           menor ou igual a 500ms em inventários de até 10.000 patrimônios.
-- Requisitos atendidos: 3.4, 5.4, 6.1, 6.4, 9.2, 9.3
--
-- ----------------------------------------------------------------------------
-- IMPORTANTE: Este script NÃO altera, cria, renomeia ou remove colunas.
-- Ele adiciona apenas índices auxiliares (e habilita a extensão 'unaccent'),
-- preservando integralmente o schema das tabelas TABELA_PATRIMONIO e
-- TABELA_COLETA (Requisitos 9.2 e 9.3).
--
-- Idempotência: todas as instruções usam CREATE ... IF NOT EXISTS e podem ser
-- executadas múltiplas vezes sem efeito colateral.
-- ----------------------------------------------------------------------------
--
-- Uso:
--   psql -h localhost -U inventario -d sispatrimonio \
--        -f sql/adicionar_indices_sugestoes_descricao.sql
--
-- Pré-requisito: usuário com privilégio para criar extensão 'unaccent'
-- (geralmente superuser ou owner do banco). A extensão já é padrão no
-- PostgreSQL 12+ e faz parte do pacote postgresql-contrib.
-- ============================================================================

-- ============================================================================
-- 0. EXTENSÃO: unaccent (filtro acento/caso-insensível)
-- ============================================================================
-- Usada em: PatrimonioDAO.buscarSugestoesNaoColetadasPaginado()
-- Propósito: remover diacríticos nas comparações de descrição, espelhando o
--            comportamento do filtro offline do app (Requisito 3.4, 5.4).

CREATE EXTENSION IF NOT EXISTS unaccent;

-- ============================================================================
-- 1. ÍNDICE FUNCIONAL: Filtro acento/caso-insensível sobre descrição
-- ============================================================================
-- Usado em: PatrimonioDAO.buscarSugestoesNaoColetadasPaginado()
-- Query alvo: WHERE unaccent(lower(p.DESCRICAO)) LIKE '%...%'
--             ORDER BY unaccent(lower(p.DESCRICAO)) ASC, p.NUMERO ASC
-- Requisitos: 3.4, 5.4, 6.4
-- Impacto: evita sequential scan em TABELA_PATRIMONIO ao filtrar por termo
--          normalizado e dá suporte à ordenação estável entre páginas.

CREATE INDEX IF NOT EXISTS idx_patrimonio_descricao_unaccent
    ON tabela_patrimonio (unaccent(lower(descricao)));

COMMENT ON INDEX idx_patrimonio_descricao_unaccent IS
    'Índice funcional para filtro acento/caso-insensível sobre descrição do patrimônio. '
    'Suporta o endpoint GET /api/mobile/descricoes/sugestoes (feature coleta-descricao-livre-com-sugestao).';

-- ============================================================================
-- 2. ÍNDICE COMPOSTO: NOT EXISTS de coleta por patrimônio + inventário
-- ============================================================================
-- Usado em: PatrimonioDAO.buscarSugestoesNaoColetadasPaginado()
-- Query alvo: NOT EXISTS (SELECT 1 FROM TABELA_COLETA c
--                          WHERE c.ID_PATRIMONIO = p.ID
--                            AND c.ID_INVENTARIO = ?)
-- Requisitos: 5.2, 6.1
-- Impacto: torna o anti-join determinístico e rápido. A ordem das colunas
--          (id_patrimonio, id_inventario) é intencional porque o predicado
--          correlacionado é resolvido por id_patrimonio primeiro.
-- Observação: existe um índice complementar em (id_inventario, id_patrimonio)
--            criado em criar_indices_performance_coleta.sql para a verificação
--            de duplicata em registro de coleta. Os dois se complementam e
--            NÃO conflitam.

CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio_inventario
    ON tabela_coleta (id_patrimonio, id_inventario);

COMMENT ON INDEX idx_coleta_patrimonio_inventario IS
    'Índice composto para NOT EXISTS (patrimônio não coletado em um inventário). '
    'Suporta o endpoint GET /api/mobile/descricoes/sugestoes (feature coleta-descricao-livre-com-sugestao).';

-- ============================================================================
-- 3. ÍNDICE SIMPLES: Filtro por status do patrimônio
-- ============================================================================
-- Usado em: PatrimonioDAO.buscarSugestoesNaoColetadasPaginado()
-- Query alvo: WHERE (p.STATUS IS NULL OR UPPER(p.STATUS) NOT IN ('BAIXADO', 'INATIVO'))
-- Requisitos: 6.1
-- Impacto: restringe a varredura para patrimônios ativos antes do anti-join.

CREATE INDEX IF NOT EXISTS idx_patrimonio_status
    ON tabela_patrimonio (status);

COMMENT ON INDEX idx_patrimonio_status IS
    'Índice para filtro por status do patrimônio. '
    'Suporta o endpoint GET /api/mobile/descricoes/sugestoes (feature coleta-descricao-livre-com-sugestao).';

-- ============================================================================
-- VERIFICAÇÃO: Listar índices criados por esta migração
-- ============================================================================

SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE indexname IN (
    'idx_patrimonio_descricao_unaccent',
    'idx_coleta_patrimonio_inventario',
    'idx_patrimonio_status'
)
ORDER BY tablename, indexname;

-- ============================================================================
-- VERIFICAÇÃO: Confirmar que a extensão unaccent está instalada
-- ============================================================================

SELECT extname, extversion
FROM pg_extension
WHERE extname = 'unaccent';

-- ============================================================================
-- ROLLBACK (manual, somente se necessário)
-- ============================================================================
-- Estes comandos NÃO são executados automaticamente. Use apenas em ambientes
-- de teste, pois remover os índices degradará o desempenho do endpoint de
-- sugestões.
--
-- DROP INDEX IF EXISTS idx_patrimonio_descricao_unaccent;
-- DROP INDEX IF EXISTS idx_coleta_patrimonio_inventario;
-- DROP INDEX IF EXISTS idx_patrimonio_status;
-- DROP EXTENSION IF EXISTS unaccent;
-- ============================================================================
