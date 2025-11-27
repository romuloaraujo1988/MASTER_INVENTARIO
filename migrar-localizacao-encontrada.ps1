# ========================================
# Script: Migração - Adicionar localizacao_encontrada
# Data: 26/11/2025
# Descrição: Adiciona campo localizacao_encontrada na tabela local_coleta
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "MIGRAÇÃO: Adicionar localizacao_encontrada" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$dbPath = "data\inventario.db"

# Verificar se o banco existe
if (-not (Test-Path $dbPath)) {
    Write-Host "❌ ERRO: Banco de dados não encontrado em: $dbPath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Execute primeiro: .\criar-banco-offline.bat" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Banco de dados encontrado: $dbPath" -ForegroundColor Green
Write-Host ""

# Verificar se a coluna já existe
Write-Host "Verificando se a coluna já existe..." -ForegroundColor Yellow
$checkColumn = & sqlite3.exe $dbPath "PRAGMA table_info(local_coleta);" 2>&1 | Select-String "localizacao_encontrada"

if ($checkColumn) {
    Write-Host "✓ Coluna 'localizacao_encontrada' já existe!" -ForegroundColor Green
    Write-Host ""
    
    # Mostrar estatísticas
    Write-Host "Estatísticas atuais:" -ForegroundColor Cyan
    & sqlite3.exe $dbPath "SELECT COUNT(*) as total, COUNT(localizacao_encontrada) as com_loc_encontrada FROM local_coleta;" 2>&1
    
    exit 0
}

Write-Host "⚠ Coluna 'localizacao_encontrada' NÃO existe. Iniciando migração..." -ForegroundColor Yellow
Write-Host ""

# Executar migração
Write-Host "Executando migração..." -ForegroundColor Cyan
$result = & sqlite3.exe $dbPath ".read adicionar-localizacao-encontrada-sqlite.sql" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Migração executada com sucesso!" -ForegroundColor Green
    Write-Host ""
    
    # Mostrar resultado
    Write-Host "Resultado da migração:" -ForegroundColor Cyan
    & sqlite3.exe $dbPath "SELECT COUNT(*) as total, COUNT(localizacao_encontrada) as com_loc_encontrada FROM local_coleta;" 2>&1
    
    Write-Host ""
    Write-Host "Exemplos de registros:" -ForegroundColor Cyan
    & sqlite3.exe $dbPath "SELECT id, localizacao_atual, localizacao_encontrada FROM local_coleta LIMIT 3;" 2>&1
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✓ MIGRAÇÃO CONCLUÍDA COM SUCESSO!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
} else {
    Write-Host "❌ ERRO ao executar migração!" -ForegroundColor Red
    Write-Host $result
    exit 1
}
