# Script para testar endpoint de responsáveis
# Requer que o servidor mobile esteja rodando

$baseUrl = "http://localhost:8081"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Teste do Endpoint de Responsáveis" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Fazer login para obter token
Write-Host "1. Fazendo login..." -ForegroundColor Yellow

$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/mobile/auth/login" `
        -Method POST `
        -Body $loginBody `
        -ContentType "application/json" `
        -ErrorAction Stop
    
    $token = $loginResponse.data.token
    Write-Host "✓ Login realizado com sucesso!" -ForegroundColor Green
    Write-Host "Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Erro no login: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Tentando com usuário 'coletor'..." -ForegroundColor Yellow
    
    $loginBody = @{
        username = "coletor"
        password = "coletor123"
    } | ConvertTo-Json
    
    try {
        $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/mobile/auth/login" `
            -Method POST `
            -Body $loginBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        $token = $loginResponse.data.token
        Write-Host "✓ Login realizado com sucesso!" -ForegroundColor Green
        Write-Host "Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
        Write-Host ""
    } catch {
        Write-Host "✗ Erro no login: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "Servidor pode não estar rodando ou credenciais incorretas" -ForegroundColor Red
        exit 1
    }
}

# 2. Testar endpoint de responsáveis
Write-Host "2. Testando endpoint /api/mobile/responsaveis..." -ForegroundColor Yellow

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $responsaveisResponse = Invoke-RestMethod -Uri "$baseUrl/api/mobile/responsaveis" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Host "✓ Endpoint funcionando!" -ForegroundColor Green
    Write-Host ""
    
    # Exibir resultado
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Resultado:" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
    Write-Host "Status: $($responsaveisResponse.status)" -ForegroundColor Green
    Write-Host "Mensagem: $($responsaveisResponse.message)" -ForegroundColor Green
    Write-Host "Quantidade de responsáveis: $($responsaveisResponse.data.Count)" -ForegroundColor Green
    Write-Host ""
    
    if ($responsaveisResponse.data.Count -gt 0) {
        Write-Host "Primeiros 10 responsáveis:" -ForegroundColor Yellow
        Write-Host "----------------------------------------" -ForegroundColor Gray
        
        $count = [Math]::Min(10, $responsaveisResponse.data.Count)
        for ($i = 0; $i -lt $count; $i++) {
            $resp = $responsaveisResponse.data[$i]
            Write-Host "$($i+1). ID: $($resp.id) - Nome: $($resp.nome)" -ForegroundColor White
            if ($resp.nomeSetor) {
                Write-Host "   Setor: $($resp.nomeSetor)" -ForegroundColor Gray
            }
        }
        
        Write-Host ""
        Write-Host "✓ Endpoint retornando dados corretamente!" -ForegroundColor Green
    } else {
        Write-Host "⚠ Endpoint retornou 0 responsáveis!" -ForegroundColor Red
        Write-Host "Verifique os logs do servidor para mais detalhes" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "✗ Erro ao chamar endpoint: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Detalhes do erro:" -ForegroundColor Yellow
    Write-Host $_.Exception -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Teste concluído" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
