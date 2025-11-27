-- Script para adicionar campos faltantes na tabela local_coleta (SQLite)
-- Objetivo: Melhorar qualidade dos dados e compatibilidade com PostgreSQL
-- Data: 26/11/2025

-- Verificar estrutura atual
.schema local_coleta

-- Adicionar campos para melhor rastreamento
ALTER TABLE local_coleta ADD COLUMN status_coleta TEXT DEFAULT 'COLETADO';
ALTER TABLE local_coleta ADD COLUMN divergencia BOOLEAN DEFAULT FALSE;
ALTER TABLE local_coleta ADD COLUMN motivo_divergencia TEXT;

-- Adicionar geolocalização
ALTER TABLE local_coleta ADD COLUMN latitude REAL;
ALTER TABLE local_coleta ADD COLUMN longitude REAL;

-- Adicionar categoria para itens sem etiqueta
ALTER TABLE local_coleta ADD COLUMN categoria_item_sem_etiqueta TEXT;

-- Adicionar métricas de performance
ALTER TABLE local_coleta ADD COLUMN tempo_coleta_segundos INTEGER;
ALTER TABLE local_coleta ADD COLUMN tempo_scan_segundos INTEGER;
ALTER TABLE local_coleta ADD COLUMN tempo_preenchimento_segundos INTEGER;

-- Adicionar informações de método de coleta
ALTER TABLE local_coleta ADD COLUMN metodo_coleta TEXT; -- 'QR_CODE', 'MANUAL', 'BARCODE'
ALTER TABLE local_coleta ADD COLUMN tipo_scan TEXT; -- 'CAMERA', 'LEITOR_EXTERNO'
ALTER TABLE local_coleta ADD COLUMN tentativas_scan INTEGER DEFAULT 1;
ALTER TABLE local_coleta ADD COLUMN erros_scan INTEGER DEFAULT 0;

-- Adicionar informações temporais
ALTER TABLE local_coleta ADD COLUMN hora_coleta INTEGER; -- 0-23
ALTER TABLE local_coleta ADD COLUMN dia_semana INTEGER; -- 1-7 (1=Domingo)
ALTER TABLE local_coleta ADD COLUMN periodo_coleta TEXT; -- 'MANHA', 'TARDE', 'NOITE'

-- Adicionar qualidade da etiqueta
ALTER TABLE local_coleta ADD COLUMN qualidade_etiqueta TEXT; -- 'BOA', 'RUIM', 'ILEGIVEL'

-- Verificar estrutura atualizada
.schema local_coleta

-- Mensagem de sucesso
SELECT 'Campos adicionados com sucesso!' as status;
SELECT 'Total de colunas: ' || COUNT(*) as info FROM pragma_table_info('local_coleta');
