# Teste direto do endpoint de responsáveis
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "Teste do Endpoint de Responsáveis" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan

# Testar sem autenticação (deve retornar 401)
Write-Host "`n[1] Testando SEM autenticação (esperado: 401)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/mobile/responsaveis" -Method Get
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__) - $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
}

# Testar endpoint de health (não requer autenticação)
Write-Host "`n[2] Testando endpoint /api/mobile/health..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/mobile/health" -Method Get
    Write-Host "✓ Health check OK" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json -Depth 2)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Health check falhou: $($_.Exception.Message)" -ForegroundColor Red
}

# Testar endpoint de connection test
Write-Host "`n[3] Testando endpoint /api/mobile/test/connection..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/mobile/test/connection" -Method Get
    Write-Host "✓ Connection test OK" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json -Depth 2)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Connection test falhou: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "Conclusão:" -ForegroundColor Cyan
Write-Host "Se health e connection funcionam, o servidor está OK" -ForegroundColor White
Write-Host "O endpoint de responsáveis requer autenticação JWT" -ForegroundColor White
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
