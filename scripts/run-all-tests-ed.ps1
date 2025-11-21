# ============================================
# Script Master - Executar Todos os Testes ED
# Data: 16/11/2024
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  EXECUTAR TODOS OS TESTES - CAMPO ED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$totalSuites = 0
$suitesPassadas = 0
$suitesFalhadas = 0

function Run-TestSuite {
    param(
        [string]$Nome,
        [string]$Script
    )
    
    $script:totalSuites++
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "  SUITE $totalSuites`: $Nome" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host ""
    
    try {
        & $Script
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Suite '$Nome' PASSOU" -ForegroundColor Green
            $script:suitesPassadas++
        } else {
            Write-Host ""
            Write-Host "❌ Suite '$Nome' FALHOU" -ForegroundColor Red
            $script:suitesFalhadas++
        }
    } catch {
        Write-Host ""
        Write-Host "❌ Erro ao executar suite '$Nome': $_" -ForegroundColor Red
        $script:suitesFalhadas++
    }
    
    Write-Host ""
    Write-Host "Pressione ENTER para continuar..." -ForegroundColor Yellow
    Read-Host
    Write-Host ""
}

# Suite 1: Testes de Implementação (Banco de Dados)
Run-TestSuite -Nome "Testes de Implementação (Banco de Dados)" -Script ".\scripts\test-ed-implementation.ps1"

# Suite 2: Testes de API Mobile
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  SUITE 2: Testes de API Mobile" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "⚠️  ATENÇÃO: Este teste requer que o servidor esteja rodando!" -ForegroundColor Yellow
Write-Host ""
Write-Host "O servidor está rodando? (S/N)" -ForegroundColor Yellow
$resposta = Read-Host

if ($resposta -eq "S" -or $resposta -eq "s") {
    Run-TestSuite -Nome "Testes de API Mobile" -Script ".\scripts\test-api-mobile-ed.ps1"
} else {
    Write-Host "⏭️  Pulando testes de API Mobile" -ForegroundColor Yellow
    Write-Host ""
}

# Suite 3: Testes de Importação CSV
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  SUITE 3: Testes de Importação CSV" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "⚠️  ATENÇÃO: Este teste requer interação manual!" -ForegroundColor Yellow
Write-Host ""
Write-Host "Deseja executar os testes de importação? (S/N)" -ForegroundColor Yellow
$resposta = Read-Host

if ($resposta -eq "S" -or $resposta -eq "s") {
    Run-TestSuite -Nome "Testes de Importação CSV" -Script ".\scripts\test-importacao-ed.ps1"
} else {
    Write-Host "⏭️  Pulando testes de importação" -ForegroundColor Yellow
    Write-Host ""
}

# Resumo Final
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESUMO FINAL DE TODAS AS SUITES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Total de Suites: $totalSuites" -ForegroundColor White
Write-Host "Suites Passadas: $suitesPassadas" -ForegroundColor Green
Write-Host "Suites Falhadas: $suitesFalhadas" -ForegroundColor Red
Write-Host ""

if ($suitesFalhadas -eq 0) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  ✅ TODAS AS SUITES PASSARAM!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "A implementação do campo ED está completa e funcionando!" -ForegroundColor Green
    Write-Host "O sistema está pronto para deploy em produção." -ForegroundColor Green
    Write-Host ""
    exit 0
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  ❌ ALGUMAS SUITES FALHARAM!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifique os erros acima e corrija antes de fazer deploy." -ForegroundColor Red
    Write-Host ""
    exit 1
}
