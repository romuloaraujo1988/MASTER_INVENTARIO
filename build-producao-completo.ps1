# Build de Produção Completo
# Gera JAR Desktop + JAR Servidor Mobile (não monolíticos)
# Compartilham a mesma pasta lib/

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  BUILD DE PRODUCAO - Sistema de Inventario                     ║" -ForegroundColor Cyan
Write-Host "║  JAR Desktop + JAR Servidor Mobile (Thin JARs)                 ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Verificar Maven
$mvnCmd = "mvn"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    if (Test-Path ".\mvnw.cmd") {
        $mvnCmd = ".\mvnw.cmd"
        Write-Host "Usando Maven Wrapper" -ForegroundColor Yellow
    } else {
        Write-Host "[ERRO] Maven nao encontrado!" -ForegroundColor Red
        exit 1
    }
}

# Diretório de saída
$outputDir = "dist\producao"
if (Test-Path $outputDir) {
    Remove-Item -Path $outputDir -Recurse -Force
}
New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
New-Item -ItemType Directory -Path "$outputDir\lib" -Force | Out-Null
New-Item -ItemType Directory -Path "$outputDir\logs" -Force | Out-Null

Write-Host "[1/5] Limpando e compilando projeto..." -ForegroundColor Yellow
& $mvnCmd clean compile -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERRO] Falha na compilacao!" -ForegroundColor Red
    exit 1
}

Write-Host "[2/5] Gerando JAR com profile thin-jar..." -ForegroundColor Yellow
& $mvnCmd package -DskipTests -P thin-jar -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERRO] Falha ao gerar JAR!" -ForegroundColor Red
    exit 1
}

Write-Host "[3/5] Copiando dependencias para lib/..." -ForegroundColor Yellow
Copy-Item "target\lib\*" "$outputDir\lib\" -Force

Write-Host "[4/5] Criando JARs separados..." -ForegroundColor Yellow

# Copiar JAR base
$baseJar = Get-ChildItem -Path "target\mobile-server" -Filter "sistema-inventario*.jar" | Select-Object -First 1
if ($baseJar) {
    # JAR Desktop
    Copy-Item $baseJar.FullName "$outputDir\sihcp-desktop.jar" -Force
    Write-Host "  - sihcp-desktop.jar criado" -ForegroundColor Green
    
    # JAR Servidor Mobile (mesmo JAR, classe principal diferente no script)
    Copy-Item $baseJar.FullName "$outputDir\mobile-server.jar" -Force
    Write-Host "  - mobile-server.jar criado" -ForegroundColor Green
} else {
    Write-Host "[ERRO] JAR base nao encontrado!" -ForegroundColor Red
    exit 1
}

# Copiar configurações
Copy-Item "target\mobile-server\application*.properties" $outputDir -Force -ErrorAction SilentlyContinue
Copy-Item "target\mobile-server\application*.yml" $outputDir -Force -ErrorAction SilentlyContinue

Write-Host "[5/5] Criando scripts de execucao..." -ForegroundColor Yellow

# Script Desktop - BAT
@'
@echo off
setlocal

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  SIHCP - Sistema de Historico e Coleta Patrimonial             ║
echo ║  Aplicacao Desktop                                             ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

set JAVA_OPTS=-Xms512m -Xmx2g
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxMetaspaceSize=256m
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=200
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication

echo Iniciando aplicacao desktop...
java %JAVA_OPTS% -cp "sihcp-desktop.jar;lib\*" com.inventario.SistemaInventarioApplication
pause
'@ | Set-Content "$outputDir\iniciar-desktop.bat" -Encoding ASCII

# Script Desktop - PS1
@'
# SIHCP - Aplicacao Desktop
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  SIHCP - Sistema de Historico e Coleta Patrimonial             ║" -ForegroundColor Cyan
Write-Host "║  Aplicacao Desktop                                             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

$javaOpts = @("-Xms512m", "-Xmx2g", "-XX:MaxMetaspaceSize=256m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-XX:+UseStringDeduplication")
Write-Host "Iniciando aplicacao desktop..." -ForegroundColor Green
& java $javaOpts -cp "sihcp-desktop.jar;lib\*" com.inventario.SistemaInventarioApplication
'@ | Set-Content "$outputDir\iniciar-desktop.ps1" -Encoding UTF8

# Script Servidor Mobile - BAT
@'
@echo off
setlocal

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  Servidor Mobile - Sistema de Inventario                       ║
echo ║  Processo Independente (Porta 8081)                            ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

set JAVA_OPTS=-Xms256m -Xmx1g
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxMetaspaceSize=192m
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=100
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JAVA_OPTS=%JAVA_OPTS% -XX:HeapDumpPath=logs/

set SERVER_OPTS=--spring.profiles.active=mobile
set SERVER_OPTS=%SERVER_OPTS% --server.port=8081
set SERVER_OPTS=%SERVER_OPTS% --server.address=0.0.0.0
set SERVER_OPTS=%SERVER_OPTS% --server.servlet.context-path=/inventario

echo Iniciando servidor mobile na porta 8081...
echo Pressione Ctrl+C para parar.
java %JAVA_OPTS% -cp "mobile-server.jar;lib\*" com.inventario.MobileApiApplication %SERVER_OPTS%
pause
'@ | Set-Content "$outputDir\iniciar-servidor-mobile.bat" -Encoding ASCII

# Script Servidor Mobile - PS1
@'
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
'@ | Set-Content "$outputDir\iniciar-servidor-mobile.ps1" -Encoding UTF8

# README
$readmeContent = @"
SISTEMA DE INVENTARIO - PRODUCAO
================================

Este diretorio contem o sistema completo com JARs separados (thin JARs).

ESTRUTURA:
  * sihcp-desktop.jar: Aplicacao desktop Swing (~1.6 MB)
  * mobile-server.jar: Servidor mobile API (~1.6 MB)
  * lib/: Dependencias compartilhadas (~134 MB)
  * logs/: Diretorio de logs

EXECUCAO:

1. APENAS DESKTOP:
   .\iniciar-desktop.ps1
   ou iniciar-desktop.bat

2. APENAS SERVIDOR MOBILE:
   .\iniciar-servidor-mobile.ps1
   ou iniciar-servidor-mobile.bat

PORTAS:
  * Desktop: Aplicacao local (sem porta)
  * Servidor Mobile: http://localhost:8081/inventario

MEMORIA:
  * Desktop: 512MB - 2GB
  * Servidor Mobile: 256MB - 1GB (processo separado)

VANTAGENS DOS THIN JARS:
  * JARs pequenos (~1.6 MB cada)
  * Dependencias compartilhadas
  * Atualizacoes rapidas
  * Processos independentes
  * Melhor controle de memoria

REQUISITOS:
  * Java 21 ou superior
  * PostgreSQL configurado
"@
$readmeContent | Set-Content "$outputDir\README.txt" -Encoding UTF8

# Estatísticas finais
Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  BUILD CONCLUIDO COM SUCESSO!                                  ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

$desktopSize = [math]::Round((Get-Item "$outputDir\sihcp-desktop.jar").Length/1MB, 2)
$mobileSize = [math]::Round((Get-Item "$outputDir\mobile-server.jar").Length/1MB, 2)
$libCount = (Get-ChildItem "$outputDir\lib" | Measure-Object).Count
$libSize = [math]::Round((Get-ChildItem "$outputDir\lib" | Measure-Object -Property Length -Sum).Sum/1MB, 2)

Write-Host "Arquivos gerados em: dist\producao\" -ForegroundColor Cyan
Write-Host ""
Write-Host "  sihcp-desktop.jar:  $desktopSize MB" -ForegroundColor White
Write-Host "  mobile-server.jar:  $mobileSize MB" -ForegroundColor White
Write-Host "  lib/ ($libCount JARs):      $libSize MB" -ForegroundColor White
Write-Host "  ─────────────────────────────" -ForegroundColor Gray
Write-Host "  Total:              $([math]::Round($desktopSize + $mobileSize + $libSize, 2)) MB" -ForegroundColor Yellow
Write-Host ""
Write-Host "Para executar:" -ForegroundColor Cyan
Write-Host "  cd dist\producao" -ForegroundColor White
Write-Host "  .\iniciar-desktop.ps1          # Apenas desktop" -ForegroundColor White
Write-Host "  .\iniciar-servidor-mobile.ps1  # Apenas servidor" -ForegroundColor White
Write-Host ""
