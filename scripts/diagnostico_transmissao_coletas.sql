-- Diagnóstico de transmissão offline → servidor (bugs F2/F3/F6/F7/F12/R8)
-- Executado em 07/05/2026

-- Q0: Quantidade total de coletas (baseline)
\echo '=== Q0: Baseline total ==='
SELECT COUNT(*) AS total_coletas FROM tabela_coleta;

-- Q1: Coletas com ID_INVENTARIO NULL ou 0 (F2)
\echo ''
\echo '=== Q1: Coletas com ID_INVENTARIO inválido (F2) ==='
SELECT
  COALESCE(id_inventario::text, 'NULL') AS id_inventario,
  COUNT(*) AS qtd
FROM tabela_coleta
WHERE id_inventario IS NULL OR id_inventario = 0
GROUP BY id_inventario
ORDER BY qtd DESC;

-- Q2: Divergência — se todos forem false/NULL, confirma F3
\echo ''
\echo '=== Q2: Distribuição de DIVERGENCIA e MOTIVO_DIVERGENCIA (F3) ==='
SELECT
  divergencia,
  COALESCE(motivo_divergencia, '(NULL)') AS motivo_divergencia,
  COUNT(*) AS qtd
FROM tabela_coleta
WHERE data_coleta >= NOW() - INTERVAL '90 days'
GROUP BY divergencia, motivo_divergencia
ORDER BY qtd DESC;

-- Q3: Itens sem etiqueta com/sem foto (F6/R8)
\echo ''
\echo '=== Q3: Itens sem etiqueta com/sem foto (F6 + R8) ==='
SELECT
  sem_etiqueta,
  COUNT(*) AS total,
  COUNT(foto_patrimonio) AS com_algum_foto,
  COUNT(foto_patrimonio) FILTER (WHERE foto_patrimonio LIKE '/%' OR foto_patrimonio LIKE 'storage%' OR foto_patrimonio LIKE '%/storage/%') AS fotos_caminho_local,
  COUNT(foto_patrimonio) FILTER (WHERE foto_patrimonio LIKE 'data:%' OR LENGTH(foto_patrimonio) > 500) AS fotos_base64_prov
FROM tabela_coleta
GROUP BY sem_etiqueta
ORDER BY sem_etiqueta DESC;

-- Q4: Amostra de FOTO_PATRIMONIO (primeiros 100 caracteres)
\echo ''
\echo '=== Q4: Amostra de FOTO_PATRIMONIO (R8) ==='
SELECT
  id,
  sem_etiqueta,
  LENGTH(foto_patrimonio) AS tamanho,
  LEFT(foto_patrimonio, 120) AS preview
FROM tabela_coleta
WHERE foto_patrimonio IS NOT NULL
ORDER BY id DESC
LIMIT 10;

-- Q5: Coletas duplicadas no mesmo (inventario, patrimonio) (F7/F11)
\echo ''
\echo '=== Q5: Duplicatas (F7 + F11) ==='
SELECT
  id_inventario,
  id_patrimonio,
  COUNT(*) AS duplicatas,
  STRING_AGG(id::text, ', ' ORDER BY id) AS ids
FROM tabela_coleta
WHERE id_patrimonio IS NOT NULL
GROUP BY id_inventario, id_patrimonio
HAVING COUNT(*) > 1
ORDER BY duplicatas DESC
LIMIT 20;

\echo ''
\echo '=== Q5b: Total de duplicatas ==='
SELECT COUNT(*) FROM (
  SELECT id_inventario, id_patrimonio
  FROM tabela_coleta
  WHERE id_patrimonio IS NOT NULL
  GROUP BY id_inventario, id_patrimonio
  HAVING COUNT(*) > 1
) dupes;

-- Q6: Métricas de scan perdidas (F12)
\echo ''
\echo '=== Q6: Métricas de scan (F12) ==='
SELECT
  COUNT(*) AS total,
  COUNT(*) FILTER (WHERE tentativas_scan IS NOT NULL AND tentativas_scan > 0) AS com_tentativas_scan,
  COUNT(*) FILTER (WHERE erros_scan IS NOT NULL AND erros_scan > 0) AS com_erros_scan,
  COUNT(*) FILTER (WHERE qualidade_etiqueta IS NOT NULL) AS com_qualidade_etiqueta,
  COUNT(*) FILTER (WHERE metodo_coleta IS NOT NULL) AS com_metodo_coleta,
  COUNT(*) FILTER (WHERE tipo_scan IS NOT NULL) AS com_tipo_scan,
  COUNT(*) FILTER (WHERE tempo_coleta_segundos IS NOT NULL) AS com_tempo_coleta
FROM tabela_coleta
WHERE data_coleta >= NOW() - INTERVAL '30 days';

-- Q7: Distribuição de ESTADO_ENCONTRADO — se "BOM" dominar absurdamente, sintoma de fallback
\echo ''
\echo '=== Q7: Distribuição de ESTADO_ENCONTRADO (suspeita: fallback "BOM") ==='
SELECT
  COALESCE(estado_encontrado, '(NULL)') AS estado_encontrado,
  COUNT(*) AS qtd,
  ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER (), 2) AS pct
FROM tabela_coleta
WHERE data_coleta >= NOW() - INTERVAL '30 days'
GROUP BY estado_encontrado
ORDER BY qtd DESC;

-- Q8: Constraints da tabela_coleta (para validar se existe UNIQUE ou FK)
\echo ''
\echo '=== Q8: Constraints da TABELA_COLETA ==='
SELECT
  con.conname AS constraint_name,
  con.contype AS tipo,
  pg_get_constraintdef(con.oid) AS definicao
FROM pg_constraint con
JOIN pg_class rel ON rel.oid = con.conrelid
WHERE rel.relname = 'tabela_coleta'
ORDER BY con.contype, con.conname;

-- Q9: Coletas sem ID_PATRIMONIO mas SEM_ETIQUETA=false (coletas quebradas)
\echo ''
\echo '=== Q9: Coletas sem id_patrimonio e sem flag sem_etiqueta ==='
SELECT COUNT(*) AS quebradas
FROM tabela_coleta
WHERE id_patrimonio IS NULL AND (sem_etiqueta IS NULL OR sem_etiqueta = false);

-- Q10: Coletas sem etiqueta por mês
\echo ''
\echo '=== Q10: Tendência de coletas sem etiqueta (últimos 90 dias) ==='
SELECT
  DATE_TRUNC('month', data_coleta) AS mes,
  COUNT(*) FILTER (WHERE sem_etiqueta = true) AS sem_etiqueta,
  COUNT(*) FILTER (WHERE sem_etiqueta = false OR sem_etiqueta IS NULL) AS com_etiqueta,
  COUNT(*) FILTER (WHERE sem_etiqueta = true AND foto_patrimonio IS NOT NULL) AS sem_etiq_com_foto
FROM tabela_coleta
WHERE data_coleta >= NOW() - INTERVAL '90 days'
GROUP BY mes
ORDER BY mes DESC;
