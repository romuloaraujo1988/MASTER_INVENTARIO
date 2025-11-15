# Script para testar coleta de patrimônio NÃO coletado

$url = "http://localhost:8081/inventario/api/mobile/coletas"
$token = "eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYWNjZXNzIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NjMxNDEwNTEsImV4cCI6MTc2MzIyNzQ1MX0.aPwvagYMuFdyOwFcewJDK4FdJrrn8_PH6lVv1zmNc2FJ5b8drNwatvOCCi8zwSO1chWel8TLdzMqvHdvvVV3zQ"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Usar patrimônio 3252 que não foi coletado
$body = @{
    numeroPatrimonio = "3252"
    idInventario = 2
    usuarioId = 1
    idSala = 25
    localizacaoEncontrada = "Área Externa"
    estadoEncontrado = "BOM"
    observacaoColeta = "Teste patrimônio 3252"
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

Write-Host "Testando coleta de patrimônio 3252 (não coletado)"
Write-Host "URL: $url"

try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $body -ContentType "application/json"
    Write-Host "✅ Sucesso!" -ForegroundColor Green
    Write-Host "ID da coleta: $($response.data.id)"
    Write-Host "Patrimônio: $($response.data.numeroPatrimonio)"
    Write-Host "Descrição: $($response.data.descricaoPatrimonio)"
} catch {
    Write-Host "❌ Erro!" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "Mensagem: $($_.Exception.Message)"
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalhes: $($_.ErrorDetails.Message)"
    }
}
