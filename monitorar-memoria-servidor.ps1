# ============================================================
# MONITORAMENTO DE MEMÓRIA DO SERVIDOR MOBILE
# ============================================================
# Este script monitora o uso de memória do servidor Java
# e alerta quando está próximo do limite
# ============================================================

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "MONITORAMENTO DE MEMÓRIA - SERVIDOR MOBILE" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Configurações
$intervaloSegundos = 10
$limiteAlertaMB = 800  # Alertar quando passar de 800MB

Write-Host "Configurações:" -ForegroundColor Yellow
Write-Host "  - Intervalo de verificação: ${intervaloSegundos}s"
Write-Host "  - Limite de alerta: ${limiteAlertaMB}MB"
Write-Host ""
Write-Host "Pressione Ctrl+C para parar o monitoramento"
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$contador = 0

while ($true) {
    $contador++
    
    # Buscar processo Java do servidor mobile
    $javaProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | 
                   Where-Object { $_.CommandLine -like "*mobile*" -or $_.CommandLine -like "*8081*" }
    
    if ($javaProcess -eq $null) {
        # Tentar buscar qualquer processo Java
        $javaProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | Select-Object -First 1
    }
    
    if ($javaProcess -ne $null) {
        $memoriaMB = [math]::Round($javaProcess.WorkingSet64 / 1MB, 2)
        $memoriaPrivadaMB = [math]::Round($javaProcess.PrivateMemorySize64 / 1MB, 2)
        $cpuPercent = [math]::Round($javaProcess.CPU, 2)
        $threads = $javaProcess.Threads.Count
        
        $timestamp = Get-Date -Format "HH:mm:ss"
        
        # Determinar cor baseado no uso de memória
        if ($memoriaMB -gt $limiteAlertaMB) {
            $cor = "Red"
            $status = "⚠️ ALERTA"
        } elseif ($memoriaMB -gt ($limiteAlertaMB * 0.7)) {
            $cor = "Yellow"
            $status = "⚡ ATENÇÃO"
        } else {
            $cor = "Green"
            $status = "✅ OK"
        }
        
        Write-Host "[$timestamp] " -NoNewline
        Write-Host "$status " -ForegroundColor $cor -NoNewline
        Write-Host "| Memória: " -NoNewline
        Write-Host "${memoriaMB}MB" -ForegroundColor $cor -NoNewline
        Write-Host " | Privada: ${memoriaPrivadaMB}MB | Threads: $threads | CPU: ${cpuPercent}s"
        
        # Alerta especial se memória muito alta
        if ($memoriaMB -gt 900) {
            Write-Host ""
            Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Red
            Write-Host "║  ⚠️  MEMÓRIA CRÍTICA! Considere reiniciar o servidor!          ║" -ForegroundColor Red
            Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Red
            Write-Host ""
        }
        
    } else {
        $timestamp = Get-Date -Format "HH:mm:ss"
        Write-Host "[$timestamp] ❌ Servidor Java não encontrado" -ForegroundColor Red
    }
    
    # Mostrar estatísticas do sistema a cada 6 verificações (1 minuto)
    if ($contador % 6 -eq 0) {
        Write-Host ""
        Write-Host "--- Estatísticas do Sistema ---" -ForegroundColor Cyan
        
        $sistemaMemoria = Get-CimInstance Win32_OperatingSystem
        $totalMemoriaMB = [math]::Round($sistemaMemoria.TotalVisibleMemorySize / 1KB, 0)
        $livreMemoriaMB = [math]::Round($sistemaMemoria.FreePhysicalMemory / 1KB, 0)
        $usadaMemoriaMB = $totalMemoriaMB - $livreMemoriaMB
        $percentualUso = [math]::Round(($usadaMemoriaMB / $totalMemoriaMB) * 100, 1)
        
        Write-Host "Sistema: ${usadaMemoriaMB}MB / ${totalMemoriaMB}MB (${percentualUso}% em uso)"
        Write-Host ""
    }
    
    Start-Sleep -Seconds $intervaloSegundos
}
