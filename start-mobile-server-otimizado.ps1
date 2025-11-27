# =====================================================
# Script de Inicialização OTIMIZADO - Servidor Mobile
# Configurado para ~15 usuários e baixo consumo de memória
# =====================================================

Write-Host "========================================"
Write-Host " Servidor Mobile - Modo OTIMIZADO"
Write-Host " Configurado para ~15 usuarios"
Write-Host "========================================"
Write-Host ""

# Verificar Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "[OK] Java encontrado: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Java nao encontrado!" -ForegroundColor Red
    exit 1
}

# Matar processos na porta 8081
Write-Host ""
Write-Host "[1/3] Liberando porta 8081..." -ForegroundColor Yellow
$processes = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
if ($processes) {
    foreach ($proc in $processes) {
        Stop-Process -Id $proc.OwningProcess -Force -ErrorAction SilentlyContinue
    }
    Start-Sleep -Seconds 2
    Write-Host "      Porta liberada" -ForegroundColor Green
} else {
    Write-Host "      Porta ja estava livre" -ForegroundColor Green
}

# Configurações JVM OTIMIZADAS
$JAVA_OPTS = @(
    "-Xms256m",                              # Memória inicial: 256MB
    "-Xmx512m",                              # Memória máxima: 512MB
    "-XX:+UseG1GC",                          # G1 Garbage Collector
    "-XX:MaxGCPauseMillis=100",              # Pausas curtas do GC
    "-XX:+UseStringDeduplication",           # Economiza memória com strings
    "-XX:+ParallelRefProcEnabled",           # Processamento paralelo
    "-XX:InitiatingHeapOccupancyPercent=45", # Inicia GC mais cedo
    "-XX:G1HeapRegionSize=4m",               # Regiões menores do heap
    "-XX:+DisableExplicitGC",                # Desabilita System.gc()
    "-Dcom.sun.management.jmxremote=false",  # Desabilita JMX
    "-Djava.awt.headless=true"               # Modo headless
) -join " "

Write-Host ""
Write-Host "[2/3] Configuracoes JVM:" -ForegroundColor Yellow
Write-Host "      Memoria inicial: 256MB"
Write-Host "      Memoria maxima:  512MB"
Write-Host "      GC: G1GC otimizado"

Write-Host ""
Write-Host "[3/3] Iniciando servidor mobile..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Servidor iniciando na porta 8081..."
Write-Host "Pressione Ctrl+C para parar"
Write-Host ""

# Iniciar servidor
& .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mobile" "-Dspring-boot.run.jvmArguments=$JAVA_OPTS"
