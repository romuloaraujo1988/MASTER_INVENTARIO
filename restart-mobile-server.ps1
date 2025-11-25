# Script para reiniciar o servidor mobile
Write-Host "Reiniciando servidor mobile..." -ForegroundColor Cyan

# 1. Parar o servidor atual
Write-Host "`n1. Parando servidor na porta 8081..." -ForegroundColor Yellow
$process = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($process) {
    Write-Host "   Processo encontrado: PID $process" -ForegroundColor Gray
    Stop-Process -Id $process -Force
    Write-Host "   ✓ Servidor parado" -ForegroundColor Green
    Start-Sleep -Seconds 2
} else {
    Write-Host "   Nenhum processo encontrado na porta 8081" -ForegroundColor Gray
}

# 2. Iniciar o novo servidor com o código corrigido
Write-Host "`n2. Iniciando servidor com código corrigido..." -ForegroundColor Yellow
$jarPath = "target\mobile-server\sistema-inventario-2.0.0.jar"

if (Test-Path $jarPath) {
    Write-Host "   JAR encontrado: $jarPath" -ForegroundColor Gray
    
    # Iniciar em background
    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = "java"
    $processInfo.Arguments = "-jar $jarPath --spring.profiles.active=mobile --server.port=8081"
    $processInfo.UseShellExecute = $false
    $processInfo.CreateNoWindow = $false
    
    $process = [System.Diagnostics.Process]::Start($processInfo)
    
    Write-Host "   ✓ Servidor iniciado (PID: $($process.Id))" -ForegroundColor Green
    Write-Host "   Aguardando inicialização..." -ForegroundColor Gray
    Start-Sleep -Seconds 10
    
    # 3. Verificar se está respondendo
    Write-Host "`n3. Verificando se o servidor está respondendo..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8081/api/mobile/coletas/all" -Method Get -TimeoutSec 5
        Write-Host "   ✓ Servidor respondendo!" -ForegroundColor Green
        Write-Host "   Total de coletas: $($response.data.Count)" -ForegroundColor Green
        
        if ($response.data.Count -gt 0) {
            $primeira = $response.data[0]
            Write-Host "`n   Primeira coleta (verificando correção):" -ForegroundColor Cyan
            Write-Host "     ID: $($primeira.id)" -ForegroundColor Gray
            Write-Host "     Número: $($primeira.numeroPatrimonio)" -ForegroundColor Gray
            Write-Host "     Localização Encontrada: $($primeira.localizacaoEncontrada)" -ForegroundColor $(if ($primeira.localizacaoEncontrada) { "Green" } else { "Red" })
            Write-Host "     Nome Sala: $($primeira.nomeSala)" -ForegroundColor Gray
            
            if ($primeira.localizacaoEncontrada) {
                Write-Host "`n   ✓✓✓ CORREÇÃO APLICADA COM SUCESSO! ✓✓✓" -ForegroundColor Green
                Write-Host "   O campo localizacaoEncontrada está sendo retornado!" -ForegroundColor Green
            } else {
                Write-Host "`n   ✗✗✗ PROBLEMA: localizacaoEncontrada está NULL ✗✗✗" -ForegroundColor Red
                Write-Host "   O servidor pode não ter sido reiniciado corretamente" -ForegroundColor Red
            }
        }
    } catch {
        Write-Host "   ✗ Servidor não respondeu" -ForegroundColor Red
        Write-Host "   Erro: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} else {
    Write-Host "   ✗ JAR não encontrado: $jarPath" -ForegroundColor Red
    Write-Host "   Execute: mvnw.cmd clean package -P thin-jar -DskipTests" -ForegroundColor Yellow
}

Write-Host "`n" -ForegroundColor Cyan
Write-Host "Servidor reiniciado!" -ForegroundColor Green
Write-Host "Agora teste no app Android." -ForegroundColor Yellow
