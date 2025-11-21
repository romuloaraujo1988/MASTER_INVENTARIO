@echo off
REM ============================================
REM Sistema de Inventario - Desktop
REM Versao: 2.0.0
REM ============================================

echo.
echo ============================================
echo   SISTEMA DE INVENTARIO - DESKTOP
echo   Versao 2.0.0
echo ============================================
echo.

REM Verificar Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Java nao encontrado!
    echo Por favor, instale o Java 21 ou superior.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

REM Verificar versao do Java
for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -fullversion 2^>^&1') do set "jver=%%j"
if %jver% LSS 21 (
    echo [ERRO] Java 21 ou superior e necessario!
    echo Versao atual: %jver%
    pause
    exit /b 1
)

echo [OK] Java %jver% detectado
echo.

REM Configurar memoria JVM
set JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200

REM Configurar encoding
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8

REM Configurar logs
set JAVA_OPTS=%JAVA_OPTS% -Dlogging.file.name=logs/sistema-inventario.log

REM Configurar profile
set JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=default

echo Iniciando Sistema de Inventario Desktop...
echo.

REM Executar aplicacao
java %JAVA_OPTS% -jar sistema-inventario-2.0.0.jar

if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Falha ao iniciar o sistema!
    echo Verifique os logs em: logs/sistema-inventario.log
    pause
    exit /b 1
)

pause
