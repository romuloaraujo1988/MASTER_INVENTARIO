# Servidor Mobile - Sistema de Inventario
# Processo Independente

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Servidor Mobile - Sistema de Inventario                       ║" -ForegroundColor Cyan
Write-Host "║  Processo Independente                                         ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Configuracoes de memoria
$javaOpts = @(
    "-Xms256m",
    "-Xmx1g",
    "-XX:MaxMetaspaceSize=192m",
    "-XX:+UseG1GC",
    "-XX:MaxGCPauseMillis=100",
    "-XX:InitiatingHeapOccupancyPercent=45",
    "-XX:+UseStringDeduplication",
    "-XX:+UseCompressedOops",
    "-XX:+HeapDumpOnOutOfMemoryError",
    "-XX:HeapDumpPath=logs/",
    "-XX:+ExitOnOutOfMemoryError"
)

# Configuracoes do servidor
$serverOpts = @(
    "--spring.profiles.active=mobile",
    "--server.port=8081",
    "--server.address=0.0.0.0",
    "--server.servlet.context-path=/inventario"
)

Write-Host "Iniciando servidor na porta 8081..." -ForegroundColor Green
Write-Host "Pressione Ctrl+C para parar." -ForegroundColor Yellow
Write-Host ""

$allArgs = $javaOpts + @("-jar", "mobile-server.jar") + $serverOpts
& java $allArgs
