@echo off
REM ============================================================
REM SIHCP - Servidor Mobile (Versão Standalone)
REM ============================================================

setlocal enabledelayedexpansion

REM Diretório atual
set "BASE_DIR=%~dp0"
cd /d "%BASE_DIR%"

echo ============================================================
echo  SIHCP - Iniciando Servidor API Mobile
echo ============================================================
echo.

REM [1/4] Verificar Java
echo [1/4] Verificando Java...
java -version 2>&1 | findstr /i "version" > nul
if errorlevel 1 (
    echo [ERRO] Java nao encontrado. Instale o JDK 17+.
    pause
    exit /b 1
)
echo [OK] Java detectado.

REM [2/4] Verificar Configuração
echo [2/4] Verificando configuracao...
if not exist "configuracao_banco.json" (
    echo [ERRO] Arquivo configuracao_banco.json nao encontrado!
    pause
    exit /b 1
)
echo [OK] Arquivo de configuracao encontrado.

REM [3/4] Ler Porta (padrão 8081)
for /f "usebackq delims=" %%a in (`powershell -NoProfile -Command "try { $j = Get-Content 'configuracao_banco.json' | ConvertFrom-Json; $j.api.porta } catch { '8081' }"`) do set "API_PORT=%%a"
if "%API_PORT%" == "" set "API_PORT=8081"

REM [4/4] Verificar JAR
echo [3/4] Verificando executavel...
if not exist "sihcp-server.jar" (
    echo [ERRO] sihcp-server.jar nao encontrado!
    pause
    exit /b 1
)
echo [OK] Executavel encontrado.

echo.
echo ============================================================
echo  SERVIDOR PRONTO
echo  Porta: %API_PORT%
echo  Pressione Ctrl+C para encerrar.
echo ============================================================
echo.

java -Xms512m -Xmx2g -jar sihcp-server.jar --spring.profiles.active=mobile,prod --server.port=%API_PORT%

pause
