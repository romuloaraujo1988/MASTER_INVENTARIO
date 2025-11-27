-- ============================================================
-- Script para inserir dados de teste de itens compostos
-- SEM destruir dados existentes (usa INSERT com verificação)
-- ============================================================

-- Verificar se as tabelas existem antes de inserir
DO $$
BEGIN
    -- Verificar se tabela de componentes existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_componente_patrimonio') THEN
        RAISE NOTICE 'Tabela tabela_componente_patrimonio não existe. Execute o script de criação primeiro.';
        RETURN;
    END IF;
END $$;

-- ============================================================
-- 1. INSERIR TIPOS DE COMPONENTES (se não existirem)
-- ============================================================
INSERT INTO tabela_tipo_componente (nome, descricao, ativo)
SELECT 'MESA', 'Mesa/Tampo do conjunto', true
WHERE NOT EXISTS (SELECT 1 FROM tabela_tipo_componente WHERE nome = 'MESA');

INSERT INTO tabela_tipo_componente (nome, descricao, ativo)
SELECT 'CADEIRA', 'Cadeira do conjunto', true
WHERE NOT EXISTS (SELECT 1 FROM tabela_tipo_componente WHERE nome = 'CADEIRA');

INSERT INTO tabela_tipo_componente (nome, descricao, ativo)
SELECT 'MONITOR', 'Monitor de computador', true
WHERE NOT EXISTS (SELECT 1 FROM tabela_tipo_componente WHERE nome = 'MONITOR');

INSERT INTO tabela_tipo_componente (nome, descricao, ativo)
SELECT 'TECLADO', 'Teclado de computador', true
WHERE NOT EXISTS (SELECT 1 FROM tabela_tipo_componente WHERE nome = 'TECLADO');

INSERT INTO tabela_tipo_componente (nome, descricao, ativo)
SELECT 'MOUSE', 'Mouse de computador', true
WHERE NOT EXISTS (SELECT 1 FROM tabela_tipo_componente WHERE nome = 'MOUSE');

INSERT INTO tabela_tipo_componente (nome, descricao, ativo)
SELECT 'CPU', 'Gabinete/CPU do computador', true
WHERE NOT EXISTS (SELECT 1 FROM tabela_tipo_componente WHERE nome = 'CPU');

INSERT INTO tabela_tipo_componente (nome, descricao, ativo)
SELECT 'ESTABILIZADOR', 'Estabilizador de tensão', true
WHERE NOT EXISTS (SELECT 1 FROM tabela_tipo_componente WHERE nome = 'ESTABILIZADOR');

INSERT INTO tabela_tipo_componente (nome, descricao, ativo)
SELECT 'NOBREAK', 'Nobreak/UPS', true
WHERE NOT EXISTS (SELECT 1 FROM tabela_tipo_componente WHERE nome = 'NOBREAK');

-- ============================================================
-- 2. MARCAR PATRIMÔNIOS COMO ITENS COMPOSTOS
-- (Apenas patrimônios que contenham palavras-chave na descrição)
-- ============================================================

-- Marcar conjuntos escolares como itens compostos
UPDATE tabela_patrimonio 
SET item_composto = true
WHERE UPPER(descricao) LIKE '%CONJUNTO ESCOLAR%'
  AND (item_composto IS NULL OR item_composto = false);

-- Marcar estações de trabalho como itens compostos
UPDATE tabela_patrimonio 
SET item_composto = true
WHERE UPPER(descricao) LIKE '%ESTAÇÃO DE TRABALHO%'
  AND (item_composto IS NULL OR item_composto = false);

-- Marcar microcomputadores como itens compostos
UPDATE tabela_patrimonio 
SET item_composto = true
WHERE UPPER(descricao) LIKE '%MICROCOMPUTADOR%'
  AND (item_composto IS NULL OR item_composto = false);

-- Marcar computadores como itens compostos
UPDATE tabela_patrimonio 
SET item_composto = true
WHERE UPPER(descricao) LIKE '%COMPUTADOR%'
  AND (item_composto IS NULL OR item_composto = false);

-- ============================================================
-- 3. INSERIR COMPONENTES PARA CONJUNTOS ESCOLARES
-- (Mesa + Cadeira para cada conjunto)
-- ============================================================

-- Inserir componente MESA para conjuntos escolares que ainda não têm
INSERT INTO tabela_componente_patrimonio (id_patrimonio, id_tipo_componente, descricao, quantidade, ativo)
SELECT 
    p.id,
    (SELECT id FROM tabela_tipo_componente WHERE nome = 'MESA' LIMIT 1),
    'Mesa escolar',
    1,
    true
FROM tabela_patrimonio p
WHERE UPPER(p.descricao) LIKE '%CONJUNTO ESCOLAR%'
  AND p.item_composto = true
  AND NOT EXISTS (
      SELECT 1 FROM tabela_componente_patrimonio cp 
      WHERE cp.id_patrimonio = p.id 
        AND cp.id_tipo_componente = (SELECT id FROM tabela_tipo_componente WHERE nome = 'MESA' LIMIT 1)
  );

-- Inserir componente CADEIRA para conjuntos escolares que ainda não têm
INSERT INTO tabela_componente_patrimonio (id_patrimonio, id_tipo_componente, descricao, quantidade, ativo)
SELECT 
    p.id,
    (SELECT id FROM tabela_tipo_componente WHERE nome = 'CADEIRA' LIMIT 1),
    'Cadeira escolar',
    1,
    true
FROM tabela_patrimonio p
WHERE UPPER(p.descricao) LIKE '%CONJUNTO ESCOLAR%'
  AND p.item_composto = true
  AND NOT EXISTS (
      SELECT 1 FROM tabela_componente_patrimonio cp 
      WHERE cp.id_patrimonio = p.id 
        AND cp.id_tipo_componente = (SELECT id FROM tabela_tipo_componente WHERE nome = 'CADEIRA' LIMIT 1)
  );

-- ============================================================
-- 4. INSERIR COMPONENTES PARA COMPUTADORES
-- (CPU + Monitor + Teclado + Mouse)
-- ============================================================

-- Inserir componente CPU para computadores que ainda não têm
INSERT INTO tabela_componente_patrimonio (id_patrimonio, id_tipo_componente, descricao, quantidade, ativo)
SELECT 
    p.id,
    (SELECT id FROM tabela_tipo_componente WHERE nome = 'CPU' LIMIT 1),
    'Gabinete/CPU',
    1,
    true
FROM tabela_patrimonio p
WHERE (UPPER(p.descricao) LIKE '%MICROCOMPUTADOR%' OR UPPER(p.descricao) LIKE '%COMPUTADOR%' OR UPPER(p.descricao) LIKE '%ESTAÇÃO DE TRABALHO%')
  AND p.item_composto = true
  AND NOT EXISTS (
      SELECT 1 FROM tabela_componente_patrimonio cp 
      WHERE cp.id_patrimonio = p.id 
        AND cp.id_tipo_componente = (SELECT id FROM tabela_tipo_componente WHERE nome = 'CPU' LIMIT 1)
  );

-- Inserir componente MONITOR para computadores que ainda não têm
INSERT INTO tabela_componente_patrimonio (id_patrimonio, id_tipo_componente, descricao, quantidade, ativo)
SELECT 
    p.id,
    (SELECT id FROM tabela_tipo_componente WHERE nome = 'MONITOR' LIMIT 1),
    'Monitor',
    1,
    true
FROM tabela_patrimonio p
WHERE (UPPER(p.descricao) LIKE '%MICROCOMPUTADOR%' OR UPPER(p.descricao) LIKE '%COMPUTADOR%' OR UPPER(p.descricao) LIKE '%ESTAÇÃO DE TRABALHO%')
  AND p.item_composto = true
  AND NOT EXISTS (
      SELECT 1 FROM tabela_componente_patrimonio cp 
      WHERE cp.id_patrimonio = p.id 
        AND cp.id_tipo_componente = (SELECT id FROM tabela_tipo_componente WHERE nome = 'MONITOR' LIMIT 1)
  );

-- Inserir componente TECLADO para computadores que ainda não têm
INSERT INTO tabela_componente_patrimonio (id_patrimonio, id_tipo_componente, descricao, quantidade, ativo)
SELECT 
    p.id,
    (SELECT id FROM tabela_tipo_componente WHERE nome = 'TECLADO' LIMIT 1),
    'Teclado',
    1,
    true
FROM tabela_patrimonio p
WHERE (UPPER(p.descricao) LIKE '%MICROCOMPUTADOR%' OR UPPER(p.descricao) LIKE '%COMPUTADOR%' OR UPPER(p.descricao) LIKE '%ESTAÇÃO DE TRABALHO%')
  AND p.item_composto = true
  AND NOT EXISTS (
      SELECT 1 FROM tabela_componente_patrimonio cp 
      WHERE cp.id_patrimonio = p.id 
        AND cp.id_tipo_componente = (SELECT id FROM tabela_tipo_componente WHERE nome = 'TECLADO' LIMIT 1)
  );

-- Inserir componente MOUSE para computadores que ainda não têm
INSERT INTO tabela_componente_patrimonio (id_patrimonio, id_tipo_componente, descricao, quantidade, ativo)
SELECT 
    p.id,
    (SELECT id FROM tabela_tipo_componente WHERE nome = 'MOUSE' LIMIT 1),
    'Mouse',
    1,
    true
FROM tabela_patrimonio p
WHERE (UPPER(p.descricao) LIKE '%MICROCOMPUTADOR%' OR UPPER(p.descricao) LIKE '%COMPUTADOR%' OR UPPER(p.descricao) LIKE '%ESTAÇÃO DE TRABALHO%')
  AND p.item_composto = true
  AND NOT EXISTS (
      SELECT 1 FROM tabela_componente_patrimonio cp 
      WHERE cp.id_patrimonio = p.id 
        AND cp.id_tipo_componente = (SELECT id FROM tabela_tipo_componente WHERE nome = 'MOUSE' LIMIT 1)
  );

-- ============================================================
-- 5. RELATÓRIO DE VERIFICAÇÃO
-- ============================================================

-- Mostrar quantos itens compostos foram criados/atualizados
SELECT 
    'Itens Compostos' as tipo,
    COUNT(*) as total
FROM tabela_patrimonio 
WHERE item_composto = true;

-- Mostrar quantos componentes existem por tipo
SELECT 
    tc.nome as tipo_componente,
    COUNT(cp.id) as total_componentes
FROM tabela_tipo_componente tc
LEFT JOIN tabela_componente_patrimonio cp ON tc.id = cp.id_tipo_componente
GROUP BY tc.nome
ORDER BY total_componentes DESC;

-- Mostrar os 10 primeiros itens compostos com seus componentes
SELECT 
    p.numero as patrimonio,
    LEFT(p.descricao, 50) as descricao,
    COUNT(cp.id) as qtd_componentes
FROM tabela_patrimonio p
LEFT JOIN tabela_componente_patrimonio cp ON p.id = cp.id_patrimonio
WHERE p.item_composto = true
GROUP BY p.id, p.numero, p.descricao
ORDER BY p.numero
LIMIT 10;

-- ============================================================
-- FIM DO SCRIPT
-- ============================================================
