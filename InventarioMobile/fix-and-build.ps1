# Script para corrigir problemas de build e gerar APK funcional
Write-Host "Corrigindo problemas de build do aplicativo mobile..." -ForegroundColor Yellow

# Parar processos Java que podem estar bloqueando arquivos
Write-Host "Parando processos Java..." -ForegroundColor Cyan
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

# Aguardar um pouco
Start-Sleep -Seconds 3

# Limpar diretórios de build com força máxima
Write-Host "Limpando diretorios de build..." -ForegroundColor Cyan
$buildDirs = @(
    ".\app\build",
    ".\.gradle",
    ".\build"
)

foreach ($dir in $buildDirs) {
    if (Test-Path $dir) {
        Write-Host "  Removendo: $dir"
        Remove-Item $dir -Recurse -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

# Verificar se Gradle Daemon está rodando e parar
Write-Host "Parando Gradle Daemon..." -ForegroundColor Cyan
& .\gradlew --stop

# Aguardar
Start-Sleep -Seconds 2

# Limpar cache do Gradle
Write-Host "Limpando cache do Gradle..." -ForegroundColor Cyan
& .\gradlew clean --no-daemon

# Gerar APK debug com configurações otimizadas
Write-Host "Gerando novo APK debug..." -ForegroundColor Green
& .\gradlew assembleDebug --no-daemon --stacktrace

# Verificar se o APK foi gerado
$apkPath = ".\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkInfo = Get-Item $apkPath
    Write-Host "APK gerado com sucesso!" -ForegroundColor Green
    Write-Host "Localizacao: $($apkInfo.FullName)" -ForegroundColor White
    Write-Host "Tamanho: $([math]::Round($apkInfo.Length / 1MB, 2)) MB" -ForegroundColor White
    Write-Host "Data: $($apkInfo.LastWriteTime)" -ForegroundColor White
    
    # Verificar integridade do APK
    Write-Host "Verificando integridade do APK..." -ForegroundColor Cyan
    try {
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        $zip = [System.IO.Compression.ZipFile]::OpenRead($apkInfo.FullName)
        $hasManifest = $zip.Entries | Where-Object { $_.Name -eq "AndroidManifest.xml" }
        $hasClasses = $zip.Entries | Where-Object { $_.Name -eq "classes.dex" }
        $zip.Dispose()
        
        if ($hasManifest -and $hasClasses) {
            Write-Host "APK esta integro e pronto para instalacao!" -ForegroundColor Green
        } else {
            Write-Host "APK pode estar corrompido" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "Erro ao verificar APK: $($_.Exception.Message)" -ForegroundColor Yellow
    }
} else {
    Write-Host "Falha ao gerar APK" -ForegroundColor Red
    Write-Host "Verifique os logs acima para mais detalhes" -ForegroundColor Yellow
}

Write-Host "Script concluido!" -ForegroundColor Magenta