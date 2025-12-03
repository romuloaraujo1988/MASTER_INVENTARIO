# Servidor Mobile - Sistema de Inventario
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Servidor Mobile - Sistema de Inventario                       ║" -ForegroundColor Cyan
Write-Host "║  Processo Independente (Porta 8081)                            ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

$javaOpts = @(
    "-Xms256m", "-Xmx1g", "-XX:MaxMetaspaceSize=192m",
    "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100",
    "-XX:+UseStringDeduplication",
    "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=logs/"
)
$serverOpts = @(
    "--spring.profiles.active=mobile",
    "--server.port=8081",
    "--server.address=0.0.0.0",
    "--server.servlet.context-path=/inventario"
)

Write-Host "Iniciando servidor mobile na porta 8081..." -ForegroundColor Green
Write-Host "Pressione Ctrl+C para parar." -ForegroundColor Yellow
& java $javaOpts -cp "mobile-server.jar;lib\*" com.inventario.MobileApiApplication $serverOpts
