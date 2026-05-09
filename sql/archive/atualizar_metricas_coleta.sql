-- Script para popular dados de métricas nas coletas existentes
-- Execute este script para ter dados no Analytics

-- Atualizar coletas existentes com dados de métricas simulados
UPDATE tabela_coleta 
SET tempo_coleta_segundos = FLOOR(RANDOM() * 60 + 10)::int,
    tempo_scan_segundos = FLOOR(RANDOM() * 10 + 2)::int,
    tempo_preenchimento_segundos = FLOOR(RANDOM() * 30 + 5)::int,
    metodo_coleta = 'MANUAL',
    hora_coleta = EXTRACT(HOUR FROM data_coleta)::int,
    dia_semana = EXTRACT(DOW FROM data_coleta)::int + 1,
    periodo_coleta = CASE 
        WHEN EXTRACT(HOUR FROM data_coleta) >= 6 AND EXTRACT(HOUR FROM data_coleta) < 12 THEN 'MANHA'
        WHEN EXTRACT(HOUR FROM data_coleta) >= 12 AND EXTRACT(HOUR FROM data_coleta) < 18 THEN 'TARDE'
        ELSE 'NOITE'
    END,
    tipo_scan = 'MANUAL',
    tentativas_scan = 1,
    erros_scan = 0,
    qualidade_etiqueta = 'BOA'
WHERE tempo_coleta_segundos IS NULL;

-- Verificar resultado
SELECT 
    COUNT(*) as total,
    COUNT(tempo_coleta_segundos) as com_metricas,
    AVG(tempo_coleta_segundos) as tempo_medio
FROM tabela_coleta;
