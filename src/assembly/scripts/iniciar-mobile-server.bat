@echo off
REM ============================================
REM Sistema de Inventario - Mobile API Server
REM Versao: 2.0.0
REM ============================================

echo.
echo ============================================
echo   SISTEMA DE INVENTARIO - MOBILE SERVER
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

REM Configurar memoria JVM (servidor precisa de mais memoria)
set JAVA_OPTS=-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200

REM Configurar encoding
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8

REM Configurar logs
set JAVA_OPTS=%JAVA_OPTS% -Dlogging.file.name=logs/mobile-server.log

REM Configurar profile mobile
set JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=mobile

REM Configurar porta (padrao 8080)
set JAVA_OPTS=%JAVA_OPTS% -Dserver.port=8080

echo Iniciando Mobile API Server...
echo Porta: 8080
echo Swagger: http://localhost:8080/inventario/swagger-ui.html
echo.

REM Executar aplicacao
java %JAVA_OPTS% -jar sistema-inventario-2.0.0.jar

if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Falha ao iniciar o servidor!
    echo Verifique os logs em: logs/mobile-server.log
    pause
    exit /b 1
)

pause
