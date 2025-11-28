# =====================================================
# Script de Inicialização - Servidor Mobile Spring Boot
# OTIMIZADO PARA 30 USUÁRIOS COM MELHOR DESEMPENHO
# =====================================================

param(
    [int]$Port = 8081,
    [string]$MinMemory = "1g",
    [string]$MaxMemory = "2g",
    [switch]$Debug,
    [switch]$Monitor
)

$Host.UI.RawUI.WindowTitle = "Servidor Mobile - 30 Usuarios"

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  SERVIDOR MOBILE - SPRING BOOT v4.0                            ║" -ForegroundColor Cyan
Write-Host "║  Otimizado para 30 usuarios simultaneos                        ║" -ForegroundColor Cyan
Write-Host "╠════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║  Porta: $Port                                                   ║" -ForegroundColor Cyan
Write-Host "║  Memoria: $MinMemory - $MaxMemory                                        ║" -ForegroundColor Cyan
Write-Host "║  Perfil: mobile                                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Verificar Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "[OK] Java encontrado: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Java nao encontrado! Instale o JDK 21+" -ForegroundColor Red
    exit 1
}

# Verificar JAR
$jarFile = "target\sistema-inventario-1.2.0.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "[AVISO] JAR nao encontrado. Compilando..." -ForegroundColor Yellow
    mvn clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERRO] Falha na compilacao!" -ForegroundColor Red
        exit 1
    }
}

# Criar diretório de logs
if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
}

# Mostrar configurações
Write-Host ""
Write-Host "=== Configuracoes de Performance ===" -ForegroundColor Yellow
Write-Host "  Pool de Conexoes: 15 (max)" -ForegroundColor White
Write-Host "  Threads Tomcat: 60 (max)" -ForegroundColor White
Write-Host "  Conexoes HTTP: 200 (max)" -ForegroundColor White
Write-Host "  Batch Size: 25" -ForegroundColor White
Write-Host "  Cache L2: Habilitado" -ForegroundColor White
Write-Host ""

# Parâmetros JVM
$jvmArgs = @(
    "-Xms$MinMemory",
    "-Xmx$MaxMemory",
    "-XX:MaxMetaspaceSize=256m",
    "-XX:+UseG1GC",
    "-XX:MaxGCPauseMillis=100",
    "-XX:G1HeapRegionSize=16m",
    "-XX:+ParallelRefProcEnabled",
    "-XX:+UseStringDeduplication",
    "-XX:+UseCompressedOops",
    "-XX:+HeapDumpOnOutOfMemoryError",
    "-XX:HeapDumpPath=logs/",
    "-Dfile.encoding=UTF-8",
    "-Dspring.profiles.active=mobile",
    "-Dserver.port=$Port"
)

# Adicionar debug se solicitado
if ($Debug) {
    $jvmArgs += "-Xdebug"
    $jvmArgs += "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
    Write-Host "[DEBUG] Modo debug habilitado na porta 5005" -ForegroundColor Magenta
}

# Montar comando
$jvmArgsString = $jvmArgs -join " "
$command = "java $jvmArgsString -jar `"$jarFile`""

Write-Host "[INFO] Iniciando servidor..." -ForegroundColor Green
Write-Host "[CMD] $command" -ForegroundColor DarkGray
Write-Host ""

# Iniciar servidor
if ($Monitor) {
    # Iniciar em background e monitorar
    $process = Start-Process -FilePath "java" -ArgumentList ($jvmArgs + @("-jar", $jarFile)) -PassThru -NoNewWindow
    
    Write-Host "[INFO] Servidor iniciado (PID: $($process.Id))" -ForegroundColor Green
    Write-Host "[INFO] Monitorando... (Ctrl+C para parar)" -ForegroundColor Yellow
    Write-Host ""
    
    # Loop de monitoramento
    while (-not $process.HasExited) {
        $cpu = (Get-Process -Id $process.Id -ErrorAction SilentlyContinue).CPU
        $mem = [math]::Round((Get-Process -Id $process.Id -ErrorAction SilentlyContinue).WorkingSet64 / 1MB, 2)
        
        Write-Host "`r[MONITOR] CPU: $cpu | Memoria: ${mem}MB | Status: Running    " -NoNewline -ForegroundColor Cyan
        Start-Sleep -Seconds 5
    }
    
    Write-Host ""
    Write-Host "[INFO] Servidor encerrado." -ForegroundColor Yellow
} else {
    # Iniciar em foreground
    Invoke-Expression $command
}
