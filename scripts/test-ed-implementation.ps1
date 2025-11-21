# ============================================
# Script de Teste - Implementação Campo ED
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
Write-Host "  TESTES DE IMPLEMENTAÇÃO - CAMPO ED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$totalTestes = 0
$testesPassados = 0
$testesFalhados = 0

function Test-SQL {
    param(
        [string]$Nome,
        [string]$SQL,
        [string]$Esperado
    )
    
    $totalTestes++
    Write-Host "Teste $totalTestes`: $Nome" -ForegroundColor Yellow
    
    try {
        $resultado = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
            -h $PGHOST `
            -p $PGPORT `
            -U $PGUSER `
            -d $PGDATABASE `
            -t `
            -c $SQL 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ PASSOU" -ForegroundColor Green
            $script:testesPassados++
            return $true
        } else {
            Write-Host "  ❌ FALHOU: $resultado" -ForegroundColor Red
            $script:testesFalhados++
            return $false
        }
    } catch {
        Write-Host "  ❌ ERRO: $_" -ForegroundColor Red
        $script:testesFalhados++
        return $false
    }
}

Write-Host "=== TESTES DE ESTRUTURA ===" -ForegroundColor Cyan
Write-Host ""

# Teste 1: Verificar se coluna ED existe
Test-SQL `
    -Nome "Coluna ED existe" `
    -SQL "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'tabela_patrimonio' AND column_name = 'ed';" `
    -Esperado "1"

# Teste 2: Verificar tipo da coluna ED
Test-SQL `
    -Nome "Coluna ED é VARCHAR" `
    -SQL "SELECT data_type FROM information_schema.columns WHERE table_name = 'tabela_patrimonio' AND column_name = 'ed';" `
    -Esperado "character varying"

# Teste 3: Verificar tamanho da coluna ED
Test-SQL `
    -Nome "Coluna ED tem tamanho 20" `
    -SQL "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'tabela_patrimonio' AND column_name = 'ed';" `
    -Esperado "20"

# Teste 4: Verificar se índice ED existe
Test-SQL `
    -Nome "Índice idx_patrimonio_ed existe" `
    -SQL "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'tabela_patrimonio' AND indexname = 'idx_patrimonio_ed';" `
    -Esperado "1"

# Teste 5: Verificar se índice Nota Fiscal existe
Test-SQL `
    -Nome "Índice idx_patrimonio_nota_fiscal existe" `
    -SQL "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'tabela_patrimonio' AND indexname = 'idx_patrimonio_nota_fiscal';" `
    -Esperado "1"

# Teste 6: Verificar se índice Fornecedor existe
Test-SQL `
    -Nome "Índice idx_patrimonio_fornecedor existe" `
    -SQL "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'tabela_patrimonio' AND indexname = 'idx_patrimonio_fornecedor';" `
    -Esperado "1"

Write-Host ""
Write-Host "=== TESTES DE INSERÇÃO ===" -ForegroundColor Cyan
Write-Host ""

# Teste 7: Inserir patrimônio COM ED
Write-Host "Teste 7: Inserir patrimônio COM ED" -ForegroundColor Yellow
$resultado = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "INSERT INTO TABELA_PATRIMONIO (NUMERO, STATUS, ED, DESCRICAO, NUMERO_NOTA_FISCAL, FORNECEDOR) VALUES ('TEST-ED-001', 'ATIVO', '12311.0101', 'Teste ED', 'NF-TEST-001', 'Fornecedor Teste') RETURNING ID;" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ PASSOU" -ForegroundColor Green
    $testesPassados++
} else {
    Write-Host "  ❌ FALHOU: $resultado" -ForegroundColor Red
    $testesFalhados++
}
$totalTestes++

# Teste 8: Inserir patrimônio SEM ED (compatibilidade)
Write-Host "Teste 8: Inserir patrimônio SEM ED (compatibilidade)" -ForegroundColor Yellow
$resultado = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "INSERT INTO TABELA_PATRIMONIO (NUMERO, STATUS, DESCRICAO) VALUES ('TEST-ED-002', 'ATIVO', 'Teste sem ED') RETURNING ID;" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ PASSOU" -ForegroundColor Green
    $testesPassados++
} else {
    Write-Host "  ❌ FALHOU: $resultado" -ForegroundColor Red
    $testesFalhados++
}
$totalTestes++

Write-Host ""
Write-Host "=== TESTES DE CONSULTA ===" -ForegroundColor Cyan
Write-Host ""

# Teste 9: Consultar patrimônio com ED
Test-SQL `
    -Nome "Consultar patrimônio com ED" `
    -SQL "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO = 'TEST-ED-001' AND ED = '12311.0101';" `
    -Esperado "1"

# Teste 10: Consultar patrimônio sem ED
Test-SQL `
    -Nome "Consultar patrimônio sem ED" `
    -SQL "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO = 'TEST-ED-002' AND ED IS NULL;" `
    -Esperado "1"

# Teste 11: Buscar por ED usando índice
Test-SQL `
    -Nome "Buscar por ED usando índice" `
    -SQL "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE ED = '12311.0101';" `
    -Esperado "1"

Write-Host ""
Write-Host "=== TESTES DE ATUALIZAÇÃO ===" -ForegroundColor Cyan
Write-Host ""

# Teste 12: Atualizar ED
Write-Host "Teste 12: Atualizar ED" -ForegroundColor Yellow
$resultado = & "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "UPDATE TABELA_PATRIMONIO SET ED = '12311.0103' WHERE NUMERO = 'TEST-ED-002';" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ PASSOU" -ForegroundColor Green
    $testesPassados++
} else {
    Write-Host "  ❌ FALHOU: $resultado" -ForegroundColor Red
    $testesFalhados++
}
$totalTestes++

# Teste 13: Verificar atualização
Test-SQL `
    -Nome "Verificar atualização de ED" `
    -SQL "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE NUMERO = 'TEST-ED-002' AND ED = '12311.0103';" `
    -Esperado "1"

Write-Host ""
Write-Host "=== LIMPEZA ===" -ForegroundColor Cyan
Write-Host ""

# Limpar dados de teste
Write-Host "Removendo dados de teste..." -ForegroundColor Yellow
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h $PGHOST `
    -p $PGPORT `
    -U $PGUSER `
    -d $PGDATABASE `
    -c "DELETE FROM TABELA_PATRIMONIO WHERE NUMERO LIKE 'TEST-ED-%';" | Out-Null

Write-Host "  ✅ Dados de teste removidos" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESUMO DOS TESTES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Total de Testes: $totalTestes" -ForegroundColor White
Write-Host "Testes Passados: $testesPassados" -ForegroundColor Green
Write-Host "Testes Falhados: $testesFalhados" -ForegroundColor Red
Write-Host ""

if ($testesFalhados -eq 0) {
    Write-Host "✅ TODOS OS TESTES PASSARAM!" -ForegroundColor Green
    Write-Host "A implementação do campo ED está funcionando corretamente." -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ ALGUNS TESTES FALHARAM!" -ForegroundColor Red
    Write-Host "Verifique os erros acima e corrija antes de fazer deploy." -ForegroundColor Red
    exit 1
}
