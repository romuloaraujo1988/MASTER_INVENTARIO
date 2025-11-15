# Script para testar o endpoint de coleta

$url = "http://localhost:8081/inventario/api/mobile/coletas"
$token = "eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYWNjZXNzIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NjMxNDEwNTEsImV4cCI6MTc2MzIyNzQ1MX0.aPwvagYMuFdyOwFcewJDK4FdJrrn8_PH6lVv1zmNc2FJ5b8drNwatvOCCi8zwSO1chWel8TLdzMqvHdvvVV3zQ"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$body = @{
    numeroPatrimonio = "3250"
    idInventario = 2
    usuarioId = 1
    idSala = 25
    localizacaoEncontrada = "Área Externa"
    estadoEncontrado = "BOM"
    observacaoColeta = "Teste via PowerShell"
    dataColeta = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss.fff")
    latitude = $null
    longitude = $null
    fotoPatrimonio = $null
    semEtiqueta = $false
    descricaoItemSemEtiqueta = $null
    categoriaItemSemEtiqueta = $null
    deviceId = "PowerShell-Test"
    appVersion = "1.2"
    divergencia = $false
    motivoDivergencia = $null
} | ConvertTo-Json

Write-Host "Testando endpoint: $url"
Write-Host "Body: $body"

try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $body -ContentType "application/json"
    Write-Host "✅ Sucesso!" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 10)
} catch {
    Write-Host "❌ Erro!" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "Mensagem: $($_.Exception.Message)"
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalhes: $($_.ErrorDetails.Message)"
    }
}
