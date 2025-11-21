-- =====================================================
-- SCRIPT DE TESTE: Relatório de Itens Não Encontrados
-- Sistema de Inventário IFMT
-- Data: 18/11/2025
-- =====================================================

-- Este script testa a query do relatório de itens não encontrados
-- Pode ser executado diretamente no PostgreSQL para validação

-- =====================================================
-- TESTE 1: Query Principal (Substitua ? pelo ID do inventário)
-- =====================================================

-- IMPORTANTE: Substitua o valor 1 pelo ID do inventário ativo
\set inventario_id 1

SELECT 
    p.NUMERO as "Número Patrimônio",
    p.DESCRICAO as "Descrição",
    COALESCE(p.MARCA, '') as "marca",
    COALESCE(p.MODELO, '') as "modelo",
    COALESCE(r.NOME, 'Sem Responsável') as "Responsável",
    COALESCE(s.NOME, 'Sem Setor') as "Setor",
    COALESCE(sa.NUMERO_SALA, 'N/A') as "Sala",
    COALESCE(sa.DESCRICAO, '') as "Localização Cadastrada",
    'NÃO ENCONTRADO' as "Estado",
    'Não coletado no inventário' as "Situação",
    COALESCE(p.VALOR_AQUISICAO, 0) as "Valor",
    p.STATUS as "status"
FROM TABELA_PATRIMONIO p
LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
LEFT JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID
WHERE p.STATUS = 'ATIVO'
  AND p.ID NOT IN (
      SELECT c.ID_PATRIMONIO 
      FROM TABELA_COLETA c 
      WHERE c.ID_INVENTARIO = :inventario_id
        AND c.STATUS_COLETA IN ('COLETADO', 'ENCONTRADO')
  )
ORDER BY 
    COALESCE(s.NOME, 'Sem Setor'),
    COALESCE(r.NOME, 'Sem Responsável'),
    p.NUMERO;

-- =====================================================
-- TESTE 2: Estatísticas Rápidas
-- =====================================================

-- Total de patrimônios ativos
SELECT 
    'Total de Patrimônios Ativos' as "Métrica",
    COUNT(*) as "Quantidade"
FROM TABELA_PATRIMONIO 
WHERE STATUS = 'ATIVO';

-- Total de patrimônios coletados no inventário
SELECT 
    'Patrimônios Coletados' as "Métrica",
    COUNT(DISTINCT c.ID_PATRIMONIO) as "Quantidade"
FROM TABELA_COLETA c
WHERE c.ID_INVENTARIO = :inventario_id
  AND c.STATUS_COLETA IN ('COLETADO', 'ENCONTRADO');

-- Total de patrimônios NÃO coletados
SELECT 
    'Patrimônios NÃO Coletados' as "Métrica",
    COUNT(*) as "Quantidade"
FROM TABELA_PATRIMONIO p
WHERE p.STATUS = 'ATIVO'
  AND p.ID NOT IN (
      SELECT c.ID_PATRIMONIO 
      FROM TABELA_COLETA c 
      WHERE c.ID_INVENTARIO = :inventario_id
        AND c.STATUS_COLETA IN ('COLETADO', 'ENCONTRADO')
  );

-- =====================================================
-- TESTE 3: Análise por Setor
-- =====================================================

SELECT 
    COALESCE(s.NOME, 'Sem Setor') as "Setor",
    COUNT(*) as "Itens Não Encontrados",
    SUM(COALESCE(p.VALOR_AQUISICAO, 0)) as "Valor Total"
FROM TABELA_PATRIMONIO p
LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
WHERE p.STATUS = 'ATIVO'
  AND p.ID NOT IN (
      SELECT c.ID_PATRIMONIO 
      FROM TABELA_COLETA c 
      WHERE c.ID_INVENTARIO = :inventario_id
        AND c.STATUS_COLETA IN ('COLETADO', 'ENCONTRADO')
  )
GROUP BY s.NOME
ORDER BY COUNT(*) DESC;

-- =====================================================
-- TESTE 4: Análise por Responsável
-- =====================================================

SELECT 
    COALESCE(r.NOME, 'Sem Responsável') as "Responsável",
    COALESCE(s.NOME, 'Sem Setor') as "Setor",
    COUNT(*) as "Itens Não Encontrados",
    SUM(COALESCE(p.VALOR_AQUISICAO, 0)) as "Valor Total"
FROM TABELA_PATRIMONIO p
LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
WHERE p.STATUS = 'ATIVO'
  AND p.ID NOT IN (
      SELECT c.ID_PATRIMONIO 
      FROM TABELA_COLETA c 
      WHERE c.ID_INVENTARIO = :inventario_id
        AND c.STATUS_COLETA IN ('COLETADO', 'ENCONTRADO')
  )
GROUP BY r.NOME, s.NOME
ORDER BY COUNT(*) DESC
LIMIT 20;

-- =====================================================
-- TESTE 5: Verificar Performance da Query
-- =====================================================

EXPLAIN ANALYZE
SELECT 
    p.NUMERO,
    p.DESCRICAO,
    r.NOME as responsavel,
    s.NOME as setor
FROM TABELA_PATRIMONIO p
LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
WHERE p.STATUS = 'ATIVO'
  AND p.ID NOT IN (
      SELECT c.ID_PATRIMONIO 
      FROM TABELA_COLETA c 
      WHERE c.ID_INVENTARIO = :inventario_id
        AND c.STATUS_COLETA IN ('COLETADO', 'ENCONTRADO')
  )
LIMIT 10;

-- =====================================================
-- TESTE 6: Validar Índices (Recomendado)
-- =====================================================

-- Verificar se existem índices nas colunas usadas
SELECT 
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN ('tabela_patrimonio', 'tabela_coleta', 'tabela_responsavel', 'tabela_setor', 'tabela_sala')
ORDER BY tablename, indexname;

-- =====================================================
-- TESTE 7: Criar Índices se Necessário (Opcional)
-- =====================================================

-- Descomentar e executar se a performance estiver lenta

-- CREATE INDEX IF NOT EXISTS idx_patrimonio_status 
--     ON TABELA_PATRIMONIO(STATUS);

-- CREATE INDEX IF NOT EXISTS idx_patrimonio_responsavel 
--     ON TABELA_PATRIMONIO(ID_RESPONSAVEL);

-- CREATE INDEX IF NOT EXISTS idx_patrimonio_sala 
--     ON TABELA_PATRIMONIO(ID_SALA);

-- CREATE INDEX IF NOT EXISTS idx_coleta_inventario_patrimonio 
--     ON TABELA_COLETA(ID_INVENTARIO, ID_PATRIMONIO, STATUS_COLETA);

-- CREATE INDEX IF NOT EXISTS idx_responsavel_setor 
--     ON TABELA_RESPONSAVEL(ID_SETOR);

-- =====================================================
-- TESTE 8: Dados de Exemplo (Para Ambiente de Teste)
-- =====================================================

-- Descomentar para criar dados de teste

-- -- Criar inventário de teste
-- INSERT INTO TABELA_INVENTARIO (NOME, ANO, STATUS_INVENTARIO, DATA_INICIO)
-- VALUES ('Inventário Teste 2024', 2024, 'EM_ANDAMENTO', CURRENT_DATE)
-- ON CONFLICT DO NOTHING;

-- -- Simular algumas coletas
-- INSERT INTO TABELA_COLETA (ID_INVENTARIO, ID_PATRIMONIO, STATUS_COLETA, DATA_COLETA)
-- SELECT 
--     (SELECT ID FROM TABELA_INVENTARIO WHERE NOME = 'Inventário Teste 2024' LIMIT 1),
--     p.ID,
--     'COLETADO',
--     CURRENT_TIMESTAMP
-- FROM TABELA_PATRIMONIO p
-- WHERE p.STATUS = 'ATIVO'
-- LIMIT 100
-- ON CONFLICT DO NOTHING;

-- =====================================================
-- RESULTADO ESPERADO
-- =====================================================

/*
O relatório deve retornar:

1. Todos os patrimônios com STATUS = 'ATIVO'
2. Que NÃO foram coletados no inventário especificado
3. Com informações completas de responsável, setor e sala
4. Ordenados por setor → responsável → número

Exemplo de resultado:

 Número Patrimônio | Descrição          | Responsável  | Setor | Sala
-------------------+--------------------+--------------+-------+------
 00123             | Computador Desktop | João Silva   | TI    | 101
 00456             | Monitor LCD        | João Silva   | TI    | 101
 00789             | Mesa Escritório    | Maria Santos | Admin | 205

Se o resultado estiver vazio, significa que:
- Todos os patrimônios ativos foram coletados (✅ Excelente!)
- OU não há patrimônios ativos no sistema
- OU o ID do inventário está incorreto
*/

-- =====================================================
-- FIM DO SCRIPT DE TESTE
-- =====================================================
