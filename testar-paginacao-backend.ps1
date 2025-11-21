# Script para testar paginação do backend
# IMPORTANTE: Servidor mobile roda na porta 8081

$PORT = 8081
$BASE_URL = "http://localhost:$PORT"

Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "TESTE DE PAGINAÇÃO - BACKEND" -ForegroundColor Cyan
Write-Host "Porta: $PORT" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Verificar se servidor está rodando
Write-Host "1. Verificando servidor na porta $PORT..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "   ✓ Servidor está UP na porta $PORT" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Servidor não está respondendo na porta $PORT" -ForegroundColor Red
    Write-Host "   Inicie o servidor com: .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mobile" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "2. Testando endpoint de patrimônios (sem autenticação)..." -ForegroundColor Yellow
Write-Host "   Endpoint: GET /api/mobile/patrimonio?page=0&size=10" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/api/mobile/patrimonio?page=0&size=10" -Method GET -TimeoutSec 10
    
    if ($response.StatusCode -eq 401) {
        Write-Host "   ⚠ Endpoint requer autenticação (esperado)" -ForegroundColor Yellow
        Write-Host "   Status: 401 Unauthorized" -ForegroundColor Gray
    } elseif ($response.StatusCode -eq 200) {
        Write-Host "   ✓ Endpoint respondeu com sucesso" -ForegroundColor Green
        $data = $response.Content | ConvertFrom-Json
        Write-Host "   Success: $($data.success)" -ForegroundColor Gray
        Write-Host "   Data Count: $($data.data.Count)" -ForegroundColor Gray
    }
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "   ⚠ Endpoint requer autenticação (esperado)" -ForegroundColor Yellow
    } else {
        Write-Host "   ✗ Erro: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "3. Verificando logs do servidor..." -ForegroundColor Yellow

$logFile = "logs\sistema-inventario.log"
if (Test-Path $logFile) {
    $recentLogs = Get-Content $logFile -Tail 20 | Select-String "LISTANDO PATRIMÔNIOS|patrimônios retornados"
    
    if ($recentLogs) {
        Write-Host "   ✓ Logs encontrados:" -ForegroundColor Green
        $recentLogs | ForEach-Object {
            Write-Host "   $_" -ForegroundColor Gray
        }
    } else {
        Write-Host "   ⚠ Nenhum log recente de paginação" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ⚠ Arquivo de log não encontrado" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PRÓXIMO PASSO:" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Abrir o app Android" -ForegroundColor White
Write-Host "2. Fazer login (admin/admin123)" -ForegroundColor White
Write-Host "3. Menu → Dados → Sincronização" -ForegroundColor White
Write-Host "4. Clicar 'Sincronizar Agora'" -ForegroundColor White
Write-Host "5. Monitorar logs: .\monitorar-sync-simples.bat" -ForegroundColor White
Write-Host ""
Write-Host "Expectativa: 10.809 patrimônios + 108 salas em ~2 minutos" -ForegroundColor Green
Write-Host ""
