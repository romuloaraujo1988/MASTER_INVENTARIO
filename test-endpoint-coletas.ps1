# Script para testar o endpoint de coletas
Write-Host "Testando endpoint de coletas..." -ForegroundColor Cyan

# Testar porta 8080
Write-Host "`nTestando porta 8080..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/mobile/coletas/all" -Method Get -ErrorAction Stop
    Write-Host "✓ Servidor respondeu na porta 8080" -ForegroundColor Green
    Write-Host "Total de coletas: $($response.data.Count)" -ForegroundColor Green
    
    if ($response.data.Count -gt 0) {
        Write-Host "`nPrimeira coleta:" -ForegroundColor Cyan
        $primeiraColeta = $response.data[0]
        Write-Host "  ID: $($primeiraColeta.id)"
        Write-Host "  Número Patrimônio: $($primeiraColeta.numeroPatrimonio)"
        Write-Host "  Localização Encontrada: $($primeiraColeta.localizacaoEncontrada)"
        Write-Host "  Nome Sala: $($primeiraColeta.nomeSala)"
        Write-Host "  Data Coleta: $($primeiraColeta.dataColeta)"
    }
} catch {
    Write-Host "✗ Servidor não respondeu na porta 8080" -ForegroundColor Red
}

# Testar porta 8081
Write-Host "`nTestando porta 8081..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/mobile/coletas/all" -Method Get -ErrorAction Stop
    Write-Host "✓ Servidor respondeu na porta 8081" -ForegroundColor Green
    Write-Host "Total de coletas: $($response.data.Count)" -ForegroundColor Green
    
    if ($response.data.Count -gt 0) {
        Write-Host "`nPrimeira coleta:" -ForegroundColor Cyan
        $primeiraColeta = $response.data[0]
        Write-Host "  ID: $($primeiraColeta.id)"
        Write-Host "  Número Patrimônio: $($primeiraColeta.numeroPatrimonio)"
        Write-Host "  Localização Encontrada: $($primeiraColeta.localizacaoEncontrada)"
        Write-Host "  Nome Sala: $($primeiraColeta.nomeSala)"
        Write-Host "  Data Coleta: $($primeiraColeta.dataColeta)"
    }
} catch {
    Write-Host "✗ Servidor não respondeu na porta 8081" -ForegroundColor Red
}

Write-Host "`n" -ForegroundColor Cyan
Write-Host "IMPORTANTE: Se o servidor não respondeu, você precisa reiniciá-lo!" -ForegroundColor Yellow
Write-Host "Execute: cd target\mobile-server && start-mobile-server.bat" -ForegroundColor Yellow
