-- ============================================================================
-- MIGRAÇÃO v2.1 - Métricas de Coleta
-- Data: 16/11/2025
-- Descrição: Adiciona campos para coletar métricas de tempo, método e scan
-- ============================================================================

-- 1. ADICIONAR COLUNAS DE MÉTRICAS DE TEMPO
ALTER TABLE TABELA_COLETA 
ADD COLUMN IF NOT EXISTS TEMPO_COLETA_SEGUNDOS INTEGER,
ADD COLUMN IF NOT EXISTS TEMPO_SCAN_SEGUNDOS INTEGER,
ADD COLUMN IF NOT EXISTS TEMPO_PREENCHIMENTO_SEGUNDOS INTEGER;

-- 2. ADICIONAR COLUNAS DE MÉTODO E CONTEXTO
ALTER TABLE TABELA_COLETA 
ADD COLUMN IF NOT EXISTS METODO_COLETA VARCHAR(20),
ADD COLUMN IF NOT EXISTS HORA_COLETA INTEGER,
ADD COLUMN IF NOT EXISTS DIA_SEMANA INTEGER,
ADD COLUMN IF NOT EXISTS PERIODO_COLETA VARCHAR(10);

-- 3. ADICIONAR COLUNAS DE SCAN (QR CODE vs CÓDIGO DE BARRAS)
ALTER TABLE TABELA_COLETA 
ADD COLUMN IF NOT EXISTS TIPO_SCAN VARCHAR(20),
ADD COLUMN IF NOT EXISTS TENTATIVAS_SCAN INTEGER DEFAULT 1,
ADD COLUMN IF NOT EXISTS ERROS_SCAN INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS QUALIDADE_ETIQUETA VARCHAR(20);

-- 4. ADICIONAR COMENTÁRIOS
COMMENT ON COLUMN TABELA_COLETA.TEMPO_COLETA_SEGUNDOS IS 'Tempo total da coleta em segundos';
COMMENT ON COLUMN TABELA_COLETA.TEMPO_SCAN_SEGUNDOS IS 'Tempo do scan (QR ou Barcode) em segundos';
COMMENT ON COLUMN TABELA_COLETA.TEMPO_PREENCHIMENTO_SEGUNDOS IS 'Tempo de preenchimento do formulário em segundos';
COMMENT ON COLUMN TABELA_COLETA.METODO_COLETA IS 'Método: QR_CODE, CODIGO_BARRAS, MANUAL, BUSCA, SEM_ETIQUETA';
COMMENT ON COLUMN TABELA_COLETA.HORA_COLETA IS 'Hora da coleta (0-23)';
COMMENT ON COLUMN TABELA_COLETA.DIA_SEMANA IS 'Dia da semana (1=Dom, 2=Seg, ..., 7=Sab)';
COMMENT ON COLUMN TABELA_COLETA.PERIODO_COLETA IS 'Período: MANHA, TARDE, NOITE';
COMMENT ON COLUMN TABELA_COLETA.TIPO_SCAN IS 'Tipo de scan: QR_CODE ou CODIGO_BARRAS';
COMMENT ON COLUMN TABELA_COLETA.TENTATIVAS_SCAN IS 'Número de tentativas até sucesso no scan';
COMMENT ON COLUMN TABELA_COLETA.ERROS_SCAN IS 'Número de erros durante o scan';
COMMENT ON COLUMN TABELA_COLETA.QUALIDADE_ETIQUETA IS 'Qualidade: OTIMA, BOA, REGULAR, RUIM';

-- 5. CRIAR ÍNDICES PARA ANÁLISE
CREATE INDEX IF NOT EXISTS idx_coleta_tempo ON TABELA_COLETA(TEMPO_COLETA_SEGUNDOS);
CREATE INDEX IF NOT EXISTS idx_coleta_metodo ON TABELA_COLETA(METODO_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_tipo_scan ON TABELA_COLETA(TIPO_SCAN);
CREATE INDEX IF NOT EXISTS idx_coleta_hora ON TABELA_COLETA(HORA_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_periodo ON TABELA_COLETA(PERIODO_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_qualidade ON TABELA_COLETA(QUALIDADE_ETIQUETA);

-- 6. ATUALIZAR COLETAS EXISTENTES (preencher horário das coletas antigas)
UPDATE TABELA_COLETA 
SET 
    HORA_COLETA = EXTRACT(HOUR FROM DATA_COLETA),
    DIA_SEMANA = EXTRACT(DOW FROM DATA_COLETA) + 1,
    PERIODO_COLETA = CASE 
        WHEN EXTRACT(HOUR FROM DATA_COLETA) BETWEEN 6 AND 11 THEN 'MANHA'
        WHEN EXTRACT(HOUR FROM DATA_COLETA) BETWEEN 12 AND 17 THEN 'TARDE'
        ELSE 'NOITE'
    END,
    METODO_COLETA = 'QR_CODE'  -- Assumir QR_CODE para coletas antigas
WHERE HORA_COLETA IS NULL;

-- 7. VERIFICAR MIGRAÇÃO
SELECT 
    'VERIFICACAO_MIGRACAO' as tipo,
    COUNT(*) as total_coletas,
    COUNT(TEMPO_COLETA_SEGUNDOS) as com_tempo,
    COUNT(METODO_COLETA) as com_metodo,
    COUNT(TIPO_SCAN) as com_tipo_scan,
    COUNT(HORA_COLETA) as com_hora,
    COUNT(PERIODO_COLETA) as com_periodo
FROM TABELA_COLETA;

-- 8. EXIBIR ESTRUTURA ATUALIZADA
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'tabela_coleta'
    AND column_name IN (
        'tempo_coleta_segundos',
        'tempo_scan_segundos',
        'tempo_preenchimento_segundos',
        'metodo_coleta',
        'tipo_scan',
        'tentativas_scan',
        'erros_scan',
        'hora_coleta',
        'dia_semana',
        'periodo_coleta',
        'qualidade_etiqueta'
    )
ORDER BY ordinal_position;

-- ============================================================================
-- FIM DA MIGRAÇÃO
-- ============================================================================
