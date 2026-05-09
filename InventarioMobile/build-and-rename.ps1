# Script para compilar e renomear APK do SIHCP
# Versão: 2.16.0

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SIHCP - Build e Renomeação de APK" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Passo 1: Compilar APK Debug
Write-Host "[1/3] Compilando APK Debug..." -ForegroundColor Yellow
& ".\gradlew.bat" assembleDebug --no-daemon

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ Erro na compilação!" -ForegroundColor Red
    Write-Host "Verifique os erros acima e tente novamente." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Compilação concluída com sucesso!" -ForegroundColor Green
Write-Host ""

# Passo 2: Verificar se APK foi gerado
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"

if (-Not (Test-Path $apkPath)) {
    Write-Host "❌ APK não encontrado em: $apkPath" -ForegroundColor Red
    exit 1
}

Write-Host "[2/3] APK encontrado: $apkPath" -ForegroundColor Yellow
Write-Host ""

# Passo 3: Renomear APK
$version = "2.16.0"
$buildNumber = "54"
$newName = "SIHCP-v$version-build$buildNumber-debug.apk"
$newPath = "app\build\outputs\apk\debug\$newName"

Write-Host "[3/3] Renomeando APK para: $newName" -ForegroundColor Yellow

# Remover arquivo antigo se existir
if (Test-Path $newPath) {
    Remove-Item $newPath -Force
}

# Copiar e renomear
Copy-Item $apkPath $newPath

if (Test-Path $newPath) {
    Write-Host "✅ APK renomeado com sucesso!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  APK Pronto!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📱 Arquivo: $newName" -ForegroundColor White
    Write-Host "📂 Localização: $newPath" -ForegroundColor White
    Write-Host ""
    Write-Host "Tamanho: $((Get-Item $newPath).Length / 1MB) MB" -ForegroundColor White
    Write-Host ""
    
    # Abrir pasta no Explorer
    Write-Host "Abrindo pasta no Explorer..." -ForegroundColor Yellow
    explorer.exe "app\build\outputs\apk\debug"
    
} else {
    Write-Host "❌ Erro ao renomear APK!" -ForegroundColor Red
    exit 1
}
