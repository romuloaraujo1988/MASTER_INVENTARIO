# SIHCP - Aplicacao Desktop
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  SIHCP - Sistema de Historico e Coleta Patrimonial             ║" -ForegroundColor Cyan
Write-Host "║  Aplicacao Desktop                                             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

$javaOpts = @("-Xms512m", "-Xmx2g", "-XX:MaxMetaspaceSize=256m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-XX:+UseStringDeduplication")
Write-Host "Iniciando aplicacao desktop..." -ForegroundColor Green
& java $javaOpts -cp "desktop.jar;lib\*" com.inventario.SistemaInventarioApplication
