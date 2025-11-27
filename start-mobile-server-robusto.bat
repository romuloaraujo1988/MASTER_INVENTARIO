@echo off
REM =====================================================
REM Servidor Mobile ROBUSTO E ESTÁVEL
REM Funciona bem em máquinas modestas (4GB RAM, 2 cores)
REM =====================================================

echo.
echo ========================================
echo   SERVIDOR MOBILE - MODO ROBUSTO
echo   Otimizado para estabilidade
echo ========================================
echo.

REM Verificar Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Java nao encontrado!
    pause
    exit /b 1
)

REM Liberar porta
echo [1/3] Liberando porta 8081...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

REM =====================================================
REM JVM ROBUSTA - Configurações para estabilidade
REM =====================================================
echo [2/3] Configurando JVM robusta...

REM Memória: 256MB inicial, 768MB máximo (bom para 4GB RAM)
set JAVA_OPTS=-Xms256m -Xmx768m

REM G1GC - Melhor para aplicações web
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=200
set JAVA_OPTS=%JAVA_OPTS% -XX:G1HeapRegionSize=4m

REM Otimizações de memória
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+ParallelRefProcEnabled

REM Metaspace controlado
set JAVA_OPTS=%JAVA_OPTS% -XX:MetaspaceSize=96m
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxMetaspaceSize=256m

REM Headless (sem interface gráfica)
set JAVA_OPTS=%JAVA_OPTS% -Djava.awt.headless=true

REM Encoding UTF-8
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8

echo.
echo    Memoria: 256MB - 768MB
echo    GC: G1GC (pausas de 200ms)
echo    Perfil: mobile-robusto
echo.

echo [3/3] Iniciando servidor...
echo.
echo ========================================
echo   URL: http://localhost:8081/inventario
echo   Health: http://localhost:8081/inventario/actuator/health
echo ========================================
echo.
echo Pressione Ctrl+C para parar
echo.

REM Iniciar com perfil robusto
call mvnw.cmd spring-boot:run ^
    -Dspring-boot.run.profiles=mobile-robusto ^
    -Dspring-boot.run.jvmArguments="%JAVA_OPTS%" ^
    -DskipTests

pause
