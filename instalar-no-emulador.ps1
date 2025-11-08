# Script PowerShell para instalar SIHCP Mobile no Emulador

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Instalacao no Emulador Android" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location InventarioMobile

Write-Host "Verificando dispositivos conectados..." -ForegroundColor Yellow
& .\gradlew.bat devices
Write-Host ""

Write-Host "Instalando aplicativo no emulador..." -ForegroundColor Yellow
& .\gradlew.bat installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Instalacao concluida com sucesso!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "O aplicativo foi instalado no emulador." -ForegroundColor White
    Write-Host "Abra o emulador e procure por 'SIHCP Mobile'" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "ERRO na instalacao!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifique se:" -ForegroundColor Yellow
    Write-Host "1. O emulador esta em execucao" -ForegroundColor Yellow
    Write-Host "2. O Android Studio esta aberto" -ForegroundColor Yellow
    Write-Host "3. O emulador esta completamente inicializado" -ForegroundColor Yellow
    Write-Host ""
}

Set-Location ..
Read-Host "Pressione Enter para sair"
