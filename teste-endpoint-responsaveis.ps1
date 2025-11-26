# Teste do endpoint de responsáveis
Write-Host "Testando endpoint de responsáveis..." -ForegroundColor Cyan

# Fazer login primeiro para obter token
$loginBody = @{
    username = "admin"
    password = "admin"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/mobile/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.data.accessToken
    
    Write-Host "✓ Login realizado com sucesso" -ForegroundColor Green
    Write-Host "Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
    
    # Testar endpoint de responsáveis
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    
    Write-Host "`nTestando GET /api/mobile/responsaveis..." -ForegroundColor Cyan
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/mobile/responsaveis" -Method Get -Headers $headers
    
    Write-Host "✓ Resposta recebida:" -ForegroundColor Green
    Write-Host "Success: $($response.success)" -ForegroundColor Yellow
    Write-Host "Message: $($response.message)" -ForegroundColor Yellow
    Write-Host "Total de responsáveis: $($response.data.Count)" -ForegroundColor Yellow
    
    if ($response.data.Count -gt 0) {
        Write-Host "`nPrimeiros 5 responsáveis:" -ForegroundColor Cyan
        $response.data | Select-Object -First 5 | ForEach-Object {
            Write-Host "  - ID: $($_.id), Nome: $($_.nome), Ativo: $($_.ativo)" -ForegroundColor White
        }
    } else {
        Write-Host "`n⚠️ PROBLEMA: Nenhum responsável retornado!" -ForegroundColor Red
        Write-Host "Verificar logs do servidor para mais detalhes" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Erro ao testar endpoint:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body: $responseBody" -ForegroundColor Yellow
    }
}
