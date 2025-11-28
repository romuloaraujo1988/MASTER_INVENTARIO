# Script para exportar itens compostos do banco local para arquivo SQL
# Uso: .\exportar-itens-compostos-servidor.ps1

$ErrorActionPreference = "Stop"

# Configurações do banco local
$dbHost = "localhost"
$dbPort = "5432"
$dbName = "sispatrimonio"
$dbUser = "inventario"

# Arquivo de saída
$outputFile = "sql\import_itens_compostos_servidor.sql"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Exportador de Itens Compostos" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Query para gerar os INSERTs
$query = @"
SELECT 
    'INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (' ||
    id || ', ' ||
    id_patrimonio_principal || ', ' ||
    '''' || REPLACE(tipo_componente, '''', '''''') || ''', ' ||
    '''' || REPLACE(descricao_componente, '''', '''''') || ''', ' ||
    quantidade_esperada || ', ' ||
    obrigatorio || ', ' ||
    COALESCE('''' || REPLACE(observacao, '''', '''''') || '''', 'NULL') || ', ' ||
    '''' || data_cadastro || ''');' as insert_sql
FROM tabela_item_composto
ORDER BY id;
"@

Write-Host "Conectando ao banco local..." -ForegroundColor Yellow

# Criar cabeçalho do arquivo
$header = @"
-- ============================================
-- Script de Importação: tabela_item_composto
-- Gerado em: $(Get-Date -Format "dd/MM/yyyy HH:mm:ss")
-- Total de registros: (será calculado)
-- ============================================

-- Desabilitar constraints temporariamente para performance
SET session_replication_role = 'replica';

-- Limpar tabela existente (CUIDADO!)
-- TRUNCATE TABLE tabela_item_composto RESTART IDENTITY CASCADE;

-- Ou usar ON CONFLICT para atualizar existentes
-- Descomente a linha abaixo se quiser limpar antes
-- DELETE FROM tabela_item_composto;

BEGIN;

"@

$footer = @"

COMMIT;

-- Reabilitar constraints
SET session_replication_role = 'origin';

-- Atualizar sequence para próximo ID
SELECT setval('tabela_item_composto_id_seq', (SELECT MAX(id) FROM tabela_item_composto));

-- Verificar importação
SELECT COUNT(*) as total_importado FROM tabela_item_composto;

"@

try {
    # Definir variável de ambiente para senha (se necessário)
    $env:PGPASSWORD = "inventario"
    
    Write-Host "Executando query de exportação..." -ForegroundColor Yellow
    
    # Executar query e capturar resultado
    $result = psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -t -A -c $query 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        throw "Erro ao executar query: $result"
    }
    
    # Contar linhas
    $lines = $result -split "`n" | Where-Object { $_ -match "^INSERT" }
    $totalRecords = $lines.Count
    
    Write-Host "Total de registros encontrados: $totalRecords" -ForegroundColor Green
    
    # Atualizar cabeçalho com total
    $header = $header -replace "\(será calculado\)", $totalRecords
    
    # Escrever arquivo
    Write-Host "Escrevendo arquivo: $outputFile" -ForegroundColor Yellow
    
    $header | Out-File -FilePath $outputFile -Encoding UTF8
    $result | Out-File -FilePath $outputFile -Encoding UTF8 -Append
    $footer | Out-File -FilePath $outputFile -Encoding UTF8 -Append
    
    $fileSize = (Get-Item $outputFile).Length / 1KB
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Exportação Concluída!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Arquivo gerado: $outputFile" -ForegroundColor Cyan
    Write-Host "Total de registros: $totalRecords" -ForegroundColor Cyan
    Write-Host "Tamanho do arquivo: $([math]::Round($fileSize, 2)) KB" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Para importar no servidor, execute:" -ForegroundColor Yellow
    Write-Host "  psql -h <servidor> -U <usuario> -d <banco> -f $outputFile" -ForegroundColor White
    
} catch {
    Write-Host "ERRO: $_" -ForegroundColor Red
    exit 1
} finally {
    $env:PGPASSWORD = $null
}
