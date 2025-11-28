# ============================================================================
# Script para extrair dados completos de Itens Compostos do PostgreSQL
# Sistema de Inventário Patrimonial - IFMT
# ============================================================================

$outputFile = "dados_itens_compostos_exportados.sql"
$connectionString = "Host=localhost;Port=5432;Database=sispatrimonio;Username=inventario;Password=inventario"

Write-Host "Extraindo dados de itens compostos..." -ForegroundColor Cyan

# Cabeçalho do arquivo
$header = @"
-- ============================================================================
-- SCRIPT DE DADOS COMPLETO - ITENS COMPOSTOS
-- Sistema de Inventário Patrimonial - IFMT
-- Gerado em: $(Get-Date -Format "dd/MM/yyyy HH:mm:ss")
-- ============================================================================

SET session_replication_role = 'replica';

-- ============================================================================
-- TABELA: tabela_item_composto
-- ============================================================================

"@

$header | Out-File -FilePath $outputFile -Encoding UTF8

# Query para extrair dados
$query = @"
SELECT 'INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (' ||
       id || ', ' ||
       COALESCE(id_patrimonio_principal::text, 'NULL') || ', ' ||
       COALESCE('''' || REPLACE(tipo_componente, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE('''' || REPLACE(descricao_componente, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE(quantidade_esperada::text, '1') || ', ' ||
       COALESCE(obrigatorio::text, 'true') || ', ' ||
       COALESCE('''' || REPLACE(observacao, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE('''' || data_cadastro::text || '''', 'NOW()') || ');' as insert_stmt
FROM tabela_item_composto
ORDER BY id;
"@

Write-Host "Executando query para tabela_item_composto..."

# Executar via psql
$env:PGPASSWORD = "inventario"
$result = psql -h localhost -U inventario -d sispatrimonio -t -A -c $query

if ($result) {
    $result | Out-File -FilePath $outputFile -Append -Encoding UTF8
    Write-Host "  -> Dados de tabela_item_composto exportados!" -ForegroundColor Green
}

# Adicionar dados de coleta_componente
$coletaHeader = @"

-- ============================================================================
-- TABELA: tabela_coleta_componente
-- ============================================================================

"@

$coletaHeader | Out-File -FilePath $outputFile -Append -Encoding UTF8

$queryColeta = @"
SELECT 'INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (' ||
       id || ', ' ||
       COALESCE(id_item_composto::text, 'NULL') || ', ' ||
       COALESCE(id_inventario::text, 'NULL') || ', ' ||
       COALESCE(id_coletor::text, 'NULL') || ', ' ||
       COALESCE(quantidade_encontrada::text, '0') || ', ' ||
       COALESCE('''' || status_componente || '''', 'NULL') || ', ' ||
       COALESCE('''' || REPLACE(observacao_coleta, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE('''' || data_coleta::text || '''', 'NOW()') || ');' as insert_stmt
FROM tabela_coleta_componente
ORDER BY id;
"@

$resultColeta = psql -h localhost -U inventario -d sispatrimonio -t -A -c $queryColeta

if ($resultColeta) {
    $resultColeta | Out-File -FilePath $outputFile -Append -Encoding UTF8
    Write-Host "  -> Dados de tabela_coleta_componente exportados!" -ForegroundColor Green
}

# Footer
$footer = @"

-- ============================================================================
-- ATUALIZAR SEQUENCES
-- ============================================================================

SELECT setval('tabela_item_composto_id_seq', (SELECT MAX(id) FROM tabela_item_composto));
SELECT setval('tabela_coleta_componente_id_seq', (SELECT MAX(id) FROM tabela_coleta_componente));

SET session_replication_role = 'origin';

-- FIM DO SCRIPT
"@

$footer | Out-File -FilePath $outputFile -Append -Encoding UTF8

Write-Host ""
Write-Host "Exportação concluída!" -ForegroundColor Green
Write-Host "Arquivo gerado: $outputFile" -ForegroundColor Yellow
