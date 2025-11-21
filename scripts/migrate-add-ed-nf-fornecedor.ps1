# ============================================
# Script de Migração: ED, Nota Fiscal e Fornecedor
# Data: 16/11/2024
# ============================================

$ErrorActionPreference = "Stop"

# Configurações
$PGHOST = "localhost"
$PGPORT = "5432"
$PGUSER = "postgres"
$PGDATABASE = "sispatrimonio"
$env:PGPASSWORD = "Romulo@2020"

$BACKUP_DIR = "backups"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"
$BACKUP_FILE = "$BACKUP_DIR/sispatrimonio_backup_ed_nf_$TIMESTAMP.backup"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MIGRAÇÃO: ED + Nota Fiscal + Fornecedor" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. CRIAR DIRETÓRIO DE BACKUP
if (-not (Test-Path $BACKUP_DIR)) {
    New-Item -ItemType Directory -Path $BACKUP_DIR | Out-Null
}

# 2. BACKUP DO BANCO
Write-Host "📦 Criando backup do banco de dados..." -ForegroundColor Yellow
& "C:\Program Files\PostgreSQL\17\bin\pg_dump.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -F c `
    -b `
    -v `
    -f $BACKUP_FILE `
    $PGDATABASE

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Backup criado: $BACKUP_FILE" -ForegroundColor Green
} else {
    Write-Host "❌ Erro ao criar backup!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 3. EXECUTAR MIGRAÇÃO
Write-Host "🔄 Executando migração..." -ForegroundColor Yellow
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -f "sql/migration_add_ed_nf_fornecedor.sql"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Migração executada com sucesso!" -ForegroundColor Green
} else {
    Write-Host "❌ Erro na migração!" -ForegroundColor Red
    Write-Host "⚠️  Restaure o backup se necessário:" -ForegroundColor Yellow
    Write-Host "   pg_restore -h $PGHOST -p $PGPORT -U $PGUSER -d $PGDATABASE $BACKUP_FILE" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# 4. TESTAR INSERÇÃO
Write-Host "🧪 Testando inserção com novos campos..." -ForegroundColor Yellow
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "INSERT INTO TABELA_PATRIMONIO (NUMERO_PATRIMONIO, DESCRICAO, ED, NUMERO_NOTA_FISCAL, FORNECEDOR, STATUS) VALUES ('TEST-ED-001', 'Teste ED', '12311.0101', 'NF-123456', 'Fornecedor Teste LTDA', 'ATIVO') RETURNING ID, ED, NUMERO_NOTA_FISCAL, FORNECEDOR;"

Write-Host ""

# 5. LIMPAR TESTE
Write-Host "🧹 Removendo dados de teste..." -ForegroundColor Yellow
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "DELETE FROM TABELA_PATRIMONIO WHERE NUMERO_PATRIMONIO = 'TEST-ED-001';"

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ MIGRAÇÃO CONCLUÍDA COM SUCESSO!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Backup salvo em: $BACKUP_FILE" -ForegroundColor Cyan
Write-Host ""
