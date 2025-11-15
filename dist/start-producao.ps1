# ========================================
# Script de Inicialização - Modo Produção
# Sistema de Inventário (PowerShell)
# ========================================

Write-Host ""
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  SISTEMA DE INVENTARIO - PRODUCAO"  -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host ""

# Verificar se o Java está instalado
try {
    $javaVersion = java -version 2>&1
    Write-Host "Verificando versao do Java..." -ForegroundColor Yellow
    Write-Host $javaVersion[0] -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Java nao encontrado!" -ForegroundColor Red
    Write-Host "Por favor, instale o Java 21 ou superior." -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Criar diretório de logs se não existir
if (!(Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
    Write-Host "Diretorio de logs criado." -ForegroundColor Green
}

# Definir variáveis de ambiente
$env:JAVA_OPTS = "-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
$env:SPRING_PROFILES_ACTIVE = "prod"

Write-Host ""
Write-Host "Iniciando sistema em modo PRODUCAO..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Configuracoes:" -ForegroundColor Cyan
Write-Host "- Perfil: PRODUCAO" -ForegroundColor White
Write-Host "- Memoria: 512MB - 2GB" -ForegroundColor White
Write-Host "- Porta: 8080" -ForegroundColor White
Write-Host "- Logs: logs/sistema-inventario-prod.log" -ForegroundColor White
Write-Host ""

# Copiar application-prod.properties para a raiz
if (Test-Path "application-prod.properties") {
    Copy-Item "application-prod.properties" ".." -Force
}

# Verificar se o JAR existe
$jarPath = "..\target\sistema-inventario-2.0.0.jar"
if (!(Test-Path $jarPath)) {
    Write-Host "[AVISO] JAR nao encontrado. Compilando..." -ForegroundColor Yellow
    Set-Location ..
    mvn clean package -DskipTests
    Set-Location dist
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERRO] Falha na compilacao!" -ForegroundColor Red
        Read-Host "Pressione Enter para sair"
        exit 1
    }
}

# Iniciar aplicação
Write-Host "Iniciando aplicacao..." -ForegroundColor Green
Write-Host ""
Set-Location ..
java $env:JAVA_OPTS -Dspring.profiles.active=$env:SPRING_PROFILES_ACTIVE -jar target\sistema-inventario-2.0.0.jar
Set-Location dist

# Se a aplicação encerrar, pausar para ver mensagens
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERRO] Aplicacao encerrada com erro!" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
}
