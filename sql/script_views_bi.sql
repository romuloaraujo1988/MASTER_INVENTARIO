-- =====================================================
-- SCRIPT DE CRIAÇÃO DE VIEWS PARA BUSINESS INTELLIGENCE
-- Sistema de Inventário IFMT
-- =====================================================

-- Remover views existentes se houver
DROP VIEW IF EXISTS vw_inventarios_bi CASCADE;
DROP VIEW IF EXISTS vw_performance_coleta CASCADE;
DROP VIEW IF EXISTS vw_distribuicao_patrimonial CASCADE;
DROP VIEW IF EXISTS vw_dashboard_kpis CASCADE;
DROP VIEW IF EXISTS vw_tendencias_temporais CASCADE;
DROP VIEW IF EXISTS vw_analise_setores CASCADE;

-- =====================================================
-- 1. VIEW PRINCIPAL DE INVENTÁRIOS COM MÉTRICAS
-- =====================================================
CREATE OR REPLACE VIEW vw_inventarios_bi AS
SELECT 
    i.id,
    i.nome,
    i.ano,
    i.data_inicio,
    i.data_fim,
    i.status_inventario,
    i.responsavel_inventario,
    i.total_patrimonios,
    i.patrimonios_coletados,
    i.percentual_conclusao,
    i.data_criacao,
    i.data_ultima_atualizacao,
    i.observacao,
    i.tipo_inventario,
    
    -- Métricas calculadas de status
    CASE 
        WHEN i.data_fim < CURRENT_DATE AND i.status_inventario NOT IN ('CONCLUIDO', 'CANCELADO') 
        THEN 'ATRASADO'
        WHEN i.data_inicio <= CURRENT_DATE AND i.data_fim >= CURRENT_DATE AND i.status_inventario = 'EM_ANDAMENTO'
        THEN 'NO_PRAZO'
        ELSE i.status_inventario
    END as status_real,
    
    -- Métricas de tempo
    CASE 
        WHEN i.data_fim IS NOT NULL 
        THEN i.data_fim - i.data_inicio 
        ELSE NULL
    END as duracao_planejada_dias,
    
    CASE 
        WHEN i.status_inventario = 'CONCLUIDO' AND i.data_ultima_atualizacao IS NOT NULL 
        THEN i.data_ultima_atualizacao::date - i.data_inicio
        WHEN i.status_inventario = 'EM_ANDAMENTO'
        THEN CURRENT_DATE - i.data_inicio
        ELSE NULL
    END as duracao_atual_dias,
    
    -- Indicadores de performance
    CASE 
        WHEN i.total_patrimonios > 0 
        THEN ROUND((i.patrimonios_coletados::decimal / i.total_patrimonios * 100), 2)
        ELSE 0
    END as percentual_conclusao_calculado,
    
    -- Categorização por tamanho
    CASE 
        WHEN i.total_patrimonios <= 100 THEN 'PEQUENO'
        WHEN i.total_patrimonios <= 500 THEN 'MEDIO'
        WHEN i.total_patrimonios <= 1000 THEN 'GRANDE'
        ELSE 'MUITO_GRANDE'
    END as categoria_tamanho,
    
    -- Indicadores de prazo
    CASE 
        WHEN i.data_fim IS NOT NULL THEN
            CASE 
                WHEN CURRENT_DATE > i.data_fim THEN 'VENCIDO'
                WHEN CURRENT_DATE > (i.data_fim - INTERVAL '7 days') THEN 'CRITICO'
                WHEN CURRENT_DATE > (i.data_fim - INTERVAL '15 days') THEN 'ATENCAO'
                ELSE 'OK'
            END
        ELSE 'SEM_PRAZO'
    END as indicador_prazo,
    
    -- Velocidade de coleta (itens por dia)
    CASE 
        WHEN i.status_inventario = 'EM_ANDAMENTO' AND (CURRENT_DATE - i.data_inicio) > 0
        THEN ROUND(i.patrimonios_coletados::decimal / (CURRENT_DATE - i.data_inicio), 2)
        WHEN i.status_inventario = 'CONCLUIDO' AND i.data_ultima_atualizacao IS NOT NULL
        THEN ROUND(i.patrimonios_coletados::decimal / GREATEST((i.data_ultima_atualizacao::date - i.data_inicio), 1), 2)
        ELSE 0
    END as velocidade_coleta_dia,
    
    -- Estimativa de conclusão
    CASE 
        WHEN i.status_inventario = 'EM_ANDAMENTO' AND i.patrimonios_coletados > 0 AND (CURRENT_DATE - i.data_inicio) > 0
        THEN i.data_inicio + 
             INTERVAL '1 day' * 
             ROUND((i.total_patrimonios::decimal / (i.patrimonios_coletados::decimal / (CURRENT_DATE - i.data_inicio))))
        ELSE NULL
    END as estimativa_conclusao
    
FROM tabela_inventario i;

-- =====================================================
-- 2. VIEW DE PERFORMANCE DE COLETA
-- =====================================================
CREATE OR REPLACE VIEW vw_performance_coleta AS
SELECT 
    c.id_coletor,
    col.nome as nome_coletor,
    c.id_inventario,
    i.nome as nome_inventario,
    i.status_inventario,
    
    -- Contadores básicos
    COUNT(*) as total_coletas,
    COUNT(DISTINCT c.id_patrimonio) as patrimonios_unicos,
    COUNT(DISTINCT DATE(c.data_coleta)) as dias_trabalhados,
    
    -- Datas
    MIN(c.data_coleta) as primeira_coleta,
    MAX(c.data_coleta) as ultima_coleta,
    
    -- Métricas de tempo
    EXTRACT(EPOCH FROM (MAX(c.data_coleta) - MIN(c.data_coleta))) / 3600 as horas_periodo,
    
    -- Produtividade
    ROUND(
        COUNT(DISTINCT c.id_patrimonio)::decimal / 
        GREATEST(COUNT(DISTINCT DATE(c.data_coleta)), 1),
        2
    ) as patrimonios_por_dia,
    
    ROUND(
        COUNT(*)::decimal / 
        GREATEST(EXTRACT(EPOCH FROM (MAX(c.data_coleta) - MIN(c.data_coleta))) / 3600, 1),
        2
    ) as coletas_por_hora,
    
    -- Status das coletas
    COUNT(CASE WHEN c.status_coleta = 'COLETADO' THEN 1 END) as coletas_sucesso,
    COUNT(CASE WHEN c.status_coleta = 'NAO_ENCONTRADO' THEN 1 END) as nao_encontrados,
    COUNT(CASE WHEN c.status_coleta = 'DIVERGENCIA' THEN 1 END) as divergencias,
    COUNT(CASE WHEN c.status_coleta = 'PENDENTE' THEN 1 END) as pendentes,
    
    -- Taxas de performance
    ROUND(
        COUNT(CASE WHEN c.status_coleta = 'COLETADO' THEN 1 END)::decimal / 
        GREATEST(COUNT(*), 1) * 100, 
        2
    ) as taxa_sucesso_percent,
    
    ROUND(
        COUNT(CASE WHEN c.status_coleta = 'NAO_ENCONTRADO' THEN 1 END)::decimal / 
        GREATEST(COUNT(*), 1) * 100, 
        2
    ) as taxa_nao_encontrado_percent,
    
    ROUND(
        COUNT(CASE WHEN c.status_coleta = 'DIVERGENCIA' THEN 1 END)::decimal / 
        GREATEST(COUNT(*), 1) * 100, 
        2
    ) as taxa_divergencia_percent,
    
    -- Classificação de performance
    CASE 
        WHEN COUNT(CASE WHEN c.status_coleta = 'COLETADO' THEN 1 END)::decimal / GREATEST(COUNT(*), 1) >= 0.95 THEN 'EXCELENTE'
        WHEN COUNT(CASE WHEN c.status_coleta = 'COLETADO' THEN 1 END)::decimal / GREATEST(COUNT(*), 1) >= 0.85 THEN 'BOM'
        WHEN COUNT(CASE WHEN c.status_coleta = 'COLETADO' THEN 1 END)::decimal / GREATEST(COUNT(*), 1) >= 0.70 THEN 'REGULAR'
        ELSE 'BAIXO'
    END as classificacao_performance
    
FROM tabela_coleta c
JOIN tabela_coletor col ON c.id_coletor = col.id
JOIN tabela_inventario i ON c.id_inventario = i.id
GROUP BY c.id_coletor, col.nome, c.id_inventario, i.nome, i.status_inventario;

-- =====================================================
-- 3. VIEW DE DISTRIBUIÇÃO PATRIMONIAL
-- =====================================================
CREATE OR REPLACE VIEW vw_distribuicao_patrimonial AS
SELECT 
    s.id as id_setor,
    s.nome as nome_setor,
    s.sigla as sigla_setor,
    sa.id_sala,
    sa.descricao as nome_sala,
    sa.numero_sala,
    sa.bloco,
    sa.andar,
    sa.tipo_sala,
    
    -- Contadores de patrimônio
    COUNT(p.id) as total_patrimonios,
    COUNT(CASE WHEN p.ativo = true THEN 1 END) as patrimonios_ativos,
    COUNT(CASE WHEN p.ativo = false THEN 1 END) as patrimonios_inativos,
    
    -- Valores financeiros
    COALESCE(SUM(p.valor_aquisicao), 0) as valor_total,
    COALESCE(AVG(p.valor_aquisicao), 0) as valor_medio,
    COALESCE(MIN(p.valor_aquisicao), 0) as valor_minimo,
    COALESCE(MAX(p.valor_aquisicao), 0) as valor_maximo,
    
    -- Estados de conservação
    COUNT(CASE WHEN p.estado_conservacao = 'BOM' THEN 1 END) as estado_bom,
    COUNT(CASE WHEN p.estado_conservacao = 'REGULAR' THEN 1 END) as estado_regular,
    COUNT(CASE WHEN p.estado_conservacao = 'RUIM' THEN 1 END) as estado_ruim,
    COUNT(CASE WHEN p.estado_conservacao IS NULL THEN 1 END) as estado_nao_informado,
    
    -- Percentuais de conservação
    ROUND(
        COUNT(CASE WHEN p.estado_conservacao = 'BOM' THEN 1 END)::decimal / 
        GREATEST(COUNT(p.id), 1) * 100, 2
    ) as percentual_bom_estado,
    
    -- Densidade patrimonial
    ROUND(
        COUNT(p.id)::decimal / GREATEST(COUNT(DISTINCT sa.id_sala), 1), 2
    ) as densidade_patrimonial,
    
    -- Valor por metro quadrado (assumindo área média de 20m²)
    ROUND(
        COALESCE(SUM(p.valor_aquisicao), 0) / GREATEST(COUNT(DISTINCT sa.id_sala) * 20, 1), 2
    ) as valor_por_m2_estimado,
    
    -- Categorização do setor por valor
    CASE 
        WHEN COALESCE(SUM(p.valor_aquisicao), 0) >= 1000000 THEN 'ALTO_VALOR'
        WHEN COALESCE(SUM(p.valor_aquisicao), 0) >= 500000 THEN 'MEDIO_VALOR'
        WHEN COALESCE(SUM(p.valor_aquisicao), 0) >= 100000 THEN 'BAIXO_VALOR'
        ELSE 'MUITO_BAIXO_VALOR'
    END as categoria_valor,
    
    -- Indicador de risco (baseado em estado de conservação)
    CASE 
        WHEN COUNT(CASE WHEN p.estado_conservacao = 'RUIM' THEN 1 END)::decimal / GREATEST(COUNT(p.id), 1) > 0.3 THEN 'ALTO_RISCO'
        WHEN COUNT(CASE WHEN p.estado_conservacao = 'RUIM' THEN 1 END)::decimal / GREATEST(COUNT(p.id), 1) > 0.1 THEN 'MEDIO_RISCO'
        ELSE 'BAIXO_RISCO'
    END as indicador_risco
    
FROM tabela_setor s
LEFT JOIN tabela_sala sa ON s.id = sa.id_setor AND sa.ativo = true
LEFT JOIN tabela_patrimonio p ON sa.id_sala = p.id_sala
WHERE s.ativo = true
GROUP BY s.id, s.nome, s.sigla, sa.id_sala, sa.descricao, sa.numero_sala, sa.bloco, sa.andar, sa.tipo_sala;

-- =====================================================
-- 4. VIEW DE KPIs PARA DASHBOARD
-- =====================================================
CREATE OR REPLACE VIEW vw_dashboard_kpis AS
SELECT 
    -- KPIs de Inventários
    COUNT(*) as total_inventarios,
    COUNT(CASE WHEN status_inventario = 'CONCLUIDO' THEN 1 END) as inventarios_concluidos,
    COUNT(CASE WHEN status_inventario = 'EM_ANDAMENTO' THEN 1 END) as inventarios_em_andamento,
    COUNT(CASE WHEN status_inventario = 'PLANEJADO' THEN 1 END) as inventarios_planejados,
    COUNT(CASE WHEN status_real = 'ATRASADO' THEN 1 END) as inventarios_atrasados,
    
    -- Percentuais
    ROUND(
        COUNT(CASE WHEN status_inventario = 'CONCLUIDO' THEN 1 END)::decimal / 
        GREATEST(COUNT(*), 1) * 100, 2
    ) as percentual_concluidos,
    
    ROUND(
        COUNT(CASE WHEN status_real = 'ATRASADO' THEN 1 END)::decimal / 
        GREATEST(COUNT(*), 1) * 100, 2
    ) as percentual_atrasados,
    
    -- Métricas de patrimônio
    SUM(total_patrimonios) as total_patrimonios_sistema,
    SUM(patrimonios_coletados) as total_patrimonios_coletados,
    
    ROUND(
        SUM(patrimonios_coletados)::decimal / 
        GREATEST(SUM(total_patrimonios), 1) * 100, 2
    ) as percentual_coleta_geral,
    
    -- Métricas de tempo
    ROUND(AVG(duracao_atual_dias), 1) as duracao_media_dias,
    ROUND(AVG(velocidade_coleta_dia), 2) as velocidade_media_coleta,
    
    -- Data de atualização
    CURRENT_TIMESTAMP as data_atualizacao
    
FROM vw_inventarios_bi;

-- =====================================================
-- 5. VIEW DE TENDÊNCIAS TEMPORAIS
-- =====================================================
CREATE OR REPLACE VIEW vw_tendencias_temporais AS
SELECT 
    DATE_TRUNC('month', data_criacao) as mes_ano,
    EXTRACT(YEAR FROM data_criacao) as ano,
    EXTRACT(MONTH FROM data_criacao) as mes,
    
    -- Contadores mensais
    COUNT(*) as inventarios_criados,
    COUNT(CASE WHEN status_inventario = 'CONCLUIDO' THEN 1 END) as inventarios_concluidos_mes,
    SUM(total_patrimonios) as patrimonios_planejados_mes,
    SUM(patrimonios_coletados) as patrimonios_coletados_mes,
    
    -- Médias mensais
    ROUND(AVG(percentual_conclusao), 2) as percentual_conclusao_medio,
    ROUND(AVG(duracao_atual_dias), 1) as duracao_media_mes,
    
    -- Comparação com mês anterior
    LAG(COUNT(*)) OVER (ORDER BY DATE_TRUNC('month', data_criacao)) as inventarios_mes_anterior,
    
    ROUND(
        (COUNT(*)::decimal - LAG(COUNT(*)) OVER (ORDER BY DATE_TRUNC('month', data_criacao))) / 
        GREATEST(LAG(COUNT(*)) OVER (ORDER BY DATE_TRUNC('month', data_criacao)), 1) * 100, 2
    ) as crescimento_percentual
    
FROM vw_inventarios_bi
GROUP BY DATE_TRUNC('month', data_criacao), EXTRACT(YEAR FROM data_criacao), EXTRACT(MONTH FROM data_criacao)
ORDER BY mes_ano;

-- =====================================================
-- 6. VIEW DE ANÁLISE POR SETORES
-- =====================================================
CREATE OR REPLACE VIEW vw_analise_setores AS
SELECT 
    dp.nome_setor,
    dp.sigla_setor,
    
    -- Métricas patrimoniais
    dp.total_patrimonios,
    dp.valor_total,
    dp.percentual_bom_estado,
    dp.densidade_patrimonial,
    dp.categoria_valor,
    dp.indicador_risco,
    
    -- Métricas de inventário (últimos 12 meses)
    COUNT(DISTINCT ib.id) as inventarios_realizados_12m,
    ROUND(AVG(ib.percentual_conclusao), 2) as percentual_conclusao_medio,
    
    -- Performance de coleta
    COALESCE(pc.total_coletas, 0) as total_coletas,
    COALESCE(pc.taxa_sucesso_media, 0) as taxa_sucesso_media,
    
    -- Ranking
    RANK() OVER (ORDER BY dp.valor_total DESC) as ranking_valor,
    RANK() OVER (ORDER BY dp.total_patrimonios DESC) as ranking_quantidade,
    RANK() OVER (ORDER BY COALESCE(pc.taxa_sucesso_media, 0) DESC) as ranking_performance
    
FROM (
    SELECT 
        nome_setor,
        sigla_setor,
        SUM(total_patrimonios) as total_patrimonios,
        SUM(valor_total) as valor_total,
        ROUND(AVG(percentual_bom_estado), 2) as percentual_bom_estado,
        ROUND(AVG(densidade_patrimonial), 2) as densidade_patrimonial,
        MODE() WITHIN GROUP (ORDER BY categoria_valor) as categoria_valor,
        MODE() WITHIN GROUP (ORDER BY indicador_risco) as indicador_risco
    FROM vw_distribuicao_patrimonial
    GROUP BY nome_setor, sigla_setor
) dp

LEFT JOIN (
    SELECT 
        i.responsavel_inventario,
        COUNT(*) as inventarios_count,
        ROUND(AVG(i.percentual_conclusao), 2) as percentual_medio
    FROM vw_inventarios_bi i
    WHERE i.data_criacao >= CURRENT_DATE - INTERVAL '12 months'
    GROUP BY i.responsavel_inventario
) ib ON dp.nome_setor = ib.responsavel_inventario

LEFT JOIN (
    SELECT 
        'GERAL' as setor,  -- Simplificado para esta versão
        SUM(total_coletas) as total_coletas,
        ROUND(AVG(taxa_sucesso_percent), 2) as taxa_sucesso_media
    FROM vw_performance_coleta
) pc ON 1=1  -- Join simplificado

ORDER BY dp.valor_total DESC;

-- =====================================================
-- COMENTÁRIOS E ÍNDICES PARA PERFORMANCE
-- =====================================================

-- Comentários nas views
COMMENT ON VIEW vw_inventarios_bi IS 'View principal com métricas calculadas para inventários';
COMMENT ON VIEW vw_performance_coleta IS 'Métricas de performance dos coletores por inventário';
COMMENT ON VIEW vw_distribuicao_patrimonial IS 'Distribuição e análise patrimonial por setor e sala';
COMMENT ON VIEW vw_dashboard_kpis IS 'KPIs principais para dashboard executivo';
COMMENT ON VIEW vw_tendencias_temporais IS 'Análise de tendências temporais dos inventários';
COMMENT ON VIEW vw_analise_setores IS 'Análise comparativa entre setores';

-- Índices recomendados para melhor performance
CREATE INDEX IF NOT EXISTS idx_inventario_status_data ON tabela_inventario(status_inventario, data_criacao);
CREATE INDEX IF NOT EXISTS idx_coleta_inventario_coletor ON tabela_coleta(id_inventario, id_coletor);
CREATE INDEX IF NOT EXISTS idx_patrimonio_setor_sala ON tabela_patrimonio(id_sala, ativo);
CREATE INDEX IF NOT EXISTS idx_sala_setor ON tabela_sala(id_setor, ativo);

-- =====================================================
-- SCRIPT CONCLUÍDO
-- =====================================================

SELECT 'Views de BI criadas com sucesso!' as resultado;

-- Para verificar as views criadas:
-- SELECT schemaname, viewname FROM pg_views WHERE schemaname = 'public' AND viewname LIKE 'vw_%';