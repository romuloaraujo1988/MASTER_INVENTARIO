@echo off
REM ============================================================
REM  Deploy do Servidor SIHCP - Substituicao segura do JAR
REM  Uso: rodar ESTE arquivo NA MAQUINA SERVIDOR (10.14.150.14)
REM  Coloque este .bat na MESMA pasta do sihcp-server.jar novo.
REM ============================================================
setlocal enabledelayedexpansion

echo.
echo ============================================================
echo   DEPLOY SERVIDOR SIHCP - Monitor ao vivo (WebSocket)
echo ============================================================
echo.

REM --- 1. Encerrar processo Java na porta 8081 ---
echo [1/4] Procurando processo na porta 8081...
set "PID="
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
    set "PID=%%a"
)

if defined PID (
    echo     Processo encontrado: PID !PID! - encerrando...
    taskkill /F /PID !PID! >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        echo     [OK] Processo !PID! encerrado.
    ) else (
        echo     [AVISO] Nao foi possivel encerrar PID !PID! ^(ja parou?^).
    )
    REM Aguardar a porta liberar
    timeout /t 3 /nobreak >nul
) else (
    echo     Nenhum processo ativo na porta 8081.
)
echo.

REM --- 2. Validar presenca do JAR novo ---
echo [2/4] Verificando JAR...
if not exist "%~dp0sihcp-server.jar" (
    echo     [ERRO] sihcp-server.jar nao encontrado nesta pasta!
    echo     Copie o JAR novo para: %~dp0
    pause
    exit /b 1
)
for %%F in ("%~dp0sihcp-server.jar") do (
    echo     JAR: %%~nxF
    echo     Tamanho: %%~zF bytes
    echo     Data: %%~tF
)
echo.

REM --- 3. Verificar Java ---
echo [3/4] Verificando Java...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo     [ERRO] Java nao encontrado no PATH!
    pause
    exit /b 1
)
echo     [OK] Java disponivel.
echo.

REM --- 4. Iniciar o servidor ---
echo [4/4] Iniciando servidor (profile mobile, porta 8081)...
echo     Log: logs\mobile-server.log
echo     Para parar: feche esta janela ou Ctrl+C
echo.
echo ============================================================
echo   Procure no log de startup por:
echo     "WsSci registrado manualmente" OU "ServerContainer JSR-356 ja presente"
echo     "WebSocket nativo registrado: /ws-coletas-live"
echo   E confirme que NAO aparece:
echo     "No 'jakarta.websocket.server.ServerContainer'"
echo ============================================================
echo.

set JAVA_OPTS=-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8
set JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=mobile

java %JAVA_OPTS% -jar "%~dp0sihcp-server.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Servidor encerrou com erro. Verifique logs\mobile-server.log
    pause
    exit /b 1
)

pause
