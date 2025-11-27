@echo off
REM =====================================================
REM Script com WATCHDOG - Reinicia automaticamente se travar
REM =====================================================

echo ========================================
echo  Servidor Mobile com WATCHDOG
echo  Reinicia automaticamente se travar
echo ========================================
echo.

:START
echo [%date% %time%] Iniciando servidor...

REM Matar processos anteriores
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

REM Configurações JVM anti-travamento
set JAVA_OPTS=-Xms128m -Xmx384m
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=50
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+DisableExplicitGC
set JAVA_OPTS=%JAVA_OPTS% -Dspring.jmx.enabled=false
set JAVA_OPTS=%JAVA_OPTS% -Djava.awt.headless=true
set JAVA_OPTS=%JAVA_OPTS% -noverify
set JAVA_OPTS=%JAVA_OPTS% -XX:TieredStopAtLevel=1

REM Iniciar servidor em background e capturar PID
start /B cmd /c "mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mobile-ultra -Dspring-boot.run.jvmArguments="%JAVA_OPTS%" -DskipTests > logs\server-output.log 2>&1"

REM Aguardar servidor iniciar
echo Aguardando servidor iniciar...
timeout /t 30 /nobreak >nul

:CHECK_LOOP
REM Verificar se servidor está respondendo
curl -s -o nul -w "%%{http_code}" http://localhost:8081/inventario/actuator/health > temp_status.txt 2>nul
set /p STATUS=<temp_status.txt
del temp_status.txt 2>nul

if "%STATUS%"=="200" (
    REM Servidor OK, verificar novamente em 30 segundos
    timeout /t 30 /nobreak >nul
    goto CHECK_LOOP
) else (
    echo [%date% %time%] ALERTA: Servidor nao respondendo! Status: %STATUS%
    echo Reiniciando em 10 segundos...
    timeout /t 10 /nobreak >nul
    goto START
)
