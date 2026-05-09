# Script para verificar e fechar processos usando a porta 8081
# Uso: .\kill-port-8081.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Verificando porta 8081..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar processos usando a porta 8081
$connections = netstat -ano | findstr :8081

if ($connections) {
    Write-Host "Processos encontrados usando a porta 8081:" -ForegroundColor Yellow
    Write-Host $connections
    Write-Host ""
    
    # Extrair PIDs únicos
    $pids = $connections | ForEach-Object {
        if ($_ -match '\s+(\d+)\s*$') {
            $matches[1]
        }
    } | Where-Object { $_ -ne "0" } | Select-Object -Unique
    
    if ($pids) {
        Write-Host "PIDs encontrados: $($pids -join ', ')" -ForegroundColor Yellow
        Write-Host ""
        
        # Mostrar informações dos processos
        foreach ($pid in $pids) {
            try {
                $process = Get-Process -Id $pid -ErrorAction Stop
                Write-Host "PID $pid - Processo: $($process.ProcessName) - Caminho: $($process.Path)" -ForegroundColor White
            } catch {
                Write-Host "PID $pid - Não foi possível obter informações do processo" -ForegroundColor Gray
            }
        }
        Write-Host ""
        
        # Perguntar se deseja matar os processos
        $response = Read-Host "Deseja encerrar estes processos? (S/N)"
        
        if ($response -eq "S" -or $response -eq "s") {
            foreach ($pid in $pids) {
                try {
                    Write-Host "Encerrando processo PID $pid..." -ForegroundColor Yellow
                    Stop-Process -Id $pid -Force -ErrorAction Stop
                    Write-Host "✓ Processo PID $pid encerrado com sucesso!" -ForegroundColor Green
                } catch {
                    Write-Host "✗ Erro ao encerrar processo PID $pid : $_" -ForegroundColor Red
                }
            }
            Write-Host ""
            Write-Host "Verificando novamente a porta 8081..." -ForegroundColor Cyan
            Start-Sleep -Seconds 2
            $newConnections = netstat -ano | findstr :8081
            if ($newConnections) {
                Write-Host "Ainda há processos usando a porta 8081:" -ForegroundColor Yellow
                Write-Host $newConnections
            } else {
                Write-Host "✓ Porta 8081 está livre!" -ForegroundColor Green
            }
        } else {
            Write-Host "Operação cancelada." -ForegroundColor Gray
        }
    } else {
        Write-Host "Nenhum PID válido encontrado." -ForegroundColor Gray
    }
} else {
    Write-Host "✓ Nenhum processo está usando a porta 8081." -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
