# ============================================================================
# Script de Sincronização PostgreSQL -> SQLite
# ============================================================================
# 
# Este script executa a sincronização completa de dados do PostgreSQL
# para o banco SQLite offline usado pelo sistema de inventário.
#
# Uso: .\sincronizar-postgresql-sqlite.ps1
# ============================================================================

Write-Host ""
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  SINCRONIZAÇÃO PostgreSQL -> SQLite" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se o Maven está disponível
Write-Host "[1/5] Verificando Maven..." -ForegroundColor Yellow

# Tentar encontrar mvnw.cmd (Maven Wrapper) primeiro
if (Test-Path "mvnw.cmd") {
    $mvnCmd = ".\mvnw.cmd"
    Write-Host "   ✓ Maven Wrapper encontrado" -ForegroundColor Green
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvnCmd = "mvn"
    Write-Host "   ✓ Maven encontrado no PATH" -ForegroundColor Green
} else {
    Write-Host "❌ ERRO: Maven não encontrado!" -ForegroundColor Red
    Write-Host "   Instale o Maven ou use o Maven Wrapper (mvnw.cmd)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Verificar se o arquivo de configuração do banco existe
Write-Host "[2/5] Verificando configuração do banco..." -ForegroundColor Yellow
$configFile = "$env:USERPROFILE\configuracao_banco.json"
$configFileLocal = "configuracao_banco.json"

if (Test-Path $configFile) {
    Write-Host "   ✓ Configuração encontrada em: $configFile" -ForegroundColor Green
} elseif (Test-Path $configFileLocal) {
    Write-Host "   ✓ Configuração encontrada em: $configFileLocal" -ForegroundColor Green
} else {
    Write-Host "⚠ AVISO: Arquivo de configuração não encontrado!" -ForegroundColor Yellow
    Write-Host "   Locais verificados:" -ForegroundColor Yellow
    Write-Host "   - $configFile" -ForegroundColor Gray
    Write-Host "   - $configFileLocal" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   Tentando continuar mesmo assim..." -ForegroundColor Yellow
}
Write-Host ""

# Criar diretório data se não existir
Write-Host "[3/5] Preparando diretório de dados..." -ForegroundColor Yellow
if (-not (Test-Path "data")) {
    New-Item -ItemType Directory -Path "data" | Out-Null
    Write-Host "   ✓ Diretório 'data' criado" -ForegroundColor Green
} else {
    Write-Host "   ✓ Diretório 'data' já existe" -ForegroundColor Green
}
Write-Host ""

# Fazer backup do SQLite existente (se houver)
Write-Host "[4/5] Verificando backup..." -ForegroundColor Yellow
$sqliteDb = "data\inventario.db"
if (Test-Path $sqliteDb) {
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupFile = "data\inventario_backup_$timestamp.db"
    Copy-Item $sqliteDb $backupFile
    Write-Host "   ✓ Backup criado: $backupFile" -ForegroundColor Green
} else {
    Write-Host "   ℹ Nenhum banco SQLite existente para backup" -ForegroundColor Gray
}
Write-Host ""

# Executar sincronização
Write-Host "[5/5] Executando sincronização..." -ForegroundColor Yellow
Write-Host ""
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""

# Executar a classe Java de sincronização
& $mvnCmd exec:java `"-Dexec.mainClass=com.inventario.offline.SyncPostgresToSQLiteV2`" `"-Dexec.cleanupDaemonThreads=false`"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "============================================================================" -ForegroundColor Green
    Write-Host "  ✅ SINCRONIZAÇÃO CONCLUÍDA COM SUCESSO!" -ForegroundColor Green
    Write-Host "============================================================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Banco SQLite atualizado: $sqliteDb" -ForegroundColor Green
    Write-Host ""
    
    # Mostrar estatísticas do banco
    Write-Host "Estatísticas do banco SQLite:" -ForegroundColor Cyan
    $dbSize = (Get-Item $sqliteDb).Length / 1MB
    Write-Host "  Tamanho: $([math]::Round($dbSize, 2)) MB" -ForegroundColor White
    Write-Host ""
    
} else {
    Write-Host ""
    Write-Host "============================================================================" -ForegroundColor Red
    Write-Host "  ❌ ERRO NA SINCRONIZAÇÃO!" -ForegroundColor Red
    Write-Host "============================================================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifique:" -ForegroundColor Yellow
    Write-Host "  1. Conexão com PostgreSQL está ativa" -ForegroundColor White
    Write-Host "  2. Credenciais do banco estão corretas" -ForegroundColor White
    Write-Host "  3. Tabelas existem no PostgreSQL" -ForegroundColor White
    Write-Host ""
    exit 1
}
