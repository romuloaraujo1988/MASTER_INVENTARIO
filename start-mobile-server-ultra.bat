@echo off
REM =====================================================
REM Script de Inicialização ULTRA OTIMIZADO - Servidor Mobile
REM Evita travamentos e otimiza uso de memória
REM =====================================================

echo ========================================
echo  Servidor Mobile - Modo ULTRA OTIMIZADO
echo  Anti-Travamento Ativado
echo ========================================
echo.

REM Verificar se Java está instalado
java -version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Java nao encontrado!
    pause
    exit /b 1
)

REM Matar processos anteriores na porta 8081
echo [1/4] Liberando porta 8081...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    echo Matando processo PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 3 /nobreak >nul

REM Limpar cache do Maven (opcional - descomente se necessário)
REM echo [2/4] Limpando cache...
REM rmdir /s /q target\classes 2>nul

echo [2/4] Verificando conexao com banco de dados...
REM Testar conexão PostgreSQL
powershell -Command "Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue | Select-Object -ExpandProperty TcpTestSucceeded" >nul 2>&1
if errorlevel 1 (
    echo AVISO: PostgreSQL pode nao estar rodando na porta 5432
    echo Verifique se o banco de dados esta ativo.
    echo.
)

REM =====================================================
REM CONFIGURAÇÕES JVM ANTI-TRAVAMENTO
REM =====================================================
echo [3/4] Configurando JVM anti-travamento...

REM Memória controlada
set JAVA_OPTS=-Xms128m -Xmx384m

REM G1GC com pausas curtas
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=50
set JAVA_OPTS=%JAVA_OPTS% -XX:G1HeapRegionSize=2m
set JAVA_OPTS=%JAVA_OPTS% -XX:InitiatingHeapOccupancyPercent=35

REM Otimizações de memória
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+ParallelRefProcEnabled
set JAVA_OPTS=%JAVA_OPTS% -XX:+DisableExplicitGC

REM CRÍTICO: Evitar travamentos
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseGCOverheadLimit
set JAVA_OPTS=%JAVA_OPTS% -XX:GCTimeLimit=90
set JAVA_OPTS=%JAVA_OPTS% -XX:GCHeapFreeLimit=10

REM Desabilitar features que consomem recursos
set JAVA_OPTS=%JAVA_OPTS% -Dcom.sun.management.jmxremote=false
set JAVA_OPTS=%JAVA_OPTS% -Djava.awt.headless=true
set JAVA_OPTS=%JAVA_OPTS% -Dspring.jmx.enabled=false

REM Timeouts mais agressivos
set JAVA_OPTS=%JAVA_OPTS% -Dsun.net.client.defaultConnectTimeout=10000
set JAVA_OPTS=%JAVA_OPTS% -Dsun.net.client.defaultReadTimeout=30000

REM Desabilitar verificação de classes (startup mais rápido)
set JAVA_OPTS=%JAVA_OPTS% -noverify
set JAVA_OPTS=%JAVA_OPTS% -XX:TieredStopAtLevel=1

echo.
echo    Memoria: 128MB - 384MB
echo    GC: G1GC com pausas de 50ms
echo    Modo: Anti-travamento ativado
echo.

echo [4/4] Iniciando servidor...
echo.
echo ========================================
echo  Servidor: http://localhost:8081/inventario
echo  Swagger:  http://localhost:8081/inventario/swagger-ui.html
echo  Health:   http://localhost:8081/inventario/actuator/health
echo ========================================
echo.
echo Pressione Ctrl+C para parar
echo.

REM Iniciar com Maven
call mvnw.cmd spring-boot:run ^
    -Dspring-boot.run.profiles=mobile ^
    -Dspring-boot.run.jvmArguments="%JAVA_OPTS%" ^
    -DskipTests

pause
