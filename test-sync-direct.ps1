# Script para testar sincronização diretamente
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Teste de Sincronização - Diagnóstico" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar se servidor está rodando
Write-Host "1. Testando servidor..." -ForegroundColor Yellow
$serverTest = curl -X GET "http://localhost:8081/inventario/api/mobile/auth/health" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Servidor está rodando" -ForegroundColor Green
} else {
    Write-Host "   ✗ Servidor NÃO está respondendo" -ForegroundColor Red
    exit 1
}

# 2. Fazer login e obter token
Write-Host ""
Write-Host "2. Fazendo login..." -ForegroundColor Yellow
$loginBody = @{username='admin'; password='admin123'} | ConvertTo-Json
$loginResponse = curl -X POST "http://localhost:8081/inventario/api/mobile/auth/login" `
    -H "Content-Type: application/json" `
    -d $loginBody 2>&1 | Out-String

if ($loginResponse -match '"accessToken":"([^"]+)"') {
    $token = $matches[1]
    Write-Host "   ✓ Login bem-sucedido" -ForegroundColor Green
    Write-Host "   Token: $($token.Substring(0, 50))..." -ForegroundColor Gray
} else {
    Write-Host "   ✗ Falha no login" -ForegroundColor Red
    exit 1
}

# 3. Testar endpoint de patrimônios
Write-Host ""
Write-Host "3. Testando endpoint de patrimônios..." -ForegroundColor Yellow
$patrimoniosResponse = curl -X GET "http://localhost:8081/inventario/api/mobile/patrimonio" `
    -H "Authorization: Bearer $token" 2>&1 | Out-String

if ($patrimoniosResponse -match '"success":true') {
    if ($patrimoniosResponse -match '"data":\[') {
        $count = ([regex]::Matches($patrimoniosResponse, '"id":')).Count
        Write-Host "   ✓ Endpoint funcionando - $count patrimônios retornados" -ForegroundColor Green
    } else {
        Write-Host "   ⚠ Endpoint retornou sucesso mas sem dados" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ✗ Endpoint falhou" -ForegroundColor Red
    Write-Host "   Resposta: $($patrimoniosResponse.Substring(0, 200))" -ForegroundColor Gray
}

# 4. Testar endpoint de salas
Write-Host ""
Write-Host "4. Testando endpoint de salas..." -ForegroundColor Yellow
$salasResponse = curl -X GET "http://localhost:8081/inventario/api/mobile/salas" `
    -H "Authorization: Bearer $token" 2>&1 | Out-String

if ($salasResponse -match '"success":true') {
    if ($salasResponse -match '"data":\[') {
        $count = ([regex]::Matches($salasResponse, '"id":')).Count
        Write-Host "   ✓ Endpoint funcionando - $count salas retornadas" -ForegroundColor Green
    } else {
        Write-Host "   ⚠ Endpoint retornou sucesso mas sem dados" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ✗ Endpoint falhou" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Diagnóstico Completo!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "PRÓXIMO PASSO:" -ForegroundColor Yellow
Write-Host "Verificar logs do app Android para ver se está usando o token correto" -ForegroundColor White
Write-Host ""
Write-Host "Execute: adb logcat -s 'SyncRepository:*' 'NetworkModule:*'" -ForegroundColor Cyan
