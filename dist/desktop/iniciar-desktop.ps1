# SIHCP - Sistema de Historico e Coleta Patrimonial
# Aplicacao Desktop

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  SIHCP - Sistema de Historico e Coleta Patrimonial             ║" -ForegroundColor Cyan
Write-Host "║  Aplicacao Desktop                                             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Configuracoes de memoria para desktop
$javaOpts = @(
    "-Xms512m",
    "-Xmx2g",
    "-XX:MaxMetaspaceSize=256m",
    "-XX:+UseG1GC",
    "-XX:MaxGCPauseMillis=200",
    "-XX:+UseStringDeduplication"
)

Write-Host "Iniciando aplicacao desktop..." -ForegroundColor Green
Write-Host ""

$allArgs = $javaOpts + @("-jar", "sistema-inventario.jar")
& java $allArgs
