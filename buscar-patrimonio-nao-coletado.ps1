# Buscar patrimônio não coletado

$url = "http://localhost:8081/inventario/api/mobile/patrimonios/nao-coletados"
$token = "eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYWNjZXNzIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NjMxNDEwNTEsImV4cCI6MTc2MzIyNzQ1MX0.aPwvagYMuFdyOwFcewJDK4FdJrrn8_PH6lVv1zmNc2FJ5b8drNwatvOCCi8zwSO1chWel8TLdzMqvHdvvVV3zQ"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $response = Invoke-RestMethod -Uri "$url`?idInventario=2&page=0&size=5" -Method Get -Headers $headers
    Write-Host "✅ Patrimônios não coletados:" -ForegroundColor Green
    $response.data.content | ForEach-Object {
        Write-Host "  - Número: $($_.numero) | Descrição: $($_.descricao.Substring(0, [Math]::Min(50, $_.descricao.Length)))..."
    }
    
    if ($response.data.content.Count -gt 0) {
        $primeiro = $response.data.content[0]
        Write-Host "`n📋 Use este patrimônio para testar:" -ForegroundColor Cyan
        Write-Host "   Número: $($primeiro.numero)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Erro!" -ForegroundColor Red
    Write-Host $_.Exception.Message
}
