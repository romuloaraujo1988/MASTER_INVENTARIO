-- ========================================
-- MIGRAÇÃO: Adicionar campo localizacao_encontrada
-- Data: 26/11/2025
-- Motivo: Campo necessário para buscar histórico de coletas por sala
-- ========================================

-- Adicionar coluna localizacao_encontrada se não existir
ALTER TABLE local_coleta ADD COLUMN localizacao_encontrada TEXT;

-- Copiar dados de localizacao_atual para localizacao_encontrada (dados existentes)
UPDATE local_coleta 
SET localizacao_encontrada = localizacao_atual 
WHERE localizacao_encontrada IS NULL;

-- Verificar resultado
SELECT 
    COUNT(*) as total_registros,
    COUNT(localizacao_encontrada) as com_localizacao_encontrada,
    COUNT(localizacao_atual) as com_localizacao_atual
FROM local_coleta;

-- Mostrar alguns exemplos
SELECT 
    id,
    localizacao_atual,
    localizacao_encontrada,
    data_coleta
FROM local_coleta
LIMIT 5;
