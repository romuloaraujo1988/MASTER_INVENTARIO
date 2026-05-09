# Script de Build para Produção - SIHCP Modular
# Versão: 2.7.0
# Data: 01/05/2026

param(
    [switch]$SkipTests = $false,
    [switch]$Desktop = $false,
    [switch]$Server = $false,
    [switch]$All = $true
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SIHCP - Build para Produção Modular  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se Maven está disponível
if (-not (Test-Path ".\mvnw.cmd")) {
    Write-Host "ERRO: mvnw.cmd não encontrado!" -ForegroundColor Red
    exit 1
}

# Timestamp
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Write-Host "Timestamp: $timestamp" -ForegroundColor Yellow
Write-Host ""

# Determinar o que buildar
$buildCore = $All
$buildDesktop = $All -or $Desktop
$buildServer = $All -or $Server

# Parâmetros Maven
$mvnParams = "clean package"
if ($SkipTests) {
    $mvnParams += " -DskipTests"
}

# ========================================
# Fase 1: Build do Core (sempre necessário)
# ========================================
if ($buildCore -or $buildDesktop -or $buildServer) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Fase 1: Building sihcp-core..." -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    Set-Location "sihcp-core"
    & ..\mvnw.cmd $mvnParams.Split()
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERRO: Build do sihcp-core falhou!" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
    
    Write-Host "✓ sihcp-core build concluído com sucesso!" -ForegroundColor Green
    Write-Host ""
    Set-Location ..
}

# ========================================
# Fase 2: Build do Desktop
# ========================================
if ($buildDesktop) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Fase 2: Building sihcp-desktop..." -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    Set-Location "sihcp-desktop"
    & ..\mvnw.cmd $mvnParams.Split()
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERRO: Build do sihcp-desktop falhou!" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
    
    Write-Host "✓ sihcp-desktop build concluído com sucesso!" -ForegroundColor Green
    Write-Host ""
    Set-Location ..
}

# ========================================
# Fase 3: Build do Server
# ========================================
if ($buildServer) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Fase 3: Building sihcp-server..." -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    Set-Location "sihcp-server"
    & ..\mvnw.cmd $mvnParams.Split()
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERRO: Build do sihcp-server falhou!" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
    
    Write-Host "✓ sihcp-server build concluído com sucesso!" -ForegroundColor Green
    Write-Host ""
    Set-Location ..
}

# ========================================
# Fase 4: Criar estrutura de distribuição
# ========================================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fase 4: Criando pacotes de distribuição..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$distDir = "dist\sihcp-$timestamp"
New-Item -ItemType Directory -Force -Path $distDir | Out-Null

# Desktop
if ($buildDesktop) {
    Write-Host "Empacotando Desktop..." -ForegroundColor Yellow
    
    $desktopDir = "$distDir\desktop"
    New-Item -ItemType Directory -Force -Path $desktopDir | Out-Null
    
    # Copiar JAR principal
    Copy-Item "sihcp-desktop\target\sihcp-desktop-2.7.0.jar" "$desktopDir\"
    
    # Copiar dependências
    Copy-Item "sihcp-desktop\target\lib" "$desktopDir\" -Recurse
    
    # Criar script de inicialização Windows
    $batContent = @(
        "@echo off",
        "echo ========================================",
        "echo   SIHCP Desktop - Sistema de Inventario",
        "echo ========================================",
        "echo.",
        "",
        "java -Xms512m -Xmx2g -jar sihcp-desktop-2.7.0.jar",
        "",
        "if errorlevel 1 (",
        "    echo.",
        "    echo ERRO: Falha ao iniciar a aplicacao!",
        "    pause",
        ")"
    )
    $batContent | Set-Content -Path "$desktopDir\iniciar-desktop.bat" -Encoding ASCII
    
    # Criar script de inicialização Linux/Mac
    $shContent = @(
        "#!/bin/bash",
        "echo \"========================================\"",
        "echo \"  SIHCP Desktop - Sistema de Inventario\"",
        "echo \"========================================\"",
        "echo \"\"",
        "",
        "java -Xms512m -Xmx2g -jar sihcp-desktop-2.7.0.jar",
        "",
        "if [ $? -ne 0 ]; then",
        "    echo \"\"",
        "    echo \"ERRO: Falha ao iniciar a aplicacao!\"",
        "    read -p \"Pressione Enter para continuar...\"",
        "fi"
    )
    $shContent | Set-Content -Path "$desktopDir\iniciar-desktop.sh" -Encoding UTF8
    
    # Criar README
    $readmeDesktop = @(
        "# SIHCP Desktop - Aplicação Swing",
        "",
        "## Requisitos",
        "- Java 17 ou superior",
        "- PostgreSQL 12+",
        "",
        "## Instalação",
        "",
        "1. Extrair todos os arquivos para um diretório",
        "2. Configurar banco de dados em ~/configuracao_banco.json",
        "3. Executar o script de inicialização",
        "",
        "## Execução",
        "",
        "### Windows",
        "```",
        "iniciar-desktop.bat",
        "```",
        "",
        "### Linux/Mac",
        "```",
        "chmod +x iniciar-desktop.sh",
        "./iniciar-desktop.sh",
        "```",
        "",
        "### Manual",
        "```",
        "java -jar sihcp-desktop-2.7.0.jar",
        "```",
        "",
        "## Estrutura",
        "```",
        "desktop/",
        "├── sihcp-desktop-2.7.0.jar    # JAR principal",
        "├── lib/                        # Dependências",
        "├── iniciar-desktop.bat         # Script Windows",
        "└── iniciar-desktop.sh          # Script Linux/Mac",
        "```",
        "",
        "## Versão",
        "2.7.0 - Build: $timestamp"
    )
    $readmeDesktop | Set-Content -Path "$desktopDir\README.md" -Encoding UTF8
    
    Write-Host "✓ Desktop empacotado em: $desktopDir" -ForegroundColor Green
}

# Server
if ($buildServer) {
    Write-Host "Empacotando Server..." -ForegroundColor Yellow
    
    $serverDir = "$distDir\server"
    New-Item -ItemType Directory -Force -Path $serverDir | Out-Null
    
    # Copiar JAR (fat JAR)
    Copy-Item "sihcp-server\target\sihcp-server-2.7.0.jar" "$serverDir\"
    
    # Copiar arquivos de configuração
    Copy-Item "sihcp-server\src\main\resources\application.properties" "$serverDir\"
    Copy-Item "sihcp-server\src\main\resources\application-mobile.properties" "$serverDir\"
    
    # Criar script de inicialização Windows
    $serverBat = @(
        "@echo off",
        "echo ========================================",
        "echo   SIHCP Server - API REST Mobile",
        "echo ========================================",
        "echo.",
        "",
        "set JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200",
        "set SPRING_OPTS=--spring.profiles.active=mobile --server.port=8081",
        "",
        "java %JAVA_OPTS% -jar sihcp-server-2.7.0.jar %SPRING_OPTS%",
        "",
        "if errorlevel 1 (",
        "    echo.",
        "    echo ERRO: Falha ao iniciar o servidor!",
        "    pause",
        ")"
    )
    $serverBat | Set-Content -Path "$serverDir\iniciar-server.bat" -Encoding ASCII
    
    # Criar script de inicialização Linux/Mac
    $serverSh = @(
        "#!/bin/bash",
        "echo \"========================================\"",
        "echo \"  SIHCP Server - API REST Mobile\"",
        "echo \"========================================\"",
        "echo \"\"",
        "",
        "JAVA_OPTS=\"-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200\"",
        "SPRING_OPTS=\"--spring.profiles.active=mobile --server.port=8081\"",
        "",
        "java $JAVA_OPTS -jar sihcp-server-2.7.0.jar $SPRING_OPTS",
        "",
        "if [ $? -ne 0 ]; then",
        "    echo \"\"",
        "    echo \"ERRO: Falha ao iniciar o servidor!\"",
        "    read -p \"Pressione Enter para continuar...\"",
        "fi"
    )
    $serverSh | Set-Content -Path "$serverDir\iniciar-server.sh" -Encoding UTF8
    
    # Criar script de instalação como serviço (systemd)
    $serviceContent = @(
        "[Unit]",
        "Description=SIHCP Server - API REST Mobile",
        "After=network.target postgresql.service",
        "",
        "[Service]",
        "Type=simple",
        "User=sihcp",
        "WorkingDirectory=/opt/sihcp-server",
        "ExecStart=/usr/bin/java -Xms512m -Xmx2g -XX:+UseG1GC -jar /opt/sihcp-server/sihcp-server-2.7.0.jar --spring.profiles.active=mobile --server.port=8081",
        "Restart=on-failure",
        "RestartSec=10",
        "StandardOutput=journal",
        "StandardError=journal",
        "",
        "[Install]",
        "WantedBy=multi-user.target"
    )
    $serviceContent | Set-Content -Path "$serverDir\sihcp-server.service" -Encoding UTF8
    
    # Criar README
    $readmeServer = @(
        "# SIHCP Server - API REST Mobile",
        "",
        "## Requisitos",
        "- Java 17 ou superior",
        "- PostgreSQL 12+",
        "",
        "## Instalação",
        "",
        "1. Extrair todos os arquivos para um diretório",
        "2. Configurar banco de dados em ~/configuracao_banco.json",
        "3. Ajustar application-mobile.properties se necessário",
        "4. Executar o script de inicialização",
        "",
        "## Execução",
        "",
        "### Windows",
        "```",
        "iniciar-server.bat",
        "```",
        "",
        "### Linux/Mac",
        "```",
        "chmod +x iniciar-server.sh",
        "./iniciar-server.sh",
        "```",
        "",
        "### Manual",
        "```",
        "java -jar sihcp-server-2.7.0.jar --spring.profiles.active=mobile --server.port=8081",
        "```",
        "",
        "## Instalação como Serviço (Linux)",
        "",
        "```bash",
        "# Copiar arquivos para /opt",
        "sudo mkdir -p /opt/sihcp-server",
        "sudo cp sihcp-server-2.7.0.jar /opt/sihcp-server/",
        "",
        "# Criar usuário",
        "sudo useradd -r -s /bin/false sihcp",
        "",
        "# Copiar service file",
        "sudo cp sihcp-server.service /etc/systemd/system/",
        "",
        "# Habilitar e iniciar",
        "sudo systemctl daemon-reload",
        "sudo systemctl enable sihcp-server",
        "sudo systemctl start sihcp-server",
        "",
        "# Verificar status",
        "sudo systemctl status sihcp-server",
        "```",
        "",
        "## Endpoints Principais",
        "",
        "- Health Check: http://localhost:8081/api/mobile/health",
        "- Swagger UI: http://localhost:8081/swagger-ui.html",
        "- API Docs: http://localhost:8081/v3/api-docs",
        "",
        "## Estrutura",
        "```",
        "server/",
        "├── sihcp-server-2.7.0.jar           # Fat JAR (todas as deps)",
        "├── application.properties            # Config base",
        "├── application-mobile.properties     # Config mobile",
        "├── iniciar-server.bat                # Script Windows",
        "├── iniciar-server.sh                 # Script Linux/Mac",
        "└── sihcp-server.service              # Systemd service",
        "```",
        "",
        "## Versão",
        "2.7.0 - Build: $timestamp"
    )
    $readmeServer | Set-Content -Path "$serverDir\README.md" -Encoding UTF8
    
    Write-Host "✓ Server empacotado em: $serverDir" -ForegroundColor Green
}

# ========================================
# Fase 5: Criar arquivo ZIP de distribuição
# ========================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fase 5: Criando arquivo ZIP..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$zipFile = "dist\sihcp-$timestamp.zip"

if (Test-Path $zipFile) {
    Remove-Item $zipFile -Force
}

Compress-Archive -Path "$distDir\*" -DestinationPath $zipFile

Write-Host "✓ Arquivo ZIP criado: $zipFile" -ForegroundColor Green

# ========================================
# Resumo Final
# ========================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BUILD CONCLUÍDO COM SUCESSO!         " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Artefatos gerados:" -ForegroundColor Yellow
Write-Host ""

if ($buildDesktop) {
    Write-Host "Desktop:" -ForegroundColor Green
    Write-Host "  - $distDir\desktop\sihcp-desktop-2.7.0.jar"
    Write-Host "  - $distDir\desktop\lib\ (dependências)"
    Write-Host "  - $distDir\desktop\iniciar-desktop.bat"
    Write-Host "  - $distDir\desktop\iniciar-desktop.sh"
    Write-Host ""
}

if ($buildServer) {
    Write-Host "Server:" -ForegroundColor Green
    Write-Host "  - $distDir\server\sihcp-server-2.7.0.jar"
    Write-Host "  - $distDir\server\iniciar-server.bat"
    Write-Host "  - $distDir\server\iniciar-server.sh"
    Write-Host "  - $distDir\server\sihcp-server.service"
    Write-Host ""
}

Write-Host "Pacote de distribuição:" -ForegroundColor Green
Write-Host "  - $zipFile"
Write-Host ""

Write-Host "Para distribuir:" -ForegroundColor Yellow
Write-Host "  1. Enviar o arquivo ZIP para o servidor/cliente"
Write-Host "  2. Extrair o conteúdo"
Write-Host "  3. Seguir as instruções no README.md de cada módulo"
Write-Host ""

Write-Host "Build finalizado em: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')" -ForegroundColor Cyan
