# Testar resposta JSON do servidor
Write-Host "Testando resposta JSON do servidor..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/mobile/coletas/all" -Method Get
    
    if ($response.data -and $response.data.Count -gt 0) {
        $primeira = $response.data[0]
        
        Write-Host "`nPrimeira coleta no JSON:" -ForegroundColor Yellow
        Write-Host "ID: $($primeira.id)"
        Write-Host "Número: $($primeira.numeroPatrimonio)"
        Write-Host "Descrição: $($primeira.descricaoPatrimonio)"
        
        Write-Host "`n=== CAMPOS DE LOCALIZAÇÃO ===" -ForegroundColor Cyan
        Write-Host "localizacaoEncontrada: '$($primeira.localizacaoEncontrada)'" -ForegroundColor $(if ($primeira.localizacaoEncontrada) { "Green" } else { "Red" })
        Write-Host "nomeSala: '$($primeira.nomeSala)'" -ForegroundColor $(if ($primeira.nomeSala) { "Green" } else { "Red" })
        Write-Host "localizacaoAtual: '$($primeira.localizacaoAtual)'" -ForegroundColor $(if ($primeira.localizacaoAtual) { "Green" } else { "Red" })
        
        Write-Host "`n=== JSON COMPLETO DA PRIMEIRA COLETA ===" -ForegroundColor Cyan
        $primeira | ConvertTo-Json -Depth 3
        
        if (-not $primeira.localizacaoEncontrada) {
            Write-Host "`n❌ PROBLEMA: localizacaoEncontrada está NULL no JSON!" -ForegroundColor Red
            Write-Host "O servidor não foi reiniciado ou está usando JAR antigo" -ForegroundColor Red
        } else {
            Write-Host "`n✅ localizacaoEncontrada está presente no JSON!" -ForegroundColor Green
        }
    } else {
        Write-Host "Nenhuma coleta encontrada" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Erro ao conectar ao servidor: $($_.Exception.Message)" -ForegroundColor Red
}
