# ============================================
# Script de Teste - Importação CSV com ED
# Data: 16/11/2024
# ============================================

$ErrorActionPreference = "Stop"

# Configurações
$PGHOST = "localhost"
$PGPORT = "5432"
$PGUSER = "postgres"
$PGDATABASE = "sispatrimonio"
$env:PGPASSWORD = "Romulo@2020"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TESTE DE IMPORTAÇÃO CSV - CAMPO ED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se arquivo de exemplo existe
$arquivoCSV = "data/exemplo_importacao_ed.csv"
if (-not (Test-Path $arquivoCSV)) {
    Write-Host "❌ Arquivo de exemplo não encontrado: $arquivoCSV" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Arquivo de exemplo encontrado: $arquivoCSV" -ForegroundColor Green
Write-Host ""

# Contar linhas do arquivo (excluindo cabeçalho)
$linhas = (Get-Content $arquivoCSV | Measure-Object -Line).Lines - 1
Write-Host "📊 Total de registros no arquivo: $linhas" -ForegroundColor Cyan
Write-Host ""

# Mostrar preview do arquivo
Write-Host "📋 Preview do arquivo:" -ForegroundColor Yellow
Get-Content $arquivoCSV -Head 3
Write-Host ""

# Verificar se registros de teste já existem
Write-Host "🔍 Verificando se registros de teste já existem..." -ForegroundColor Yellow
$existentes = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -t `
    -c "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-%';" 2>&1

if ($existentes -gt 0) {
    Write-Host "⚠️  Encontrados $existentes registros de teste existentes" -ForegroundColor Yellow
    Write-Host "🧹 Removendo registros antigos..." -ForegroundColor Yellow
    
    & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
        -h $PGHOST `
        -p $PGPORT `
        -U $PGUSER `
        -d $PGDATABASE `
        -c "DELETE FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-%';" | Out-Null
    
    Write-Host "✅ Registros antigos removidos" -ForegroundColor Green
}
Write-Host ""

Write-Host "📝 INSTRUÇÕES PARA TESTE MANUAL:" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Abrir o sistema desktop" -ForegroundColor White
Write-Host "2. Menu: Arquivo → Importar → Importar CSV" -ForegroundColor White
Write-Host "3. Selecionar arquivo: $arquivoCSV" -ForegroundColor White
Write-Host "4. Aguardar processamento" -ForegroundColor White
Write-Host "5. Verificar relatório de importação" -ForegroundColor White
Write-Host ""
Write-Host "Pressione ENTER após realizar a importação manual..." -ForegroundColor Yellow
Read-Host

Write-Host ""
Write-Host "🔍 Verificando dados importados..." -ForegroundColor Yellow
Write-Host ""

# Verificar se dados foram importados
$importados = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -t `
    -c "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-%';" 2>&1

Write-Host "📊 Total de registros importados: $importados" -ForegroundColor Cyan
Write-Host ""

if ($importados -eq $linhas) {
    Write-Host "✅ Todos os registros foram importados com sucesso!" -ForegroundColor Green
} elseif ($importados -gt 0) {
    Write-Host "⚠️  Alguns registros foram importados ($importados de $linhas)" -ForegroundColor Yellow
} else {
    Write-Host "❌ Nenhum registro foi importado!" -ForegroundColor Red
    Write-Host "Verifique se a importação foi realizada corretamente." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "📋 Dados importados:" -ForegroundColor Yellow
Write-Host ""

& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "SELECT NUMERO, ED, NUMERO_NOTA_FISCAL, FORNECEDOR FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-%' ORDER BY NUMERO;"

Write-Host ""
Write-Host "🧪 Validando campos ED..." -ForegroundColor Yellow

# Verificar se todos os EDs foram importados
$comED = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -t `
    -c "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-%' AND ED IS NOT NULL;" 2>&1

Write-Host "  Registros com ED: $comED de $importados" -ForegroundColor Cyan

if ($comED -eq $importados) {
    Write-Host "  ✅ Todos os registros têm ED preenchido" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Alguns registros não têm ED preenchido" -ForegroundColor Yellow
}

# Verificar se todos os Números de Nota Fiscal foram importados
$comNF = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -t `
    -c "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-%' AND NUMERO_NOTA_FISCAL IS NOT NULL;" 2>&1

Write-Host "  Registros com Nota Fiscal: $comNF de $importados" -ForegroundColor Cyan

if ($comNF -eq $importados) {
    Write-Host "  ✅ Todos os registros têm Nota Fiscal preenchida" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Alguns registros não têm Nota Fiscal preenchida" -ForegroundColor Yellow
}

# Verificar se todos os Fornecedores foram importados
$comFornecedor = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -t `
    -c "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-%' AND FORNECEDOR IS NOT NULL;" 2>&1

Write-Host "  Registros com Fornecedor: $comFornecedor de $importados" -ForegroundColor Cyan

if ($comFornecedor -eq $importados) {
    Write-Host "  ✅ Todos os registros têm Fornecedor preenchido" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Alguns registros não têm Fornecedor preenchido" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🧹 Deseja remover os dados de teste? (S/N)" -ForegroundColor Yellow
$resposta = Read-Host

if ($resposta -eq "S" -or $resposta -eq "s") {
    Write-Host "🧹 Removendo dados de teste..." -ForegroundColor Yellow
    
    & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
        -h $PGHOST `
        -p $PGPORT `
        -U $PGUSER `
        -d $PGDATABASE `
        -c "DELETE FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-%';" | Out-Null
    
    Write-Host "✅ Dados de teste removidos" -ForegroundColor Green
} else {
    Write-Host "ℹ️  Dados de teste mantidos no banco" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ TESTE DE IMPORTAÇÃO CONCLUÍDO" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
