#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Script otimizado para build do SIHCP Mobile

.DESCRIPTION
    Este script realiza um build limpo e otimizado do aplicativo Android,
    incluindo verificações de integridade e geração de APK.

.PARAMETER BuildType
    Tipo de build: Debug ou Release (padrão: Debug)

.PARAMETER Clean
    Se deve fazer clean antes do build (padrão: true)

.EXAMPLE
    .\build-optimized.ps1 -BuildType Debug -Clean $true
#>

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("Debug", "Release")]
    [string]$BuildType = "Debug",
    
    [Parameter(Mandatory=$false)]
    [bool]$Clean = $true
)

# Cores para output
$Red = "`e[31m"
$Green = "`e[32m"
$Yellow = "`e[33m"
$Blue = "`e[34m"
$Reset = "`e[0m"

function Write-ColorOutput {
    param([string]$Message, [string]$Color = $Reset)
    Write-Host "$Color$Message$Reset"
}

function Write-Step {
    param([string]$Message)
    Write-ColorOutput "🔄 $Message" $Blue
}

function Write-Success {
    param([string]$Message)
    Write-ColorOutput "✅ $Message" $Green
}

function Write-Error {
    param([string]$Message)
    Write-ColorOutput "❌ $Message" $Red
}

function Write-Warning {
    param([string]$Message)
    Write-ColorOutput "⚠️  $Message" $Yellow
}

# Verificar se estamos no diretório correto
if (-not (Test-Path "app/build.gradle")) {
    Write-Error "Erro: Execute este script no diretório raiz do projeto Android"
    exit 1
}

Write-ColorOutput "🚀 SIHCP Mobile - Build Otimizado" $Blue
Write-ColorOutput "=================================" $Blue
Write-Host ""

# Verificar Java/JDK
Write-Step "Verificando Java/JDK..."
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Success "Java encontrado: $javaVersion"
} catch {
    Write-Error "Java não encontrado. Instale o JDK 11 ou superior."
    exit 1
}

# Verificar Gradle
Write-Step "Verificando Gradle..."
if (Test-Path "gradlew.bat") {
    Write-Success "Gradle Wrapper encontrado"
} else {
    Write-Error "Gradle Wrapper não encontrado"
    exit 1
}

# Clean se solicitado
if ($Clean) {
    Write-Step "Limpando projeto..."
    try {
        & .\gradlew.bat clean
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Projeto limpo com sucesso"
        } else {
            Write-Error "Erro durante limpeza"
            exit 1
        }
    } catch {
        Write-Error "Erro ao executar gradle clean: $_"
        exit 1
    }
}

# Verificar dependências
Write-Step "Verificando dependências..."
try {
    & .\gradlew.bat dependencies --configuration implementation | Out-Null
    Write-Success "Dependências verificadas"
} catch {
    Write-Warning "Aviso: Não foi possível verificar todas as dependências"
}

# Build
$buildTask = if ($BuildType -eq "Release") { "assembleRelease" } else { "assembleDebug" }
Write-Step "Executando build $BuildType..."

try {
    $buildStart = Get-Date
    & .\gradlew.bat $buildTask
    $buildEnd = Get-Date
    $buildTime = ($buildEnd - $buildStart).TotalSeconds
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Build concluído com sucesso em $([math]::Round($buildTime, 2)) segundos"
    } else {
        Write-Error "Erro durante o build"
        exit 1
    }
} catch {
    Write-Error "Erro ao executar build: $_"
    exit 1
}

# Verificar APK gerado
$apkPath = if ($BuildType -eq "Release") {
    "app/build/outputs/apk/release/app-release.apk"
} else {
    "app/build/outputs/apk/debug/app-debug.apk"
}

if (Test-Path $apkPath) {
    $apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
    Write-Success "APK gerado: $apkPath ($apkSize MB)"
    
    # Informações do APK
    Write-Step "Informações do APK:"
    Write-Host "  📁 Localização: $apkPath"
    Write-Host "  📏 Tamanho: $apkSize MB"
    Write-Host "  🏷️  Tipo: $BuildType"
    Write-Host "  📅 Data: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')"
} else {
    Write-Warning "APK não encontrado no local esperado"
}

# Executar testes unitários (opcional)
$runTests = Read-Host "Deseja executar testes unitários? (s/N)"
if ($runTests -eq "s" -or $runTests -eq "S") {
    Write-Step "Executando testes unitários..."
    try {
        & .\gradlew.bat test
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Testes executados com sucesso"
        } else {
            Write-Warning "Alguns testes falharam"
        }
    } catch {
        Write-Warning "Erro ao executar testes: $_"
    }
}

Write-Host ""
Write-ColorOutput "🎉 Build Concluído!" $Green
Write-ColorOutput "==================" $Green

# Sugestões pós-build
Write-Host ""
Write-ColorOutput "📋 Próximos passos:" $Yellow
Write-Host "  • Para instalar no dispositivo: .\gradlew.bat install$BuildType"
Write-Host "  • Para gerar bundle: .\gradlew.bat bundle$BuildType"
Write-Host "  • Para executar no emulador: Abra o Android Studio"

if ($BuildType -eq "Release") {
    Write-Host ""
    Write-ColorOutput "⚠️  Lembrete para Release:" $Yellow
    Write-Host "  • Verifique se o APK está assinado corretamente"
    Write-Host "  • Teste em dispositivos reais"
    Write-Host "  • Valide todas as funcionalidades"
}

Write-Host ""
Write-ColorOutput "✨ Script concluído com sucesso!" $Green