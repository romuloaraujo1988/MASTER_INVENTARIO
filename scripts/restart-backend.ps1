# ============================================
# Script para Reiniciar Backend
# Data: 16/11/2024
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  REINICIANDO BACKEND" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Encontrar processo do servidor
Write-Host "1. Procurando processo do servidor..." -ForegroundColor Yellow

$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
$serverProcess = $null

foreach ($proc in $javaProcesses) {
    try {
        $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($proc.Id)").CommandLine
        if ($cmdLine -like "*sistema-inventario*" -or $cmdLine -like "*MobileApiApplication*") {
            $serverProcess = $proc
            Write-Host "   ✅ Processo encontrado: PID $($proc.Id)" -ForegroundColor Green
            break
        }
    } catch {
        # Ignorar erros de acesso
    }
}

if ($serverProcess) {
    Write-Host ""
    Write-Host "2. Parando servidor..." -ForegroundColor Yellow
    Stop-Process -Id $serverProcess.Id -Force
    Start-Sleep -Seconds 3
    Write-Host "   ✅ Servidor parado" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Servidor não está rodando" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "3. Iniciando servidor..." -ForegroundColor Yellow
Write-Host "   Executando: java -jar target/sistema-inventario-2.0.0-exec.jar" -ForegroundColor Gray
Write-Host ""

# Iniciar servidor em background
Start-Process -FilePath "java" `
    -ArgumentList "-jar", "target/sistema-inventario-2.0.0-exec.jar" `
    -WindowStyle Normal `
    -WorkingDirectory (Get-Location)

Write-Host "   ✅ Servidor iniciado em nova janela" -ForegroundColor Green
Write-Host ""
Write-Host "Aguardando 10 segundos para servidor inicializar..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ BACKEND REINICIADO" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Servidor disponível em:" -ForegroundColor Cyan
Write-Host "   http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "🧪 Testar endpoint:" -ForegroundColor Cyan
Write-Host "   curl http://localhost:8080/api/mobile/inventario/ativo" -ForegroundColor White
Write-Host ""
